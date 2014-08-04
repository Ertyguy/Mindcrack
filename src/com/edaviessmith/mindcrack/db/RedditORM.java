package com.edaviessmith.mindcrack.db;


import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.edaviessmith.mindcrack.data.Post;

public class RedditORM {

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
            RedditORM.COL_ID     	+ " INTEGER, "+
            RedditORM.COL_TITLE      + " TEXT, " +
            RedditORM.COL_TITLEFLAIR  + " TEXT, " +
            RedditORM.COL_AUTHOR       + " TEXT, " +
            RedditORM.COL_POINTS         + " INTEGER, " +
            RedditORM.COL_NUMCOMMENTS     + " INTEGER, " +
            RedditORM.COL_PERMALINK      + " TEXT, " +
            RedditORM.COL_URL      		+ " TEXT, " +
            RedditORM.COL_TEXTHTML     + " TEXT, " +
            RedditORM.COL_CREATED     + " INTEGER, " +
            RedditORM.COL_VIDEOID     + " TEXT, " +
            RedditORM.COL_IMAGEMED     + " TEXT, " +
            RedditORM.COL_IMAGEHIGH     + " TEXT " + ");";
    
    public static String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE;
    public static String ORDER_BY_ID = RedditORM.COL_ID + " ASC";


    
    public static List<Post> getRedditFeed(Context context) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        
        Cursor cursor = database.query(false, RedditORM.TABLE, null, null, null, null, null, ORDER_BY_ID, null);
        
        List<Post> postList = new ArrayList<Post>();

        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Post post = cursorToYoutubeItem(cursor);
                postList.add(post);
                cursor.moveToNext();
            }
        }

        database.close();

        return postList;
    }
    
    
    public static Post getLatestRedditFeed(Context context) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        
        //TODO Get latest id
        Cursor cursor = database.query(false, RedditORM.TABLE, null, RedditORM.COL_ID + " = ?", new String[]{"0"}, null, null, ORDER_BY_ID, null);
        
        Post post = null;

        if(cursor.getCount() == 1) {
            cursor.moveToFirst();
            post = cursorToYoutubeItem(cursor);
        }

        database.close();

        return post;
    }
    
    
    
    //Set posts with memberId
	public static void updateRedditFeed(Context context, List<Post> posts) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        try {
        	database.beginTransaction();     
        	database.delete(TABLE, null, null);	//Clear table
	        for(Post post : posts) {
	        	Log.d(TAG, "updating: "+post.toDatabaseString());
	        	database.update(TABLE, postUpdateToContentValues(post), RedditORM.COL_ID + " = ?", new String[]{String.valueOf(post.getId())});
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
	public static void insertRedditFeed(Context context, List<Post> posts) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        try {
        	database.beginTransaction();
        	database.delete(TABLE, null, null);	//Clear table
	        for(Post post : posts) {
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
    
    public static void updatePost(Context context, Post post) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();

        //TODO check this is working right in other orms
        database.update(TABLE, postUpdateToContentValues(post), RedditORM.COL_ID + " = ?", new String[]{post.getId()});
    
        database.close();
    }
    
    
    private static ContentValues postUpdateToContentValues(Post post) {
        ContentValues values = new ContentValues();
        values.put(RedditORM.COL_TITLE, post.getTitle());
        values.put(RedditORM.COL_TITLEFLAIR, post.getTitleFlair());
        values.put(RedditORM.COL_AUTHOR, post.getAuthor());
        values.put(RedditORM.COL_POINTS, post.getPoints());
        values.put(RedditORM.COL_NUMCOMMENTS, post.getNumComments());
        values.put(RedditORM.COL_PERMALINK, post.getPermalink());
        values.put(RedditORM.COL_URL, post.getUrl());        
        values.put(RedditORM.COL_TEXTHTML, post.getTextHtml());
        values.put(RedditORM.COL_CREATED, post.getCreated());
        values.put(RedditORM.COL_VIDEOID, post.getVideoId());
        values.put(RedditORM.COL_IMAGEMED, post.getImageMed());
        values.put(RedditORM.COL_IMAGEHIGH, post.getImageHigh());
        return values;
    }
 
    
    public static ContentValues postInsertToContentValues(Post post) {
        ContentValues values = new ContentValues();
        
        values.put(RedditORM.COL_ID, post.getId());
        values.put(RedditORM.COL_TITLE, post.getTitle());
        values.put(RedditORM.COL_TITLEFLAIR, post.getTitleFlair());
        values.put(RedditORM.COL_AUTHOR, post.getAuthor());
        values.put(RedditORM.COL_POINTS, post.getPoints());
        values.put(RedditORM.COL_NUMCOMMENTS, post.getNumComments());
        values.put(RedditORM.COL_PERMALINK, post.getPermalink());
        values.put(RedditORM.COL_URL, post.getUrl());
        values.put(RedditORM.COL_TEXTHTML, post.getTextHtml());
        values.put(RedditORM.COL_CREATED, post.getCreated());
        values.put(RedditORM.COL_VIDEOID, post.getVideoId());
        values.put(RedditORM.COL_IMAGEMED, post.getImageMed());
        values.put(RedditORM.COL_IMAGEHIGH, post.getImageHigh());
        return values;
    }

    private static Post cursorToYoutubeItem(Cursor cursor) {
        Post post = new Post();
        post.setId(cursor.getString(cursor.getColumnIndex(RedditORM.COL_ID)));
        post.setTitle(cursor.getString(cursor.getColumnIndex(RedditORM.COL_TITLE)));
        post.setTitleFlair(cursor.getString(cursor.getColumnIndex(RedditORM.COL_TITLEFLAIR)));
        post.setAuthor(cursor.getString(cursor.getColumnIndex(RedditORM.COL_AUTHOR)));
        post.setPoints(cursor.getInt(cursor.getColumnIndex(RedditORM.COL_POINTS)));
        post.setNumComments(cursor.getInt(cursor.getColumnIndex(RedditORM.COL_NUMCOMMENTS)));
        post.setPermalink(cursor.getString(cursor.getColumnIndex(RedditORM.COL_PERMALINK)));
        post.setUrl(cursor.getString(cursor.getColumnIndex(RedditORM.COL_URL)));        
        post.setTextHtml(cursor.getString(cursor.getColumnIndex(RedditORM.COL_TEXTHTML)));
        post.setCreated(cursor.getInt(cursor.getColumnIndex(RedditORM.COL_CREATED)));
        post.setVideoId(cursor.getString(cursor.getColumnIndex(RedditORM.COL_VIDEOID)));
        post.setImageMed(cursor.getString(cursor.getColumnIndex(RedditORM.COL_IMAGEMED)));
        post.setImageHigh(cursor.getString(cursor.getColumnIndex(RedditORM.COL_IMAGEHIGH)));
        return post;
    }
    
}