package com.example.retrofitexample.MyPage.ImageFilters;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.retrofitexample.MyPage.ImageFilters.utils.BitmapUtils;
import com.example.retrofitexample.R;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.imageprocessors.subfilters.BrightnessSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.ContrastSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.SaturationSubfilter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ImageFiltersActivity extends AppCompatActivity implements FiltersListFragment.FiltersListFragmentListener {

    private static final String TAG = ImageFiltersActivity.class.getSimpleName();

    public static final String IMAGE_NAME = "dog.jpg";

    public static final int SELECT_GALLERY_IMAGE = 101;

    @BindView(R.id.image_preview)
    ImageView imagePreview;

    @BindView(R.id.tabs)
    TabLayout tabLayout;

    @BindView(R.id.viewpager)
    ViewPager viewPager;

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;

    Bitmap originalImage;
    // to backup image with filter applied
    Bitmap filteredImage;

    // the final image after applying
    // brightness, saturation, contrast
    Bitmap finalImage;

    FiltersListFragment filtersListFragment;

    // modified image values
    int brightnessFinal = 0;
    float saturationFinal = 1.0f;
    float contrastFinal = 1.0f;

    // load native image filters library
    static {
        // System.loadLibrary(“NativeImageProcessor”) is called to initialize the native library.
        // 네이티브 라이브러리를 초기화하기 위해 System.loadLibrary (“NativeImageProcessor”)가 호출됩니다.
        System.loadLibrary("NativeImageProcessor");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_filters);
        ButterKnife.bind(this);
        Log.d(TAG, "onCreate: ");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.activity_title_main));

        loadImage();

        ////FiltersListFragment가 setupViewPager() 메서드의 ViewPager에 추가 됨.
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
    }


    //FiltersListFragment가 setupViewPager() 메서드의 ViewPager에 추가 됨.
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        Log.d(TAG, "setupViewPager: viewPager: " + viewPager);
        // adding filter list fragment
        filtersListFragment = new FiltersListFragment();
        filtersListFragment.setListener(this);


        adapter.addFragment(filtersListFragment, getString(R.string.tab_filters));

        viewPager.setAdapter(adapter);
    }


    // onFilterSelected() will be called when a filter is selected in FiltersListFragment.
    // The selected filter is processed and the final image is displayed imagePreview.
    // onFilterSelected ()는 FiltersListFragment에서 필터를 선택하면 호출됩니다.
    // 선택한 필터가 처리되고 최종 이미지가 imagePreview로 표시됩니다.
    @Override
    public void onFilterSelected(Filter filter) {
        Log.d(TAG, "onFilterSelected: ");
        // reset image controls
        //resetControls();

        // applying the selected filter
        filteredImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
        // preview filtered image
        imagePreview.setImageBitmap(filter.processFilter(filteredImage));

        finalImage = filteredImage.copy(Bitmap.Config.ARGB_8888, true);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
            Log.d(TAG, "ViewPagerAdapter: manager: " + manager);
        }

        @Override
        public Fragment getItem(int position) {
            Log.d(TAG, "getItem: " + mFragmentList.get(position));
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            Log.d(TAG, "getCount: " + mFragmentList.size());
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            Log.d(TAG, "addFragment: fragment: " + fragment);
            Log.d(TAG, "addFragment: title: " + title);
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Log.d(TAG, "getPageTitle: mFragmentTitleList.get(position): " + mFragmentTitleList.get(position));
            return mFragmentTitleList.get(position);
        }
    }

    // load the default image from assets on app launch
    private void loadImage() {
        Log.d(TAG, "loadImage: 앱 실행 시에 기본 이미지를 assets에서 불러 온다.");
        Log.d(TAG, "loadImage: IMAGE_NAME: " + IMAGE_NAME);
        originalImage = BitmapUtils.getBitmapFromAssets(this, IMAGE_NAME, 300, 300);
        Log.d(TAG, "loadImage: originalImage: " + originalImage);
        filteredImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
        Log.d(TAG, "loadImage: filteredImage: " + filteredImage);
        finalImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
        Log.d(TAG, "loadImage: finalImage: " + finalImage);
        imagePreview.setImageBitmap(originalImage);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        Log.d(TAG, "onCreateOptionsMenu: ");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_open) {
            Log.d(TAG, "onOptionsItemSelected: action_open id: " + id);
            openImageFromGallery();
            return true;
        }

        if (id == R.id.action_save) {
            Log.d(TAG, "onOptionsItemSelected: action_save id: " + id);
            saveImageToGallery();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == SELECT_GALLERY_IMAGE) {
            Bitmap bitmap = BitmapUtils.getBitmapFromGallery(this, data.getData(), 800, 800);

            Log.d(TAG, "onActivityResult: data: " + data);
            
            // clear bitmap memory
            originalImage.recycle();
            finalImage.recycle();
            finalImage.recycle();

            originalImage = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            filteredImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
            finalImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
            imagePreview.setImageBitmap(originalImage);
            bitmap.recycle();

            // render selected image thumbnails
            filtersListFragment.prepareThumbnail(originalImage);
        }
    }

    private void openImageFromGallery() {
        Dexter.withActivity(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            Log.d(TAG, "onPermissionsChecked: " + report);
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType("image/*");
                            startActivityForResult(intent, SELECT_GALLERY_IMAGE);
                        } else {
                            Log.d(TAG, "onPermissionsChecked: else: " + report);
                            Toast.makeText(getApplicationContext(), "Permissions are not granted!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        Log.d(TAG, "onPermissionRationaleShouldBeShown: ");
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    /*
     * saves image to camera gallery
     * */
    private void saveImageToGallery() {
        Dexter.withActivity(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            final String path = BitmapUtils.insertImage(getContentResolver(), finalImage, System.currentTimeMillis() + "_profile.jpg", null);
                            Log.d(TAG, "onPermissionsChecked: ");
                            if (!TextUtils.isEmpty(path)) {
                                Snackbar snackbar = Snackbar
                                        .make(coordinatorLayout, "Image saved to gallery!", Snackbar.LENGTH_LONG)
                                        .setAction("OPEN", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Log.d(TAG, "onClick: ");
                                                openImage(path);
                                            }
                                        });

                                snackbar.show();
                            } else {
                                Snackbar snackbar = Snackbar
                                        .make(coordinatorLayout, "Unable to save image!", Snackbar.LENGTH_LONG);
                                Log.d(TAG, "onPermissionsChecked: else ");
                                snackbar.show();
                            }
                        } else {
                            Log.d(TAG, "onPermissionsChecked: permissions are not granted");
                            Toast.makeText(getApplicationContext(), "Permissions are not granted!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        Log.d(TAG, "onPermissionRationaleShouldBeShown: token: " + token);
                        token.continuePermissionRequest();
                    }
                }).check();

    }

    // opening image in default image viewer app
    private void openImage(String path) {
        Log.d(TAG, "openImage: ");
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(path), "image/*");
        startActivity(intent);
    }
}