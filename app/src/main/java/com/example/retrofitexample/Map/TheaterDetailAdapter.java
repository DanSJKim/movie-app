package com.example.retrofitexample.Map;

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.retrofitexample.BoxOffice.BoxOfficeAdapter;
import com.example.retrofitexample.BoxOffice.Item.DailyBoxOfficeList;
import com.example.retrofitexample.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class TheaterDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    public static final String TAG = "TheaterDetailAdapter : ";

    //adapter에 들어갈 아이템
    private ArrayList<TheaterDetail> theaterDetails;
    private String name[]; //이 배열의 개수만큼 리사이클러뷰를 카운트한다.

    private String currentDateandTime;

    String[] splitedArray;

    int cnt; // CGV와 그 외 영화관 인덱스 시작 지점을 다르게 설정한다.



    public TheaterDetailAdapter(ArrayList<TheaterDetail> theaterDetails, String name[]) {
        this.theaterDetails = theaterDetails;
        this.name = name;
    }


    public interface TheaterDetailRecyclerViewClickListener {//클릭이벤트 만들어주기. 다른 클래스에 implement시킨다
        void onReplyClicked(int position);//내가 누른 아이템의 포지션을 외부에서 알 수 있도록 정의
    }


    private TheaterDetailAdapter.TheaterDetailRecyclerViewClickListener mListener;//위의 인터페이스를 내부에서 하나 들고있어야 한다

    public void setOnClickListener(TheaterDetailAdapter.TheaterDetailRecyclerViewClickListener listener) {//외부에서 메소드를 지정할 수 있도록 이런 메소드를 준비
        Log.d(TAG, "setOnClickListener");

        mListener = listener;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder");

        View view;

        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.theater_detail_item, parent, false);
        TheaterDetailHolder viewHolder = new TheaterDetailHolder(view);
        return viewHolder;

    }


    //각 Item form에 맞게 초기 세팅
    public class TheaterDetailHolder extends RecyclerView.ViewHolder {
        TextView movieName;
        TextView movieTime;

        public TheaterDetailHolder(View itemView) {
            super(itemView);
            Log.d(TAG, "FirstViewBoardHolder");

            movieName = (TextView) itemView.findViewById(R.id.tvDetailMovieName);
            movieTime = (TextView) itemView.findViewById(R.id.tvDetailMovieTime);

        }
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder");

        String theaterName = theaterDetails.get(0).getTheaterName();
        String moviename[] = theaterDetails.get(0).getMovieName();
        String movietime[] = theaterDetails.get(0).getMovieTime();
        ((TheaterDetailHolder)holder).movieName.setText(moviename[position]);
        //((TheaterDetailHolder)holder).movieTime.setText(movietime[position]);

        Log.d(TAG, "onBindViewHolder: theaterName: " + theaterName);
        Log.d(TAG, "onBindViewHolder: moviename[]: " + position + " : " + moviename[position]);
        Log.d(TAG, "onBindViewHolder: movietime[]: " + position + " : " + movietime[position]);

        //현재 시간 불러오기
        java.text.SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");
        currentDateandTime = sdf2.format(new Date());
        Log.d(TAG, "onBindViewHolder: current time: " + currentDateandTime);

        //자르기 전 문자열
        Log.d(TAG, "onBindViewHolder: before text: " + movietime[position]);

        //CGV와 그 외 영화관 문자열 형식이 다르다.
        //CGV시간표와 형식을 맞춰주기 위해 시간 사이에 들어가있는 "| "를 삭제해 준다.
        if(theaterName.contains("CGV")){
            splitedArray = movietime[position].split(" ");
        }else{
            String replacedtext = movietime[position].replaceAll("[|] ", "");
            Log.d(TAG, "onBindViewHolder: replacedtext: " + replacedtext);
            // 문자열 시간별로 자르기
            splitedArray = replacedtext.split(" ");
        }




        ((TheaterDetailHolder)holder).movieTime.setText("");
        // 시간별로 비교해서 글자 색상 설정 (지나간 영화 시간은 빨간색으로 바꿔준다.)


        // CGV는 인덱스 1부터, 그 외 영화관은 인덱스 0부터
        if(theaterName.contains("CGV")){
            cnt = 1;
        }else{
            cnt = 0;
        }

        for (int i = cnt; i < splitedArray.length; i++) {

            Log.d(TAG, "onBindViewHolder: length: " + splitedArray.length);
            Log.d(TAG, "onBindViewHolder: splitedArray " + i + " : " + splitedArray[i]);

            // 현재 시간, int로 변환해서 비교하기 위해 : 를 제거해준다.
            String currentstr = currentDateandTime.replaceAll(":", "");
            Log.d(TAG, "onBindViewHolder: currentstr: " + currentstr);

            // 영화 시간, int로 변환해서 비교하기 위해 : 를 제거해준다.
            String mvstr = splitedArray[i].replaceAll(":", "");
            Log.d(TAG, "onBindViewHolder: mvstr: " + mvstr);

            // 시간 크기를 비교하기 위해 String->int로 변환
            int currentTime = Integer.parseInt(currentstr);
            int mvTime = Integer.parseInt(mvstr);

            // 현재 시간이 영화관 시간보다 크면 영화관 시간 글씨 빨간색 (영화 시간이 지났다는 의미)
            if(currentTime > mvTime){
                Log.d(TAG, "onBindViewHolder: is red text");
                Log.d(TAG, "onBindViewHolder: currentTime1: " + currentTime);
                Log.d(TAG, "onBindViewHolder: mvTime1: " + mvTime);

                //글자를 빨간색으로 설정한 후 텍스트뷰에 append해 준다.
                SpannableString sText = new SpannableString(splitedArray[i]);
                sText.setSpan(new ForegroundColorSpan(Color.RED), 0,  5, 0);
                ((TheaterDetailHolder)holder).movieTime.append(sText);

                //마지막 부분에는 짝대기를 삽입하지 않는다.
                if(i == (splitedArray.length-1)){
                }else{
                    ((TheaterDetailHolder)holder).movieTime.append(" | ");
                }

                Log.d(TAG, "onBindViewHolder: 현재 시간 > 영화 시간: sText: " + sText);
                Log.d(TAG, "onBindViewHolder: ((TheaterDetailHolder)holder).movieTime1: " + ((TheaterDetailHolder)holder).movieTime.getText());


            //현재 시간이 영화관 시간보다 작으면 일반 글자색
            }else{
                Log.d(TAG, "onBindViewHolder: is normal text");
                Log.d(TAG, "onBindViewHolder: currentTime2:  " + currentTime);
                Log.d(TAG, "onBindViewHolder: mvTime2: " + mvTime);

                //글자를 텍스트뷰에 append해 준다.
                SpannableString sText = new SpannableString(splitedArray[i]);
                ((TheaterDetailHolder)holder).movieTime.append(sText);

                //마지막 부분에는 짝대기를 삽입하지 않는다.
                if(i == (splitedArray.length-1)){
                }else{
                    ((TheaterDetailHolder)holder).movieTime.append(" | ");
                }

                Log.d(TAG, "onBindViewHolder: 현재 시간 < 영화 시간: sText: " + sText);
                Log.d(TAG, "onBindViewHolder: ((TheaterDetailHolder)holder).movieTime2: " + ((TheaterDetailHolder)holder).movieTime.getText());
            }
        }//for
    }


    @Override
    public int getItemCount() {

        Log.d(TAG, "getItemCount: " + name.length);
        return name.length; // 영화 이름을 담은 배열의 크기만큼 카운트
    }

}
