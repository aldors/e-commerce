package com.aldo.e_commerce.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aldo.e_commerce.dto.categoria.CategoriaRequest;
import com.aldo.e_commerce.dto.categoria.CategoriaResponse;
import com.aldo.e_commerce.entity.Categoria;
import com.aldo.e_commerce.exception.CategoriaNoEncontradaException;
import com.aldo.e_commerce.exception.CategoriaYaExistenteException;
import com.aldo.e_commerce.mapper.CategoriaMapper;
import com.aldo.e_commerce.repository.CategoriaRepository;
import com.aldo.e_commerce.service.interfaces.CategoriaService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Override
    public CategoriaResponse crear(CategoriaRequest request) {

        if(categoriaRepository.findByNombre(request.getNombre()).isPresent()) {
            throw new CategoriaYaExistenteException(request.getNombre());
        }

        Categoria categoria = CategoriaMapper.toEntity(request);

        Categoria categoriaGuardada = categoriaRepository.save(categoria);

        return CategoriaMapper.toResponse(categoriaGuardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return categoriaRepository.findAll()
                .stream()
                .map(CategoriaMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CategoriaResponse actualizar(CategoriaRequest request, Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new CategoriaNoEncontradaException(id));

        categoria.setNombre(request.getNombre());

        Categoria categoriaActualizada = categoriaRepository.save(categoria);

        return CategoriaMapper.toResponse(categoriaActualizada);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new CategoriaNoEncontradaException(id));

        categoriaRepository.delete(categoria);
    }
    
}
