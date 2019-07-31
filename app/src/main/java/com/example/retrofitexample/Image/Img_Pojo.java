package com.example.retrofitexample.Image;

import com.google.gson.annotations.SerializedName;

public class Img_Pojo {

    @SerializedName("image_name")
    private String Title;

    ////비트맵->스트링 이미지파일
    @SerializedName("image")
    private String Image;

    @SerializedName("response")
    private String Response;

    public String getResponse() {
        return Response;
    }
}
