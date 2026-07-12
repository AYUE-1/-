package com.petadoption.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResultVO {
    private String token;
    private UserVO user;
}
