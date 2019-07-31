package com.example.retrofitexample.Board;

public class BoardLike {

    int id;
    int board_id;
    int user_id;

    public BoardLike(int id, int board_id, int user_id) {
        this.id = id;
        this.board_id = board_id;
        this.user_id = user_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBoard_id() {
        return board_id;
    }

    public void setBoard_id(int board_id) {
        this.board_id = board_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }
}
