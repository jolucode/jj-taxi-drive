package org.jjdrive.repository;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.jjdrive.entities.RideRequest;

@ApplicationScoped
public class RideRequestRepository implements ReactivePanacheMongoRepository<RideRequest> {
}