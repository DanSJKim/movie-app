package com.example.retrofitexample.Board;

import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.retrofitexample.Board.Image.BoardImageItem;
import com.example.retrofitexample.Board.Image.BoardImageUploadAdapter;
import com.example.retrofitexample.Board.Image.ImageListAdapter;
import com.example.retrofitexample.Board.Image.ResponseImages;
import com.example.retrofitexample.Board.Image.ViewPager.MyCustomPagerAdapter;
import com.example.retrofitexample.Retrofit.Api;
import com.example.retrofitexample.LoginRegister.SharedPref;
import com.example.retrofitexample.R;
import com.example.retrofitexample.Retrofit.ApiClient;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BoardUploadActivity extends AppCompatActivity implements BoardImageUploadAdapter.BoardImageUploadRecyclerViewClickListener{

    public static final String TAG = "BoardUploadActivity : ";

    ImageView ivImage;
    EditText Title, Content;
    Button Submit, Cancel;

    private  static final int IMAGE = 100;
    int boardId;//게시물 번호
    int loggedUserId;//회원 번호

    List<Uri> uriList;
    int imgpos; //이미지 클릭 시 받아오는 이미지 포지션 값

    //뷰페이저 이미지 리스트
//    ViewPager viewPager;
//    MyCustomPagerAdapter myCustomPagerAdapter;

    //리사이클러뷰 이미지 리스트
    private RecyclerView recyclerView;
    private BoardImageUploadAdapter adapter;

    //현재 화면에 보이는 포지션 값 저장
    int currentVisiblePosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_upload);

        // Initializing list view with the custom adapter
        ArrayList <BoardImageItem> itemList = new ArrayList<BoardImageItem>();

        ivImage = (ImageView) findViewById(R.id.ivUploadImage);
        Title = (EditText) findViewById(R.id.etUploadTitle);
        Content = (EditText) findViewById(R.id.etUploadContent);
        Submit = (Button) findViewById(R.id.btnUpload);
        Cancel = (Button) findViewById(R.id.btnUploadCancel);

        final String loggedUseremail = SharedPref.getInstance(this).LoggedInEmail();
        Log.d(TAG, "onCreate: loggeduseremail:" + loggedUseremail);
        loggedUserId = SharedPref.getInstance(this).LoggedInId();
        Log.d(TAG, "onCreate: loggedUserId: " + loggedUserId);

        Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String holder_title = Title.getText().toString();
                String holder_content = Content.getText().toString();

                uploadingBoard(loggedUserId, loggedUseremail, holder_title, holder_content);



            }
        });

        ivImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();// 이미지 업로드
            }
        });

        initViews();
    }

    private void uploadingBoard(int uid, String useremail, String title, String content) {

        //making api call
        Api api = ApiClient.getClient().create(Api.class);
        Call<BoardItem> updatemypage = api.boardUpload(uid, useremail, title, content);

        updatemypage.enqueue(new Callback<BoardItem>() {
            @Override
            public void onResponse(Call<BoardItem> call, Response<BoardItem> response) {

                //데이터 사용자가 데이터베이스에 입력 한 데이터와 일치하면 SharedPreferences에 저장된 사용자 이름으로 응답을 보내고 ActivityProfile이 시작됩니다.
                //php로부터 받은 getIsSuccess값이 1이면
                    Log.d(TAG, "onResponse: Successfully Uploaded");
                    boardId = response.body().getId();//게시물 번호
                    Log.d(TAG, "onResponse: board.getId(): " + response.body().getId());

                uploadMultipleImages(uriList);
            }

            @Override
            public void onFailure(Call<BoardItem> call, Throwable t) {

                Log.d(TAG, "onFailure: fail!");
                Toast.makeText(BoardUploadActivity.this,t.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initViews(){
        recyclerView = (RecyclerView)findViewById(R.id.recyclerUploadImageList);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setOnFlingListener(null);//snaphelper에서 발생하는 onflinglistener already set 오류 방지용 코드

        SnapHelper snapHelper = new PagerSnapHelper();
        recyclerView.setLayoutManager(layoutManager);
        snapHelper.attachToRecyclerView(recyclerView);



        uriList = new ArrayList();

        //게시물 목록 업로드
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode== 1 && resultCode==RESULT_OK && data!=null)
        {
            //단일 이미지
            if(data.getData() != null) {
                Log.d(TAG, "onActivityResult: one image");

                //이미지를 5개 이상 선택하면 이미지가 불러와지지 않는다.
                if(uriList.size() > 4){

                    //notify user here...
                    Toast.makeText(this, "최대 5개의 이미지 선택 가능합니다.", Toast.LENGTH_SHORT).show();
                }else{

                    Uri uri = data.getData();
                    uriList.add(uri);
                }

                adapter = new BoardImageUploadAdapter(uriList);
                adapter.setOnClickListener(BoardUploadActivity.this);
                recyclerView.setAdapter(adapter);

                //dot indicator
                recyclerView.addItemDecoration(new CirclePagerIndicatorDecoration());
            }

            //다중 이미지
            if(data.getClipData() != null) {

                ClipData clipData = data.getClipData();
                Log.d(TAG, "onActivityResult: clipdata: " + clipData);


                //이미지를 5개 이상 선택하면 이미지가 불러와지지 않는다.
                if(clipData.getItemCount() > 5){
                    //notify user here...
                    Toast.makeText(this, "최대 5개의 이미지 선택 가능합니다.", Toast.LENGTH_SHORT).show();

                }else {
                    for (int i = 0; i < data.getClipData().getItemCount(); i++) {

                        //clipdata에서 각각의 이미지 uri 꺼내기
                        Uri selectedImage = data.getClipData().getItemAt(i).getUri();
                        Log.e("uri", selectedImage + "");
                        uriList.add(selectedImage);
                    }
                }

                adapter = new BoardImageUploadAdapter(uriList);
                adapter.setOnClickListener(BoardUploadActivity.this);
                recyclerView.setAdapter(adapter);

                //dot indicator
                recyclerView.addItemDecoration(new CirclePagerIndicatorDecoration());


                Log.d(TAG, "onActivityResult: uriList: " + uriList);
                //데이터 변경 확인
                adapter.notifyDataSetChanged();
            }

        }
        else if((requestCode== 2 && resultCode==RESULT_OK && data!=null)){

            //단일 이미지
            if(data.getData() != null) {
                    Uri uri = data.getData();
                    uriList.set(imgpos, uri);

                adapter = new BoardImageUploadAdapter(uriList);
                adapter.setOnClickListener(BoardUploadActivity.this);
                recyclerView.setAdapter(adapter);

                //dot indicator
                recyclerView.addItemDecoration(new CirclePagerIndicatorDecoration());

                Log.d(TAG, "onActivityResult: after uriList: " + uriList);
            }
        }
    }

    //Uri 실제 기기 내 경로 가져오기
    private String getRealPathFromURI(Uri contentUri) {

        if (contentUri.getPath().startsWith("/storage")) {
            return contentUri.getPath();
        }

        Log.d(TAG, "getRealPathFromURI: contentUri: " + contentUri);
        String id = DocumentsContract.getDocumentId(contentUri).split(":")[1];
        Log.d(TAG, "getRealPathFromURI: id: " + id);

        String[] columns = { MediaStore.Files.FileColumns.DATA };
        Log.d(TAG, "getRealPathFromURI: columns: " + columns);

        String selection = MediaStore.Files.FileColumns._ID + " = " + id;
        Log.d(TAG, "getRealPathFromURI: selection: " + selection);

        Cursor cursor = getContentResolver().query(MediaStore.Files.getContentUri("external"), columns, selection, null, null);
        Log.d(TAG, "getRealPathFromURI: cursor: " + cursor);

        try {
            int columnIndex = cursor.getColumnIndex(columns[0]);
            Log.d(TAG, "getRealPathFromURI: columnIndex: " + columnIndex);
            if (cursor.moveToFirst()) {
                Log.d(TAG, "getRealPathFromURI: cursor.getString(columnIndex): " + cursor.getString(columnIndex));
                return cursor.getString(columnIndex);
            }
        } finally {
            cursor.close();
        }
        return null;
    }


    //uri를 MultipartBody형식으로 변환해 준다.
    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri) {
        Log.d(TAG, "prepareFilePart: realpath: " + getRealPathFromURI(fileUri));

        //uri 실제경로를 담는다.
        File file = new File(getRealPathFromURI(fileUri));
        Log.d(TAG, "prepareFilePart: is file: " + file);

        // create RequestBody instance from file
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        Log.d(TAG, "prepareFilePart: requestFile: " + requestFile);

        Log.d(TAG, "prepareFilePart: MultipartBody.Part.createFormData(partName, file.getName(), requestFile): " + MultipartBody.Part.createFormData(partName, file.getName(), requestFile));
        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }

    //다중 이미지 업로드
    private void uploadMultipleImages(final List<Uri> uris){

        List<MultipartBody.Part> parts = new ArrayList<>();
        for(Uri uri: uris){
            Log.d(TAG, "uploadMultipleImages: uri: " + uri);

            //very important files[]
            MultipartBody.Part imageRequest = prepareFilePart("file[]", uri);
            Log.d(TAG, "uploadMultipleImages: imageRequest: " + imageRequest);
            parts.add(imageRequest);
        }

        //서버 통신 요청
        Api apiInterface = ApiClient.getClient().create(Api.class);
        Log.d(TAG, "uploadMultipleImages: parts: " + parts);
        Call<ResponseImages> call = apiInterface.uploadMultipleFilesDynamic(parts, boardId);

        call.enqueue(new Callback<ResponseImages>() {
            @Override
            public void onResponse(Call<ResponseImages> call, Response<ResponseImages> response) {

                Log.e("onResponse getMessage: ",""+response.body());
                finish();

            }
            @Override
            public void onFailure(Call<ResponseImages> call, Throwable t) {

                Log.e("Server Response fail!: ",""+t.toString());
                finish();
            }
        });
    }

    public void onItemClicked(int position){
        //Log.d(TAG, "onItemClicked: img: " + position + uriList.get(position));

        imgpos = position;

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.ACTION_CAMERA_BUTTON, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"), 2);
    }

    public void onDeleteClicked(int position){

        uriList.remove(position);
        recyclerView.setAdapter(adapter);

    }

    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart() called");

//        finish();
//        startActivity(getIntent());
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called");

    }

    @Override
    protected void onResume() {
        super.onResume();

        //스크롤 이동
        Log.d(TAG, "onResponse: itemTotalCount: " + currentVisiblePosition);
        ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPosition(currentVisiblePosition);

    }

    @Override
    protected void onPause() {
        super.onPause();

        currentVisiblePosition = ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
        Log.d(TAG, "onPause: currentvisibleposition: " + currentVisiblePosition);
        Log.d(TAG, "onPause() called");
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
    }


}
