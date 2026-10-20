package tecnomovil.model;
import java.time.LocalDateTime;
import java.util.Objects;


public record Registro(int idUsuario, String ruta, String estacion,
                       Accion accion, LocalDateTime timestamp) {

    public Registro {
        Objects.requireNonNull(ruta, "ruta");
        Objects.requireNonNull(estacion, "estacion");
        Objects.requireNonNull(accion, "accion");
        Objects.requireNonNull(timestamp, "timestamp");
    }
}  