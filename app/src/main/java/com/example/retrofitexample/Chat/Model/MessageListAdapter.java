package com.example.retrofitexample.Chat.Model;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.retrofitexample.GlideApp;
import com.example.retrofitexample.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.MessageListViewHolder> {

    private ArrayList<MessageListContent> mList;

    public class MessageListViewHolder extends RecyclerView.ViewHolder {
        protected CircleImageView ivImage;
        protected TextView tvTitle;
        protected TextView tvContent;
        protected TextView tvDate;
        protected LinearLayout linearLayout;
        protected TextView tvChatListMessageCount;


        public MessageListViewHolder(View view) {
            super(view);
            this.ivImage = (CircleImageView) view.findViewById(R.id.ivChatListImage);
            this.tvTitle = (TextView) view.findViewById(R.id.tvChatListTitle);
            this.tvContent = (TextView) view.findViewById(R.id.tvChatListContent);
            this.tvDate = (TextView) view.findViewById(R.id.tvChatListDate);
            this.linearLayout = (LinearLayout) view.findViewById(R.id.chat_list_item_linearlayout);
            this.tvChatListMessageCount = (TextView) view.findViewById(R.id.tvChatListMessageCount);
        }

    }


    public MessageListAdapter(ArrayList<MessageListContent> list) {
        this.mList = list;
    }

    public interface MessageListRecyclerViewClickListener{//클릭이벤트 만들어주기. 다른 클래스에 implement시킨다
        void onChatListClicked(int position);//내가 누른 아이템의 포지션을 외부에서 알 수 있도록 정의
    }


    private MessageListRecyclerViewClickListener mListener;//위의 인터페이스를 내부에서 하나 들고있어야 한다

    public void setOnClickListener(MessageListRecyclerViewClickListener listener){//외부에서 메소드를 지정할 수 있도록 이런 메소드를 준비
        Log.d("messagelistrecycler ", "setOnClickListener");

        mListener = listener;
    }



    // RecyclerView에 새로운 데이터를 보여주기 위해 필요한 ViewHolder를 생성해야 할 때 호출됩니다.
    @Override
    public MessageListViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_list, null);
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.chat_list_item, viewGroup, false);

        MessageListViewHolder viewHolder = new MessageListViewHolder(view);

        return viewHolder;
    }


    // Adapter의 특정 위치(position)에 있는 데이터를 보여줘야 할때 호출됩니다.
    @Override
    public void onBindViewHolder(@NonNull final MessageListViewHolder viewholder, int position) {

        final int pos = position;

        GlideApp.with(viewholder.itemView).load(mList.get(position).getImg_path())
                .override(300,400)
                .into(viewholder.ivImage);

        viewholder.tvTitle.setText(mList.get(position).getTitle()); // 유저 이메일
        viewholder.tvContent.setText(mList.get(position).getContent()); // 마지막 채팅 내용
        viewholder.tvDate.setText(mList.get(position).getChatTime()); // 채팅 시간

        // 안 읽은 메세지가 0이면 메세지 카운트 뷰를 표시하지 않는다.
        if(mList.get(position).getCount() == 0){
            viewholder.tvChatListMessageCount.setVisibility(View.GONE);
        }else{
            viewholder.tvChatListMessageCount.setVisibility(View.VISIBLE);
            viewholder.tvChatListMessageCount.setText(String.valueOf(mList.get(position).getCount())); // 안 읽은 메세지
        }


        viewholder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewholder.linearLayout.setBackgroundColor(Color.LTGRAY);
                mListener.onChatListClicked(pos);
            }
        });

    }

    @Override
    public int getItemCount() {
        return (null != mList ? mList.size() : 0);
    }

}