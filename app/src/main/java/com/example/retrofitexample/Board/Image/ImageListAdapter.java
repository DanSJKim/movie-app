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

public class ImageListAdapter extends RecyclerView.Adapter<ImageListAdapter.ViewHolder> {

    //All methods in this adapter are required for a bare minimum recyclerview adapter
    private ArrayList<BoardImageItem> itemList;
    Context ctx;

    // Constructor of the class
    public ImageListAdapter(Context ctx, ArrayList<BoardImageItem> itemList) {
        this.itemList = itemList;
        this.ctx = ctx;
    }

    public interface BoardUpdateRecyclerViewClickListener{//클릭이벤트 만들어주기. 다른 클래스에 implement시킨다
        void onImageAddClick(int position);//내가 누른 아이템의 포지션을 외부에서 알 수 있도록 정의
        void onImageDeleteClick(int position);
        void onItemClicked(int position);
        void onItemLongClicked(int position);
    }


    private ImageListAdapter.BoardUpdateRecyclerViewClickListener mListener;//위의 인터페이스를 내부에서 하나 들고있어야 한다

    public void setOnClickListener(ImageListAdapter.BoardUpdateRecyclerViewClickListener listener){//외부에서 메소드를 지정할 수 있도록 이런 메소드를 준비
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
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.board_update_item, parent, false);
        ViewHolder myViewHolder = new ViewHolder(view);
        return myViewHolder;
    }

    // load data in each row element
    @SuppressLint("RestrictedApi")//holder.fabDelete.setVisibility(View.GONE); 추가하면서 생김
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int listPosition) {
//        img = holder.img;


        holder.img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemClicked(listPosition);

                itemList.get(listPosition).getImg_path();
//                Log.d("adapter! ", "onClick: " + itemList.get(listPosition).getImg_path());
            }
        });

        holder.img.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mListener.onItemLongClicked(listPosition);

                itemList.get(listPosition).getImg_path();
                Log.d("adapter! ", "onClick: " + itemList.get(listPosition).getImg_path());

                return true;//true = 롱클릭시 다음 이벤트 진행하지 않음.
            }
        });

        //리사이클러뷰 마지막 포지션이면 이미지 추가버튼을 출력한다.
        if(itemList.get(listPosition).getImg_path().equals("addbutton")){

            int drawableIdentifier = ctx.getResources().getIdentifier("add_image", "drawable", ctx.getPackageName());
            GlideApp.with(holder.itemView).load(drawableIdentifier)
                    .override(300, 400)
                    .into(holder.img);

            holder.img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("img", "onClick: clicked! " + listPosition);
                    mListener.onImageAddClick(listPosition);
                }
            });

            holder.fabDelete.setVisibility(View.GONE);
        }else {

            //게시물 이미지
            //getImg_path가 uri이미지면 load()메소드에 서버 주소를 붙이지 않는다.
            if(itemList.get(listPosition).getImg_path().contains("content://")){
                GlideApp.with(holder.itemView).load(itemList.get(listPosition).getImg_path())
                        .override(300, 400)
                        .into(holder.img);
            }else {
                GlideApp.with(holder.itemView).load("http://13.209.49.7/movieApp" + itemList.get(listPosition).getImg_path())
                        .override(300, 400)
                        .into(holder.img);
            }

            holder.fabDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onImageDeleteClick(listPosition);
                }
            });
        }

    }

    // Static inner class to initialize the views of rows
    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView img;
        public FloatingActionButton fabDelete;

        public ViewHolder(View itemView) {
            super(itemView);
//            itemView.setOnClickListener(this);
            img = (ImageView) itemView.findViewById(R.id.ivBoardUpdateImage);
            fabDelete = (FloatingActionButton) itemView.findViewById(R.id.fabBoardUpdateDelete);
        }
        @Override
        public void onClick(View view) {
            Log.d("onClick!", "onClick: ");
        }
    }
}
