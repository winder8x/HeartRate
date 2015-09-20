package com.wow.heartrate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 数据操作工具
 * @author gongjan
 *
 */
public class DBUtils {
	private SQLiteDatabase db;
	public DBUtils(SQLiteDatabase db){
		this.db = db;
	}
	/**
	 * "create table u_heartreate(" +
        		" test_date datetime not null , " +
        		" ddate datetime not null , " +
        		" heartreate int not null," +
        		" remark varchar(50) );"; 
	 * @param asql
	 */
	public void insert(String testDate,int heartRate){
		String sql = "insert into u_heartreate(test_date,ddate,heartrate,remark) " +
				"values (?,?,?,?);";
		Object[] args = new Object[4];
		args[0] = testDate;
		args[1] = getDatetime();
		args[2] = heartRate;
		args[3] = null;
		db.execSQL(sql,args);		
	}
	
	/**
	 * 查询所有测试记录
	 * @return
	 */
	public List<String> query(){
		String sql = "select distinct test_date from u_heartreate order by test_date desc limit 10";
		String[] selectionArgs = new String[0];
		Cursor c = db.rawQuery(sql, selectionArgs);
		List<String> ret = new ArrayList<String>();
		if(c.moveToFirst()){
			for(int i = 0; i < c.getCount(); i++){
				if(c.moveToNext()){
					String tdate = c.getString(c.getColumnIndex("test_date"));
					ret.add(tdate);
				}
			}
		}
		return ret;
	}
	
	/**
	 * 查询指定的测试记录
	 * @param testDate
	 */
	public List<HeartRateEntity> query(String testDate){
		String sql = "select * from u_heartreate where test_date = ?";
		String[] selectionArgs = new String[1];
		selectionArgs[0] = testDate;
		Cursor c = db.rawQuery(sql, selectionArgs);
		
		List<HeartRateEntity> ret = new ArrayList<HeartRateEntity>();
		if(c.moveToFirst()){
			int cnt = c.getCount();
			for(int i = 0; i < cnt; i++){
				if(c.moveToNext()){
					String ddate = c.getString(c.getColumnIndex("ddate"));
					int rate = c.getInt(c.getColumnIndex("heartrate"));
					HeartRateEntity r = new HeartRateEntity();
					r.setDataDate(ddate);
					r.setRate(rate);
					r.setTestDate(testDate);
					ret.add(r);
				}
			}
		}
		return ret;
	}
	
	/**
	 * 删除制定的数据
	 * @param testDate
	 */
	public void delete(String testDate){
		String sql = "delete from u_heartreate where test_date like ?";
		String[] selectionArgs = new String[1];
		selectionArgs[0] = testDate + "%";
		db.execSQL(sql, selectionArgs);
	}
	
	/**
	 * 删除所有数据
	 */
	public void clearData(){
		String sql = "delete from u_heartreate";
		db.execSQL(sql);
	}
	
	public static String getDatetime(){
		String ret = "";
		Date s = new Date();
		java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyyMMddHHmmss",Locale.CHINA);
		ret = fmt.format(s);
		return ret;
	}
	
}
