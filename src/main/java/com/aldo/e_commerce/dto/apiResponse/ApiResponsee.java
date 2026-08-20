package com.aldo.e_commerce.dto.apiResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponsee<T> {
    
    private boolean success;
    private String message;
    private T data;
}
