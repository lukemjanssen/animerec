package com.animerec.animerec;

public class Anime {
    String mal_id;
    String url;
    String title;

    private int weight;

    // getters and setters...
    public String getTitle() {
        return title;
    }

    public String getMal_id() {
        return mal_id;
    }

    public String getUrl() {
        return url;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
