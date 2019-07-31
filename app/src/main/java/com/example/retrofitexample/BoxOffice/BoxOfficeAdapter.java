package com.example.retrofitexample.BoxOffice;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.retrofitexample.BoxOffice.Item.DailyBoxOfficeList;
import com.example.retrofitexample.R;

import java.util.ArrayList;

public class BoxOfficeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String TAG = "BoxOfficeAdapter : ";

    //adapter에 들어갈 아이템
    private ArrayList<DailyBoxOfficeList> dailyBoxOfficeList;

    public BoxOfficeAdapter(ArrayList<DailyBoxOfficeList> dailyBoxOfficeList) {
        this.dailyBoxOfficeList = dailyBoxOfficeList;
    }


    public interface BoxOfficeRecyclerViewClickListener {//클릭이벤트 만들어주기. 다른 클래스에 implement시킨다
        void onReplyClicked(int position);//내가 누른 아이템의 포지션을 외부에서 알 수 있도록 정의
    }


    private BoxOfficeAdapter.BoxOfficeRecyclerViewClickListener mListener;//위의 인터페이스를 내부에서 하나 들고있어야 한다

    public void setOnClickListener(BoxOfficeAdapter.BoxOfficeRecyclerViewClickListener listener) {//외부에서 메소드를 지정할 수 있도록 이런 메소드를 준비
        Log.d(TAG, "setOnClickListener");

        mListener = listener;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder");

        View view;

        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.boxoffice_item, parent, false);
        BoxOfficeHolder viewHolder = new BoxOfficeHolder(view);
        return viewHolder;

    }


    //각 Item form에 맞게 초기 세팅
    public class BoxOfficeHolder extends RecyclerView.ViewHolder {
        TextView movieName;
        TextView movieRank;

        TextView dailyAud;
        TextView accAud;

        TextView openDt;

        public BoxOfficeHolder(View itemView) {
            super(itemView);
            Log.d(TAG, "FirstViewBoardHolder");

            movieName = (TextView) itemView.findViewById(R.id.tvBoxOfficeMovieName);
            movieRank = (TextView) itemView.findViewById(R.id.tvBoxOfficeMovieRank);
            dailyAud = (TextView) itemView.findViewById(R.id.tvdailyAudCnt);
            accAud = (TextView) itemView.findViewById(R.id.tvaccAudCnt);
            openDt = (TextView) itemView.findViewById(R.id.tvOpenDt);

        }
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder");
        Log.d(TAG, "onBindViewHolder: commentitem.get(position).parent: " + dailyBoxOfficeList.get(position).getMovieNm());

        ((BoxOfficeHolder)holder).movieName.setText(dailyBoxOfficeList.get(position).getMovieNm());
        ((BoxOfficeHolder)holder).movieRank.setText(dailyBoxOfficeList.get(position).getRank());
        ((BoxOfficeHolder)holder).dailyAud.setText(dailyBoxOfficeList.get(position).getAudiCnt());
        ((BoxOfficeHolder)holder).accAud.setText(dailyBoxOfficeList.get(position).getAudiAcc());
        ((BoxOfficeHolder)holder).openDt.setText(dailyBoxOfficeList.get(position).getOpenDt());

    }


    @Override
    public int getItemCount() {

        return dailyBoxOfficeList.size();
    }

}