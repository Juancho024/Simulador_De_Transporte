package Model;

import java.util.LinkedList;
import java.util.List;

public class ResultadoRuta {

    private final List<String> ruta;
    private final double costoTotal;
    private final double distanciaTotal;
    private final double tiempoTotal;
    private final int transbordosTotales;
    private final boolean esAlcanzable;
    private final String mensajeError;
    private String evento;

    // Constructor para una ruta exitosa
    public ResultadoRuta(List<String> ruta, double costoTotal, double distanciaTotal, double tiempoTotal, int transbordosTotales, String evento) {
        this.ruta = ruta;
        this.costoTotal = costoTotal;
        this.distanciaTotal = distanciaTotal;
        this.tiempoTotal = tiempoTotal;
        this.transbordosTotales = transbordosTotales;
        this.esAlcanzable = true;
        this.mensajeError = "";
        this.evento = evento;
    }

    // Constructor para un resultado no exitoso (ruta no encontrada o error)
    public ResultadoRuta(String mensajeError) {
        this.ruta = new LinkedList<>();
        this.costoTotal = 0;
        this.distanciaTotal = 0;
        this.tiempoTotal = 0;
        this.transbordosTotales = 0;
        this.esAlcanzable = false;
        this.mensajeError = mensajeError;
        this.evento = "--";
    }

    // Getters
    public double getCostoTotal() { return costoTotal; }
    public double getDistanciaTotal() { return distanciaTotal; }
    public double getTiempoTotal() { return tiempoTotal; }
    public int getTransbordosTotales() { return transbordosTotales; }
    public boolean esAlcanzable() { return esAlcanzable; }
    public String getMensajeError() { return mensajeError; }
    public String getEvento() {return evento;}
    public void setEvento(String evento) {this.evento = evento; }

    /**
     * Devuelve una representación en cadena del resultado de la ruta.
     * Si la ruta no es alcanzable, devuelve el mensaje de error.
     * Si la ruta es alcanzable, devuelve los detalles de la ruta.
     */
    @Override
    public String toString() {
        if (!esAlcanzable) {
            return mensajeError;
        }
        return "Ruta: " + ruta +
                ", Costo: " + String.format("%.2f", costoTotal) +
                ", Distancia: " + String.format("%.2f", distanciaTotal) + " km" +
                ", Tiempo: " + String.format("%.2f", tiempoTotal) + " min" +
                ", Transbordos: " + transbordosTotales + " Evento: " + evento;
    }

    public List<String> getRuta() {
        return  ruta;
    }
}