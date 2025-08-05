//package com.project.run_to_own.service;
//
//import com.project.run_to_own.dto.TileDTO;
//import com.project.run_to_own.model.Tile;
//import com.project.run_to_own.repositories.TileRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import java.util.List;
//
//@Service
//public class TileService {
//
//    @Autowired
//    private TileRepository tileRepository;
//
//    public List<TileDTO> getTilesOwnedByUser(String userId) {
//        List<Tile> tiles = tileRepository.findByOwnerId(userId);
//
//        return tiles.stream().map(tile ->
//                new TileDTO(
//                        tile.getId(),
//                        tile.getOwnerId(),
//                        tile.getX(),
//                        tile.getY(),
//                        tile.getColor()
//                )
//        ).toList();
//    }
//}