package com.beichen.hookwxx5.data;

import org.json.JSONObject;

public abstract class BaseItem {
    public static final String JSON_KEY_NAME = "game";
    public static final String JSON_VALUE_NAME = "data";

    protected String gameName;
    protected String appId;
    protected boolean available;
    protected String profile;


    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }



    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }


    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }



    public abstract JSONObject item2Json();
    public abstract BaseItem json2Item(JSONObject json);
    public abstract BaseItem copy();
}
