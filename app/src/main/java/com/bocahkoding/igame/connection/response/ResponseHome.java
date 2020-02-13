package com.bocahkoding.igame.connection.response;


import com.bocahkoding.igame.model.News;
import com.bocahkoding.igame.model.Topic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ResponseHome implements Serializable {

    public String status = "";
    public List<News> featured = new ArrayList<>();
    public List<Topic> topic = new ArrayList<>();

}
