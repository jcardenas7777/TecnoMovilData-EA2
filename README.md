## Getting Started

1 Los seis requerimientos en lenguaje simple
#	Pregunta que responde el programa	Método
1	¿Cuánta gente entra por cada estación?->	afluenciaPorEstacion
2	¿A qué horas hay más movimiento?->	flujoPorHora, horasPico
3	¿Qué rutas se usan más?->	rutasMasUtilizadas
4	¿Qué recorrido hace cada usuario?->	patronesDeViaje
5	¿Cuánto tarda en promedio ir de una estación a otra?	tiempoPromedioEntreEstaciones
6	¿Qué rutas están sobrecargadas?->	detectarSobrecarga





# TecnoMovil Data — Módulo Analítico Funcional en Java

Este repositorio contiene la solución del proyecto **TecnoMovil Data**, un módulo de analítica de datos masivos para el sistema de transporte masivo de la Alcaldía de TecnoValle. Desarrollado en **Java SE nativo** (JDK 17+ / JDK 25) sin frameworks externos, el proyecto aplica los principios del paradigma de **programación funcional** para procesar registros de validadores electrónicos, sensores IoT y aplicaciones móviles.

---

##  Información del Proyecto

* **Institución:** Institución Universitaria Digital de Antioquia
* **Programa:** Ingeniería de Software y Datos
* **Asignatura:** Programación Orientada a Objetos II[cite: 1]
* **Estudiante:** Jonatan Estiben Cardenas Carrasquilla[cite: 1]
* **Profesor:** Leonardo Montes Gutiérrez[cite: 1]
* **Fecha:** Octubre de 2026[cite: 1]

---

##  Características y Requerimientos Resueltos

El sistema procesa y analiza un dataset simulado de más de 600.000 registros diarios[cite: 1] mediante pipelines declarativos con la API de `Stream` de Java[cite: 1], respondiendo a 6 requerimientos analíticos clave:

1. **Afluencia por Estación:** Conteo total de ingresos (`ENTRADA`) por cada estación del sistema[cite: 1].
2. **Horas Pico:** Determinación de las franjas horarias con mayor tráfico de pasajeros[cite: 1].
3. **Rutas más Utilizadas:** Ranking de las rutas con mayor volumen de abordajes[cite: 1].
4. **Patrones de Viaje por Usuario:** Identificación del recorrido secuencial de cada usuario colapsando estaciones repetidas consecutivas[cite: 1].
5. **Tiempo Promedio entre Estaciones:** Cálculo en minutos de los tiempos de desplazamiento entre parejas origen-destino dentro de una misma ruta[cite: 1].
6. **Detección de Sobrecarga en Rutas:** Evaluación del porcentaje de ocupación sobre la capacidad diaria para clasificar rutas en estado `NORMAL` o `CRITICA`[cite: 1].

---

##  Conceptos Funcionales Aplicados

* **Inmutabilidad:** Modelado del dominio mediante Java `record` (`Registro`, `Ruta`, `EstadoRuta`)[cite: 1] y colecciones de solo lectura (`List.copyOf`, `Collections::unmodifiableMap`)[cite: 1].
* **Funciones Puras:** Métodos analíticos deterministas y libres de efectos secundarios[cite: 1].
* **Funciones de Orden Superior:** Reutilización de abstracciones genéricas como `contarPor` que aceptan predicados y funciones de transformación[cite: 1], así como fábricas de funciones (`superaUmbral`)[cite: 1].
* **Expresiones Lambda y Referencias a Métodos:** Encapsulamiento de comportamiento declarativo (`Registro::estacion`, `ES_ENTRADA`)[cite: 1].
* **Pipelines de Streams:** Eliminación total de ciclos imperativos (`for` / `while`) en las clases de análisis[cite: 1].
* **Paralelismo Seguro:** Procesamiento mediante `parallelStream()` garantizando idénticos resultados frente al modo secuencial[cite: 1].

---

## 📁 Estructura del Código

```text
TecnoMovilData/
└── src/
    └── tecnomovil/
        ├── Main.java                 # Punto de entrada y coordinación
        ├── modelo/
        │   ├── Accion.java           # Enum (ENTRADA, SALIDA)
        │   ├── Registro.java         # Record del evento de transporte
        │   ├── Ruta.java             # Record de configuración de ruta
        │   └── EstadoRuta.java       # Record con resultado de sobrecarga
        ├── datos/
        │   ├── Catalogo.java         # Red simulada y capacidades de ruta
        │   └── GeneradorDatos.java   # Generador determinista de registros
        └── analisis/
            └── AnalizadorTransporte.java # Lógica analítica puramente funcional
```[cite: 1]

---

##  Requisitos e Instalación

* **Java Development Kit (JDK):** Versión 17 o superior (desarrollado y probado en JDK 25)[cite: 1].
* **IDE recomendado:** VS Code, IntelliJ IDEA o Eclipse (no requiere herramientas de construcción externas como Maven o Gradle).

---

## 💻Compilación y Ejecución

Desde la terminal, posicionándose dentro del directorio raíz del código fuente (`src/`):

1. **Compilar el código:**
   ```bash
   javac tecnomovil/Main.java tecnomovil/modelo/*.java tecnomovil/datos/*.java tecnomovil/analisis/*.java