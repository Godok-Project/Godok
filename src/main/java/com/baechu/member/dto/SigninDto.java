package com.baechu.member.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SigninDto {

    private String email;

    private String password;

    private Integer sex;

    private Integer age;
}
