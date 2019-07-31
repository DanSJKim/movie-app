package com.example.retrofitexample.Map;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.nfc.Tag;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.retrofitexample.BoxOffice.ProfileActivity;
import com.example.retrofitexample.LoginRegister.MainActivity;
import com.example.retrofitexample.MovieSearch.MovieSearchActivity;
import com.example.retrofitexample.R;
import com.example.retrofitexample.Retrofit.Api;
import com.example.retrofitexample.Retrofit.ApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.MarkerManager;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MapActivity extends AppCompatActivity
        implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {


    /**
     * 지도
     */

    private GoogleMap mGoogleMap = null;
    private Marker currentMarker = null; //처음 실행 시 표기하는 기본 마커. 서울시청

    private static final String TAG = "googlemap_example";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int UPDATE_INTERVAL_MS = 1000;  // 1초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500; // 0.5초


    // onRequestPermissionsResult에서 수신된 결과에서 ActivityCompat.requestPermissions를 사용한 퍼미션 요청을 구별하기 위해 사용됩니다.
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    boolean needRequest = false;


    // 앱을 실행하기 위해 필요한 퍼미션을 정의합니다.
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};  // 외부 저장소


    Location mCurrentLocatiion;
    LatLng currentPosition;


    private FusedLocationProviderClient mFusedLocationClient; //나의 위치를 알기 위해
    private LocationRequest locationRequest;
    private Location location;


    private View mLayout;  // Snackbar 사용하기 위해서는 View가 필요합니다.
    // (참고로 Toast에서는 Context가 필요했습니다.)

    int cnt = 0; // 카메라 이동을 처음 재생 시에만 하기 위해 만든 변수.

    Button btnNearTheaters;

    private ArrayList<Theater> mArrayList; //받아온 영화관 리스트

    double distance = 0; // 설정한 거리

    Button btnFind; // 영화관 찾기 버튼

    int markerPosition; // 마커 정보창에 영화관 정보 넣기 위한 포지션값

    ImageView ivHome, ivSearch, ivBoard, ivChat; // 메뉴 탭

    //클러스터 변수 선언
    private ClusterManager<MyItem> myItemClusterManager;
    private MyItem clickedClusterItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_map);

        final TextView tvShowDistance = (TextView) findViewById(R.id.tvShowDistance);

        //맵액티비티 레이아웃
        mLayout = findViewById(R.id.layout_main);

        Log.d(TAG, "onCreate");

        ivHome = (ImageView) findViewById(R.id.ivHome);
        ivHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapActivity.this, ProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        btnNearTheaters = findViewById(R.id.btnNear);
        btnNearTheaters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: currentMarker: " + currentMarker);
                if (currentMarker != null) currentMarker.remove();

                myItemClusterManager.clearItems();
                mGoogleMap.clear(); // 모든 마커 삭제
                Log.d(TAG, "onClick: getposition: " + currentMarker.getPosition());
            }
        });

        btnFind = (Button) findViewById(R.id.btnFind);
        btnFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "onClick: currentMarker: " + currentMarker);
                Log.d(TAG, "onClick: getLatitude: " + location.getLatitude());
                Log.d(TAG, "onClick: getLongitude: " + location.getLongitude());

                listTheaters(location.getLatitude(), location.getLongitude(), distance); // 영화관 목록 마커 출력
            }
        });


        locationRequest = new LocationRequest()
                // setPriority 메소드: Google Play 의 위치 서비스를 사용하는데 중요한 힌트인, 요청 우선순위를 설정한다
                // PRIORITY_HIGH_ACCURACY: 가장 정확한 위치를 요청하고 싶을때 사용. 이 설정으로 하면, 위치 서비스는 GPS를 사용하여 위치를 결정한다.
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                // setInterval: 위치 업데이트 수신간격을 밀리초단위로 설정한다.
                .setInterval(UPDATE_INTERVAL_MS)
                // setFastestInterval: 앱에서 위치 업데이트를 가장 빠르게 처리할 수 있도록 밀리초 단위로 설정한다.
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);
        Log.d(TAG, "onCreate: locationRequest: ");


        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder();

        // Gps가 꺼져있다면 LocationSettingsRequest로 설정창을 띄울 수 있다.
        builder.addLocationRequest(locationRequest);
        Log.d(TAG, "onCreate: builder ");


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        SeekBar seekBar = (SeekBar) findViewById(R.id.sbDistance);
        seekBar.setMax(200);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Log.d(TAG, "onProgressChanged: i: " + i);

                if (i < 10) {
                    tvShowDistance.setText("거리: " + i * 100 + "m");
                    //double per = Double.parseDouble(String.format("%.2f",i));

                    distance = i / 10.0;
                } else {
                    tvShowDistance.setText("거리: " + i / 10 + "." + i % 10 + "km");
                    distance = i / 10.0;
                }


                Log.d(TAG, "onProgressChanged: distance: " + distance);

//                // 반경 1KM원
//                CircleOptions circle1KM = new CircleOptions().center(currentPosition) //원점
//                        .radius(i*100)      //반지름 단위 : m
//                        .strokeWidth(0f)  //선너비 0f : 선없음
//                        .fillColor(Color.parseColor("#880000ff")); //배경색
//
//                //원추가
//                mGoogleMap.addCircle(circle1KM);


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


    }


    // 장치 위치가 변경되었거나 더 이상 결정할 수없는 경우  FusedLocationProviderClient에서 알림을 수신하는 데 사용됩니다.
    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            List<Location> locationList = locationResult.getLocations();

            Log.d(TAG, "onLocationResult: locationList.size(): " + locationList.size());
            if (locationList.size() > 0) {
                location = locationList.get(locationList.size() - 1);
                //location = locationList.get(0);

                currentPosition
                        = new LatLng(location.getLatitude(), location.getLongitude());

                String markerTitle = getCurrentAddress(currentPosition);
                String markerSnippet = "위도:" + String.valueOf(location.getLatitude())
                        + " 경도:" + String.valueOf(location.getLongitude());

                Log.d(TAG, "onLocationResult : " + markerSnippet);

                //현재 위치에 마커 생성하고 이동
                if (cnt == 0) {
                    setCurrentLocation(location, markerTitle, markerSnippet);
                    //getLocation(location.getLatitude(), location.getLongitude(), 500);
                }

                cnt = 1;// 카메라 이동 한번만 시키고 변수를 1로 바꿔준다.

                mCurrentLocatiion = location;
            }
        }
    };


    //위치 업데이트
    private void startLocationUpdates() {

        if (!checkLocationServicesStatus()) {

            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting");

            //GPS 활성화를 위한 메소드
            showDialogForLocationServiceSetting();
        } else {

            int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION);


            if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED ||
                    hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED) {

                Log.d(TAG, "startLocationUpdates : 퍼미션 안가지고 있음");
                return;
            }

            Log.d(TAG, "startLocationUpdates : call mFusedLocationClient.requestLocationUpdates");

            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

            if (checkPermission())
                mGoogleMap.setMyLocationEnabled(true);

        }

    }


    //지도를 사용할 준비가되면 호출됩니다.
    @Override
    public void onMapReady(GoogleMap googleMap) {

        Log.d(TAG, "onMapReady :");

        mGoogleMap = googleMap;


        //런타임 퍼미션 요청 대화상자나 GPS 활성 요청 대화상자 보이기전에
        //지도의 초기위치를 서울로 이동
        setDefaultLocation();

        // 클러스터 매니저 생성
        myItemClusterManager = new ClusterManager<>(MapActivity.this, mGoogleMap);

        mGoogleMap.setOnCameraIdleListener(myItemClusterManager);
        mGoogleMap.setOnMarkerClickListener(myItemClusterManager);

        //처음 지도 실행 시 내 위치 아이콘과 GPS버튼이 나오지 않아서 설정한 코드
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mGoogleMap.setMyLocationEnabled(true); //처음 지도 실행 시 내 위치 아이콘과 GPS버튼이 나오지 않아서 설정한 코드
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);

        Log.d(TAG, "onMapReady: setMyLocationButtonEnabled: " + mGoogleMap.getUiSettings());
        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {

                Log.d(TAG, "onMapClick :");
            }
        });


    }


    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "onStart");

        if (checkPermission()) {

            Log.d(TAG, "onStart : call mFusedLocationClient.requestLocationUpdates");
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

            if (mGoogleMap != null)
                mGoogleMap.setMyLocationEnabled(true);

        }
    }


    @Override
    protected void onStop() {

        super.onStop();

        if (mFusedLocationClient != null) {

            Log.d(TAG, "onStop : call stopLocationUpdates");
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }


    public String getCurrentAddress(LatLng latlng) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);
        } catch (IOException ioException) {

            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {

            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }


        if (addresses == null || addresses.size() == 0) {
            //Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }

    }


    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {

        if (currentMarker != null) currentMarker.remove();

        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentLatLng);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);

        //currentMarker = mGoogleMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng); //카메라 이동 설정
        Log.d(TAG, "setCurrentLocation: currentLatLng: " + currentLatLng);
        mGoogleMap.moveCamera(cameraUpdate);

    }


    public void setDefaultLocation() {


        //디폴트 위치, Seoul
        LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
        String markerTitle = "위치정보 가져올 수 없음";
        String markerSnippet = "위치 퍼미션과 GPS 활성 요부 확인하세요";

        if (currentMarker != null) currentMarker.remove();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker = mGoogleMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
        mGoogleMap.moveCamera(cameraUpdate);
    }


    //여기부터 런타임 퍼미션 처리을 위한 메소드들
    private boolean checkPermission() {

        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        return false;

    }


    /*
     * ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드
     */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면

            boolean check_result = true;


            // 모든 퍼미션을 허용했는지 체크합니다.

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if (check_result) {

                // 퍼미션을 허용했다면 위치 업데이트를 시작합니다.
                startLocationUpdates();
            } else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {


                    // 사용자가 거부만 선택한 경우에는 앱을 다시 실행하여 허용을 선택하면 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            finish();
                        }
                    }).show();

                } else {


                    // "다시 묻지 않음"을 사용자가 체크하고 거부를 선택한 경우에는 설정(앱 정보)에서 퍼미션을 허용해야 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            finish();
                        }
                    }).show();
                }
            }

        }
    }


    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {
        Log.d(TAG, "showDialogForLocationServiceSetting: ");

        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d(TAG, "onActivityResult : GPS 활성화 되있음");


                        needRequest = true;

                        return;
                    }
                }
                break;
        }
    }


    private void listTheaters(double lat, double lng, double distance) {

        Api api = ApiClient.getClient().create(Api.class);

        //받아온 값 확인
        Log.d(TAG, "listTheaters: lat: " + lat);
        Log.d(TAG, "listTheaters: lng: " + lng);
        Log.d(TAG, "listTheaters: distance: " + distance);

        //영화관 리스트 전체 받아오기
        Call<TheaterResponse> call = api.listTheaters(lat, lng, distance);
        call.enqueue(new Callback<TheaterResponse>() {
            @Override
            public void onResponse(Call<TheaterResponse> call, final Response<TheaterResponse> response) {

                Log.d(TAG, "onResponse: response.body().getTheaterlist().length: " + response.body().getTheaterlist().length);

                // 거리 설정에 따라 서버에서 불러 온 영화관의 개수
                int theaterlistcount = response.body().getTheaterlist().length;

                if (currentMarker != null) currentMarker.remove();

                mGoogleMap.clear(); // 모든 마커 삭제
                myItemClusterManager.clearItems(); // 모든 마커 삭제


                //배열을 arraylist로 변환
                mArrayList = new ArrayList<>(Arrays.asList(response.body().getTheaterlist()));
                for (int i = 0; i < theaterlistcount; i++) {

                    markerPosition = i;

                    Log.d(TAG, "onResponse: theaterlistcount: " + theaterlistcount);

                    //영화관 아이디
                    int id = mArrayList.get(i).getTheaterid();
                    Log.d(TAG, "onResponse: id: " + id);

                    //영화관 이름
                    final String name = mArrayList.get(i).getTheaterName();
                    Log.d(TAG, "onResponse: name: " + name);

                    //영화관 도로명 주소
                    String address = mArrayList.get(i).getTheaterAddress();

                    //영화관 url
                    String url = mArrayList.get(i).getTheaterUrl();
                    Log.d(TAG, "onResponse: url: " + url);

                    //영화관 위도
                    String latitude = mArrayList.get(i).getTheaterLatitude();
                    double latvalue = Double.parseDouble(latitude); // 위도 변환
                    Log.d(TAG, "onResponse: latitude: " + latvalue);

                    //영화관 경도
                    String longitude = mArrayList.get(i).getTheaterLongitude();
                    double longvalue = Double.parseDouble(longitude); // 경도 변환
                    Log.d(TAG, "onResponse: longitude: " + longvalue);

                    String info = address;
//                    // 1. 마커 옵션 설정 (만드는 과정)
//                    MarkerOptions markerOptions = new MarkerOptions();
//                    markerOptions // LatLng에 대한 어레이를 만들어서 이용할 수도 있다.
//                            .position(new LatLng(latvalue, longvalue))
//                            .title(name)
//                            .snippet(info); // 타이틀.



                    if(name.contains("CGV")){
                        MyItem item = new MyItem(latvalue, longvalue, name, address, R.drawable.cgv); // 위도, 경도, 영화관 이름, 영화관 주소
                        myItemClusterManager.cluster(); // 찾기 버튼 눌렀을 때 마커들이 바로 표시되게
                        myItemClusterManager.setRenderer(new ItemRenderer());
                        myItemClusterManager.addItem(item);
                    }else if(name.contains("메가박스")){
                        MyItem item = new MyItem(latvalue, longvalue, name, address, R.drawable.megabox); // 위도, 경도, 영화관 이름, 영화관 주소
                        myItemClusterManager.cluster(); // 찾기 버튼 눌렀을 때 마커들이 바로 표시되게
                        myItemClusterManager.setRenderer(new ItemRenderer());
                        myItemClusterManager.addItem(item);
                    }else if(name.contains("롯데시네마")){
                        MyItem item = new MyItem(latvalue, longvalue, name, address, R.drawable.lottecinema); // 위도, 경도, 영화관 이름, 영화관 주소
                        myItemClusterManager.cluster(); // 찾기 버튼 눌렀을 때 마커들이 바로 표시되게
                        myItemClusterManager.setRenderer(new ItemRenderer());
                        myItemClusterManager.addItem(item);
                    }else{
                        MyItem item = new MyItem(latvalue, longvalue, name, address, R.drawable.theater); // 위도, 경도, 영화관 이름, 영화관 주소
                        myItemClusterManager.cluster(); // 찾기 버튼 눌렀을 때 마커들이 바로 표시되게
                        myItemClusterManager.setRenderer(new ItemRenderer());
                        myItemClusterManager.addItem(item);
                    }



                    // 2. 마커 생성 (마커를 나타냄)
                    //mGoogleMap.addMarker(markerOptions);

                    //CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(LOCATION, 15);
                    //mGoogleMap.moveCamera(cameraUpdate);
                }

                //정보창 클릭 리스너
                GoogleMap.OnInfoWindowClickListener infoWindowClickListener = new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        //마커 고유 아이디
                        String markerId = marker.getId();
                        Log.d(TAG, "onInfoWindowClick: title: " + marker.getTitle());
                        Log.d(TAG, "onInfoWindowClick: snippet: " + marker.getSnippet());

                        //화면 넘어가기
                        Intent intent = new Intent(MapActivity.this, TheaterDetailActivity.class);
                        intent.putExtra("theaterName", marker.getTitle());
                        intent.putExtra("theaterAddress", marker.getSnippet());

                        startActivity(intent);

                        Toast.makeText(MapActivity.this, "정보창 클릭 Marker ID : "+markerId, Toast.LENGTH_SHORT).show();
                    }
                };

                mGoogleMap.setOnInfoWindowClickListener(infoWindowClickListener);
            }

            @Override
            public void onFailure(Call<TheaterResponse> call, Throwable t) {
                Log.d("Error",t.getMessage());
            }
        });
    }


    /**
     * Draws profile photos inside markers (using IconGenerator).
     * When there are multiple people in the cluster, draw multiple photos (using MultiDrawable).
     */
    private class ItemRenderer extends DefaultClusterRenderer<MyItem> {
        private final IconGenerator mIconGenerator = new IconGenerator(getApplicationContext());
        private final IconGenerator mClusterIconGenerator = new IconGenerator(getApplicationContext());
        private final ImageView mImageView;
        private final ImageView mClusterImageView;
        private final int mDimension;

        public ItemRenderer() {
            super(getApplicationContext(), mGoogleMap, myItemClusterManager);

            View multiProfile = getLayoutInflater().inflate(R.layout.multi_profile, null);
            mClusterIconGenerator.setContentView(multiProfile);
            mClusterImageView = (ImageView) multiProfile.findViewById(R.id.image);

            mImageView = new ImageView(getApplicationContext());
            //getDimension: 특정 리소스 아이디에 대한 dimension을 가져온다.
            mDimension = (int) getResources().getDimension(R.dimen.custom_profile_image);
            mIconGenerator.setContentView(mImageView);
        }

        @Override
        protected void onBeforeClusterItemRendered(MyItem person, MarkerOptions markerOptions) {

            // Draw a single person.
            // Set the info window to show their name.
            mImageView.setImageResource(person.profilePhoto);
            Bitmap icon = mIconGenerator.makeIcon();
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<MyItem> cluster, MarkerOptions markerOptions) {
            // Draw multiple people.
            // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
            List<Drawable> profilePhotos = new ArrayList<Drawable>(Math.min(4, cluster.getSize()));
            int width = mDimension;
            int height = mDimension;

            for (MyItem p : cluster.getItems()) {
                // Draw 4 at most.
                if (profilePhotos.size() == 4) break;
                Drawable drawable = getResources().getDrawable(p.profilePhoto);
                drawable.setBounds(0, 0, width, height);
                profilePhotos.add(drawable);
            }
            MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
            multiDrawable.setBounds(0, 0, width, height);

            mClusterImageView.setImageDrawable(multiDrawable);
            Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {

            // Always render clusters.
            return cluster.getSize() > 1;
        }
    }


}