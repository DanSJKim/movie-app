package com.example.retrofitexample.BoxOffice;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CalendarView;
import android.widget.Toast;

import com.example.retrofitexample.R;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CalendarActivity extends AppCompatActivity {

    private static final String TAG = "CalendarActivity";
    private CalendarView mCalendarView;
    private int to;
    private String currentDateandTime2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        mCalendarView = (CalendarView) findViewById(R.id.cvCalendarView);

        //현재 년월일
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy년 MM월 dd일");
        currentDateandTime2 = sdf2.format(new Date());
        Log.d(TAG, "getBoxOffice: currentdate: " + currentDateandTime2);

        //현재 년월일
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String currentDateandTime = sdf.format(new Date());
        Log.d(TAG, "getBoxOffice: currentdate: " + currentDateandTime);

        //00시가 지나서 날짜가 넘어가면 바로 박스오피스 API가 갱신되지 않아 데이터가 없어서 오류가 뜨기 때문에 전일로 변환해서 불러온다.
        to = Integer.parseInt(currentDateandTime);
        to = to-1;

        mCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView calendarView, int year, int month, int dayOfMonth) {

                month = month+1;

                String month2 = String.valueOf(month);
                String dayofMonth2 = String.valueOf(dayOfMonth);

                //월이 한자리수면 앞에 0을 붙여준다.
                if(month < 10){
                    month2 = "0"+month2;
                }
                //일이 한자리수면 앞에 0을 붙여준다.
                if(dayOfMonth < 10){
                    dayofMonth2 = "0"+dayofMonth2;
                }

                String date = year + month2 + dayofMonth2;
                Log.d(TAG, "onSelectedDayChange: yyyymmdd" + date);


                int dateToInt = Integer.parseInt(date);//선택한 날짜
                //선택한 날짜가 어제날짜보다 크면 intent를 실행하지 않는다.
                if(dateToInt > to){
                    Toast.makeText(CalendarActivity.this, currentDateandTime2 + " 이전 날짜로 조회하세요.", Toast.LENGTH_SHORT).show();
                }else if(dateToInt <= to){
                    Intent intent = new Intent(CalendarActivity.this, ProfileActivity.class);
                    intent.putExtra("date", date);
                    startActivity(intent);
                }


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
