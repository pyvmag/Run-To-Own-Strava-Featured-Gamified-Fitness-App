//package com.project.run_to_own.model;
//
//import jakarta.persistence.*;
//
//@Entity
//@Table(name = "runner_ownership")
//public class RunnerOwnership {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private String runnerId;
//    private String tileId;
//    private String source;
//    private Long timestamp;
//
//    public RunnerOwnership() {}
//
//    public RunnerOwnership(String runnerId, String tileId, String source, Long timestamp) {
//        this.runnerId = runnerId;
//        this.tileId = tileId;
//        this.source = source;
//        this.timestamp = timestamp;
//    }
//
//    public Long getId() { return id; }
//
//    public String getRunnerId() { return runnerId; }
//    public void setRunnerId(String runnerId) { this.runnerId = runnerId; }
//
//    public String getTileId() { return tileId; }
//    public void setTileId(String tileId) { this.tileId = tileId; }
//
//    public String getSource() { return source; }
//    public void setSource(String source) { this.source = source; }
//
//    public Long getTimestamp() { return timestamp; }
//    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
//}