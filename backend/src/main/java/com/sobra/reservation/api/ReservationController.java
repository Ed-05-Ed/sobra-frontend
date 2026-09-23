package com.sobra.reservation.api;

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

import com.sobra.reservation.service.ReservationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse create(
            @Valid @RequestBody CreateReservationRequest request
    ) {
        return reservationService.create(request);
    }

    @GetMapping
    public List<ReservationResponse> list(
            @RequestParam UUID userId
    ) {
        return reservationService.listByUser(userId);
    }

    @GetMapping("/received")
    public List<ReservationResponse> listReceived(
            @RequestParam UUID ownerId
    ) {
        return reservationService.listReceivedByOwner(ownerId);
    }

    @PatchMapping("/{id}/cancel")
    public ReservationResponse cancel(@PathVariable UUID id) {
        return reservationService.cancel(id);
    }

    @PatchMapping("/{id}/complete")
    public ReservationResponse complete(
            @PathVariable UUID id,
            @Valid @RequestBody CompleteReservationRequest request
    ) {
        return reservationService.complete(
                id,
                request.pickupCode()
        );
    }

}