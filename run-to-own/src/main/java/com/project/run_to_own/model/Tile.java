//package com.project.run_to_own.model;
//
//import jakarta.persistence.*;
//
//@Entity
//@Table(name = "tiles")
//public class Tile {
//
//    @Id
//    private String id;
//
//    private int x;
//    private int y;
//    private String color;
//
//    @Column(name = "owner_id")
//    private String ownerId;
//
//    public Tile() {}
//
//    public Tile(String id, int x, int y, String color, String ownerId) {
//        this.id = id;
//        this.x = x;
//        this.y = y;
//        this.color = color;
//        this.ownerId = ownerId;
//    }
//
//    public String getId() { return id; }
//    public void setId(String id) { this.id = id; }
//
//    public int getX() { return x; }
//    public void setX(int x) { this.x = x; }
//
//    public int getY() { return y; }
//    public void setY(int y) { this.y = y; }
//
//    public String getColor() { return color; }
//    public void setColor(String color) { this.color = color; }
//
//    public String getOwnerId() { return ownerId; }
//    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
//}