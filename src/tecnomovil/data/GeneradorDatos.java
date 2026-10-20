package tecnomovil.data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SplittableRandom;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import tecnomovil.model.Accion;
import tecnomovil.model.Ruta;
import tecnomovil.model.Registro;
public final class GeneradorDatos {
private GeneradorDatos() {
}
public static final LocalDate DIA = LocalDate.of(2026, 3, 16);
public static final Comparator<Registro> ORDEN_CRONOLOGICO = Comparator
.comparing(Registro::timestamp)
.thenComparingInt(Registro::idUsuario)
.thenComparing(Registro::accion);
// Franjas horarias (hora, peso). Separadas con holgura para que los viajes de un mismo
// usuario nunca se solapen. Los pesos crean horas pico a las 7-8 y a las 17-18.

private static final List<Integer> HORAS_MANANA = ponderar(new int[][] {{5, 2}, {6, 6}, {7, 10}, {8, 10}, {9, 5}});
private static final List<Integer> HORAS_MEDIODIA = ponderar(new int[][] {{11, 3}, {12, 5}, {13, 5}, {14, 3}});
private static final List<Integer> HORAS_TARDE = ponderar(
new int[][] {{16, 5}, {17, 9}, {18, 10}, {19, 7}, {20, 3}, {21, 2}});
private static final List<List<Integer>> FRANJAS = List.of(HORAS_MANANA, HORAS_MEDIODIA, HORAS_TARDE);
private static List<Integer> ponderar(int[][] pares) {
return Arrays.stream(pares)
.flatMap(p -> Collections.nCopies(p[1], p[0]).stream())
.toList();
}
public static List<Registro> generar(int usuarios, long semilla) {
return IntStream.rangeClosed(1, usuarios)
.parallel()
.boxed()
.flatMap(id -> registrosDeUsuario(id, semilla))
.sorted(ORDEN_CRONOLOGICO)
.toList();
}
static Stream<Registro> registrosDeUsuario(int idUsuario, long semilla) {
boolean haceViajeDeMediodia = new SplittableRandom(mezclar(semilla, idUsuario, 99)).nextBoolean();
return IntStream.range(0, FRANJAS.size())
.filter(franja -> franja != 1 || haceViajeDeMediodia)
.boxed()
.flatMap(franja -> viaje(idUsuario, franja, semilla));
}
static Stream<Registro> viaje(int idUsuario, int franja, long semilla) {
// El generador es local a esta llamada: no se comparte, por eso la función sigue
// siendo pura de cara al exterior (misma entrada => misma salida).
SplittableRandom azar = new SplittableRandom(mezclar(semilla, idUsuario, franja));
Ruta ruta = Catalogo.RUTAS_PONDERADAS.get(azar.nextInt(Catalogo.RUTAS_PONDERADAS.size()));
int n = ruta.estaciones().size();
int origen = azar.nextInt(n);
int destino = (origen + 1 + azar.nextInt(n - 1)) % n; // siempre distinto del origen
List<Integer> horas = FRANJAS.get(franja);
LocalDateTime inicio = DIA.atTime(horas.get(azar.nextInt(horas.size())), azar.nextInt(60), azar.nextInt(60));
long segundosPorParada = 150 + azar.nextInt(90); // entre 2,5 y 4 minutos por parada
long duracion = Math.abs(destino - origen) * segundosPorParada;
return Stream.of(
new Registro(idUsuario, ruta.id(), ruta.estaciones().get(origen), Accion.ENTRADA, inicio),

new Registro(idUsuario, ruta.id(), ruta.estaciones().get(destino), Accion.SALIDA,
inicio.plusSeconds(duracion)));
}
private static long mezclar(long semilla, int idUsuario, int viaje) {
return semilla * 0x9E3779B97F4A7C15L + idUsuario * 0xBF58476D1CE4E5B9L + viaje * 0x94D049BB133111EBL;
}
}