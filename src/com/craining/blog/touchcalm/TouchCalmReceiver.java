package com.craining.blog.touchcalm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.craining.blog.touchcalm.service.TouchCalmService;

public class TouchCalmReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {

			if(TouchCalmHelper.DIR_TAG_OPEN.exists()) {
				Intent i = new Intent(context, TouchCalmService.class);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startService(i);
			}
		}
	}
}
