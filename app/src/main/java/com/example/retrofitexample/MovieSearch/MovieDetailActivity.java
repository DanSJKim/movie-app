package com.example.retrofitexample.MovieSearch;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.retrofitexample.GlideApp;
import com.example.retrofitexample.R;

public class MovieDetailActivity extends AppCompatActivity {
    public static final String TAG = "MovieDetailActivity : ";

    ImageView ivPosterimg;
    TextView tvTitle, tvOverview, tvDirector, tvActor, tvRating, tvBoxoffice;
    CardView cvBoxoffice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        //init
        ivPosterimg = (ImageView) findViewById(R.id.ivMoviePosterimg);
        tvTitle = (TextView) findViewById(R.id.tvMovieTitle);
        tvOverview = (TextView) findViewById(R.id.tvMovieOverview);
        tvDirector = (TextView) findViewById(R.id.tvMovieDirector);
        tvActor = (TextView) findViewById(R.id.tvMovieActor);
        tvRating = (TextView) findViewById(R.id.tvMovieRating);
        tvBoxoffice = (TextView) findViewById(R.id.tvMovieBoxOffice);
        cvBoxoffice = (CardView) findViewById(R.id.cvMovieBoxoffice);

        //getIntent MovieSearchActivity -> MovieDetailActivity
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String posterimg = intent.getStringExtra("posterimg");
        String overview = intent.getStringExtra("overview");
        String director = intent.getStringExtra("director");
        String actor = intent.getStringExtra("actor");
        String rating = intent.getStringExtra("rating");
        String boxoffice = intent.getStringExtra("boxoffice");

        //set
        GlideApp.with(MovieDetailActivity.this).load(posterimg)
                .override(300, 400)
                .into(ivPosterimg);
        tvTitle.setText(title);
        tvOverview.setText(overview);
        tvDirector.setText(director);
        tvActor.setText(actor);
        tvRating.setText(rating);
        tvBoxoffice.setText(boxoffice);

        if(tvBoxoffice.getText() == ""){
            tvBoxoffice.setVisibility(View.GONE);
            cvBoxoffice.setVisibility(View.GONE);
        }

    }
}
