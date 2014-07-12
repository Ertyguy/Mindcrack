package com.edaviessmith.mindcrack.db;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.edaviessmith.mindcrack.R;
import com.edaviessmith.mindcrack.data.Member;

public class DatabaseHelper extends SQLiteOpenHelper {

	static final String DB_NAME = "mindcrack.db";
	static final int DB_VERSION = 2;
	
	private static Context context;
	
	@SuppressWarnings("static-access")
	public DatabaseHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION); 
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(MemberORM.SQL_CREATE_TABLE);
        
        // Put mindcrack members into database
        createMembers(sqLiteDatabase);
        
        sqLiteDatabase.execSQL(YoutubeItemORM.SQL_CREATE_TABLE);  
        sqLiteDatabase.execSQL(TwitterORM.SQL_CREATE_TABLE);  
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
		db.execSQL(YoutubeItemORM.SQL_DROP_TABLE);
		db.execSQL(TwitterORM.SQL_DROP_TABLE);
		
		if(oldVersion <= 1) {
			addNewMember(db, 3);
			addNewMember(db, 22);
		}
		
		
		db.execSQL(YoutubeItemORM.SQL_CREATE_TABLE);  
        db.execSQL(TwitterORM.SQL_CREATE_TABLE);  		
	}
	
	
	public static void createMembers(SQLiteDatabase sqLiteDatabase) {
        
        
        
        try {
        	sqLiteDatabase.beginTransaction();
        	for(Member member : mindcrackers) {
            	sqLiteDatabase.insert(MemberORM.TABLE, null, MemberORM.memberInsertToContentValues(context, member));
            }
        	sqLiteDatabase.setTransactionSuccessful();
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
        	sqLiteDatabase.endTransaction();
        }  
    }
	
	//On upgrade add members by sort Id that were not in previous db version
	private void addNewMember(SQLiteDatabase sqLiteDatabase, int newId) {

        try {
        	sqLiteDatabase.beginTransaction();
        	for(int i= mindcrackers.size()-1; i >= newId; i--) {
        		MemberORM.incrementMember(sqLiteDatabase, i);
        		MemberORM.incrementSort(sqLiteDatabase, i);
            }
        	sqLiteDatabase.insert(MemberORM.TABLE, null, MemberORM.memberInsertToContentValues(context, mindcrackers.get(newId)));
        	sqLiteDatabase.setTransactionSuccessful();
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
        	sqLiteDatabase.endTransaction();
        } 
	}
	
	@SuppressWarnings("serial")
	static List<Member> mindcrackers = new ArrayList<Member>() {{
		//			//Id	//Name			//Image				//Icon					//Youtube PlaylistId		//Twitter Name
        add(new Member(0, "Adlingtont", R.drawable.adlingtont, R.drawable.adlingtont_icon, "UUfhM3yJ8a_sAJj0qOKb40gw", "adlingtont"));
        add(new Member(1, "AnderZEL", R.drawable.anderzel, R.drawable.anderzel_icon, "UU-_VTaWqRsZ1nzZLHQIGwQA", "youtubeanderzel"));
        add(new Member(2, "Arkas", R.drawable.arkas, R.drawable.arkas_icon, "UUStPXwuYhdUu-B6fkVgi3vQ", "MCArkas"));
        add(new Member(3, "Aureylian", R.drawable.aureylian, R.drawable.aureylian_icon, "UUM2FHDmMP92caH9aK7RwEdw", "aureylian"));            
        add(new Member(4, "AvidyaZEN", R.drawable.avidyazen, R.drawable.avidyazen_icon, "UUDREKsabG-MOTPxmJOHctEw", "AvidyaZen"));
        add(new Member(5, "BdoubleO", R.drawable.bdoubleo, R.drawable.bdoubleo_icon, "UUlu2e7S8atp6tG2galK9hgg", "BdoubleO100"));
        add(new Member(6, "BlameTC", R.drawable.blametc, R.drawable.blametc_icon, "UUmSwqv2aPbuOGiuii2TeaLQ", "BlameTC"));
        add(new Member(7, "Docm", R.drawable.docm, R.drawable.docm_icon, "UU4O9HKe9Jt5yAhKuNv3LXpQ", "docm77"));     
        add(new Member(8, "Etho", R.drawable.etho, R.drawable.etho_icon, "UUFKDEp9si4RmHFWJW1vYsMA", "EthoLP"));       
        add(new Member(9, "GenerikB", R.drawable.generikb, R.drawable.generikb_icon, "UUJTWU5K7kl9EE109HBeoldA", "generikb"));
        add(new Member(10, "Guude", R.drawable.guude, R.drawable.guude_icon, "UUAxBpbVbSXT2wsCwZfrIIVg", "GuudeLP"));
        add(new Member(11, "Jsano", R.drawable.jsano, R.drawable.jsano_icon, "UUJbgutTlUYZyUpfjTyYLWdw", "jsano19"));
        add(new Member(12, "KurtjMac", R.drawable.kurtmac, R.drawable.kurtmac_icon, "UU1Un5592U9mFx5n6j2HyXow", "kurtmac"));
        add(new Member(13, "MCGamer", R.drawable.mcgamer, R.drawable.mcgamer_icon, "UU6MqXe9o-xBQHzE-DmebTRw", "SuperMCGamer"));
        add(new Member(14, "Mhykol", R.drawable.mhykol, R.drawable.mhykol_icon, "UUD2JcdggW1j72WBgbuwrt-g", "Mhykol"));
        add(new Member(15, "Millbee", R.drawable.millbee, R.drawable.millbee_icon, "UUIsp57CkuqoPQyHP2B2Y5NA", "millbeeful"));
        add(new Member(16, "Nebris", R.drawable.nebris, R.drawable.nebris_icon, "UUChaPGDM0d6YQv2yyVoVfGg", "cheatynebris"));
        add(new Member(17, "Pakratt", R.drawable.pakratt, R.drawable.pakratt_icon, "UUEpnkm5LLXPyDo8mw8_w3MA", "pakratt0013"));
        add(new Member(18, "PaulsoaresJr", R.drawable.paulsoaresjr, R.drawable.paulsoaresjr_icon, "UUP6f9x4iXk3LH8Q1sqJmYPQ", "paulsoaresjr"));       
        add(new Member(19, "PauseUnpause", R.drawable.pauseunpause, R.drawable.pauseunpause_icon, "UUcoMCX6scirNav1fEMbjaPA", "pauseunpauses"));       
        add(new Member(20, "Pyro", R.drawable.pyro, R.drawable.pyro_icon, "UU6pSdcEaOjeUyVl-Vivp-dQ", "Pyrao"));
        add(new Member(21, "SethBling", R.drawable.sethbling, R.drawable.sethbling_icon, "UU8aG3LDTDwNR1UQhSn9uVrw", "SethBling"));
        add(new Member(22, "Sevadus", R.drawable.sevadus, R.drawable.sevadus_icon, "UUfiMdLmi7v8tz9Kc5QKUCJg", "Sevadus"));
        add(new Member(23, "TheJims", R.drawable.thejims, R.drawable.thejims_icon, "UUjyZBSE0KfXinkrlOG9uGbQ", "thejimslp"));
        add(new Member(24, "Vechs", R.drawable.vechs, R.drawable.vechs_icon, "UUOy5LOrYA5wVg5F_jAWtwfw", "Vechs"));        
        add(new Member(25, "VintageBeef", R.drawable.vintagebeef, R.drawable.vintagebeef_icon, "UUu17Sme-KE87ca9OTzP0p7g", "VintageBeefLP"));      
        add(new Member(26, "W92Baj", R.drawable.baj, R.drawable.baj_icon, "UUB3hoa-iGe3FVLBw1PmRRug", "W92Baj"));
        add(new Member(27, "Zisteau", R.drawable.zisteau, R.drawable.zisteau_icon, "UUewxof_QqDdqVdXY1BaDtqQ", "zisteau"));
        add(new Member(28, "Podcast", R.drawable.ic_launcher, R.drawable.ic_launcher, "UUAWQEAjn8udSFKN6D4NlqWQ", "mindcracklp"));
    }};
    
}
