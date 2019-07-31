package com.example.retrofitexample.Board;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.retrofitexample.GlideApp;
import com.example.retrofitexample.R;

import java.util.ArrayList;

//리사이클러뷰의 기본 틀은 getItemCount, onCreateViewHolder, MyViewHolder, onBindViewholder 순서
public class LikeListAdapter extends RecyclerView.Adapter<LikeListAdapter.ViewLikeHolder> {

    public static final String TAG = "LikeListAdapter : ";

    Context context;
    private ArrayList<BoardItem> bt; //아이템

    public LikeListAdapter(ArrayList<BoardItem> bt) {
        this.bt = bt;
    }


    public interface LikeListRecyclerViewClickListener {//클릭이벤트 만들어주기. 다른 클래스에 implement시킨다

        void onItemClicked(int position);//내가 누른 아이템의 포지션을 외부에서 알 수 있도록 정의
    }


    private LikeListRecyclerViewClickListener mListener;//위의 인터페이스를 내부에서 하나 들고있어야 한다

    public void setOnClickListener(LikeListRecyclerViewClickListener listener) {//외부에서 메소드를 지정할 수 있도록 이런 메소드를 준비
        Log.d(TAG, "setOnClickListener");

        mListener = listener;
    }


    @Override
    public ViewLikeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.like_list_item, parent, false);
        ViewLikeHolder boardHolder = new ViewLikeHolder(view);
        Log.d(TAG, "onCreateViewHolder");

        return boardHolder;
    }


    //각 Item form에 맞게 초기 세팅
    public class ViewLikeHolder extends RecyclerView.ViewHolder {

        TextView board_writer;
        ImageView board_profile;

        public ViewLikeHolder(View itemView) {
            super(itemView);
            Log.d(TAG, "ViewLikeHolder");

            board_writer = (TextView) itemView.findViewById(R.id.like_list_email);
            board_profile = (ImageView) itemView.findViewById(R.id.like_list_profile);
        }
    }

    @Override
    public void onBindViewHolder(ViewLikeHolder holder, int position) {

        Log.d(TAG, "onBindViewHolder");

        final int pos = position;
        //final BoardItem data = item.get(position);
        holder.board_writer.setText(bt.get(position).getWriter());
        //게시물 이미지
        GlideApp.with(holder.itemView).load(bt.get(position).getImage())
                .override(300,400)
                .into(holder.board_profile);

    }

    @Override
    public int getItemCount() {
        //Log.d(TAG, "getItemCount: " + bt.size());
        return bt.size();
    }
}
