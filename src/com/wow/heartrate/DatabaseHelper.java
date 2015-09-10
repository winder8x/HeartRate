package com.wow.heartrate;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
 
    private static final String DB_NAME = "mydata.db"; //数据库名称
    private static final int version = 1; //数据库版本
    
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, version);
        // TODO Auto-generated constructor stub
    }
 
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table u_heartreate(" +
        		" test_date varchar(20) not null , " +
        		" ddate varchar(20) not null , " +
        		" heartrate int not null," +
        		" remark varchar(50) );";          
        db.execSQL(sql);
    }
 
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
    
    }
    
 
}