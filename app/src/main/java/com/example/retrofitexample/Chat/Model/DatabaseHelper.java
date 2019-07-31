package com.example.retrofitexample.Chat.Model;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String TAG = "DatabaseHelpder : ";

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "ChatManager";
    private static final String TABLE_MESSAGELIST = "MessageList";

    private static final String KEY_ID = "id";
    private static final String KEY_ROOMNO = "roomno";
    private static final String KEY_PROFILE = "profile";
    private static final String KEY_TITLE = "title";
    private static final String KEY_CONTENT = "content";
    private static final String KEY_DATE = "date";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //3rd argument to be passed is CursorFactory instance
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate: ");

        String CREATE_MESSAGELISTCONTENTS_TABLE = "CREATE TABLE " + TABLE_MESSAGELIST + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"+ KEY_ROOMNO + " TEXT," + KEY_PROFILE + " TEXT," + KEY_TITLE + " TEXT," + KEY_CONTENT + " TEXT,"
                + KEY_DATE + " TEXT" + ")";

        String CREATE_MESSAGES_TABLE = "CREATE TABLE " + "Messages" + "("
                + "id" + " INTEGER PRIMARY KEY AUTOINCREMENT," + "roomNo"+ " TEXT," + "profile" + " TEXT," + "name" + " TEXT," + "message" + " TEXT,"
                + "date" + " TEXT" + ")";

        db.execSQL(CREATE_MESSAGELISTCONTENTS_TABLE);
        db.execSQL(CREATE_MESSAGES_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade: ");

        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGELIST);
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + "Messages");

        // Create tables again
        onCreate(db);
    }

    // code to add the new messageListContent
    public void addmessageListContent(MessageListContent messageListContent) {
        Log.d(TAG, "addmessageListContent: ");
        Log.d(TAG, "addmessageListContent: getid: " + messageListContent.getId());

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ROOMNO, messageListContent.getRoomNo()); // MessageListContent RoomNo
        values.put(KEY_PROFILE, messageListContent.getImg_path()); // MessageListContent Profile
        values.put(KEY_TITLE, messageListContent.getTitle()); // MessageListContent Title
        values.put(KEY_CONTENT, messageListContent.getContent()); // MessageListContent Title
        values.put(KEY_DATE, messageListContent.getChatTime()); // MessageListContent Title


        // Inserting Row
        db.insert(TABLE_MESSAGELIST, null, values);
        //2nd argument is String containing nullColumnHack
        db.close(); // Closing database connection
    }

    // code to get the single messageListContent
//    MessageListContent getmessageListContent(int id) {
//        Log.d(TAG, "getmessageListContent: ");
//
//        SQLiteDatabase db = this.getReadableDatabase();
//
//        Cursor cursor = db.query(TABLE_MESSAGELIST, new String[] { KEY_ID,
//                        KEY_TITLE, KEY_PROFILE, KEY_CONTENT, KEY_DATE }, KEY_ID + "=?",
//                new String[] { String.valueOf(id) }, null, null, null, null);
//        if (cursor != null)
//            cursor.moveToFirst();
//
//        MessageListContent messageListContent = new MessageListContent(cursor.getString(0),cursor.getString(1),
//                cursor.getString(2), cursor.getString(3), cursor.getString(4));
//        // return messageListContent
//        return messageListContent;
//    }

    // code to get all messageListContents in a list view
    public List<MessageListContent> getAllMessageListContents() {
        Log.d(TAG, "getAllMessageListContents: ");

        List<MessageListContent> messageListContentList = new ArrayList<MessageListContent>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_MESSAGELIST;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                MessageListContent messageListContent = new MessageListContent();
                messageListContent.setRoomNo(cursor.getInt(1));
                messageListContent.setImg_path(cursor.getString(2));
                messageListContent.setTitle(cursor.getString(3));
                messageListContent.setContent(cursor.getString(4));
                messageListContent.setChatTime(cursor.getString(5));
                // Adding messageListContent to list
                messageListContentList.add(messageListContent);
            } while (cursor.moveToNext());
        }

        // return messageListContent list
        return messageListContentList;
    }

    // code to update the single messageListContents
    public int updateMessageListContent(MessageListContent messageListContent) {
        Log.d(TAG, "updateMessageListContent: ");

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PROFILE, messageListContent.getImg_path());
        values.put(KEY_TITLE, messageListContent.getTitle());
        values.put(KEY_CONTENT, messageListContent.getContent());
        values.put(KEY_DATE, messageListContent.getChatTime());

        // updating row
        return db.update(TABLE_MESSAGELIST, values, KEY_TITLE + " = ?",
                new String[] { String.valueOf(messageListContent.getTitle()) });
    }

    // Deleting single messageListContents
    public void deleteMessageListContent(MessageListContent messageListContent) {
        Log.d(TAG, "deleteMessageListContent: ");

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MESSAGELIST, KEY_ID + " = ?",
                new String[] { String.valueOf(messageListContent.getId()) });
        db.close();
    }

    // Getting messageListContents Count
    public int getMessageListContentsCount() {
        Log.d(TAG, "getMessageListContentsCount: ");

        String countQuery = "SELECT  * FROM " + TABLE_MESSAGELIST;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }


    public boolean CheckIsTitleDataAlreadyInDBorNot(String titlevalue) {
        Log.d(TAG, "CheckIsTitleDataAlreadyInDBorNot: ");

        SQLiteDatabase db = this.getReadableDatabase();
        String Query = "SELECT * FROM " + TABLE_MESSAGELIST + " WHERE " + KEY_TITLE + " = ? ORDER BY date";
        Cursor cursor = db.rawQuery(Query, new String[]{titlevalue});

        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    /*
    아이디가 일치하는 채팅방 데이터 row에서 방 번호를 가져온다.
     */
    public String GetSpecificRowRoomNo(String titlevalue) {
        Log.d(TAG, "CheckIsTitleDataAlreadyInDBorNot: ");

        SQLiteDatabase db = this.getReadableDatabase();
        String Query = "SELECT "+ KEY_ROOMNO +" FROM " + TABLE_MESSAGELIST + " WHERE " + KEY_TITLE + " = ? ";
        Cursor cursor = db.rawQuery(Query, new String[]{titlevalue});
        cursor.moveToFirst();

        if(cursor.getCount() <= 0){
            cursor.close();
            return "no row";
        }

        String returnedRoomNo = cursor.getString(cursor.getColumnIndex(KEY_ROOMNO));
        Log.d(TAG, "GetSpecificRowRoomNo: returnedRoomNo: " + cursor.getString(0));
        cursor.close();
        return returnedRoomNo;
    }

}