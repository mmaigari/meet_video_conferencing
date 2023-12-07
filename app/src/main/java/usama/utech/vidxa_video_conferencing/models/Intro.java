package usama.utech.vidxa_video_conferencing.models;

import android.graphics.drawable.Drawable;

import androidx.annotation.Keep;

@Keep
public class Intro {

    Drawable img;
    String title;
public Intro() {
}

    public Intro(Drawable img, String title) {
        this.img = img;
        this.title = title;
    }

    public Drawable getImg() {
        return img;
    }

    public void setImg(Drawable img) {
        this.img = img;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
