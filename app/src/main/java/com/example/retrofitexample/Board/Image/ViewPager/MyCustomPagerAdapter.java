package com.example.retrofitexample.Board.Image.ViewPager;

import android.content.Context;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.retrofitexample.GlideApp;
import com.example.retrofitexample.R;

import java.util.List;

public class MyCustomPagerAdapter extends PagerAdapter {
    Context context;
    private List<Uri> itemList;
    LayoutInflater layoutInflater;


    public MyCustomPagerAdapter(Context context, List<Uri> itemList) {
        this.context = context;
        this.itemList = itemList;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        View itemView = layoutInflater.inflate(R.layout.item, container, false);

        ImageView imageView = (ImageView) itemView.findViewById(R.id.ivBoardListImage);

        //이미지
        GlideApp.with(MyCustomPagerAdapter.this.context).load(itemList.get(position))
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