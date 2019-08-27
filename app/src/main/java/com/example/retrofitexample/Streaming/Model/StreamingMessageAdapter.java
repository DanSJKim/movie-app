package com.example.retrofitexample.Streaming.Model;

import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
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

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.retrofitexample.Streaming.PlayerActivity.tokenBalance;

/**
 * 스트리밍 중에 주고 받는 채팅 메세지 리사이클러뷰 어댑터
 */
public class StreamingMessageAdapter extends RecyclerView.Adapter<StreamingMessageAdapter.ViewHolder> {
    private ArrayList<StreamingMessage> mArrayList;

    public static final String TAG = "StreamingMessageAdap : ";

    public interface StreamingMessageRecyclerviewClickListener {//클릭이벤트 만들어주기. 다른 클래스에 implement시킨다

        void onItemClicked(int position);//내가 누른 아이템의 포지션을 외부에서 알 수 있도록 정의
    }

    private StreamingMessageAdapter.StreamingMessageRecyclerviewClickListener mListener;//위의 인터페이스를 내부에서 하나 들고있어야 한다

    public void setOnClickListener(StreamingMessageAdapter.StreamingMessageRecyclerviewClickListener listener) {//외부에서 메소드를 지정할 수 있도록 이런 메소드를 준비
        Log.d(TAG, "setOnClickListener: ");

        mListener = listener;
    }

    public StreamingMessageAdapter(ArrayList<StreamingMessage> arrayList) {
        Log.d(TAG, "StreamingMessageAdapter: ");
        mArrayList = arrayList;
    }

    @Override
    public StreamingMessageAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.streaming_chat_item, viewGroup, false);
        Log.d(TAG, "onCreateViewHolder: ");
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StreamingMessageAdapter.ViewHolder viewHolder, int i) {
        Log.d(TAG, "onBindViewHolder: ");

        // 프로필 이미지
        GlideApp.with(viewHolder.itemView).load(mArrayList.get(i).getProfile())
                .override(300, 400)
                .into(viewHolder.civProfile);

        if(mArrayList.get(i).getProfile().equals("Server")){
            Log.d(TAG, "onBindViewHolder: 메세지 발신자가 Server일 경우");
            viewHolder.civProfile.setVisibility(View.GONE); // 프사 가리기
            viewHolder.tvStreamingChatName.setVisibility(View.GONE); // 이름 가리기
            viewHolder.ivBalloon.setVisibility(View.GONE); // 풍선 가리기
            viewHolder.tvTokenCount.setVisibility(View.GONE); // 토큰 선물 개수 가리기
            viewHolder.tvStreamingChatMessage.setText(mArrayList.get(i).getMessage());
            viewHolder.tvStreamingChatMessage.setTextColor(Color.parseColor("#FFFF00"));


        }else if(mArrayList.get(i).getProfile().equals("Token")){
            Log.d(TAG, "onBindViewHolder: 메세지 발신자가 토큰 선물일 경우");
            viewHolder.civProfile.setVisibility(View.GONE); // 프사 가리기
            viewHolder.tvStreamingChatName.setVisibility(View.GONE); // 이름 가리기
            viewHolder.ivBalloon.setVisibility(View.VISIBLE); // 풍선 표시
            viewHolder.tvTokenCount.setVisibility(View.VISIBLE); // 토큰 선물 개수 표시
            viewHolder.tvStreamingChatMessage.setVisibility(View.VISIBLE);
            viewHolder.tvStreamingChatMessage.setText(mArrayList.get(i).getMessage());
            viewHolder.tvStreamingChatMessage.setTextColor(Color.parseColor("#FFFF00"));

            String[] array = mArrayList.get(i).getMessage().split(" "); // 토큰 선물 메세지를 Split 해서 토큰 선물 개수를 가져 온다.
            viewHolder.tvTokenCount.setText(array[3]); // 토큰 선물 개수 표시

            AnimationDrawable anim = (AnimationDrawable) viewHolder.ivBalloon.getBackground();
            anim.start();

        }else{
            Log.d(TAG, "onBindViewHolder: 일반 채팅 메세지일 경우");
            viewHolder.civProfile.setVisibility(View.VISIBLE); // 프사 표시
            viewHolder.tvStreamingChatName.setVisibility(View.VISIBLE); // 이름 표시
            viewHolder.ivBalloon.setVisibility(View.GONE); // 풍선 가리기
            viewHolder.tvTokenCount.setVisibility(View.GONE); // 토큰 선물개수 가리기
            viewHolder.tvStreamingChatName.setText(mArrayList.get(i).getSenderEmail());
            viewHolder.tvStreamingChatMessage.setText(mArrayList.get(i).getMessage());
            viewHolder.tvStreamingChatMessage.setTextColor(Color.parseColor("#FFFFFF"));


        }



    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: " + mArrayList.size());
        return mArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView civProfile;
        private TextView tvStreamingChatName;
        private TextView tvStreamingChatMessage;
        private ImageView ivBalloon;
        private AnimationDrawable animationDrawable;
        private TextView tvTokenCount;

        public ViewHolder(View view) {
            super(view);
            Log.d(TAG, "ViewHolder: ");

            //ivPosterimg = (ImageView) view.findViewById(R.id.ivSearchedMoviePoster);
            civProfile = (CircleImageView) view.findViewById(R.id.civStreamingChatProfile);
            tvStreamingChatName = (TextView) view.findViewById(R.id.tvStreamingChatName);
            tvStreamingChatMessage = (TextView) view.findViewById(R.id.tvStreamingChatMessage);
            ivBalloon = (ImageView) view.findViewById(R.id.ivStreamingBalloon);
            tvTokenCount = (TextView) view.findViewById(R.id.tvTokenCount);

        }
    }

}

