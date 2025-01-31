package com.example.kahoot;

public class Player {
    private String image_emoji;
    private String username;
    private String image_color;

    // Constructor vacío necesario para Firebase
    public Player() {}

    // Constructor con parámetros

    public Player(String image_emoji, String username, String color) {
        this.image_emoji = image_emoji;
        this.username = username;
        this.image_color = color;
    }

    public String getImage_emoji() {
        return image_emoji;
    }

    public void setImage_emoji(String image_emoji) {
        this.image_emoji = image_emoji;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImage_color() {
        return image_color;
    }

    public void setImage_color(String image_color) {
        this.image_color = image_color;
    }
}

