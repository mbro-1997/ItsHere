package com.itshere.backend.service;

import com.itshere.backend.dto.CoordinateDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GeometryService {

    private static final double METERS_PER_DEGREE_LAT = 111_320D;

    public int distancePointToPolylineMeters(double lat, double lng, List<CoordinateDto> polyline) {
        if (polyline == null || polyline.isEmpty()) {
            return Integer.MAX_VALUE;
        }
        if (polyline.size() == 1) {
            CoordinateDto only = polyline.get(0);
            return (int) Math.round(distanceMeters(lat, lng, only.getLatitude(), only.getLongitude()));
        }

        double best = Double.MAX_VALUE;
        for (int i = 0; i < polyline.size() - 1; i++) {
            CoordinateDto start = polyline.get(i);
            CoordinateDto end = polyline.get(i + 1);
            double distance = distancePointToSegmentMeters(
                    lat, lng,
                    start.getLatitude(), start.getLongitude(),
                    end.getLatitude(), end.getLongitude()
            );
            best = Math.min(best, distance);
        }
        return (int) Math.round(best);
    }

    public Bounds expandedBounds(List<CoordinateDto> polyline, int radiusMeters) {
        double minLat = Double.MAX_VALUE;
        double maxLat = -Double.MAX_VALUE;
        double minLng = Double.MAX_VALUE;
        double maxLng = -Double.MAX_VALUE;
        for (CoordinateDto point : polyline) {
            minLat = Math.min(minLat, point.getLatitude());
            maxLat = Math.max(maxLat, point.getLatitude());
            minLng = Math.min(minLng, point.getLongitude());
            maxLng = Math.max(maxLng, point.getLongitude());
        }

        double centerLat = (minLat + maxLat) / 2D;
        double latDelta = radiusMeters / METERS_PER_DEGREE_LAT;
        double lngDelta = radiusMeters / metersPerDegreeLng(centerLat);
        return new Bounds(minLat - latDelta, maxLat + latDelta, minLng - lngDelta, maxLng + lngDelta);
    }

    private double distancePointToSegmentMeters(
            double pointLat, double pointLng,
            double startLat, double startLng,
            double endLat, double endLng
    ) {
        double originLat = (pointLat + startLat + endLat) / 3D;
        double px = toMeterX(pointLng, originLat);
        double py = toMeterY(pointLat);
        double ax = toMeterX(startLng, originLat);
        double ay = toMeterY(startLat);
        double bx = toMeterX(endLng, originLat);
        double by = toMeterY(endLat);

        double vx = bx - ax;
        double vy = by - ay;
        double wx = px - ax;
        double wy = py - ay;
        double lengthSquared = vx * vx + vy * vy;
        if (lengthSquared == 0D) {
            return Math.hypot(px - ax, py - ay);
        }
        double t = Math.max(0D, Math.min(1D, (wx * vx + wy * vy) / lengthSquared));
        double projectionX = ax + t * vx;
        double projectionY = ay + t * vy;
        return Math.hypot(px - projectionX, py - projectionY);
    }

    private double distanceMeters(double lat1, double lng1, double lat2, double lng2) {
        double originLat = (lat1 + lat2) / 2D;
        return Math.hypot(toMeterX(lng1 - lng2, originLat), toMeterY(lat1 - lat2));
    }

    private double toMeterX(double lng, double lat) {
        return lng * metersPerDegreeLng(lat);
    }

    private double toMeterY(double lat) {
        return lat * METERS_PER_DEGREE_LAT;
    }

    private double metersPerDegreeLng(double lat) {
        double factor = Math.cos(Math.toRadians(lat));
        if (Math.abs(factor) < 0.01D) {
            factor = 0.01D;
        }
        return METERS_PER_DEGREE_LAT * factor;
    }

    public record Bounds(double minLat, double maxLat, double minLng, double maxLng) {
    }
}
