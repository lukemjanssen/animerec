package com.animerec.animerec;

/**
 * This class represents a user and their score for a particular anime.
 */
public class UserScore {
    private String username;
    private int score;

    // getters and setters
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public int getScore() {
        return score;
    }
    public void setScore(int score) {
        this.score = score;
    }

}