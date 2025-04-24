package org.jjdrive.entities;

import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.types.ObjectId;
import org.jjdrive.entities.dto.Location;

import java.time.LocalDateTime;

@MongoEntity(collection = "rides")
public class RideRequest {
    public ObjectId id;
    public ObjectId passengerId;
    public Location origin;
    public Location destination;
    public double proposedFare;
    public String status = "PENDING"; // PENDING, ASSIGNED, CANCELLED, COMPLETED
    public LocalDateTime createdAt = LocalDateTime.now();
    public ObjectId assignedDriverId;
}

