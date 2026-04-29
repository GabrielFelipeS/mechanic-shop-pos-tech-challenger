package org.project.mechanic_shop.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {

	@Getter
	private final HttpStatus status;

	private final transient Object object;

	public ApiException(HttpStatus status, Object data) {
		this.status = status;
		this.object = data;
	}

	public Object getData() {
		return object == null ? java.util.List.of() : object;
	}
}
