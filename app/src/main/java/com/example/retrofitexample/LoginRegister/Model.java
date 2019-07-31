package com.example.retrofitexample.LoginRegister;


/**
 * Created by Kamere on 8/31/2018.
 */

    //새 Java 파일 이름을 Model 클래스로 작성하십시오.
    //이 클래스는 서버에서 보내고받을 데이터를 가져오고 설정하는 사용자입니다.
    //사용자가 등록 할 때 다음 JSON을 보냅니다.
    //

    /*
    {
        "username":"john",
        "email":"john@gmail.com",
        "password":"12345678"
    }
    On response we receive
    {
        "isSuccess":1,
        "message":"Registered Successfully"
    }
     */

    //사용자가 로그인하면 우리가 보냅니다.
    /*
    {
        "username":"john",
        "password":"12345678"
    }
    On response we receive
    {
        "isSuccess":1,
        "username":"john",
        "message":"Successful"
    }
     */
public class Model {

    //To take care of all this JSON, we shall need.
    private String username;
    private String email;
    private String password;
    private String image;
    private int id;

    private int isSuccess;
    private String message;
    private String img_path;


    public Model(String username, String email, String password, int isSuccess, String message) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.isSuccess = isSuccess;
        this.message = message;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getIsSuccess() {
        return isSuccess;
    }

    public void setIsSuccess(int isSuccess) {
        this.isSuccess = isSuccess;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImg_path() {
        return img_path;
    }

    public void setImg_path(String img_path) {
        this.img_path = img_path;
    }
}