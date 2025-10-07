# Enunciado e Historias de Usuario Del Reto Bootcamp
## Proyecto "CrediYa"

- **Información**:
  https://docs.google.com/spreadsheets/d/1CUuskO2iTXHTEY2jRpdQ8SKzHrS9VUxlZGPjsvWRPDY/edit?gid=1479050755#gid=1479050755

# Proyecto Base Implementando Clean Architecture

## Antes de Iniciar

Empezaremos por explicar los diferentes componentes del proyectos y partiremos de los componentes externos, continuando con los componentes core de negocio (dominio) y por último el inicio y configuración de la aplicación.

Lee el artículo [Clean Architecture — Aislando los detalles](https://medium.com/bancolombia-tech/clean-architecture-aislando-los-detalles-4f9530f35d7a)

# Arquitectura

![Clean Architecture](https://miro.medium.com/max/1400/1*ZdlHz8B0-qu9Y-QO3AXR_w.png)

## Domain

Es el módulo más interno de la arquitectura, pertenece a la capa del dominio y encapsula la lógica y reglas del negocio mediante modelos y entidades del dominio.

## Usecases

Este módulo gradle perteneciente a la capa del dominio, implementa los casos de uso del sistema, define lógica de aplicación y reacciona a las invocaciones desde el módulo de entry points, orquestando los flujos hacia el módulo de entities.

## Infrastructure

### Helpers

En el apartado de helpers tendremos utilidades generales para los Driven Adapters y Entry Points.

Estas utilidades no están arraigadas a objetos concretos, se realiza el uso de generics para modelar comportamientos
genéricos de los diferentes objetos de persistencia que puedan existir, este tipo de implementaciones se realizan
basadas en el patrón de diseño [Unit of Work y Repository](https://medium.com/@krzychukosobudzki/repository-design-pattern-bc490b256006)

Estas clases no puede existir solas y debe heredarse su compartimiento en los **Driven Adapters**

### Driven Adapters

Los driven adapter representan implementaciones externas a nuestro sistema, como lo son conexiones a servicios rest,
soap, bases de datos, lectura de archivos planos, y en concreto cualquier origen y fuente de datos con la que debamos
interactuar.

### Entry Points

Los entry points representan los puntos de entrada de la aplicación o el inicio de los flujos de negocio.

## Application

Este módulo es el más externo de la arquitectura, es el encargado de ensamblar los distintos módulos, resolver las dependencias y crear los beans de los casos de use (UseCases) de forma automática, inyectando en éstos instancias concretas de las dependencias declaradas. Además inicia la aplicación (es el único módulo del proyecto donde encontraremos la función “public static void main(String[] args)”.

**Los beans de los casos de uso se disponibilizan automaticamente gracias a un '@ComponentScan' ubicado en esta capa.**

## 📖 Documentación del API

La documentación del API se genera automáticamente con **springdoc-openapi**.

- **Swagger UI (interfaz gráfica):**  
  👉 http://localhost:9000/swagger-ui.html

- **OpenAPI JSON (especificación en formato JSON):**  
  👉 http://localhost:9000/v3/api-docs

### 📖 Documentación HU-04 Obtener el listado de las solicitudes de préstamo para posteriormente ser revisadas por un Admin/Asesor

#### 📌 Método: getLoanApplicationsForReview
```
public Mono<PageResponse<LoanReviewItem>> getLoanApplicationsForReview(
        List<String> statuses,
        int page,
        int size,
        String token
)
```
#### 🔹 Propósito

Obtiene un listado paginado de solicitudes de préstamo que requieren revisión manual.
El método combina información de dos **orígenes distintos:**

1. **Microservicio de Solicitudes**

   - Proporciona los datos básicos de la solicitud (```amount```, ```termMonth```, ```loanType```, ```status```, ```email```, etc.).

    - Se consulta mediante R2DBC con filtros por estado (```statuses```), paginación (```page```, ```size```) y cálculo de ```totalElements```.

2. **Microservicio de Autenticación**
   - Aporta la información adicional del usuario (```fullName```, ```baseSalary```), consultando por lote de correos electrónicos extraídos de las solicitudes.

#### 🔹 Flujo general

1. **Contar total de solicitudes** en la base de datos (para calcular paginación).

2. **Consultar solicitudes** según ```statuses``` y con paginación (```limit + offset```).

3. **Extraer los emails únicos** de la lista de solicitudes.

4. **Llamar al micro de autenticación** con ese lote de emails → retorna un ```Flux<UserBasicInfo>```.

5. **Convertir ese Flux en un Map** (```email -> UserBasicInfo```) para búsqueda eficiente.

6. **Enriquecer la lista de solicitudes** agregando ```fullName``` y ```baseSalary``` desde el ```Map```.

7. **Construir la respuesta paginada** (```PageResponse<LoanReviewItem>```) con ```content```, ```page```, ```size```, ```totalElements```, ```totalPages```.

#### 🔹 Notas importantes

- **Reactividad 100%**: se evita bloquear hilos, toda la composición es con ```Mono/Flux```.

- ```collectMap```: clave para transformar el ```Flux<UserBasicInfo>``` en un ```Map``` y no duplicar usuarios si hay múltiples solicitudes del mismo email.

- **Control de errores**: si la lista de correos está vacía, se puede retornar un ```new PageResponse<LoanReviewItem>```.

- **Paginación**: se calcula manualmente con ```offset = page * size y Math.ceil(totalElements / size)```.

- **Token**: se pasa al consumer porque el micro de autenticación está protegido con JWT.

#### 🔹 Ejemplo de retorno
```
{
  "content": [
    {
      "monto": 500000,
      "plazo": 12,
      "email": "user@test.com",
      "nombre": "Juan Pérez",
      "tipo_prestamo": "MICROCREDIT",
      "tasa_interes": 1.2,
      "estado_solicitud": "PENDING_REVIEW",
      "salario_base": 2000000,
      "deuda_total_mensual_solicitudes_aprobadas": 800000
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 25,
  "totalPages": 3
}
```

#### 📖 **Tip**:
Cuando tengas que **combinar información de dos orígenes reactivos** (ej. solicitudes + usuarios), piensa en este patrón:

```Flux A (solicitudes)``` → extraer IDs/emails → ```Flux B (usuarios)``` → ```collectMap``` → enriquecer lista → ```Mono<PageResponse>```

---
## 📌 Manejo de deuda total mensual de solicitudes aprobadas

En este proyecto se implementó la lógica de **cálculo automático de la deuda mensual total de un usuario** (campo ```total_month_debt_approved_applications``` en la tabla ```loan_applications```) mediante **triggers en PostgreSQL**.


### 🎯 Objetivo

Mantener actualizado, en todo momento, el valor de la suma de las cuotas mensuales de todos los préstamos aprobados (```status = APPROVED```) asociados a un mismo usuario (identificado por ```email```).

La fórmula utilizada corresponde a la **cuota fija de un préstamo amortizado**:
```
cuota = (monto * tasa_interés) / (1 - (1 + tasa_interés)^(-n))
```
- **monto** → ```amount``` del préstamo

- **tasa_interés** → ```interest_rate``` definido en la ```tabla loan_types```

- **n** → ```term_month``` (número de meses del préstamo)

### ⚡ Triggers implementados
1. ```trg_update_debt_insert```

   - **Evento**: ```BEFORE INSERT``` en ```loan_applications```.

   - **Función asociada**: ```update_total_debt_on_insert```.

    - **Responsabilidad**:

      - Cuando un préstamo se inserta con estado ```APPROVED```, se recalcula la deuda total mensual del usuario.

        - El valor calculado se **propaga a todos los registros de ese usuario**.
        

2. ```trg_update_debt_update```

   - **Evento**: ```AFTER UPDATE``` en ```loan_applications```.

   - **Función asociada**: ```update_total_debt_on_update```.

    - **Responsabilidad**:

      - Recalcular la deuda total cuando ocurre alguno de estos casos:

        - Un préstamo cambia a estado ```APPROVED```.

        - Un préstamo deja de estar en estado ```APPROVED```.

        - Un préstamo aprobado cambia en **monto**, **plazo** o **tipo de préstamo**.

      - El valor calculado se **propaga a todos los registros del usuario**.

### 🔄 Notas técnicas importantes
- El trigger **INSERT** y **UPDATE** es ```AFTER```, porque primero se requiere que el cambio se materialice en la fila antes de recalcular la deuda total.

- Esta estrategia garantiza que todos los registros de un usuario siempre reflejen la deuda consolidada de sus préstamos aprobados.

### 🚨 Limitaciones / Mejoras futuras

- Si un usuario tiene un número muy grande de préstamos, el cálculo con ```SUM``` puede ser costoso.

- Una alternativa sería llevar este valor en una **tabla agregada por usuario** (```user_total_debt```) y actualizarla directamente.

- Actualmente se hace un ```JOIN``` con ```statuses``` para validar el estado ```APPROVED```. Esto podría optimizarse almacenando el ```status_id``` correspondiente en una variable.