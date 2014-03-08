package com.edaviessmith.mindcrack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmBroadcastReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {

	  if(intent.getAction() != null) {

		  boolean bootIntent =  intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED);
		  boolean bootEnabled = Util.notificationsOnBootEnabled();
		  //If booting and boot is enabled or being called regularly
		  if ((bootIntent && bootEnabled) || intent.getAction().equals(Constants.NOTIFY_ACTION)) {
			  Intent i = new Intent(context, MemberActivityService.class);
		      context.startService(i);  
		      Util.startAlarm();
		  }
	  }
  }

} 