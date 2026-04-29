package org.project.mechanic_shop.common.responses;

public record ApiResponse(int status, String message, Object data) {}
