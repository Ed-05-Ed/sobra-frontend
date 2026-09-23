package com.sobra.inventory.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Component;

import com.sobra.inventory.model.DateStatus;

@Component
public class InventoryDatePolicy {

    private final Clock clock;

    public InventoryDatePolicy(Clock clock) {
        this.clock = clock;
    }

    public DateInformation evaluate(LocalDate labelDate) {
        long daysUntilLabelDate = ChronoUnit.DAYS.between(LocalDate.now(clock), labelDate);
        DateStatus status;
        if (daysUntilLabelDate < 0) {
            status = DateStatus.DATE_PASSED;
        } else if (daysUntilLabelDate <= 3) {
            status = DateStatus.PRIORITY;
        } else {
            status = DateStatus.UPCOMING;
        }
        return new DateInformation(status, daysUntilLabelDate);
    }

    public record DateInformation(DateStatus status, long daysUntilLabelDate) {
    }
}
