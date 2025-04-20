package org.jjdrive.controller;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.bson.types.ObjectId;
import org.jjdrive.entities.Product;
import org.jjdrive.repository.ProductRepository;

import java.util.List;

@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {

    // 🔍 Listar todos los productos
    /*@GET
    public List<Product> listAll() {
        return Product.listAll();
    }*/

    @Inject
    ProductRepository repo;

    // 🔍 Obtener un producto por ID
    @GET
    @Path("/{id}")
    public Product getById(@PathParam("id") String id) {
        Product product = Product.findById(new ObjectId(id));
        if (product == null) {
            throw new NotFoundException("Producto no encontrado con ID " + id);
        }
        return product;
    }

    // ➕ Crear un nuevo producto
    @POST
    public Response create(Product product) {
        product.persist();
        return Response.status(Response.Status.CREATED).entity(product).build();
    }

    // ✏️ Actualizar un producto existente
    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") String id, Product updatedProduct) {
        Product existing = Product.findById(new ObjectId(id));
        if (existing == null) {
            throw new NotFoundException("Producto no encontrado con ID " + id);
        }

        existing.code = updatedProduct.code;
        existing.name = updatedProduct.name;
        existing.description = updatedProduct.description;

        existing.update();
        return Response.ok(existing).build();
    }

    // 🗑️ Eliminar un producto
    @POST
    @Path("/{id}")
    public Response delete(@PathParam("id") String id) {
        boolean deleted = Product.deleteById(new ObjectId(id));
        if (!deleted) {
            throw new NotFoundException("No se pudo eliminar. Producto no encontrado con ID " + id);
        }
        return Response.noContent().build();
    }

    @GET
    public Uni<Response> listAll() {
        return repo.listAll()
                .onItem()
                .transform(listProd -> Response.ok(listProd).build());
    }

    @DELETE
    @Path("/{id}")
    public Uni<Response> delete2(@PathParam("id") String id) {
        return repo.deleteById(new ObjectId(id))
                .onItem()
                .transform(aBoolean -> {
                    if (aBoolean) {
                        return Response.noContent().build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND)
                                .entity("Product no encontrado con id : " + id)
                                .build();
                    }
                });
    }
}
