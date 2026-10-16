package edu.eci.arsw.blueprints.controllers;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
//PUNTO 1: Modificacion del path base del controlador a la ruta especificada
@RequestMapping("/api/v1/blueprints")
public class BlueprintsAPIController {

    private final BlueprintsServices services;

    public BlueprintsAPIController(BlueprintsServices services) { this.services = services; }

    // GET /blueprints
    @Operation(
            summary = "Get de todos los BluePrints",
            description = "Retorna una lista de todos los planos guardados en el sistema"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Blueprints retornados existosamente",
            content = @Content(schema = @Schema(implementation = Blueprint.class))
    )
    @GetMapping
    public ResponseEntity<ApiResponse<Set<Blueprint>>> getAll() {
        Set<Blueprint> blueprints = services.getAllBlueprints();
        return ResponseEntity.ok(ApiResponse.ok(blueprints));
    }

    // GET /blueprints/{author}
    @Operation(
            summary = "Get de todos los BluePrints filtrando por autor",
            description = "Retorna una lista con todos los planos de un autor en especifico."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Blueprint encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Blueprint no encontrado")
    })
    @GetMapping("/{author}")
    public ResponseEntity<ApiResponse<?>> byAuthor(@PathVariable String author) {
        try {
            Set<Blueprint> blueprints = services.getBlueprintsByAuthor(author);
            return ResponseEntity.ok(ApiResponse.ok(blueprints));
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(404, e.getMessage()));
        }
    }

    // GET /blueprints/{author}/{bpname}
    @Operation(
            summary = "Retorna un plano en especifico",
            description = "Retorna un plano en especifico filtrando por nombre y autor"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Blueprint encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Blueprint no encontrado")
    })
    @GetMapping("/{author}/{bpname}")
    public ResponseEntity<?> byAuthorAndName(@PathVariable String author, @PathVariable String bpname) {
        try {
            Blueprint blueprint = services.getBlueprint(author, bpname);
            return ResponseEntity.ok(ApiResponse.ok(blueprint));
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(404, e.getMessage()));
        }
    }

    // POST /blueprints
    @Operation(
            summary = "Crear un nuevo plano",
            description = "Registra un nuevo plano con su autor, nombre del plano y una lista de puntos"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201", description = "Blueprint creado exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "La lista de puntos no puede estar vacia"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409", description = "Ya existe un plano con esos parametros")
    })
    @PostMapping
    public ResponseEntity<?> add(@Valid @RequestBody NewBlueprintRequest req) {
        try {
            if (req.points() == null || req.points().isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error(400, "La lista de puntos no puede estar vacia"));
            }

            Blueprint bp = new Blueprint(req.author(), req.name(), req.points());
            services.addNewBlueprint(bp);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.created(bp));

        } catch (BlueprintPersistenceException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(409, e.getMessage()));
        }
    }

    // PUT /blueprints/{author}/{bpname}/points
    @Operation(
            summary = "Añadir un punto a un plano",
            description = "Añade un punto (x, y) a un plano dado, filtrando por autor y nombre del plano."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "202", description = "Punto añadido exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Plano no encontrado")
    })
    @PutMapping("/{author}/{bpname}/points")
    public ResponseEntity<?> addPoint(@PathVariable String author, @PathVariable String bpname,
                                      @RequestBody Point p) {
        try {
            if (p == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error(400, "los puntos son requeridos"));
            }
            services.addPoint(author, bpname, p.x(), p.y());
            return ResponseEntity
                    .status(HttpStatus.ACCEPTED)
                    .body(ApiResponse.accepted("Coordenadas de puntos cambiadas correctamente"));

        } catch (BlueprintNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, e.getMessage()));
        }
    }

    public record NewBlueprintRequest(
            @NotBlank String author,
            @NotBlank String name,
            @Valid java.util.List<Point> points
    ) { }
}
