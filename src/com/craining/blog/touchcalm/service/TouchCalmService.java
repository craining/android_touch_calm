package com.craining.blog.touchcalm.service;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.util.Log;

import com.craining.blog.touchcalm.LockScreenAdmin;
import com.craining.blog.touchcalm.R;
import com.craining.blog.touchcalm.GetProtityActivity;
import com.craining.blog.touchcalm.TouchCalmHelper;
import com.craining.blog.touchcalm.db.DataBaseAdapter;

public class TouchCalmService extends Service {

	private SensorManager sensorMgr;
	private AudioManager audioMgr;
	private SensorEventListener sensorListener;
	private Sensor orientation;
	private PowerManager pm;
	private WakeLock wakeLock;
	// private WakeLock screenLock;
	// private KeyguardLock keyguardLock;
	// private KeyguardManager km;
	private String TAG = "services";

	private boolean tag = false;// 为了使其仅在每次反转时才执行
	private boolean bool_playSound;
	private boolean bool_doVibrate;
	private boolean bool_phoneDown;
	private float double_sensitivity = -6;

	private static final int MSG_PLAY_SOUND = 101;
	private static final int MSG_DO_VIBRATE = 102;
	private static final int MSG_SET_UP = 103;
	private static final int MSG_SET_DOWN = 104;
	private static final int MSG_SCREEN_ON_OFF = 105;
	private setHandler handler_set;
	private SoundPool pool = null;
	private Vibrator vb = null;
	private int sound_up;
	private int sound_down;

	@Override
	public IBinder onBind(Intent intent) {
		Log.e(TAG, "MAIN SERVICE BInDED!");
		return null;
	}

	@Override
	public void onCreate() {
		Log.e(TAG, "MAIN SERVICE CREATED!");

		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, this.getClass().getCanonicalName());
		wakeLock.acquire();

		sensorMgr = (SensorManager) this.getSystemService(SENSOR_SERVICE);
		audioMgr = (AudioManager) this.getSystemService(AUDIO_SERVICE);
		sensorListener = new SensorListener();
		handler_set = new setHandler();
		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.e(TAG, "Start");

		TouchCalmHelper.toUpdateWidgetAlarmState(TouchCalmService.this);// 更新widget

		getSetData();

		List<Sensor> sensorList = sensorMgr.getSensorList(Sensor.TYPE_ALL);
		int size = sensorList.size();
		for (int i = 0; i < size; i++) {
			int type = sensorList.get(i).getType();
			Log.d(TAG, "sensor type:" + String.valueOf(type));
			if (type == Sensor.TYPE_ACCELEROMETER) {// 找到我们要的感应器
				orientation = sensorList.get(i);
				break;
			}
		}
		if (orientation != null) { // 注册感应监听器
			sensorMgr.registerListener(sensorListener, orientation, SensorManager.SENSOR_DELAY_NORMAL);
		} else { // 没有对应的Sensor,取消.
			stopSelf();
		}
		super.onStart(intent, startId);
	}

	@Override
	public void onRebind(Intent intent) {
		Log.e(TAG, "rebind");
		super.onRebind(intent);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.e(TAG, "unbind");
		return super.onUnbind(intent);
	}

	@Override
	public void onDestroy() {
		TouchCalmHelper.toUpdateWidgetAlarmState(TouchCalmService.this);
		sensorMgr.unregisterListener(sensorListener, orientation);// 释放sensor
		if (TouchCalmHelper.DIR_TAG_OPEN.exists()) {
			TouchCalmHelper.DIR_TAG_OPEN.delete();
		}
		// 释放振动和音池
		if (vb != null) {
			vb.cancel();
			vb = null;
		}
		if (pool != null) {
			pool.release();
			pool = null;
		}
		if (wakeLock != null && wakeLock.isHeld()) {
			wakeLock.release();
			wakeLock = null;
		}
		Log.e(TAG, "MAIN SERVICE destroyed!");
		super.onDestroy();
	}

	/**
	 * 
	 */
	private void getSetData() {
		// 获得感应设置数据，
		// 1、开启振动否？开启提示音否？开启状态栏显示否？
		if (TouchCalmHelper.DIR_SOUND_ON.exists()) {
			bool_playSound = true;
			pool = new SoundPool(2, AudioManager.STREAM_NOTIFICATION, 4);
			sound_up = pool.load(TouchCalmService.this, R.raw.sound_up, 0);
			sound_down = pool.load(TouchCalmService.this, R.raw.sound_down, 0);

		} else {
			bool_playSound = false;
		}
		if (!TouchCalmHelper.DIR_VIBRATE_OFF.exists()) {
			bool_doVibrate = true;
		} else {
			bool_doVibrate = false;
		}
		// if(TouchCalmHelper.DIR_TAG_OPEN.exists() &&
		// !TouchCalmHelper.DIR_TITLEBR_SHOW.exists()) {
		// 状态栏显示
		// }
		// 2、获得感应数据
		if (TouchCalmHelper.FILE_KEEP_SENSIBILITY.exists()) {
			String getSensitivityValue = TouchCalmHelper.androidFileload(TouchCalmService.this, TouchCalmHelper.FILE_NAME_KEEP_SENSIBILITY);
			double_sensitivity = Float.parseFloat(getSensitivityValue);
		} else {
			// 若丢失，则恢复默认
			double_sensitivity = TouchCalmHelper.SENSOR_SENSIBILITY_DEFAULT - TouchCalmHelper.SENSOR_SENSIBILITY_TEMP;
			TouchCalmHelper.androidFileSave(TouchCalmService.this, TouchCalmHelper.FILE_NAME_KEEP_SENSIBILITY, "" + double_sensitivity);
		}
	}

	protected void phoneUpSet() {
		DataBaseAdapter db_helper = new DataBaseAdapter(TouchCalmService.this);
		db_helper.open();
		ArrayList<String> arrylist_style = DataBaseAdapter.getColumnThingsInf(DataBaseAdapter.KEY_STYLE);
		ArrayList<String> arrylist_upValue = DataBaseAdapter.getColumnThingsInf(DataBaseAdapter.KEY_UP);
		ArrayList<String> arrylist_effectiveValue = DataBaseAdapter.getColumnThingsInf(DataBaseAdapter.KEY_EFFECTIVE);
		db_helper.close();
		int count = arrylist_style.size();

		for (int m = 0; m < count; m++) {
			if (arrylist_effectiveValue.get(m).equals("1")) {
				if (arrylist_style.get(m).equals(TouchCalmHelper.TAG_RING)) {
					audioMgr.setStreamVolume(AudioManager.STREAM_RING, Integer.parseInt(arrylist_upValue.get(m)), 0);
				} else if (arrylist_style.get(m).equals(TouchCalmHelper.TAG_MEDIA)) {
					audioMgr.setStreamVolume(AudioManager.STREAM_MUSIC, Integer.parseInt(arrylist_upValue.get(m)), 0);
				} else if (arrylist_style.get(m).equals(TouchCalmHelper.TAG_CLOCK)) {
					audioMgr.setStreamVolume(AudioManager.STREAM_ALARM, Integer.parseInt(arrylist_upValue.get(m)), 0);
				} else if (arrylist_style.get(m).equals(TouchCalmHelper.TAG_NOTICE)) {
					audioMgr.setStreamVolume(AudioManager.STREAM_NOTIFICATION, Integer.parseInt(arrylist_upValue.get(m)), 0);
				} else if (arrylist_style.get(m).equals(TouchCalmHelper.TAG_VOICE)) {
					audioMgr.setStreamVolume(AudioManager.STREAM_VOICE_CALL, Integer.parseInt(arrylist_upValue.get(m)), 0);
				} else if (arrylist_style.get(m).equals(TouchCalmHelper.TAG_DTMF)) {
					audioMgr.setStreamVolume(AudioManager.STREAM_DTMF, Integer.parseInt(arrylist_upValue.get(m)), 0);
				} else if (arrylist_style.get(m).equals(TouchCalmHelper.TAG_SYSTEM)) {
					audioMgr.setStreamVolume(AudioManager.STREAM_SYSTEM, Integer.parseInt(arrylist_upValue.get(m)), 0);
				} else if (arrylist_style.get(m).equals(TouchCalmHelper.TAG_RING_VIBRATE)) {
					audioMgr.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, Integer.parseInt(arrylist_upValue.get(m)));
				} else if (arrylist_style.get(m).equals(TouchCalmHelper.TAG_NOTICE_VIBRATE)) {
					audioMgr.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, Integer.parseInt(arrylist_upValue.get(m)));
				}
			}
		}

	}

	protected void phoneDownSet() {

		DataBaseAdapter db_helper = new DataBaseAdapter(TouchCalmService.this);
		db_helper.open();
		ArrayList<String> arrylist_style = DataBaseAdapter.getColumnThingsInf(DataBaseAdapter.KEY_STYLE);
		ArrayList<String> arrylist_downValue = DataBaseAdapter.getColumnThingsInf(DataBaseAdapter.KEY_DOWN);
		ArrayList<String> arrylist_effectiveValue = DataBaseAdapter.getColumnThingsInf(DataBaseAdapter.KEY_EFFECTIVE);
		db_helper.close();
		int count = arrylist_style.size();
		// 不播放提示音
		for (int n = 0; n < count; n++) {
			if (arrylist_effectiveValue.get(n).equals("1")) {
				if (arrylist_style.get(n).equals(TouchCalmHelper.TAG_RING)) {
					audioMgr.setStreamVolume(AudioManager.STREAM_RING, Integer.parseInt(arrylist_downValue.get(n)), 0);
				} else if (arrylist_style.get(n).equals(TouchCalmHelper.TAG_MEDIA)) {
					audioMgr.setStreamVolume(AudioManager.STREAM_MUSIC, Integer.parseInt(arrylist_downValue.get(n)), 0);
				} else if (arrylist_style.get(n).equals(TouchCalmHelper.TAG_CLOCK)) {
					audioMgr.setStreamVolume(AudioManager.STREAM_ALARM, Integer.parseInt(arrylist_downValue.get(n)), 0);
				} else if (arrylist_style.get(n).equals(TouchCalmHelper.TAG_NOTICE)) {
					audioMgr.setStreamVolume(AudioManager.STREAM_NOTIFICATION, Integer.parseInt(arrylist_downValue.get(n)), 0);
				} else if (arrylist_style.get(n).equals(TouchCalmHelper.TAG_VOICE)) {
					audioMgr.setStreamVolume(AudioManager.STREAM_VOICE_CALL, Integer.parseInt(arrylist_downValue.get(n)), 0);
				} else if (arrylist_style.get(n).equals(TouchCalmHelper.TAG_DTMF)) {
					audioMgr.setStreamVolume(AudioManager.STREAM_DTMF, Integer.parseInt(arrylist_downValue.get(n)), 0);
				} else if (arrylist_style.get(n).equals(TouchCalmHelper.TAG_SYSTEM)) {
					audioMgr.setStreamVolume(AudioManager.STREAM_SYSTEM, Integer.parseInt(arrylist_downValue.get(n)), 0);
				} else if (arrylist_style.get(n).equals(TouchCalmHelper.TAG_RING_VIBRATE)) {
					audioMgr.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, Integer.parseInt(arrylist_downValue.get(n)));
				} else if (arrylist_style.get(n).equals(TouchCalmHelper.TAG_NOTICE_VIBRATE)) {
					audioMgr.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, Integer.parseInt(arrylist_downValue.get(n)));
				}
			}
		}

	}// method

	class setThread extends Thread {

		@Override
		public void run() {
			// Log.e("thread", "thread  !!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			try {
				if (bool_playSound) {
					handler_set.sendEmptyMessageDelayed(MSG_PLAY_SOUND, 0);// 提示音
				}
				if (bool_doVibrate) {
					handler_set.sendEmptyMessageDelayed(MSG_DO_VIBRATE, 0);// 振动
				}
				if (TouchCalmHelper.DIR_CTRL_SCREEN_ON.exists()) {
					handler_set.sendEmptyMessageDelayed(MSG_SCREEN_ON_OFF, 0);// 屏幕开关
				}
				sleep(500);// 等待
				if (bool_phoneDown) {
					handler_set.sendEmptyMessageDelayed(MSG_SET_DOWN, 0);
				} else {
					handler_set.sendEmptyMessageDelayed(MSG_SET_UP, 0);
				}
			} catch (Exception e) {
				System.out.print(e);
				Log.e("set thread", "ERROR!!!!!!!!!");
			}
			super.run();
		}
	}

	private class setHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case MSG_PLAY_SOUND: {
				audioMgr.setStreamVolume(AudioManager.STREAM_NOTIFICATION, TouchCalmHelper.int_MediaU, 0);
				// 播放提示音
				if (bool_phoneDown) {
					pool.play(sound_down, 0.3f, 0.3f, 0, 0, 1);
					Log.e("s", "s  !!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				} else {
					pool.play(sound_up, 0.3f, 0.3f, 0, 0, 1);
					Log.e("s", "s  !!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				}
				TouchCalmHelper.toUpdateWidgetAlarmState(TouchCalmService.this);
				break;
			}

			case MSG_DO_VIBRATE: {
				// 振动提示：
				Log.e("V", "V  !!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				vb = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);
				vb.vibrate(new long[] { 0, 50 }, -1);
				TouchCalmHelper.toUpdateWidgetAlarmState(TouchCalmService.this);
				break;
			}

			case MSG_SCREEN_ON_OFF: {
				if (!bool_phoneDown) {
					// 开启屏幕
					PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
					PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
					wl.acquire();
					wl.release();
				} else {
					// 关闭屏幕
					DevicePolicyManager policyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
					ComponentName componentName = new ComponentName(TouchCalmService.this, LockScreenAdmin.class);
					boolean active = policyManager.isAdminActive(componentName);
					if (active) {
						policyManager.lockNow();
					}
				}
			}
				break;

			case MSG_SET_UP: {

				phoneUpSet();
			}
				break;

			case MSG_SET_DOWN: {
				phoneDownSet();

			}
				break;

			default:
				break;
			}
			super.handleMessage(msg);

		}
	}

	class SensorListener implements SensorEventListener {
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			if ((int) event.values[SensorManager.DATA_Z] - double_sensitivity < 0) {// 与float作比较应作差

				// 如果电话翻转
				if (tag) {
					tag = false;
					bool_phoneDown = true;
					if (TouchCalmHelper.DIR_TAG_OPEN.exists()) {
						// Log.e("", "phoneDown()");
						Thread down = new setThread();
						down.start();
					}
				}
			} else {
				// 如果电话翻转回来
				if (!tag) {
					tag = true;
					bool_phoneDown = false;
					if (TouchCalmHelper.DIR_TAG_OPEN.exists()) {
						Thread up = new setThread();
						up.start();
					}

				}
			}
		}
	}

}
