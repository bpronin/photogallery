package com.bo.android.photogallery;


import java.util.ArrayList;
import java.util.List;

public class FlickrItems {

    private List<GalleryItem> items;
    private int total;
    private int pages;

    public FlickrItems(List<GalleryItem> items, int total, int pages) {
        this.items = items;
        this.total = total;
        this.pages = pages;
    }

    public FlickrItems() {
        this(new ArrayList<GalleryItem>(), 0, 0);
    }

    public List<GalleryItem> getItems() {
        return items;
    }

    public int getTotal() {
        return total;
    }

    public int getPages() {
        return pages;
    }

    @Override
    public String toString() {
        return "FlickrItems{" +
                "items=" + items +
                ", total=" + total +
                ", pages=" + pages +
                '}';
    }
}
