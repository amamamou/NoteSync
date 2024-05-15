package entities;

import java.sql.Timestamp;

public class Note {
    private int id;
    private String title;
    private String content;
    private Timestamp timestamp;
    private int categoryId; // New field for category ID
    private int userId; // Field for user ID


    public Note() {
        this.timestamp = new Timestamp(System.currentTimeMillis());
    }


    public Note(int id, String title, String content, Timestamp timestamp, int categoryId, int userId) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
        this.categoryId = categoryId;
        this.userId = userId;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
