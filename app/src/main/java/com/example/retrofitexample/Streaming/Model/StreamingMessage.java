package com.example.retrofitexample.Streaming.Model;

/**
 * TCP 채팅 메세지를 담는 클래스
 */
public class StreamingMessage {

    //multi viewtype용 변수
    public static final int FIRST_TYPE=0;
    public static final int SECOND_TYPE=1;

    int mode;
    int roomNo;
    String profile;
    String senderEmail;
    String message;

    public StreamingMessage(int mode, String profile, String senderEmail, String message) {
        this.mode = mode;
        this.profile = profile;
        this.senderEmail = senderEmail;
        this.message = message;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public int getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(int roomNo) {
        this.roomNo = roomNo;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
