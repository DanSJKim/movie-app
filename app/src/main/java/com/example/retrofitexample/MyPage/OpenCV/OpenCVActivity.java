package com.example.retrofitexample.MyPage.OpenCV;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.example.retrofitexample.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Semaphore;


public class OpenCVActivity extends AppCompatActivity
        implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "opencv";
    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat matInput;
    private Mat matResult;

    //수정 4.  기존 코드를 주석처리하고 cpp에 추가할 jni함수를 위한 네이티브 메소드 선언(노란색)을 추가합니다.
    //public native void ConvertRGBtoGray(long matAddrInput, long matAddrResult);
    public native long loadCascade(String cascadeFileName );
    public native void detect(long cascadeClassifier_face,

                              long cascadeClassifier_eye, long matAddrInput, long matAddrResult);
    public long cascadeClassifier_face = 0;
    public long cascadeClassifier_eye = 0;
    //


    // 3-2. MainActivity.java 파일에 세마포어를 사용하기 위한 코드를 추가합니다.
    private final Semaphore writeLock = new Semaphore(1);

    public void getWriteLock() throws InterruptedException {
        Log.d(TAG, "getWriteLock: writeLock: " + writeLock);
        writeLock.acquire();
    }

    public void releaseWriteLock() {
        Log.d(TAG, "releaseWriteLock: writeLock: " + writeLock);
        writeLock.release();
    }



    static {
        System.loadLibrary("opencv_java4");
        System.loadLibrary("native-lib");
    }

    //수정 5.  xml 파일을 가져오기 위한 메소드를 추가합니다.
    //
    //cpp 파일의 loadCascade 함수를 호출하도록 구현되어있는데 자바 함수를 사용하도록 변경해도 됩니다.
    //
    //현재 이미 파일을 copyFile 메소드를 이용해서 가져온 경우에 대한 처리가 빠져있습니다.
    private void copyFile(String filename) {
        String baseDir = Environment.getExternalStorageDirectory().getPath();
        String pathDir = baseDir + File.separator + filename;

        AssetManager assetManager = this.getAssets();

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            Log.d( TAG, "copyFile :: 다음 경로로 파일복사 "+ pathDir);
            inputStream = assetManager.open(filename);
            outputStream = new FileOutputStream(pathDir);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            inputStream.close();
            inputStream = null;
            outputStream.flush();
            outputStream.close();
            outputStream = null;
        } catch (Exception e) {
            Log.d(TAG, "copyFile :: 파일 복사 중 예외 발생 "+e.toString() );
        }

    }

    private void read_cascade_file(){
        copyFile("haarcascade_frontalface_alt.xml");
        copyFile("haarcascade_eye_tree_eyeglasses.xml");

        Log.d(TAG, "read_cascade_file:");

        cascadeClassifier_face = loadCascade( "haarcascade_frontalface_alt.xml");
        Log.d(TAG, "read_cascade_file:");

        cascadeClassifier_eye = loadCascade( "haarcascade_eye_tree_eyeglasses.xml");
    }




    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_open_cv);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //퍼미션 상태 확인
            if (!hasPermissions(PERMISSIONS)) {

                //퍼미션 허가 안되어있다면 사용자에게 요청
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
            //수정 3.  기존 코드(흰색)를 주석처리하고 퍼미션이 이미 허가된 경우의 처리를 위한 코드를 추가하고 사용하는 카메라를 전면 카메라로 바꾸기 위한 코드를 수정합니다.
            else  read_cascade_file(); //추가
        }
        //수정 3.  기존 코드(흰색)를 주석처리하고 퍼미션이 이미 허가된 경우의 처리를 위한 코드를 추가하고 사용하는 카메라를 전면 카메라로 바꾸기 위한 코드를 수정합니다.
        else  read_cascade_file(); //추가

        mOpenCvCameraView = (CameraBridgeViewBase)findViewById(R.id.activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        // 기존 코드 주석 처리
        //mOpenCvCameraView.setCameraIndex(0); // front-camera(1),  back-camera(0)
        //수정 3.  기존 코드(흰색)를 주석처리하고 퍼미션이 이미 허가된 경우의 처리를 위한 코드를 추가하고 사용하는 카메라를 전면 카메라로 바꾸기 위한 코드를 수정합니다.
        mOpenCvCameraView.setCameraIndex(1); // front-camera(1),  back-camera(0)

        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);


        //3-4. 버튼 클릭시 사진을 저장하기 위한 코드를 onCreate 메소드에 추가합니다. 
        Button button = (Button)findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                try {
                    Log.d(TAG, "onClick: ");
                    getWriteLock();

                    File path = new File(Environment.getExternalStorageDirectory() + "/Images/");
                    path.mkdirs();
                    File file = new File(path, "image.jpg");

                    String filename = file.toString();

                    Imgproc.cvtColor(matResult, matResult, Imgproc.COLOR_BGR2RGBA);
                    boolean ret  = Imgcodecs.imwrite( filename, matResult);
                    if ( ret ) Log.d(TAG, "SUCESS");
                    else Log.d(TAG, "FAIL");


                    Intent mediaScanIntent = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    mediaScanIntent.setData(Uri.fromFile(file));
                    sendBroadcast(mediaScanIntent);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                releaseWriteLock();

            }
        });
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "onResume :: Internal OpenCV library not found.");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "onResum :: OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();

        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    // 카메라로부터 캡쳐된 영상을 onCameraFrame에서 받는다.
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        //3-3. 기존 코드에서 카메라 접근하는 부분을 세마포어로 둘러싸둡니다.
        try {
            Log.d(TAG, "onCameraFrame: ");
            getWriteLock();

            matInput = inputFrame.rgba();

            //if ( matResult != null ) matResult.release(); fix 2018. 8. 18

            if ( matResult == null ){
                matResult = new Mat(matInput.rows(), matInput.cols(), matInput.type());
                Log.d(TAG, "onCameraFrame: matInput.rows(): " + matInput.rows());
                Log.d(TAG, "onCameraFrame: matInput.cols(): " + matInput.cols());
                Log.d(TAG, "onCameraFrame: matInput.type(): " + matInput.type());
            }

            //수정 6. 기존 코드(흰색)를 주석처리하고 얼굴 검출하는 cpp 코드를 호출하는 코드를 추가합니다.
            //
            //카메라로부터 영상을 가져올 때마다 호출되는 onCameraFrame 메소드에서  jni 함수 detect를 호출하도록 합니다.
            // 기존 코드 주석 처리
            //ConvertRGBtoGray(matInput.getNativeObjAddr(), matResult.getNativeObjAddr());
            Core.flip(matInput, matInput, 1);
            Log.d(TAG, "onCameraFrame: matInput: " + matInput);

            Log.d(TAG, "onCameraFrame: cascadeClassifier_face: " + cascadeClassifier_face);
            Log.d(TAG, "onCameraFrame: cascadeClassifier_eye: " + cascadeClassifier_eye);
            Log.d(TAG, "onCameraFrame: matInput.getNativeObjAddr(): " + matInput.getNativeObjAddr());
            Log.d(TAG, "onCameraFrame: matResult.getNativeObjAddr(): " + matResult.getNativeObjAddr());

            detect(cascadeClassifier_face,cascadeClassifier_eye, matInput.getNativeObjAddr(),
                    matResult.getNativeObjAddr());

        } catch (InterruptedException e) {
            e.printStackTrace(); //3-3. 기존 코드에서 카메라 접근하는 부분을 세마포어로 둘러싸둡니다.
        }

        releaseWriteLock();

        return matResult; // 결과를 저장하는 Mat 객체, 자바 코드에서 matResult 객체를 리턴하면 그레이스케일 영상이 화면에 보여 진다.
    }



    //여기서부턴 퍼미션 관련 메소드
    static final int PERMISSIONS_REQUEST_CODE = 1000;
    //String[] PERMISSIONS  = {"android.permission.CAMERA"};
    // 수정 1. 기존 코드(흰색)를 주석 처리하고 외장 저장소에 파일 저장하기 위한 퍼미션(노란색)을 추가합니다.
    String[] PERMISSIONS  = {"android.permission.CAMERA",
            "android.permission.WRITE_EXTERNAL_STORAGE"};


    private boolean hasPermissions(String[] permissions) {
        int result;

        //스트링 배열에 있는 퍼미션들의 허가 상태 여부 확인
        for (String perms : permissions){

            result = ContextCompat.checkSelfPermission(this, perms);

            if (result == PackageManager.PERMISSION_DENIED){
                //허가 안된 퍼미션 발견
                return false;
            }
        }

        //모든 퍼미션이 허가되었음
        return true;
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode){

            case PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean cameraPermissionAccepted = grantResults[0]
                            == PackageManager.PERMISSION_GRANTED;

//                    if (!cameraPermissionAccepted)
//                        showDialogForPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");

                    // 수정 2.   기존 코드(흰색)를 주석처리하고 WRITE_EXTERNAL_STORAGE 퍼미션을 위해 필요한 코드(노란색)를 추가합니다.
                    boolean writePermissionAccepted = grantResults[1]
                            == PackageManager.PERMISSION_GRANTED;

                    if (!cameraPermissionAccepted || !writePermissionAccepted) {
                        showDialogForPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");
                        return;
                    }else
                    {
                        read_cascade_file();
                    }
                }
                break;
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void showDialogForPermission(String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder( OpenCVActivity.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id){
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                finish();
            }
        });
        builder.create().show();
    }


}

