package com.example.retrofitexample.Board.Image;

import java.io.Serializable;

public class BoardImageItem  implements Serializable {

    private String img_path;
    private int board_id;
    private int id;

    public BoardImageItem(String img_path) {
        this.img_path = img_path;
    }

    public String getImg_path() {
        return img_path;
    }

    public void setImg_path(String img_path) {
        this.img_path = img_path;
    }

    public int getBoard_id() {
        return board_id;
    }

    public void setBoard_id(int board_id) {
        this.board_id = board_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}