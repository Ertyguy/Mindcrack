package com.edaviessmith.mindcrack.db;


import java.util.ArrayList;
import java.util.List;

import com.edaviessmith.mindcrack.data.Member;
import com.edaviessmith.mindcrack.data.Tweet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TwitterORM {

	static final String TAG = "TwitterORM";
	
    static final String TABLE = "twitter";
    static final String COL_ID = "id";
    static final String COL_MEMBERID = "memberid";
    static final String COL_TWEETID = "tweetid";
    static final String COL_TEXT = "text";
    static final String COL_DATE = "date";
    //static final String COL_IMAGEMED = "imagemed";
    //static final String COL_IMAGEHIGH = "imagehigh";
    
    public static String SQL_CREATE_TABLE = "CREATE TABLE "+ TABLE +" (" + 
            TwitterORM.COL_ID     		 + " INTEGER, "+
            TwitterORM.COL_MEMBERID      + " INTEGER, " +
            TwitterORM.COL_TWEETID       + " INTEGER, " +
            TwitterORM.COL_TEXT         + " TEXT, " +
            TwitterORM.COL_DATE          + " INTEGER " +");";
            //TwitterORM.COL_IMAGEMED      + " TEXT, " +
            //TwitterORM.COL_IMAGEHIGH     + " TEXT "+ ");";
    
    public static String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE;
    public static String ORDER_BY_ID = TwitterORM.COL_ID + " ASC";


    
    public static List<Tweet> getMemberTwitterFeed(Context context, int memberId) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        
        Cursor cursor = database.query(false, TwitterORM.TABLE, null, TwitterORM.COL_MEMBERID + " = ?", new String[]{String.valueOf(memberId)}, null, null, ORDER_BY_ID, null);
        
        List<Tweet> tweetList = new ArrayList<Tweet>();

        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Tweet tweet = cursorToYoutubeItem(cursor);
                tweetList.add(tweet);
                cursor.moveToNext();
            }
        }

        database.close();

        return tweetList;
    }
    
    
    public static Tweet getMemberLatestTwitterFeed(Context context, int memberId) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        
        
        Cursor cursor = database.query(false, TwitterORM.TABLE, null, TwitterORM.COL_ID + " = ? AND "+TwitterORM.COL_MEMBERID + " = ?", new String[]{"0", String.valueOf(memberId)}, null, null, ORDER_BY_ID, null);
        
        Tweet tweet = null;

        if(cursor.getCount() == 1) {
            cursor.moveToFirst();
            tweet = cursorToYoutubeItem(cursor);
        }

        database.close();

        return tweet;
    }
    
    
    
    //Set tweets with memberId
	public static void updateMemberTwitterFeed(Context context, List<Tweet> tweets) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        try {
        	database.beginTransaction();
	        for(Tweet tweet : tweets) {
	        	Log.d(TAG, "updating: "+tweet.toDatabaseString());
	        	database.update(TABLE, tweetUpdateToContentValues(tweet), TwitterORM.COL_ID + " = ? AND "+TwitterORM.COL_MEMBERID + " = ?", new String[]{String.valueOf(tweet.getId()), String.valueOf(tweet.getMemberId())});
	        }
	        database.setTransactionSuccessful();
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
        	database.endTransaction();
        	database.close();
        }

	}
    
	
    //Add tweets with memberId (only used if no previous YoutubeItem for member exist)
	public static void insertMemberTwitterFeed(Context context, List<Tweet> tweets) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        try {
        	database.beginTransaction();
	        for(Tweet tweet : tweets) {
	        	Log.d(TAG, "inserting: "+tweet.toDatabaseString());
	        	database.insert(TABLE, null, tweetInsertToContentValues(tweet));
	        }
	        database.setTransactionSuccessful();
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
        	database.endTransaction();
        	database.close();
        }

	}
    
    public static void updateYoutubeItem(Context context, Tweet tweet) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();

        database.update(TABLE, tweetUpdateToContentValues(tweet), TwitterORM.COL_ID + " = ?", new String[tweet.getId()]);
    
        database.close();
    }
    
    
    private static ContentValues tweetUpdateToContentValues(Tweet tweet) {
        ContentValues values = new ContentValues();
        values.put(TwitterORM.COL_MEMBERID, tweet.getMemberId());
        values.put(TwitterORM.COL_TWEETID, tweet.getTweetId());
        values.put(TwitterORM.COL_TEXT, tweet.getText());
        values.put(TwitterORM.COL_DATE, tweet.getDate());

        return values;
    }
    
    
    
    public static ContentValues tweetInsertToContentValues(Tweet tweet) {
        ContentValues values = new ContentValues();
        
        values.put(TwitterORM.COL_ID, tweet.getId());
        values.put(TwitterORM.COL_MEMBERID, tweet.getMemberId());
        values.put(TwitterORM.COL_TWEETID, tweet.getTweetId());
        values.put(TwitterORM.COL_TEXT, tweet.getText());
        values.put(TwitterORM.COL_DATE, tweet.getDate());

        return values;
    }

    private static Tweet cursorToYoutubeItem(Cursor cursor) {
        Tweet tweet = new Tweet();
        tweet.setId(cursor.getInt(cursor.getColumnIndex(TwitterORM.COL_ID)));
        tweet.setMemberId(cursor.getInt(cursor.getColumnIndex(TwitterORM.COL_MEMBERID)));
        tweet.setTweetId(cursor.getInt(cursor.getColumnIndex(TwitterORM.COL_TWEETID)));
        tweet.setText(cursor.getString(cursor.getColumnIndex(TwitterORM.COL_TEXT)));
        tweet.setDate(cursor.getLong(cursor.getColumnIndex(TwitterORM.COL_DATE)));
        return tweet;
    }
    
}