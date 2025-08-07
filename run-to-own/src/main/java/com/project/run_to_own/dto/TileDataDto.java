package com.project.run_to_own.dto;

public record TileDataDto(
        String h3Index,
        String ownerName,
        boolean isCurrentUserOwner
) {}