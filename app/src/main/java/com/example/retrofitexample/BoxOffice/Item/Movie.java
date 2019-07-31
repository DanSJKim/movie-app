package com.example.retrofitexample.BoxOffice.Item;

public class Movie {
    int moveid;
    String title;
    String posterimg;
    String overview;
    String director;
    String actor;
    String rating;
    String boxoffice;

    int searchedItem;

    public int getSearchedItem() {
        return searchedItem;
    }

    public void setSearchedItem(int searchedItem) {
        this.searchedItem = searchedItem;
    }

    public int getMoveid() {
        return moveid;
    }

    public void setMoveid(int moveid) {
        this.moveid = moveid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPosterimg() {
        return posterimg;
    }

    public void setPosterimg(String posterimg) {
        this.posterimg = posterimg;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getBoxoffice() {
        return boxoffice;
    }

    public void setBoxoffice(String boxoffice) {
        this.boxoffice = boxoffice;
    }
}
