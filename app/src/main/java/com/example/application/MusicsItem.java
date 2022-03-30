package com.example.application;

public class MusicsItem {
    int musics;
    int icon;
    String titleMusics;

    public MusicsItem(int musics, int icon, String titleMusics) {
        this.musics = musics;
        this.icon = icon;
        this.titleMusics = titleMusics;
    }

    public int getMusics() {
        return musics;
    }

    public void setMusics(int musics) {
        this.musics = musics;
    }

    public String getTitleMusics() {
        return titleMusics;
    }

    public void setTitleMusics(String titleMusics) {
        this.titleMusics = titleMusics;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
}
