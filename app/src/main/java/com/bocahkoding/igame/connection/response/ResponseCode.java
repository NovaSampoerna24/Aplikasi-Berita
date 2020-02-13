package com.bocahkoding.igame.connection.response;

import com.bocahkoding.igame.model.User;

import java.io.Serializable;

public class ResponseCode implements Serializable {
    public String code = "";
    public User user = new User();
}
