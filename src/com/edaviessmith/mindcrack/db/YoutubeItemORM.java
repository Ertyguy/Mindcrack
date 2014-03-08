package com.edaviessmith.mindcrack.db;


import java.util.ArrayList;
import java.util.List;

import com.edaviessmith.mindcrack.data.Member;
import com.edaviessmith.mindcrack.data.YoutubeItem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class YoutubeItemORM {

	static final String TAG = "YoutubeItemORM";
	
    static final String TABLE = "youtubeitems";
    static final String COL_ID = "id";
    static final String COL_MEMBERID = "memberid";
    static final String COL_VIDEOID = "videoid";
    static final String COL_TITLE = "title";
    static final String COL_DATE = "date";
    static final String COL_IMAGEMED = "imagemed";
    static final String COL_IMAGEHIGH = "imagehigh";
    
    public static String SQL_CREATE_TABLE = "CREATE TABLE "+ TABLE +" (" + 
            YoutubeItemORM.COL_ID     		 + " INTEGER, "+
            YoutubeItemORM.COL_MEMBERID      + " INTEGER, " +
            YoutubeItemORM.COL_VIDEOID       + " TEXT, " +
            YoutubeItemORM.COL_TITLE         + " TEXT, " +
            //DATE NEEDS TO BE INT (UNIX TIME)
            YoutubeItemORM.COL_DATE          + " INTEGER, " +
            YoutubeItemORM.COL_IMAGEMED      + " TEXT, " +
            YoutubeItemORM.COL_IMAGEHIGH     + " TEXT "+ ");";
    
    public static String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE;
    public static String ORDER_BY_ID = YoutubeItemORM.COL_ID + " ASC";


    
    public static List<YoutubeItem> getMemberYoutubeItems(Context context, int memberId) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();

        //Cursor cursor = database.rawQuery("SELECT * FROM " + YoutubeItemORM.TABLE, null);
        Cursor cursor = database.query(false, YoutubeItemORM.TABLE, null, YoutubeItemORM.COL_MEMBERID + " = ?", new String[]{String.valueOf(memberId)}, null, null, ORDER_BY_ID, null);
        
        Log.i("YoutubeItemORM", "Loaded " + cursor.getCount() + " YoutubeItems...");
        List<YoutubeItem> youtubeItemList = new ArrayList<YoutubeItem>();

        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                YoutubeItem youtubeItem = cursorToYoutubeItem(cursor);
                youtubeItemList.add(youtubeItem);
                cursor.moveToNext();
            }
            Log.i("YoutubeItemORM", "YoutubeItems loaded successfully.");
        }

        database.close();

        return youtubeItemList;
    }
    
    
    public static YoutubeItem getMemberLatestYoutubeItem(Context context, int memberId) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();

        //Cursor cursor = database.rawQuery("SELECT * FROM " + YoutubeItemORM.TABLE, null);
        Cursor cursor = database.query(false, YoutubeItemORM.TABLE, null, YoutubeItemORM.COL_ID + " = ? AND "+YoutubeItemORM.COL_MEMBERID + " = ?", new String[]{"0", String.valueOf(memberId)}, null, null, ORDER_BY_ID, null);
        
        YoutubeItem youtubeItem = null;

        if(cursor.getCount() == 1) {
            cursor.moveToFirst();
            youtubeItem = cursorToYoutubeItem(cursor);
            Log.i("YoutubeItemORM", "Latest YoutubeItem loaded successfully.");
        }

        database.close();

        return youtubeItem;
    }
    
    
    
    //Set youtubeItems with memberId
	public static void updateMemberYoutubeItems(Context context, List<YoutubeItem> youtubeItems) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        try {
        	database.beginTransaction();
	        for(YoutubeItem youtubeItem : youtubeItems) {
	        	Log.d(TAG, "updating: "+youtubeItem.toDatabaseString());
	        	database.update(TABLE, youtubeItemUpdateToContentValues(youtubeItem), YoutubeItemORM.COL_ID + " = ? AND "+YoutubeItemORM.COL_MEMBERID + " = ?", new String[]{String.valueOf(youtubeItem.getId()), String.valueOf(youtubeItem.getMemberId())});
	        }
	        database.setTransactionSuccessful();
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
        	database.endTransaction();
        	database.close();
        }

	}
    
	
    //Add youtubeItems with memberId (only used if no previous YoutubeItem for member exist)
	public static void insertMemberYoutubeItems(Context context, List<YoutubeItem> youtubeItems) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        try {
        	database.beginTransaction();
	        for(YoutubeItem youtubeItem : youtubeItems) {
	        	Log.d(TAG, "inserting: "+youtubeItem.toDatabaseString());
	        	database.insert(TABLE, null, youtubeItemInsertToContentValues(youtubeItem));
	        }
	        database.setTransactionSuccessful();
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
        	database.endTransaction();
        	database.close();
        }

	}
    
    public static void updateYoutubeItem(Context context, YoutubeItem youtubeItem) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();

        database.update(TABLE, youtubeItemUpdateToContentValues(youtubeItem), YoutubeItemORM.COL_ID + " = ?", new String[youtubeItem.getId()]);
    
        database.close();
    }
    
    
    private static ContentValues youtubeItemUpdateToContentValues(YoutubeItem youtubeItem) {
        ContentValues values = new ContentValues();
        values.put(YoutubeItemORM.COL_MEMBERID, youtubeItem.getMemberId());
        values.put(YoutubeItemORM.COL_VIDEOID, youtubeItem.getVideoId());
        values.put(YoutubeItemORM.COL_TITLE, youtubeItem.getTitle());
        values.put(YoutubeItemORM.COL_DATE, youtubeItem.getDate());
        values.put(YoutubeItemORM.COL_IMAGEMED, youtubeItem.getImageMed());
        values.put(YoutubeItemORM.COL_IMAGEHIGH, youtubeItem.getImageHigh());

        return values;
    }
    
    
    
    public static ContentValues youtubeItemInsertToContentValues(YoutubeItem youtubeItem) {
        ContentValues values = new ContentValues();
        
        values.put(YoutubeItemORM.COL_ID, youtubeItem.getId());
        values.put(YoutubeItemORM.COL_MEMBERID, youtubeItem.getMemberId());
        values.put(YoutubeItemORM.COL_VIDEOID, youtubeItem.getVideoId());
        values.put(YoutubeItemORM.COL_TITLE, youtubeItem.getTitle());
        values.put(YoutubeItemORM.COL_DATE, youtubeItem.getDate());
        values.put(YoutubeItemORM.COL_IMAGEMED, youtubeItem.getImageMed());
        values.put(YoutubeItemORM.COL_IMAGEHIGH, youtubeItem.getImageHigh());

        return values;
    }

    private static YoutubeItem cursorToYoutubeItem(Cursor cursor) {
        YoutubeItem youtubeItem = new YoutubeItem();
        youtubeItem.setId(cursor.getInt(cursor.getColumnIndex(YoutubeItemORM.COL_ID)));
        youtubeItem.setMemberId(cursor.getInt(cursor.getColumnIndex(YoutubeItemORM.COL_MEMBERID)));
        youtubeItem.setVideoId(cursor.getString(cursor.getColumnIndex(YoutubeItemORM.COL_VIDEOID)));
        youtubeItem.setTitle(cursor.getString(cursor.getColumnIndex(YoutubeItemORM.COL_TITLE)));
        youtubeItem.setDate(cursor.getLong(cursor.getColumnIndex(YoutubeItemORM.COL_DATE)));
        youtubeItem.setImageMed(cursor.getString(cursor.getColumnIndex(YoutubeItemORM.COL_IMAGEMED)));
        youtubeItem.setImageHigh(cursor.getString(cursor.getColumnIndex(YoutubeItemORM.COL_IMAGEHIGH)));
        return youtubeItem;
    }
    
}