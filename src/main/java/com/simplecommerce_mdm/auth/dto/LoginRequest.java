package com.simplecommerce_mdm.auth.dto;

import lombok.Getter;

import java.io.Serializable;

@Getter
public class LoginRequest implements Serializable {
    private String email;
    private String password;
    private String platform; // web, mobile, tablet
    private String deviceToken; // for push notify
    private String versionApp;
}

