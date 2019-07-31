package com.example.retrofitexample.MovieSearch;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.retrofitexample.Board.BoardItemAdapter;
import com.example.retrofitexample.BoxOffice.Item.Movie;
import com.example.retrofitexample.GlideApp;
import com.example.retrofitexample.R;

import java.util.ArrayList;

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder>{
    private ArrayList<Movie> mArrayList;
    private ArrayList<Movie> mFilteredList;

    public static final String TAG = "DataAdapter : ";

    public interface MovieRecyclerviewClickListener{//클릭이벤트 만들어주기. 다른 클래스에 implement시킨다
        void onItemClicked(int position);//내가 누른 아이템의 포지션을 외부에서 알 수 있도록 정의
    }

    private DataAdapter.MovieRecyclerviewClickListener mListener;//위의 인터페이스를 내부에서 하나 들고있어야 한다

    public void setOnClickListener(DataAdapter.MovieRecyclerviewClickListener listener){//외부에서 메소드를 지정할 수 있도록 이런 메소드를 준비
        Log.d(TAG, "setOnClickListener mListener: " + mListener);

        mListener = listener;
    }

    public DataAdapter(ArrayList<Movie> arrayList) {
        mArrayList = arrayList;
       //mFilteredList = arrayList;
    }

    @Override
    public DataAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.searched_movie_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DataAdapter.ViewHolder viewHolder, int i) {

        final int pos = i;
        //viewHolder.ivPosterimg.setText(mFilteredList.get(i).getPosterimg());
        //프로필 이미지
        GlideApp.with(viewHolder.itemView).load(mArrayList.get(i).getPosterimg())
                .override(300, 400)
                .into(viewHolder.ivPosterimg);
        viewHolder.tvTitle.setText(mArrayList.get(i).getTitle());

        viewHolder.tvTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: pos: " + pos);
                Log.d(TAG, "onClick: mListener: " + mListener);
                mListener.onItemClicked(pos);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mArrayList.size();
    }

//    @Override
//    public Filter getFilter() {
//
//        return new Filter() {
//            @Override
//            protected FilterResults performFiltering(CharSequence charSequence) {
//
//                String charString = charSequence.toString();
//
//                if (charString.isEmpty()) {
//
//                    mFilteredList = mArrayList;
//                } else {
//
//                    ArrayList<Movie> filteredList = new ArrayList<>();
//
//                    for (Movie movie : mArrayList) {
//
//                        if (movie.getTitle().toLowerCase().contains(charString)) {
//
//                            filteredList.add(movie);
//                        }
//                    }
//
//                    mFilteredList = filteredList;
//                }
//
//                FilterResults filterResults = new FilterResults();
//                filterResults.values = mFilteredList;
//                return filterResults;
//            }
//
//            @Override
//            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
//                mFilteredList = (ArrayList<Movie>) filterResults.values;
//                notifyDataSetChanged();
//            }
//        };
//    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView ivPosterimg;
        private TextView tvTitle;

        public ViewHolder(View view) {
            super(view);

            ivPosterimg = (ImageView) view.findViewById(R.id.ivSearchedMoviePoster);
            tvTitle = (TextView)view.findViewById(R.id.tvSearchedMovieTitle);

        }
    }

}