package com.example.retrofitexample.MyPage;

import com.google.gson.annotations.SerializedName;

public class MyPage_Img {

    @SerializedName("image_name")
    private String Title;

    ////비트맵->스트링 이미지파일
    @SerializedName("image")
    private String Image;

    @SerializedName("response")
    private String Response;

    private String img_path;

    public String getResponse() {
        return Response;
    }

    public String getImg_path() {
        return img_path;
    }

    public void setImg_path(String img_path) {
        this.img_path = img_path;
    }
}
