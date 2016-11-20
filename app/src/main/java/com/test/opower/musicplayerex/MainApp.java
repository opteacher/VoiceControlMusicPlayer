package com.test.opower.musicplayerex;

import android.app.Application;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

/**
 * Created by opower on 2016/11/17.
 */

public class MainApp extends Application
{
	private final static String APP_ID = "5825f536";

	private SpeechUtility su = null;

	@Override
	public void onCreate()
	{
		//初始化语音配置对象
		su = SpeechUtility.createUtility(this, SpeechConstant.APPID + "=" + APP_ID);

		super.onCreate();
	}
}
