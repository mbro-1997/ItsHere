package com.itshere.backend.service;

import com.itshere.backend.common.BusinessException;
import com.itshere.backend.dto.UserPlaceSaveRequest;
import com.itshere.backend.entity.UserPlaceEntity;
import com.itshere.backend.mapper.UserPlaceMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserPlaceService {

    private final UserPlaceMapper userPlaceMapper;

    public UserPlaceService(UserPlaceMapper userPlaceMapper) {
        this.userPlaceMapper = userPlaceMapper;
    }

    public List<UserPlaceEntity> listMine(Long userId) {
        return userPlaceMapper.findMine(userId);
    }

    public UserPlaceEntity save(UserPlaceSaveRequest request) {
        if (Boolean.TRUE.equals(request.getEnabledForRoute()) && userPlaceMapper.countEnabled(request.getUserId()) >= 5) {
            throw new BusinessException("个人常用点位最多添加 5 个");
        }
        UserPlaceEntity place = new UserPlaceEntity();
        place.setUserId(request.getUserId());
        place.setName(request.getName());
        place.setAddress(request.getAddress());
        place.setLongitude(request.getLongitude());
        place.setLatitude(request.getLatitude());
        place.setCategoryId(request.getCategoryId());
        place.setBrandName(request.getBrandName());
        place.setNotes(request.getNotes());
        place.setIsEnabledForRoute(request.getEnabledForRoute());
        userPlaceMapper.insert(place);
        return place;
    }

    public void deleteMine(Long userId, Long id) {
        int affected = userPlaceMapper.deleteMine(userId, id);
        if (affected == 0) {
            throw new BusinessException("个人点位不存在或无权限删除");
        }
    }
}
