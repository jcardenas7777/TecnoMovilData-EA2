package tecnomovil.model;

public record EstadoRuta(String ruta, long usos, int capacidad, double ocupacion, Nivel nivel) {
public enum Nivel { NORMAL, CRITICA }
public boolean esCritica() {
return nivel == Nivel.CRITICA;
}
}
