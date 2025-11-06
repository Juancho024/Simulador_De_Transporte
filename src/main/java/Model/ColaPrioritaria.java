package Model;

public class ColaPrioritaria {
    private Long id;
    private float km;

    public ColaPrioritaria(Long id, float km) {
        this.id = id;
        this.km = km;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public float getKm() {
        return km;
    }
    public void setKm(int km) {
        this.km = km;
    }
}
