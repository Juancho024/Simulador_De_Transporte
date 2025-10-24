package Model;

public class Ruta {
    private Parada origen;
    private Parada destino;
    private int peso;
    private int tiempoRecorrido;
    private float costo;
    private float numTransbordos;
    private String posibleEvento;

    public Ruta(Parada origen, Parada destino, int peso) {
        this.origen = origen;
        this.destino = destino;
        this.peso = peso;
    }

    public Ruta(Parada origen, Parada destino, int peso, int tiempoRecorrido, float costo, float numTransbordos, String posibleEvento) {
        this.origen = origen;
        this.destino = destino;
        this.peso = peso;
        this.tiempoRecorrido = tiempoRecorrido;
        this.costo = costo;
        this.numTransbordos = numTransbordos;
        this.posibleEvento = posibleEvento;
    }

    private void ConsecuenciasEvento() {
        // Lógica para manejar las consecuencias del evento
        if(posibleEvento.equals("Accidente")) {
            tiempoRecorrido *= 2;
        }
        if(posibleEvento.equals("Manifestación")) {
            tiempoRecorrido *= 3;
        }
        if(posibleEvento.equals("LLuvias intensas")) {
            costo *= 1.5;
            tiempoRecorrido *= 2;
        }
        if(posibleEvento.equals("Obras viales")) {
            tiempoRecorrido *= 2;
            peso *= 1.5;
        }
        if(posibleEvento.equals("Normales")) {
            tiempoRecorrido /= 2;
        }
    }

    public String getPosibleEvento() {
        return posibleEvento;
    }
    public void setPosibleEvento(String posibleEvento) {
        this.posibleEvento = posibleEvento;
    }

    public Parada getOrigen() {
        return origen;
    }

    public void setOrigen(Parada origen) {
        this.origen = origen;
    }

    public Parada getDestino() {
        return destino;
    }

    public void setDestino(Parada destino) {
        this.destino = destino;
    }

    public int getPeso() {
        return peso;
    }

    public void setPeso(int peso) {
        this.peso = peso;
    }

    public int getTiempoRecorrido() {
        return tiempoRecorrido;
    }

    public void setTiempoRecorrido(int tiempoRecorrido) {
        this.tiempoRecorrido = tiempoRecorrido;
    }

    public float getCosto() {
        return costo;
    }

    public void setCosto(float costo) {
        this.costo = costo;
    }

    public float getNumTransbordos() {
        return numTransbordos;
    }

    public void setNumTransbordos(float numTransbordos) {
        this.numTransbordos = numTransbordos;
    }
}
