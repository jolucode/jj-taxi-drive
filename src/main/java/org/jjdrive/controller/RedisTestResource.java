package org.jjdrive.controller;

import io.quarkus.redis.client.RedisClient;
import io.vertx.redis.client.Response;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/redis-test")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class RedisTestResource {

    @Inject
    RedisClient redis;

    @GET
    @Path("/set")
    public String set() {
        redis.set(List.of("location:123", "{\"lat\": -12.1, \"lng\": -77.03}"));
        redis.set(List.of("status:123", "ONLINE"));
        return "Ubicación y estado guardados en Redis";
    }

    @GET
    @Path("/get")
    public String get() {
        Response location = redis.get("location:123");
        Response status = redis.get("status:123");

        return "Ubicación: " + (location != null ? location.toString() : "N/A") +
                "\nEstado: " + (status != null ? status.toString() : "N/A");
    }
}
