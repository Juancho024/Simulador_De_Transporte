package Model;

public class Parada {
    private String nombre;
    private int posicionx;
    private int posiciony;

    public Parada(String nombre, int posicionx, int posiciony) {
        this.nombre = nombre;
        this.posicionx = posicionx;
        this.posiciony = posiciony;
    }

    public int getPosicionx() {
        return posicionx;
    }

    public void setPosicionx(int posicionx) {
        this.posicionx = posicionx;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getPosiciony() {
        return posiciony;
    }

    public void setPosiciony(int posiciony) {
        this.posiciony = posiciony;
    }
}
