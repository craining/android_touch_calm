package com.craining.blog.touchcalm.widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class TouchWidgetConfig extends Activity{

	private int mAppWidgetId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setResult(RESULT_CANCELED);
		
			// Find the widget id from the intent.
			Intent intent = getIntent();
			Bundle extras = intent.getExtras();
			if (extras != null) {
				mAppWidgetId = extras.getInt(
						AppWidgetManager.EXTRA_APPWIDGET_ID,
						AppWidgetManager.INVALID_APPWIDGET_ID);
			}

			// If they gave us an intent without the widget id, just bail.
			if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
				finish();
			}

			// return OK
			Intent resultValue = new Intent();
			resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
					mAppWidgetId);

			// 取得AppWidgetManager实例
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(TouchWidgetConfig.this);
			// 更新AppWidget
			TouchWidgetProvider.updateAppWidget(TouchWidgetConfig.this, appWidgetManager, mAppWidgetId);
			setResult(RESULT_OK, resultValue);
			finish();
	}
}