package org.jjdrive.controller;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.bson.types.ObjectId;
import org.jjdrive.entities.User;
import org.jjdrive.entities.UserType;
import org.jjdrive.entities.dto.CredentialRequest;
import org.jjdrive.entities.dto.UserTypeRequest;
import org.jjdrive.entities.dto.VehicleRequest;
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

    @POST
    public Uni<Response> createReactive(User user) {
        return repo.persist(user)
                .onItem().transform(u -> Response.status(Response.Status.CREATED).entity(u).build());
    } //endpoint para crear usuarios con POST /users

    @POST
    @Path("/auth/login")
    public Uni<Response> login(CredentialRequest request) {
        return repo.findByEmail(request.email)
                .onItem().ifNotNull().transform(user -> {
                    if (user.password.equals(request.password)) {
                        return Response.ok(user).build();
                    } else {
                        return Response.status(Response.Status.UNAUTHORIZED).entity("Contraseña incorrecta").build();
                    }
                })
                .onItem().ifNull().continueWith(Response.status(Response.Status.NOT_FOUND).entity("Usuario no encontrado").build());
    }

    @PUT
    @Path("/{id}/type")
    public Uni<Response> updateType(@PathParam("id") String id, UserTypeRequest request) {
        return repo.findById(new ObjectId(id))
                .onItem().ifNotNull().transformToUni(user -> {
                    user.type = request.type;
                    return repo.update(user).onItem().transform(u -> Response.ok(u).build());
                })
                .onItem().ifNull().continueWith(Response.status(Response.Status.NOT_FOUND).build());
    }

    @PUT
    @Path("/{id}/vehicle")
    public Uni<Response> updateVehicle(@PathParam("id") String id, VehicleRequest request) {
        return repo.findById(new ObjectId(id))
                .onItem().ifNotNull().transformToUni(user -> {
                    if (user.type != UserType.DRIVER) {
                        return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                                .entity("Solo los conductores pueden tener vehículo").build());
                    }
                    user.vehicle = request.vehicle;
                    return repo.update(user).onItem().transform(u -> Response.ok(u).build());
                })
                .onItem().ifNull().continueWith(Response.status(Response.Status.NOT_FOUND).build());
    }


}
