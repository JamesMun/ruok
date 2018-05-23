package com.example.mun.ruok.Database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.mun.ruok.Fragment.SettingFragment;

public class FitSQLiteHelper {


    // Database 생성 및 열기
    //public SQLiteDatabase db;

    // Table 생성
    public  void createTable(SQLiteDatabase db){
        String sql = "create table if not exists FitTime(id INTEGER, Hour INTEGER, Minute INTEGER) ";
        db.execSQL(sql);
    }

    // Table 삭제
    public  void dropTable(SQLiteDatabase db){
        String sql = "drop table if exists FitTime;";
        db.execSQL(sql);
    }

    // Data 추가
    public  void insertData(SQLiteDatabase db, int Hour, int Minute){
        String sql = "insert into FitTime values(1," + Hour + ", " + Minute + ");";
        db.execSQL(sql);
    }

    // Data 삭제
    public  void removeData(SQLiteDatabase db){
        String sql = "delete from FitTime where id = 1;";
        db.execSQL(sql);
    }

    //--------------------------insert 확인
    public void selectAll(SQLiteDatabase db){
        String sql = "select * from FitTime;";
        Cursor results = db.rawQuery(sql, null);

        results.moveToFirst();

        while(!results.isAfterLast()){
            //SettingFragment.fitHour = results.getInt(1);
            //SettingFragment.fitMinute = results.getInt(2);
            results.moveToNext();
        }
        results.close();
    }

    public boolean isTable(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name ='FitTime'" , null);
        cursor.moveToFirst();

        if(cursor.getCount()>0){
            return true;
        }else{
            return false;
        }
    }
}