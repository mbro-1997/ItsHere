package com.itshere.backend.service;

import com.itshere.backend.common.BusinessException;
import com.itshere.backend.dto.TagSaveRequest;
import com.itshere.backend.entity.PoiTagEntity;
import com.itshere.backend.mapper.PoiTagMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class TagService {

    private static final int PUBLIC_PROMOTION_THRESHOLD = 5;

    private final PoiTagMapper tagMapper;

    public TagService(PoiTagMapper tagMapper) {
        this.tagMapper = tagMapper;
    }

    public List<PoiTagEntity> listMine(Long userId) {
        return tagMapper.findMineTags(userId);
    }

    public List<PoiTagEntity> listMineForPoi(Long userId, String poiType, Long poiId) {
        return tagMapper.findMineForPoi(userId, poiType, poiId);
    }

    @Transactional
    public List<PoiTagEntity> saveForPoi(TagSaveRequest request) {
        String normalized = normalize(request.getTagName());
        if (normalized.isBlank()) {
            throw new BusinessException("标签不能为空");
        }
        PoiTagEntity tag = tagMapper.findMineByName(request.getUserId(), normalized);
        if (tag == null) {
            tag = new PoiTagEntity();
            tag.setName(request.getTagName().trim());
            tag.setNormalizedName(normalized);
            tag.setSourceType("user");
            tag.setCreatedByUserId(request.getUserId());
            tag.setIsPublic(false);
            tagMapper.insertTag(tag);
        }
        tagMapper.insertRelation(request.getUserId(), request.getPoiType(), request.getPoiId(), tag.getId());
        promoteToPublicIfNeeded(request.getPoiType(), request.getPoiId(), normalized);
        return tagMapper.findMineForPoi(request.getUserId(), request.getPoiType(), request.getPoiId());
    }

    @Transactional
    public List<PoiTagEntity> deleteForPoi(Long userId, String poiType, Long poiId, String tagName) {
        String normalized = normalize(tagName);
        if (normalized.isBlank()) {
            throw new BusinessException("标签不能为空");
        }
        tagMapper.deleteRelationByName(userId, poiType, poiId, normalized);
        return tagMapper.findMineForPoi(userId, poiType, poiId);
    }

    private void promoteToPublicIfNeeded(String poiType, Long poiId, String normalizedName) {
        int userCount = tagMapper.countDistinctUsersForPoiTag(poiType, poiId, normalizedName);
        if (userCount >= PUBLIC_PROMOTION_THRESHOLD) {
            tagMapper.promotePoiTagRelationsToPublic(poiType, poiId, normalizedName);
        }
    }

    private String normalize(String name) {
        if (name == null) {
            return "";
        }
        return name.trim().replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
    }
}
