package com.test.opower.musicplayerex;


import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class VoiceContolFragment extends Fragment implements View.OnClickListener, VoiceParserItf
{
	private ImageView imgRecord = null;
	private TextView txtVoice = null;


	private static VoiceContolFragment instance = new VoiceContolFragment();
	public static VoiceContolFragment ins()	{ return instance; }

	public VoiceContolFragment()
	{
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		//导入根视图
		View root = inflater.inflate(R.layout.fragment_voice_contol, container, false);

		//获取界面上的组件
		imgRecord = (ImageView) root.findViewById(R.id.imgRecord);
		txtVoice = (TextView) root.findViewById(R.id.txtVoice);

		//配置组件
		imgRecord.setOnClickListener(this);

		//设置语音识别器的上下文为当前Fragment的上下文
		VoiceParser.ins((Activity) root.getContext());

		//设置语音识别器的接口为当前Fragment
		VoiceParser.ins(null).attachVoiceParserItf(this);

		return root;
	}

	@Override
	public void onClick(View v)
	{
		VoiceParser vp = VoiceParser.ins(null);
		if(!vp.isRecording())
		{
			vp.startRecord();

		}
		else
		{
			vp.stopRecord();
		}
	}

	@Override
	public void onVoiceParsed(String result)
	{
		txtVoice.setText(result);
	}

	@Override
	public void onVolumnChange(int i)
	{
		float sclRate = i/30.0f;
		imgRecord.setScaleX(sclRate);
		imgRecord.setScaleY(sclRate);
	}

	@Override
	public void onStartRecord()
	{
		imgRecord.setImageResource(R.drawable.record_active);
	}

	@Override
	public void onStopRecord()
	{
		imgRecord.setImageResource(R.drawable.record);
		imgRecord.setScaleX(1);
		imgRecord.setScaleX(1);
	}
}
