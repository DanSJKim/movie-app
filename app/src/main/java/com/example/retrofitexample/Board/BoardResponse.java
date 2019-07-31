package com.example.retrofitexample.Board;

//게시물 객체들을 배열로 저장하는 객체
public class BoardResponse {
    private BoardItem[] boarditems;

    public BoardItem[] getBoardItems() {
        return boarditems;
    }
}