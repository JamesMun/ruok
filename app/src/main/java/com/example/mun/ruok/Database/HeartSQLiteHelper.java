package com.example.mun.ruok.Database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.mun.ruok.Service.SensorService;

public class HeartSQLiteHelper {

    public int dbMode = Context.MODE_PRIVATE;

    // Database 생성 및 열기
    //public SQLiteDatabase db;

    // Table 생성
    public  void createTable(SQLiteDatabase db){
        String Heart_CT = "create table if not exists HEART_SETTING(id INTEGER, max_heart_rate INTEGER, min_heart_rate INTEGER) ";
        db.execSQL(Heart_CT);
    }

    // Table 삭제
    public  void dropTable(SQLiteDatabase db){
        String Heart_DT = "drop table if exists HEART_SETTING;";
        db.execSQL(Heart_DT);
    }

    // Data 추가
    public  void insertData(SQLiteDatabase db, int max_heart_rate, int min_heart_rate){
        String Heart_ID = "insert into HEART_SETTING values(1," + max_heart_rate +", " +  min_heart_rate + ");";
        db.execSQL(Heart_ID);
    }

    // Data 삭제
    public  void removeData(SQLiteDatabase db){
        String Heart_RD = "delete from HEART_SETTING where id = 1;";
        db.execSQL(Heart_RD);
    }

    //--------------------------insert 확인
    public void selectAll(SQLiteDatabase db){
        String sql = "select * from HEART_SETTING;";
        Cursor results = db.rawQuery(sql, null);

        results.moveToFirst();

        while(!results.isAfterLast()){
            SensorService.max_heart_rate = results.getInt(1);
            SensorService.min_heart_rate = results.getInt(2);
            Log.d("HeartSQL", String.valueOf(results.getInt(1)) + " " + String.valueOf(results.getInt(2)));
            results.moveToNext();
        }
        results.close();
    }

    public boolean isTable(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name ='HEART_SETTING'" , null);
        cursor.moveToFirst();

        if(cursor.getCount()>0){
            return true;
        }else{
            return false;
        }
    }
}