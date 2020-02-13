package com.bocahkoding.igame.model;

import java.io.Serializable;

public class Comment implements Serializable {
    public long id = -1;
    public long news_id = -1;
    public long user_app_id = -1;
    public String comment = "";
    public String status = "";
    public String name = "";
    public String image = "";
    public long created_at = -1;
    public long last_update = -1;
}
