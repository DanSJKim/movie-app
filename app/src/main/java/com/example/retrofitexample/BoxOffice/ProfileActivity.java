package com.example.retrofitexample.BoxOffice;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.retrofitexample.Board.BoardActivity;
import com.example.retrofitexample.Chat.ChatActivity;
import com.example.retrofitexample.Chat.ChatService;
import com.example.retrofitexample.GlideApp;
import com.example.retrofitexample.LoginRegister.MainActivity;
import com.example.retrofitexample.LoginRegister.SharedPref;
import com.example.retrofitexample.BoxOffice.Item.BoxOfficeResult;
import com.example.retrofitexample.BoxOffice.Item.DailyBoxOfficeList;
import com.example.retrofitexample.BoxOffice.Item.MovieResponse;
import com.example.retrofitexample.Map.MapActivity;
import com.example.retrofitexample.MovieSearch.MovieSearchActivity;
import com.example.retrofitexample.MyPage.MyPageActivity;
import com.example.retrofitexample.MyPage.MyPage_Img;
import com.example.retrofitexample.R;
import com.example.retrofitexample.Retrofit.Api;
import com.example.retrofitexample.Retrofit.ApiClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.retrofitexample.BootReceiver.isService;
import static com.example.retrofitexample.Chat.ChatService.currentRoomNo;

//우리는 프로파일 액티비티를 런처 (launcher)로 만들고 있습니다. 애플리케이션이 시작되면 프로파일 액티비티가 시작되고 사용자가 로그인하지 않은 SharedPreferences를 참조하여 로그인했는지 확인합니다. 로그인 상태가 아닌 MainActivity.class를 시작합니다.
//처음 실행되는 클래스 인 ProfileActivity는 먼저 사용자가 로그인했는지 여부를 확인합니다. 그렇지 않은 경우 로그인 활동이 시작됩니다. 다른 사용자가 사용자 이름에 로그인하면 로그 아웃 버튼이 표시됩니다.
public class ProfileActivity extends AppCompatActivity {

    public static final String TAG = "ProfileActivity : ";

    ImageView ivBoard;//게시판 탭
    ImageView ivSearch;//검색 탭
    ImageView ivMap;//지도 탭
    ImageView ivChat;//채팅 탭

    TextView boxOfficeDate;//상단 박스오피스 날짜

    //네비게이션 드로어 헤더 뷰
    ImageView navMyPage;
    TextView navUserEmail;
    TextView navUserName;
    ImageView navProfileImage;

    Toolbar myToolbar;

    private RecyclerView boxofficeRecyclerview;
    private BoxOfficeAdapter boxofficeAdapter;

    ImageView CalenderButton;

    //드로어1. 네비게이션 드로어에 필요한 인스턴스 변수를 선언하고 onCreate()함수 내에서 findViewById메소드를 사용해서 변수를 찾는다.
    private DrawerLayout dl;
    private ActionBarDrawerToggle t;
    private NavigationView nv;

    int loggedUserId;
    public static String loggedUseremail;
    public static String loggedUsername;


    ArrayList<BoxOfficeResult> boxofficedata;
    ArrayList<DailyBoxOfficeList> dailyBoxOfficeLists;

    String date;

    //ChatService chatService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);



        currentRoomNo = -1;

        //Toolbar를 생성한다.
        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setTitle("Daily Box Office");
        setSupportActionBar(myToolbar);

        //네비게이션 헤더 선언
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigationView);
        View headerView = navigationView.getHeaderView(0);

        //헤더 내의 뷰 선언
        navUserEmail = (TextView) headerView.findViewById(R.id.textviewNaviEmail);
        navUserName = (TextView) headerView.findViewById(R.id.textviewNaviName);
        navMyPage = (ImageView) headerView.findViewById(R.id.imageviewNaviMyPage);
        navProfileImage = (ImageView) headerView.findViewById(R.id.ivNavImage);

        ivBoard = (ImageView) findViewById(R.id.ivBoard);//게시물 탭
        ivSearch = (ImageView) findViewById(R.id.ivSearch);//검색 탭
        ivMap = (ImageView) findViewById(R.id.ivMap);//지도 탭
        ivChat = (ImageView) findViewById(R.id.ivChat);//채팅 탭
        boxOfficeDate = (TextView) findViewById(R.id.tvBoxOfficeDate);//상단 박스오피스 날짜

        CalenderButton = (ImageView) findViewById(R.id.ivBoxOfficeCalender);

        //드로어1. 네비게이션 드로어에 필요한 인스턴스 변수를 선언하고 onCreate()함수 내에서 findViewById메소드를 사용해서 변수를 찾는다.
        dl = (DrawerLayout)findViewById(R.id.activity_profile);
        t = new ActionBarDrawerToggle(this, dl,R.string.Open, R.string.Close);
        dl.addDrawerListener(t);
        t.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ivBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(ProfileActivity.this, BoardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0,0);
            }
        });

        ivSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(ProfileActivity.this, MovieSearchActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0,0);

            }
        });

        ivMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(ProfileActivity.this, MapActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0,0);
            }
        });

        ivChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(ProfileActivity.this, ChatActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0,0);
            }
        });

        //네비게이션드로어 헤더 마이페이지 버튼 클릭 이벤트
        navMyPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, MyPageActivity.class);
                startActivity(intent);
            }
        });

        //드로어2. nv변수에 setNavigationItemSelectedListener를 추가해서 드로어에서 특정 아이템을 선택할 때 클릭 이벤트를 수신한다.
        nv = (NavigationView)findViewById(R.id.navigationView);
        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch(id)
                {
                    case R.id.logout:
                        finish();
                        SharedPref.getInstance(getApplicationContext()).logout();

                        //서비스 중지
                        Intent intent = new Intent(ProfileActivity.this, ChatService.class);
                        stopService(intent);
                        return true;

                    default:
                        return true;
                }
            }

            //드로어3. 마지막으로 메뉴 리소스 파일에 지정된 항목에 올바르게 응답하는 'onOptionsItemSelected ()'메서드를 아래쪽에 재정의 한다.
        });

    //check if user is logged in
        if (!SharedPref.getInstance(this).isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        //SharedPreferences에 저장된 유저 정보 가져오기
        loggedUsername = SharedPref.getInstance(this).LoggedInUser();
        loggedUseremail = SharedPref.getInstance(this).LoggedInEmail();
        loggedUserId = SharedPref.getInstance(this).LoggedInId();
        navUserName.setText(loggedUsername);
        navUserEmail.setText(loggedUseremail);
        Log.d(TAG, "onCreate: loggedUserId: " + loggedUserId);
        Log.d(TAG, "onCreate: email: " + loggedUseremail);

        getProfileImage();

        CalenderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ProfileActivity.this, CalendarActivity.class);
                startActivity(intent);
            }
        });

        Intent incomingIntent = getIntent();
        date = incomingIntent.getStringExtra("date");

        if(date == null){

            //일일 박스오피스를 출력하기 위해 현재 년월일을 불러온다.
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String currentDateandTime = sdf.format(new Date());
            Log.d(TAG, "getBoxOffice: currentdate: " + currentDateandTime);

            //00시가 지나서 날짜가 넘어가면 바로 박스오피스 API가 갱신되지 않아 데이터가 없어서 오류가 뜨기 때문에 전일로 변환해서 불러온다.
            int to = Integer.parseInt(currentDateandTime);
            to = to-1;
            date = Integer.toString(to);
        }


        Log.d(TAG, "getBoxOffice: date: " + date);
        boxOfficeDate.setText(date);

        getBoxOffice(date);

        Log.d(TAG, "onCreate: isService: " + isService);
        if(!isService){
            Intent intent = new Intent(
                    getApplicationContext(),//현재제어권자
                    ChatService.class); // 이동할 컴포넌트
            startService(intent); // 서비스 시작

            isService = true;
        }
    }


    public void getBoxOffice(String Date){

        //base url
        String BASE_URL = "http://www.kobis.or.kr/";

        Retrofit  retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL) //어떤 서버로 네트워크 통신을 요청할 것인지에 대한 설정
                        .addConverterFactory(GsonConverterFactory.create()) //통신이 완료된 후, 어떤 Converter를 이용하여 데이터를 파싱할 것인지에 대한 설정
                        .build(); //Retrofit.Builder객체에 설정한 정보를 이용하여 실질적으로 Retrofit 객체를 만들어 반환

        Api api = retrofit.create(Api.class);
        Call<MovieResponse> call = api.getBoxOfficeList(Date);

        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {

                boxofficedata = new ArrayList<>(Arrays.asList(response.body().getBoxOfficeResult())); //BoardItem객체들을 받아온 배열을 리스트로 바꿔준다.

                boxofficedata.get(0).getDailyBoxOfficeList();
                Log.d(TAG, "onResponse: boxofficedata.get(0).getWeeklyBoxOfficeList() " + boxofficedata.get(0).getDailyBoxOfficeList());

                dailyBoxOfficeLists = new ArrayList<>(Arrays.asList(boxofficedata.get(0).getDailyBoxOfficeList()));

                dailyBoxOfficeLists.get(0).getMovieNm();
                Log.d(TAG, "onResponse: weeklyBoxOfficeList.get(0).getMovieNm() " + dailyBoxOfficeLists.get(0).getMovieNm());

                boxofficeRecyclerview = (RecyclerView)findViewById(R.id.boxoffice_list);
                boxofficeRecyclerview.setHasFixedSize(true);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                boxofficeRecyclerview.setLayoutManager(layoutManager);

                boxofficeAdapter = new BoxOfficeAdapter(dailyBoxOfficeLists);
                //boxofficeAdapter.setOnClickListener(BoardActivity.this);
                boxofficeRecyclerview.setAdapter(boxofficeAdapter);
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                Log.d(TAG, "onFailure: fail!" + t.getMessage());

            }
        });
    }


    //드로어3. 마지막으로 메뉴 리소스 파일에 지정된 항목에 올바르게 응답하는 'onOptionsItemSelected ()'메서드를 재정의 한다.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(t.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

    private void getProfileImage(){
        //making api call
        Api api = ApiClient.getClient().create(Api.class);
        Call<MyPage_Img> call = api.getProfileImage(loggedUserId);

        call.enqueue(new Callback<MyPage_Img>() {
            @Override
            public void onResponse(Call<MyPage_Img> call, Response<MyPage_Img> response) {

                Log.d(TAG, "onResponse: response.body().getResponse(): " + response.body().getImg_path());

                GlideApp.with(ProfileActivity.this).load(response.body().getImg_path())
                        .override(300,400)
                        .into(navProfileImage);

                SharedPref.getInstance(ProfileActivity.this).storeProfileImage(response.body().getImg_path());
            }

            @Override
            public void onFailure(Call<MyPage_Img> call, Throwable t) {
                Log.d("Error!",t.getMessage());
            }
        });
    }

    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart() called");

        //refresh
        finish();
        startActivity(getIntent());
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called");

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
        finish();
    }


}