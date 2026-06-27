package com.itshere.backend.mapper;

import com.itshere.backend.entity.UserPlaceEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserPlaceMapper {

    @Select("""
            select * from user_places
            where user_id = #{userId}
            order by updated_at desc, id desc
            """)
    List<UserPlaceEntity> findMine(@Param("userId") Long userId);

    @Select("""
            select * from user_places
            where id = #{id} and user_id = #{userId}
            limit 1
            """)
    UserPlaceEntity findMineById(@Param("userId") Long userId, @Param("id") Long id);

    @Select("""
            select * from user_places
            where user_id = #{userId}
              and is_enabled_for_route = 1
            order by updated_at desc, id desc
            """)
    List<UserPlaceEntity> findEnabledForRoute(@Param("userId") Long userId);

    @Select("""
            select count(1)
            from user_places
            where user_id = #{userId} and is_enabled_for_route = 1
            """)
    int countEnabled(@Param("userId") Long userId);

    @Insert("""
            insert into user_places(user_id, name, address, longitude, latitude, category_id, brand_name, notes,
                                    is_enabled_for_route, created_at, updated_at)
            values(#{userId}, #{name}, #{address}, #{longitude}, #{latitude}, #{categoryId}, #{brandName}, #{notes},
                   #{isEnabledForRoute}, now(), now())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserPlaceEntity place);

    @Delete("""
            delete from user_places
            where id = #{id} and user_id = #{userId}
            """)
    int deleteMine(@Param("userId") Long userId, @Param("id") Long id);
}
