package tecnomovil.analytics;

import static java.util.stream.Collectors.averagingLong;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableMap;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import tecnomovil.model.Accion;
import tecnomovil.model.EstadoRuta;
import tecnomovil.model.Registro;

public  final class AnalizadorTransporte {
    // Funciones reutilizables (lambdas guardadas en constantes)
public static final Predicate<Registro> ES_ENTRADA = r -> r.accion() == Accion.ENTRADA;
public static final Function<Registro, Integer> HORA_DEL_DIA = r -> r.timestamp().getHour();
// Único campo: final, así que el analizador no cambia nunca
private final boolean paralelo;
public AnalizadorTransporte(boolean paralelo) {
this.paralelo = paralelo;
}
// Elige stream() o parallelStream(). El resto del código no se entera.
private <T> Stream<T> fuente(Collection<T> coleccion) {
return paralelo ? coleccion.parallelStream() : coleccion.stream();
}
// ===================== TAREA 1 =====================
public Map<String, Long> afluenciaPorEstacion(List<Registro> registros) {
return contarPor(registros, ES_ENTRADA, Registro::estacion);
}
// ===================== utilidades =====================

// Filtra, clasifica y cuenta. Las tareas 1, 3 y 6 usan esta misma función.
private <K> Map<K, Long> contarPor(
List<Registro> registros, Predicate<Registro> filtro, Function<Registro, K> clasificador) {
return fuente(registros)
.filter(filtro)
.collect(collectingAndThen(
groupingBy(clasificador, counting()),
Collections::unmodifiableMap));
}
// Las n entradas con mayor valor (de mayor a menor), en una lista inmutable
public static <K extends Comparable<K>, V extends Comparable<V>> List<Map.Entry<K, V>> topN(
Map<K, V> mapa, int n) {
return mapa.entrySet().stream()
.sorted(Comparator.comparing((Map.Entry<K, V> e) -> e.getValue()).reversed()
.thenComparing(e -> e.getKey()))
.limit(n)
.map(e -> Map.entry(e.getKey(), e.getValue()))
.toList();
    
}
// ===================== TAREA 2 =====================
// Entradas por hora del día, ordenado por hora (TreeMap)
public SortedMap<Integer, Long> flujoPorHora(List<Registro> registros) {
return fuente(registros)
.filter(ES_ENTRADA)
.collect(collectingAndThen(
groupingBy(HORA_DEL_DIA, TreeMap::new, counting()),
Collections::unmodifiableSortedMap));
}
// Las n horas con más flujo
public List<Map.Entry<Integer, Long>> horasPico(List<Registro> registros, int n) {
return topN(flujoPorHora(registros), n);
}
// ===================== TAREA 3 =====================
public List<Map.Entry<String, Long>> rutasMasUtilizadas(List<Registro> registros, int n) {
return topN(contarPor(registros, ES_ENTRADA, Registro::ruta), n);
}

// ===================== TAREA 4 =====================
// Para cada usuario: estaciones que visitó, en el orden de los registros
public Map<Integer, List<String>> patronesDeViaje(List<Registro> registros) {
return fuente(registros)
.collect(collectingAndThen(
groupingBy(Registro::idUsuario, mapping(Registro::estacion, toList())),
porUsuario -> fuente(porUsuario.entrySet())
.collect(toUnmodifiableMap(
Map.Entry::getKey,
e -> colapsarConsecutivos(e.getValue())))));
}
// [A, B, B, C] -> [A, B, C] (quita repetidas que aparecen seguidas)
static List<String> colapsarConsecutivos(List<String> estaciones) {
return IntStream.range(0, estaciones.size())
.filter(i -> i == 0 || !estaciones.get(i).equals(estaciones.get(i - 1)))
.mapToObj(estaciones::get)
.toList();
}

// ===================== TAREA 5 =====================
// Un tramo: de qué estación a cuál y cuántos segundos tardó
private record Tramo(String origen, String destino, long segundos) {
String clave() {
return origen + " → " + destino;
}
}
// Tiempo promedio (en minutos) entre estaciones, clave "A → B"
public Map<String, Double> tiempoPromedioEntreEstaciones(List<Registro> registros) {
Map<Integer, List<Registro>> porUsuario = fuente(registros).collect(groupingBy(Registro::idUsuario));
return fuente(porUsuario.values())
.flatMap(AnalizadorTransporte::tramosDe)
.collect(collectingAndThen(
groupingBy(Tramo::clave,
collectingAndThen(averagingLong(Tramo::segundos), seg -> seg / 60.0)),
Collections::unmodifiableMap));
}
// Recorre los registros de UN usuario por parejas consecutivas (sin for)
private static Stream<Tramo> tramosDe(List<Registro> delUsuario) {
List<Registro> ordenados = delUsuario.stream()
.sorted(Comparator.comparing(Registro::timestamp))
.toList();
return IntStream.range(0, ordenados.size() - 1)
.mapToObj(i -> Map.entry(ordenados.get(i), ordenados.get(i + 1)))
.filter(par -> esTramo(par.getKey(), par.getValue()))
.map(par -> new Tramo(
par.getKey().estacion(),
par.getValue().estacion(),
Duration.between(par.getKey().timestamp(), par.getValue().timestamp()).toSeconds()));
}
// Es tramo si: ENTRADA seguida de SALIDA en la misma ruta
private static boolean esTramo(Registro primero, Registro segundo) {
return primero.accion() == Accion.ENTRADA
&& segundo.accion() == Accion.SALIDA
&& primero.ruta().equals(segundo.ruta());
}

// ===================== TAREA 6 =====================
// Fábrica de criterios: devuelve una función "¿supera este umbral?"
public static Predicate<Double> superaUmbral(double umbral) {
return ocupacion -> ocupacion > umbral;
}
// ocupación = abordajes / capacidad. El criterio de "crítica" llega como parámetro.
public List<EstadoRuta> detectarSobrecarga(
List<Registro> registros, Map<String, Integer> capacidadDiaria, Predicate<Double> esCritica) {
Map<String, Long> usos = contarPor(registros, ES_ENTRADA, Registro::ruta);
return capacidadDiaria.entrySet().stream()
.map(e -> evaluar(e.getKey(), usos.getOrDefault(e.getKey(), 0L), e.getValue(), esCritica))
.sorted(Comparator.comparingDouble(EstadoRuta::ocupacion).reversed()
.thenComparing(EstadoRuta::ruta))
.toList();
}
// Función pura: arma el estado de una ruta solo con sus parámetros
static EstadoRuta evaluar(String ruta, long usos, int capacidad, Predicate<Double> esCritica) {
double ocupacion = (double) usos / capacidad;
EstadoRuta.Nivel nivel = esCritica.test(ocupacion) ? EstadoRuta.Nivel.CRITICA : EstadoRuta.Nivel.NORMAL;
return new EstadoRuta(ruta, usos, capacidad, ocupacion, nivel);
}
// ===================== EXTRA: composición de funciones =====================
public static Predicate<Registro> enFranja(int desde, int hasta) {
return r -> HORA_DEL_DIA.apply(r) >= desde && HORA_DEL_DIA.apply(r) <= hasta;
}
public Map<String, Long> afluenciaPorEstacionEnFranja(List<Registro> registros, int desde, int hasta) {
return contarPor(registros, ES_ENTRADA.and(enFranja(desde, hasta)), Registro::estacion);
}

}