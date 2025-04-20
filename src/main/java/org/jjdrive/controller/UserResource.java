package org.jjdrive.controller;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.bson.types.ObjectId;
import org.jjdrive.entities.User;
import org.jjdrive.entities.UserType;
import org.jjdrive.repository.UserRepository;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    UserRepository repo;

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> getByIdReactive(@PathParam("id") String id) {
        return repo.findById(new ObjectId(id))
                .onItem().ifNotNull().transform(user -> Response.ok(user).build())
                .onItem().ifNull().continueWith(Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    public Uni<Response> createReactive(User user) {
        return repo.persist(user)
                .onItem().transform(u -> Response.status(Response.Status.CREATED).entity(u).build());
    }

    @PUT
    @Path("/{id}")
    public Uni<Response> updateUserReactive(@PathParam("id") String id, User updatedUser) {
        return repo.findById(new ObjectId(id))
                .onItem().ifNotNull().transformToUni(userExist -> {
                    // Actualiza campos principales
                    userExist.type = updatedUser.type;
                    userExist.name = updatedUser.name;
                    userExist.password = updatedUser.password;
                    userExist.phone = updatedUser.phone;
                    userExist.email = updatedUser.email;

                    // Actualiza vehículo solo si aplica
                    if (userExist.type == UserType.DRIVER && updatedUser.vehicle != null) {
                        userExist.vehicle = updatedUser.vehicle;
                    } else {
                        userExist.vehicle = null;
                    }

                    // Encadena la actualización de forma reactiva
                    return repo.update(userExist).onItem().transform(userUpdate -> Response.ok(userUpdate).build());
                })
                .onItem().ifNull().continueWith(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    public Uni<Response> listAllReactive() {
        return repo.listAll()
                .onItem()
                .transform(listUsers -> Response.ok(listUsers).build());
    }

    @DELETE
    @Path("/{id}")
    public Uni<Response> deleteReactive(@PathParam("id") String id) {
        return repo.deleteById(new ObjectId(id))
                .onItem()
                .transform(aBoolean -> {
                    if (aBoolean) {
                        return Response.noContent().build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND)
                                .entity("User no encontrado con id : " + id)
                                .build();
                    }
                });
    }
}
