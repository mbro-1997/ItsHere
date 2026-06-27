package com.itshere.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itshere.backend.common.BusinessException;
import com.itshere.backend.config.ItsHereProperties;
import com.itshere.backend.dto.AuthLoginResponse;
import com.itshere.backend.entity.UserEntity;
import com.itshere.backend.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Locale;

@Service
public class WechatAuthService {

    private final ItsHereProperties properties;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;
    private final RestClient restClient = RestClient.create();

    public WechatAuthService(ItsHereProperties properties, UserMapper userMapper, ObjectMapper objectMapper) {
        this.properties = properties;
        this.userMapper = userMapper;
        this.objectMapper = objectMapper;
    }

    public AuthLoginResponse loginByCode(String code) {
        URI uri = UriComponentsBuilder.fromHttpUrl("https://api.weixin.qq.com/sns/jscode2session")
                .queryParam("appid", properties.getWechat().getAppId())
                .queryParam("secret", properties.getWechat().getAppSecret())
                .queryParam("js_code", code)
                .queryParam("grant_type", "authorization_code")
                .build(true)
                .toUri();

        // 外部接口调用：这里调用微信 jscode2session，用小程序 wx.login 的 code 换 openid。
        String body = restClient.get().uri(uri).retrieve().body(String.class);
        try {
            JsonNode root = objectMapper.readTree(body);
            if (root.has("errcode")) {
                throw new BusinessException("微信登录失败：" + root.path("errmsg").asText());
            }
            String openid = root.path("openid").asText();
            String unionid = root.path("unionid").asText(null);
            if (openid == null || openid.isBlank()) {
                throw new BusinessException("微信没有返回 openid");
            }
            return loginByOpenid(openid, unionid);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("解析微信登录结果失败：" + ex.getMessage());
        }
    }

    public AuthLoginResponse devLogin() {
        return loginByOpenid("dev-openid-local", null);
    }

    private AuthLoginResponse loginByOpenid(String openid, String unionid) {
        UserEntity user = userMapper.findByOpenid(openid);
        if (user == null) {
            user = new UserEntity();
            user.setOpenid(openid);
            user.setUnionid(unionid);
            user.setNickname(generateDefaultNickname(openid));
            userMapper.insert(user);
        } else {
            if (user.getNickname() == null || user.getNickname().isBlank()) {
                user.setNickname(generateDefaultNickname(openid));
                userMapper.updateNickname(user.getId(), user.getNickname());
            }
            userMapper.touchLogin(user.getId());
        }
        return new AuthLoginResponse(user.getId(), openid, String.valueOf(user.getId()), user.getNickname());
    }

    private String generateDefaultNickname(String openid) {
        long value = Math.abs((long) (openid == null ? 0 : openid.hashCode()));
        String suffix = Long.toString(value, 36).toUpperCase(Locale.ROOT);
        if (suffix.length() < 4) {
            suffix = ("0000" + suffix).substring(suffix.length());
        }
        return "济南微信用户" + suffix.substring(0, 4);
    }
}
