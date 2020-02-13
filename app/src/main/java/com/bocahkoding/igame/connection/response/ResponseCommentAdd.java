package com.bocahkoding.igame.connection.response;

import com.bocahkoding.igame.model.Comment;

import java.io.Serializable;

public class ResponseCommentAdd implements Serializable {
    public String code = "";
    public Comment comment = new Comment();
}
