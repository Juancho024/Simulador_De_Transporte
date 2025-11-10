package Model;

import DataBase.ParadaDAO;
import DataBase.RutaDAO;

import java.util.*;

public class RedParada {

    private HashMap<Long, LinkedList<Ruta>> rutas;
    private HashMap<Long, Parada> lugar;
    private static RedParada instance = null;

    public RedParada() {
        this.rutas = RutaDAO.getInstancia().obtenerRutas();
        this.lugar = ParadaDAO.getInstance().obtenerParadas();
    }

    public static RedParada getInstance() {
        if (instance == null) {
            instance = new RedParada();
        }
        return instance;
    }

    public HashMap<Long, LinkedList<Ruta>> getRutas() { return rutas; }
    public void setRutas(HashMap<Long, LinkedList<Ruta>> rutas) { this.rutas = rutas; }
    public HashMap<Long, Parada> getLugar() { return lugar; }
    public void setLugar(HashMap<Long, Parada> lugar) { this.lugar = lugar; }

    public void recargarGrafo() {
        this.rutas = RutaDAO.getInstancia().obtenerRutas();
        this.lugar = ParadaDAO.getInstance().obtenerParadas();
    }

    public ResultadoRuta calcularRutaMasEficiente(Long origen_id, Long destino_id) {
        return dijkstraGeneral(origen_id, destino_id, "eficiente");
    }


    public ResultadoRuta calcularRutaMenorDistancia(Long origen_id, Long destino_id) {
        return dijkstraGeneral(origen_id, destino_id, "distancia");
    }


    public ResultadoRuta calcularRutaMenorCosto(Long origen_id, Long destino_id) {
        if (!lugar.containsKey(origen_id) || !lugar.containsKey(destino_id)) {
            return new ResultadoRuta("El lugar de origen_id o destino_id no existe.");
        }

        int numNodos = lugar.size();
        HashMap<Long, Float> costos = new HashMap<>();
        HashMap<Long, Long> previo = new HashMap<>();
        List<Ruta> allRutas = getAllRutas();

        for (Long nodo : lugar.keySet()) {
            costos.put(nodo, Float.MAX_VALUE);
            previo.put(nodo, null); //prueba
        }
        costos.put(origen_id, 0.0f);

        for (int i = 0; i < numNodos - 1; i++) {
            for (Ruta ruta : allRutas) {
                Long origenRuta = ruta.getOrigen().getId();
                Long destinoRuta = ruta.getDestino().getId();
                if (costos.get(origenRuta) != Float.MAX_VALUE && costos.get(origenRuta) + ruta.getCosto() < costos.get(destinoRuta)) {
                    costos.put(destinoRuta, costos.get(origenRuta) + ruta.getCosto());
                    previo.put(destinoRuta, origenRuta);
                }
            }
        }

        for (Ruta ruta : allRutas) {
            if (costos.get(ruta.getOrigen().getId()) != Float.MAX_VALUE && costos.get(ruta.getOrigen().getId()) + ruta.getCosto() < costos.get(ruta.getDestino().getId())) {
                return new ResultadoRuta("Error: Se detectó un ciclo de costo negativo.");
            }
        }

        if (costos.get(destino_id) == Float.MAX_VALUE) {
            String inicioNombre = lugar.get(origen_id) != null ? lugar.get(origen_id).getNombre() : origen_id.toString();
            String finNombre = lugar.get(destino_id) != null ? lugar.get(destino_id).getNombre() : destino_id.toString();
            return new ResultadoRuta("No hay ruta disponible de " + inicioNombre + " a " + finNombre + ".");
        }

        LinkedList<String> rutaNodos = reconstruirRutaNombres(previo, destino_id);
        return calcularDetallesRuta(rutaNodos);
    }


    private ResultadoRuta dijkstraGeneral(Long origin_id, Long destino_id, String criterio) {
        recargarGrafo();
        if (!lugar.containsKey(origin_id) || !lugar.containsKey(destino_id)) {
            return new ResultadoRuta("El lugar de inicio o fin no existe.");
        }

        HashMap<Long, Float> pesos = new HashMap<>();
        HashMap<Long, Long> previo = new HashMap<>();
        PriorityQueue<ColaPrioritaria> cola = new PriorityQueue<>(Comparator.comparingDouble(ColaPrioritaria::getKm));

        for (Long nodo : lugar.keySet()) {
            pesos.put(nodo, Float.MAX_VALUE);
            previo.put(nodo, null); //prueba
        }
        pesos.put(origin_id, 0.0f);
        cola.add(new ColaPrioritaria(origin_id, 0.0f));

        while (!cola.isEmpty()) {
            Long idActual = cola.poll().getId();
            if (idActual.equals(destino_id)) break;
            if (rutas.get(idActual) == null) continue;

            for (Ruta arista : rutas.get(idActual)) {
                Long vecino_id = arista.getDestino().getId();
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

                if (pesos.get(idActual) + pesoArista < pesos.get(vecino_id)) {
                    pesos.put(vecino_id, pesos.get(idActual) + pesoArista);
                    previo.put(vecino_id, idActual);
                    cola.add(new ColaPrioritaria(vecino_id, pesos.get(vecino_id)));
                }
            }
        }

        if (pesos.get(destino_id) == Float.MAX_VALUE) {
            return new ResultadoRuta("No hay ruta disponible de " + origin_id + " a " + destino_id + ".");
        }

        LinkedList<String> rutaNodos = reconstruirRutaNombres(previo, destino_id);
        return calcularDetallesRuta(rutaNodos);
    }

    /**
     * Reconstruye el camino desde el nodo final hasta el inicio usando el mapa de predecesores.
     */
    private LinkedList<String> reconstruirRutaNombres(HashMap<Long, Long> previo, Long finId) {
        LinkedList<String> ruta = new LinkedList<>();
        Long actualId = finId;
        
        while (actualId != null) {
            Parada parada = lugar.get(actualId);

            if (parada != null) {
                ruta.addFirst(parada.getNombre()); 
            } else {
                System.err.println("Error interno: ID de parada no encontrado en el mapa de lugares.");
                break;
            }
            actualId = previo.get(actualId);
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
    //Para pasar a nombre los ids
    private Long buscarIdPorNombre(String nombreParada) {
        for (Map.Entry<Long, Parada> entry : lugar.entrySet()) {
            if (entry.getValue().getNombre().equals(nombreParada)) {
                return entry.getKey();
            }
        }
        return null; // No encontrado
    }
    /**
     * Encuentra una arista directa entre dos nodos.
     */
    private Ruta encontrarRutaDirecta(String nombreOrigen, String nombreDestino) {
        Long idOrigen = buscarIdPorNombre(nombreOrigen);

        if (idOrigen != null && rutas.containsKey(idOrigen)) {
            for (Ruta ruta : rutas.get(idOrigen)) {
                if (ruta.getDestino().getNombre().equals(nombreDestino)) {
                    return ruta;
                }
            }
        }
        return null;
    }

    private List<Ruta> getAllRutas() {
        List<Ruta> allRutas = new LinkedList<>();
        for (LinkedList<Ruta> listaRutas : rutas.values()) {
            allRutas.addAll(listaRutas);
        }
        return allRutas;
    }

    public ResultadoRuta calcularRutaMenorTiempo(Long origen_id, Long destino_id) {
        return dijkstraGeneral(origen_id, destino_id, "tiempo");
    }
    public void mostrarRutaSimplePorConsola(Long origen_id, Long destino_id) {
        ResultadoRuta resultado = calcularRutaMenorDistancia(origen_id, destino_id);
        System.out.println("============ RUTA MÁS RÁPIDA ============");
        System.out.printf("   Distancia Total: %.2f km\n", resultado.getDistanciaTotal());

        LinkedList<String> camino = (LinkedList<String>) resultado.getRuta();

        if (camino != null && !camino.isEmpty()) {
            System.out.println("   El camino pasa por: ");

            String rutaFormateada = String.join(" -> ", camino);
            System.out.println("   " + rutaFormateada);

        } else {
            System.out.println("   (El camino no pudo ser reconstruido.)");
        }
        System.out.println("=========================================");
    }
}