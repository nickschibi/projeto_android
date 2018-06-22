package com.example.bonnie.picturesque;

public class Photo {

    private String id;
    private String url;
    private String tag;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Photo() {

    }

    @Override
    public String toString() {
        return "Photo{" +
                "id='" + id + '\'' +
                ", url='" + url + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Photo(String id, String url, String tag) {

        this.id = id;
        this.url = url;
        this.tag = tag;
    }
}
