package Model;

import java.util.*;

public class RedParada {

    private HashMap<String, LinkedList<Ruta>> rutas;
    private HashMap<String, Parada> lugar;
    private static RedParada instance = null;

    public RedParada() {
        this.rutas = new HashMap<>();
        this.lugar = new HashMap<>();
    }

    public static RedParada getInstance() {
        if (instance == null) {
            instance = new RedParada();
        }
        return instance;
    }

    public HashMap<String, LinkedList<Ruta>> getRutas() { return rutas; }
    public void setRutas(HashMap<String, LinkedList<Ruta>> rutas) { this.rutas = rutas; }
    public HashMap<String, Parada> getLugar() { return lugar; }
    public void setLugar(HashMap<String, Parada> lugar) { this.lugar = lugar; }


    public void agregarRuta(Ruta ruta) {
        String origen = ruta.getOrigen().getNombre();
        rutas.computeIfAbsent(origen, k -> new LinkedList<>()).add(ruta);
    }
    public Parada buscarParadaPorNombre(String origen) {
        return lugar.get(origen);
    }
    public void agregarParada(Parada nuevaParada) {
        String nombre = nuevaParada.getNombre();
        lugar.put(nombre, nuevaParada);
        rutas.putIfAbsent(nombre, new LinkedList<>());
    }
    public boolean existeRutaIgual(Ruta nuevaRuta) { /* ... sin cambios ... */ return false; }

    public ResultadoRuta calcularRutaMasEficiente(String inicio, String fin) {
        return dijkstraGeneral(inicio, fin, "eficiente");
    }


    public ResultadoRuta calcularRutaMenorDistancia(String inicio, String fin) {
        return dijkstraGeneral(inicio, fin, "distancia");
    }


    public ResultadoRuta calcularRutaMenorCosto(String inicio, String fin) {
        if (!lugar.containsKey(inicio) || !lugar.containsKey(fin)) {
            return new ResultadoRuta("El lugar de inicio o fin no existe.");
        }

        int numNodos = lugar.size();
        HashMap<String, Float> costos = new HashMap<>();
        HashMap<String, String> previo = new HashMap<>();
        List<Ruta> allRutas = getAllRutas();

        for (String nodo : lugar.keySet()) {
            costos.put(nodo, Float.MAX_VALUE);
            previo.put(nodo, null);
        }
        costos.put(inicio, 0.0f);

        for (int i = 0; i < numNodos - 1; i++) {
            for (Ruta ruta : allRutas) {
                String origenRuta = ruta.getOrigen().getNombre();
                String destinoRuta = ruta.getDestino().getNombre();
                if (costos.get(origenRuta) != Float.MAX_VALUE && costos.get(origenRuta) + ruta.getCosto() < costos.get(destinoRuta)) {
                    costos.put(destinoRuta, costos.get(origenRuta) + ruta.getCosto());
                    previo.put(destinoRuta, origenRuta);
                }
            }
        }

        for (Ruta ruta : allRutas) {
            if (costos.get(ruta.getOrigen().getNombre()) != Float.MAX_VALUE && costos.get(ruta.getOrigen().getNombre()) + ruta.getCosto() < costos.get(ruta.getDestino().getNombre())) {
                return new ResultadoRuta("Error: Se detectó un ciclo de costo negativo.");
            }
        }

        if (costos.get(fin) == Float.MAX_VALUE) {
            return new ResultadoRuta("No hay ruta disponible de " + inicio + " a " + fin + ".");
        }

        LinkedList<String> rutaNodos = reconstruirRuta(previo, fin);
        return calcularDetallesRuta(rutaNodos);
    }


    private ResultadoRuta dijkstraGeneral(String inicio, String fin, String criterio) {
        if (!lugar.containsKey(inicio) || !lugar.containsKey(fin)) {
            return new ResultadoRuta("El lugar de inicio o fin no existe.");
        }

        HashMap<String, Float> pesos = new HashMap<>();
        HashMap<String, String> previo = new HashMap<>();
        PriorityQueue<ColaPrioritaria> cola = new PriorityQueue<>(Comparator.comparingDouble(ColaPrioritaria::getKm));

        for (String nodo : lugar.keySet()) {
            pesos.put(nodo, Float.MAX_VALUE);
            previo.put(nodo, null);
        }
        pesos.put(inicio, 0.0f);
        cola.add(new ColaPrioritaria(inicio, 0.0f));

        while (!cola.isEmpty()) {
            String nombreActual = cola.poll().getNombre();
            if (nombreActual.equals(fin)) break;
            if (rutas.get(nombreActual) == null) continue;

            for (Ruta arista : rutas.get(nombreActual)) {
                String vecino = arista.getDestino().getNombre();
                float pesoArista;

                switch (criterio) {
                    case "distancia":
                        pesoArista = arista.getDistancia();
                        break;
                    case "tiempo":
                        pesoArista = arista.getTiempoRecorrido();
                        break;
                    default: // "eficiente"
                        pesoArista = arista.getDistancia() + 1.0f * arista.getCosto() + 2.0f * arista.getTiempoRecorrido() + 1.0f * arista.getNumTransbordos();
                        break;
                }

                if (pesos.get(nombreActual) + pesoArista < pesos.get(vecino)) {
                    pesos.put(vecino, pesos.get(nombreActual) + pesoArista);
                    previo.put(vecino, nombreActual);
                    cola.add(new ColaPrioritaria(vecino, pesos.get(vecino)));
                }
            }
        }

        if (pesos.get(fin) == Float.MAX_VALUE) {
            return new ResultadoRuta("No hay ruta disponible de " + inicio + " a " + fin + ".");
        }

        LinkedList<String> rutaNodos = reconstruirRuta(previo, fin);
        return calcularDetallesRuta(rutaNodos);
    }

    /**
     * Reconstruye el camino desde el nodo final hasta el inicio usando el mapa de predecesores.
     */
    private LinkedList<String> reconstruirRuta(HashMap<String, String> previo, String fin) {
        LinkedList<String> ruta = new LinkedList<>();
        String actual = fin;
        while (actual != null) {
            ruta.addFirst(actual);
            actual = previo.get(actual);
        }
        return ruta;
    }

    /**
     * Dado un camino de nodos (nombres), calcula los totales de costo, distancia, etc.
     */
    private ResultadoRuta calcularDetallesRuta(LinkedList<String> rutaNodos) {
        if (rutaNodos.size() < 2) {
            return new ResultadoRuta("Ruta inválida.");
        }

        double costoTotal = 0;
        double distanciaTotal = 0;
        double tiempoTotal = 0;
        int transbordosTotales = Math.max(0, rutaNodos.size() - 2);

        for (int i = 0; i < rutaNodos.size() - 1; i++) {
            String origen = rutaNodos.get(i);
            String destino = rutaNodos.get(i + 1);
            Ruta rutaSegmento = encontrarRutaDirecta(origen, destino);
            if (rutaSegmento != null) {
                costoTotal += rutaSegmento.getCosto();
                distanciaTotal += rutaSegmento.getDistancia();
                tiempoTotal += rutaSegmento.getTiempoRecorrido();

            } else {

                return new ResultadoRuta("Error interno: no se encontró segmento de ruta " + origen + " -> " + destino);
            }
        }

        return new ResultadoRuta(rutaNodos, costoTotal, distanciaTotal, tiempoTotal, transbordosTotales);
    }

    /**
     * Encuentra una arista directa entre dos nodos.
     */
    private Ruta encontrarRutaDirecta(String nombreOrigen, String nombreDestino) {
        if (rutas.containsKey(nombreOrigen)) {
            for (Ruta ruta : rutas.get(nombreOrigen)) {
                if (ruta.getDestino().getNombre().equals(nombreDestino)) {
                    return ruta;
                }
            }
        }
        return null;
    }


    private void dijkstra(String inicio, String fin) {  }
    public void bellmanFord(String inicio) {  }
    private void floydWarshall() {  }


    public void mostrarGrafo() { }
    public static void main(String[] args) {  }
    class DisjointSet {  }
    private List<Ruta> getAllRutas() {
        List<Ruta> allRutas = new LinkedList<>();
        for (LinkedList<Ruta> listaRutas : rutas.values()) {
            allRutas.addAll(listaRutas);
        }
        return allRutas;
    }
    public void mostrarArbolExpansionMinima() {  }
    private void kruskal() {  }
    private HashMap<String, LinkedList<Ruta>> getGrafoNoDirigido() {  return null; }
    private void prim() { }


    static class ColaPrioritaria {
        private String nombre;
        private float km;

        public ColaPrioritaria(String nombre, float km) {
            this.nombre = nombre;
            this.km = km;
        }
        public String getNombre() { return nombre; }
        public float getKm() { return km; }
    }

    public ResultadoRuta calcularRutaMenorTiempo(String inicio, String fin) {
        return dijkstraGeneral(inicio, fin, "tiempo");
    }
}