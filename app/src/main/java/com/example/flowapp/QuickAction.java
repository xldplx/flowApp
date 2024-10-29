package com.example.flowapp;

public class QuickAction {
    private int iconResId;
    private String title;

    public QuickAction(int iconResId, String title) {
        this.iconResId = iconResId;
        this.title = title;
    }

    public int getIconResId() {
        return iconResId;
    }

    public String getTitle() {
        return title;
    }
}