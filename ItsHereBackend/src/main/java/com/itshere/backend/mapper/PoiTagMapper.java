package com.itshere.backend.mapper;

import com.itshere.backend.dto.TagBadgeDto;
import com.itshere.backend.entity.PoiTagEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface PoiTagMapper {

    @Select("""
            select t.*
            from poi_tags t
            join poi_tag_relations r on r.tag_id = t.id
            where r.created_by_user_id = #{userId}
              and r.poi_type = #{poiType}
              and r.poi_id = #{poiId}
            order by t.id asc
            """)
    List<PoiTagEntity> findMineForPoi(@Param("userId") Long userId,
                                      @Param("poiType") String poiType,
                                      @Param("poiId") Long poiId);

    @Select("""
            select visible_tags.name
            from (
                select
                    t.normalized_name,
                    substring_index(
                        group_concat(
                            t.name
                            order by
                                case when r.created_by_user_id = #{userId} then 0 else 1 end,
                                r.created_at asc
                            separator '\\n'
                        ),
                        '\\n',
                        1
                    ) as name,
                    max(case when r.created_by_user_id = #{userId} then 1 else 0 end) as mine_first
                from poi_tags t
                join poi_tag_relations r on r.tag_id = t.id
                where r.poi_type = #{poiType}
                  and r.poi_id = #{poiId}
                  and (r.created_by_user_id = #{userId} or r.is_public = 1)
                group by t.normalized_name
            ) visible_tags
            order by visible_tags.mine_first desc, visible_tags.name asc
            """)
    List<String> findVisibleTagNamesForPoi(@Param("userId") Long userId,
                                           @Param("poiType") String poiType,
                                           @Param("poiId") Long poiId);

    @Select("""
            select
                visible_tags.name,
                case when visible_tags.public_first = 1 then 'public' else 'private' end as visibility
            from (
                select
                    t.normalized_name,
                    substring_index(
                        group_concat(
                            t.name
                            order by
                                case when r.created_by_user_id = #{userId} then 0 else 1 end,
                                r.created_at asc
                            separator '\\n'
                        ),
                        '\\n',
                        1
                    ) as name,
                    max(case when r.created_by_user_id = #{userId} then 1 else 0 end) as mine_first,
                    max(case when r.is_public = 1 then 1 else 0 end) as public_first
                from poi_tags t
                join poi_tag_relations r on r.tag_id = t.id
                where r.poi_type = #{poiType}
                  and r.poi_id = #{poiId}
                  and (r.created_by_user_id = #{userId} or r.is_public = 1)
                group by t.normalized_name
            ) visible_tags
            order by visible_tags.mine_first desc, visible_tags.public_first desc, visible_tags.name asc
            """)
    List<TagBadgeDto> findVisibleTagBadgesForPoi(@Param("userId") Long userId,
                                                 @Param("poiType") String poiType,
                                                 @Param("poiId") Long poiId);

    @Select("""
            select distinct t.*
            from poi_tags t
            join poi_tag_relations r on r.tag_id = t.id
            where r.created_by_user_id = #{userId}
            order by t.name asc
            """)
    List<PoiTagEntity> findMineTags(@Param("userId") Long userId);

    @Select("""
            select * from poi_tags
            where source_type = 'user'
              and created_by_user_id = #{userId}
              and normalized_name = #{normalizedName}
            limit 1
            """)
    PoiTagEntity findMineByName(@Param("userId") Long userId, @Param("normalizedName") String normalizedName);

    @Insert("""
            insert into poi_tags(name, normalized_name, source_type, created_by_user_id, is_public, created_at, updated_at)
            values(#{name}, #{normalizedName}, 'user', #{createdByUserId}, 0, now(), now())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertTag(PoiTagEntity tag);

    @Insert("""
            insert ignore into poi_tag_relations(poi_id, poi_type, tag_id, created_by_user_id, is_public, created_at)
            values(#{poiId}, #{poiType}, #{tagId}, #{userId}, 0, now())
            """)
    int insertRelation(@Param("userId") Long userId,
                       @Param("poiType") String poiType,
                       @Param("poiId") Long poiId,
                       @Param("tagId") Long tagId);

    @Select("""
            select count(distinct r.created_by_user_id)
            from poi_tag_relations r
            join poi_tags t on t.id = r.tag_id
            where r.poi_type = #{poiType}
              and r.poi_id = #{poiId}
              and t.normalized_name = #{normalizedName}
            """)
    int countDistinctUsersForPoiTag(@Param("poiType") String poiType,
                                    @Param("poiId") Long poiId,
                                    @Param("normalizedName") String normalizedName);

    @Update("""
            update poi_tag_relations r
            join poi_tags t on t.id = r.tag_id
            set r.is_public = 1
            where r.poi_type = #{poiType}
              and r.poi_id = #{poiId}
              and t.normalized_name = #{normalizedName}
            """)
    int promotePoiTagRelationsToPublic(@Param("poiType") String poiType,
                                       @Param("poiId") Long poiId,
                                       @Param("normalizedName") String normalizedName);

    @Delete("""
            delete r
            from poi_tag_relations r
            join poi_tags t on t.id = r.tag_id
            where r.created_by_user_id = #{userId}
              and r.poi_type = #{poiType}
              and r.poi_id = #{poiId}
              and t.source_type = 'user'
              and t.created_by_user_id = #{userId}
              and t.normalized_name = #{normalizedName}
            """)
    int deleteRelationByName(@Param("userId") Long userId,
                             @Param("poiType") String poiType,
                             @Param("poiId") Long poiId,
                             @Param("normalizedName") String normalizedName);
}
