package com.itshere.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itshere.backend.common.BusinessException;
import com.itshere.backend.config.ItsHereProperties;
import com.itshere.backend.dto.CoordinateDto;
import com.itshere.backend.dto.LocationSuggestionDto;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Service
public class TencentMapClient {

    private final ItsHereProperties properties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public TencentMapClient(ItsHereProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.create();
    }

    public List<CoordinateDto> planBicyclingRoute(CoordinateDto start, CoordinateDto end) {
        URI uri = UriComponentsBuilder.fromHttpUrl(properties.getMap().getRouteUrl())
                .queryParam("from", start.getLatitude() + "," + start.getLongitude())
                .queryParam("to", end.getLatitude() + "," + end.getLongitude())
                .queryParam("key", properties.getMap().getKey())
                .build(true)
                .toUri();

        // 外部接口调用：这里调用腾讯地图骑行（电动车）路线规划，只拿路线 polyline。
        // 后续 POI 召回、点到路线距离计算、评价和标签都查我们自己的 MySQL，不再逐个调用地图接口。
        String body = restClient.get().uri(uri).retrieve().body(String.class);
        try {
            JsonNode root = objectMapper.readTree(body);
            int status = root.path("status").asInt(-1);
            if (status != 0) {
                throw new BusinessException("腾讯地图路线规划失败：" + root.path("message").asText("unknown"));
            }
            JsonNode polylineNode = root.path("result").path("routes").path(0).path("polyline");
            List<CoordinateDto> points = parseTencentPolyline(polylineNode);
            if (points.size() < 2) {
                return List.of(start, end);
            }
            return points;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("解析腾讯地图路线失败：" + ex.getMessage());
        }
    }

    public String reverseGeocode(double latitude, double longitude) {
        URI uri = UriComponentsBuilder.fromHttpUrl(properties.getMap().getGeocoderUrl())
                .queryParam("location", latitude + "," + longitude)
                .queryParam("key", properties.getMap().getKey())
                .build(true)
                .toUri();

        // 外部接口调用：这里调用腾讯地图逆地址解析，把当前位置坐标转换成可读地点/地址。
        String body = restClient.get().uri(uri).retrieve().body(String.class);
        try {
            JsonNode root = objectMapper.readTree(body);
            int status = root.path("status").asInt(-1);
            if (status != 0) {
                throw new BusinessException("腾讯地图逆地址解析失败：" + root.path("message").asText("unknown"));
            }
            JsonNode result = root.path("result");
            String recommend = result.path("formatted_addresses").path("recommend").asText("");
            if (!recommend.isBlank()) {
                return recommend;
            }
            return result.path("address").asText("当前位置");
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("解析腾讯地图逆地址结果失败：" + ex.getMessage());
        }
    }

    public List<LocationSuggestionDto> suggestLocations(String keyword, String region) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        if (normalizedKeyword.length() < 2) {
            return List.of();
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(properties.getMap().getSuggestionUrl())
                .queryParam("keyword", normalizedKeyword)
                .queryParam("key", properties.getMap().getKey())
                .queryParam("region_fix", 1)
                .queryParam("page_size", 8);
        if (region != null && !region.isBlank()) {
            builder.queryParam("region", region.trim());
        }
        URI uri = builder.build().encode().toUri();

        // 外部接口调用：这里调用腾讯地图关键词输入提示，用于起点/终点下拉联想。
        // 前端有防抖，且空词/单字不调用，避免浪费配额。
        String body = restClient.get().uri(uri).retrieve().body(String.class);
        try {
            JsonNode root = objectMapper.readTree(body);
            int status = root.path("status").asInt(-1);
            if (status != 0) {
                throw new BusinessException("腾讯地图关键词输入提示失败：" + root.path("message").asText("unknown"));
            }
            List<LocationSuggestionDto> suggestions = new ArrayList<>();
            JsonNode data = root.path("data");
            if (data.isArray()) {
                for (JsonNode item : data) {
                    JsonNode location = item.path("location");
                    if (location.path("lat").isMissingNode() || location.path("lng").isMissingNode()) {
                        continue;
                    }
                    LocationSuggestionDto dto = new LocationSuggestionDto();
                    dto.setId(item.path("id").asText(""));
                    dto.setTitle(item.path("title").asText(""));
                    dto.setAddress(item.path("address").asText(""));
                    dto.setLatitude(location.path("lat").asDouble());
                    dto.setLongitude(location.path("lng").asDouble());
                    suggestions.add(dto);
                }
            }
            return suggestions;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("解析腾讯地图关键词输入提示失败：" + ex.getMessage());
        }
    }

    private List<CoordinateDto> parseTencentPolyline(JsonNode polylineNode) {
        List<Double> values = new ArrayList<>();
        if (polylineNode != null && polylineNode.isArray()) {
            for (JsonNode node : polylineNode) {
                values.add(node.asDouble());
            }
        }
        if (values.size() < 4) {
            return List.of();
        }

        List<CoordinateDto> uncompressed = toCoordinatePairs(values);
        List<Double> decompressedValues = new ArrayList<>(values);
        if (Math.abs(decompressedValues.get(0)) > 1000D) {
            decompressedValues.set(0, decompressedValues.get(0) / 1_000_000D);
            decompressedValues.set(1, decompressedValues.get(1) / 1_000_000D);
        }
        for (int i = 2; i < decompressedValues.size(); i++) {
            decompressedValues.set(i, decompressedValues.get(i - 2) + decompressedValues.get(i) / 1_000_000D);
        }
        List<CoordinateDto> compressed = toCoordinatePairs(decompressedValues);
        return routeScore(compressed) <= routeScore(uncompressed) ? compressed : uncompressed;
    }

    private List<CoordinateDto> toCoordinatePairs(List<Double> values) {
        List<CoordinateDto> result = new ArrayList<>();
        for (int i = 0; i + 1 < values.size(); i += 2) {
            CoordinateDto point = new CoordinateDto();
            point.setLatitude(values.get(i));
            point.setLongitude(values.get(i + 1));
            result.add(point);
        }
        return result;
    }

    private double routeScore(List<CoordinateDto> points) {
        if (points.size() < 2) {
            return Double.MAX_VALUE;
        }
        double score = 0D;
        for (CoordinateDto point : points) {
            if (point.getLatitude() == null || point.getLongitude() == null
                    || Math.abs(point.getLatitude()) > 90D || Math.abs(point.getLongitude()) > 180D) {
                score += 1_000_000_000D;
            }
        }
        for (int i = 0; i < points.size() - 1; i++) {
            CoordinateDto a = points.get(i);
            CoordinateDto b = points.get(i + 1);
            score += Math.abs(a.getLatitude() - b.getLatitude()) + Math.abs(a.getLongitude() - b.getLongitude());
        }
        return score;
    }
}
