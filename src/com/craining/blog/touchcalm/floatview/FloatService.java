package com.craining.blog.touchcalm.floatview;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.craining.blog.touchcalm.R;
import com.craining.blog.touchcalm.TouchCalmHelper;
import com.craining.blog.touchcalm.service.TouchCalmService;

public class FloatService extends Service {

	private WindowManager wm_float;
	private WindowManager.LayoutParams floatParams;
	private View view_floating;
	private ImageView imgViewOk;
	private CheckBox check_Sound;
	private CheckBox check_Vibrate;
	private SeekBar seek_sensibility;
//	private ToggleButton tglbtn_showTitleIc;

	// private Animation animAlpha;

	@Override
	public void onCreate() {
		
		super.onCreate();
		// animAlpha = AnimationUtils.loadAnimation(FloatService.this,
		// R.anim.my_alpha_action);
		view_floating = LayoutInflater.from(this).inflate(R.layout.layout_float, null);
		// View bg = (View) view_floating.findViewById(R.id.float_bg);
		// bg.startAnimation(animAlpha);
		imgViewOk = (ImageView) view_floating.findViewById(R.id.img_setsvok);
		check_Sound = (CheckBox) view_floating.findViewById(R.id.check_sound_on);
		check_Vibrate = (CheckBox) view_floating.findViewById(R.id.check_vibrate_on);
		seek_sensibility = (SeekBar) view_floating.findViewById(R.id.seek_sensibility);
//		tglbtn_showTitleIc = (ToggleButton) view_floating.findViewById(R.id.check_title_ic);

		initView();// ��ʾ֮ǰ����
		imgViewOk.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				// ���SV����
				if (check_Sound.isChecked()) {
					if (!TouchCalmHelper.DIR_SOUND_ON.exists()) {
						TouchCalmHelper.DIR_SOUND_ON.mkdir();
					}
				} else {
					if (TouchCalmHelper.DIR_SOUND_ON.exists()) {
						TouchCalmHelper.DIR_SOUND_ON.delete();
					}
				}
				if (check_Vibrate.isChecked()) {
					if (TouchCalmHelper.DIR_VIBRATE_OFF.exists()) {
						TouchCalmHelper.DIR_VIBRATE_OFF.delete();
					}
				} else {
					if (!TouchCalmHelper.DIR_VIBRATE_OFF.exists()) {
						TouchCalmHelper.DIR_VIBRATE_OFF.mkdir();
					}

				}
				// �������������
				int getSetValue = seek_sensibility.getProgress();
				int toSaveINTValue = getSetValue - TouchCalmHelper.SENSOR_SENSIBILITY_TEMP;
				TouchCalmHelper.androidFileSave(FloatService.this, TouchCalmHelper.FILE_NAME_KEEP_SENSIBILITY, "" + toSaveINTValue);
				// ��������ʹ֮��Ч
				if (TouchCalmHelper.DIR_TAG_OPEN.exists()) {
					startService(new Intent(FloatService.this, TouchCalmService.class));
				}
				stopSelf();
			}
		});

		createView();
	}

	/**
	 * 
	 */
	private void initView() {
		seek_sensibility.setMax(TouchCalmHelper.SENSOR_SENSIBILITY_MAX);
		// ����������Ѿ����ã����ȡ����ʾ��seek bar�ϣ���������ΪĬ��
		if (TouchCalmHelper.FILE_KEEP_SENSIBILITY.exists()) {
			String getSensibility = TouchCalmHelper.androidFileload(FloatService.this, TouchCalmHelper.FILE_NAME_KEEP_SENSIBILITY);
			if (getSensibility != null) {
				seek_sensibility.setProgress(Integer.parseInt(getSensibility) + TouchCalmHelper.SENSOR_SENSIBILITY_TEMP);
			}
		} else {
			seek_sensibility.setProgress(TouchCalmHelper.SENSOR_SENSIBILITY_DEFAULT);
			int toSaveSensibilityValue = TouchCalmHelper.SENSOR_SENSIBILITY_DEFAULT - TouchCalmHelper.SENSOR_SENSIBILITY_TEMP;
			TouchCalmHelper.androidFileSave(FloatService.this, TouchCalmHelper.FILE_NAME_KEEP_SENSIBILITY, Integer.toString(toSaveSensibilityValue));
		}

		if (TouchCalmHelper.DIR_SOUND_ON.exists()) {
			check_Sound.setChecked(true);
		}
		if (!TouchCalmHelper.DIR_VIBRATE_OFF.exists()) {
			check_Vibrate.setChecked(true);
		}
//		if(!TouchCalmHelper.DIR_TITLEBR_SHOW.exists()) {
//			tglbtn_showTitleIc.setChecked(true);
//		} else {
//			tglbtn_showTitleIc.setChecked(false);
//		}
	}

	private void createView() {
		// ��ȡWindowManager
		wm_float = (WindowManager) getApplicationContext().getSystemService(WINDOW_SERVICE);
		// ����LayoutParams(ȫ�ֱ�������ز���
		floatParams = ((FloatApp) getApplication()).getMywmParams();
		floatParams.type = WindowManager.LayoutParams.TYPE_PHONE;// �������ṩ���û���������������Ӧ�ó����Ϸ���������״̬������
		floatParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;// �������κΰ����¼�
		floatParams.gravity = Gravity.LEFT | Gravity.TOP; // �����������������Ͻ�
		// ����Ļ���Ͻ�Ϊԭ�㣬����x��y��ʼֵ
		floatParams.x = 50;
		floatParams.y = 115;
		// �����������ڳ�������
		floatParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		floatParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		floatParams.format = PixelFormat.RGBA_8888;

		wm_float.addView(view_floating, floatParams);

	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		Log.e("", "Destroy");
		wm_float.removeView(view_floating);
		super.onDestroy();

	}

}
