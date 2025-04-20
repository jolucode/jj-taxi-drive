package org.jjdrive.repository;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.jjdrive.entities.User;

@ApplicationScoped
public class UserRepository implements ReactivePanacheMongoRepository<User> {}
