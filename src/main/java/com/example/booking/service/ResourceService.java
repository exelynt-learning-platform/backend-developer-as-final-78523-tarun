package com.example.booking.service;

import com.example.booking.dto.ResourceDto;
import com.example.booking.dto.PageResponse;

import java.math.BigDecimal;

public interface ResourceService {
    ResourceDto createResource(ResourceDto resourceDto);
    ResourceDto getResourceById(Long id);
    PageResponse<ResourceDto> getAllAvailableResources(int page, int size, String sortBy, String sortDir);
    PageResponse<ResourceDto> searchResources(int page, int size, String sortBy, String sortDir, 
                                           String name, BigDecimal minPrice, BigDecimal maxPrice);
    ResourceDto updateResource(Long id, ResourceDto resourceDto);
    void deleteResource(Long id);
}