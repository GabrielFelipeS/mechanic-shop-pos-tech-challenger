package org.project.mechanic_shop.domain.dto.responses;

public record ApiResponse(int status, String message, Object data) {}
