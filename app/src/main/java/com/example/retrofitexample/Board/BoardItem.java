package com.example.retrofitexample.Board;

import com.example.retrofitexample.Board.Image.BoardImageItem;

public class BoardItem {

    String writer;
    String title;
    String content;
    String datetime;
    String img_path;
    String profile_img_path;

    int id; //게시물 번호
    int user_id; //작성자 번호
    private int isLiked; // 좋아요 유무
    private int likeCount; //좋아요 개수 카운트
    private int commentCount; //댓글 개수 카운트


    private BoardImageItem[] boardImageItems;//다중 이미지

    public BoardItem(int id, String writer, String title, String content, String datetime, String img_path) {
        this.id = id;
        this.writer = writer;
        this.title = title;
        this.content = content;
        this.datetime = datetime;
        this.img_path = img_path;
    }

    public String getWriter() {
        return writer;
    }

    public void setWriter(String writer) {
        this.writer = writer;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImage() {
        return img_path;
    }

    public void setImage(String img_path) {
        this.img_path = img_path;
    }

    public int getIsLiked() {
        return isLiked;
    }

    public void setIsLiked(int isLiked) {
        this.isLiked = isLiked;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getProfile_img_path() {
        return profile_img_path;
    }

    public void setProfile_img_path(String profile_img_path) {
        this.profile_img_path = profile_img_path;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public BoardImageItem[] getBoardImageItems() {
        return boardImageItems;
    }

    public void setBoardImageItems(BoardImageItem[] boardImageItems) {
        this.boardImageItems = boardImageItems;
    }
}
