package org.project.mechanic_shop.domain.dto.responses;

import java.util.List;
import org.project.mechanic_shop.domain.dto.error_field_dto.ErrorField;

public record ErrorResponse(int status, String message, List<ErrorField> errors) {
	private static final int BAD_REQUEST = 400;
	private static final int FORBIDDEN = 403;
	private static final int NOT_FOUND = 404;
	private static final int CONFLICT = 409;

	public static ErrorResponse defaultResponse(String message) {
		return new ErrorResponse(BAD_REQUEST, message, List.of());
	}

	public static ErrorResponse conflict(String message) {
		return new ErrorResponse(CONFLICT, message, List.of());
	}

	public static ErrorResponse forbidden(String message) {
		return new ErrorResponse(FORBIDDEN, message, List.of());
	}

	public static ErrorResponse notFound(String message) {
		return new ErrorResponse(NOT_FOUND, message, List.of());
	}
}
