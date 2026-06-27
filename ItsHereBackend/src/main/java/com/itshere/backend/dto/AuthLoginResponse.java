package com.itshere.backend.dto;

public class AuthLoginResponse {
    private Long userId;
    private String openid;
    private String token;
    private String nickname;

    public AuthLoginResponse(Long userId, String openid, String token, String nickname) {
        this.userId = userId;
        this.openid = openid;
        this.token = token;
        this.nickname = nickname;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
