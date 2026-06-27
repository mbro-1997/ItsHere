package com.itshere.backend.mapper;

import com.itshere.backend.entity.PoiBrandEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Insert;

import java.util.List;

@Mapper
public interface PoiBrandMapper {

    @Select("""
            select b.*, c.name as category_name,
                   coalesce(cfg.is_enabled, b.default_enabled) as account_enabled
            from poi_brands b
            left join poi_categories c on c.id = b.category_id
            left join account_brand_configs cfg
                   on cfg.brand_id = b.id and cfg.account_id = #{userId}
            where b.is_active = 1
            order by c.sort_order asc, b.sort_order asc, b.id asc
            """)
    List<PoiBrandEntity> findForUser(@Param("userId") Long userId);

    @Select("""
            select b.*
            from poi_brands b
            left join account_brand_configs cfg
                   on cfg.brand_id = b.id and cfg.account_id = #{userId}
            where b.is_active = 1
              and coalesce(cfg.is_enabled, b.default_enabled) = 1
            order by b.sort_order asc, b.id asc
            """)
    List<PoiBrandEntity> findEnabledForRoute(@Param("userId") Long userId);

    @Insert("""
            insert into account_brand_configs(account_id, brand_id, is_enabled, created_at, updated_at)
            values(#{userId}, #{brandId}, #{enabled}, now(), now())
            on duplicate key update is_enabled = values(is_enabled), updated_at = now()
            """)
    int upsertUserConfig(@Param("userId") Long userId, @Param("brandId") Long brandId, @Param("enabled") Boolean enabled);

    @Update("""
            update account_brand_configs
            set is_enabled = #{enabled}, updated_at = now()
            where account_id = #{userId} and brand_id = #{brandId}
            """)
    int updateUserConfig(@Param("userId") Long userId, @Param("brandId") Long brandId, @Param("enabled") Boolean enabled);
}
