package com.bocahkoding.igame.model;

import java.io.Serializable;

public class SearchBody implements Serializable {

    public int page = 1;
    public int count = 10;
    public String q = "";
    public String col = "id";
    public String ord = "DESC";
    public long topic_id = -1;
    public int feat = -1;

    public SearchBody() {
    }

    public SearchBody(int page, int count, String q) {
        this.page = page;
        this.count = count;
        this.q = q;
    }

    public SearchBody(int page, int count, int feat) {
        this.page = page;
        this.count = count;
        this.feat = feat;

    }
}
