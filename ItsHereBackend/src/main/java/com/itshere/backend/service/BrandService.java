package com.itshere.backend.service;

import com.itshere.backend.entity.PoiBrandEntity;
import com.itshere.backend.mapper.PoiBrandMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BrandService {

    private final PoiBrandMapper brandMapper;

    public BrandService(PoiBrandMapper brandMapper) {
        this.brandMapper = brandMapper;
    }

    public List<PoiBrandEntity> listForUser(Long userId) {
        return brandMapper.findForUser(userId);
    }

    public void saveConfig(Long userId, Long brandId, Boolean enabled) {
        brandMapper.upsertUserConfig(userId, brandId, enabled);
    }
}
