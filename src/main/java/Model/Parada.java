package Model;

public class Parada {
    private String nombre;
    private String tipoTransporte;
    private int posicionx;
    private int posiciony;

    public Parada(String nombre, String tipoTransporte, int posiciony, int posicionx) {
        this.nombre = nombre;
        this.posiciony = posiciony;
        this.posicionx = posicionx;
        this.tipoTransporte = tipoTransporte;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipoTransporte() {
        return tipoTransporte;
    }

    public void setTipoTransporte(String tipoTransporte) {
        this.tipoTransporte = tipoTransporte;
    }

    public int getPosicionx() {
        return posicionx;
    }

    public void setPosicionx(int posicionx) {
        this.posicionx = posicionx;
    }

    public int getPosiciony() {
        return posiciony;
    }

    public void setPosiciony(int posiciony) {
        this.posiciony = posiciony;
    }
}
