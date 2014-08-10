package com.edaviessmith.mindcrack.db;


import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.edaviessmith.mindcrack.data.Reddit;

public class RedditORM {

	static final String TAG = "RedditORM";
	
    static final String TABLE 	  = "reddits";
    static final String COL_ID 	  = "id";
    static final String COL_TITLE = "title";
    static final String COL_URL   = "url";
	static final String COL_IMAGE = "image";
	static final String COL_ICON  = "icon";	
    
    public static String SQL_CREATE_TABLE = "CREATE TABLE "+ TABLE +" (" + 
            RedditORM.COL_ID     		+ " INTEGER, "+
            RedditORM.COL_TITLE         + " TEXT, " +
            RedditORM.COL_URL          	+ " TEXT, " +
			MemberORM.COL_IMAGE		 	+ " INTEGER, " +
			MemberORM.COL_ICON		 	+ " INTEGER " +");";
    
    public static String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE;
    public static String ORDER_BY_ID = RedditORM.COL_ID + " ASC";
    
    public static List<Reddit> getReddits(Context context) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();

        //Cursor cursor = database.rawQuery("SELECT * FROM " + RedditORM.TABLE, null);
        Cursor cursor = database.query(false, RedditORM.TABLE, null, null, null, null, null, ORDER_BY_ID, null);
        
        Log.i("RedditORM", "Loaded " + cursor.getCount() + " Reddits...");
        List<Reddit> redditList = new ArrayList<Reddit>();

        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Reddit reddit = cursorToReddit(cursor);
                redditList.add(reddit);
                cursor.moveToNext();
            }
            Log.i("RedditORM", "Reddits loaded successfully.");
        }

        database.close();

        return redditList;
    }
    
    
    /*public static Reddit getMemberLatestReddit(Context context, int redditId) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();

        //Cursor cursor = database.rawQuery("SELECT * FROM " + RedditORM.TABLE, null);
        Cursor cursor = database.query(false, RedditORM.TABLE, null, RedditORM.COL_ID + " = ? AND "+RedditORM.COL_MEMBERID + " = ?", new String[]{"0", String.valueOf(redditId)}, null, null, ORDER_BY_ID, null);
        
        Reddit reddit = null;

        if(cursor.getCount() == 1) {
            cursor.moveToFirst();
            reddit = cursorToReddit(cursor);
            Log.i("RedditORM", "Latest Reddit loaded successfully.");
        }

        database.close();

        return reddit;
    }*/
    
    
    
    //Set reddits with redditId
	/*public static void updateMemberReddits(Context context, List<Reddit> reddits) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        try {
        	database.beginTransaction();
	        for(Reddit reddit : reddits) {
	        	Log.d(TAG, "updating: "+reddit.toDatabaseString());
	        	database.update(TABLE, redditUpdateToContentValues(context, reddit), RedditORM.COL_ID + " = ? AND "+RedditORM.COL_MEMBERID + " = ?", new String[]{String.valueOf(reddit.getId()), String.valueOf(reddit.getMemberId())});
	        }
	        database.setTransactionSuccessful();
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
        	database.endTransaction();
        	database.close();
        }

	}*/
    
	
    //Add reddits with redditId (only used if no previous Reddit for reddit exist)
	public static void insertMemberReddits(Context context, List<Reddit> reddits) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        try {
        	database.beginTransaction();
	        for(Reddit reddit : reddits) {
	        	Log.d(TAG, "inserting: "+reddit.toDatabaseString());
	        	database.insert(TABLE, null, redditInsertToContentValues(context, reddit));
	        }
	        database.setTransactionSuccessful();
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
        	database.endTransaction();
        	database.close();
        }

	}
    
    /*public static void updateReddit(Context context, Reddit reddit) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();

        database.update(TABLE, redditUpdateToContentValues(context, reddit), RedditORM.COL_ID + " = ?", new String[reddit.getId()]);
    
        database.close();
    }
    */
    
/*    private static ContentValues redditUpdateToContentValues(Context context, Reddit reddit) {
        ContentValues values = new ContentValues();
        
        values.put(RedditORM.COL_TITLE, reddit.getTitle());
        values.put(RedditORM.COL_URL, reddit.getUrl());
        values.put(MemberORM.COL_IMAGE, context.getResources().getResourceEntryName(reddit.getImage()));
        values.put(MemberORM.COL_ICON, context.getResources().getResourceEntryName(reddit.getIcon()));

        return values;
    }*/
    
    
    
    public static ContentValues redditInsertToContentValues(Context context, Reddit reddit) {
        ContentValues values = new ContentValues();
        
        values.put(RedditORM.COL_ID, reddit.getId());
        values.put(RedditORM.COL_TITLE, reddit.getTitle());
        values.put(RedditORM.COL_URL, reddit.getUrl());
        values.put(MemberORM.COL_IMAGE, context.getResources().getResourceEntryName(reddit.getImage()));
        values.put(MemberORM.COL_ICON, context.getResources().getResourceEntryName(reddit.getIcon()));

        return values;
    }

    private static Reddit cursorToReddit(Cursor cursor) {
        Reddit reddit = new Reddit();
        reddit.setId(cursor.getInt(cursor.getColumnIndex(RedditORM.COL_ID)));
        reddit.setTitle(cursor.getString(cursor.getColumnIndex(RedditORM.COL_TITLE)));
        reddit.setUrl(cursor.getString(cursor.getColumnIndex(RedditORM.COL_URL)));
        reddit.setImage(cursor.getInt(cursor.getColumnIndex(RedditORM.COL_IMAGE)));
        reddit.setIcon(cursor.getInt(cursor.getColumnIndex(RedditORM.COL_ICON)));
        return reddit;
    }
    
}