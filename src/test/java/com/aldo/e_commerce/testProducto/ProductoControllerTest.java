package com.aldo.e_commerce.testProducto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.springframework.test.web.servlet.MockMvc;
import com.aldo.e_commerce.controller.ProductoController;
import com.aldo.e_commerce.dto.producto.ProductoRequest;
import com.aldo.e_commerce.dto.producto.ProductoResponse;
import com.aldo.e_commerce.security.CustomUserDetailsService;
import com.aldo.e_commerce.security.JwtService;
import com.aldo.e_commerce.service.interfaces.ProductoService;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(ProductoController.class) // Levanta solo la capa web. (json, validaciones @valid, controllers) nada de bdd real o services.
@AutoConfigureMockMvc(addFilters = true)
public class ProductoControllerTest {
    
    @MockitoBean
    private ProductoService productoService;

    @Autowired
    private MockMvc mockMvc; // Simula requests HTTP. osea las peticiones sin levantar el servidor

//  @Autowired
//  private ObjectMapper objectMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void deberiaCrearProducto() throws Exception { // Se usa throws Exception porque mockMvc.perform puede lanzar excepciones

        ProductoRequest request = new ProductoRequest();

        request.setNombre("Laptop");
        request.setDescripcion("Laptop gamer");
        request.setPrecio(15000.0);
        request.setStock(10);
        request.setCategoriaId(1L);

        ProductoResponse response = new ProductoResponse(
                1L,
                "Laptop",
                "Laptop gamer",
                15000.0,
                10,
                "Tecnologia",
                true
        );

        when(productoService.crear(any(ProductoRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/productos/crear")
                        .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.nombre")
                        .value("Laptop"))
                .andExpect(jsonPath("$.success")
                        .value(true));
    }

    @Test
    @WithMockUser
    void deberiaListarProductos() throws Exception {

        ProductoResponse response = new ProductoResponse(
                1L,
                "Laptop",
                "Laptop gamer",
                15000.0,
                10,
                "Tecnologia",
                true
        );

        when(productoService.listar(
                any(Integer.class),
                any(Integer.class),
                any(String.class),
                any(String.class),
                any(),
                any(),
                any()
        )).thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(
                get("/productos/listar"))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].nombre")
                        .value("Laptop"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deberiaActualizarProducto() throws Exception {

        ProductoRequest request = new ProductoRequest();

        request.setNombre("Laptop PRO");
        request.setDescripcion("Nueva descripcion");
        request.setPrecio(20000.0);
        request.setStock(5);
        request.setCategoriaId(1L);

        ProductoResponse response = new ProductoResponse(
                1L,
                "Laptop PRO",
                "Nueva descripcion",
                20000.0,
                5,
                "Tecnologia",
                true
        );

        when(productoService.actualizar(any(ProductoRequest.class), any(Long.class)))
                .thenReturn(response);

        mockMvc.perform(
                put("/productos/actualizar/1")
                .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nombre")
                        .value("Laptop PRO"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deberiaEliminarProducto() throws Exception {

        doNothing().when(productoService).eliminar(1L);

        mockMvc.perform(
                delete("/productos/eliminar/1")
                .with(csrf()))
                .andExpect(status().isNoContent());

                // Esto lo debo poner solo si en el controller uso ResponseEntity.ok(); al eliminar algo
                // .andExpect(status().isOk())
                // .andExpect(jsonPath("$.success").value(true))
                // .andExpect(jsonPath("$.data").doesNotExist());
    }

    // Test de jwt
    @Test
    void deberiaRetornar401SiNoEstaAutenticado() throws Exception {

        mockMvc.perform(get("/productos/listar"))

                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "aldo@gmail.com", roles = {"USER"})
    void deberiaPermitirAccesoAUsuarioAutenticado() throws Exception {

        mockMvc.perform(get("/productos/listar"))

                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "aldo@gmail.com", roles = {"USER"})
    void deberiaRetornar403SiUsuarioNoEsAdmin() throws Exception {

        mockMvc.perform(delete("/productos/eliminar/1"))

                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = {"ADMIN"})
    void deberiaPermitirEliminarSiEsAdmin() throws Exception {

        mockMvc.perform(delete("/productos/eliminar/1")
                .with(csrf()))

                .andExpect(status().isNoContent());
    }
}
