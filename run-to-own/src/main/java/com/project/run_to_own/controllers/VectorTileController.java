//package com.project.run_to_own.controllers;
//
//import com.project.run_to_own.model.Athlete;
//import com.project.run_to_own.model.Tile;
//import com.project.run_to_own.repositories.TileRepository;
//import com.uber.h3core.H3Core;
//import com.uber.h3core.util.LatLng;
//import io.github.sebasbaumh.mapbox.vectortile.adapt.jts.JtsAdapter;
//import io.github.sebasbaumh.mapbox.vectortile.adapt.jts.MvtLayerProps;
//import io.github.sebasbaumh.mapbox.vectortile.build.MvtLayerBuild;
//import io.github.sebasbaumh.mapbox.vectortile.spec.VectorTile;
//import jakarta.servlet.http.HttpSession;
//import org.locationtech.jts.geom.Coordinate;
//import org.locationtech.jts.geom.Geometry;
//import org.locationtech.jts.geom.GeometryFactory;
//import org.locationtech.jts.geom.PrecisionModel;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.io.IOException;
//import java.util.List;
//import java.util.Map;
//
//@RestController
//public class VectorTileController {
//
//    private final TileRepository tileRepository;
//    private final H3Core h3;
//    private final GeometryFactory geometryFactory;
//
//    public VectorTileController(TileRepository tileRepository) throws IOException {
//        this.tileRepository = tileRepository;
//        this.h3 = H3Core.newInstance();
//        // 4326 is the standard for Latitude/Longitude.
//        this.geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
//    }
//
//    @GetMapping(value = "/tiles/{z}/{x}/{y}.pbf", produces = "application/vnd.mapbox-vector-tile")
//    public byte[] getTile(@PathVariable int z, @PathVariable int x, @PathVariable int y, HttpSession session) {
//
//        Athlete athlete = (Athlete) session.getAttribute("athlete");
//        Long currentUserId = (athlete != null) ? athlete.getId() : -1L;
//
//        List<Tile> ownedTiles = tileRepository.findAll().stream()
//                .filter(tile -> tile.getOwnerId() != null)
//                .toList();
//
//        final int mvtExtent = 4096;
//        VectorTile.Tile.Layer.Builder layerBuilder = MvtLayerBuild.newLayerBuilder("tiles", mvtExtent);
//        MvtLayerProps layerProps = new MvtLayerProps();
//
//        for (Tile tile : ownedTiles) {
//            List<LatLng> boundary = h3.cellToBoundary(tile.getH3Index());
//            boundary.add(boundary.get(0));
//            Coordinate[] coords = boundary.stream()
//                    .map(g -> new Coordinate(g.lng, g.lat))
//                    .toArray(Coordinate[]::new);
//            Geometry hexagon = geometryFactory.createPolygon(coords);
//
//            Map<String, Object> properties = Map.of(
//                    "ownerName", tile.getOwnerName(),
//                    "isCurrentUserOwner", tile.getOwnerId().equals(currentUserId)
//            );
//
//            List<VectorTile.Tile.Feature> features = JtsAdapter.toFeatures(hexagon, layerProps, (geom) -> properties);
//            layerBuilder.addAllFeatures(features);
//        }
//
//        MvtLayerBuild.writeProps(layerBuilder, layerProps);
//
//        VectorTile.Tile.Builder tileBuilder = VectorTile.Tile.newBuilder();
//        tileBuilder.addLayers(layerBuilder.build());
//        return tileBuilder.build().toByteArray();
//    }
//}