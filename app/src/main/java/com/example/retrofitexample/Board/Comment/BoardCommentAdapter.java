package com.example.retrofitexample.Board.Comment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Movie;
import android.media.Image;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.retrofitexample.Board.BoardItem;
import com.example.retrofitexample.GlideApp;
import com.example.retrofitexample.LoginRegister.SharedPref;
import com.example.retrofitexample.R;

import org.w3c.dom.Text;

import java.util.ArrayList;

//리사이클러뷰의 기본 틀은 getItemCount, onCreateViewHolder, MyViewHolder, onBindViewholder 순서
public class BoardCommentAdapter  extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String TAG = "BoardCommentAdapter : ";

    //adapter에 들어갈 아이템
    private ArrayList<CommentItem> commentitem;
    Context ctx;

    public BoardCommentAdapter(Context ctx, ArrayList<CommentItem> commentitem) {
        this.commentitem = commentitem;
        this.ctx=ctx;
    }


    public interface BoardCommentRecyclerViewClickListener{//클릭이벤트 만들어주기. 다른 클래스에 implement시킨다
        void onReplyClicked(int position);//내가 누른 아이템의 포지션을 외부에서 알 수 있도록 정의
        void onReplySubmitClicked(int position);
        void onCommentEditClicked(int position);
    }


    private BoardCommentAdapter.BoardCommentRecyclerViewClickListener mListener;//위의 인터페이스를 내부에서 하나 들고있어야 한다

    public void setOnClickListener(BoardCommentAdapter.BoardCommentRecyclerViewClickListener listener){//외부에서 메소드를 지정할 수 있도록 이런 메소드를 준비
        Log.d(TAG, "setOnClickListener");

        mListener = listener;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder");

        View view;
        switch(viewType){
            case CommentItem.FIRST_DEPTH_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.board_comment_item, parent, false);
                return new FirstViewBoardHolder(view);
            case CommentItem.SECOND_DEPTH_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.board_comment_reply_item, parent, false);
                return new SecondBoardHolder(view);
        }

        return null;
    }


    //각 Item form에 맞게 초기 세팅
    public class FirstViewBoardHolder extends RecyclerView.ViewHolder{

        private ImageView profile;
        private TextView email;
        private TextView content;
        private TextView date;
        private TextView reply;
        private EditText replyform;
        private Button replybutton;
        private View subItem;//레이아웃
        private ImageView edit;

        public FirstViewBoardHolder(View itemView) {
            super(itemView);
            Log.d(TAG, "FirstViewBoardHolder");

            profile = (ImageView) itemView.findViewById(R.id.ivBoardCommentProfile);
            email = (TextView) itemView.findViewById(R.id.tvBoardCommentEmail);
            content = (TextView) itemView.findViewById(R.id.tvBoardCommentContent);
            date = (TextView) itemView.findViewById(R.id.tvBoardCommentDate);
            reply = (TextView) itemView.findViewById(R.id.tvBoardCommentReply); //답글 버튼
            replyform = (EditText) itemView.findViewById(R.id.etBoardCommentReplyContent);
            replybutton = (Button) itemView.findViewById(R.id.btnBoardCommentSubmit);
            subItem = itemView.findViewById(R.id.sub_comment);//답글 작성 레이아웃
            edit = (ImageView) itemView.findViewById(R.id.ivBoardCommentEdit);
        }

        private void bind(CommentItem citem) {
            boolean expanded = citem.isExpanded();

            subItem.setVisibility(expanded ? View.VISIBLE : View.GONE);

        }

    }

    //각 Item form에 맞게 초기 세팅
    public class SecondBoardHolder extends RecyclerView.ViewHolder {

        ImageView profile;
        TextView email;
        TextView content;
        TextView date;

        private ImageView edit;

        public SecondBoardHolder(View itemView) {
            super(itemView);
            Log.d(TAG, "SecondBoardHolder");

            profile = (ImageView) itemView.findViewById(R.id.ivBoardCommentProfile);
            email = (TextView) itemView.findViewById(R.id.tvBoardCommentEmail);
            content = (TextView) itemView.findViewById(R.id.tvBoardCommentContent);
            date = (TextView) itemView.findViewById(R.id.tvBoardCommentDate);
            edit = (ImageView) itemView.findViewById(R.id.ivBoardCommentEdit);

        }
    }





    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder");
        Log.d(TAG, "onBindViewHolder: commentitem.get(position).parent: " + commentitem.get(position).parent);

        final int pos = position;

        ////////////////////////////댓글////////////////////////////
        if(commentitem.get(position).parent == -1){

            ((FirstViewBoardHolder)holder).bind(commentitem.get(position));
            //댓글 프로필 이미지
            GlideApp.with(holder.itemView).load(commentitem.get(position).getImg_path())
                    .override(300,400)
                    .into(((FirstViewBoardHolder)holder).profile);

            //holder.board_id.setText(String.valueOf(bt.get(position).getCmtid()));//게시물 번호
            ((FirstViewBoardHolder)holder).email.setText(commentitem.get(position).getEmail());//이메일
            ((FirstViewBoardHolder)holder).content.setText(commentitem.get(position).getContent());//내용
            ((FirstViewBoardHolder)holder).date.setText(commentitem.get(position).getDate());//날짜

            //대댓글 펼치기 버튼
            ((FirstViewBoardHolder)holder).reply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //mListener.onReplyClicked(pos);
                    Log.d(TAG, "onClick:");

                    // Get the current state of the item
                    boolean expanded = commentitem.get(pos).isExpanded();
                    // Change the state
                    commentitem.get(pos).setExpanded(!expanded);
                    // Notify the adapter that item has changed
                    notifyItemChanged(pos);
                }
            });

            //대댓글 등록 버튼
            ((FirstViewBoardHolder)holder).replybutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onReplySubmitClicked(pos);
                }
            });

            //댓글 수정 버튼
            ((FirstViewBoardHolder)holder).edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onCommentEditClicked(pos);
                }
            });

            //좋아요 목록 보기

            //로그인 사용자 이메일과 댓글 업로드 이메일 비교
            String email = SharedPref.getInstance(ctx).LoggedInEmail();//로그인 사용자
            String commentEmail = commentitem.get(position).getEmail();//게시물 사용자
            Log.d(TAG, "onBindViewHolder: SharedPref.getInstance(ctx).LoggedInEmail(): " + email);
            Log.d(TAG, "onBindViewHolder: commentitem.get(position).getEmail()" + commentEmail);

            //같으면 댓글 수정 삭제 버튼 표시
            if(commentEmail.equals(email)){
                ((FirstViewBoardHolder)holder).edit.setVisibility(View.VISIBLE);
            }else{
                ((FirstViewBoardHolder)holder).edit.setVisibility(View.GONE);
            }

        }else{////////////////////////////대댓글////////////////////////////

            //대댓글 프로필 이미지
            GlideApp.with(holder.itemView).load(commentitem.get(position).getImg_path())
                    .override(300,400)
                    .into(((SecondBoardHolder)holder).profile);

            //holder.board_id.setText(String.valueOf(bt.get(position).getCmtid()));//게시물 번호
            ((SecondBoardHolder)holder).email.setText(commentitem.get(position).getEmail());//이메일
            ((SecondBoardHolder)holder).content.setText(commentitem.get(position).getContent());
            ((SecondBoardHolder)holder).date.setText(commentitem.get(position).getDate());

            //로그인 사용자 이메일과 대댓글 업로드 이메일 비교
            String email = SharedPref.getInstance(ctx).LoggedInEmail();//로그인 사용자
            String commentEmail = commentitem.get(position).getEmail();//게시물 사용자
            Log.d(TAG, "onBindViewHolder: SharedPref.getInstance(ctx).LoggedInEmail(): " + email);
            Log.d(TAG, "onBindViewHolder: commentitem.get(position).getEmail()" + commentEmail);


            //같으면 대댓글 수정 삭제 버튼 표시
            if(commentEmail.equals(email)){
                ((SecondBoardHolder)holder).edit.setVisibility(View.VISIBLE);
            }else{
                ((SecondBoardHolder)holder).edit.setVisibility(View.GONE);
            }

            //댓글 수정 버튼
            ((SecondBoardHolder)holder).edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onCommentEditClicked(pos);
                }
            });
        }
    }


    //viewtype을 정하는 메소드
    @Override
    public int getItemViewType(int position) {
        Log.d(TAG, "getItemViewType: commentitem.get(position).getParent(): " + commentitem.get(position).parent);

        if(commentitem.get(position).parent == -1){
            return CommentItem.FIRST_DEPTH_TYPE;
        }else{
            return CommentItem.SECOND_DEPTH_TYPE;
        }
    }


    @Override
    public int getItemCount() {

        return commentitem.size();
    }
}