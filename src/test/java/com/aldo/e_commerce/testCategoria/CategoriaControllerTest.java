package com.aldo.e_commerce.testCategoria;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.aldo.e_commerce.controller.CategoriaController;
import com.aldo.e_commerce.dto.categoria.CategoriaRequest;
import com.aldo.e_commerce.dto.categoria.CategoriaResponse;
import com.aldo.e_commerce.security.CustomUserDetailsService;
import com.aldo.e_commerce.security.JwtService;
import com.aldo.e_commerce.service.interfaces.CategoriaService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(CategoriaController.class)
public class CategoriaControllerTest {
    
    @MockitoBean
    private CategoriaService categoriaService;

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void deberiaCrearCategoriaCorrectamente() throws Exception{
        
        CategoriaRequest categoriaRequest = new CategoriaRequest();
        categoriaRequest.setNombre("Tecnologia");

        CategoriaResponse categoriaResponse = new CategoriaResponse(1L, "Tecnologia");

        when(categoriaService.crear(any(CategoriaRequest.class))).thenReturn(categoriaResponse);

        mockMvc.perform(
                    post("/categorias/crear")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(categoriaRequest))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nombre").value("Tecnologia"));
    }

    @Test
    @WithMockUser
    void deberiaListarLasCategorias() throws Exception{

        CategoriaResponse categoriaResponse = new CategoriaResponse(1L, "Tecnologia");

        when(categoriaService.listar()).thenReturn(List.of(categoriaResponse));

        mockMvc.perform(
                    get("/categorias/listar")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].nombre").value("Tecnologia"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deberiaActualizarCategoria() throws Exception{

        CategoriaRequest categoriaRequest = new CategoriaRequest();
        categoriaRequest.setNombre("Nueva categoria");

        CategoriaResponse categoriaResponse = new CategoriaResponse(1L, "Nueva categoria");

        when(categoriaService.actualizar(any(CategoriaRequest.class), any(Long.class))).thenReturn(categoriaResponse);

        mockMvc.perform(
                    put("/categorias/actualizar/1")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(categoriaRequest))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nombre").value("Nueva categoria"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deberiaEliminarCategoria() throws Exception{

        doNothing().when(categoriaService).eliminar(1L);

        mockMvc.perform(
                delete("/categorias/eliminar/1")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

}
