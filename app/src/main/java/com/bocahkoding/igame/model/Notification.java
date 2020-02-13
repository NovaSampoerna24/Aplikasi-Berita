package com.bocahkoding.igame.model;

import java.io.Serializable;

public class Notification implements Serializable {

    public Long id;
    public String title;
    public String content;
    public String type;
    public Long obj_id;
    public String image;
    public String link;

    // extra attribute
    public Boolean read = false;
    public Long created_at;

}
