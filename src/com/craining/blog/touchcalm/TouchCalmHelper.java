package com.craining.blog.touchcalm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import com.craining.blog.touchcalm.widget.TouchWidgetProvider;

import android.app.ActivityManager;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;

public class TouchCalmHelper {

	public static final String[] AUTHOR_EMAIL_ADDRESS = {"craining@163.com"};
	public static final String AUTHOR_EMAIL_SUB = "TouchCalm-1.0";
	
	public static final File DIR_TAG_OPEN = new File("/data/data/com.craining.blog.touchcalm/open");

	public static final String TAG_RING = "ring";
	public static final String TAG_MEDIA = "media";
	public static final String TAG_CLOCK = "clock";
	public static final String TAG_NOTICE = "notice";
	public static final String TAG_SYSTEM = "system";
	public static final String TAG_VOICE = "voice";
	public static final String TAG_DTMF = "dtmf";
	public static final String TAG_RING_VIBRATE = "ringv";
	public static final String TAG_NOTICE_VIBRATE = "noticev";

	// 极值，用于 SeekBar.setMax()等操作
	public static final int int_RingU = 7;
	public static final int int_MediaU = 15;
	public static final int int_NoticeU = 7;
	public static final int int_ClockU = 7;
	public static final int int_SystemU = 7;
	public static final int int_VoiceU = 5;
	public static final int int_DtmfU = 15;

	// 默认设置：
	//手机反扣音量
	public static final int default_RingD = 0;
	public static final int default_MediaD = 0;
	public static final int default_NoticeD = 0;
	public static final int default_ClockD = 0;
	public static final int default_SystemD = 0;
	public static final int default_VoiceD = 5;
	public static final int default_DtmfD = 0;
	//手机仰卧音量
	public static final int default_RingU = 5;
	public static final int default_MediaU = 4;
	public static final int default_NoticeU = 5;
	public static final int default_ClockU = 5;
	public static final int default_SystemU = 5;
	public static final int default_VoiceU = 5;
	public static final int default_DtmfU = 10;
	//是否开启自动设置
	public static final boolean b_CheckRing_On = true;
	public static final boolean b_CheckMedia_On = false;
	public static final boolean b_CheckNotice_On = true;
	public static final boolean b_CheckClock_On = true;
	public static final boolean b_CheckSystem_On = true;
	public static final boolean b_CheckVoice_On = false;
	public static final boolean b_CheckDtmf_On = true;
	public static final boolean b_CheckRingV_On = true;
	public static final boolean b_CheckNoticeV_On = true;
	
	public static final boolean b_CheckRingVD_On = true;
	public static final boolean b_CheckRingVU_On = false;
	public static final boolean b_CheckNoticeVD_On = true;
	public static final boolean b_CheckNoticeVU_On = false;
	

	public static final int DATA_ITEM_SIZE = 9;// 音量种类数目

	public static final int SENSOR_SENSIBILITY_MAX = 8;// 灵敏度最大值
	public static final int SENSOR_SENSIBILITY_DEFAULT = 1;// 默认灵敏度
	public static final int SENSOR_SENSIBILITY_TEMP = 8;
	// 灵敏度对照：
	// 灵敏度： 7 6 5 4 3 2 1 0
	// 传感器Z值：-1 -2 -3 -4 -5 -6 -7 -8
	// 计算： 传感器z的临界值 = 灵敏度 - 10

	public static final File FILE_KEEP_SENSIBILITY = new File("/data/data/com.craining.blog.touchcalm/files/sensibility.file");
	public static final String FILE_NAME_KEEP_SENSIBILITY = "sensibility.file";
	
	public static final File DIR_SOUND_ON = new File("/data/data/com.craining.blog.touchcalm/sound");
	public static final File DIR_VIBRATE_OFF = new File("/data/data/com.craining.blog.touchcalm/vibrate");
//	public static final File DIR_TITLEBR_SHOW = new File("/data/data/com.craining.blog.touchcalm/titleshow");

	public static final File DIR_CTRL_SCREEN_ON = new File("/data/data/com.craining.blog.touchcalm/screen");
	
	public static final String FLOAT_SERVICE_NAME = "com.craining.blog.touchcalm.floatview.FloatService";
	public static final String MAIN_SERVICE_NAME = "com.craining.blog.touchcalm.service.TouchCalmService";
	private static final String FILE_ENCODING = "utf-8";

	
	public static final String TAG_EXTRA_SHOW_TITLEICO = "showornot";
	
	/**
	 * 通过Service的类名来判断是否启动某个服务
	 * 
	 * @param mServiceList
	 * @param className
	 * @return
	 */
	public static boolean serviceIsRunning(Context context, String serviceName) {

		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> mServiceList = activityManager.getRunningServices(100);

		for (int i = 0; i < mServiceList.size(); i++) {
			if (serviceName.equals(mServiceList.get(i).service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * android file保存
	 * 
	 * @param fileName
	 * @param toSave
	 * @return
	 */
	public static boolean androidFileSave(Context con, String fileName, String toSave) {
		Properties properties = new Properties();
		properties.put(FILE_ENCODING, toSave);
		try {
			FileOutputStream stream = con.openFileOutput(fileName, Context.MODE_WORLD_WRITEABLE);
			properties.store(stream, "");
		} catch (FileNotFoundException e) {
			return false;
		} catch (IOException e) {
			return false;
		}

		return true;
	}

	/**
	 * android file读取数据
	 * 
	 * @param fileName
	 * @return
	 */
	public static String androidFileload(Context con, String fileName) {
		Properties properties = new Properties();
		try {
			FileInputStream stream = con.openFileInput(fileName);
			properties.load(stream);
		} catch (FileNotFoundException e) {
			return null;
		} catch (IOException e) {
			return null;
		}

		return properties.get(FILE_ENCODING).toString();
	}

	

	public static void toUpdateWidgetAlarmState(Context context) {
		AppWidgetManager gm = AppWidgetManager.getInstance(context);
		int[] appWidgetIds = gm.getAppWidgetIds(new ComponentName(context, TouchWidgetProvider.class));
		// 更新所有AppWidget
		if (appWidgetIds != null) {
			final int N = appWidgetIds.length;
			for (int i = 0; i < N; i++) {
				TouchWidgetProvider.updateAppWidget(context, gm, appWidgetIds[i]);
			}

		}
	}

	
}
