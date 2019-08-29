package com.example.retrofitexample.MyPage.ImageFilters.utils;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by ravi on 23/10/17.
 */
/**
 * 이 클래스는 RecyclerView 썸네일 이미지 주위에 패딩을 추가하는 것입니다. 오른쪽 여백은 모든 축소판 이미지에 추가되지만 목록의 마지막 항목에는 추가되지 않습니다.
 */
public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
    private int space;

    public SpacesItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (parent.getChildAdapterPosition(view) == state.getItemCount() - 1) {
            outRect.left = space;
            outRect.right = 0;
        }else{
            outRect.right = space;
            outRect.left = 0;
        }
    }
}