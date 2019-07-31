package com.example.retrofitexample.Board.Image;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
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
import java.util.List;

public class BoardImageUploadAdapter extends RecyclerView.Adapter<BoardImageUploadAdapter.ViewHolder> {

//All methods in this adapter are required for a bare minimum recyclerview adapter
        Context ctx;

    private List<Uri> itemList;

// Constructor of the class
public BoardImageUploadAdapter(List<Uri> itemList) {
        //this.ctx = ctx;
        this.itemList = itemList;
        }

public interface BoardImageUploadRecyclerViewClickListener{//클릭이벤트 만들어주기. 다른 클래스에 implement시킨다
    void onItemClicked(int position);
    void onDeleteClicked(int position);
}


    private BoardImageUploadAdapter.BoardImageUploadRecyclerViewClickListener mListener;//위의 인터페이스를 내부에서 하나 들고있어야 한다

    public void setOnClickListener(BoardImageUploadAdapter.BoardImageUploadRecyclerViewClickListener listener){//외부에서 메소드를 지정할 수 있도록 이런 메소드를 준비
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
    public BoardImageUploadAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.board_upload_item, parent, false);
        BoardImageUploadAdapter.ViewHolder myViewHolder = new BoardImageUploadAdapter.ViewHolder(view);
        return myViewHolder;
    }

    // load data in each row element
    @SuppressLint("RestrictedApi")//holder.fabDelete.setVisibility(View.GONE); 추가하면서 생김
    @Override
    public void onBindViewHolder(final BoardImageUploadAdapter.ViewHolder holder, final int listPosition) {
//        img = holder.img;



        //게시물 이미지
        GlideApp.with(holder.itemView).load(itemList.get(listPosition))
                .override(300, 400)
                .into(holder.img);

        holder.img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemClicked(listPosition);
            }
        });

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onDeleteClicked(listPosition);
            }
        });
    }

// Static inner class to initialize the views of rows
static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public ImageView img;
    public FloatingActionButton delete;

    public ViewHolder(View itemView) {
        super(itemView);

        img = (ImageView) itemView.findViewById(R.id.ivBoardUploadImage);
        delete = (FloatingActionButton) itemView.findViewById(R.id.fabBoardUploadDelete);

    }
    @Override
    public void onClick(View view) {
        Log.d("onClick!", "onClick: ");
    }
}
}