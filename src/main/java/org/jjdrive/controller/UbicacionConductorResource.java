package org.jjdrive.controller;

import io.quarkus.redis.client.RedisClient;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;
import org.jjdrive.entities.dto.LocationConductorRequest;

import java.util.List;

@Path("/conductores/ubicacion")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UbicacionConductorResource {

    @Inject
    RedisClient redis;

    @POST
    public jakarta.ws.rs.core.Response actualizarUbicacion(LocationConductorRequest ubicacion) {
        if (ubicacion.userId == null || ubicacion.lat == 0.0 || ubicacion.lng == 0.0) {
            return jakarta.ws.rs.core.Response.status(Status.BAD_REQUEST)
                    .entity("Faltan datos").build();
        }

        // Guardar ubicación
        String locationKey = "location:" + ubicacion.userId;
        String locationValue = String.format("{\"lat\": %.6f, \"lng\": %.6f}",
                ubicacion.lat, ubicacion.lng);
        redis.set(List.of(locationKey, locationValue));

        // Marcar como ONLINE
        redis.set(List.of("status:" + ubicacion.userId, "ONLINE"));

        return jakarta.ws.rs.core.Response.ok("Ubicación y estado actualizados").build();

        /*
        Verifica en Redis con redis-cli:
        get location:662c02ad8653a91f5b3fd9de
        get status:662c02ad8653a91f5b3fd9de
        Listo para que esta información sea usada cuando el pasajero cree una solicitud de viaje.
        */

    }
}
