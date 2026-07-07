package org.project.mechanic_shop.domain.dto.responses;

import java.util.List;
import org.project.mechanic_shop.domain.dto.error_field_dto.ErrorField;
import org.springframework.http.HttpStatus;

public record ErrorResponse(int status, String message, List<ErrorField> errors) {
	public static ErrorResponse defaultResponse(String message) {
		return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message, List.of());
	}

	public static ErrorResponse conflict(String message) {
		return new ErrorResponse(HttpStatus.CONFLICT.value(), message, List.of());
	}

	public static ErrorResponse forbidden(String message) {
		return new ErrorResponse(HttpStatus.FORBIDDEN.value(), message, List.of());
	}

	public static ErrorResponse notFound(String message) {
		return new ErrorResponse(HttpStatus.NOT_FOUND.value(), message, List.of());
	}
}
