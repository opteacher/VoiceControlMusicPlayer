package com.test.opower.musicplayerex;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

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
	private boolean isRecording = false;
	private int volumn = 0;
	private SpeechRecognizer sr = null;
	private RecognizerListener rl = new RecognizerListener()
	{
		@Override
		public void onVolumeChanged(int i, byte[] bytes)
		{
			volumn = i;

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
			if(!result.equals("。") && !result.equals("."))
			{
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
				if(isRecording)
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

	private Context context = null;

	private static VoiceParser instance = null;
	public static VoiceParser ins(Context ctt)
	{
		if(ctt != null)
		{
			if(instance != null && ctt == instance.context)
			{
				return instance;
			}
			instance = new VoiceParser(ctt);
		}
		return instance;
	}
	private VoiceParser(Context ctt)
	{
		context = ctt;

		//读取云语法文件
		InputStream is = ctt.getResources()
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
		sr = SpeechRecognizer.createRecognizer(ctt, null);

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
		isRecording = true;
		for(VoiceParserItf vpi : vpis)
		{
			vpi.onStartRecord();
		}
	}

	public void stopRecord()
	{
		sr.stopListening();
		isRecording = false;
		for(VoiceParserItf vpi : vpis)
		{
			vpi.onStopRecord();
		}
	}

	public boolean isRecording()
	{
		return isRecording;
	}

	public void switchRecord()
	{
		isRecording = !isRecording;
		if(isRecording)	{ startRecord(); }
		else			{ stopRecord(); }
	}

	public int getVolumn()
	{
		return volumn;
	}
}
