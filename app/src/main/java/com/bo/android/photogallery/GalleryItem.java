package com.bo.android.photogallery;

public class GalleryItem {

    private String id;
    private String caption;
    private String url;
    private String owner;

    public GalleryItem() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getPhotoPageUrl() {
        return "http://www.flickr.com/photos/" + owner + "/" + id;
    }

    @Override
    public String toString() {
        return caption;
    }
}
