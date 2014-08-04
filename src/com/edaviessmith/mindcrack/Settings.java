package com.edaviessmith.mindcrack;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;
import android.widget.ToggleButton;
import com.edaviessmith.mindcrack.R;

public class Settings extends Activity {

	Spinner picker_mobile, picker_wifi;
	ToggleButton toggle_notifications, toggle_notifications_on_boot, toggle_notifications_icon, toggle_mobile_hires, toggle_wifi_hires; 
	Context context;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		// Show the Up button in the action bar.
		//getActionBar().setDisplayHomeAsUpEnabled(true);
		context = getApplicationContext();
		final SharedPreferences settings = context.getSharedPreferences(Constants.PREFS, 0);
		
		
		toggle_notifications = (ToggleButton)findViewById(R.id.toggle_notifications);
		toggle_notifications.setChecked(Util.notificationsEnabled(context));
		toggle_notifications.setOnClickListener(new OnClickListener() {

	        @Override
	        public void onClick(View view) {
	        	Util.setNotificationEnabled(context, toggle_notifications.isChecked());
	        }
	    });
		
		toggle_notifications_on_boot = (ToggleButton)findViewById(R.id.toggle_notifications_on_boot);
		toggle_notifications_on_boot.setChecked(Util.notificationsOnBootEnabled());
		toggle_notifications_on_boot.setOnClickListener(new OnClickListener() {

	        @Override
	        public void onClick(View view) {
	        	Util.setNotificationOnBootEnabled(toggle_notifications_on_boot.isChecked());
	        }
	    });
		
		toggle_notifications_icon = (ToggleButton)findViewById(R.id.toggle_notifications_icon);
		toggle_notifications_icon.setChecked(Util.notificationsIconEnabled());
		toggle_notifications_icon.setOnClickListener(new OnClickListener() {

	        @Override
	        public void onClick(View view) {
	        	Util.setNotificationIconEnabled(toggle_notifications_icon.isChecked());
	        }
	    });
		

		long wifi_time = settings.getLong(Constants.PREF_WIFI_TIME, 60L);
		long mobile_time = settings.getLong(Constants.PREF_NETWORK_TIME, 180L);
		
		picker_mobile = (Spinner) findViewById(R.id.picker_mobile);
		picker_mobile.setSelection(longToIndex(mobile_time));
		picker_mobile.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				SharedPreferences.Editor editor = settings.edit();
				editor.putLong(Constants.PREF_NETWORK_TIME , indexToLong(position));
				editor.commit();
				Util.startAlarm();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) { }
		});
		
		
		picker_wifi = (Spinner) findViewById(R.id.picker_wifi);
		picker_wifi.setSelection(longToIndex(wifi_time));
		picker_wifi.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				SharedPreferences.Editor editor = settings.edit();
				editor.putLong(Constants.PREF_WIFI_TIME , indexToLong(position));
				editor.commit();
				Util.startAlarm();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) { }
		});
		
		toggle_wifi_hires = (ToggleButton)findViewById(R.id.toggle_wifi_hires);
		toggle_wifi_hires.setChecked(Util.WifiHiResEnabled());
		toggle_wifi_hires.setOnClickListener(new OnClickListener() {

	        @Override
	        public void onClick(View view) {
	        	Util.setWifiHiResEnabled(toggle_wifi_hires.isChecked());
	        }
	    });
		
		toggle_mobile_hires = (ToggleButton)findViewById(R.id.toggle_mobile_hires);
		toggle_mobile_hires.setChecked(Util.MobileHiResEnabled());
		toggle_mobile_hires.setOnClickListener(new OnClickListener() {
	        @Override
	        public void onClick(View view) {
	        	Util.setMobileHiResEnabled(toggle_mobile_hires.isChecked());
	        }
	    });
		
	}

	private int longToIndex(long time) {
		if(time == 0L) return 0;
		if(time == 15L) return 1;
		if(time == 30L) return 2;
		if(time == 60L) return 3;
		if(time == 180L) return 4;
		if(time == 360L) return 5;
		if(time == 720L) return 6;
		if(time == 1440L) return 7;
		return 0;
		
	}
	
	private long indexToLong(int index) {
		switch(index) {
			case 0: return 0L;
			case 1: return 15L;
			case 2: return 30L;
			case 3: return 60L;
			case 4: return 180L;
			case 5: return 360L;
			case 6: return 720L;
			case 7: return 1440L;
		}
		return 0L;
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getSupportMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
	    case R.id.about_button:
	    	Intent intent = new Intent(this, About.class);
	    	startActivity(intent);
            return true;
		
		}
		return super.onOptionsItemSelected(item);
	}

}
