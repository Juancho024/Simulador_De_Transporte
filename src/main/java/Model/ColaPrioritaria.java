package Model;

public class ColaPrioritaria {
    private String nombre;
    private float km;
    private int tiempo;
    private float costo;
    private float numTransbordos;

    public ColaPrioritaria(String nombre, float km) {
        this.nombre = nombre;
        this.km = km;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public float getKm() {
        return km;
    }

    public void setKm(int km) {
        this.km = km;
    }
}
