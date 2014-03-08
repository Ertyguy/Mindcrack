package com.edaviessmith.mindcrack;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import com.bugsense.trace.BugSenseHandler;
import com.edaviessmith.mindcrack.data.Member;
import com.edaviessmith.mindcrack.data.MemberActivity;
import com.google.api.services.youtube.YouTube.Videos.GetRating;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;


public class Util{
	
	
	
	public static void setIndex(int id, int index) {
		AppInstance.getMindcrackers().get(id).setSort(index);
	}
		
/*	public static JSONArray getMemberStates(Context context) {
		
		SharedPreferences settings = context.getSharedPreferences(Constants.PREFS, 0);
	    String memberStateString = settings.getString(Constants.PREF_MEMBER_STATE+"break", "[]");
	    
	    if(memberStateString != null) {
	    	try {
	            JSONArray jsonMembers = new JSONArray(memberStateString);
	            for (int i = 0; i < jsonMembers.length(); i++) {
	                 if(jsonMembers.getInt(i) != Constants.VISIBLE) {
	                	 AppInstance.getMindcrackers().get(i).status = jsonMembers.getInt(i);
	                 }
	            }	            
	            return jsonMembers;
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	    return null;
	}
	
	public static void setMemberStates() {
		
		JSONArray jsonMembers = new JSONArray();
        for (final Member member : AppInstance.getMindcrackers())
        {    
        	jsonMembers.put(member.status);
        }
		
		SharedPreferences.Editor settings = AppInstance.getContext().getSharedPreferences(Constants.PREFS, 0).edit();
        settings.putString(Constants.PREF_MEMBER_STATE,  jsonMembers.toString());
        settings.commit();

	}*/
	
/*	public static SparseArray<MemberActivity> getMemberActivity() {
		
		SharedPreferences settings = AppInstance.getContext().getSharedPreferences(Constants.PREFS, 0);
	    String memberStateString = settings.getString(Constants.PREF_MEMBER_ACTIVITY, "[]");
	    
	    SparseArray<MemberActivity> memberActivities = new SparseArray<MemberActivity>();
	    if(memberStateString != null) {
	    	try {
	            JSONArray jsonMembers = new JSONArray(memberStateString);
	            for (int i = 0; i < jsonMembers.length(); i++) {
	            	JSONObject json = jsonMembers.getJSONObject(i);
	            	int id = json.getInt("id");
	            	memberActivities.put(id, new MemberActivity(id, json.getString("video")));
	            }	            
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	    return memberActivities;
	}
	
	
	
	public static void setMemberActivity(SparseArray<MemberActivity> memberActivities) {
		
		JSONArray jsonMembers = new JSONArray();
		
		int key = 0;
		for(int i = 0; i < memberActivities.size(); i++) {
		   key = memberActivities.keyAt(i);
		   // get the object by the key.
		   MemberActivity member = memberActivities.get(key);
		   try {
			   JSONObject json = new JSONObject();
       			json.put("id", key);
				json.put("video", member.latestVideo);
			
				jsonMembers.put(json);
       		} catch (JSONException e) {
				e.printStackTrace();
			}
		   
		}
		Log.i("Util","Members saved as:"+jsonMembers.toString());
		SharedPreferences.Editor settings = AppInstance.getContext().getSharedPreferences(Constants.PREFS, 0).edit();
        settings.putString(Constants.PREF_MEMBER_ACTIVITY,  jsonMembers.toString());
        settings.commit(); 
	}
	
	
	public static void clearMemberActivity() {
		SharedPreferences.Editor settings = AppInstance.getContext().getSharedPreferences(Constants.PREFS, 0).edit();
		settings.remove(Constants.PREF_MEMBER_ACTIVITY);
        settings.commit();
	}*/
	
	
	public static String getTimeSince(long publishedDate) {
     	String date;
     	
     	if(publishedDate <= 0) {
     		date = AppInstance.getContext().getResources().getString(R.string.loading_date);
     	} else {
	     	Calendar calNow = Calendar.getInstance();
	     	calNow.add(Calendar.SECOND, - (int)publishedDate);
	     	
	     	//  TimeZone.getDefault().getRawOffset()
	 		int days = (int) ((calNow.getTimeInMillis()) / 1000 / 60 / 60 / 24);
	     	if(days < 7) {
	     		
	     		date = days + (days == 1 ? " day ago": " days ago"); 
	     		//minutes or hours (
	     		if(days <= 1) {
	     			int minutes = (int) (calNow.getTimeInMillis() / 1000 / 60); 
	     			
	     			if(minutes < 60) {
	     				date = minutes <= 1 ? "1 minute ago": minutes + " minutes ago"; 
	     			} else {
	     				int hours = (minutes - (minutes % 60)) / 60;
	     				
	     				if(hours < 24) {
	     					date = hours <= 1 ? "1 hour ago": hours + " hours ago"; 
	     				}
	     			}
	     		}
	     		
	     	} else {
	     		int weeks = (days - (days % 7)) / 7;
	     		
	     		if(weeks < 4) {
	     			date = (weeks <= 1 ? "1 week ago": weeks + " weeks ago"); 
	     		} else {
	     			int months = (days - (days % 30)) / 30;
	     			
	     			if(months < 12) {
	     				date = (months <= 1 ? "1 month ago": months + " months ago"); 
	     			} else {
	     				int years = (days - (days % 365)) / 365;
	     				
	     				date = (years <= 1 ? "1 year ago": years + " years ago");
	     			}
	     		}
	     	}
     	}
     	
		return date;	
	}
	
	
	public static boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager = (ConnectivityManager) AppInstance.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	public static int getConnectionType() {
	ConnectivityManager connectivityManager = (ConnectivityManager) AppInstance.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
	
		boolean isMobile = false, isWifi = false;
	
		final NetworkInfo mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
	    if (mobile != null && mobile.isAvailable() && mobile.isConnected()) {
	    	isMobile = true;
	    }
	
	    final android.net.NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	    if (wifi != null && wifi.isAvailable() && wifi.isConnected()) {
	        
	        isWifi = true;
	    }
		if (!isMobile && !isWifi) 
		{ 
			Log.e("Util", "No connections found");
		} 
		 else 
		{ 
		        if(isWifi) return ConnectivityManager.TYPE_WIFI;
		        if(isMobile) return ConnectivityManager.TYPE_MOBILE;
		} 
		
		BugSenseHandler.addCrashExtraData("Members", "Connection type set to -1");
		return -1;
	}
		
	
	public static boolean getHiResThumbnail() {
		int conn = Util.getConnectionType();
		if(conn == ConnectivityManager.TYPE_WIFI && Util.WifiHiResEnabled() ||
				conn == ConnectivityManager.TYPE_MOBILE && Util.MobileHiResEnabled()){
			return true;
		}
		return false;
	}
	
	//Only start alarmManager if enabled and not already set
	public static void startAlarm() {
		
		if(notificationsEnabled(AppInstance.getContext())) {
			Intent intent = new Intent(AppInstance.getContext(), AlarmBroadcastReceiver.class);
			intent.setAction(Constants.NOTIFY_ACTION);
			boolean alarmActive = (PendingIntent.getBroadcast(AppInstance.getContext(), 0, intent, PendingIntent.FLAG_NO_CREATE) != null);

			//If alarm isn't already active create a new alarm
			if(!alarmActive) {
				try {
			        long minutes = getMinutes();	
			        BugSenseHandler.addCrashExtraData("Members", "Alarm Minutes set to "+minutes);
			        if(minutes != 0L) {
				        PendingIntent pendingIntent = PendingIntent.getBroadcast(AppInstance.getContext(), Constants.REQUEST_CODE, intent, 0);
				        AlarmManager alarmManager = (AlarmManager) AppInstance.getContext().getSystemService(Context.ALARM_SERVICE);
				        BugSenseHandler.addCrashExtraData("Members", "Alarm Pending");
				        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + minutes, pendingIntent);
				        Log.d("Util", "AlarmManager update was set to run in "+ (minutes / 60000) + " minutes");
			        } else {
			        	 Log.d("Util", "Alarm time set to never");
			        }
				}
				catch (Exception ex) {
					ex.printStackTrace(); // in case you want to see the stacktrace in your log cat output
					//Toast.makeText(AppInstance.getContext(), "Unable to start Notifications", Toast.LENGTH_SHORT).show();
					BugSenseHandler.addCrashExtraData("Exception", "Alarm exception");
					BugSenseHandler.sendException(ex);
				}
			} else {
				Log.d("Util", "AlarmManager already active");
			}
		}
	}
	
	
	public static void cancelAlarm() {

        Intent intent = new Intent(AppInstance.getContext(), AlarmBroadcastReceiver.class);
        intent.setAction(Constants.NOTIFY_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(AppInstance.getContext().getApplicationContext(), Constants.REQUEST_CODE, intent, 0);
        AlarmManager alarmManager = (AlarmManager) AppInstance.getContext().getSystemService(Context.ALARM_SERVICE);
        
        // Cancel alarms
        try {
        	alarmManager.cancel(pendingIntent);
        } catch (Exception e) {
            Log.e("Util", "AlarmManager update was not canceled. " + e.toString());
        }
	}
		
	
	private static long getMinutes() {
		SharedPreferences settings = AppInstance.getContext().getSharedPreferences(Constants.PREFS, 0);
		int conn = getConnectionType();
		
		long minutes = 120L; //Wait 2 hours if not connection
		if(conn == ConnectivityManager.TYPE_WIFI) {
			minutes = settings.getLong(Constants.PREF_WIFI_TIME, 180L);
		} else if(conn == ConnectivityManager.TYPE_MOBILE) {
			minutes = settings.getLong(Constants.PREF_NETWORK_TIME, 720L);
		}
		
		return  minutes * Constants.SECONDS_PER_MINUTE * Constants.MILLISECONDS_PER_SECOND;
	}
	
	
	public static boolean notificationsEnabled(Context context) {
		SharedPreferences settings = context.getSharedPreferences(Constants.PREFS, 0);
	    return settings.getBoolean(Constants.PREF_NOTIFY, true);
	}
	
	@SuppressLint("NewApi")
	public static void setNotificationEnabled(Context context, boolean state) {
		SharedPreferences.Editor settings = context.getSharedPreferences(Constants.PREFS, 0).edit();
		settings.putBoolean(Constants.PREF_NOTIFY, state);
        if (android.os.Build.VERSION.SDK_INT >= 9) {
        	settings.apply();
        } else {
        	settings.commit();
        }
		
	    if(state) {
	    	startAlarm();
	    } else {
	    	cancelAlarm();
	    }
	}
	
	
	public static boolean notificationsOnBootEnabled() {
		SharedPreferences settings = AppInstance.getContext().getSharedPreferences(Constants.PREFS, 0);
	    return settings.getBoolean(Constants.PREF_NOTIFY_ON_BOOT, true);
	}
	
	@SuppressLint("NewApi")
	public static void setNotificationOnBootEnabled(boolean state) {
		SharedPreferences.Editor settings = AppInstance.getContext().getSharedPreferences(Constants.PREFS, 0).edit();
		settings.putBoolean(Constants.PREF_NOTIFY_ON_BOOT, state);
        if (android.os.Build.VERSION.SDK_INT >= 9) {
        	settings.apply();
        } else {
        	settings.commit();
        }
	}

	public static boolean notificationsIconEnabled() {
		SharedPreferences settings = AppInstance.getContext().getSharedPreferences(Constants.PREFS, 0);
	    return settings.getBoolean(Constants.PREF_NOTIFY_ICON, false);
	}
	
	@SuppressLint("NewApi")
	public static void setNotificationIconEnabled(boolean state) {
		SharedPreferences.Editor settings = AppInstance.getContext().getSharedPreferences(Constants.PREFS, 0).edit();
		settings.putBoolean(Constants.PREF_NOTIFY_ICON, state);
        if (android.os.Build.VERSION.SDK_INT >= 9) {
        	settings.apply();
        } else {
        	settings.commit();
        }
	}	

	public static boolean MobileHiResEnabled() {
		SharedPreferences settings = AppInstance.getContext().getSharedPreferences(Constants.PREFS, 0);
	    return settings.getBoolean(Constants.PREF_MOBILE_HIRES, false);
	}
	
	@SuppressLint("NewApi")
	public static void setMobileHiResEnabled(boolean state) {
		SharedPreferences.Editor settings = AppInstance.getContext().getSharedPreferences(Constants.PREFS, 0).edit();
		settings.putBoolean(Constants.PREF_MOBILE_HIRES, state);
        if (android.os.Build.VERSION.SDK_INT >= 9) {
        	settings.apply();
        } else {
        	settings.commit();
        }
	}	
	
	public static boolean WifiHiResEnabled() {
		SharedPreferences settings = AppInstance.getContext().getSharedPreferences(Constants.PREFS, 0);
	    return settings.getBoolean(Constants.PREF_WIFI_HIRES, true);
	}
	
	@SuppressLint("NewApi")
	public static void setWifiHiResEnabled(boolean state) {
		SharedPreferences.Editor settings = AppInstance.getContext().getSharedPreferences(Constants.PREFS, 0).edit();
		settings.putBoolean(Constants.PREF_WIFI_HIRES, state);
        if (android.os.Build.VERSION.SDK_INT >= 9) {
        	settings.apply();
        } else {
        	settings.commit();
        }
	}
	
	/*public static boolean stringNotNullOrEmpty(String s) {
		return s != null && s.length() > 0;
	}*/
	
	public static boolean isDeviceTablet(){
		return (AppInstance.getContext().getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
	            >= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}
	
	public static boolean isDeviceLandscape(){
		return (AppInstance.getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
	}

}
