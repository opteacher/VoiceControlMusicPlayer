package com.test.opower.musicplayerex;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by opower on 2016/11/20.
 */

public class VoiceFloatBtn extends ImageButton
{
	private Map<Integer, Integer> mpBtnState = null;
	private Rect rctImg = new Rect();
	private Path spcVol = new Path();

	public VoiceFloatBtn(Context context)
	{
		this(context, null, 0);
	}

	public VoiceFloatBtn(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	public VoiceFloatBtn(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);

		//初始化属性
		mpBtnState = new HashMap<>();
		mpBtnState.put(R.drawable.record_fb_stop, R.drawable.record_fb_stop_down);
		mpBtnState.put(R.drawable.record_fb_active, R.drawable.record_fb_active_down);
		mpBtnState.put(R.drawable.record_fb_search, R.drawable.record_fb_search_down);

		setOnTouchListener(new OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				ImageButton ib = (ImageButton) v;
				switch(event.getAction())
				{
				case MotionEvent.ACTION_DOWN:
					//反转录音状态
					VoiceParser.ins(null).switchRecord();

					ib.setImageResource(
							VoiceParser.ins(null).isRecording() ?
							R.drawable.record_fb_active_down :
							R.drawable.record_fb_stop_down);
					break;
				case MotionEvent.ACTION_UP:
					ib.setImageResource(
							VoiceParser.ins(null).isRecording() ?
							R.drawable.record_fb_active :
							R.drawable.record_fb_stop);
					break;
				}
				return true;
			}
		});
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);

		if(VoiceParser.ins(null).isRecording())
		{
			//到处图片资源，并设置尺寸
			Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.record_fb_speech);
			rctImg.set(0, 0, bmp.getWidth(), bmp.getHeight());

			//生成界面裁剪路径
			Rect cvsRct = canvas.getClipBounds();
			spcVol.reset();
			float radius = cvsRct.width() * (VoiceParser.ins(null).getVolumn() / 30.0f);
			spcVol.addCircle(cvsRct.centerX(), cvsRct.centerY(), radius, Path.Direction.CW);

			//裁剪并绘制录音图片
			canvas.clipPath(spcVol);
			canvas.drawBitmap(bmp, rctImg, cvsRct, null);
		}
	}
}
