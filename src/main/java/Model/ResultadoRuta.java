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

    // Constructor para una ruta exitosa
    public ResultadoRuta(List<String> ruta, double costoTotal, double distanciaTotal, double tiempoTotal, int transbordosTotales) {
        this.ruta = ruta;
        this.costoTotal = costoTotal;
        this.distanciaTotal = distanciaTotal;
        this.tiempoTotal = tiempoTotal;
        this.transbordosTotales = transbordosTotales;
        this.esAlcanzable = true;
        this.mensajeError = "";
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
    }

    // Getters
    public double getCostoTotal() { return costoTotal; }
    public double getDistanciaTotal() { return distanciaTotal; }
    public double getTiempoTotal() { return tiempoTotal; }
    public int getTransbordosTotales() { return transbordosTotales; }
    public boolean esAlcanzable() { return esAlcanzable; }
    public String getMensajeError() { return mensajeError; }

    @Override
    public String toString() {
        if (!esAlcanzable) {
            return mensajeError;
        }
        return "Ruta: " + ruta +
                ", Costo: " + String.format("%.2f", costoTotal) +
                ", Distancia: " + String.format("%.2f", distanciaTotal) + " km" +
                ", Tiempo: " + String.format("%.2f", tiempoTotal) + " min" +
                ", Transbordos: " + transbordosTotales;
    }

    public List<String> getRuta() {
        return  ruta;
    }
}