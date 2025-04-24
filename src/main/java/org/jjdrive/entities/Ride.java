package org.jjdrive.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.mongodb.panache.PanacheMongoEntity;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;
import org.jjdrive.entities.dto.Location;

@MongoEntity(collection = "rides")
public class Ride {

    @BsonId
    public ObjectId id;
    public String pasajeroId;
    public String conductorId;
    public Location origen;
    public Location destino;
    public double presupuesto;
    public String estado;
}