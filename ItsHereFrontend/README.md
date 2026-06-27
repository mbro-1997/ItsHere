# ItsHereFrontend

Native WeChat mini program MVP for "搁这儿呢".

## Pages

```text
pages/index/index             地图首页，选择起终点并发起顺路查询
pages/route-result/index      顺路结果，原生 map 展示路线和 marker
pages/result-filter/index     本次结果前端筛选，不调后端
pages/place-detail/index      点位详情、复制地址、评价/标签入口
pages/brand-config/index      用户品牌配置，会影响后端顺路查询召回
pages/user-places/index       个人点位列表和新增
pages/reviews/index           我的评价
pages/review-edit/index       写评价
pages/tag-edit/index          打标签，仅自己可见
pages/profile/index           我的
```

## Request Boundary

- Route result calls backend `/api/routes/search`.
- Result filter does not call backend; it filters `routePlaces` in local storage.
- Map rendering uses the WeChat native `map` component.
- Backend is responsible for Tencent Map route planning and MySQL POI filtering.
