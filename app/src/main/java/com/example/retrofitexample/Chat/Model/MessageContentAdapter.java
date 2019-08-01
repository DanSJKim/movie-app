package com.example.retrofitexample.Chat.Model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
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

import static com.example.retrofitexample.BoxOffice.ProfileActivity.loggedUseremail;

//리사이클러뷰의 기본 틀은 getItemCount, onCreateViewHolder, MyViewHolder, onBindViewholder 순서
public class MessageContentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "MessageContentAdapter";

    //All methods in this adapter are required for a bare minimum recyclerview adapter
    private ArrayList<MessageContent> itemList;
    Context ctx;

    // Constructor of the class
    public MessageContentAdapter(Context ctx, ArrayList<MessageContent> itemList) {
        this.ctx = ctx;
        this.itemList = itemList;
    }

    public interface MessageContentRecyclerViewClickListener{//클릭이벤트 만들어주기. 다른 클래스에 implement시킨다
        void onImageClicked(int position);//내가 누른 아이템의 포지션을 외부에서 알 수 있도록 정의
    }

    private MessageContentAdapter.MessageContentRecyclerViewClickListener mListener;//위의 인터페이스를 내부에서 하나 들고있어야 한다

    public void setOnClickListener(MessageContentAdapter.MessageContentRecyclerViewClickListener listener){//외부에서 메소드를 지정할 수 있도록 이런 메소드를 준비
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
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;

        switch(viewType){
            case MessageContent.MY_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_my_item, parent, false);
                return new MyViewHolder(view);
            case MessageContent.OTHER_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_other_item, parent, false);
                return new OtherViewHolder(view);
        }


        return null;
    }

    // load data in each row element
    @SuppressLint("RestrictedApi")//holder.fabDelete.setVisibility(View.GONE); 추가하면서 생김
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

        final int pos = position;

        // 내 메세지
        if(itemList.get(position).getSenderEmail().equals(loggedUseremail)){

            Log.d(TAG, "onBindViewHolder: getContent(): " + itemList.get(position).getContent());
            Log.d(TAG, "onBindViewHolder: getMode(): " + itemList.get(position).getMode());

            // 텍스트 메세지일 경우
            if((itemList.get(position).getMode() == 1) || (itemList.get(position).getMode() == 4) || (itemList.get(position).getMode() == 5)){

                ((MyViewHolder)holder).ivMyChatImage.setVisibility(View.GONE);
                ((MyViewHolder)holder).tvMessage.setVisibility(View.VISIBLE);

                ((MyViewHolder)holder).tvMessage.setText(itemList.get(pos).getContent());


            // 이미지 메세지일 경우
            }else if(itemList.get(position).getMode() == 2){

                ((MyViewHolder)holder).tvMessage.setVisibility(View.GONE);
                ((MyViewHolder)holder).ivMyChatImage.setVisibility(View.VISIBLE);

                GlideApp.with(holder.itemView).load("http://13.209.49.7/movieApp"+itemList.get(position).getContent())
                        .override(600,800)
                        .placeholder(R.drawable.ic_photo_black_24dp)
                        .into(((MyViewHolder)holder).ivMyChatImage);

                ((MyViewHolder)holder).ivMyChatImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onImageClicked(pos);
                    }
                });
            }


            int count = itemList.get(position).getCount();
            Log.d(TAG, "onBindViewHolder: itemList.get(position).getCount(): " + count);

            if(count == 0){

                ((MyViewHolder)holder).tvMyChatCount.setVisibility(View.GONE);
            }else{

                ((MyViewHolder)holder).tvMyChatCount.setVisibility(View.VISIBLE);
                ((MyViewHolder)holder).tvMyChatCount.setText(String.valueOf(count));
            }

            ((MyViewHolder)holder).tvDate.setText(itemList.get(pos).getChatTime());

        // 상대방 메세지
        }else{

            ((OtherViewHolder)holder).tvName.setText(itemList.get(pos).getSenderEmail());
            ((OtherViewHolder)holder).tvMessage.setText(itemList.get(pos).getContent());
            ((OtherViewHolder)holder).tvDate.setText(itemList.get(pos).getChatTime());
            GlideApp.with(holder.itemView).load(itemList.get(position).getImg_path())
                    .placeholder(R.drawable.ic_photo_black_24dp)
                    .override(600,800)
                    .into(((OtherViewHolder)holder).civProfile);

            // 텍스트 메세지일 경우
            if(itemList.get(position).getMode() == 1 || (itemList.get(position).getMode() == 4) || (itemList.get(position).getMode() == 5)){

                ((OtherViewHolder)holder).ivOtherChatImage.setVisibility(View.GONE);
                ((OtherViewHolder)holder).tvMessage.setVisibility(View.VISIBLE);

                ((OtherViewHolder)holder).tvMessage.setText(itemList.get(pos).getContent());

                // 이미지 메세지일 경우
            }else if(itemList.get(position).getMode() == 2){

                ((OtherViewHolder)holder).tvMessage.setVisibility(View.GONE);
                ((OtherViewHolder)holder).ivOtherChatImage.setVisibility(View.VISIBLE);

                GlideApp.with(holder.itemView).load("http://13.209.49.7/movieApp"+itemList.get(position).getContent())
                        .placeholder(R.drawable.ic_photo_black_24dp)
                        .override(600,800)
                        .into(((OtherViewHolder)holder).ivOtherChatImage);


                ((OtherViewHolder)holder).ivOtherChatImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onImageClicked(pos);
                    }
                });

            }
        }
    }

    // Static inner class to initialize the views of rows
    static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView tvMessage;
        public TextView tvDate;
        public ImageView ivMyChatImage;
        public TextView tvMyChatCount;

        public MyViewHolder(View itemView) {
            super(itemView);

            tvMessage = (TextView) itemView.findViewById(R.id.tvMyChatMessage);
            tvDate = (TextView) itemView.findViewById(R.id.tvMyChatDate);
            ivMyChatImage = (ImageView) itemView.findViewById(R.id.ivMyChatImage);
            tvMyChatCount = (TextView) itemView.findViewById(R.id.tvMyChatCount);
        }
        @Override
        public void onClick(View view) {
            Log.d("onClick!", "onClick: ");
        }
    }

    // Static inner class to initialize the views of rows
    static class OtherViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public CircleImageView civProfile;
        public TextView tvName;
        public TextView tvMessage;
        public TextView tvDate;
        public ImageView ivOtherChatImage;

        public OtherViewHolder(View itemView) {
            super(itemView);
            //            itemView.setOnClickListener(this);
            //img = (ImageView) itemView.findViewById(R.id.ivBoardListImage);

            civProfile = (CircleImageView) itemView.findViewById(R.id.civOtherChatProfile);
            tvName = (TextView) itemView.findViewById(R.id.tvOtherChatNickname);
            tvMessage = (TextView) itemView.findViewById(R.id.tvOtherChatMessage);
            tvDate = (TextView) itemView.findViewById(R.id.tvOtherChatDate);
            ivOtherChatImage = (ImageView) itemView.findViewById(R.id.ivOtherChatImage);



        }
        @Override
        public void onClick(View view) {
            Log.d("onClick!", "onClick: ");
        }
    }

    //viewtype을 정하는 메소드
    @Override
    public int getItemViewType(int position) {

        if(itemList.get(position).getSenderEmail().equals(loggedUseremail)){
            return MessageContent.MY_TYPE;
        }else{
            return MessageContent.OTHER_TYPE;
        }
    }




}
