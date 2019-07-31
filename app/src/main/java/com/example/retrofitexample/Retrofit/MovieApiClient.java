package com.example.retrofitexample.Retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MovieApiClient {

    //base url
    public static final String BASE_URL = "http://dsjkim.iwinv.net/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL) //어떤 서버로 네트워크 통신을 요청할 것인지에 대한 설정
                    .addConverterFactory(GsonConverterFactory.create()) //통신이 완료된 후, 어떤 Converter를 이용하여 데이터를 파싱할 것인지에 대한 설정
                    .build(); //Retrofit.Builder객체에 설정한 정보를 이용하여 실질적으로 Retrofit 객체를 만들어 반환
        }
        return retrofit;
    }
}

