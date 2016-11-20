package com.test.opower.musicplayerex;

/**
 * Created by opower on 2016/11/19.
 */

public interface VoiceParserItf
{
	void onVoiceParsed(String result);
	void onStartRecord();
	void onStopRecord();
	void onVolumnChange(int i);
}
