package org.jjdrive.controller;

import io.quarkus.redis.client.RedisClient;
import io.vertx.redis.client.Response;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.bson.types.ObjectId;
import org.jjdrive.entities.Ride;
import org.jjdrive.entities.dto.AceptarViajeRequest;
import org.jjdrive.entities.dto.Location;
import org.jjdrive.entities.dto.SolicitudViajeRequest;
import org.jjdrive.repository.RideRepository;
import org.jjdrive.websocket.WebSocketHandler;

import java.util.*;

@Path("/pasajeros/viajes")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class RidePasajeroResource {

    @Inject
    RedisClient redis;

    @Inject
    RideRepository rideRepo;

    @Inject
    WebSocketHandler socketHandler;

    @POST
    public jakarta.ws.rs.core.Response crearViaje(SolicitudViajeRequest req) {
        Location origen = new Location(req.latOrigen, req.lngOrigen);
        Location destino = new Location(req.latDestino, req.lngDestino);

        Ride ride = new Ride();
        ride.id = new ObjectId();
        ride.pasajeroId = req.pasajeroId;
        ride.origen = origen;
        ride.destino = destino;
        ride.presupuesto = req.presupuesto;
        ride.estado = "BUSCANDO";
        rideRepo.persist(ride);

        Response redisKeys = redis.keys("status:*");
        if (redisKeys == null || redisKeys.size() == 0) {
            return jakarta.ws.rs.core.Response.ok(Collections.emptyList()).build();
        }

        List<String> claves = new ArrayList<>();
        for (int i = 0; i < redisKeys.size(); i++) {
            claves.add(redisKeys.get(i).toString());
        }

        List<String> conductoresOnline = new ArrayList<>();
        for (String redisKey : claves) {
            String userId = redisKey.replace("status:", "");
            Response estado = redis.get(redisKey);
            if (estado != null && "ONLINE".equalsIgnoreCase(estado.toString())) {
                conductoresOnline.add(userId);
            }
        }

        List<String> notificados = new ArrayList<>();
        for (String userId : conductoresOnline) {
            Response ubicacionRaw = redis.get("location:" + userId);
            if (ubicacionRaw != null) {
                Location loc = parseLocation(ubicacionRaw.toString());
                double distancia = distanciaKm(origen, loc);
                if (distancia <= 2.0) {
                    socketHandler.enviarMensaje(userId, "📢 ¡Nuevo viaje disponible! ID: " + ride.id);
                    notificados.add(userId);
                }
            }
        }

        return jakarta.ws.rs.core.Response.ok(Map.of(
                "viajeId", ride.id.toString(),
                "pasajeroId", req.pasajeroId,
                "conductoresNotificados", notificados
        )).build();
    }

    @POST
    @Path("/viaje/aceptar")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public jakarta.ws.rs.core.Response aceptarViaje(AceptarViajeRequest req) {
        ObjectId id;
        try {
            id = new ObjectId(req.viajeId);
        } catch (Exception e) {
            return jakarta.ws.rs.core.Response.status(jakarta.ws.rs.core.Response.Status.BAD_REQUEST).entity("ID inválido").build();
        }

        Ride actualizado = rideRepo.asignarViajeSiDisponible(id, req.conductorId);
        if (actualizado == null) {
            return jakarta.ws.rs.core.Response.status(jakarta.ws.rs.core.Response.Status.CONFLICT)
                    .entity("Este viaje ya fue aceptado por otro conductor").build();
        }

        return jakarta.ws.rs.core.Response.ok(Map.of(
                "rideId", actualizado.id.toString(),
                "estado", actualizado.estado,
                "conductorId", actualizado.conductorId
        )).build();
    }

    private Location parseLocation(String json) {
        Location loc = new Location();
        String[] parts = json.replace("{", "").replace("}", "").replace("\"", "").split(",");
        for (String part : parts) {
            String[] kv = part.split(":");
            if (kv[0].trim().equals("lat")) loc.lat = Double.parseDouble(kv[1]);
            if (kv[0].trim().equals("lng")) loc.lng = Double.parseDouble(kv[1]);
        }
        return loc;
    }

    private double distanciaKm(Location a, Location b) {
        final int R = 6371;
        double latDist = Math.toRadians(b.lat - a.lat);
        double lonDist = Math.toRadians(b.lng - a.lng);
        double h = Math.sin(latDist / 2) * Math.sin(latDist / 2)
                + Math.cos(Math.toRadians(a.lat)) * Math.cos(Math.toRadians(b.lat))
                * Math.sin(lonDist / 2) * Math.sin(lonDist / 2);
        return R * 2 * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h));
    }

    @GET
    @Path("/{rideId}")
    public jakarta.ws.rs.core.Response obtenerEstadoViaje(@PathParam("rideId") String rideId) {
        if (rideId == null || rideId.length() != 24) {
            return jakarta.ws.rs.core.Response.status(jakarta.ws.rs.core.Response.Status.BAD_REQUEST)
                    .entity("El ID proporcionado no es válido. Debe tener 24 caracteres hexadecimales.").build();
        }

        Ride ride = rideRepo.findByIdOptional(new ObjectId(rideId)).orElse(null);

        if (ride == null) {
            return jakarta.ws.rs.core.Response.status(jakarta.ws.rs.core.Response.Status.NOT_FOUND)
                    .entity("Viaje no encontrado").build();
        }


        // ⚠️ Protección contra ride.id == null
        String safeRideId = ride.id != null ? ride.id.toString() : rideId;

        return jakarta.ws.rs.core.Response.ok(Map.of(
                "rideId", safeRideId,
                "estado", ride.estado != null ? ride.estado : "DESCONOCIDO",
                "pasajeroId", ride.pasajeroId != null ? ride.pasajeroId : "N/A",
                "conductorId", ride.conductorId != null ? ride.conductorId : "N/A",
                "origen", ride.origen,
                "destino", ride.destino,
                "presupuesto", ride.presupuesto
        )).build();
    }
}

