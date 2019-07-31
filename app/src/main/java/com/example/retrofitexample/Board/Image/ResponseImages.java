package com.example.retrofitexample.Board.Image;

public class ResponseImages {
    private BoardImageItem[] imageitems;
    int result;

    public BoardImageItem[] getImageItems() {
        return imageitems;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }
}