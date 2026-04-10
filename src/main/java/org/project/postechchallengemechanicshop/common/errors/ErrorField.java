package org.project.postechchallengemechanicshop.common.errors;

public record ErrorField( String field, String message) {

    public String getField(){
        return field;
    }

    public String getMessage(){
        return message;
    }

}
