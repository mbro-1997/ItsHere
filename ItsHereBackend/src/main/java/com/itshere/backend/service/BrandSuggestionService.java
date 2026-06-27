package com.itshere.backend.service;

import com.itshere.backend.common.BusinessException;
import com.itshere.backend.dto.BrandSuggestionRequest;
import com.itshere.backend.dto.BrandSuggestionResponse;
import com.itshere.backend.mapper.BrandSuggestionMapper;
import com.itshere.backend.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class BrandSuggestionService {

    private static final int REACHED_THRESHOLD = 10;

    private final BrandSuggestionMapper suggestionMapper;
    private final UserMapper userMapper;

    public BrandSuggestionService(BrandSuggestionMapper suggestionMapper, UserMapper userMapper) {
        this.suggestionMapper = suggestionMapper;
        this.userMapper = userMapper;
    }

    @Transactional
    public BrandSuggestionResponse submit(BrandSuggestionRequest request) {
        if (userMapper.findById(request.getUserId()) == null) {
            throw new BusinessException("用户不存在，请先完成微信登录");
        }
        String rawName = request.getBrandName() == null ? "" : request.getBrandName().trim();
        String normalizedName = normalize(rawName);
        if (normalizedName.isBlank()) {
            throw new BusinessException("请输入品牌名称");
        }
        if (rawName.length() > 64 || normalizedName.length() > 64) {
            throw new BusinessException("品牌名称太长了");
        }

        int inserted = suggestionMapper.insertIgnore(request.getUserId(), rawName, normalizedName);
        int userCount = suggestionMapper.countDistinctUsers(normalizedName);
        if (userCount >= REACHED_THRESHOLD) {
            suggestionMapper.markReached(normalizedName);
        }

        BrandSuggestionResponse response = new BrandSuggestionResponse();
        response.setRawName(rawName);
        response.setNormalizedName(normalizedName);
        response.setSuggestUserCount(userCount);
        response.setStatus(suggestionMapper.findAggregateStatus(normalizedName));
        response.setDuplicate(inserted == 0);
        return response;
    }

    private String normalize(String name) {
        if (name == null) {
            return "";
        }
        return name.trim().replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
    }
}
