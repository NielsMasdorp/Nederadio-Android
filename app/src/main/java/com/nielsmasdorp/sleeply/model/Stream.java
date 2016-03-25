package com.nielsmasdorp.sleeply.model;

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
public class Stream {

    private int id;
    private String url;
    private String title;
    private String desc;
    private int imageResource;

    public Stream(int id, String url, String title, String desc, int imageResource) {
        this.id = id;
        this.url = url;
        this.title = title;
        this.desc = desc;
        this.imageResource = imageResource;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getImageResource() {
        return imageResource;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }
}
