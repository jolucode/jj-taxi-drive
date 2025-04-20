package org.jjdrive.repository;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.jjdrive.entities.Product;

@ApplicationScoped
public class ProductRepository implements ReactivePanacheMongoRepository<Product> {}
