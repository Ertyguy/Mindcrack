package com.edaviessmith.mindcrack.db;


import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.edaviessmith.mindcrack.data.RedditPost;

public class RedditPostORM {

	static final String TAG = "RedditORM";
	String id;
	String title;
    String author;
    int points;
    int numComments;
    
    String permalink;
    String url;    
    String domain;
    static final String TABLE = "reddit";
    static final String COL_ID = "id";
    static final String COL_TITLE = "title";
    static final String COL_AUTHOR = "author";
    static final String COL_POINTS = "text";
    static final String COL_NUMCOMMENTS = "numcomments";
    static final String COL_PERMALINK = "permalink";
    static final String COL_URL = "url";
    
    static final String COL_TITLEFLAIR = "titleflair";
    static final String COL_TEXTHTML = "texthtml";
    static final String COL_CREATED = "created";
    static final String COL_VIDEOID = "videoid";
    static final String COL_IMAGEMED = "imagemed";
    static final String COL_IMAGEHIGH = "imagehigh";
    
    public static String SQL_CREATE_TABLE = "CREATE TABLE "+ TABLE +" (" + 
            RedditPostORM.COL_ID     	+ " INTEGER, "+
            RedditPostORM.COL_TITLE      + " TEXT, " +
            RedditPostORM.COL_TITLEFLAIR  + " TEXT, " +
            RedditPostORM.COL_AUTHOR       + " TEXT, " +
            RedditPostORM.COL_POINTS         + " INTEGER, " +
            RedditPostORM.COL_NUMCOMMENTS     + " INTEGER, " +
            RedditPostORM.COL_PERMALINK      + " TEXT, " +
            RedditPostORM.COL_URL      		+ " TEXT, " +
            RedditPostORM.COL_TEXTHTML     + " TEXT, " +
            RedditPostORM.COL_CREATED     + " INTEGER, " +
            RedditPostORM.COL_VIDEOID     + " TEXT, " +
            RedditPostORM.COL_IMAGEMED     + " TEXT, " +
            RedditPostORM.COL_IMAGEHIGH     + " TEXT " + ");";
    
    public static String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE;
    public static String ORDER_BY_ID = RedditPostORM.COL_ID + " ASC";


    
    public static List<RedditPost> getRedditFeed(Context context) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        
        Cursor cursor = database.query(false, RedditPostORM.TABLE, null, null, null, null, null, ORDER_BY_ID, null);
        
        List<RedditPost> postList = new ArrayList<RedditPost>();

        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                RedditPost post = cursorToYoutubeItem(cursor);
                postList.add(post);
                cursor.moveToNext();
            }
        }

        database.close();

        return postList;
    }
    
    
    public static RedditPost getLatestRedditFeed(Context context) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        
        //TODO Get latest id
        Cursor cursor = database.query(false, RedditPostORM.TABLE, null, RedditPostORM.COL_ID + " = ?", new String[]{"0"}, null, null, ORDER_BY_ID, null);
        
        RedditPost post = null;

        if(cursor.getCount() == 1) {
            cursor.moveToFirst();
            post = cursorToYoutubeItem(cursor);
        }

        database.close();

        return post;
    }
    
    
    
    //Set posts with memberId
	public static void updateRedditFeed(Context context, List<RedditPost> posts) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        try {
        	database.beginTransaction();     
        	database.delete(TABLE, null, null);	//Clear table
	        for(RedditPost post : posts) {
	        	Log.d(TAG, "updating: "+post.toDatabaseString());
	        	database.update(TABLE, postUpdateToContentValues(post), RedditPostORM.COL_ID + " = ?", new String[]{String.valueOf(post.getId())});
	        }
	        database.setTransactionSuccessful();
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
        	database.endTransaction();
        	database.close();
        }

	}
    
	
    //Add posts with memberId (only used if no previous YoutubeItem for member exist)
	public static void insertRedditFeed(Context context, List<RedditPost> posts) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        try {
        	database.beginTransaction();
        	database.delete(TABLE, null, null);	//Clear table
	        for(RedditPost post : posts) {
	        	Log.d(TAG, "inserting: "+post.toDatabaseString());
	        	database.insert(TABLE, null, postInsertToContentValues(post));
	        }
	        database.setTransactionSuccessful();
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
        	database.endTransaction();
        	database.close();
        }

	}
    
    public static void updatePost(Context context, RedditPost post) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();

        //TODO check this is working right in other orms
        database.update(TABLE, postUpdateToContentValues(post), RedditPostORM.COL_ID + " = ?", new String[]{post.getId()});
    
        database.close();
    }
    
    
    private static ContentValues postUpdateToContentValues(RedditPost post) {
        ContentValues values = new ContentValues();
        values.put(RedditPostORM.COL_TITLE, post.getTitle());
        values.put(RedditPostORM.COL_TITLEFLAIR, post.getTitleFlair());
        values.put(RedditPostORM.COL_AUTHOR, post.getAuthor());
        values.put(RedditPostORM.COL_POINTS, post.getPoints());
        values.put(RedditPostORM.COL_NUMCOMMENTS, post.getNumComments());
        values.put(RedditPostORM.COL_PERMALINK, post.getPermalink());
        values.put(RedditPostORM.COL_URL, post.getUrl());        
        values.put(RedditPostORM.COL_TEXTHTML, post.getTextHtml());
        values.put(RedditPostORM.COL_CREATED, post.getCreated());
        values.put(RedditPostORM.COL_VIDEOID, post.getVideoId());
        values.put(RedditPostORM.COL_IMAGEMED, post.getImageMed());
        values.put(RedditPostORM.COL_IMAGEHIGH, post.getImageHigh());
        return values;
    }
 
    
    public static ContentValues postInsertToContentValues(RedditPost post) {
        ContentValues values = new ContentValues();
        
        values.put(RedditPostORM.COL_ID, post.getId());
        values.put(RedditPostORM.COL_TITLE, post.getTitle());
        values.put(RedditPostORM.COL_TITLEFLAIR, post.getTitleFlair());
        values.put(RedditPostORM.COL_AUTHOR, post.getAuthor());
        values.put(RedditPostORM.COL_POINTS, post.getPoints());
        values.put(RedditPostORM.COL_NUMCOMMENTS, post.getNumComments());
        values.put(RedditPostORM.COL_PERMALINK, post.getPermalink());
        values.put(RedditPostORM.COL_URL, post.getUrl());
        values.put(RedditPostORM.COL_TEXTHTML, post.getTextHtml());
        values.put(RedditPostORM.COL_CREATED, post.getCreated());
        values.put(RedditPostORM.COL_VIDEOID, post.getVideoId());
        values.put(RedditPostORM.COL_IMAGEMED, post.getImageMed());
        values.put(RedditPostORM.COL_IMAGEHIGH, post.getImageHigh());
        return values;
    }

    private static RedditPost cursorToYoutubeItem(Cursor cursor) {
        RedditPost post = new RedditPost();
        post.setId(cursor.getString(cursor.getColumnIndex(RedditPostORM.COL_ID)));
        post.setTitle(cursor.getString(cursor.getColumnIndex(RedditPostORM.COL_TITLE)));
        post.setTitleFlair(cursor.getString(cursor.getColumnIndex(RedditPostORM.COL_TITLEFLAIR)));
        post.setAuthor(cursor.getString(cursor.getColumnIndex(RedditPostORM.COL_AUTHOR)));
        post.setPoints(cursor.getInt(cursor.getColumnIndex(RedditPostORM.COL_POINTS)));
        post.setNumComments(cursor.getInt(cursor.getColumnIndex(RedditPostORM.COL_NUMCOMMENTS)));
        post.setPermalink(cursor.getString(cursor.getColumnIndex(RedditPostORM.COL_PERMALINK)));
        post.setUrl(cursor.getString(cursor.getColumnIndex(RedditPostORM.COL_URL)));        
        post.setTextHtml(cursor.getString(cursor.getColumnIndex(RedditPostORM.COL_TEXTHTML)));
        post.setCreated(cursor.getInt(cursor.getColumnIndex(RedditPostORM.COL_CREATED)));
        post.setVideoId(cursor.getString(cursor.getColumnIndex(RedditPostORM.COL_VIDEOID)));
        post.setImageMed(cursor.getString(cursor.getColumnIndex(RedditPostORM.COL_IMAGEMED)));
        post.setImageHigh(cursor.getString(cursor.getColumnIndex(RedditPostORM.COL_IMAGEHIGH)));
        return post;
    }
    
}