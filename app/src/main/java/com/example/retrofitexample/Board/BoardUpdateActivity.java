package com.example.retrofitexample.Board;

import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.retrofitexample.Board.Image.BoardImageItem;
import com.example.retrofitexample.Board.Image.ImageListAdapter;
import com.example.retrofitexample.Board.Image.ResponseImages;
import com.example.retrofitexample.Retrofit.Api;
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

public class BoardUpdateActivity extends AppCompatActivity implements ImageListAdapter.BoardUpdateRecyclerViewClickListener{
    public static final String TAG = "BoardUpdateActivity : ";

    int id;//게시물 아이디
    String writer, title, content, datetime, imagepath;
    EditText etContent;
    Button update, back;

    //이미지
    List<Uri> uriList;
    //수정 할 이미지
    List<Uri> uriUpdateList;

    //기존 이미지 아이디
    List<Integer> imgIdList;
    //서버에 보낼 이미지 아이디
    List<Integer> imgUpdateIdList;

    //이미지 리스트
    ArrayList<BoardImageItem> imagelist;

    //리사이클러뷰 이미지 리스트
    private RecyclerView recyclerView;
    private ImageListAdapter adapter;

    int imgpos; //수정 할 이미지 포지션

    public ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_update);

        //뷰 초기화
        etContent = (EditText) findViewById(R.id.etBoardUpdateContent);
        update = (Button) findViewById(R.id.btnBoardUpdate);
        back = (Button) findViewById(R.id.btnBoardUpdateBack);
        progressBar = (ProgressBar) findViewById(R.id.updateProgressBar);

        progressBar.setVisibility(View.GONE);

        //image = (ImageView) findViewById(R.id.ivBoardUpdateImage);

        //intent BoardActivity -> BoardUpdateActivity
        Intent intent = getIntent();
        id = intent.getExtras().getInt("id");
        writer = intent.getStringExtra("writer");
        title = intent.getStringExtra("title");
        content = intent.getStringExtra("content");
        datetime = intent.getStringExtra("datetime");
        imagepath = intent.getStringExtra("imagepath");
        imagelist = (ArrayList<BoardImageItem>) intent.getSerializableExtra("images");
        Log.d(TAG, "onCreate: list: " + imagelist);


        uriList = new ArrayList<>();//카메라 uri 담는 리스트
        uriUpdateList = new ArrayList<>();//수정된 이미지 담는 리스트
        imgUpdateIdList = new ArrayList<>();//서버에 보낼 이미지 아이디 리스트

        //기존 이미지 번호 리스트에 저장
        imgIdList = new ArrayList<>();
        for(int i = 0 ; i < imagelist.size() ; i++){
            imgIdList.add(imagelist.get(i).getId());
        }
        Log.d(TAG, "onCreate: imgIdList: " + imgIdList);

        etContent.setText(content);

        //수정 확인 버튼
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: clicked");
                Log.d(TAG, "onClick: imagelist.size(): " + imagelist.size());

                if(imagelist.size() < 2){//"+"이미지 아이템도 포함이기 때문에 2로 설정
                    Toast.makeText(BoardUpdateActivity.this, "이미지를 1개 이상 선택하세요.", Toast.LENGTH_SHORT).show();
                }else {
                    boardUpdate();
                }
            }
        });

        //리사이클러뷰 초기화
        recyclerView = (RecyclerView)findViewById(R.id.rvBoardUpdateImageList);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this,3);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new ImageListAdapter(this, imagelist);
        adapter.setOnClickListener(BoardUpdateActivity.this);
        recyclerView.setAdapter(adapter);

        BoardImageItem bii = new BoardImageItem("addbutton");
        imagelist.add(bii);
        Log.d(TAG, "onCreate: imagelist: " + imagelist);
        adapter.notifyDataSetChanged();
    }


    //프로그레스바 생성
    public void setProgressDialog() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {


                finish();
            }
        }, 3000);
    }

    private void boardUpdate(){

        Log.d(TAG, "loadJSON: id: " + id);//게시물 번호
        content = etContent.getText().toString();//게시물 내용

        if(imgIdList.size() < 1){
            imgIdList.add(-1);
        }
        //making api call
        Log.d(TAG, "boardUpdate: imgIdList: " + imgIdList);
        Api api = ApiClient.getClient().create(Api.class);
        Call<ResponseServer> call = api.boardUpdate(id, content, imgIdList);

        call.enqueue(new Callback<ResponseServer>() {
            @Override
            public void onResponse(Call<ResponseServer> call, Response<ResponseServer> response) {

                Log.d(TAG, "onResponse: response!: " + response.body().getMessage());
                Log.d(TAG, "onResponse: uriList?" + uriList);
                Log.d(TAG, "onResponse: uriUpdateList? " + uriUpdateList);

                //게시물 추가, 게시물 수정
                if(uriList.size() > 0 && uriUpdateList.size() > 0) {
                    Log.d(TAG, "onResponse: 000");

                    uploadMultipleImages(uriList);
                    updateImages(uriUpdateList);


                }
                //게시물 추가
                else if(uriList.size() > 0 && uriUpdateList.size() == 0){
                    Log.d(TAG, "onResponse: 111");

                    uploadMultipleImages(uriList);

                //게시물 수정
                }else if(uriList.size() == 0 && uriUpdateList.size() > 0){
                    Log.d(TAG, "onResponse: 222");

                    updateImages(uriUpdateList);
                    Log.d(TAG, "onResponse: updateImages!@");

                //둘다 안 할 경우
                }else if(uriList.size() == 0 && uriUpdateList.size() == 0){
                    Log.d(TAG, "onResponse: 333");

                    finish();
                }
            }

            @Override
            public void onFailure(Call<ResponseServer> call, Throwable t) {

                Log.d("Error",t.getMessage());
            }
        });
    }


    @Override
    public void onItemClicked(int position) {

        imgpos = position;
        Log.d(TAG, "onItemClicked: " + imagelist.get(position).getImg_path());

        //uri 이미지
        //imgIdList는 기존 있던 이미지들의 아이디를 담아놓은 리스트인데, 기존 아이디보다 포지션값이 크면 새로 추가 된 uri 이미지다.
        if(position >= imgIdList.size()){
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.putExtra(Intent.ACTION_GET_CONTENT, true);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,"Select Picture"), 3);

            //기존 이미지
        }else{
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.putExtra(Intent.ACTION_GET_CONTENT, true);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,"Select Picture"), 2);
        }

    }

    public void onItemLongClicked(int position){

        imgpos = position;
        Log.d(TAG, "onItemLongClicked: " + imagelist.get(position).getImg_path());
//        Log.d(TAG, "onItemLongClicked: id: " + imgIdList.get(imgpos));

        //uri 이미지
        //imgIdList는 기존 있던 이미지들의 아이디를 담아놓은 리스트인데, 기존 아이디보다 포지션값이 크면 새로 추가 된 uri 이미지다.
        if(position >= imgIdList.size()){
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.putExtra(Intent.ACTION_GET_CONTENT, true);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,"Select Picture"), 3);

        //기존 이미지
        }else{
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.putExtra(Intent.ACTION_GET_CONTENT, true);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,"Select Picture"), 2);
        }


    }

    //이미지 삭제
    public void onImageDeleteClick(int position){
        Log.d(TAG, "onImageDeleteClick: urilist: " + uriList);

            //갤러리에서 이미지를 추가했다가 업로드하지 않고 바로 지울 경우 urilist 목록도 다시 삭제해야 하기 때문에 만든 코드
            for(int i = 0 ; i < uriList.size() ; i++){//uriList와 클릭한 이미지 값을 비교한 후 같은 이미지 uri가 있으면 삭제
                if(((uriList.get(i)).toString()).equals(imagelist.get(position).getImg_path())){

                    Log.d(TAG, "onImageDeleteClick: before urilist: " + uriList.get(i));
                    Log.d(TAG, "onImageDeleteClick: before imagelist: " + imagelist.get(position).getImg_path());
                    uriList.remove(i);
                    break;
                }
            }

        //수정 된 이미지 삭제
        Log.d(TAG, "onImageDeleteClick: before uriUpdateList: " + uriUpdateList);
        Log.d(TAG, "onImageDeleteClick: before imgUpdateIdList: " + imgUpdateIdList);
        Log.d(TAG, "onImageDeleteClick: before imgIdList: " + imgIdList);
        for(int i = 0 ; i < imgUpdateIdList.size() ; i++){
            if(imgIdList.get(position).equals(imgUpdateIdList.get(i))){
                Log.d(TAG, "onImageDeleteClick: ?imgIdList.get(position): " + imgIdList.get(position));
                uriUpdateList.remove(i);
                imgUpdateIdList.remove(i);
                Log.d(TAG, "onImageDeleteClick: after uriUpdateList: " + uriUpdateList);
                Log.d(TAG, "onImageDeleteClick: after imgUpdateIdList: " + imgUpdateIdList);
                break;
            }
        }


        Log.d(TAG, "onImageDeleteClick: before imgIdList deleted: " + imgIdList);
        Log.d(TAG, "onImageDeleteClick: imagelist.get(position).getId(): " + imagelist.get(position).getId());
            //imagelist id(현재 선택 한 이미지의 아이디)와 imgidlist id(기존 이미지 저장한 이이디 리스트)에 아이디가 존재하면 해당 imgidlist 아이템 삭제
            for(int i = 0 ; i < imgIdList.size() ; i++){
                if(imgIdList.get(position) == imgIdList.get(i)){

                    imgIdList.remove(i);
                    Log.d(TAG, "onImageDeleteClick: after imgIdList deleted: " + imgIdList);
                    Log.d(TAG, "onImageDeleteClick: after imgUpdateIdList: " + imgUpdateIdList);
                }
            }



        //전체 이미지 목록중 아이템 삭제
        imagelist.remove(position);
        Log.d(TAG, "onImageDeleteClick: imagelist: " + imagelist);

        //데이터 변경 확인
        adapter.notifyDataSetChanged();
    }

    public void onImageAddClick(int position){

        Log.d(TAG, "onImageAddClick: clicked");
        if(imagelist.size() > 5){

            //notify user here...
            Toast.makeText(this, "최대 5개의 이미지만 선택 가능합니다.1", Toast.LENGTH_SHORT).show();
        }else{
            selectImage();
        }

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
            //단일 이미지 추가
            if(data.getData() != null) {
                Log.d(TAG, "onActivityResult: one image");


                Uri uri = data.getData();
                uriList.add(uri);

                if(imagelist.size() > 5){
                    //notify user here...
                    Toast.makeText(this, "최대 5개의 이미지만 선택 가능합니다.2", Toast.LENGTH_SHORT).show();

                }else{

                    //이미지가 추가되기 때문에 맨 마지막에 있는 이미지 더하기 버튼을 삭제해 둔다.
                    imagelist.remove(imagelist.size()-1);

                    BoardImageItem bii = new BoardImageItem(data.getData().toString());
                    imagelist.add(bii);

                    //마지막에 이미지 추가버튼을 다시 생성한다.
                    BoardImageItem biiAddButton = new BoardImageItem("addbutton");
                    imagelist.add(biiAddButton);

                    //삭제버튼이 제대로 갱신이 되지 않기 때문에 adapter를 새로 갱신해 준다.
                    adapter = new ImageListAdapter(this, imagelist);
                    adapter.setOnClickListener(BoardUpdateActivity.this);
                    recyclerView.setAdapter(adapter);
                    //데이터 변경 확인
                    adapter.notifyDataSetChanged();
                }

            }

            //다중 이미지 추가
            if(data.getClipData() != null) {

                ClipData clipData = data.getClipData();
                Log.d(TAG, "onActivityResult: clipdata: " + clipData);

                //이미지가 총 5개가 넘으면 이미지를 불러오지 않는다.
                Log.d(TAG, "onActivityResult: size?: " + (imagelist.size() + clipData.getItemCount()));
                if(imagelist.size() + clipData.getItemCount() > 6){//6인 이유는 마지막 포지션에 게시물 추가 이미지 아이템도 있기 때문이다.
                    //notify user here...
                    Toast.makeText(this, "최대 5개의 이미지만 선택 가능합니다.3", Toast.LENGTH_SHORT).show();

                }else {

                    //이미지가 추가되기 때문에 맨 마지막에 있는 이미지 더하기 버튼을 삭제해 둔다.
                    imagelist.remove(imagelist.size()-1);

                    //이미지를 추가한다.
                    for (int i = 0; i < data.getClipData().getItemCount(); i++) {

                        //clipdata에서 각각의 이미지 uri 꺼내기
                        Uri selectedImage = data.getClipData().getItemAt(i).getUri();
                        Log.e("uri", selectedImage + "");
                        uriList.add(selectedImage);

                        //클라이언트 이미지 갱신을 위함
                        BoardImageItem bii = new BoardImageItem(data.getClipData().getItemAt(i).getUri().toString());
                        imagelist.add(bii);
                    }

                    //이미지가 모두 추가되고 마지막에 이미지 추가버튼을 다시 생성한다.
                    BoardImageItem bii = new BoardImageItem("addbutton");
                    imagelist.add(bii);
                    Log.d(TAG, "onActivityResult: imagelist.size(): " + imagelist.size());

                    //삭제버튼이 제대로 갱신이 되지 않기 때문에 adapter를 새로 갱신해 준다.
                    adapter = new ImageListAdapter(this, imagelist);
                    adapter.setOnClickListener(BoardUpdateActivity.this);
                    recyclerView.setAdapter(adapter);
                    Log.d(TAG, "onActivityResult: imagepath: " + imagelist.get(imagelist.size()-2).getImg_path());
                    //데이터 변경 확인
                    adapter.notifyDataSetChanged();
                }
            }

            //이미지 수정(기존 이미지)
        }else if((requestCode== 2 && resultCode==RESULT_OK && data!=null)){

            if(data.getData() != null) {

                Log.d(TAG, "onActivityResult: uriList: " + uriList);
                Log.d(TAG, "onActivityResult: imgIdList: " + imgIdList);
                Log.d(TAG, "onActivityResult: before uriUpdateList: " + uriUpdateList);
                Log.d(TAG, "onActivityResult: before imgUpdateIdList: " + imgUpdateIdList);

                int cnt = 0;
                for(int i = 0 ; i < imgUpdateIdList.size() ; i++){

                    Log.d(TAG, "onActivityResult: imgUpdateIdList.get(): " + imgUpdateIdList.get(i));
                    Log.d(TAG, "onActivityResult: imgIdList.getid : " + imgIdList.get(imgpos));
                    //이미지를 또 변경 할 경우 리스트를 추가하지 않고 업데이트만 한다.
                    if(imgIdList.get(imgpos).equals(imgUpdateIdList.get(i))){
                        uriUpdateList.set(i, data.getData());
                        cnt = 1;
                        Log.d(TAG, "onActivityResult: !!!!!!!");
                        break;
                    }
                }

                //기존 이미지를 처음 변경할 경우
                if(cnt == 0){

                    //이미지 아이디와 이미지uri를 각 리스트에 추가
                    uriUpdateList.add(data.getData());
                    imgUpdateIdList.add(imagelist.get(imgpos).getId());
                }
                Log.d(TAG, "onActivityResult: cnt: " + cnt);

                Log.d(TAG, "onActivityResult: after uriUpdateList: " + uriUpdateList);
                Log.d(TAG, "onActivityResult: after imgUpdateIdList: " + imgUpdateIdList);


                //클라이언트 이미지 갱신용
                BoardImageItem bii = new BoardImageItem(data.getData().toString());
                Log.d(TAG, "onActivityResult: before image: " + imagelist.get(imgpos).getImg_path());
                imagelist.set(imgpos, bii);
                Log.d(TAG, "onActivityResult: after image: " + imagelist.get(imgpos).getImg_path());

                adapter = new ImageListAdapter(this, imagelist);
                adapter.setOnClickListener(BoardUpdateActivity.this);
                recyclerView.setAdapter(adapter);
                Log.d(TAG, "onActivityResult: imagepath: " + imagelist.get(imagelist.size()-2).getImg_path());
                //데이터 변경 확인
                adapter.notifyDataSetChanged();

                Log.d(TAG, "onActivityResult: uriList: " + uriList);
            }

            //uri 이미지 수정(새로 추가 한 이미지)
        }else if((requestCode== 3 && resultCode==RESULT_OK && data!=null)){

            if(data.getData() != null) {

                Log.d(TAG, "onActivityResult: imgpos: " + imgpos);
                Log.d(TAG, "onActivityResult: imgpos-imgIdList.size(): " + (imgpos-imgIdList.size()));

                //변경 전
                Log.d(TAG, "onActivityResult: before uriList: " + uriList);

                //새로 추가한 Uri 인덱스 가져오기
                //imgpos(현재 포지션)에서 imgIdList(기존 이미지 크기)를 빼면 uriList에 저장 된 해당 Uri 이미지를 선택할 수 있다.
                uriList.set((imgpos-imgIdList.size()), data.getData());

                //변경 후
                Log.d(TAG, "onActivityResult: after uriList: " + uriList);

                //클라이언트 이미지 갱신을 위함
                BoardImageItem bii = new BoardImageItem(data.getData().toString());
                Log.d(TAG, "onActivityResult: before image: " + imagelist.get(imgpos).getImg_path());
                imagelist.set(imgpos, bii);
                Log.d(TAG, "onActivityResult: after image: " + imagelist.get(imgpos).getImg_path());

                adapter = new ImageListAdapter(this, imagelist);
                adapter.setOnClickListener(BoardUpdateActivity.this);
                recyclerView.setAdapter(adapter);
                Log.d(TAG, "onActivityResult: imagepath: " + imagelist.get(imagelist.size()-2).getImg_path());
                //데이터 변경 확인
                adapter.notifyDataSetChanged();

                Log.d(TAG, "onActivityResult: uriList: " + uriList);
            }
        }
    }

    //다중 이미지 업로드
    private void updateImages(final List<Uri> uris){

        List<MultipartBody.Part> parts = new ArrayList<>();
        for(Uri uri: uris){
            Log.d(TAG, "uploadMultipleImages: uri: " + uri);

            //very important files[]
            MultipartBody.Part imageRequest = prepareFilePart("file[]", uri);
            parts.add(imageRequest);
        }

        //서버 통신 요청
        Api apiInterface = ApiClient.getClient().create(Api.class);
        Log.d(TAG, "updateImages: parts: " + parts);
        Log.d(TAG, "updateImages: imgUpdateIdList: " + imgUpdateIdList);
        Call<ResponseImages> call = apiInterface.uploadMultipleFilesDynamic2(parts, imgUpdateIdList);

        call.enqueue(new Callback<ResponseImages>() {
            @Override
            public void onResponse(Call<ResponseImages> call, Response<ResponseImages> response) {

                Log.e("onResponse updated: ",""+response.body().getResult());

                progressBar.setVisibility(View.VISIBLE);
                progressBar.setIndeterminate(true);
                setProgressDialog();

            }
            @Override
            public void onFailure(Call<ResponseImages> call, Throwable t) {

                Log.e("Server Response fail!: ",""+t.toString());
                finish();
            }
        });
    }


    //Uri 실제 기기 내 경로 가져오기
    private String getRealPathFromURI(Uri contentUri) {

        if (contentUri.getPath().startsWith("/storage")) {
            return contentUri.getPath();
        }

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
        } finally
        {
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
        Call<ResponseImages> call = apiInterface.uploadMultipleFilesDynamic(parts, id);

        call.enqueue(new Callback<ResponseImages>() {
            @Override
            public void onResponse(Call<ResponseImages> call, Response<ResponseImages> response) {

                progressBar.setVisibility(View.VISIBLE);
                progressBar.setIndeterminate(true);
                Log.e("onResponse getMessage: ",""+response.body());
                setProgressDialog();
            }
            @Override
            public void onFailure(Call<ResponseImages> call, Throwable t) {

                Log.e("Server Response fail!: ",""+t.toString());
                finish();
            }
        });
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
        Log.d(TAG, "onResume() called");

    }

    @Override
    protected void onPause() {
        super.onPause();
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
        finish();
    }
}
