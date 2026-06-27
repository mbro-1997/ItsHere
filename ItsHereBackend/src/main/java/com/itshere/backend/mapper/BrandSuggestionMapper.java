package com.itshere.backend.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface BrandSuggestionMapper {

    @Insert("""
            insert ignore into brand_suggestions(user_id, raw_name, normalized_name, status, created_at, updated_at)
            values(#{userId}, #{rawName}, #{normalizedName}, 'pending', now(), now())
            """)
    int insertIgnore(@Param("userId") Long userId,
                     @Param("rawName") String rawName,
                     @Param("normalizedName") String normalizedName);

    @Select("""
            select count(distinct user_id)
            from brand_suggestions
            where normalized_name = #{normalizedName}
            """)
    int countDistinctUsers(@Param("normalizedName") String normalizedName);

    @Update("""
            update brand_suggestions
            set status = 'reached', updated_at = now()
            where normalized_name = #{normalizedName}
              and status = 'pending'
            """)
    int markReached(@Param("normalizedName") String normalizedName);

    @Select("""
            select case
                when sum(case when status = 'imported' then 1 else 0 end) > 0 then 'imported'
                when sum(case when status = 'reached' then 1 else 0 end) > 0 then 'reached'
                when count(distinct user_id) >= 10 then 'reached'
                when sum(case when status = 'rejected' then 1 else 0 end) = count(*) then 'rejected'
                else 'pending'
            end
            from brand_suggestions
            where normalized_name = #{normalizedName}
            """)
    String findAggregateStatus(@Param("normalizedName") String normalizedName);
}
