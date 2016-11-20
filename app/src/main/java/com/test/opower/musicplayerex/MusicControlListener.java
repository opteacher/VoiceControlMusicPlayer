package com.test.opower.musicplayerex;

import android.view.View;
import android.widget.ProgressBar;

/**
 * Created by opower on 2016/11/16.
 */

public class MusicControlListener implements View.OnClickListener
{
	private ProgressBar pgs = null;

	public MusicControlListener(ProgressBar pgsPlay)
	{
		pgs = pgsPlay;
	}

	@Override
	public void onClick(View v)
	{
		switch(v.getId())
		{
		case R.id.imgSoundAlbum:
		case R.id.lytSoundInfoDetail:

			//将播放进度条设置到播放器中
			MusicPlayer mp = MusicPlayer.ins(pgs);

			//从进度条中获取音乐url
			Object obj = pgs.getTag();
			if(obj == null)	{ break; }
			String mscUrl = (String) obj;

			//开始播放音乐
			mp.playMscUrl(mscUrl);
		}
	}
}
