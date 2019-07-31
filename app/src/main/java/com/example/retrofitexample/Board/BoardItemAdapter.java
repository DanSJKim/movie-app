package com.example.retrofitexample.Board;

import android.app.Activity;
import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.retrofitexample.Board.Image.BoardImageItem;
import com.example.retrofitexample.Board.Image.BoardImageListAdapter;
import com.example.retrofitexample.GlideApp;
import com.example.retrofitexample.R;

import java.util.ArrayList;
import java.util.Arrays;


//리사이클러뷰의 기본 틀은 getItemCount, onCreateViewHolder, MyViewHolder, onBindViewholder 순서
public class BoardItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String TAG = "BoardItemAdapter : ";

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;

    Context context;
    private ArrayList<BoardItem> boardItems; //아이템
    private ArrayList<BoardImageItem> boardImageItems;

    private Activity activity;
    private boolean isLoading;



    public BoardItemAdapter(RecyclerView recyclerView, ArrayList<BoardItem> boardItems, Activity activity) {
        this.boardItems = boardItems;
        this.activity = activity;

        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();


    }


    public interface BoardItemRecyclerViewClickListener{//클릭이벤트 만들어주기. 다른 클래스에 implement시킨다
        void onItemClicked(int position);//내가 누른 아이템의 포지션을 외부에서 알 수 있도록 정의
        void onMoreClicked(int position);
        void onLikeClicked(int position);
        void onUnlikeClicked(int position);
        void onCommentClicked(int position);
        void onLikeCountClicked(int position);
        void onProfileClicked(int position);
    }


    private BoardItemRecyclerViewClickListener mListener;//위의 인터페이스를 내부에서 하나 들고있어야 한다

    public void setOnClickListener(BoardItemRecyclerViewClickListener listener){//외부에서 메소드를 지정할 수 있도록 이런 메소드를 준비
        Log.d(TAG, "setOnClickListener");

        mListener = listener;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {

            View view = LayoutInflater.from(activity).inflate(R.layout.board_item, parent, false);
            ViewBoardHolder boardHolder = new ViewBoardHolder(view);
            Log.d(TAG, "onCreateViewHolder 1");
            context = parent.getContext();

            return boardHolder;

        } else if (viewType == VIEW_TYPE_LOADING) {

            View view = LayoutInflater.from(activity).inflate(R.layout.board_item_loading, parent, false);
            LoadingViewHolder loadingViewHolder = new LoadingViewHolder(view);
            Log.d(TAG, "onCreateViewHolder 2");
            return loadingViewHolder;
        }
            return null;
    }


    private class LoadingViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public LoadingViewHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar1);
        }
    }

    //각 Item form에 맞게 초기 세팅
   public class ViewBoardHolder extends RecyclerView.ViewHolder {

        TextView board_id;
        TextView board_title;
        TextView board_writer;
        TextView board_datetime;
        TextView board_content;
        TextView board_like_count;
        TextView board_comment_count;

        ImageView board_more;
        ImageView board_like;
        ImageView board_unlike;
        ImageView board_profile;
        ImageView board_comment;

        RecyclerView recyclerView;
        RecyclerView.LayoutManager layoutManager;
        BoardImageListAdapter adapter;
        SnapHelper snapHelper;

        public ViewBoardHolder(View itemView) {
            super(itemView);
            Log.d(TAG, "ViewBoardHolder");

            board_title = (TextView) itemView.findViewById(R.id.tvBoardTitle);
            board_content = (TextView) itemView.findViewById(R.id.tvBoardContent);
            board_writer = (TextView) itemView.findViewById(R.id.tvBoardWriter);
            board_datetime = (TextView) itemView.findViewById(R.id.tvBoardDate);
            board_more = (ImageView) itemView.findViewById(R.id.ivMoreButton);
            board_id = (TextView) itemView.findViewById(R.id.boardId);
            board_like = (ImageView) itemView.findViewById(R.id.ivBoardLike);
            board_unlike = (ImageView) itemView.findViewById(R.id.ivBoardUnLike);
            board_like_count = (TextView) itemView.findViewById(R.id.tvBoardLikeCount);
            board_profile = (ImageView) itemView.findViewById(R.id.ivBoardProfile);
            board_comment = (ImageView) itemView.findViewById(R.id.ivBoardComment);
            board_comment_count = (TextView) itemView.findViewById(R.id.tvBoardCommentCount);

            recyclerView = (RecyclerView)itemView.findViewById(R.id.rvBoardItemList);
            //snap helper
            snapHelper = new PagerSnapHelper();
            snapHelper.attachToRecyclerView(recyclerView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        Log.d(TAG, "onBindViewHolder");

        if (holder instanceof ViewBoardHolder) {

            final int pos = position;
            //final BoardItem data = item.get(position);
            ((ViewBoardHolder)holder).board_title.setText(boardItems.get(position).getTitle());
            ((ViewBoardHolder)holder).board_content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onItemClicked(pos);
                }
            });

            ((ViewBoardHolder)holder).board_writer.setText(boardItems.get(position).getWriter());
            ((ViewBoardHolder)holder).board_datetime.setText(boardItems.get(position).getDatetime());
            ((ViewBoardHolder)holder).board_content.setText(boardItems.get(position).getContent());
            ((ViewBoardHolder)holder).board_id.setText(String.valueOf(boardItems.get(position).getId()));//게시물 번호
            ((ViewBoardHolder)holder).board_like_count.setText("좋아요 "+ boardItems.get(position).getLikeCount() + "개");
            ((ViewBoardHolder)holder).board_comment_count.setText("댓글 " + boardItems.get(position).getCommentCount() + "개");

            ((ViewBoardHolder)holder).board_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onMoreClicked(pos);
                }
            });


            if(boardItems.get(position).getProfile_img_path() != null) {

                //프로필 이미지
                GlideApp.with(holder.itemView).load("http://13.209.49.7/movieApp" + boardItems.get(position).getProfile_img_path())
                        .override(300, 400)
                        .into(((ViewBoardHolder)holder).board_profile);
            }

            ((ViewBoardHolder)holder).board_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: profile " + pos);
                    mListener.onProfileClicked(pos);
                }
            });

            ((ViewBoardHolder)holder).board_like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: like " + pos);
                    mListener.onLikeClicked(pos);
                }
            });

            ((ViewBoardHolder)holder).board_unlike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: unlike " + pos);
                    mListener.onUnlikeClicked(pos);
                }
            });

            ((ViewBoardHolder)holder).board_comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: comment " + pos);
                    mListener.onCommentClicked(pos);
                }
            });

            if(boardItems.get(position).getIsLiked() == 0){
                ((ViewBoardHolder)holder).board_like.setVisibility(View.VISIBLE);
                ((ViewBoardHolder)holder).board_unlike.setVisibility(View.GONE);
            }else{
                ((ViewBoardHolder)holder).board_unlike.setVisibility(View.VISIBLE);
                ((ViewBoardHolder)holder).board_like.setVisibility(View.GONE);
            }

            ((ViewBoardHolder)holder).board_like_count.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onLikeCountClicked(pos);

                }
            });

            //init
            ((ViewBoardHolder)holder).recyclerView.setHasFixedSize(true);
            ((ViewBoardHolder)holder).layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
            ((ViewBoardHolder)holder).recyclerView.setLayoutManager(((ViewBoardHolder)holder).layoutManager);

            //dot indicator
            ((ViewBoardHolder)holder).recyclerView.addItemDecoration(new CirclePagerIndicatorDecoration());

            //이미지객체들을 저장한 배열을 어레이리스트로 바꿔준다.
            boardImageItems = new ArrayList<>(Arrays.asList(boardItems.get(position).getBoardImageItems()));
            ((ViewBoardHolder)holder).adapter = new BoardImageListAdapter(context, boardImageItems);
            ((ViewBoardHolder)holder).recyclerView.setAdapter(((ViewBoardHolder)holder).adapter);

        } else if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }
    }

    public void setLoaded() {
        isLoading = false;
    }

    @Override
    public int getItemViewType(int position) {
        return boardItems.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return boardItems.size();
    }

}