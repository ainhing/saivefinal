package com.example.saive.models;

public class Notification {
    public enum Type {
        DROP, ORDER, CAPSULE, REMINDER
    }

    private String id;
    private String title;
    private String description;
    private String actionLabel;
    private String time;
    private int iconResId;
    private int iconBgColor;
    private boolean isRead;
    private int dotColor;
    private Type type;

    private long timestamp;

    public Notification(String id, String title, String description, String actionLabel, String time, int iconResId, int iconBgColor, boolean isRead, int dotColor, Type type) {
        this(id, title, description, actionLabel, time, iconResId, iconBgColor, isRead, dotColor, type, System.currentTimeMillis());
    }

    public Notification(String id, String title, String description, String actionLabel, String time, int iconResId, int iconBgColor, boolean isRead, int dotColor, Type type, long timestamp) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.actionLabel = actionLabel;
        this.time = time;
        this.iconResId = iconResId;
        this.iconBgColor = iconBgColor;
        this.isRead = isRead;
        this.dotColor = dotColor;
        this.type = type;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getActionLabel() { return actionLabel; }
    public String getTime() { return time; }
    public int getIconResId() { return iconResId; }
    public int getIconBgColor() { return iconBgColor; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public int getDotColor() { return dotColor; }
    public long getTimestamp() { return timestamp; }
    public Type getType() { return type; }
}
