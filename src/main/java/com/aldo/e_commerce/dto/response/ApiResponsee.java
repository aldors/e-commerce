package com.aldo.e_commerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponsee<T> {
    
    private boolean success;
    private String message;
    private T data;
}
