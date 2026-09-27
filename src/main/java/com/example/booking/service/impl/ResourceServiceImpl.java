package com.example.booking.service.impl;

import com.example.booking.dto.PageResponse;
import com.example.booking.dto.ResourceDto;
import com.example.booking.model.Resource;
import com.example.booking.repository.ResourceRepository;
import com.example.booking.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ResourceServiceImpl implements ResourceService {

    @Autowired
    private ResourceRepository resourceRepository;

    @Override
    public ResourceDto createResource(ResourceDto resourceDto) {
        Resource resource = new Resource();
        resource.setName(resourceDto.getName());
        resource.setDescription(resourceDto.getDescription());
        resource.setPrice(resourceDto.getPrice());
        resource.setIsAvailable(resourceDto.getIsAvailable() != null ? resourceDto.getIsAvailable() : true);
        
        Resource savedResource = resourceRepository.save(resource);
        return convertToDto(savedResource);
    }

    @Override
    public ResourceDto getResourceById(Long id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resource not found with id: " + id));
        return convertToDto(resource);
    }

    @Override
    public PageResponse<ResourceDto> getAllAvailableResources(int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Resource> resources = resourceRepository.findAll(pageable);
        List<ResourceDto> resourceDtos = resources.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        return new PageResponse<>(
                resourceDtos,
                resources.getNumber(),
                resources.getSize(),
                resources.getTotalElements(),
                resources.getTotalPages(),
                resources.isFirst(),
                resources.isLast(),
                resources.isEmpty()
        );
    }

    @Override
    public PageResponse<ResourceDto> searchResources(int page, int size, String sortBy, String sortDir, 
                                                  String name, BigDecimal minPrice, BigDecimal maxPrice) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Use a simpler approach for now - get all resources and filter in memory
        Page<Resource> resources = resourceRepository.findAll(pageable);
        
        // Filter the results
        List<Resource> filteredResources = resources.getContent().stream()
                .filter(resource -> 
                    (name == null || name.isEmpty() || 
                     resource.getName().toLowerCase().contains(name.toLowerCase())) &&
                    (minPrice == null || resource.getPrice().compareTo(minPrice) >= 0) &&
                    (maxPrice == null || resource.getPrice().compareTo(maxPrice) <= 0)
                )
                .collect(Collectors.toList());
        
        List<ResourceDto> resourceDtos = filteredResources.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        // Create a new PageResponse with filtered results
        return new PageResponse<>(
                resourceDtos,
                resources.getNumber(),
                resources.getSize(),
                resourceDtos.size(), // Use filtered count
                (int) Math.ceil((double) resourceDtos.size() / size), // Calculate total pages
                resources.isFirst(),
                resources.isLast(),
                resourceDtos.isEmpty()
        );
    }

    @Override
    public ResourceDto updateResource(Long id, ResourceDto resourceDto) {
        Resource existingResource = resourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resource not found with id: " + id));
        
        existingResource.setName(resourceDto.getName());
        existingResource.setDescription(resourceDto.getDescription());
        existingResource.setPrice(resourceDto.getPrice());
        existingResource.setIsAvailable(resourceDto.getIsAvailable() != null ? resourceDto.getIsAvailable() : true);
        
        Resource updatedResource = resourceRepository.save(existingResource);
        return convertToDto(updatedResource);
    }

    @Override
    public void deleteResource(Long id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resource not found with id: " + id));
        resourceRepository.delete(resource);
    }

    private ResourceDto convertToDto(Resource resource) {
        ResourceDto dto = new ResourceDto();
        dto.setId(resource.getId());
        dto.setName(resource.getName());
        dto.setDescription(resource.getDescription());
        dto.setPrice(resource.getPrice());
        dto.setIsAvailable(resource.getIsAvailable());
        return dto;
    }
}