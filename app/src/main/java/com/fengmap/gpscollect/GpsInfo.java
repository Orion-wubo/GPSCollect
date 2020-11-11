package com.fengmap.gpscollect;

import org.litepal.crud.LitePalSupport;

public class GpsInfo extends LitePalSupport {
    private int id;
    private String info;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
