package com.craining.blog.touchcalm;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.craining.blog.touchcalm.db.DataBaseAdapter;
import com.craining.blog.touchcalm.floatview.FloatService;
import com.craining.blog.touchcalm.service.TouchCalmService;

public class TouchCalmActivity extends Activity {

	private Button btn_reset;
	private Button btn_set;
	private Button btn_help;
	private Button btn_exit;
	private TextView text_sound;

	private SeekBar seek_down_ring;
	private SeekBar seek_up_ring;
	private SeekBar seek_down_media;
	private SeekBar seek_up_media;
	private SeekBar seek_down_clock;
	private SeekBar seek_up_clock;
	private SeekBar seek_down_notice;
	private SeekBar seek_up_notice;
	private SeekBar seek_down_system;
	private SeekBar seek_up_system;
	private SeekBar seek_down_voice;
	private SeekBar seek_up_voice;
	private SeekBar seek_down_dtmf;
	private SeekBar seek_up_dtmf;
	private ToggleButton tglbtn_down_ring;
	private ToggleButton tglbtn_down_notice;
	private ToggleButton tglbtn_up_ring;
	private ToggleButton tglbtn_up_notice;

	private ToggleButton tglbtn_ring_effective;
	private ToggleButton tglbtn_media_effective;
	private ToggleButton tglbtn_clock_effective;
	private ToggleButton tglbtn_notice_effective;
	private ToggleButton tglbtn_system_effective;
	private ToggleButton tglbtn_voice_effective;
	private ToggleButton tglbtn_dtmf_effective;
	private ToggleButton tglbtn_ring_v_effective;
	private ToggleButton tglbtn_notice_v_effective;
	private ToggleButton tglbtn_screen_ctrl_effective;

	private ArrayList<String> array_style = new ArrayList<String>();
	private ArrayList<String> array_down = new ArrayList<String>();
	private ArrayList<String> array_up = new ArrayList<String>();
	private ArrayList<String> array_effective = new ArrayList<String>();

	private static final int EVENT_TIME_TO_UPDATE_SOUND_TEXTVIEW = 100;
	private static final int EVENT_TIME_TO_UPDATE_DEVICE = 120;
	public Handler mainHandler = new mHandler();

	private DevicePolicyManager policyManager;
	private ComponentName componentName;

	@Override
	public void onConfigurationChanged(Configuration newConfig) {

		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.main);

		if (TouchCalmHelper.DIR_TAG_OPEN.exists()) {
			startService(new Intent(TouchCalmActivity.this, TouchCalmService.class));
		}
		policyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		componentName = new ComponentName(TouchCalmActivity.this, LockScreenAdmin.class);
		
		TouchCalmHelper.toUpdateWidgetAlarmState(TouchCalmActivity.this);

		getViewsId();

		setSeekBarMax();// 初始化各个seekbar

		aboutDataGetAndViewShow();

		tglbtn_screen_ctrl_effective.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				if (tglbtn_screen_ctrl_effective.isChecked()) {
					// 激活设备控制器权限
					Intent intent = new Intent(TouchCalmActivity.this, GetProtityActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
//					Toast.makeText(TouchCalmActivity.this, R.string.toast_active_ctrl, Toast.LENGTH_SHORT).show();
					tglbtn_screen_ctrl_effective.setChecked(true);
				} else {
					// 解除权限
					boolean active = policyManager.isAdminActive(componentName);
					if (active) {
						policyManager.removeActiveAdmin(componentName);
					}
				}
			}
		});
		tglbtn_media_effective.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				if (tglbtn_media_effective.isChecked()) {
					Toast.makeText(getBaseContext(), R.string.warn_media_auto, Toast.LENGTH_SHORT).show();
				}
			}
		});

		tglbtn_voice_effective.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				if (tglbtn_voice_effective.isChecked()) {
					Toast.makeText(getBaseContext(), R.string.warn_voice_auto, Toast.LENGTH_SHORT).show();
				}
			}
		});

		btn_reset.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				if (TouchCalmHelper.serviceIsRunning(TouchCalmActivity.this, TouchCalmHelper.FLOAT_SERVICE_NAME)) {
					stopService(new Intent(TouchCalmActivity.this, FloatService.class));
				} else {
					setDefault();
					saveSetData();
					stopService(new Intent(TouchCalmActivity.this, FloatService.class));
					if (TouchCalmHelper.DIR_TAG_OPEN.exists()) {
						startService(new Intent(TouchCalmActivity.this, TouchCalmService.class));
					}
					Toast.makeText(TouchCalmActivity.this, R.string.toast_str_defaultsuccess, Toast.LENGTH_SHORT).show();
				}
			}
		});
		btn_set.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				if (TouchCalmHelper.serviceIsRunning(TouchCalmActivity.this, TouchCalmHelper.FLOAT_SERVICE_NAME)) {
					stopService(new Intent(TouchCalmActivity.this, FloatService.class));
				} else {
					saveSetedData();
					if (TouchCalmHelper.DIR_TAG_OPEN.exists()) {
						startService(new Intent(TouchCalmActivity.this, TouchCalmService.class));
					}
				}
			}
		});
		btn_help.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(TouchCalmActivity.this, TouchCalmHelperActivity.class));
				if (TouchCalmHelper.serviceIsRunning(TouchCalmActivity.this, TouchCalmHelper.FLOAT_SERVICE_NAME)) {
					stopService(new Intent(TouchCalmActivity.this, FloatService.class));
				}
			}
		});
		btn_exit.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				if (TouchCalmHelper.serviceIsRunning(TouchCalmActivity.this, TouchCalmHelper.FLOAT_SERVICE_NAME)) {
					stopService(new Intent(TouchCalmActivity.this, FloatService.class));
				}
				TouchCalmHelper.toUpdateWidgetAlarmState(TouchCalmActivity.this);
				finish();
			}
		});
		text_sound.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				startService(new Intent(TouchCalmActivity.this, FloatService.class));
			}
		});

	}

	/**
	 * 
	 */
	private void getViewsId() {
		seek_down_ring = (SeekBar) findViewById(R.id.seek_down_ring);
		seek_up_ring = (SeekBar) findViewById(R.id.seek_up_ring);
		seek_down_media = (SeekBar) findViewById(R.id.seek_down_media);
		seek_up_media = (SeekBar) findViewById(R.id.seek_up_media);
		seek_down_clock = (SeekBar) findViewById(R.id.seek_down_clock);
		seek_up_clock = (SeekBar) findViewById(R.id.seek_up_clock);
		seek_down_notice = (SeekBar) findViewById(R.id.seek_down_notice);
		seek_up_notice = (SeekBar) findViewById(R.id.seek_up_notice);
		seek_down_system = (SeekBar) findViewById(R.id.seek_down_system);
		seek_up_system = (SeekBar) findViewById(R.id.seek_up_system);
		seek_down_voice = (SeekBar) findViewById(R.id.seek_down_voice);
		seek_up_voice = (SeekBar) findViewById(R.id.seek_up_voice);
		seek_down_dtmf = (SeekBar) findViewById(R.id.seek_down_dtmf);
		seek_up_dtmf = (SeekBar) findViewById(R.id.seek_up_dtmf);
		tglbtn_down_ring = (ToggleButton) findViewById(R.id.check_vibrate_ring_downlie);
		tglbtn_down_notice = (ToggleButton) findViewById(R.id.check_vibrate_notice_downlie);
		tglbtn_up_ring = (ToggleButton) findViewById(R.id.check_vibrate_ring_uplie);
		tglbtn_up_notice = (ToggleButton) findViewById(R.id.check_vibrate_notice_uplie);

		tglbtn_ring_effective = (ToggleButton) findViewById(R.id.check_ring_on);
		tglbtn_media_effective = (ToggleButton) findViewById(R.id.check_media_on);
		tglbtn_clock_effective = (ToggleButton) findViewById(R.id.check_clock_on);
		tglbtn_notice_effective = (ToggleButton) findViewById(R.id.check_notice_on);
		tglbtn_system_effective = (ToggleButton) findViewById(R.id.check_system_on);
		tglbtn_voice_effective = (ToggleButton) findViewById(R.id.check_voice_on);
		tglbtn_dtmf_effective = (ToggleButton) findViewById(R.id.check_dtmf_on);
		tglbtn_ring_v_effective = (ToggleButton) findViewById(R.id.check_ring_v_on);
		tglbtn_notice_v_effective = (ToggleButton) findViewById(R.id.check_notice_v_on);
		tglbtn_screen_ctrl_effective = (ToggleButton) findViewById(R.id.check_ctrl_screen_on);

		btn_reset = (Button) findViewById(R.id.btn_reset);
		btn_set = (Button) findViewById(R.id.btn_set);
		btn_help = (Button) findViewById(R.id.btn_help);
		btn_exit = (Button) findViewById(R.id.btn_exit);
		text_sound = (TextView) findViewById(R.id.text_sound);
	}

	/**
	 * 获得是否开启翻转手机提示声
	 */
	private void getSoundAndVibrateState() {

		Resources resource = (Resources) getBaseContext().getResources();
		ColorStateList csl;

		if (TouchCalmHelper.DIR_SOUND_ON.exists() && !TouchCalmHelper.DIR_VIBRATE_OFF.exists()) {
			text_sound.setText(getString(R.string.text_sound_on_v_on));
			csl = (ColorStateList) resource.getColorStateList(R.color.color_light_blue);
			text_sound.setTextColor(csl);
		} else if (TouchCalmHelper.DIR_SOUND_ON.exists() && TouchCalmHelper.DIR_VIBRATE_OFF.exists()) {
			text_sound.setText(getString(R.string.text_sound_on_v_off));
			csl = (ColorStateList) resource.getColorStateList(R.color.color_light_blue);
			text_sound.setTextColor(csl);
		} else if (!TouchCalmHelper.DIR_SOUND_ON.exists() && !TouchCalmHelper.DIR_VIBRATE_OFF.exists()) {
			text_sound.setText(getString(R.string.text_sound_off_v_on));
			csl = (ColorStateList) resource.getColorStateList(R.color.color_light_blue);
			text_sound.setTextColor(csl);
		} else {
			text_sound.setText(getString(R.string.text_sound_off_v_off));
			csl = (ColorStateList) resource.getColorStateList(R.color.color_red);
			text_sound.setTextColor(csl);
		}
	}

	/**
	 * 
	 */
	private void aboutDataGetAndViewShow() {
		// 获取数据库对象，先判断是否为空的，若为空，则设置为默认值：
		DataBaseAdapter db_helper = new DataBaseAdapter(TouchCalmActivity.this);
		db_helper.open();
		if (db_helper.isEmpty(db_helper)) {
			setDefault();// 设置为默认
		} else {
			getDBData(db_helper);// 获得数据
			if (array_down.size() == 0) {
				setDefault();
			} else {
				setGotDataToView();
			}
		}
		db_helper.close();
	}

	/**
	 * 
	 */
	private void getDBData(DataBaseAdapter db) {
		array_down = DataBaseAdapter.getColumnThingsInf(DataBaseAdapter.KEY_DOWN);
		array_effective = DataBaseAdapter.getColumnThingsInf(DataBaseAdapter.KEY_EFFECTIVE);
		array_style = DataBaseAdapter.getColumnThingsInf(DataBaseAdapter.KEY_STYLE);
		array_up = DataBaseAdapter.getColumnThingsInf(DataBaseAdapter.KEY_UP);
	}

	/**
	 * 
	 */
	private void setGotDataToView() {
		int count = array_down.size();
		for (int i = 0; i < count; i++) {
			if (array_style.get(i).equals(TouchCalmHelper.TAG_RING)) {
				seek_down_ring.setProgress(Integer.valueOf(array_down.get(i)));
				seek_up_ring.setProgress(Integer.valueOf(array_up.get(i)));
				tglbtn_ring_effective.setChecked(stringToBoolean(array_effective.get(i)));
			} else if (array_style.get(i).equals(TouchCalmHelper.TAG_MEDIA)) {
				seek_down_media.setProgress(Integer.valueOf(array_down.get(i)));
				seek_up_media.setProgress(Integer.valueOf(array_up.get(i)));
				tglbtn_media_effective.setChecked(stringToBoolean(array_effective.get(i)));

			} else if (array_style.get(i).equals(TouchCalmHelper.TAG_CLOCK)) {
				seek_down_clock.setProgress(Integer.valueOf(array_down.get(i)));
				seek_up_clock.setProgress(Integer.valueOf(array_up.get(i)));
				tglbtn_clock_effective.setChecked(stringToBoolean(array_effective.get(i)));
			} else if (array_style.get(i).equals(TouchCalmHelper.TAG_NOTICE)) {
				seek_down_notice.setProgress(Integer.valueOf(array_down.get(i)));
				seek_up_notice.setProgress(Integer.valueOf(array_up.get(i)));
				tglbtn_notice_effective.setChecked(stringToBoolean(array_effective.get(i)));
			} else if (array_style.get(i).equals(TouchCalmHelper.TAG_SYSTEM)) {
				seek_down_system.setProgress(Integer.valueOf(array_down.get(i)));
				seek_up_system.setProgress(Integer.valueOf(array_up.get(i)));
				tglbtn_system_effective.setChecked(stringToBoolean(array_effective.get(i)));
			} else if (array_style.get(i).equals(TouchCalmHelper.TAG_VOICE)) {
				seek_down_voice.setProgress(Integer.valueOf(array_down.get(i)));
				seek_up_voice.setProgress(Integer.valueOf(array_up.get(i)));
				tglbtn_voice_effective.setChecked(stringToBoolean(array_effective.get(i)));
			} else if (array_style.get(i).equals(TouchCalmHelper.TAG_DTMF)) {
				seek_down_dtmf.setProgress(Integer.valueOf(array_down.get(i)));
				seek_up_dtmf.setProgress(Integer.valueOf(array_up.get(i)));
				tglbtn_dtmf_effective.setChecked(stringToBoolean(array_effective.get(i)));
			} else if (array_style.get(i).equals(TouchCalmHelper.TAG_RING_VIBRATE)) {
				tglbtn_up_ring.setChecked(stringToBoolean(array_up.get(i)));
				tglbtn_down_ring.setChecked(stringToBoolean(array_down.get(i)));
				tglbtn_ring_v_effective.setChecked(stringToBoolean(array_effective.get(i)));

			} else if (array_style.get(i).equals(TouchCalmHelper.TAG_NOTICE_VIBRATE)) {
				tglbtn_up_notice.setChecked(stringToBoolean(array_up.get(i)));
				tglbtn_down_notice.setChecked(stringToBoolean(array_down.get(i)));
				tglbtn_notice_v_effective.setChecked(stringToBoolean(array_effective.get(i)));
			}

			if (!TouchCalmHelper.DIR_CTRL_SCREEN_ON.exists()) {
				tglbtn_screen_ctrl_effective.setChecked(false);
			} else {
				tglbtn_screen_ctrl_effective.setChecked(true);
			}
		}
	}

	/**
	 * 设置为默认值
	 */
	private void setDefault() {

		seek_up_ring.setProgress(TouchCalmHelper.default_RingU);
		seek_down_ring.setProgress(TouchCalmHelper.default_RingD);
		seek_down_media.setProgress(TouchCalmHelper.default_MediaD);
		seek_up_media.setProgress(TouchCalmHelper.default_MediaU);
		seek_down_clock.setProgress(TouchCalmHelper.default_ClockD);
		seek_up_clock.setProgress(TouchCalmHelper.default_ClockU);
		seek_down_notice.setProgress(TouchCalmHelper.default_NoticeD);
		seek_up_notice.setProgress(TouchCalmHelper.default_NoticeU);
		seek_down_system.setProgress(TouchCalmHelper.default_SystemD);
		seek_up_system.setProgress(TouchCalmHelper.default_SystemU);
		seek_down_voice.setProgress(TouchCalmHelper.default_VoiceD);
		seek_up_voice.setProgress(TouchCalmHelper.default_VoiceU);
		seek_down_dtmf.setProgress(TouchCalmHelper.default_DtmfD);
		seek_up_dtmf.setProgress(TouchCalmHelper.default_DtmfU);

		tglbtn_up_ring.setChecked(TouchCalmHelper.b_CheckRingVU_On);
		tglbtn_down_ring.setChecked(TouchCalmHelper.b_CheckRingVD_On);
		tglbtn_up_notice.setChecked(TouchCalmHelper.b_CheckNoticeVU_On);
		tglbtn_down_notice.setChecked(TouchCalmHelper.b_CheckNoticeVD_On);

		tglbtn_ring_effective.setChecked(TouchCalmHelper.b_CheckRing_On);
		tglbtn_media_effective.setChecked(TouchCalmHelper.b_CheckMedia_On);
		tglbtn_clock_effective.setChecked(TouchCalmHelper.b_CheckClock_On);
		tglbtn_notice_effective.setChecked(TouchCalmHelper.b_CheckNotice_On);
		tglbtn_system_effective.setChecked(TouchCalmHelper.b_CheckSystem_On);
		tglbtn_voice_effective.setChecked(TouchCalmHelper.b_CheckVoice_On);
		tglbtn_dtmf_effective.setChecked(TouchCalmHelper.b_CheckDtmf_On);
		tglbtn_ring_v_effective.setChecked(TouchCalmHelper.b_CheckRingV_On);
		tglbtn_notice_v_effective.setChecked(TouchCalmHelper.b_CheckNoticeV_On);

		int toSaveSensibilityValue = TouchCalmHelper.SENSOR_SENSIBILITY_DEFAULT - TouchCalmHelper.SENSOR_SENSIBILITY_TEMP;
		TouchCalmHelper.androidFileSave(TouchCalmActivity.this, TouchCalmHelper.FILE_NAME_KEEP_SENSIBILITY, Integer.toString(toSaveSensibilityValue));
		TouchCalmHelper.DIR_SOUND_ON.delete();
		TouchCalmHelper.DIR_VIBRATE_OFF.delete();
		if (TouchCalmHelper.DIR_CTRL_SCREEN_ON.exists()) {
			TouchCalmHelper.DIR_CTRL_SCREEN_ON.delete();
		}
		tglbtn_screen_ctrl_effective.setChecked(false);
		saveSetData();
		// TouchCalmHelper.DIR_TITLEBR_SHOW.delete();
	}

	/**
	 * 
	 */
	private void saveSetData() {
		// 设置生效
		// 获取屏幕数据
		int int_RingD = seek_down_ring.getProgress();
		int int_RingU = seek_up_ring.getProgress();
		boolean b_CheckRing_On = tglbtn_ring_effective.isChecked();

		int int_MediaD = seek_down_media.getProgress();
		int int_MediaU = seek_up_media.getProgress();
		boolean b_CheckMedia_On = tglbtn_media_effective.isChecked();

		int int_NoticeD = seek_down_notice.getProgress();
		int int_NoticeU = seek_up_notice.getProgress();
		boolean b_CheckNotice_On = tglbtn_notice_effective.isChecked();

		int int_ClockD = seek_down_clock.getProgress();
		int int_ClockU = seek_up_clock.getProgress();
		boolean b_CheckClock_On = tglbtn_clock_effective.isChecked();

		int int_SystemD = seek_down_system.getProgress();
		int int_SystemU = seek_up_system.getProgress();
		boolean b_CheckSystem_On = tglbtn_system_effective.isChecked();

		int int_VoiceD = seek_down_voice.getProgress();
		int int_VoiceU = seek_up_voice.getProgress();
		boolean b_CheckVoice_On = tglbtn_voice_effective.isChecked();

		int int_DtmfD = seek_down_dtmf.getProgress();
		int int_DtmfU = seek_up_dtmf.getProgress();
		boolean b_CheckDtmf_On = tglbtn_dtmf_effective.isChecked();

		boolean b_CheckRingVD_On = tglbtn_down_ring.isChecked();
		boolean b_CheckRingVU_On = tglbtn_up_ring.isChecked();
		boolean b_CheckRingV_On = tglbtn_ring_v_effective.isChecked();

		boolean b_CheckNoticeVD_On = tglbtn_down_notice.isChecked();
		boolean b_CheckNoticeVU_On = tglbtn_up_notice.isChecked();
		boolean b_CheckNoticeV_On = tglbtn_notice_v_effective.isChecked();
		if (!TouchCalmHelper.DIR_CTRL_SCREEN_ON.exists() && tglbtn_screen_ctrl_effective.isChecked()) {
			TouchCalmHelper.DIR_CTRL_SCREEN_ON.mkdir();
		} else if (TouchCalmHelper.DIR_CTRL_SCREEN_ON.exists() && !tglbtn_screen_ctrl_effective.isChecked()) {
			TouchCalmHelper.DIR_CTRL_SCREEN_ON.delete();
		}

		// 保存到数据库
		DataBaseAdapter db_helper = new DataBaseAdapter(TouchCalmActivity.this);
		// 先将原数据都清空再重新保存新数据
		db_helper.open();
		db_helper.clearTable();
		db_helper.insertData(TouchCalmHelper.TAG_RING, Integer.toString(int_RingD), Integer.toString(int_RingU), boolToString(b_CheckRing_On));
		db_helper.insertData(TouchCalmHelper.TAG_MEDIA, Integer.toString(int_MediaD), Integer.toString(int_MediaU), boolToString(b_CheckMedia_On));
		db_helper.insertData(TouchCalmHelper.TAG_CLOCK, Integer.toString(int_ClockD), Integer.toString(int_ClockU), boolToString(b_CheckClock_On));
		db_helper.insertData(TouchCalmHelper.TAG_NOTICE, Integer.toString(int_NoticeD), Integer.toString(int_NoticeU), boolToString(b_CheckNotice_On));
		db_helper.insertData(TouchCalmHelper.TAG_SYSTEM, Integer.toString(int_SystemD), Integer.toString(int_SystemU), boolToString(b_CheckSystem_On));
		db_helper.insertData(TouchCalmHelper.TAG_VOICE, Integer.toString(int_VoiceD), Integer.toString(int_VoiceU), boolToString(b_CheckVoice_On));
		db_helper.insertData(TouchCalmHelper.TAG_DTMF, Integer.toString(int_DtmfD), Integer.toString(int_DtmfU), boolToString(b_CheckDtmf_On));
		db_helper.insertData(TouchCalmHelper.TAG_RING_VIBRATE, boolToString(b_CheckRingVD_On), boolToString(b_CheckRingVU_On), boolToString(b_CheckRingV_On));
		db_helper.insertData(TouchCalmHelper.TAG_NOTICE_VIBRATE, boolToString(b_CheckNoticeVD_On), boolToString(b_CheckNoticeVU_On), boolToString(b_CheckNoticeV_On));
		db_helper.close();
	}

	private boolean stringToBoolean(String str) {
		if (str.equals("1")) {
			return true;
		} else {
			return false;
		}
	}

	private String boolToString(boolean bool) {
		if (bool) {
			return "1";
		} else {
			return "0";
		}
	}

	/**
	 * 初始化各个seekbar
	 */
	private void setSeekBarMax() {
		seek_down_ring.setMax(TouchCalmHelper.int_RingU);
		seek_up_ring.setMax(TouchCalmHelper.int_RingU);
		seek_down_media.setMax(TouchCalmHelper.int_MediaU);
		seek_up_media.setMax(TouchCalmHelper.int_MediaU);
		seek_down_clock.setMax(TouchCalmHelper.int_ClockU);
		seek_up_clock.setMax(TouchCalmHelper.int_ClockU);
		seek_down_notice.setMax(TouchCalmHelper.int_NoticeU);
		seek_up_notice.setMax(TouchCalmHelper.int_NoticeU);
		seek_down_system.setMax(TouchCalmHelper.int_SystemU);
		seek_up_system.setMax(TouchCalmHelper.int_SystemU);
		seek_down_voice.setMax(TouchCalmHelper.int_VoiceU);
		seek_up_voice.setMax(TouchCalmHelper.int_VoiceU);
		seek_down_dtmf.setMax(TouchCalmHelper.int_DtmfU);
		seek_up_dtmf.setMax(TouchCalmHelper.int_DtmfU);
		// 开启更新TextView
		Thread thread_update = new updateThread();
		thread_update.start();
	}

	protected void checkSensor() {
		Sensor orientation = null;
		SensorManager sensorMgr = (SensorManager) this.getSystemService(SENSOR_SERVICE);
		// 获取Sensor对象
		List<Sensor> sensorList = sensorMgr.getSensorList(Sensor.TYPE_ALL);
		int size = sensorList.size();
		for (int i = 0; i < size; i++) {
			int type = sensorList.get(i).getType();
			if (type == Sensor.TYPE_ACCELEROMETER) {// 找到我们要的感应器
				orientation = sensorList.get(i);
				break;
			}
		}
		if (orientation == null) { // 若找不到传感器
			Toast.makeText(TouchCalmActivity.this, R.string.error_nullsensor, Toast.LENGTH_LONG).show();
			finish();
		}
	}

	class SensorListener implements SensorEventListener {
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
		}
	}

	/**
	 * 
	 */
	private void saveSetedData() {
		try {
			saveSetData();
			if (TouchCalmHelper.DIR_TAG_OPEN.exists()) {
				startService(new Intent(TouchCalmActivity.this, TouchCalmService.class));
				Toast.makeText(TouchCalmActivity.this, R.string.toast_str_setsuccessok, Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(TouchCalmActivity.this, R.string.toast_str_setsuccess, Toast.LENGTH_LONG).show();
			}

		} catch (Exception e) {
			Toast.makeText(TouchCalmActivity.this, R.string.toast_str_setfail, Toast.LENGTH_LONG).show();
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private class mHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case EVENT_TIME_TO_UPDATE_SOUND_TEXTVIEW: {
				getSoundAndVibrateState();
				break;
			}
			case EVENT_TIME_TO_UPDATE_DEVICE: {
				boolean active = policyManager.isAdminActive(componentName);
				if (active) {
					tglbtn_screen_ctrl_effective.setChecked(true);
				} else {
					tglbtn_screen_ctrl_effective.setChecked(false);
				}
			}
			default:
				break;
			}
		}
	}

	/**
	 * 更新widget
	 * 
	 */
	class updateThread extends Thread {
		@Override
		public void run() {
			super.run();
			try {
				do {
					mainHandler.sendEmptyMessageDelayed(EVENT_TIME_TO_UPDATE_SOUND_TEXTVIEW, 0);
					mainHandler.sendEmptyMessageDelayed(EVENT_TIME_TO_UPDATE_DEVICE, 0);
					sleep(1000);
				} while (true);
			} catch (Exception e) {
				Log.e(" ", "Thread Error!");
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if (TouchCalmHelper.serviceIsRunning(TouchCalmActivity.this, TouchCalmHelper.FLOAT_SERVICE_NAME)) {
				stopService(new Intent(TouchCalmActivity.this, FloatService.class));
			} else {
				Toast.makeText(TouchCalmActivity.this, R.string.toast_str_setuneffective, Toast.LENGTH_LONG).show();
			}

			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

}
