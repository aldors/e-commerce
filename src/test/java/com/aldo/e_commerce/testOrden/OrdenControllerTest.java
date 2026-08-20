package com.aldo.e_commerce.testOrden;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.aldo.e_commerce.controller.OrdenController;
import com.aldo.e_commerce.dto.orden.OrdenResponse;
import com.aldo.e_commerce.security.CustomUserDetailsService;
import com.aldo.e_commerce.security.JwtService;
import com.aldo.e_commerce.service.interfaces.OrdenService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(OrdenController.class)
public class OrdenControllerTest {

    @MockitoBean
    private OrdenService ordenService;

    @Autowired
    private MockMvc mockMvc;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;


    @Test
    @WithMockUser
    void deberiaHacerCheckout() throws Exception {

        OrdenResponse ordenResponse = new OrdenResponse(
            1L,
            200.00,
            "PENDIENTE",
            LocalDateTime.now(),
            new ArrayList<>());

        when(ordenService.checkout()).thenReturn(ordenResponse);

        mockMvc.perform(
                    post("/ordenes/checkout")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.estado").value("PENDIENTE"));
    }


    @Test
    @WithMockUser
    void deberiaMostrarHistorial() throws Exception {
        List<OrdenResponse> historialOrdenes = List.of(
            new OrdenResponse(
                1L,
                200.00,
                "PENDIENTE",
                LocalDateTime.now(),
                new ArrayList<>()
            )
        );

        when(ordenService.historial()).thenReturn(historialOrdenes);

        mockMvc.perform(
                    get("/ordenes")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].total").value(200.00));
                    
    }
}
