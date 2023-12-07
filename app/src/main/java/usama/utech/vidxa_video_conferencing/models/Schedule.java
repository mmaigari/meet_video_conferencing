package usama.utech.vidxa_video_conferencing.models;

import androidx.annotation.Keep;

import java.io.Serializable;

@Keep

public class Schedule implements Serializable {

    String id;
    String title;
    String date;
    String startTime;
    String duration;
    String reminderIdandrequestcode;
    String userId;
    String meeetingId;

    public Schedule() {
    }


    public String getReminderIdandrequestcode() {
        return reminderIdandrequestcode;
    }

    public void setReminderIdandrequestcode(String reminderIdandrequestcode) {
        this.reminderIdandrequestcode = reminderIdandrequestcode;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getMeeetingId() {
        return meeetingId;
    }

    public void setMeeetingId(String meeetingId) {
        this.meeetingId = meeetingId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
