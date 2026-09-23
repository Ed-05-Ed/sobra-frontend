package com.sobra.marketplace.api;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.sobra.marketplace.service.ListingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/listings")
public class ListingController {

    private final ListingService listingService;

    public ListingController(ListingService listingService) {
        this.listingService = listingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ListingResponse create(
            @Valid @RequestBody CreateListingRequest request
    ) {
        return listingService.create(request);
    }

    @PatchMapping("/{id}/close")
    public ListingResponse close(@PathVariable UUID id) {
        return listingService.close(id);
    }

    @GetMapping
    public List<ListingResponse> list(
            @RequestParam(required = false) UUID userId
    ) {
        if (userId != null) {
            return listingService.listByOwner(userId);
        }

        return listingService.listActive();
    }
}