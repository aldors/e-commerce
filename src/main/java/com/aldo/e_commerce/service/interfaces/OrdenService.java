package com.aldo.e_commerce.service.interfaces;

import java.util.List;

import com.aldo.e_commerce.dto.orden.OrdenResponse;

public interface OrdenService {
    OrdenResponse checkout();
    List<OrdenResponse> historial();
}
