package com.test.opower.musicplayerex;

import android.app.Activity;
import android.app.Fragment;
import android.view.View;
import android.widget.Toast;

/**
 * Created by opower on 2016/11/20.
 */

public class VoiceCmdParser implements VoiceParserItf
{
	public final static String SEARCH = "搜索";
	public final static String MUSIC_LIST = "歌单";

	private Activity activity = null;

	public enum ParseState
	{
		WF_RECORD,
		RECORDING,
		WF_SEARCH,
		SEARCH_TP,
		WF_SEARCH_MSC_LST,

	}
	private ParseState ps = ParseState.WF_RECORD;
	private Fragment fgt = null;
	private VoiceFloatBtn vfb = null;

	private static VoiceCmdParser instance = null;
	public static VoiceCmdParser ins(Activity act)
	{
		if(act != null)
		{
			instance = new VoiceCmdParser(act);
		}
		return instance;
	}
	private VoiceCmdParser(Activity act)
	{
		activity = act;
		vfb = (VoiceFloatBtn) act.findViewById(R.id.imgVoiceFltBtn);
	}

	@Override
	public void onVoiceParsed(String result)
	{
		//获得当前主容器中的界面
		fgt = activity.getFragmentManager().findFragmentById(R.id.lytMainContainer);
		SearchControlFragment scf = SearchControlFragment.ins();

		//显示Toast
		Toast.makeText(activity, result, Toast.LENGTH_LONG);

		if(result.equals(SEARCH))
		{
			//判断当前界面是否为搜索界面，不是的话做跳转
			if(!(fgt instanceof SearchControlFragment))
			{
				//界面跳转
				activity.getFragmentManager()
						.beginTransaction()
						.replace(R.id.lytMainContainer, SearchControlFragment.ins())
						.commit();

				scf.getEdtMscTbl().setText("");
			}

			//如果有语音浮动按钮，将其设置为搜索模式
			if(vfb != null && vfb.getVisibility() == View.VISIBLE)
			{
				vfb.setImageResource(R.drawable.record_fb_search);
			}

			//状态改变到等待搜索输入
			ps = ParseState.WF_SEARCH;
		}
		else
		if(result.equals(MUSIC_LIST))
		{
			//判断当前界面是否为搜索界面，不是的话做跳转
			if(fgt instanceof SearchControlFragment)
			{
				scf.getEdtMscTbl().setHint(R.string.input_music_table_id);
			}

			//如果有语音浮动按钮，将其设置为列表搜索模式
			if(vfb != null && vfb.getVisibility() == View.VISIBLE)
			{
				vfb.setImageResource(R.drawable.record_fb_music_list);
			}

			//状态改变到等待歌单输入
			ps = ParseState.WF_SEARCH_MSC_LST;
		}
		else
		{
			switch(ps)
			{
			case WF_SEARCH_MSC_LST:
				//判断当前界面是不是搜索界面
				if(fgt instanceof SearchControlFragment)
				{
					scf.getEdtMscTbl().setText(result);
					scf.getBtnConfirm().callOnClick();
				}

				break;
			}
		}
	}

	@Override
	public void onStartRecord()
	{
		ps = ParseState.RECORDING;
	}

	@Override
	public void onStopRecord()
	{
		ps = ParseState.WF_RECORD;
	}

	@Override
	public void onVolumnChange(int i)
	{
		if(vfb != null && vfb.getVisibility() == View.VISIBLE)
		{
			vfb.invalidate();
		}
	}

	public ParseState getParseState()
	{
		return ps;
	}
}
