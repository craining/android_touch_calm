package com.craining.blog.touchcalm;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class GetProtityActivity extends Activity {

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_LEFT_ICON); //申请设置个性化小图标
		// //以下是 设置小图标
		// Window win = getWindow();
		// Bundle extra = getIntent().getExtras();
		// int showOrNot =
		// extra.getInt(TouchCalmHelper.TAG_EXTRA_SHOW_TITLEICO);
		// if(showOrNot > 0) {
		// //show
		// win.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
		// R.drawable.ic_launcher);
		// } else {
		// //hide
		// }
		ComponentName mComponentname = new ComponentName(GetProtityActivity.this, LockScreenAdmin.class);
		Intent intent = new  Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
		intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mComponentname);
		intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getString(R.string.title_please_active));
		startActivityForResult(intent, 1);

        finish();
	}

}
