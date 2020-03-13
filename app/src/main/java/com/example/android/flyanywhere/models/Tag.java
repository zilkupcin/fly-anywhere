package com.example.android.flyanywhere.models;

/**
 * Created by baroc on 02/05/2018.
 */

public class Tag {

    private Integer tagId;
    private String tagName;
    private Boolean tagIsSelected;

    public Tag(Integer tagId, String tagName, Boolean tagIsSelected) {
        this.tagId = tagId;
        this.tagName = tagName;
        this.tagIsSelected = tagIsSelected;
    }

    public Integer getTagId() {
        return tagId;
    }

    public void setTagId(Integer tagId) {
        this.tagId = tagId;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public Boolean getTagIsSelected() {
        return tagIsSelected;
    }

    public void setTagIsSelected(Boolean tagIsSelected) {
        this.tagIsSelected = tagIsSelected;
    }
}
