package org.jjdrive.repository;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.jjdrive.entities.Ride;

@ApplicationScoped
public class RideRepository implements PanacheMongoRepository<Ride> {

    public Ride asignarViajeSiDisponible(ObjectId viajeId, String conductorId) {
        Bson filtro = Filters.eq("_id", viajeId);
        Bson condicionEstado = Filters.eq("estado", "BUSCANDO");
        Bson combinacion = Filters.and(filtro, condicionEstado);

        Bson actualizacion = Updates.combine(
                Updates.set("estado", "ASIGNADO"),
                Updates.set("conductorId", conductorId)
        );

        FindOneAndUpdateOptions opciones = new FindOneAndUpdateOptions()
                .returnDocument(ReturnDocument.AFTER);

        return mongoCollection().findOneAndUpdate(combinacion, actualizacion, opciones);
    }

}