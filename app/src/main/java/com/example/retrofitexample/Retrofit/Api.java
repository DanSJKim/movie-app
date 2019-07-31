package com.example.retrofitexample.Retrofit;

import com.example.retrofitexample.Board.BoardItem;
import com.example.retrofitexample.Board.BoardResponse;
import com.example.retrofitexample.Board.Comment.CommentItem;
import com.example.retrofitexample.Board.Comment.CommentResponse;
import com.example.retrofitexample.Board.Image.ResponseImages;
import com.example.retrofitexample.Board.ResponseServer;
import com.example.retrofitexample.Chat.Model.MessageContent;
import com.example.retrofitexample.Chat.Model.MessageContentResponse;
import com.example.retrofitexample.Chat.Model.MessageListContent;
import com.example.retrofitexample.Chat.Model.MessageListResponse;
import com.example.retrofitexample.Chat.Model.ResponseResult;
import com.example.retrofitexample.Chat.Model.Room;
import com.example.retrofitexample.Chat.Model.UserInfo;
import com.example.retrofitexample.Image.Img_Pojo;
import com.example.retrofitexample.LoginRegister.Model;
import com.example.retrofitexample.BoxOffice.Item.MovieResponse;
import com.example.retrofitexample.Map.TheaterDetailResponse;
import com.example.retrofitexample.Map.TheaterResponse;
import com.example.retrofitexample.MovieSearch.SearchResponse;
import com.example.retrofitexample.MyPage.MyPage_Img;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;


//모든 메소드와 매개 변수를 정의 할 수있는 새로운 Retrofit API 인터페이스
//실질적인 통신 전에 선행되어야 할 두번째 작업입니다.
//
//우리는 네트워킹과 관련된 코드를 작성하기에 앞서, 서버 개발자와 사전에 협의된 REST API 명세에 맞게 Interface를 선언해주어야 합니다.
public interface Api {

    //@가 붙은 키워드는 흔히 Java 언어에서 사용되는 Annotation 키워드 입니다.//Retrofit은 REST API 통신에 필요한 요소들에 대해서 Annotation을 만들어 두었습니다.
    //Call<Model>의 경우는 해당 코드를 추적하여 들여다 보면 Retrofit이 Call<T> 형태의 Generic Type을 매개변수로 받는 Callback Interface인 것을 알 수 있습니다.

    //가입
    @POST("retrofit/register.php")
    @FormUrlEncoded
    Call<Model> register(@Field("username") String username, @Field("email") String email, @Field("password") String password);

    //로그인
    @POST("retrofit/login.php")
    @FormUrlEncoded
    Call<Model> login(@Field("email") String username, @Field("password") String password);

    //마이페이지 수정
    @POST("retrofit/UpdateMyPage.php")
    @FormUrlEncoded
    Call<Model> updatemypage(@Field("email") String email, @Field("username") String username, @Field("password") String password);

    //게시물 업로드
    @POST("retrofit/BoardUpload.php")
    @FormUrlEncoded
    Call<BoardItem> boardUpload(@Field("userid") int userid, @Field("email") String email, @Field("title") String title, @Field("content") String content);

    //게시물 수정
    @POST("retrofit/BoardUpdate.php")
    @FormUrlEncoded
    Call<ResponseServer> boardUpdate(@Field("id") int id, @Field("content") String content, @Field("imgIdList[]") List<Integer> imgIdList);

    //게시물 삭제
    @POST("retrofit/BoardRemove.php")
    @FormUrlEncoded
    Call<BoardItem> boardRemove(@Field("id") int id);

    //게시물 목록 출력
    @POST("retrofit/BoardList.php")
    @FormUrlEncoded
    Call<BoardResponse> getBoard(@Field("userId") int id, @Field("limit") int limit);

    //이미지 업로드 예제
    @FormUrlEncoded
    @POST("image/upload.php")
    Call<Img_Pojo> uploadImage(@Field("image_name") String title, @Field("image") String image);

    //마이페이지 이미지 업로드
    @FormUrlEncoded
    @POST("image/ProfileImageUpload.php")
    Call<MyPage_Img> uploadProfileImage(@Field("image_name") String image_name, @Field("image") String image, @Field("userid") int userid);

    //게시물 좋아요
    @FormUrlEncoded
    @POST("retrofit/BoardLike.php")
    Call<BoardItem> boardLike(@Field("user_id") int userId, @Field("board_id") int boardId);

    //게시물 댓글 업로드
    @FormUrlEncoded
    @POST("retrofit/BoardUploadComment.php")
    Call<CommentItem> uploadComment(@Field("content") String content, @Field("user_id") int user_id, @Field("board_id") int board_id, @Field("cmtid") int cmtid);

    //게시물 댓글 출력
    @FormUrlEncoded
    @POST("retrofit/getBoardComment.php")
    Call<CommentResponse> listComment(@Field("board_id") int boardId);

    //게시물 댓글 삭제
    @FormUrlEncoded
    @POST("retrofit/BoardCommentDelete.php")
    Call<CommentItem> deleteComment(@Field("cmtid") int cmtid, @Field("parent") int parent);

    //게시물 댓글 수정
    @FormUrlEncoded
    @POST("retrofit/BoardCommentUpdate.php")
    Call<CommentItem> updateComment(@Field("cmtid") int cmtid, @Field("content") String content);

    //프로필 이미지 가져오기
    @FormUrlEncoded
    @POST("image/getProfileImage.php")
    Call<MyPage_Img> getProfileImage(@Field("userid") int userid);

    //좋아요 목록 출력
    @POST("retrofit/BoardLikeList.php")
    @FormUrlEncoded
    Call<BoardResponse> getLikeList(@Field("board_id") int board_id);

    //다중 이미지 전송
    @Multipart
    @POST("image/uploadImages.php")
    Call<ResponseImages>uploadMultipleFilesDynamic(
            @Part List<MultipartBody.Part> images,
            @Part("boardId") Integer boardId
    );

    //다중 이미지 전송 게시물 수정
    @Multipart
    @POST("image/updateImages.php")
    Call<ResponseImages>uploadMultipleFilesDynamic2(
            @Part List<MultipartBody.Part> images,
            @Part ("imgIdList[]") List<Integer> imgIdList
    );

    //네이버API 박스오피스 리스트
    @GET("kobisopenapi/webservice/rest/boxoffice/searchDailyBoxOfficeList.json?key=a711b9ea0bef9b28b595fa6ce0684f55")
    Call<MovieResponse> getBoxOfficeList(
        @Query("targetDt") String targetDt
    );

    //영화 검색
    @FormUrlEncoded
    @POST("retrofit/search/searchMovies.php")
    Call<SearchResponse> searchMovies(@Field("title") String title, @Field("limit") int limit);

    //최근 본 영화 디비에 저장
    @FormUrlEncoded
    @POST("retrofit/search/searchedMovie.php")
    Call<SearchResponse> searchedMovie(@Field("id") int id);

    //최근 본 영화 가져오기
    @GET("retrofit/search/recentlySearchedList.php")
    Call<SearchResponse> searchedList();

    //영화관 리스트 가져오기
    @FormUrlEncoded
    @POST("retrofit/map/listTheaters.php")
    Call<TheaterResponse> listTheaters(@Field("currentLat") double lat, @Field("currentLng") double lng, @Field("currentDistance") double distance);

    //영화관 상세 정보 가져오기
    @FormUrlEncoded
    @POST("retrofit/map/theaterDetail.php")
    Call<TheaterDetailResponse> theaterDetail(@Field("theaterName") String theaterName, @Field("theaterAddress") String theaterAddress);

    //회원 정보 검색
    @FormUrlEncoded
    @POST("retrofit/map/searchUser.php")
    Call<SearchResponse> searchUser(@Field("text") String text);

    //회원 페이지 정보 가져오기
    @FormUrlEncoded
    @POST("retrofit/chat/getUserPageInfo.php")
    Call<UserInfo> getUserPageInfo(@Field("userEmail") String userEmail);

    // 방 있는지 확인
    @FormUrlEncoded
    @POST("retrofit/chat/CheckRoomExistOrNot.php")
    Call<Room> CheckRoomExistOrNot(@Field("myEmail") String myEmail, @Field("yourEmail") String yourEmail);

    // 방 있는지 확인 후 저장된 채팅 메세지들 불러오기
    @FormUrlEncoded
    @POST("retrofit/chat/CheckRoomNoExistOrNot.php")
    Call<Room> CheckRoomNoExistOrNot(@Field("roomNo") int roomNo);

    // 방 있는지 확인 후 채팅 메세지 데이터베이스에 저장
    @FormUrlEncoded
    @POST("retrofit/chat/sendMessage.php")
    Call<Room> sendMessage(@Field("mode") int mode, @Field("roomNo") int roomNo, @Field("myEmail") String myEmail, @Field("yourEmail") String yourEmail, @Field("message") String message, @Field("chatDate") String chatDate, @Field("chatTime") String chatTime);

    // 채팅방 메세지 불러오기
    @FormUrlEncoded
    @POST("retrofit/chat/getChatRoomMessage.php")
    Call<MessageContentResponse> getChatRoomMessage(@Field("roomNo") int roomNo);

    // 채팅방 목록 불러오기
    @FormUrlEncoded
    @POST("retrofit/chat/getChatRoomList.php")
    Call<MessageListResponse> getChatRoomList(@Field("myEmail") String myEmail);

    // 채팅 이미지 전송
    @Multipart
    @POST("retrofit/chat/uploadChatImages.php")
    Call<ResponseResult>uploadChatImages(
            @Part List<MultipartBody.Part> images,
            @Part("mode") Integer mode,
            @Part("roomNo") Integer roomNo,
            @Part("myEmail") String myEmail,
            @Part("yourEmail") String yourEmail,
            @Part("date") String date,
            @Part("time") String time
    );

    // 채팅 단일 이미지 전송
    @FormUrlEncoded
    @POST("retrofit/chat/sendChatImage.php")
    Call<ResponseResult> sendChatImage(@Field("mode") int mode, @Field("roomNo") int roomNo, @Field("image") String image, @Field("myEmail") String myEmail, @Field("yourEmail") String yourEmail, @Field("date") String date, @Field("time") String time);

    // 채팅방 목록 불러오기
    @FormUrlEncoded
    @POST("retrofit/chat/CheckMessageCount.php")
    Call<MessageContent> checkMessageCount(@Field("myEmail") String myEmail, @Field("roomNo") int roomNo);

    // 채팅방 목록 불러오기
    @FormUrlEncoded
    @POST("retrofit/chat/countNewMessage.php")
    Call<MessageContent> countNewMessage(@Field("myEmail") String myEmail, @Field("roomNo") int roomNo);


}