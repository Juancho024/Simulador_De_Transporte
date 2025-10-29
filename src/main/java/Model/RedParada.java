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
    public void agregarRuta(Ruta ruta) {
        String origen = ruta.getOrigen().getNombre();
        rutas.computeIfAbsent(origen, k -> new LinkedList<>()).add(ruta);
    }

    public Parada buscarParadaPorNombre(String origen) {
        for (Parada aux : lugar.values()) {
            if (aux.getNombre().equals(origen)) {
                return aux;
            }
        }
        return null;
    }

    public boolean existeRutaIgual(Ruta nuevaRuta) {
        String origen = nuevaRuta.getOrigen().getNombre();
        String destino = nuevaRuta.getDestino().getNombre();

        // Revisar rutas desde el origen
        LinkedList<Ruta> listaOrigen = rutas.get(origen);
        if (listaOrigen != null) {
            for (Ruta ruta : listaOrigen) {
                if ((ruta.getDestino().getNombre().equals(destino) && ruta.getOrigen().getNombre().equals(origen)) ||
                        (ruta.getDestino().getNombre().equals(origen) && ruta.getOrigen().getNombre().equals(destino))) {
                    return true;
                }
            }
        }

        // Revisar rutas desde el destino inverso
        LinkedList<Ruta> listaDestino = rutas.get(destino);
        if (listaDestino != null) {
            for (Ruta ruta : listaDestino) {
                if ((ruta.getDestino().getNombre().equals(destino) && ruta.getOrigen().getNombre().equals(origen)) ||
                        (ruta.getDestino().getNombre().equals(origen) && ruta.getOrigen().getNombre().equals(destino))) {
                    return true;
                }
            }
        }

        return false;
    }

    public void agregarParada(Parada nuevaParada) {
        String nombre = nuevaParada.getNombre();
        lugar.put(nombre, nuevaParada);
        rutas.putIfAbsent(nombre, new LinkedList<>());
    }

    public HashMap<String, LinkedList<Ruta>> getRutas() {
        return rutas;
    }

    public void setRutas(HashMap<String, LinkedList<Ruta>> rutas) {
        this.rutas = rutas;
    }

    public HashMap<String, Parada> getLugar() {
        return lugar;
    }

    public void setLugar(HashMap<String, Parada> lugar) {
        this.lugar = lugar;
    }


    /////////////////////////////////////////////////Funciones Prueba//////////////////////////////////////
    // --- Inicio de la clase interna DisjointSet ---
    /**
     * Clase auxiliar para la estructura de datos Disjoint Set (Union-Find).
     * Esencial para detectar ciclos eficientemente en el algoritmo de Kruskal.
     */
    class DisjointSet {
        private HashMap<String, String> parent;

        public DisjointSet(Set<String> nodos) {
            parent = new HashMap<>();
            // Inicialmente, cada nodo es su propio padre
            for (String nodo : nodos) {
                parent.put(nodo, nodo);
            }
        }

        /**
         * Encuentra el representante (raíz) del conjunto al que pertenece 'i'.
         * Utiliza compresión de caminos para optimizar futuras búsquedas.
         */
        public String find(String i) {
            if (parent.get(i).equals(i))
                return i;
            // Compresión de caminos
            String root = find(parent.get(i));
            parent.put(i, root);
            return root;
        }

        /**
         * Une los conjuntos a los que pertenecen 'x' e 'y'.
         */
        public void union(String x, String y) {
            String rootX = find(x);
            String rootY = find(y);
            if (!rootX.equals(rootY)) {
                // Une un árbol al otro
                parent.put(rootX, rootY);
            }
        }
    }
    // --- Fin de la clase interna DisjointSet ---


    public void agregarNodo(String nombre, int posiciony, int posicionx) { //Agregar parada
        Parada aux = new Parada(nombre, "", posiciony, posicionx);
        lugar.put(nombre, aux);
        rutas.putIfAbsent(nombre, new LinkedList<>());
    }

    public void agregarArista(String origen, String destino, int peso) { //Agregar ruta
        Parada paradaOrigen = lugar.get(origen);
        Parada paradaDestino = lugar.get(destino);

        if (paradaOrigen != null && paradaDestino != null) {
            Ruta arista = new Ruta(paradaOrigen, paradaDestino, peso, 0.0f, 0.0f, 0.0f, "");
            rutas.computeIfAbsent(origen, k -> new LinkedList<>()).add(arista);
        } else {
            System.out.println("Uno de los lugares no existe.");
        }
    }

    public void mostrarGrafo() {
        System.out.print("RedParada (Grafo Dirigido): \n");
        for (String aux : rutas.keySet()) {
            LinkedList<Ruta> listRutas = rutas.get(aux);
            System.out.print("Lugar " + aux + " tiene rutas al ");
            if (listRutas.isEmpty()) {
                System.out.print("ningún lugar.");
            }
            for (int i = 0; i < listRutas.size(); i++) {
                Ruta arista = listRutas.get(i);
                System.out.print(arista.getDestino().getNombre() + " (" + arista.getDistancia() + "km)");
                if (i != listRutas.size() - 1) {
                    System.out.print(", ");
                }
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        RedParada redParada = new RedParada();

        // Agregar nodos (paradas)
        redParada.agregarNodo("A", 10, 15);
        redParada.agregarNodo("B", 10, 15);
        redParada.agregarNodo("C", 10, 15);
        redParada.agregarNodo("D", 10, 15);
        redParada.agregarNodo("E", 10, 15);

        // Agregar aristas (rutas) - Bidireccionales para un buen test de MST
        redParada.agregarArista("A", "B", 10);
        redParada.agregarArista("B", "A", 10);

        redParada.agregarArista("A", "C", 30);
        redParada.agregarArista("C", "A", 30);

        redParada.agregarArista("B", "C", 5);
        redParada.agregarArista("C", "B", 5);

        redParada.agregarArista("B", "D", 20);
        redParada.agregarArista("D", "B", 20);

        redParada.agregarArista("C", "D", 8);
        redParada.agregarArista("D", "C", 8);

        redParada.agregarArista("D", "E", 2);
        redParada.agregarArista("E", "D", 2);

        redParada.agregarArista("C", "E", 15);
        redParada.agregarArista("E", "C", 15);


        // --- Pruebas de Algoritmos ---

        redParada.mostrarGrafo();
        System.out.println("\n----------------------------------------");

        redParada.dijkstra("A", "D");
        System.out.println("\n----------------------------------------");

        redParada.floydWarshall();
        System.out.println("\n----------------------------------------");

        // Llamar a los nuevos algoritmos de MST
        redParada.mostrarArbolExpansionMinima();
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

                if (nombreActual.equals(fin)) break; // Optimización: parar al encontrar el destino

                for (Ruta arista : rutas.get(nombreActual)) {
                    String vecino = arista.getDestino().getNombre();

                    float nuevoPeso = pesoTotal.get(nombreActual)
                            + arista.getDistancia()
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

            System.out.println("Dijkstra - Ruta más eficiente de " + inicio + " a " + fin + ": " + ruta);
            System.out.println("Dijkstra - Peso total: " + pesoTotal.get(fin));
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
        final int numMax = Integer.MAX_VALUE / 2; // Evitar overflow

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
                distancia[i][j] = (int) ruta.getDistancia();
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
        System.out.println("Floyd-Warshall - Matriz de distancias mínimas:");
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
        if(claves.isEmpty()) return;

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

    /**
     * Implementación del algoritmo de Bellman-Ford.
     * Encuentra la ruta de MENOR COSTO desde un nodo de inicio a todos los demás,
     * permitiendo costos negativos y detectando ciclos negativos.
     */
    public void bellmanFord(String inicio) {
        if (!lugar.containsKey(inicio)) {
            System.out.println("El lugar de inicio '" + inicio + "' no existe.");
            return;
        }

        int numNodos = lugar.size();
        HashMap<String, Float> costos = new HashMap<>();
        HashMap<String, String> previo = new HashMap<>();
        List<Ruta> allRutas = getAllRutas(); // Obtenemos todas las aristas

        // 1. Inicialización
        for (String nodo : lugar.keySet()) {
            costos.put(nodo, Float.MAX_VALUE);
            previo.put(nodo, null);
        }
        costos.put(inicio, 0.0f);

        // 2. Relajación de aristas (V-1 iteraciones)
        // Repetir V-1 veces (V = numNodos)
        for (int i = 0; i < numNodos - 1; i++) {
            boolean algoCambio = false;
            // Iterar por todas las aristas en el grafo
            for (Ruta ruta : allRutas) {
                String origen = ruta.getOrigen().getNombre();
                String destino = ruta.getDestino().getNombre();
                float costoRuta = ruta.getCosto();

                // Si encontramos un camino más barato
                if (costos.get(origen) != Float.MAX_VALUE && costos.get(origen) + costoRuta < costos.get(destino)) {
                    costos.put(destino, costos.get(origen) + costoRuta);
                    previo.put(destino, origen);
                    algoCambio = true;
                }
            }
            // Optimización: si en una pasada entera no cambió nada, ya terminamos
            if (!algoCambio) break;
        }

        // 3. Detección de ciclos negativos
        // Hacemos una pasada MÁS (la V-ésima iteración)
        boolean hayCicloNegativo = false;
        for (Ruta ruta : allRutas) {
            String origen = ruta.getOrigen().getNombre();
            String destino = ruta.getDestino().getNombre();
            float costoRuta = ruta.getCosto();

            // Si AÚN podemos encontrar un camino más barato, es por un ciclo negativo
            if (costos.get(origen) != Float.MAX_VALUE && costos.get(origen) + costoRuta < costos.get(destino)) {
                hayCicloNegativo = true;
                break; // Encontramos uno, no es necesario seguir
            }
        }

        // 4. Mostrar resultados
        System.out.println("Bellman-Ford - Rutas de menor COSTO desde '" + inicio + "':");
        if (hayCicloNegativo) {
            System.out.println("  ¡ERROR! Se detectó un ciclo de costo negativo.");
            System.out.println("  Los costos de las rutas no son fiables.");
        } else {
            // Imprimir todas las rutas más baratas
            for (String destino : lugar.keySet()) {
                if (destino.equals(inicio)) continue;

                if (costos.get(destino) == Float.MAX_VALUE) {
                    System.out.printf("  Ruta a %-5s: No alcanzable\n", destino);
                } else {
                    // Reconstruir la ruta
                    LinkedList<String> ruta = new LinkedList<>();
                    String actual = destino;
                    while (actual != null) {
                        ruta.addFirst(actual);
                        actual = previo.get(actual);
                    }
                    System.out.printf("  Ruta a %-5s: %-25s (Costo total: $%.2f)\n",
                            destino, ruta.toString(), costos.get(destino));
                }
            }
        }
    }

    // --- IMPLEMENTACIÓN DE ÁRBOL DE EXPANSIÓN MÍNIMA (MST) ---

    public void mostrarArbolExpansionMinima() {
        if (lugar.isEmpty()) {
            System.out.println("No hay paradas en la red para calcular el MST.");
            return;
        }
        kruskal();
        System.out.println(); // Separador
        prim();
    }

    /**
     * Recopila todas las aristas (Ruta) de la red en una sola lista.
     * Necesario para el algoritmo de Kruskal.
     */
    private List<Ruta> getAllRutas() {
        List<Ruta> allRutas = new LinkedList<>();
        for (LinkedList<Ruta> listaRutas : rutas.values()) {
            allRutas.addAll(listaRutas);
        }
        return allRutas;
    }

    /**
     * Implementación del algoritmo de Kruskal para encontrar el MST.
     */
    private void kruskal() {
        System.out.println("Algoritmo de Kruskal (Árbol de Expansión Mínima):");

        List<Ruta> mstRutas = new LinkedList<>();
        double costoTotal = 0;

        // 1. Obtener todas las aristas y ordenarlas por distancia (peso)
        List<Ruta> allRutas = getAllRutas();
        allRutas.sort(Comparator.comparingDouble(Ruta::getDistancia));

        // 2. Inicializar DisjointSet con todas las paradas (nodos)
        DisjointSet ds = new DisjointSet(lugar.keySet());

        // 3. Iterar por las aristas ordenadas
        for (Ruta ruta : allRutas) {
            String origen = ruta.getOrigen().getNombre();
            String destino = ruta.getDestino().getNombre();

            // 4. Si origen y destino no están en el mismo conjunto, unirlos
            if (!ds.find(origen).equals(ds.find(destino))) {
                mstRutas.add(ruta);
                costoTotal += ruta.getDistancia();
                ds.union(origen, destino);
            }
        }

        // 5. Mostrar resultados
        System.out.println("  Rutas en el MST:");
        for (Ruta ruta : mstRutas) {
            System.out.printf("    - %s <-> %s (Distancia: %.1f km)\n",
                    ruta.getOrigen().getNombre(),
                    ruta.getDestino().getNombre(),
                    ruta.getDistancia());
        }
        System.out.printf("  Distancia total del MST (Kruskal): %.1f km\n", costoTotal);
    }

    /**
     * Construye y retorna una representación de grafo no dirigido.
     * Necesario para el algoritmo de Prim.
     */
    private HashMap<String, LinkedList<Ruta>> getGrafoNoDirigido() {
        HashMap<String, LinkedList<Ruta>> grafoNoDirigido = new HashMap<>();

        // Inicializar el mapa con todas las paradas
        for (String paradaNombre : lugar.keySet()) {
            grafoNoDirigido.put(paradaNombre, new LinkedList<>());
        }

        // Agregar ambas direcciones para cada ruta
        for (LinkedList<Ruta> listaRutas : rutas.values()) {
            for (Ruta ruta : listaRutas) {
                Parada origen = ruta.getOrigen();
                Parada destino = ruta.getDestino();

                // Agregar arista original A -> B
                grafoNoDirigido.get(origen.getNombre()).add(ruta);

                // Crear y agregar arista inversa B -> A
                // (Se copian los valores, pero se invierten origen y destino)
                Ruta rutaInversa = new Ruta(destino, origen,
                        ruta.getDistancia(),
                        ruta.getCosto(),
                        ruta.getTiempoRecorrido(),
                        ruta.getNumTransbordos(),
                        ruta.getPosibleEvento());
                grafoNoDirigido.get(destino.getNombre()).add(rutaInversa);
            }
        }
        return grafoNoDirigido;
    }

    /**
     * Implementación del algoritmo de Prim para encontrar el MST.
     */
    private void prim() {
        System.out.println("Algoritmo de Prim (Árbol de Expansión Mínima):");

        List<Ruta> mstRutas = new LinkedList<>();
        double costoTotal = 0;

        // Nodos que ya están en el MST
        HashSet<String> inMST = new HashSet<>();

        // Cola de prioridad para almacenar las aristas (Ruta) por distancia
        PriorityQueue<Ruta> pq = new PriorityQueue<>(Comparator.comparingDouble(Ruta::getDistancia));

        // 1. Obtener el grafo no dirigido
        HashMap<String, LinkedList<Ruta>> grafoNoDirigido = getGrafoNoDirigido();

        // 2. Elegir un nodo de inicio (cualquiera)
        String startNode = lugar.keySet().iterator().next();
        inMST.add(startNode);

        // 3. Agregar todas las aristas del nodo de inicio a la cola
        pq.addAll(grafoNoDirigido.get(startNode));

        // 4. Bucle principal de Prim
        while (!pq.isEmpty() && mstRutas.size() < lugar.size() - 1) {
            // 5. Extraer la arista con menor peso
            Ruta minRuta = pq.poll();

            Parada origen = minRuta.getOrigen();
            Parada destino = minRuta.getDestino();

            // 6. Verificar si la arista conecta un nodo nuevo
            String nodoNoEnMST = null;
            if (inMST.contains(origen.getNombre()) && !inMST.contains(destino.getNombre())) {
                nodoNoEnMST = destino.getNombre();
            } else if (!inMST.contains(origen.getNombre()) && inMST.contains(destino.getNombre())) {
                nodoNoEnMST = origen.getNombre();
            }

            // 7. Si es una arista válida (conecta un nodo nuevo)
            if (nodoNoEnMST != null) {
                mstRutas.add(minRuta);
                costoTotal += minRuta.getDistancia();
                inMST.add(nodoNoEnMST);

                // 8. Agregar todas las aristas del nuevo nodo a la cola
                for (Ruta rutaVecina : grafoNoDirigido.get(nodoNoEnMST)) {
                    // Solo agregar si el otro extremo no está ya en el MST
                    if (!inMST.contains(rutaVecina.getDestino().getNombre())) {
                        pq.add(rutaVecina);
                    }
                }
            }
        }

        // 9. Mostrar resultados
        System.out.println("  Rutas en el MST:");
        for (Ruta ruta : mstRutas) {
            System.out.printf("    - %s <-> %s (Distancia: %.1f km)\n",
                    ruta.getOrigen().getNombre(),
                    ruta.getDestino().getNombre(),
                    ruta.getDistancia());
        }
        System.out.printf("  Distancia total del MST (Prim): %.1f km\n", costoTotal);
    }
}