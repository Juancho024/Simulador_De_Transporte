package Model;

public class Parada {
    private long id;
    private String nombre;
    private String tipoTransporte;
    private int posicionx;
    private int posiciony;
    private byte[] icono; //cambiar  BYTE

    public Parada(String nombre, String tipoTransporte, int posiciony, int posicionx, byte[] icono) {
        this.nombre = nombre;
        this.posiciony = posiciony;
        this.posicionx = posicionx;
        this.tipoTransporte = tipoTransporte;
        this.icono = icono;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public byte[] getIcono() {
        return icono;
    }

    public void setIcono(byte[] icono) {
        this.icono = icono;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Parada parada)) return false;

        return id == parada.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
//    @Override
//    public String toString() {
//        return "Parada{" +
//                "id=" + id +
//                ", nombre='" + nombre + '\'' +
//                ", tipoTransporte='" + tipoTransporte + '\'' +
//                ", posicionX=" + posicionx +
//                ", posicionY=" + posiciony +
//                '}';
//    }
}
