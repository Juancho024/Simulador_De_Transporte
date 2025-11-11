package Model;

public class GrafoInfo {
    private final String id;
    private final String label;

    public GrafoInfo(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof GrafoInfo grafoInfo)) return false;

        return id.equals(grafoInfo.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return label;
    }
}
