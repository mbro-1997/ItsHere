package com.itshere.backend.mapper;

import com.itshere.backend.entity.PoiCategoryEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PoiCategoryMapper {

    @Select("""
            select *
            from poi_categories
            where is_active = 1
            order by sort_order asc, id asc
            """)
    List<PoiCategoryEntity> findActive();
}
