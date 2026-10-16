## Parcial Práctico Corte 2 – REST API Blueprints (Java 21 / Spring Boot 3.3.x)
# Escuela Colombiana de Ingeniería – Arquitecturas de Software
# JUAN CAMILO CRISTANCHO VELASQUEZ

---

## 📋 Requisitos
- Java 21
- Maven 3.9+

## ▶️ Ejecución del proyecto
```bash
mvn clean install
mvn spring-boot:run
```
Probar con `curl`:
```bash
curl -s http://localhost:8080/blueprints | jq
curl -s http://localhost:8080/blueprints/john | jq
curl -s http://localhost:8080/blueprints/john/house | jq
curl -i -X POST http://localhost:8080/blueprints -H 'Content-Type: application/json' -d '{ "author":"john","name":"kitchen","points":[{"x":1,"y":1},{"x":2,"y":2}] }'
curl -i -X PUT  http://localhost:8080/blueprints/john/kitchen/points -H 'Content-Type: application/json' -d '{ "x":3,"y":3 }'
```

Abrir en navegador:
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## 🗂️ Estructura de carpetas (arquitectura)

```
src/main/java/edu/eci/arsw/blueprints
  ├── model/         # Entidades de dominio: Blueprint, Point
  ├── persistence/   # Interfaz + repositorios (InMemory, Postgres)
  │    └── impl/     # Implementaciones concretas
  ├── services/      # Lógica de negocio y orquestación
  ├── filters/       # Filtros de procesamiento (Identity, Redundancy, Undersampling)
  ├── controllers/   # REST Controllers (BlueprintsAPIController)
  └── config/        # Configuración (Swagger/OpenAPI, etc.)
```

> Esta separación sigue el patrón **capas lógicas** (modelo, persistencia, servicios, controladores), facilitando la extensión hacia nuevas tecnologías o fuentes de datos.

---

## 📖 Actividades del parcial

Todo este parcial se ejecuta o se hacen cambios principalmente en la clase controller (BlueprintsAPIController).

### 1. Buenas prácticas de API REST (Path)
- Cambia el path base de los controladores a `/api/v1/blueprints`.

## Solución

```java
    @RestController
    //PUNTO 1: Modificacion del path base del controlador a la ruta especificada
    @RequestMapping("/api/v1/blueprints")
    public class BlueprintsAPIController {
  ```



### 2. Buenas prácticas de API REST (Errores Http)
- Usa **códigos HTTP** correctos:
    - `200 OK` (consultas exitosas).  Todas las peticiones GET.
    - `201 Created` (creación).  La peticioón POST.
    - `202 Accepted` (actualizaciones).  La petición PUT.
    - `400 Bad Request` (datos inválidos). Peticiones POST y PUT.
    - `404 Not Found` (recurso inexistente o no se encuentra la data solicitada). Peticiones GET y PUT.
### 3. Buenas prácticas de API REST (Respuesta estandar)
- Implementa una clase genérica de respuesta uniforme:
  ```java
  public record ApiResponse<T>(int code, String message, T data) {}
  ```
  Ejemplo JSON:
  ```json
  {
    "code": 200,
    "message": "execute ok",
    "data": { "author": "john", "name": "house", "points": [...] }
  }
  ```
  
## Solucion
Para hacer el punto 2 y 3, empezamos por crear la clase record de respuestas de API, en esta, podemos dar un unico
formato a la salida y usar los codigos http correctos, la ubicacion esta en la misma carpeta del controller.

Empezamos con el primer codigo de GET, el cual tomara la siguiente estructura.

```java
  public record ApiResponse<T>(int code, String message, T data) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(200, "Success", data);
    }
  }

  ```
Asi cambiaremos el metodo del get en el controlador, para que de como respuesta este ApiResponse

```java
    @GetMapping
    public ResponseEntity<ApiResponse<Set<Blueprint>>> getAll() {
        Set<Blueprint> blueprints = services.getAllBlueprints();
        return ResponseEntity.ok(ApiResponse.ok(blueprints));
    }
  ```

Ya funcionando el primero seguimos para los demas metodos del controller. 
### 4. OpenAPI / Swagger
- Configura `springdoc-openapi` en el proyecto.
- Expón documentación automática en `/swagger-ui.html`.
- Actualiza la documentación de los endpoints con `@Operation` y `@ApiResponse` de todos los métodos del controler.

## Solucion

Para formar la documentacion del parcial, usamos @operation que nos ayuda a darle un titulo y una descripcion a cada endpoint
y el @ApiResponse nos ayuda a documentar las distintas salidas esperadas con su respectivo codigo http.

Un ejemplo se ve en el metodo GET, donde la documentacion se ve de esta manera

```java
    @Operation(
        summary = "Get de todos los BluePrints",
        description = "Retorna una lista de todos los planos guardados en el sistema"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Blueprints retornados existosamente",
            content = @Content(schema = @Schema(implementation = Blueprint.class))
    )
  ```

 y en swagger de la siguiente manera

![](img/get.png)

Y seguimos el mismo patron para los demas metodos.


