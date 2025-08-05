//package com.project.run_to_own.controllers;
//
//import com.project.run_to_own.dto.TileDTO;
//import com.project.run_to_own.model.RunnerOwnership;
//import com.project.run_to_own.repositories.RunnerOwnershipRepository;
//import com.project.run_to_own.service.TileService;
//
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/explore")
//public class ExploreController {
//
//    private final TileService tileService;
//    private final RunnerOwnershipRepository ownershipRepository;
//
//    public ExploreController(TileService tileService, RunnerOwnershipRepository ownershipRepository) {
//        this.tileService = tileService;
//        this.ownershipRepository = ownershipRepository;
//    }
//
//    @GetMapping("/tiles")
//    public List<TileDTO> getUserOwnedTiles(@RequestHeader("runnerId") String runnerId) {
//        return tileService.getTilesOwnedByUser(runnerId);
//    }
//
//    @GetMapping("/ownership")
//    public List<RunnerOwnership> getOwnership(@RequestHeader("runnerId") String runnerId) {
//        return ownershipRepository.findByRunnerId(runnerId);
//    }
//}