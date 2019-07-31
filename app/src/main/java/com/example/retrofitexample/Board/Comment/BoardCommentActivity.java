package com.example.retrofitexample.Board.Comment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.retrofitexample.Board.BoardActivity;
import com.example.retrofitexample.Board.BoardItem;
import com.example.retrofitexample.Board.BoardItemAdapter;
import com.example.retrofitexample.Board.BoardResponse;
import com.example.retrofitexample.Board.BoardUpdateActivity;
import com.example.retrofitexample.Board.BoardUploadActivity;
import com.example.retrofitexample.LoginRegister.SharedPref;
import com.example.retrofitexample.R;
import com.example.retrofitexample.Retrofit.Api;
import com.example.retrofitexample.Retrofit.ApiClient;

import org.w3c.dom.Comment;

import java.util.ArrayList;
import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BoardCommentActivity extends AppCompatActivity implements BoardCommentAdapter.BoardCommentRecyclerViewClickListener {
    public static final String TAG = "BoardCommentActivity : ";

    //View
    EditText etComment;
    Button btnSubmit;

    String holder_content;
    int userid;
    int boardid; //BoardActivity에서 온 게시물 번호

    private static RecyclerView recyclerView;
    private static ArrayList<CommentItem> data;
    private static BoardCommentAdapter cadapter;

    //현재 화면에 보이는 포지션 값 저장
    static int currentVisiblePosition = 0;

    int dpos; //댓글 삭제 포지션


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_comment);

        etComment = (EditText) findViewById(R.id.etComment);
        btnSubmit = (Button) findViewById(R.id.btnCommentSubmit);

        //유저 번호
        userid = SharedPref.getInstance(this).LoggedInId();
        Log.d(TAG, "onCreate: userid: " + userid);

        //게시물 번호
        Intent intent = getIntent(); //from BoardActivity
        boardid = intent.getIntExtra("boardid",-1);
        Log.d(TAG, "onCreate: boardid: " + boardid);

        //댓글 표시
        initViews();

        //댓글 등록 클릭이벤트
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //작성 된 댓글 내용
                holder_content = etComment.getText().toString();
                Log.d(TAG, "onClick: holder_content: " + holder_content);
                if(holder_content.isEmpty()){
                    Toast.makeText(getApplicationContext(), "내용을 입력하세요.", Toast.LENGTH_LONG).show();

                }else{
                    uploadingComment(holder_content, userid, boardid, -1);
                    etComment.getText().clear();

                }


            }
        });
    }


    private void uploadingComment(String content, int user_id, int board_id, int cmtid) {

        //making api call
        Api api = ApiClient.getClient().create(Api.class);
        Call<CommentItem> uploadcomment = api.uploadComment(content, user_id, board_id, cmtid);

        uploadcomment.enqueue(new Callback<CommentItem>() {
            @Override
            public void onResponse(Call<CommentItem> call, Response<CommentItem> response) {
                Log.d(TAG, "onResponse: Successfully Uploaded" + response.body());
                etComment.getText().clear();

                //refresh
                finish();
                startActivity(getIntent());
                overridePendingTransition(0, 0);
            }

            @Override
            public void onFailure(Call<CommentItem> call, Throwable t) {
                Log.d(TAG, "onFailure: fail!");

                Toast.makeText(BoardCommentActivity.this,t.getLocalizedMessage(),Toast.LENGTH_SHORT).show();

            }
        });
    }

    //리사이클러뷰 초기화
    private void initViews(){

        recyclerView = (RecyclerView)findViewById(R.id.comment_list);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);



        //댓글 목록 업로드
        listComments(boardid);
    }


    //댓글 목록 불러오기
    private void listComments(int board_id){

//        making api call
        Api api = ApiClient.getClient().create(Api.class);
        Call<CommentResponse> listComment = api.listComment(board_id);

        listComment.enqueue(new Callback<CommentResponse>() {
            @Override
            public void onResponse(Call<CommentResponse> call, Response<CommentResponse> response) {
                Log.d(TAG, "onResponse: Successfully Uploaded" + response.body().getCommentItems());

                CommentResponse commentResponse = response.body();
                data = new ArrayList<>(Arrays.asList(commentResponse.getCommentItems())); //BoardItem객체들을 받아온 배열을 리스트로 바꿔준다.
                cadapter = new BoardCommentAdapter(getApplicationContext(),data);
                cadapter.setOnClickListener(BoardCommentActivity.this);
                recyclerView.setAdapter(cadapter);

                recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);

                        int lastVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
                        int itemTotalCount = recyclerView.getAdapter().getItemCount();
                        Log.d(TAG, "onScrolled: lastVisibleItemPosition" + lastVisibleItemPosition );
                        Log.d(TAG, "onScrolled: itemTotalCount" + itemTotalCount );
                        if(lastVisibleItemPosition == itemTotalCount){
                            Log.d(TAG, "onScrolled2: lastVisibleItemPosition" + lastVisibleItemPosition );
                            Log.d(TAG, "onScrolled2: itemTotalCount" + itemTotalCount );

                        }
                    }
                });

            }

            @Override
            public void onFailure(Call<CommentResponse> call, Throwable t) {
                Log.d(TAG, "onFailure: fail!");
                //Toast.makeText(BoardCommentActivity.this,t.getLocalizedMessage(),Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void deleteComment(int cmtid, int parent) {

        //making api call
        Api api = ApiClient.getClient().create(Api.class);
        Call<CommentItem> deletecomment = api.deleteComment(cmtid, parent);

        deletecomment.enqueue(new Callback<CommentItem>() {
            @Override
            public void onResponse(Call<CommentItem> call, Response<CommentItem> response) {
                Log.d(TAG, "onResponse: Successfully Uploaded" + response.body());

                //refresh
                finish();
                startActivity(getIntent());
                overridePendingTransition(0, 0);
                Toast.makeText(getApplicationContext(), "댓글이 삭제 되었습니다.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<CommentItem> call, Throwable t) {
                Log.d(TAG, "onFailure: fail!");

                Toast.makeText(BoardCommentActivity.this,t.getLocalizedMessage(),Toast.LENGTH_SHORT).show();

            }
        });
    }


    //댓글 펼치기
    @Override
    public void onReplyClicked(int position) {
        Log.d(TAG, "onReplyClicked data.get(position).getParent(): " + data.get(position).getCmtid());

        //uploadingComment(data.get(position).getContent(), userid, boardid, data.get(position).getCmtid());



    }

    //댓댓글 등록
    public void onReplySubmitClicked(int position){
        int parentId = data.get(position).getCmtid();
        Log.d(TAG, "onReplyClicked data.get(position).getParent(): " + parentId);
        String content = ((EditText) recyclerView.findViewHolderForAdapterPosition(position).itemView.findViewById(R.id.etBoardCommentReplyContent)).getText().toString();
        Log.d(TAG, "onReplySubmitClicked: title: " + content);
        if(content.isEmpty()) {
            Toast.makeText(getApplicationContext(), "내용을 입력하세요.", Toast.LENGTH_SHORT).show();
        }else{
            uploadingComment(content, userid, boardid, parentId);
        }
    }

    //댓글 삭제
    public void onCommentEditClicked(int position){
        final int cmtid = data.get(position).getCmtid();
        final int parent = data.get(position).getParent();
        Log.d(TAG, "onCommentDeleteClicked: cmtid: " + cmtid);
        Log.d(TAG, "onCommentDeleteClicked: parent: " + parent);


        Log.d(TAG, "메뉴 이미지 클릭");
        dpos = position;
        AlertDialog.Builder ad = new AlertDialog.Builder(BoardCommentActivity.this);

            ad.setTitle("편집");       // 제목 설정
            //ad.setMessage("편집");   // 내용 설정

            // 수정 버튼 설정
            ad.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    AlertDialog.Builder ad2 = new AlertDialog.Builder(BoardCommentActivity.this);

                    ad2.setTitle("삭제 하겠습니까?");       // 제목 설정
                    //ad.setMessage("편집");   // 내용 설정

                    // 취소 버튼 설정
                    ad2.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();     //닫기

                        }
                    });
                    // 확인 버튼 설정
                    ad2.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            deleteComment(cmtid, parent);
                            dialog.dismiss();//닫기

                        }
                    });
                    // 창 띄우기
                    ad2.show();
                    Log.d(TAG, "onCreate()");

                }
            });
            // 중립 버튼 설정
            ad.setNeutralButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();     //닫기

                }
            });
            // 수정 버튼 설정
            ad.setNegativeButton("수정", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    String content = ((TextView) recyclerView.findViewHolderForAdapterPosition(dpos).itemView.findViewById(R.id.tvBoardCommentContent)).getText().toString();
                    Log.d(TAG, "onClick: content: " + content);
                    Log.d(TAG, "onClick: cmtid: " + data.get(dpos).getCmtid());
                    //데이터 담아서 팝업(액티비티) 호출
                    Intent intent = new Intent(BoardCommentActivity.this, BoardCommentUpdateActivity.class);
                    intent.putExtra("cmtid", data.get(dpos).getCmtid());
                    intent.putExtra("content", content);
                    intent.putExtra("dpos", dpos);
                    startActivity(intent);



                    dialog.dismiss();
                }
            });
            // 창 띄우기
            ad.show();
            Log.d(TAG, "onCreate()");

    }

    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart() called");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called");

    }

    @Override
    protected void onResume() {
        super.onResume();

        initViews();

        Log.d(TAG, "onResume() called");
        ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPosition(currentVisiblePosition);
        Log.d(TAG, "onResume: currentVisiblePosition: " +  currentVisiblePosition);

        currentVisiblePosition = 0;
        Log.d(TAG, "onResume: currentVisiblePosition: " +  currentVisiblePosition);

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called");


        currentVisiblePosition = ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        Log.d(TAG, "onPause(): ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPosition(currentVisiblePosition): " +  currentVisiblePosition);


    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
        finish();
    }
}
