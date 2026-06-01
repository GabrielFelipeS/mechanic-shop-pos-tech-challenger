package org.project.mechanic_shop.shared.exception;

public class OperationNotPermitted extends RuntimeException {

	public OperationNotPermitted(String message) {
		super(message);
	}
}
