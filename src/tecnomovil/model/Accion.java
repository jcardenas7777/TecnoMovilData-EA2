package tecnomovil.model;
import java.util.Arrays;
public enum Accion {

    ENTRADA("Entrada"),
    SALIDA("Salida");
private final String texto;

    Accion(String texto) {
        this.texto = texto;
    }

    public String getTexto() {
        return texto;
    }

   public static Accion desde(String texto) {
            return Arrays.stream(values())
            .filter(a -> a.texto.equalsIgnoreCase(texto))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Acción desconocida: " + texto));


            }

} 
