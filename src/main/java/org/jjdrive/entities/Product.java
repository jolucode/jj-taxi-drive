package org.jjdrive.entities;


import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;


@MongoEntity(collection = "products")
public class Product extends PanacheMongoEntity {
    public String code;
    public String name;
    public String description;
}