package com.itshere.backend.service;

import com.itshere.backend.common.BusinessException;
import com.itshere.backend.dto.CoordinateDto;
import com.itshere.backend.dto.PlaceSummaryDto;
import com.itshere.backend.dto.RouteSearchRequest;
import com.itshere.backend.dto.RouteSearchResponse;
import com.itshere.backend.entity.PoiBrandEntity;
import com.itshere.backend.entity.PoiPlaceEntity;
import com.itshere.backend.entity.UserPlaceEntity;
import com.itshere.backend.mapper.PoiBrandMapper;
import com.itshere.backend.mapper.PoiPlaceMapper;
import com.itshere.backend.mapper.UserMapper;
import com.itshere.backend.mapper.UserPlaceMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RouteSearchService {

    private final TencentMapClient tencentMapClient;
    private final GeometryService geometryService;
    private final PoiBrandMapper brandMapper;
    private final PoiPlaceMapper placeMapper;
    private final UserPlaceMapper userPlaceMapper;
    private final UserMapper userMapper;
    private final PlaceAssembler placeAssembler;

    public RouteSearchService(TencentMapClient tencentMapClient,
                              GeometryService geometryService,
                              PoiBrandMapper brandMapper,
                              PoiPlaceMapper placeMapper,
                              UserPlaceMapper userPlaceMapper,
                              UserMapper userMapper,
                              PlaceAssembler placeAssembler) {
        this.tencentMapClient = tencentMapClient;
        this.geometryService = geometryService;
        this.brandMapper = brandMapper;
        this.placeMapper = placeMapper;
        this.userPlaceMapper = userPlaceMapper;
        this.userMapper = userMapper;
        this.placeAssembler = placeAssembler;
    }

    public RouteSearchResponse search(RouteSearchRequest request) {
        ensureUserExists(request.getUserId());
        int radius = normalizeRadius(request.getRadiusMeters());

        // 外部接口调用：只在这里调腾讯地图，获取起点到终点的骑行路线 polyline。
        List<CoordinateDto> polyline = tencentMapClient.planBicyclingRoute(request.getStart(), request.getEnd());
        GeometryService.Bounds bounds = geometryService.expandedBounds(polyline, radius);

        List<Long> brandIds = brandMapper.findEnabledForRoute(request.getUserId())
                .stream()
                .map(PoiBrandEntity::getId)
                .toList();

        List<PlaceSummaryDto> results = new ArrayList<>();
        if (!brandIds.isEmpty()) {
            // 查我们自己的 MySQL：先用路线外接矩形扩大 radius 做粗筛，再在 Java 里算点到路线折线的最短距离。
            List<PoiPlaceEntity> candidates = placeMapper.findCandidates(
                    brandIds,
                    bounds.minLat(), bounds.maxLat(),
                    bounds.minLng(), bounds.maxLng()
            );
            for (PoiPlaceEntity candidate : candidates) {
                if (candidate.getLatitude() == null || candidate.getLongitude() == null) {
                    continue;
                }
                int distance = geometryService.distancePointToPolylineMeters(
                        candidate.getLatitude().doubleValue(),
                        candidate.getLongitude().doubleValue(),
                        polyline
                );
                if (distance <= radius) {
                    results.add(placeAssembler.toSystemSummary(request.getUserId(), candidate, distance));
                }
            }
        }

        // 查我们自己的 MySQL：用户个人点位也参与当前用户自己的顺路查询。
        for (UserPlaceEntity userPlace : userPlaceMapper.findEnabledForRoute(request.getUserId())) {
            if (userPlace.getLatitude() == null || userPlace.getLongitude() == null) {
                continue;
            }
            int distance = geometryService.distancePointToPolylineMeters(
                    userPlace.getLatitude().doubleValue(),
                    userPlace.getLongitude().doubleValue(),
                    polyline
            );
            if (distance <= radius) {
                results.add(placeAssembler.toUserSummary(request.getUserId(), userPlace, distance));
            }
        }

        results.sort(Comparator.comparing(PlaceSummaryDto::getDistanceMeters, Comparator.nullsLast(Integer::compareTo)));
        RouteSearchResponse response = new RouteSearchResponse();
        response.setPolyline(polyline);
        response.setPlaces(results);
        return response;
    }

    private void ensureUserExists(Long userId) {
        if (userMapper.findById(userId) == null) {
            throw new BusinessException("用户不存在，请先完成微信登录");
        }
    }

    private int normalizeRadius(Integer radiusMeters) {
        if (radiusMeters == null || radiusMeters <= 0) {
            return 500;
        }
        return Math.min(radiusMeters, 3000);
    }
}
