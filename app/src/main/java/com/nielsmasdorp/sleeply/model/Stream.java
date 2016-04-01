package com.nielsmasdorp.sleeply.model;

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
public class Stream {

    private int id;
    private String url;
    private String title;
    private String desc;
    private int bigImgRes;
    private int smallImgRes;

    public Stream(int id, String url, String title, String desc, int bigImgRes, int smallImgRes) {
        this.id = id;
        this.url = url;
        this.title = title;
        this.desc = desc;
        this.bigImgRes = bigImgRes;
        this.smallImgRes = smallImgRes;
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

    public int getBigImgRes() {
        return bigImgRes;
    }

    public void setBigImgRes(int bigImgRes) {
        this.bigImgRes = bigImgRes;
    }

    public int getSmallImgRes() {
        return smallImgRes;
    }

    public void setSmallImgRes(int smallImgRes) {
        this.smallImgRes = smallImgRes;
    }
}
