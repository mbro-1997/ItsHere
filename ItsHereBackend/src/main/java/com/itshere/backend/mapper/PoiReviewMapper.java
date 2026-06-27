package com.itshere.backend.mapper;

import com.itshere.backend.entity.PoiReviewEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PoiReviewMapper {

    @Select("""
            select r.*, u.nickname as user_nickname
            from poi_reviews r
            left join users u on u.id = r.user_id
            where r.user_id = #{userId}
            order by r.updated_at desc, r.id desc
            """)
    List<PoiReviewEntity> findMine(@Param("userId") Long userId);

    @Select("""
            select r.*, u.nickname as user_nickname
            from poi_reviews r
            left join users u on u.id = r.user_id
            where r.poi_type = #{poiType}
              and r.poi_id = #{poiId}
              and (r.visibility = 'public' or r.user_id = #{userId})
            order by r.updated_at desc, r.id desc
            """)
    List<PoiReviewEntity> findVisibleForPoi(@Param("userId") Long userId,
                                            @Param("poiType") String poiType,
                                            @Param("poiId") Long poiId);

    @Select("""
            select count(1)
            from poi_reviews
            where user_id = #{userId} and poi_type = #{poiType} and poi_id = #{poiId}
            """)
    int countMineForPoi(@Param("userId") Long userId,
                        @Param("poiType") String poiType,
                        @Param("poiId") Long poiId);

    @Insert("""
            insert into poi_reviews(user_id, poi_id, poi_type, rating, content, visibility, created_at, updated_at)
            values(#{userId}, #{poiId}, #{poiType}, #{rating}, #{content}, #{visibility}, now(), now())
            on duplicate key update
              id = last_insert_id(id),
              rating = values(rating),
              content = values(content),
              visibility = values(visibility),
              updated_at = now()
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int upsert(PoiReviewEntity review);

    @Select("""
            select r.*, u.nickname as user_nickname
            from poi_reviews r
            left join users u on u.id = r.user_id
            where r.user_id = #{userId}
              and r.poi_type = #{poiType}
              and r.poi_id = #{poiId}
            limit 1
            """)
    PoiReviewEntity findMineForPoi(@Param("userId") Long userId,
                                   @Param("poiType") String poiType,
                                   @Param("poiId") Long poiId);
}
