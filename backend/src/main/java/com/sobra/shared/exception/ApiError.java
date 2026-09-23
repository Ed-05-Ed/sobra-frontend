package com.sobra.shared.exception;

import java.util.List;

public record ApiError(String code, String message, List<ErrorDetail> details) {
}
