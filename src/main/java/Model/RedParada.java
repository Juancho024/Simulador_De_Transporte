package Model;

import DataBase.ParadaDAO;
import DataBase.RutaDAO;
import javafx.scene.control.Alert;

import java.util.*;

public class RedParada {

    private HashMap<Long, LinkedList<Ruta>> rutas;
    private HashMap<Long, Parada> lugar;
    private static RedParada instance = null;

    // Variables para Floyd-Warshall
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

    //Precargar la informacion de la db en caso de que el controlador falle
    public void recargarGrafo() {
        this.rutas = RutaDAO.getInstancia().obtenerRutas();
        this.lugar = ParadaDAO.getInstance().obtenerParadas();
        this.distanciasFW = null;
        this.siguientesNodosFW = null;
    }

    // ==========================================
    //       SEGUNDA MEJOR RUTA (NUEVO)
    // ==========================================

    public ResultadoRuta calcularSegundaMejorRuta(Long origenId, Long destinoId, String criterio) {
        // 1. Obtener la mejor ruta actual
        ResultadoRuta mejorRuta = dijkstraGeneral(origenId, destinoId, criterio, Evento.NORMAL, "normal");

        if (!mejorRuta.esAlcanzable() || mejorRuta.getRuta().size() < 2) {
            return new ResultadoRuta("No existe ruta alternativa.");
        }

        List<String> caminoOriginal = mejorRuta.getRuta();
        ResultadoRuta segundaMejor = null;
        double mejorValorAlternativo = Double.MAX_VALUE;

        // 2. Iterar sobre cada arista de la mejor ruta, eliminarla temporalmente y buscar ruta
        for (int i = 0; i < caminoOriginal.size() - 1; i++) {
            String nombreU = caminoOriginal.get(i);
            String nombreV = caminoOriginal.get(i + 1);

            Long uId = buscarIdPorNombre(nombreU);

            // Guardar arista para restaurar
            Ruta aristaRemovida = eliminarAristaTemporal(uId, nombreV);

            // Calcular nueva ruta sin esa arista
            ResultadoRuta candidata = dijkstraGeneral(origenId, destinoId, criterio, Evento.NORMAL, "normal");

            // Restaurar arista
            if (aristaRemovida != null) {
                restaurarArista(uId, aristaRemovida);
            }

            // Evaluar si es la mejor alternativa encontrada hasta ahora
            if (candidata.esAlcanzable()) {
                double valorCandidata = obtenerValorPorCriterio(candidata, criterio);
                if (valorCandidata < mejorValorAlternativo) {
                    mejorValorAlternativo = valorCandidata;
                    segundaMejor = candidata;
                }
            }
        }

        if (segundaMejor == null) {
            return new ResultadoRuta("No se encontró una ruta alternativa viable.");
        }
        return segundaMejor;
    }

    private Ruta eliminarAristaTemporal(Long origenId, String nombreDestino) {
        if (origenId == null || !rutas.containsKey(origenId)) return null;
        LinkedList<Ruta> adyacentes = rutas.get(origenId);

        for (int i = 0; i < adyacentes.size(); i++) {
            if (adyacentes.get(i).getDestino().getNombre().equals(nombreDestino)) {
                return adyacentes.remove(i);
            }
        }
        return null;
    }

    private void restaurarArista(Long origenId, Ruta ruta) {
        if (origenId != null && ruta != null) {
            rutas.computeIfAbsent(origenId, k -> new LinkedList<>()).add(ruta);
        }
    }

    private double obtenerValorPorCriterio(ResultadoRuta res, String criterio) {
        switch (criterio) {
            case "distancia":
                return res.getDistanciaTotal();
            case "tiempo":
                return res.getTiempoTotal();
            case "costo":
                return res.getCostoTotal();
            default:
                return res.getDistanciaTotal() + res.getCostoTotal() + res.getTiempoTotal();
        }
    }

    // ==========================================
    //           ALGORITMO DE KRUSKAL
    // ==========================================

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

            // Verificar si el origen y destino ya están en el mismo conjunto
            if (ds.find(origen) != ds.find(destino)) {
                mst.add(ruta);
                ds.union(origen, destino);
            }
        }
        return mst;
    }

    // Clase interna para Kruskal
    static class DisjointSet {
        private Map<Long, Long> parent = new HashMap<>();

        public DisjointSet(Set<Long> nodos) {
            for (Long nodo : nodos) {
                parent.put(nodo, nodo);
            }
        }

        public long find(Long i) {
            if (!parent.containsKey(i)) return i; // Seguridad extra
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
    // ==========================================
    //          FLOYD-WARSHALL (RESTITUIDO)
    // ==========================================

    public void calcularTodasLasRutasMasCortas() {
        if (distanciasFW != null) {
            System.out.println("Floyd-Warshall: Usando resultados calculados previamente.");
            return;
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
                    if (distanciasFW[i][k] != Float.MAX_VALUE && distanciasFW[k][j] != Float.MAX_VALUE && distanciasFW[i][k] + distanciasFW[k][j] < distanciasFW[i][j]) {
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

        return calcularDetallesRuta(rutaNodos, "normal");
    }

    // ==========================================
    //           DIJKSTRA GENERAL (CORE)
    // ==========================================
    private ResultadoRuta dijkstraGeneral(Long origin_id, Long destino_id, String criterio, Evento evento, String tipoEvento) {
        if (!lugar.containsKey(origin_id) || !lugar.containsKey(destino_id)) {
            return new ResultadoRuta("El lugar de inicio o fin no existe.");
        }

        HashMap<Long, Float> pesos = new HashMap<>();
        HashMap<Long, Long> previo = new HashMap<>();
        PriorityQueue<ColaPrioritaria> cola = new PriorityQueue<>(Comparator.comparingDouble(ColaPrioritaria::getPeso));

        for (Long nodo : lugar.keySet()) {
            pesos.put(nodo, Float.MAX_VALUE);
            previo.put(nodo, null);
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
                    case "costo":
                        pesoArista = arista.getCosto();
                        break;
                    default: // "eficiente"
                        // Combinamos atributos para la heurística eficiente
                        pesoArista = arista.getDistancia() + arista.getCosto() + arista.getTiempoRecorrido() + arista.getNumTransbordos();
                        break;
                }

                // Aplicar factor según el evento y según el criterio solicitado (SOLO AQUÍ)
                if (evento != null && evento != Evento.NORMAL) {
                    switch (criterio) {
                        case "distancia":
                            pesoArista *= 1.0f + (float) evento.factorDistancia;
                            break;
                        case "tiempo":
                            pesoArista *= 1.0f + (float) evento.factorTiempo;
                            break;
                        case "costo":
                            pesoArista *= 1.0f + (float) evento.factorCosto;
                            break;
                        default: // eficiente
                            pesoArista *= 1.0f + (float) evento.factorEficiente;
                            break;
                    }
                }

                if (pesos.get(idActual) + pesoArista < pesos.get(vecino_id)) {
                    pesos.put(vecino_id, pesos.get(idActual) + pesoArista);
                    previo.put(vecino_id, idActual);
                    cola.add(new ColaPrioritaria(vecino_id, pesos.get(vecino_id)));
                }
            }
        }

        if (pesos.get(destino_id) == Float.MAX_VALUE) {
            return new ResultadoRuta("No hay ruta disponible.");
        }

        LinkedList<String> rutaNodos = reconstruirRutaNombres(previo, destino_id);
        return calcularDetallesRuta(rutaNodos, tipoEvento);
    }

    // ==========================================
    //              MÉTODOS AUXILIARES
    // ==========================================

    private LinkedList<String> reconstruirRutaNombres(HashMap<Long, Long> previo, Long finId) {
        LinkedList<String> ruta = new LinkedList<>();
        Long actualId = finId;

        while (actualId != null) {
            Parada parada = lugar.get(actualId);
            if (parada != null) {
                ruta.addFirst(parada.getNombre());
            } else {
                break;
            }
            actualId = previo.get(actualId);
        }
        return ruta;
    }

    private ResultadoRuta calcularDetallesRuta(LinkedList<String> rutaNodos, String tipoEvento) {
        if (rutaNodos.size() < 2) {
            return new ResultadoRuta("Ruta inválida.");
        }

        Evento evento = getEventoPorNombre(tipoEvento);

        double factorAumentoDistancia = evento.factorDistancia;
        double factorAumentoTiempo = evento.factorTiempo;
        double factorAumentoCosto = evento.factorCosto;

        double costoTotal = 0;
        double distanciaTotal = 0;
        double tiempoTotal = 0;
        int transbordosTotales = Math.max(0, rutaNodos.size() - 2);

        for (int i = 0; i < rutaNodos.size() - 1; i++) {
            String origen = rutaNodos.get(i);
            String destino = rutaNodos.get(i + 1);
            Ruta rutaSegmento = encontrarRutaDirecta(origen, destino);
            if (rutaSegmento != null) {
                double costoSegmento = rutaSegmento.getCosto() * (1.0 + factorAumentoCosto);
                double distanciaSegmento = rutaSegmento.getDistancia() * (1.0 + factorAumentoDistancia);
                double tiempoSegmento = rutaSegmento.getTiempoRecorrido() * (1.0 + factorAumentoTiempo);

                costoTotal += costoSegmento;
                distanciaTotal += distanciaSegmento;
                tiempoTotal += tiempoSegmento;
            }
        }

        return new ResultadoRuta(rutaNodos, costoTotal, distanciaTotal, tiempoTotal, transbordosTotales, tipoEvento);
    }

    private Evento getEventoPorNombre(String tipoEvento) {
        try {
            return Evento.valueOf(tipoEvento.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Evento.NORMAL;
        }
    }

    public Long buscarIdPorNombre(String nombreParada) {
        for (Map.Entry<Long, Parada> entry : lugar.entrySet()) {
            if (entry.getValue().getNombre().equals(nombreParada)) {
                return entry.getKey();
            }
        }
        return null;
    }

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
        private float peso;

        public ColaPrioritaria(Long id, float peso) {
            this.id = id;
            this.peso = peso;
        }

        public Long getId() {
            return id;
        }

        public float getPeso() {
            return peso;
        }
    }
    //Logica de Posible evento
    private final Random rand = new Random();

    public Evento elegirEventoAleatorio() {
        //40% normal, 20% lluvia, 20% choque, 20% huelga
        int r = rand.nextInt(100); //100%
        if (r < 20) return Evento.LLUVIA;
        if (r < 40) return Evento.CHOQUE;
        if (r < 60) return Evento.HUELGA;
        return Evento.NORMAL;
    }

    public Map<String, ResultadoRuta> calcularTodasLasRutasConEvento(Long origen_id, Long destino_id) {
        Map<String, ResultadoRuta> resultados = new HashMap<>();

        if (!lugar.containsKey(origen_id) || !lugar.containsKey(destino_id)) {
            resultados.put("error", new ResultadoRuta("El lugar de inicio o fin no existe."));
            return resultados;
        }

        ResultadoRuta base = dijkstraGeneral(origen_id, destino_id, "eficiente", Evento.NORMAL, "normal");
        if (!base.esAlcanzable()) {
            resultados.put("Error!", new ResultadoRuta("No hay ruta disponible."));
            return resultados;
        }

        Evento evento = elegirEventoAleatorio();
        if(!evento.equals(Evento.NORMAL)){
            String nombreOrigen = lugar.get(origen_id).getNombre();
            String nombreDestino = lugar.get(destino_id).getNombre();

            //Muestra la alerta
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Ocurrió un evento");
            alert.setHeaderText("Sucedió un evento");
            String mensaje = String.format(
                    "La red de transporte fue afectada por: %s.\nEsto puede alterar las rutas de %s a %s.\n",
                    evento.name(), nombreOrigen, nombreDestino
            );
            alert.setContentText(mensaje);
            alert.showAndWait();
        }

        resultados.put("evento", new ResultadoRuta(evento.name()));
        resultados.put("eficiente", dijkstraGeneral(origen_id, destino_id, "eficiente", evento, evento.name()));
        resultados.put("distancia", dijkstraGeneral(origen_id, destino_id, "distancia", evento, evento.name()));
        resultados.put("tiempo", dijkstraGeneral(origen_id, destino_id, "tiempo", evento, evento.name()));
        resultados.put("costo", dijkstraGeneral(origen_id, destino_id, "costo", evento, evento.name()));

        return resultados;
    }


    // ==========================================
    //       MÉTODOS DE CÁLCULO DE RUTAS
    // ==========================================

    public ResultadoRuta calcularRutaMasEficiente(Long origen_id, Long destino_id) {
        return dijkstraGeneral(origen_id, destino_id, "eficiente", Evento.NORMAL, "normal");
    }

    public ResultadoRuta calcularRutaMenorDistancia(Long origen_id, Long destino_id) {
        return dijkstraGeneral(origen_id, destino_id, "distancia", Evento.NORMAL, "normal");
    }

    public ResultadoRuta calcularRutaMenorTiempo(Long origen_id, Long destino_id) {
        return dijkstraGeneral(origen_id, destino_id, "tiempo", Evento.NORMAL, "normal");
    }

    public ResultadoRuta calcularRutaMenorCosto(Long origen_id, Long destino_id) {
        return dijkstraGeneral(origen_id, destino_id, "costo", Evento.NORMAL, "normal");
    }
    // ==========================================
    //             ALGORITMO DE PRIM
    // ==========================================

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

}