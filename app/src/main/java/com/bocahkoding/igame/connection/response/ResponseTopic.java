package com.bocahkoding.igame.connection.response;

import com.bocahkoding.igame.model.Topic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ResponseTopic implements Serializable {

    public String status = "";
    public int count = -1;
    public int count_total = -1;
    public int pages = -1;
    public List<Topic> topics = new ArrayList<>();

}
