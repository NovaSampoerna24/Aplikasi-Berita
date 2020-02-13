package com.bocahkoding.igame.model;

import com.bocahkoding.igame.model.type.SortType;

import java.io.Serializable;

public class SearchFilter implements Serializable {

    public Topic topic = new Topic();
    public SortBy sort_by = new SortBy();

    public SearchFilter() {
    }

    public SearchFilter(Topic topic) {
        this.topic = topic;
    }

    public SearchFilter(Topic topic, SortBy sort_by) {
        this.sort_by = sort_by;
    }

    public boolean isDefault() {
        return topic.id == -1 && sort_by.type == SortType.DEFAULT;
    }
}
