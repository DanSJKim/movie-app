package com.example.retrofitexample.Board.Image.ViewPager;

import android.content.Context;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.retrofitexample.Board.BoardItem;
import com.example.retrofitexample.Board.Image.BoardImageItem;
import com.example.retrofitexample.GlideApp;
import com.example.retrofitexample.R;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

//게시판 업데이트 이미지로 사용하다가 리사이클러뷰로 바꾸면서 사용하지 않음.
public class BoardImageListPagerAdapter extends PagerAdapter {
    Context context;
    private ArrayList<BoardImageItem> imgList;
    LayoutInflater layoutInflater;


    public BoardImageListPagerAdapter(Context context, ArrayList<BoardImageItem> imgList) {
        this.context = context;
        this.imgList = imgList;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Log.d("11", "BoardImageListPagerAdapter: imgList: " + imgList);
    }

    @Override
    public int getCount() {
        Log.d("size", "getCount: imgList.size() " + imgList.size());
        return imgList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }

    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
        final View itemView = layoutInflater.inflate(R.layout.item, container, false);

        final ImageView imageView = (ImageView) itemView.findViewById(R.id.ivBoardListImage);

        //이미지
//        GlideApp.with(BoardImageListPagerAdapter.this.context).load(imgList.get(position))
//                .override(300,400)
//                .into(imageView);

        Log.d("123", "instantiateItem: imageitems" + imgList.get(position).getImg_path());


        //게시물 이미지

            Log.d("1", "instantiateItem: false");
            GlideApp.with(BoardImageListPagerAdapter.this.context).load("http://13.209.49.7/movieApp" + imgList.get(position).getImg_path())
                    .override(300,400)
                    .into(imageView);

        container.addView(itemView);

        //listening to image click
        imageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Toast.makeText(context, "you clicked image " + (position + 1), Toast.LENGTH_LONG).show();
            }
        });

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }
}