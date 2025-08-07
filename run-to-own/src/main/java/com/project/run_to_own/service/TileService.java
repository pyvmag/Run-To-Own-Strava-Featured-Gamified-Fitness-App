package com.project.run_to_own.service;

import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.LatLng;
import com.project.run_to_own.model.Tile;
import com.project.run_to_own.model.TileUserStats;
import com.project.run_to_own.model.User;
import com.project.run_to_own.repositories.TileRepository;
import com.project.run_to_own.repositories.TileUserStatsRepository;
import com.uber.h3core.H3Core;
import com.uber.h3core.exceptions.LineUndefinedException;
import com.uber.h3core.util.GeoCoord;
import jakarta.transaction.Transactional;
import org.locationtech.jts.geom.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TileService {

    private static final int H3_RESOLUTION = 10;
    private final TileRepository tileRepository;
    private final TileUserStatsRepository tileUserStatsRepository;
    private final H3Core h3;
    private final GeometryFactory geometryFactory;

    @Autowired
    public TileService(TileRepository tileRepository, TileUserStatsRepository tileUserStatsRepository) throws IOException {
        this.tileRepository = tileRepository;
        this.tileUserStatsRepository = tileUserStatsRepository;
        this.h3 = H3Core.newInstance();
        this.geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    }

    @Transactional
    public void processStravaActivity(User user, Map<String, Object> stravaActivity) {
        // ... (code to get polyline and calculate distances remains the same) ...
        Map<String, Object> mapData = (Map<String, Object>) stravaActivity.get("map");
        if (mapData == null || mapData.get("summary_polyline") == null) { return; }
        String encodedPolyline = (String) mapData.get("summary_polyline");
        List<LatLng> path = new EncodedPolyline(encodedPolyline).decodePath();
        if (path.size() < 2) { return; }
        Coordinate[] pathCoords = path.stream().map(latLng -> new Coordinate(latLng.lng, latLng.lat)).toArray(Coordinate[]::new);
        LineString runPath = geometryFactory.createLineString(pathCoords);
        Set<String> h3Indexes = new HashSet<>();
        for (int i = 0; i < path.size() - 1; i++) {
            LatLng start = path.get(i);
            LatLng end = path.get(i);
            long startH3 = h3.geoToH3(start.lat, start.lng, H3_RESOLUTION);
            long endH3 = h3.geoToH3(end.lat, end.lng, H3_RESOLUTION);
            try {
                h3.h3Line(startH3, endH3).forEach(h3Long -> h3Indexes.add(String.valueOf(h3Long)));
            } catch (LineUndefinedException e) {
                System.err.println("Could not calculate H3 line segment, skipping: " + e.getMessage());
            }
        }
        Map<String, Double> tileDistances = new HashMap<>();
        for (String h3Index : h3Indexes) {
            double distanceInMeters = calculateDistanceInTile(runPath, h3Index);
            if (distanceInMeters > 0) {
                tileDistances.put(h3Index, distanceInMeters);
            }
        }

        // This loop contains the critical fix
        for (Map.Entry<String, Double> entry : tileDistances.entrySet()) {
            String h3Index = entry.getKey();
            Double distance = entry.getValue();

            // ✅ CORRECTED LOGIC
            // Find the tile, or if it doesn't exist, create AND SAVE it immediately.
            Tile tile = tileRepository.findById(h3Index).orElseGet(() -> {
                Tile newTile = new Tile(h3Index);
                return tileRepository.save(newTile); // Save the new tile to the DB
            });

            TileUserStats userStats = tileUserStatsRepository.findByUser_IdAndTile_H3Index(user.getId(), h3Index)
                    .orElseGet(() -> new TileUserStats(user, tile));

            userStats.addDistance(distance);
            tileUserStatsRepository.save(userStats);
            updateTileOwner(tile);
        }
    }

    private void updateTileOwner(Tile tile) {
        tileUserStatsRepository.findTopByTile_H3IndexOrderByTotalDistanceInMetersDesc(tile.getH3Index())
                .ifPresent(topRunnerStats -> {
                    User newOwner = topRunnerStats.getUser();
                    if (tile.getOwnerId() == null || !tile.getOwnerId().equals(newOwner.getId())) {
                        System.out.println("Ownership changed for tile " + tile.getH3Index() + ". New owner: " + newOwner.getUsername());
                        tile.setOwnerId(newOwner.getId());
                        tile.setOwnerName(newOwner.getUsername());
                        tileRepository.save(tile);
                    }
                });
    }

    private double calculateDistanceInTile(LineString runPath, String h3Index) {
        List<GeoCoord> boundaryGeoCoords = h3.h3ToGeoBoundary(Long.parseLong(h3Index));
        boundaryGeoCoords.add(boundaryGeoCoords.get(0));
        Coordinate[] boundaryCoords = boundaryGeoCoords.stream().map(g -> new Coordinate(g.lng, g.lat)).toArray(Coordinate[]::new);
        Polygon hexagon = geometryFactory.createPolygon(boundaryCoords);
        Geometry intersection = runPath.intersection(hexagon);
        if (!intersection.isEmpty()) {
            return lengthInDegreesToMeters(intersection.getLength(), intersection.getCentroid().getY());
        }
        return 0.0;
    }

    private double lengthInDegreesToMeters(double lengthInDegrees, double latitude) {
        double latRad = Math.toRadians(latitude);
        return lengthInDegrees * (111132.954 - 559.822 * Math.cos(2 * latRad) + 1.175 * Math.cos(4 * latRad));
    }
}