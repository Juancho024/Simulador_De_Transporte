package Model;

public enum Evento {
    NORMAL(0.0, 0.0, 0.0, 0.0),
    LLUVIA(0.05, 0.15, 0.0, 0.15),   //(distancia, tiempo, costo, eficiente)
    CHOQUE(0.10, 0.50, 0.10, 0.40), // poco costo y distancia
    HUELGA(0.02, 0.25, 0.35, 0.30);  //poca distancia en huelga

    public final double factorDistancia;
    public final double factorTiempo;
    public final double factorCosto;
    public final double factorEficiente;

    Evento(double factorDistancia, double factorTiempo, double factorCosto, double factorEficiente) {
        this.factorDistancia = factorDistancia;
        this.factorTiempo = factorTiempo;
        this.factorCosto = factorCosto;
        this.factorEficiente = factorEficiente;
    }
}