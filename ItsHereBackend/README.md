# ItsHereBackend

Spring Boot backend for the ItsHere WeChat mini program.

## Tech Baseline

- Java 17
- Spring Boot 3.5.8
- MyBatis Spring Boot Starter 3.0.4
- MySQL 8

## MVP Modules

- `auth`: WeChat `openid` login
- `users`: user profile and identity
- `poi`: POI list and detail
- `route`: route-based POI search
- `brands`: account brand configuration
- `tags`: private user tags
- `reviews`: private/public reviews
- `user-places`: user custom places

## Database Assumption

Database table creation and POI import can be handled by a separate agent. At the moment the import has not actually been executed; this backend expects the schema documented in:

```text
../../文档/顺路找点数据库表设计说明.md
```

Important conventions:

- `account_brand_configs.account_id = users.id`
- `poi_places.raw_payload = item.raw`
- `poi_places.extra_payload = item top-level fields not mapped to columns`
- `origin_query` is JSON
- POI coordinates use GCJ-02

## Local Configuration

- MySQL: `localhost:3306/itshere`
- MySQL user: `root`
- Backend port: `8686`
- WeChat AppID: `wx9e992fd9783fabfe`
- Map provider: Tencent Map

## API Boundary

External API calls:

- `POST /api/auth/login`: backend calls WeChat `jscode2session` to exchange mini-program `code` for `openid`.
- `POST /api/routes/search`: backend calls Tencent Map ebicycling route planning once to get route `polyline`.
- `GET /api/map/reverse-geocode`: backend calls Tencent Map geocoder to show readable current location.
- `GET /api/map/suggestion`: backend calls Tencent Map keyword suggestion for start/end dropdowns.

Internal database calls:

- POI candidates are queried from `poi_places`.
- Enabled brands are queried from `account_brand_configs` + `poi_brands`.
- User places are queried from `user_places`.
- Reviews are queried from `poi_reviews`.
- Tags are queried from `poi_tags` + `poi_tag_relations`.

Route search flow:

```text
mini program start/end coordinates
-> backend calls Tencent ebicycling route API once
-> backend gets route polyline
-> backend queries MySQL POI candidates by expanded bounding box
-> backend calculates point-to-polyline distance locally
-> backend returns route places with distance, review flag and private tags
-> mini program does category / brand / custom tag display filtering locally
```

## MVP Endpoints

```text
POST /api/auth/login
POST /api/routes/search
GET  /api/places/{poiType}/{poiId}?userId=
GET  /api/categories
GET  /api/map/reverse-geocode?latitude=&longitude=
GET  /api/map/suggestion?keyword=&region=济南
GET  /api/brands?userId=
POST /api/brands/config
GET  /api/reviews/mine?userId=
POST /api/reviews
GET  /api/tags/mine?userId=
POST /api/tags
GET  /api/user-places?userId=
POST /api/user-places
```
