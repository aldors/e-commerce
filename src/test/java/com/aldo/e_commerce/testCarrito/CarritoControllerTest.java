package com.aldo.e_commerce.testCarrito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.aldo.e_commerce.controller.CarritoController;
import com.aldo.e_commerce.dto.carrito.AddProductoRequest;
import com.aldo.e_commerce.dto.carrito.CarritoResponse;
import com.aldo.e_commerce.security.CustomUserDetailsService;
import com.aldo.e_commerce.security.JwtService;
import com.aldo.e_commerce.service.interfaces.CarritoService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(CarritoController.class)
public class CarritoControllerTest {
    
    @MockitoBean
    private CarritoService carritoService;

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser
    void deberiaObtenerCarrito() throws Exception {

        CarritoResponse carritoResponse = new CarritoResponse(new ArrayList<>(), 100.00);

        when(carritoService.obtenerCarrito()).thenReturn(carritoResponse);

        mockMvc.perform(get("/carrito")
                        )
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.total").value(100.00));
    }

    @Test
    @WithMockUser
    void deberiaAgregarProductoAlCarrito() throws Exception {

        AddProductoRequest addProductoRequest = new AddProductoRequest();

        addProductoRequest.setProductoId(1L);
        addProductoRequest.setCantidad(2);

        CarritoResponse carritoResponse = new CarritoResponse(new ArrayList<>(), 200.00);

        when(carritoService.agregarProducto(any(AddProductoRequest.class))).thenReturn(carritoResponse);

        mockMvc.perform(
                    post("/carrito")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(addProductoRequest))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(200.00));
    }

    @Test
    @WithMockUser
    void deberiaEliminarProducto() throws Exception {

        Long productoId = 1L;

        CarritoResponse carritoResponse = new CarritoResponse(new ArrayList<>(), 0.00);
        
        when(carritoService.eliminarProducto(productoId)).thenReturn(carritoResponse);

        mockMvc.perform(
                    delete("/carrito/1")
                    .with(csrf())
                    )
                    .andExpect(status().isNoContent());

    }

}
