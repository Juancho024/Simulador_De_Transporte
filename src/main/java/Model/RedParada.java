package Model;

import java.util.*;

public class RedParada {
    private HashMap<String, LinkedList<Ruta>> rutas;
    private HashMap<String, Parada> lugar;

    public RedParada() {
        this.rutas = new HashMap<>();
        this.lugar = new HashMap<>();
    }

    public void agregarNodo(String nombre, int posicionx, int posiciony) { //Agregar parada
        Parada aux = new Parada(nombre, posicionx, posiciony);
        lugar.put(nombre, aux);
        rutas.putIfAbsent(nombre, new LinkedList<>());
    }

    public void agregarArista(String origen, String destino, int peso) { //Agregar ruta
        Parada paradaOrigen = lugar.get(origen);
        Parada paradaDestino = lugar.get(destino);

        if (paradaOrigen != null && paradaDestino != null) {
            Ruta arista = new Ruta(paradaOrigen, paradaDestino, peso);
            rutas.computeIfAbsent(origen, k -> new LinkedList<>()).add(arista);
        } else {
            System.out.println("Uno de los lugares no existe.");
        }
    }

    public void mostrarGrafo() {
        System.out.print("RedParada: \n");
        for (String aux : rutas.keySet()) {
            LinkedList<Ruta> listRutas = rutas.get(aux);
            System.out.print("Lugar " + aux + " tiene rutas al ");
            for (int i = 0; i < listRutas.size(); i++) {
                Ruta arista = listRutas.get(i);
                System.out.print(arista.getDestino().getNombre() + " hay " + arista.getPeso() + "km");
                if (i != listRutas.size() - 1) {
                    System.out.print(", al ");
                }
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        RedParada redParada = new RedParada();
        redParada.agregarNodo("A", 10, 15);
        redParada.agregarNodo("B", 10, 15);
        redParada.agregarNodo("C", 10, 15);

        redParada.agregarArista("A", "B", 10);
        redParada.agregarArista("B", "C", 5);
        redParada.agregarArista("A", "C", 30);

        redParada.mostrarGrafo();
        redParada.dijkstra("A", "C");
        redParada.floydWarshall();
    }

    private void dijkstra(String inicio, String fin) {
        if (lugar.containsKey(inicio) && lugar.containsKey(fin)) {
            HashMap<String, Float> pesoTotal = new HashMap<>();
            HashMap<String, String> previo = new HashMap<>();
            HashSet<String> visitados = new HashSet<>();
            PriorityQueue<ColaPrioritaria> cola = new PriorityQueue<>(Comparator.comparingDouble(ColaPrioritaria::getKm));

            for (String nodo : lugar.keySet()) {
                pesoTotal.put(nodo, Float.MAX_VALUE);
                previo.put(nodo, null);
            }

            pesoTotal.put(inicio, 0.0f);
            cola.add(new ColaPrioritaria(inicio, 0.0f));

            while (!cola.isEmpty()) {
                ColaPrioritaria actual = cola.poll();
                String nombreActual = actual.getNombre();

                if (visitados.contains(nombreActual)) continue;
                visitados.add(nombreActual);

                for (Ruta arista : rutas.get(nombreActual)) {
                    String vecino = arista.getDestino().getNombre();

                    float nuevoPeso = pesoTotal.get(nombreActual)
                            + arista.getPeso()
                            + 1.0f * arista.getCosto()
                            + 2.0f * arista.getTiempoRecorrido()
                            + 1.0f * arista.getNumTransbordos();

                    if (nuevoPeso < pesoTotal.get(vecino) && !visitados.contains(vecino)) {
                        pesoTotal.put(vecino, nuevoPeso);
                        previo.put(vecino, nombreActual);
                        cola.add(new ColaPrioritaria(vecino, nuevoPeso));
                    }
                }
            }

            if (pesoTotal.get(fin) == Float.MAX_VALUE) {
                System.out.println("No hay ruta disponible de " + inicio + " a " + fin);
                return;
            }

            LinkedList<String> ruta = new LinkedList<>();
            String actual = fin;
            while (actual != null) {
                ruta.addFirst(actual);
                actual = previo.get(actual);
            }

            System.out.println("Ruta más eficiente: " + ruta);
            System.out.println("Peso total: " + pesoTotal.get(fin));
        } else {
            System.out.println("Uno de los lugares no existe.");
        }
    }



    private void floydWarshall() {
        LinkedList<String> nombres = new LinkedList<>(lugar.keySet());
        HashMap<String, Integer> indice = new HashMap<>();

        int n = lugar.size();
        for (int i = 0; i < n; i++) {
            indice.put(nombres.get(i), i);
        }
        int distancia[][] = new int[n][n];
        final int numMax = Integer.MAX_VALUE / 2;

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j)
                    distancia[i][j] = 0;
                else
                    distancia[i][j] = numMax;
            }
        }
        for (String lugar : lugar.keySet()) {
            for (Ruta ruta : rutas.get(lugar)) {
                int i = indice.get(lugar);
                int j = indice.get(ruta.getDestino().getNombre());
                distancia[i][j] = (int) ruta.getPeso();
            }
        }
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (distancia[i][j] > distancia[i][k] + distancia[k][j]) {
                        distancia[i][j] = distancia[i][k] + distancia[k][j];
                    }
                }
            }
        }
        System.out.println("\nMatriz de distancias mínimas:");
        System.out.print("     ");
        for (String nombre : nombres) {
            System.out.printf("%5s", nombre);
        }
        System.out.println();
        for (int i = 0; i < n; i++) {
            System.out.printf("%5s", nombres.get(i));
            for (int j = 0; j < n; j++) {
                if (distancia[i][j] == numMax)
                    System.out.printf("%5s", "∞");
                else
                    System.out.printf("%5d", distancia[i][j]);
            }
            System.out.println();
        }

    }

    private void generarEventoRandow() {
        // Implementación para generar eventos aleatorios en el grafo
        Random random = new Random();
        List<String> claves = new ArrayList<>(this.rutas.keySet());
        String claveAleatoria = claves.get(random.nextInt(claves.size()));
        LinkedList<Ruta> aristas = this.rutas.get(claveAleatoria);
        if (aristas != null && !aristas.isEmpty()) {
            Ruta aristaAleatoria = aristas.get(random.nextInt(aristas.size()));
            String[] eventosPosibles = {"Accidente", "Manifestación", "LLuvias intensas", "Obras viales"};
            String eventoSeleccionado = eventosPosibles[random.nextInt(eventosPosibles.length)];
            aristaAleatoria.setPosibleEvento(eventoSeleccionado);
            System.out.println("Evento generado en la ruta de " + aristaAleatoria.getOrigen().getNombre() + " a " + aristaAleatoria.getDestino().getNombre() + ": " + eventoSeleccionado);
        }
    }

    private void bellmanFord(String inicio) {
        // Implementación del algoritmo de Bellman-Ford
    }

    private void ArbolExpansionMinima() {
        // Implementación del algoritmo de Árbol de Expansión Mínima
    }


    public void agregarRuta(Ruta ruta) {
        String origen = ruta.getOrigen().getNombre();
        rutas.computeIfAbsent(origen, k -> new LinkedList<>()).add(ruta);
    }

    public Parada buscarParadaPorNombre(String origen) {
        for(Parada aux : lugar.values()){
            if(aux.getNombre().equals(origen)){
                return aux;
            }
        }
        return null;
    }

    public boolean existeRutaEntreParadas(String origen, String destino) {
        for(Ruta ruta : rutas.get(origen)){
            if(ruta.getDestino().getNombre().equals(destino) && ruta.getOrigen().getNombre().equals(origen)){
                return true;
            }
        }
        return false;
    }
}
