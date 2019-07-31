package com.example.retrofitexample.Board.Image;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.retrofitexample.GlideApp;
import com.example.retrofitexample.R;

import java.util.ArrayList;

public class BoardImageListAdapter extends RecyclerView.Adapter<BoardImageListAdapter.ViewHolder> {

//All methods in this adapter are required for a bare minimum recyclerview adapter
private ArrayList<BoardImageItem> itemList;
        Context ctx;

// Constructor of the class
public BoardImageListAdapter(Context ctx, ArrayList<BoardImageItem> itemList) {
        this.ctx = ctx;
        this.itemList = itemList;
        }

public interface BoardUpdateRecyclerViewClickListener{//클릭이벤트 만들어주기. 다른 클래스에 implement시킨다

}


    private BoardImageListAdapter.BoardUpdateRecyclerViewClickListener mListener;//위의 인터페이스를 내부에서 하나 들고있어야 한다

    public void setOnClickListener(BoardImageListAdapter.BoardUpdateRecyclerViewClickListener listener){//외부에서 메소드를 지정할 수 있도록 이런 메소드를 준비
        Log.d("ImageListAdapter", "setOnClickListener");

        mListener = listener;
    }

    // get the size of the list
    @Override
    public int getItemCount() {
        return itemList == null ? 0 : itemList.size();
    }


    // specify the row layout file and click for each row
    @Override
    public BoardImageListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        BoardImageListAdapter.ViewHolder myViewHolder = new BoardImageListAdapter.ViewHolder(view);
        return myViewHolder;
    }

    // load data in each row element
    @SuppressLint("RestrictedApi")//holder.fabDelete.setVisibility(View.GONE); 추가하면서 생김
    @Override
    public void onBindViewHolder(final BoardImageListAdapter.ViewHolder holder, final int listPosition) {
//        img = holder.img;

            //게시물 이미지
                GlideApp.with(holder.itemView).load("http://13.209.49.7/movieApp" + itemList.get(listPosition).getImg_path())
                        .override(300, 400)
                        .into(holder.img);
    }

    // Static inner class to initialize the views of rows
    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView img;

        public ViewHolder(View itemView) {
            super(itemView);
    //            itemView.setOnClickListener(this);
            img = (ImageView) itemView.findViewById(R.id.ivBoardListImage);
        }
        @Override
        public void onClick(View view) {
            Log.d("onClick!", "onClick: ");
        }
    }
}
