package Model;

import DataBase.ParadaDAO;
import DataBase.RutaDAO;

import java.util.*;

public class RedParada {

    private HashMap<Long, LinkedList<Ruta>> rutas;
    private HashMap<Long, Parada> lugar;
    private static RedParada instance = null;

    //    Variables para el Floyd-Warshall
    private Float[][] distanciasFW;
    private Long[][] siguientesNodosFW;
    private List<Long> idsIndexados;

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

    public HashMap<Long, LinkedList<Ruta>> getRutas() {
        return rutas;
    }

    public void setRutas(HashMap<Long, LinkedList<Ruta>> rutas) {
        this.rutas = rutas;
    }

    public HashMap<Long, Parada> getLugar() {
        return lugar;
    }

    public void setLugar(HashMap<Long, Parada> lugar) {
        this.lugar = lugar;
    }

    public void recargarGrafo() {
        this.rutas = RutaDAO.getInstancia().obtenerRutas();
        this.lugar = ParadaDAO.getInstance().obtenerParadas();
        this.distanciasFW = null;
        this.siguientesNodosFW = null;
    }

    public ResultadoRuta calcularRutaMasEficiente(Long origen_id, Long destino_id) {
        return dijkstraGeneral(origen_id, destino_id, "eficiente");
    }


    public ResultadoRuta calcularRutaMenorDistancia(Long origen_id, Long destino_id) {
        return dijkstraGeneral(origen_id, destino_id, "distancia");
    }

    static class DisjointSet {
        private Map<Long, Long> parent = new HashMap<>();

        public DisjointSet(Set<Long> nodos) {
            for (Long nodo : nodos) {
                parent.put(nodo, nodo);
            }
        }

        public long find(Long i) {
            if (parent.get(i) == i) return i;
            long root = find(parent.get(i));
            parent.put(i, root);
            return root;
        }

        public void union(Long i, Long j) {
            long rootI = find(i);
            long rootJ = find(j);
            if (rootI != rootJ) {
                parent.put(rootI, rootJ);
            }
        }
    }

//    BellmanFord

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
            previo.put(nodo, null); //pruebaa
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

//    FloydWarshall

    public void calcularTodasLasRutasMasCortas() {
        if (distanciasFW != null) {
            System.out.println("Floyd-Warshall: Usando resultados calculados previamente.");
            return; // Ya fue calculado
        }

        System.out.println("Floyd-Warshall: Calculando todas las rutas...");
        recargarGrafo();
        int n = lugar.size();
        idsIndexados = new ArrayList<>(lugar.keySet());
        HashMap<Long, Integer> idToIndex = new HashMap<>();
        for (int i = 0; i < n; i++) {
            idToIndex.put(idsIndexados.get(i), i);
        }

        distanciasFW = new Float[n][n];
        siguientesNodosFW = new Long[n][n];


        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                distanciasFW[i][j] = (i == j) ? 0.0f : Float.MAX_VALUE;
                siguientesNodosFW[i][j] = null;
            }
        }


        for (Ruta ruta : getAllRutas()) {
            Integer u = idToIndex.get(ruta.getOrigen().getId());
            Integer v = idToIndex.get(ruta.getDestino().getId());
            if (u != null && v != null && ruta.getDistancia() < distanciasFW[u][v]) {
                distanciasFW[u][v] = ruta.getDistancia();
                siguientesNodosFW[u][v] = ruta.getDestino().getId();
            }
        }

        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (distanciasFW[i][k] != Float.MAX_VALUE && distanciasFW[k][j] != Float.MAX_VALUE &&
                            distanciasFW[i][k] + distanciasFW[k][j] < distanciasFW[i][j]) {
                        distanciasFW[i][j] = distanciasFW[i][k] + distanciasFW[k][j];
                        siguientesNodosFW[i][j] = siguientesNodosFW[i][k];
                    }
                }
            }
        }
        System.out.println("Floyd-Warshall: Cálculo completado.");
    }

    public ResultadoRuta obtenerRutaFloydWarshall(Long origenId, Long destinoId) {

        if (distanciasFW == null) {
            calcularTodasLasRutasMasCortas();
        }

        HashMap<Long, Integer> idToIndex = new HashMap<>();
        for (int i = 0; i < idsIndexados.size(); i++) {
            idToIndex.put(idsIndexados.get(i), i);
        }

        Integer origenIdx = idToIndex.get(origenId);
        Integer destinoIdx = idToIndex.get(destinoId);

        if (origenIdx == null || destinoIdx == null || distanciasFW[origenIdx][destinoIdx] == Float.MAX_VALUE) {
            return new ResultadoRuta("No hay ruta disponible (según Floyd-Warshall).");
        }

        LinkedList<String> rutaNodos = new LinkedList<>();
        Long actualId = origenId;
        while (actualId != null && !actualId.equals(destinoId)) {
            rutaNodos.add(lugar.get(actualId).getNombre());
            origenIdx = idToIndex.get(actualId);
            actualId = siguientesNodosFW[origenIdx][destinoIdx];
        }
        if (actualId != null) {
            rutaNodos.add(lugar.get(actualId).getNombre());
        }

        return calcularDetallesRuta(rutaNodos);
    }

//    Kruskal

    public List<Ruta> calcularMstKruskal(boolean porCosto) {
        recargarGrafo();
        List<Ruta> mst = new ArrayList<>();
        List<Ruta> todasLasRutas = getAllRutas();

        if (porCosto) {
            todasLasRutas.sort(Comparator.comparing(Ruta::getCosto));
        } else {
            todasLasRutas.sort(Comparator.comparing(Ruta::getDistancia));
        }

        DisjointSet ds = new DisjointSet(lugar.keySet());

        for (Ruta ruta : todasLasRutas) {
            Long origen = ruta.getOrigen().getId();
            Long destino = ruta.getDestino().getId();

            if (ds.find(origen) != ds.find(destino)) {
                mst.add(ruta);
                ds.union(origen, destino);
            }
        }
        return mst;
    }

//    Prim

    public List<Ruta> calcularMstPrim(Long inicioId) {
        if (!lugar.containsKey(inicioId)) {
            System.err.println("Error en Prim: La parada de inicio no existe.");
            return new ArrayList<>();
        }
        recargarGrafo();
        List<Ruta> mst = new ArrayList<>();
        Set<Long> visitados = new HashSet<>();
        PriorityQueue<Ruta> pq = new PriorityQueue<>(Comparator.comparing(Ruta::getCosto));

        visitados.add(inicioId);
        if (rutas.containsKey(inicioId)) {
            pq.addAll(rutas.get(inicioId));
        }

        while (!pq.isEmpty() && visitados.size() < lugar.size()) {
            Ruta rutaMasBarata = pq.poll();
            Long destino = rutaMasBarata.getDestino().getId();

            if (visitados.contains(destino)) {
                continue;
            }

            mst.add(rutaMasBarata);
            visitados.add(destino);

            if (rutas.containsKey(destino)) {
                for (Ruta rutaAdyacente : rutas.get(destino)) {
                    if (!visitados.contains(rutaAdyacente.getDestino().getId())) {
                        pq.add(rutaAdyacente);
                    }
                }
            }
        }
        return mst;
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


//     Reconstruye el camino desde el nodo final hasta el inicio usando el mapa de predecesores.

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


//     Dado un camino de nodos (nombres), calcula los totales de costo, distancia, etc.

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
    public Long buscarIdPorNombre(String nombreParada) {
        for (Map.Entry<Long, Parada> entry : lugar.entrySet()) {
            if (entry.getValue().getNombre().equals(nombreParada)) {
                return entry.getKey();
            }
        }
        return null; // No encontrado
    }

//      Encuentra una arista directa entre dos nodos.

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

    public List<Ruta> getAllRutas() {
        List<Ruta> allRutas = new LinkedList<>();
        for (LinkedList<Ruta> listaRutas : rutas.values()) {
            allRutas.addAll(listaRutas);
        }
        return allRutas;
    }

    static class ColaPrioritaria {
        private Long id;
        private float km;

        public ColaPrioritaria(Long id, float km) {
            this.id = id;
            this.km = km;
        }

        public Long getId() {
            return id;
        }

        public float getKm() {
            return km;
        }
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
    public List<Ruta> obtenerRutaEficienteComoListaRuta(Long origen_id, Long destino_id) {
        ResultadoRuta resultado = calcularRutaMasEficiente(origen_id, destino_id);
        if (resultado == null || !resultado.esAlcanzable() || resultado.getRuta() == null || resultado.getRuta().isEmpty()) {
            return Collections.emptyList();
        }
        LinkedList<String> rutaNodos = (LinkedList<String>) resultado.getRuta();

        List<Ruta> rutaCompleta = new ArrayList<>();
        for (int i = 0; i < rutaNodos.size() - 1; i++) {
            String origen = rutaNodos.get(i);
            String destino = rutaNodos.get(i + 1);

            Ruta rutaSegmento = encontrarRutaDirecta(origen, destino);

            if (rutaSegmento != null) {
                rutaCompleta.add(rutaSegmento);
            } else {
                System.err.println("Error interno al reconstruir: No se encontró segmento de ruta " + origen + " -> " + destino);
                return Collections.emptyList();
            }
        }
        return rutaCompleta;
    }
}