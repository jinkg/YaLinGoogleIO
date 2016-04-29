package com.yalin.googleio.io.model;

import com.google.gson.annotations.SerializedName;

/**
 * 作者：YaLin
 * 日期：2016/4/19.
 */
public class Tag {
    public String tag;
    public String name;
    public String category;
    public String color;
    @SerializedName("abstract")
    public String _abstract;
    public int order_in_category;
}
