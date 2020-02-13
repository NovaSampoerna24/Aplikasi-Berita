package com.bocahkoding.igame.model;

import com.bocahkoding.igame.model.type.SortType;

import java.io.Serializable;

public class SortBy implements Serializable {

    public SortType type = SortType.DEFAULT;
    public String label;
    public String column = "id";
    public String order = "DESC";

    public SortBy() {
    }

    public SortBy(SortType type, String label, String column, String order) {
        this.type = type;
        this.label = label;
        this.column = column;
        this.order = order;
    }

}
