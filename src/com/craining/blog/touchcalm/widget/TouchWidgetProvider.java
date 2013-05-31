package com.craining.blog.touchcalm.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.craining.blog.touchcalm.R;
import com.craining.blog.touchcalm.TouchCalmHelper;
import com.craining.blog.touchcalm.service.TouchWidgetService;

public class TouchWidgetProvider extends AppWidgetProvider {

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		final int N = appWidgetIds.length;
		for (int i = 0; i < N; i++) {
			int appWidgetId = appWidgetIds[i];
			Log.i("deleteId", "this is [" + appWidgetId + "] onDelete!");
		}
	}

//	@Override
//	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
//
//		final int N = appWidgetIds.length;
//		for (int i = 0; i < N; i++) {
//			int appWidgetId = appWidgetIds[i];
//			Log.i("myLog", "this is [" + appWidgetId + "] onUpdate!");
//
//		}
//		super.onUpdate(context, appWidgetManager, appWidgetIds);
//	}

	// ��AppWidgetProvider�ṩ�����һ��������ɾ��ʱ����
	public void onDisabled(Context context) {
		PackageManager pm = context.getPackageManager();
		pm.setComponentEnabledSetting(new ComponentName(context.getPackageName(), ".MyBroadcastReceiver"), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
		Log.e("DelWidgetProvider", "++++++++++++++++++++++++++++++++++++++++============> OnDelAll");
		/* �رվ��� */
		if (TouchCalmHelper.DIR_TAG_OPEN.exists()) {
			TouchCalmHelper.DIR_TAG_OPEN.delete();
		}
	}

	/**
	 * ����һ��Widget
	 * 
	 * @param context
	 * @param appWidgetManager
	 * @param appWidgetId
	 */
	public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
		Log.e("WidgetProvider", "UpdateAppWidget Methord is Running");
		/* ����RemoteViews�����������沿�����и��� */
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.layout_widget);

		// ����widget���ж��Ƿ����ù���
		if (TouchCalmHelper.DIR_TAG_OPEN.exists() && TouchCalmHelper.serviceIsRunning(context, TouchCalmHelper.MAIN_SERVICE_NAME)) {
			Log.e("", "change  img");
			views.setTextViewText(R.id.text_widget, context.getString(R.string.widget_text_open));
			views.setImageViewResource(R.id.img_widget, R.drawable.ic_auto);
		} else {
			Log.e("", "change  img");
			views.setTextViewText(R.id.text_widget, context.getString(R.string.widget_text_close));
			views.setImageViewResource(R.id.img_widget, R.drawable.ic_unauto);
		}

		Intent intent0 = new Intent(context, TouchWidgetService.class);
		// �˴��мǣ� ��Ҫ��requestCode����Ϊһ����ֵ������bundle���͵�ֵ����������ӵ�widgetID
		PendingIntent pendingIntent0 = PendingIntent.getService(context, appWidgetId, intent0, PendingIntent.FLAG_UPDATE_CURRENT);
		views.setOnClickPendingIntent(R.id.img_widget, pendingIntent0);

		appWidgetManager.updateAppWidget(appWidgetId, views);
	}

}
