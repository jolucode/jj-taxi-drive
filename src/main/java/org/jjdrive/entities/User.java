package org.jjdrive.entities;


import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.types.ObjectId;


@MongoEntity(collection = "users")
public class User extends PanacheMongoEntity {

    //@JsonIgnore // 👈 Swagger ya no lo documentará
    public ObjectId id;

    public UserType type;    // 👈 ahora es enum   // "PASSENGER" or "DRIVER"
    public String name;
    public String password;
    public String phone;
    public String email;
    public String clientIdPhone;

    public Vehicle vehicle;      // Optional: only for DRIVER

    public static class Vehicle {
        public String plate;
        public String model;
        public String color;
    }
}