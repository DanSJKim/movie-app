package com.example.retrofitexample.Board.Comment;

public class CommentItem {

    //multi viewtype용 변수
    public static final int FIRST_DEPTH_TYPE=0;
    public static final int SECOND_DEPTH_TYPE=1;

    private int cmtid; //댓글 번호
    private int board_id; //게시물 번호
    public int parent; //부모 댓글 번호

    private String img_path; //사진
    private String email; //사용자 메일 이름
    private String content; //내용
    private String date; //날짜

    // State of the item
    private boolean expanded;

    public CommentItem(int data, int cmtid) {
        //this.type = type; //view type 상수를 보유합니다
        //this.data = data; // data 변수는 우리가 채울 각 데이터를 저장하는 데 사용됩니다.이상적으로는 드로어 블 또는 raw 타입 리소스를 포함합니다.
        this.cmtid = cmtid;
    }

    public int getCmtid() {
        return cmtid;
    }

    public void setCmtid(int cmtid) {
        this.cmtid = cmtid;
    }

    public int getBoard_id() {
        return board_id;
    }

    public void setBoard_id(int board_id) {
        this.board_id = board_id;
    }

    public String getImg_path() {
        return img_path;
    }

    public void setImg_path(String img_path) {
        this.img_path = img_path;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getParent() {
        return parent;
    }

    public void setParent(int parent) {
        this.parent = parent;
    }

    //아이템 확장
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public boolean isExpanded() {
        return expanded;
    }

}
