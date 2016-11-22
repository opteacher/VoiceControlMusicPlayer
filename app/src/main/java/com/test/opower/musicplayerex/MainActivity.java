package com.test.opower.musicplayerex;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends Activity
{
	private ImageView imgVoiceView = null;
	private ImageView imgFormView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		//组件初始化
		VoiceParser vp = VoiceParser.ins(this);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//收集界面上的组件
		imgVoiceView = (ImageView) this.findViewById(R.id.imgVoiceControlView);
		imgFormView = (ImageView) this.findViewById(R.id.imgFormControlView);

		//设置按键事件
		imgVoiceView.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{

				//改变按钮的颜色
				imgVoiceView.setBackgroundResource(R.color.colorNavBackgroundActive);
				imgFormView.setBackgroundResource(R.color.colorNavBackground);

				//页面跳转
				MainActivity.this
						.getFragmentManager()
						.beginTransaction()
						.replace(
								R.id.lytMainContainer,
								VoiceContolFragment.ins())
						.commit();
			}
		});

		imgFormView.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{

				//改变按钮的颜色
				imgFormView.setBackgroundResource(R.color.colorNavBackgroundActive);
				imgVoiceView.setBackgroundResource(R.color.colorNavBackground);

				//页面跳转
				MainActivity.this
						.getFragmentManager()
						.beginTransaction()
						.replace(
								R.id.lytMainContainer,
								SearchControlFragment.ins())
						.commit();
			}
		});

		imgFormView.callOnClick();
	}
}

