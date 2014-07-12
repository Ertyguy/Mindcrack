package com.edaviessmith.mindcrack.db;

import java.util.ArrayList;
import java.util.List;

import com.edaviessmith.mindcrack.Constants;
import com.edaviessmith.mindcrack.data.Member;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;



public class MemberORM {
	static final String TAG = "MemberORM";
	
	static final String TABLE = "members";
	static final String COL_ID = "id";
	static final String COL_SORT = "sort";
	static final String COL_NAME = "name";
	static final String COL_STATUS = "status";
	static final String COL_IMAGE = "image";
	static final String COL_ICON = "icon";	
	static final String COL_UPLOADSID = "uploadsid";
	static final String COL_TWITTERID = "twitterid";
	
	public static String SQL_CREATE_TABLE = "CREATE TABLE "+ TABLE +" (" + 
			MemberORM.COL_ID 	 + " INTEGER PRIMARY KEY AUTOINCREMENT, "+
			MemberORM.COL_SORT   	 + " INTEGER, " +
			MemberORM.COL_NAME   	 + " TEXT, " +
			MemberORM.COL_STATUS 	 + " INTEGER, " +
			MemberORM.COL_IMAGE		 + " TEXT, " +
			MemberORM.COL_ICON		 + " TEXT, " +
			MemberORM.COL_UPLOADSID	 + " TEXT, " +
			MemberORM.COL_TWITTERID	 + " TEXT "+ ");";
	
	public static String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE;
	
	public static String ORDER_BY_SORT = MemberORM.COL_SORT + " ASC";
	
	/*public static void insertMember(Context context, Member member) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();

        ContentValues values = memberToContentValues(member);
        long memberId = database.insert(MemberORM.TABLE, null, values);
        Log.i("MemberORM", "Inserted new Member with ID: " + memberId);

        database.close();
    }*/
	

    
	public static List<Member> getMembers(Context context) {
		
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        
        		
        Cursor cursor = database.query(false, MemberORM.TABLE, null, null, null, null, null, ORDER_BY_SORT, null);

        Log.i("MemberORM", "Loaded " + cursor.getCount() + " Members...");
        List<Member> memberList = new ArrayList<Member>();

        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Member member = cursorToMember(context, cursor);
                memberList.add(member);
                cursor.moveToNext();
            }
            Log.i("MemberORM", "Members loaded successfully.");
        }

        database.close();

        return memberList;
    }
	
	public static List<Member> getVisibleMembers(Context context) {
		
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        
        		
        Cursor cursor = database.query(false, MemberORM.TABLE, null, MemberORM.COL_STATUS+" != ?", new String[]{String.valueOf(Constants.HIDDEN)}, null, null, ORDER_BY_SORT, null);

        Log.i("MemberORM", "Loaded " + cursor.getCount() + " Members...");
        List<Member> memberList = new ArrayList<Member>();

        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Member member = cursorToMember(context, cursor);
                memberList.add(member);
                cursor.moveToNext();
            }
            Log.i("MemberORM", "Members loaded successfully.");
        }

        database.close();

        return memberList;
    }
	
	
	public static List<Member> getMembersbyStatus(Context context, int status) {
		
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        
        		
        Cursor cursor = database.query(false, MemberORM.TABLE, null, MemberORM.COL_STATUS+" = ?", new String[]{String.valueOf(status)}, null, null, ORDER_BY_SORT, null);

        Log.i("MemberORM", "Loaded " + cursor.getCount() + " Members...");
        List<Member> memberList = new ArrayList<Member>();

        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Member member = cursorToMember(context, cursor);
                if(member != null) {
                	memberList.add(member);
                }
                cursor.moveToNext();
            }
            Log.i("MemberORM", "Members loaded successfully.");
        }

        database.close();

        return memberList;
    }

	
	public static void updateMember(Context context, Member member) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        try {
	        database.update(TABLE, memberUpdateToContentValues(context, member), MemberORM.COL_ID + " = ?", new String[]{String.valueOf(member.getId())});	        
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
        	database.close();
        }
        
	}
	
	public static void incrementMember(SQLiteDatabase database, int id) {
        try {
        	ContentValues values = new ContentValues();
        	values.put(MemberORM.COL_ID, id+1);
            
	        database.update(TABLE, values, MemberORM.COL_ID + " = ?", new String[]{String.valueOf(id)});	        
        }catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public static void incrementSort(SQLiteDatabase database, int id) {
        try {
        	ContentValues values = new ContentValues();
        	values.put(MemberORM.COL_SORT, id+1);
            
	        database.update(TABLE, values, MemberORM.COL_SORT + " = ?", new String[]{String.valueOf(id)});	        
        }catch (Exception e) {
            e.printStackTrace();
        }     
	}
	
	public static void updateMembers(Context context, List<Member> members) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        try {
        	database.beginTransaction();
	        for(Member member : members) {	        	
	        	database.update(TABLE, memberUpdateToContentValues(context, member), MemberORM.COL_ID + " = ?", new String[]{String.valueOf(member.getId())});
	        }
	        database.setTransactionSuccessful();
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
        	database.endTransaction();
        	database.close();
        }
	}
	
	
    private static ContentValues memberUpdateToContentValues(Context context, Member member) {
        ContentValues values = new ContentValues();
        values.put(MemberORM.COL_SORT, member.getSort());
        values.put(MemberORM.COL_NAME, member.getName());
        values.put(MemberORM.COL_STATUS, member.getStatus());
        values.put(MemberORM.COL_IMAGE, context.getResources().getResourceEntryName(member.getImage()));
        values.put(MemberORM.COL_ICON, context.getResources().getResourceEntryName(member.getIcon()));
        values.put(MemberORM.COL_UPLOADSID, member.getUploadsId());
        values.put(MemberORM.COL_TWITTERID, member.getTwitterId());

        return values;
    }
    
    
    public static ContentValues memberInsertToContentValues(Context context, Member member) {
        ContentValues values = new ContentValues();
        
        values.put(MemberORM.COL_ID, member.getId());
        values.put(MemberORM.COL_SORT, member.getSort());
        values.put(MemberORM.COL_NAME, member.getName());
        values.put(MemberORM.COL_STATUS, member.getStatus());
        values.put(MemberORM.COL_IMAGE, context.getResources().getResourceEntryName(member.getImage()));
        values.put(MemberORM.COL_ICON, context.getResources().getResourceEntryName(member.getIcon()));
        values.put(MemberORM.COL_UPLOADSID, member.getUploadsId());
        values.put(MemberORM.COL_TWITTERID, member.getTwitterId());

        return values;
    }

    private static Member cursorToMember(Context context, Cursor cursor) {
        Member member = new Member();
        member.setId(cursor.getInt(cursor.getColumnIndex(MemberORM.COL_ID)));
        member.setSort(cursor.getInt(cursor.getColumnIndex(MemberORM.COL_SORT)));
        member.setName(cursor.getString(cursor.getColumnIndex(MemberORM.COL_NAME)));
        member.setStatus(cursor.getInt(cursor.getColumnIndex(MemberORM.COL_STATUS)));
        String imageResource = cursor.getString(cursor.getColumnIndex(MemberORM.COL_IMAGE));
        member.setImage(context.getResources().getIdentifier(imageResource, "drawable", "com.edaviessmith.mindcrack"));
        String iconResource =cursor.getString(cursor.getColumnIndex(MemberORM.COL_ICON));
        member.setIcon( context.getResources().getIdentifier(iconResource, "drawable", "com.edaviessmith.mindcrack"));
        member.setUploadsId(cursor.getString(cursor.getColumnIndex(MemberORM.COL_UPLOADSID)));
        member.setTwitterId(cursor.getString(cursor.getColumnIndex(MemberORM.COL_TWITTERID)));
        return member;
    }
    
}
