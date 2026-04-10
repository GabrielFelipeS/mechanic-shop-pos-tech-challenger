package org.project.postechchallengemechanicshop.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {
    @Getter
    private final HttpStatus status;
    private  final Object data;


    public ApiException(HttpStatus status, Object data) {
        this.status = status;
        this.data = data;
    }

    public Object getData() {
        return data == null ? java.util.List.of() : data;
    }
    
}
