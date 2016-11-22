package com.test.opower.musicplayerex;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.GrammarListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.SpeechRecognizer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.iflytek.cloud.VerifierResult.TAG;

/**
 * Created by opower on 2016/11/19.
 */

public class VoiceParser
{
	public final static String SEARCH = "搜索";
	public final static String MUSIC_LIST = "歌单";

	public enum ParseState
	{
		WF_RECORD,
		RECORDING,
		WF_SEARCH,
		SEARCH_TP,
		WF_SEARCH_MSC_LST,

	}
	private ParseState ps = ParseState.WF_RECORD;
	private SpeechRecognizer sr = null;
	private RecognizerListener rl = new RecognizerListener()
	{
		@Override
		public void onVolumeChanged(int i, byte[] bytes)
		{
			for(VoiceParserItf vpi : vpis)
			{
				vpi.onVolumnChange(i);
			}
		}

		@Override
		public void onBeginOfSpeech()
		{
			Log.d(TAG, "开始讲话");
		}

		@Override
		public void onEndOfSpeech()
		{
			Log.d(TAG, "结束讲话");
		}

		@Override
		public void onResult(RecognizerResult recognizerResult, boolean b)
		{
			String json = recognizerResult.getResultString();
			String result = "";
			try
			{
				JSONTokener jsonTknr = new JSONTokener(json);
				JSONObject jsonObj = (JSONObject) jsonTknr.nextValue();
				JSONArray jsonAry = jsonObj.getJSONArray("ws");
				for(int i = 0; i < jsonAry.length(); ++i)
				{
					JSONObject obj = jsonAry.getJSONObject(i);
					JSONArray ary = obj.getJSONArray("cw");
					for(int j = 0; j < ary.length(); ++j)
					{
						result += ary.getJSONObject(j).getString("w");
					}
				}
			}
			catch(JSONException e)
			{
				e.printStackTrace();
			}
			result = result.trim();
			if(!result.equals("。") && !result.equals("."))
			{
				VoiceParser.instance.parseResult(result);

				for(VoiceParserItf vpi : vpis)
				{
					vpi.onVoiceParsed(result);
				}
			}
		}

		@Override
		public void onError(SpeechError speechError)
		{
			Log.d(TAG, "识别错误，错误码：" + speechError.getErrorCode());
		}

		@Override
		public void onEvent(int i, int i1, int i2, Bundle bundle)
		{
			if(i == SpeechEvent.EVENT_RECORD_STOP)
			{
				if(ps != ParseState.WF_RECORD)
				{
					sr.startListening(this);
				}
			}
			System.out.println("事件：" + i);
		}
	};
	private String cldGrm = "";
	private String grmId = "";
	private GrammarListener gl = new GrammarListener()
	{
		@Override
		public void onBuildFinish(String s, SpeechError speechError)
		{
			if(speechError == null)
			{
				if(!TextUtils.isEmpty(s))
				{
					grmId = s;
				}
			}
			else
			{
				Log.d(TAG, "语法构建错误，错误码：" + speechError.getErrorCode());
			}
		}
	};

	private List<VoiceParserItf> vpis = new ArrayList<>();
	public void attachVoiceParserItf(VoiceParserItf vpi)
	{
		vpis.add(vpi);
	}
	public void detachVoiceParserItf(VoiceParserItf vpi)
	{
		vpis.remove(vpi);
	}

	private List<VoiceCmdParserItf> vcpis = new ArrayList<>();
	public void attachVoiceCmdParserItf(VoiceCmdParserItf vcpi)	{ vcpis.add(vcpi); }
	public void detachVoiceCmdParserItf(VoiceCmdParserItf vcpi)	{ vcpis.remove(vcpi); }

	private Activity activity = null;
	private Fragment fgt = null;

	private static VoiceParser instance = null;
	public static VoiceParser ins(Activity act)
	{
		if(act != null)
		{
			if(instance != null && act == instance.activity)
			{
				return instance;
			}
			instance = new VoiceParser(act);
		}
		return instance;
	}
	private VoiceParser(Activity act)
	{
		activity = act;

		//读取云语法文件
		InputStream is = act.getResources()
				.openRawResource(R.raw.cloud_grammar);
		try
		{
			byte[] buf = new byte[is.available()];
			is.read(buf);
			cldGrm = new String(buf, "GBK");
			is.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

		//创建SpeechRecognizer对象
		sr = SpeechRecognizer.createRecognizer(act, null);

		//构建ABNF语法文件
		sr.buildGrammar("abnf", cldGrm, gl);
		sr.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
		sr.setParameter(SpeechConstant.ENGINE_TYPE, "cloud");
		sr.setParameter(SpeechConstant.CLOUD_GRAMMAR, grmId);
		sr.setParameter(SpeechConstant.VAD_BOS, "4000");

		//设置听写参数
		//		sr.setParameter(SpeechConstant.DOMAIN, "iat");
		//		sr.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
		//		sr.setParameter(SpeechConstant.ACCENT, "mandarin");
	}

	public void startRecord()
	{
		int ret = sr.startListening(rl);
		if(ret != ErrorCode.SUCCESS)
		{
			Log.d(TAG, "识别失败，错误码：" + ret);
		}
		ps = ParseState.RECORDING;
		for(VoiceParserItf vpi : vpis)
		{
			vpi.onStartRecord();
		}
	}

	public void stopRecord()
	{
		sr.stopListening();
		ps = ParseState.WF_RECORD;
		for(VoiceParserItf vpi : vpis)
		{
			vpi.onStopRecord();
		}
	}

	public boolean isRecording()
	{
		return ps != ParseState.WF_RECORD;
	}

	public void switchRecord()
	{
		if(ps != ParseState.WF_RECORD)
		{
			stopRecord();
			ps = ParseState.WF_RECORD;
		}
		else
		{
			startRecord();
			ps = ParseState.RECORDING;
		}
	}

	public void parseResult(String result)
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

			//调用回调函数
			for(VoiceCmdParserItf vcpi : vcpis)
			{
				vcpi.onCallSearch();
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

			//调用回调函数
			for(VoiceCmdParserItf vcpi : vcpis)
			{
				vcpi.onCallMusicList();
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

				//调用回调函数
				for(VoiceCmdParserItf vcpi : vcpis)
				{
					vcpi.onCallNumber(result);
				}

				break;
			}
		}
	}
}
