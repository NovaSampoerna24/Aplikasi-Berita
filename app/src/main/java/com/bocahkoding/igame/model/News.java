package com.bocahkoding.igame.model;

import com.bocahkoding.igame.model.type.SourceType;

import java.io.Serializable;

public class News implements Serializable {

    public long id = -1;
    public String title = "";
    public String content = "";
    public String image = "";
    public String url = "";
    public String type = "";
    public long total_view = 0;
    public long total_comment = 0;
    public int featured = 0;
    public long date = -1;
    public long created_at = -1;
    public long last_update = -1;
    public SourceType source_type = SourceType.DEFAULT;

    public boolean isDraft() {
        return !(content != null && !content.trim().equals(""));
    }

    public News() {
    }

    public News(long id, SourceType source_type) {
        this.id = id;
        this.source_type = source_type;
    }
}
