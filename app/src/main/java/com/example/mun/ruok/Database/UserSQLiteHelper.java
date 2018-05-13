package com.example.mun.ruok.Database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.mun.ruok.SensorService;

import java.sql.Blob;

public class UserSQLiteHelper {


    // Database 생성 및 열기
    //public SQLiteDatabase db;

    // Table 생성
    public void createTable(SQLiteDatabase db){
        String USER_CT = "create table if not exists USER(id INTEGER, UserID TEXT, UserType INTEGER) ";
        db.execSQL(USER_CT);
    }

    // Table 삭제
    public void dropTable(SQLiteDatabase db){
        String USER_DT = "drop table if exists USER;";
        db.execSQL(USER_DT);
    }

    // Data 추가
    public void insertData(SQLiteDatabase db, String UserID, int UserType) {
        String USER_ID = "insert into USER values(1, 'RUOK-" + UserID + "'," + UserType + ");";
        db.execSQL(USER_ID);
    }

    // Data 삭제
    public void removeData(SQLiteDatabase db){
        String USER_RD = "delete from USER where id = 1;";
        db.execSQL(USER_RD);
    }

    //--------------------------insert 확인
    public void selectAll(SQLiteDatabase db){
        String sql = "select * from USER;";
        String USERID = null;
        Cursor results = db.rawQuery(sql, null);

        results.moveToFirst();

        while(!results.isAfterLast()){
            SensorService.userid = results.getString(1);
            SensorService.UserType = results.getInt(2);
            results.moveToNext();
        }
        results.close();
    }

    public boolean isTable(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name ='USER'" , null);
        cursor.moveToFirst();

        if(cursor.getCount()>0){
            return true;
        }else{
            return false;
        }
    }
}