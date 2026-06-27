package com.itshere.backend.mapper;

import com.itshere.backend.entity.UserEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper {

    @Select("select * from users where openid = #{openid} limit 1")
    UserEntity findByOpenid(@Param("openid") String openid);

    @Select("select * from users where id = #{id} limit 1")
    UserEntity findById(@Param("id") Long id);

    @Insert("""
            insert into users(openid, unionid, nickname, status, last_login_at, created_at, updated_at)
            values(#{openid}, #{unionid}, #{nickname}, 'normal', now(), now(), now())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserEntity user);

    @Update("update users set last_login_at = now(), updated_at = now() where id = #{id}")
    int touchLogin(@Param("id") Long id);

    @Update("update users set nickname = #{nickname}, updated_at = now() where id = #{id}")
    int updateNickname(@Param("id") Long id, @Param("nickname") String nickname);
}
