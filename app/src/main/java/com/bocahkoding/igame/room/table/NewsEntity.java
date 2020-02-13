package com.bocahkoding.igame.room.table;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.bocahkoding.igame.model.News;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
/*
 * To save favorites news
 */

@Entity(tableName = "news")
public class NewsEntity {

    @PrimaryKey
    private long id = -1;

    @ColumnInfo(name = "title")
    private String title = "";

    @ColumnInfo(name = "content")
    private String content = "";

    @ColumnInfo(name = "image")
    private String image = "";

    @ColumnInfo(name = "url")
    private String url = "";

    @ColumnInfo(name = "type")
    private String type = "";

    @ColumnInfo(name = "total_view")
    private long total_view = 0;

    @ColumnInfo(name = "total_comment")
    private long total_comment = 0;

    @ColumnInfo(name = "featured")
    private int featured = 0;

    @ColumnInfo(name = "date")
    private long date = -1;

    @ColumnInfo(name = "saved_date")
    private long savedDate = -1;

    @ColumnInfo(name = "topics")
    private String topics = null;

    @ColumnInfo(name = "gallery")
    private String gallery = null;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTotal_view() {
        return total_view;
    }

    public void setTotal_view(long total_view) {
        this.total_view = total_view;
    }

    public long getTotal_comment() {
        return total_comment;
    }

    public void setTotal_comment(long total_comment) {
        this.total_comment = total_comment;
    }

    public int getFeatured() {
        return featured;
    }

    public void setFeatured(int featured) {
        this.featured = featured;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public long getSavedDate() {
        return savedDate;
    }

    public void setSavedDate(long savedDate) {
        this.savedDate = savedDate;
    }

    public String getTopics() {
        return topics;
    }

    public void setTopics(String topics) {
        this.topics = topics;
    }

    public String getGallery() {
        return gallery;
    }

    public void setGallery(String gallery) {
        this.gallery = gallery;
    }

    public void setTopicsList(ArrayList<String> topics) {
        this.topics = new Gson().toJson(topics);
    }

    public ArrayList<String> getTopicsList() {
        if(this.topics == null || this.topics.equals("")){
            return new ArrayList<>();
        }
        ArrayList<String> topics_res = new Gson().fromJson(this.topics, new TypeToken<ArrayList<String>>(){}.getType());
        return topics_res;
    }

    public void setGalleryList(ArrayList<String> images) {
        this.gallery = new Gson().toJson(images);
    }

    public ArrayList<String> getGalleryList() {
        if(this.image == null || this.image.equals("")){
            return new ArrayList<>();
        }
        ArrayList<String> images_res = new Gson().fromJson(this.gallery, new TypeToken<ArrayList<String>>(){}.getType());
        return images_res;
    }

    public static NewsEntity entity(News n) {
        NewsEntity entity = new NewsEntity();
        entity.setId(n.id);
        entity.setTitle(n.title);
        entity.setContent(n.content);
        entity.setImage(n.image);
        entity.setUrl(n.url);
        entity.setType(n.type);
        entity.setTotal_view(n.total_view);
        entity.setTotal_comment(n.total_comment);
        entity.setFeatured(n.featured);
        entity.setDate(n.date);
        entity.setSavedDate(System.currentTimeMillis());
        return entity;
    }

    public News original() {
        News n = new News();
        n.id = getId();
        n.title = getTitle();
        n.content = getContent();
        n.image = getImage();
        n.url = getUrl();
        n.type = getType();
        n.total_view = getTotal_view();
        n.total_comment = getTotal_comment();
        n.featured = getFeatured();
        n.date = getDate();
        return n;
    }
}
