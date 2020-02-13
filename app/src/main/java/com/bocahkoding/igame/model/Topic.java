package com.bocahkoding.igame.model;

import java.io.Serializable;

public class Topic implements Serializable {
    public long id = -1;
    public String name = "";
    public String icon = "";
    public String color = "";
    public long priority = -1;
    public long featured = 0;
    public long created_at = -1;
    public long last_update = -1;

    public Topic() {
    }

    public Topic(long id, String name) {
        this.id = id;
        this.name = name;
    }

}
