package org.project.mechanic_shop.domain.dto.error_field_dto;

public record ErrorField(String field, String message) {
	public String getField() {
		return field;
	}

	public String getMessage() {
		return message;
	}
}
