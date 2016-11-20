package com.test.opower.musicplayerex;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ProgressBar;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by opower on 2016/11/16.
 */

public class MusicPlayer implements
		MediaPlayer.OnBufferingUpdateListener,
		MediaPlayer.OnCompletionListener,
		MediaPlayer.OnPreparedListener
{
	private MediaPlayer mp = null;
	private String curMscUrl = "";
	private Timer tm = new Timer();//计时器
	private ProgressBar pgsMusic = null;

	private TimerTask tt = new TimerTask()
	{
		@Override
		public void run()
		{
			if(mp == null)
			{
				return;
			}
			if(mp.isPlaying())
			{
				hdl.sendEmptyMessage(0);
			}
		}
	};

	private Handler hdl = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			float pos = mp.getCurrentPosition();
			float dur = mp.getDuration();
			if(dur > 0)
			{
				pgsMusic.setProgress((int)(pgsMusic.getMax() * (pos/dur)));
			}
		}
	};

	private MusicPlayer()
	{
		mp = new MediaPlayer();
		mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mp.setOnBufferingUpdateListener(this);
		mp.setOnPreparedListener(this);
		tm.schedule(tt, 0, 1000);
	}
	private static MusicPlayer instance = new MusicPlayer();
	public static MusicPlayer ins(ProgressBar pgsMusic)
	{
		if(instance.pgsMusic != pgsMusic)
		{
			instance.stop();
			instance.pgsMusic = pgsMusic;
		}
		return instance;
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent)
	{
		pgsMusic.setSecondaryProgress(percent);
		//int curPgs = pgsMusic.getMax() * (mp.getCurrentPosition() / mp.getDuration());
	}

	@Override
	public void onCompletion(MediaPlayer mp)
	{
		this.hidMscPgs();
	}

	@Override
	public void onPrepared(MediaPlayer mp)
	{
		mp.start();
	}

	public void playMscUrl(String url)
	{
		if(curMscUrl.equals(url))
		{
			this.pause();
		}
		else
		{
			try
			{
				pgsMusic.setVisibility(View.VISIBLE);
				mp.reset();
				mp.setDataSource(url);
				mp.prepare();
				curMscUrl = url;
			}
			catch(IOException e)
			{
				e.printStackTrace();
				this.hidMscPgs();
			}
		}
	}

	public void pause()
	{
		if(mp.isPlaying())
		{
			mp.pause();
		}
		else
		{
			mp.start();
		}
	}

	public void stop()
	{
		if(mp.isPlaying())
		{
			mp.stop();
//			mp.release();
//			mp = null;

			this.hidMscPgs();
		}
	}

	private void hidMscPgs()
	{
		if(pgsMusic != null)
		{
			pgsMusic.setVisibility(View.GONE);
			pgsMusic = null;
		}
	}
}
