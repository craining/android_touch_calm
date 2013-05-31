package com.craining.blog.touchcalm.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import com.craining.blog.touchcalm.R;
import com.craining.blog.touchcalm.TouchCalmHelper;

public class TouchWidgetService extends Service {

	@Override
	public void onCreate() {
		if (TouchCalmHelper.DIR_TAG_OPEN.exists()) {
			Toast.makeText(getBaseContext(), R.string.widget_text_closing, Toast.LENGTH_SHORT).show();
			if (TouchCalmHelper.DIR_TAG_OPEN.delete()) {
				// 关闭监听服务
				stopService(new Intent(TouchWidgetService.this, TouchCalmService.class));
			} else {
				Toast.makeText(getBaseContext(), R.string.widget_text_close_fail, Toast.LENGTH_SHORT).show();			
			}
		} else {
			Toast.makeText(getBaseContext(), R.string.widget_text_opening, Toast.LENGTH_SHORT).show();
			
			if (TouchCalmHelper.DIR_TAG_OPEN.mkdir()) {
				// 开启监听服务
				startService(new Intent(TouchWidgetService.this, TouchCalmService.class));
			} else {
				Toast.makeText(getBaseContext(), R.string.widget_text_open_fail, Toast.LENGTH_SHORT).show();
			}
		}

		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId) {

		stopSelf();
		super.onStart(intent, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
