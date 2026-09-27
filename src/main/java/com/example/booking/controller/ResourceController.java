package com.example.booking.controller;

import com.example.booking.dto.PageResponse;
import com.example.booking.dto.ResourceDto;
import com.example.booking.service.ResourceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/resources")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ResourceController {
    @Autowired
    private ResourceService resourceService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResourceDto> createResource(@Valid @RequestBody ResourceDto resourceDto) {
        ResourceDto createdResource = resourceService.createResource(resourceDto);
        return ResponseEntity.ok(createdResource);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResourceDto> getResourceById(@PathVariable Long id) {
        ResourceDto resource = resourceService.getResourceById(id);
        return ResponseEntity.ok(resource);
    }

    @GetMapping
    public ResponseEntity<PageResponse<ResourceDto>> getAllAvailableResources(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        PageResponse<ResourceDto> resources = resourceService.getAllAvailableResources(page, size, sortBy, sortDir);
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<ResourceDto>> searchResources(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        
        PageResponse<ResourceDto> resources = resourceService.searchResources(
                page, size, sortBy, sortDir, name, minPrice, maxPrice);
        return ResponseEntity.ok(resources);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResourceDto> updateResource(@PathVariable Long id, @Valid @RequestBody ResourceDto resourceDto) {
        ResourceDto updatedResource = resourceService.updateResource(id, resourceDto);
        return ResponseEntity.ok(updatedResource);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteResource(@PathVariable Long id) {
        resourceService.deleteResource(id);
        return ResponseEntity.noContent().build();
    }
}