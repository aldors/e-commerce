package com.aldo.e_commerce.testCategoria;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aldo.e_commerce.dto.categoria.CategoriaRequest;
import com.aldo.e_commerce.dto.categoria.CategoriaResponse;
import com.aldo.e_commerce.entity.Categoria;
import com.aldo.e_commerce.exception.CategoriaNoEncontradaException;
import com.aldo.e_commerce.exception.CategoriaYaExistenteException;
import com.aldo.e_commerce.repository.CategoriaRepository;
import com.aldo.e_commerce.service.CategoriaServiceImpl;

@ExtendWith(MockitoExtension.class)
public class CategoriaServiceImplTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaServiceImpl categoriaServiceImpl;

    private Categoria categoria;
    private CategoriaRequest categoriaRequest;
    private CategoriaResponse categoriaResponse;

    @BeforeEach
    void setUp(){

        categoria = Categoria.builder()
            .id(1L)
            .nombre("Tecnologia")
            .build();

        categoriaRequest = new CategoriaRequest();
        categoriaRequest.setNombre("Tecnologia");

        categoriaResponse = new CategoriaResponse(1L, "Tecnologia");
    }
    

    //Happy path
    @Test
    void deberiaCrearCategoriaCorrectamente(){

        when(categoriaRepository.findByNombre("Tecnologia")).thenReturn(Optional.empty());
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoria);

        categoriaResponse = categoriaServiceImpl.crear(categoriaRequest);

        assertNotNull(categoriaResponse);
        assertEquals("Tecnologia", categoriaResponse.getNombre());
        assertEquals(1L, categoriaResponse.getId());

        verify(categoriaRepository).save(any(Categoria.class));

    }

    @Test
    void deberiaListarLasCategorias(){

        List<Categoria> categorias = List.of(categoria);

        when(categoriaRepository.findAll()).thenReturn(categorias);

        List<CategoriaResponse> response = categoriaServiceImpl.listar();

        assertNotNull(response);
        assertEquals("Tecnologia", response.get(0).getNombre());
        assertEquals(1L, response.get(0).getId());
    }

    @Test
    void deberiaActualizarCategoria(){

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));

        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoria);

        categoriaRequest.setNombre("Nueva categoria");
        categoriaResponse = categoriaServiceImpl.actualizar(categoriaRequest, 1L);

        assertNotNull(categoriaResponse);
        assertEquals(1L, categoriaResponse.getId());
        assertEquals("Nueva categoria", categoriaResponse.getNombre());
        
        verify(categoriaRepository).save(any(Categoria.class));
    }

    @Test
    void deberiaEliminarCategoria(){

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));

        categoriaServiceImpl.eliminar(1L);

        verify(categoriaRepository).delete(any(Categoria.class));
    }


    //Error path
    @Test
    void deberiaLanzarExcepcionSiYaExisteUnaCategoria(){
        
        when(categoriaRepository.findByNombre("Tecnologia")).thenReturn(Optional.of(categoria));

        assertThrows(CategoriaYaExistenteException.class, () -> categoriaServiceImpl.crear(categoriaRequest));

        verify(categoriaRepository, never()).save(any());
    }

    @Test
    void deberiaLanzarExcepcionSiNoExisteLaCategoriaAlActualizar(){

         when(categoriaRepository.findById(1L)).thenReturn(Optional.empty());

         assertThrows(CategoriaNoEncontradaException.class, () -> categoriaServiceImpl.actualizar(categoriaRequest, 1l));

         verify(categoriaRepository, never()).save(any());

    }

    @Test
    void deberiaLanzarExcepcionSiNoExisteLaCategoriaAlEliminar(){

         when(categoriaRepository.findById(1L)).thenReturn(Optional.empty());

         assertThrows(CategoriaNoEncontradaException.class, () -> categoriaServiceImpl.eliminar(1l));

    }

}