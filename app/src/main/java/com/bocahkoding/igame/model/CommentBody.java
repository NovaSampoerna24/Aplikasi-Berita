package com.bocahkoding.igame.model;

import java.io.Serializable;

public class CommentBody implements Serializable {

    public long news_id = -1;
    public long user_app_id = -1;
    public String comment = "";

    public CommentBody() {
    }

    public CommentBody(long news_id, long user_app_id, String comment) {
        this.news_id = news_id;
        this.user_app_id = user_app_id;
        this.comment = comment;
    }
}
