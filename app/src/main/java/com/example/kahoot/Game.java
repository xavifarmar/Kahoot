package com.example.kahoot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Game {
    private int gameCode;
    private String status;
    private Map<String, Object> questionary;;

    public Game() {
    }

    public Game(int gameCode, String status) {
        this.gameCode = gameCode;
        this.status = status;
        this.questionary = new HashMap<>();
    }

    public int getGameCode() {
        return gameCode;
    }

    public void setGameCode(int gameCode) {
        this.gameCode = gameCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, Object> getQuestionary() {
        return questionary;
    }

    public void setQuestionary(Map<String, Object> players) {
        this.questionary = players;
    }
}
