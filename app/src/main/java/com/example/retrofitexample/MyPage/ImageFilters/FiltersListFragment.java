package com.example.retrofitexample.MyPage.ImageFilters;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.retrofitexample.MyPage.ImageFilters.utils.BitmapUtils;
import com.example.retrofitexample.MyPage.ImageFilters.utils.SpacesItemDecoration;
import com.example.retrofitexample.R;
import com.zomato.photofilters.FilterPack;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.utils.ThumbnailItem;
import com.zomato.photofilters.utils.ThumbnailsManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Now we’ll create a Fragment class to render the filtered image thumbnails in horizontal list.
 * To achieve this, we need a RecyclerView and provide list of thumbnail images to adapter class.
 * 이제 필터링 된 이미지 축소판을 가로 목록으로 렌더링하는 Fragment 클래스를 만듭니다.
 * 이를 위해서는 RecyclerView가 필요하며 썸네일 이미지 목록을 어댑터 클래스에 제공하십시오.
 * 선택한 이미지 필터의 실제 처리는 ImageFiltersActivity에서 처리됩니다.
 */
public class FiltersListFragment extends Fragment implements ThumbnailsAdapter.ThumbnailsAdapterListener {
    @BindView(R.id.recycler_view)

    private static final String TAG = "FiltersListFragment";
    RecyclerView recyclerView;

    ThumbnailsAdapter mAdapter;

    List<ThumbnailItem> thumbnailItemList;

    FiltersListFragmentListener listener;

    public void setListener(FiltersListFragmentListener listener) {
        this.listener = listener;
    }

    public FiltersListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_filters_list, container, false);

        Log.d(TAG, "onCreateView: ");

        ButterKnife.bind(this, view);

        thumbnailItemList = new ArrayList<>();
        mAdapter = new ThumbnailsAdapter(getActivity(), thumbnailItemList, this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        int space = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8,
                getResources().getDisplayMetrics());
        recyclerView.addItemDecoration(new SpacesItemDecoration(space));
        recyclerView.setAdapter(mAdapter);

        prepareThumbnail(null);

        return view;
    }

    /**
     * Renders thumbnails in horizontal list
     * loads default image from Assets if passed param is null
     *
     * @param bitmap
     */
    // In prepareThumbnail() method the filters are lopped through and each thumbnail item is added to ThumbnailsManager to process them.
    // The processed thumbnails are added back to thumbnailItemList which is the data resource for the RecyclerView.
    // PreparingThumbnail () 메서드에서 필터를 잘라 내고 각 축소판 항목을 처리하기 위해 ThumbnailsManager에 추가합니다.
    // 처리 된 썸네일은 RecyclerView의 데이터 리소스 인 thumbnailItemList에 다시 추가됩니다.
    public void prepareThumbnail(final Bitmap bitmap) {
        Log.d(TAG, "prepareThumbnail: ");
        Runnable r = new Runnable() {
            public void run() {
                Bitmap thumbImage;

                if (bitmap == null) {
                    thumbImage = BitmapUtils.getBitmapFromAssets(getActivity(), ImageFiltersActivity.IMAGE_NAME, 100, 100);
                    Log.d(TAG, "run: bitmap == null thumbImage: " + thumbImage);
                } else {
                    thumbImage = Bitmap.createScaledBitmap(bitmap, 100, 100, false);
                    Log.d(TAG, "run: else thumbImage: " + thumbImage);
                }

                if (thumbImage == null)
                    return;

                ThumbnailsManager.clearThumbs();
                thumbnailItemList.clear();

                // add normal bitmap first
                ThumbnailItem thumbnailItem = new ThumbnailItem();
                thumbnailItem.image = thumbImage;
                thumbnailItem.filterName = getString(R.string.filter_normal);
                ThumbnailsManager.addThumb(thumbnailItem);

                //FilterPack.getFilterPack ()은 라이브러리에서 사용 가능한 필터 목록을 제공합니다.
                List<Filter> filters = FilterPack.getFilterPack(getActivity());

                for (Filter filter : filters) {
                    ThumbnailItem tI = new ThumbnailItem();
                    tI.image = thumbImage;
                    tI.filter = filter;
                    tI.filterName = filter.getName();
                    ThumbnailsManager.addThumb(tI);
                }

                thumbnailItemList.addAll(ThumbnailsManager.processThumbs(getActivity()));

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        // Once the thumbnail data set is ready, mAdapter.notifyDataSetChanged() called to render the list.
                        // All this was done in a background thread as image process takes sometime and we shouldn’t block main thread.
                        // 썸네일 데이터 세트가 준비되면 mAdapter.notifyDataSetChanged ()가 호출되어 목록을 렌더링합니다.
                        // 이미지 프로세스에 시간이 걸리기 때문에이 작업은 백그라운드 스레드에서 수행되었으며 메인 스레드를 차단해서는 안됩니다.
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        };

        new Thread(r).start();
    }

    @Override
    public void onFilterSelected(Filter filter) {
        if (listener != null)
            listener.onFilterSelected(filter);
    }

    // FiltersListFragmentListener 인터페이스는 새 필터를 선택할 때마다 ImageFiltersActivity에 콜백 메소드를 제공합니다.
    public interface FiltersListFragmentListener {
        void onFilterSelected(Filter filter);
    }
}