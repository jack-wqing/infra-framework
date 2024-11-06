package com.jindi.infra.space.user;

import lombok.Data;

import java.util.Date;

@Data
public class User {

    private Long id;
    private String username;
    private String address;
    private Date create_time;
}
