package com.example.retrofitexample.Retrofit;


import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//새 Java 파일을 만들고 이름을 ApiClient로 지정하십시오.이 파일은 클라이언트입니다.
//이 클래스에서는 기본 URL을 정의합니다.
//참고 : 슬래시를 사용하여 API 끝으로 기본 URL을 정의 할 때 http://192.168.43.254/larntech/api/

//실질적인 통신에 앞서, Retrofit을 이용한 REST API 통신을 위해서는 2가지의 선행 작업이 필요합니다.
//
//Retrofit 객체 생성.
//REST API 명세에 맞는 Interface 선언.

public class ApiClient {

    //base url
    public static final String BASE_URL = "http://13.209.49.7/movieApp/";
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

