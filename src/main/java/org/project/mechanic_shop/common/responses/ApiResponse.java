package org.project.mechanic_shop.common.responses;

public record ApiResponse( int code, String message, Object data ) {
}
