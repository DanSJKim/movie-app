package com.example.retrofitexample.Board.Comment;

//댓글 객체들을 배열로 저장하는 객체
public class CommentResponse {
    private CommentItem[] commentitems;

    public CommentItem[] getCommentItems() {
        return commentitems;
    }
}