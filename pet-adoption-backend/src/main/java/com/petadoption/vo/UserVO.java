package com.petadoption.vo;

import lombok.Data;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String avatar;
    private String role;
    private String status;
    private String bio;
    private String certStatus;
    private String certData;
    private String shelterName;
    private String shelterAddress;
}
