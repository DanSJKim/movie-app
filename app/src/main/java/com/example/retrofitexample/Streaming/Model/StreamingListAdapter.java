package com.example.retrofitexample.Streaming.Model;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.retrofitexample.GlideApp;
import com.example.retrofitexample.R;

import java.util.ArrayList;

public class StreamingListAdapter extends RecyclerView.Adapter<StreamingListAdapter.ViewHolder>{
    private ArrayList<StreamingListContent> mArrayList;

    public static final String TAG = "StreamingListAdapter : ";

    public interface StreamingListContentRecyclerviewClickListener{//클릭이벤트 만들어주기. 다른 클래스에 implement시킨다
        void onItemClicked(int position);//내가 누른 아이템의 포지션을 외부에서 알 수 있도록 정의
    }

    private StreamingListAdapter.StreamingListContentRecyclerviewClickListener mListener;//위의 인터페이스를 내부에서 하나 들고있어야 한다

    public void setOnClickListener(StreamingListAdapter.StreamingListContentRecyclerviewClickListener listener){//외부에서 메소드를 지정할 수 있도록 이런 메소드를 준비
        Log.d(TAG, "setOnClickListener mListener: " + mListener);

        mListener = listener;
    }

    public StreamingListAdapter(ArrayList<StreamingListContent> arrayList) {
        Log.d(TAG, "StreamingListAdapter: ");
        mArrayList = arrayList;
    }

    @Override
    public StreamingListAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.streaming_list_item, viewGroup, false);
        Log.d(TAG, "onCreateViewHolder: ");
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StreamingListAdapter.ViewHolder viewHolder, int i) {
        Log.d(TAG, "onBindViewHolder: ");

        //썸네일
        GlideApp.with(viewHolder.itemView).load("https://cloud.wowza.com/proxy/thumbnail2/?target=13.125.122.111&app=app-fc5b&stream=fa06bb8a&fitMode=fitwidth&width=360&width=324&r=0.37134894028887855")
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .override(300, 400)
                .into(viewHolder.ivThumbnail);

        viewHolder.tvRoomName.setText(mArrayList.get(i).getRoomName());
        viewHolder.tvRoomHost.setText(mArrayList.get(i).getRoomHost());

        viewHolder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: 스트리밍 방 클릭");
                mListener.onItemClicked(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: ");
        return mArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private LinearLayout layout;
        private ImageView ivThumbnail;
        private TextView tvRoomName; // 방제
        private TextView tvRoomHost; // 방장

        public ViewHolder(View view) {
            super(view);
            Log.d(TAG, "ViewHolder: ");

            ivThumbnail = (ImageView) view.findViewById(R.id.ivStreamingThumbnail);
            tvRoomName = (TextView)view.findViewById(R.id.tvStreamingRoomName);
            tvRoomHost = (TextView)view.findViewById(R.id.tvStreamingRoomHost);
            layout = (LinearLayout)view.findViewById(R.id.streamingListLayout);

        }
    }

}