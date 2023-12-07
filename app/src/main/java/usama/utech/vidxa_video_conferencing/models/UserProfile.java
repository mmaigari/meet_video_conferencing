package usama.utech.vidxa_video_conferencing.models;

import androidx.annotation.Keep;

import java.io.Serializable;

@Keep

public class UserProfile implements Serializable {

    String id;
    String name;
    String email;
    String profile_pic;

public UserProfile() {
}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfile_pic() {
        return profile_pic;
    }

    public void setProfile_pic(String profile_pic) {
        this.profile_pic = profile_pic;
    }
}
