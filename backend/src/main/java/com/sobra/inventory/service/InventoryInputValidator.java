package com.sobra.inventory.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.sobra.inventory.model.Unit;
import com.sobra.shared.exception.ApiException;
import com.sobra.shared.exception.ErrorDetail;

@Component
public class InventoryInputValidator {

    private static final int MAX_NAME_LENGTH = 120;
    private static final int MAX_INTEGER_DIGITS = 9;
    private static final int MAX_FRACTION_DIGITS = 3;

    public String normalizeName(String name) {
        if (name == null) {
            throw ApiException.badRequest("El nombre es obligatorio.",
                    new ErrorDetail("name", "no puede ser nulo"));
        }
        String normalized = name.trim();
        if (normalized.isEmpty()) {
            throw ApiException.badRequest("El nombre es obligatorio.",
                    new ErrorDetail("name", "no puede estar vacío"));
        }
        if (normalized.length() > MAX_NAME_LENGTH) {
            throw ApiException.badRequest("El nombre es demasiado largo.",
                    new ErrorDetail("name", "debe tener como máximo 120 caracteres"));
        }
        return normalized;
    }

    public void validateQuantity(BigDecimal quantity, Unit unit, String field) {
        if (quantity == null) {
            throw ApiException.badRequest("La cantidad es obligatoria.",
                    new ErrorDetail(field, "no puede ser nula"));
        }
        if (quantity.signum() <= 0) {
            throw ApiException.badRequest("La cantidad debe ser mayor que cero.",
                    new ErrorDetail(field, "debe ser mayor que cero"));
        }

        int integerDigits = Math.max(0, quantity.precision() - quantity.scale());
        if (quantity.scale() > MAX_FRACTION_DIGITS || integerDigits > MAX_INTEGER_DIGITS) {
            throw ApiException.badRequest("La cantidad excede NUMERIC(12,3).",
                    new ErrorDetail(field, "admite hasta 9 enteros y 3 decimales"));
        }
        if (unit == Unit.PIECE && quantity.stripTrailingZeros().scale() > 0) {
            throw ApiException.badRequest("PIECE solo admite cantidades enteras.",
                    new ErrorDetail(field, "debe ser matemáticamente entera"));
        }
    }
}
