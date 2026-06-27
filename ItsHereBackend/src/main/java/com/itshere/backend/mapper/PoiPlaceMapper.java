package com.itshere.backend.mapper;

import com.itshere.backend.entity.PoiPlaceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PoiPlaceMapper {

    @Select("select * from poi_places where id = #{id} and is_active = 1 limit 1")
    PoiPlaceEntity findById(@Param("id") Long id);

    @Select("""
            <script>
            select *
            from poi_places
            where is_active = 1
              and latitude between #{minLat} and #{maxLat}
              and longitude between #{minLng} and #{maxLng}
              <if test="brandIds != null and brandIds.size() > 0">
                and brand_id in
                <foreach collection="brandIds" item="brandId" open="(" separator="," close=")">
                  #{brandId}
                </foreach>
              </if>
            limit 2000
            </script>
            """)
    List<PoiPlaceEntity> findCandidates(
            @Param("brandIds") List<Long> brandIds,
            @Param("minLat") double minLat,
            @Param("maxLat") double maxLat,
            @Param("minLng") double minLng,
            @Param("maxLng") double maxLng
    );
}
