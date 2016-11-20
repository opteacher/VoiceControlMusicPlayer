package com.test.opower.musicplayerex;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Created by opower on 2016/11/16.
 */

public class MusicListAdapter extends BaseAdapter
{
	public static final String SONG_NAME = "nam";
	public static final String SONG_IMAGE = "img";
	public static final String SONG_ARTISTS = "ats";
	public static final String SONG_MPS_URL = "url";
	public static final String SONG_DURATION = "dur";

	private List<Map<String, Object>> data = null;
	private LayoutInflater li = null;

	public MusicListAdapter(Context context)
	{
		this.li = LayoutInflater.from(context);
	}

	public MusicListAdapter setData(List<Map<String, Object>> data)
	{
		this.data = data;
		return this;
	}

	@Override
	public int getCount()
	{
		return data.size();
	}

	@Override
	public Object getItem(int position)
	{
		return data.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	private class ViewHolder
	{
		public ImageView imgAlbum = null;
		public LinearLayout lytSoundInfo = null;
		public TextView txtName = null;
		public TextView txtArtists = null;
		public ProgressBar pgsMusic = null;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		//判断视图是否存在，不存在就创建并将其添加到视图Tag中
		ViewHolder vh = null;
		if(convertView == null)
		{
			convertView = li.inflate(R.layout.item_music_list, null);
			vh = new ViewHolder();
			vh.imgAlbum = (ImageView) convertView.findViewById(R.id.imgSoundAlbum);
			vh.lytSoundInfo = (LinearLayout) convertView.findViewById(R.id.lytSoundInfoDetail);
			vh.txtName = (TextView) convertView.findViewById(R.id.txtSongName);
			vh.txtArtists = (TextView) convertView.findViewById(R.id.txtSongArtist);
			vh.pgsMusic = (ProgressBar) convertView.findViewById(R.id.pgsMusic);
			convertView.setTag(vh);
		}
		else
		{
			vh = (ViewHolder) convertView.getTag();
		}

		//设置列表项中的专辑图片
		Map<String, Object> datTmp = data.get(position);
		final String imgUrl = (String) datTmp.get(SONG_IMAGE);
		if(imgUrl.isEmpty())
		{
			vh.imgAlbum.setImageResource(R.drawable.disc);
		}
		else
		{
			final ViewHolder vhFnl = vh;
			final Message msg = new Message();
			final Bundle bdl = new Bundle();
			final Handler hdl = new Handler()
			{
				@Override
				public void handleMessage(Message msg)
				{
					Bundle bdl = msg.getData();
					Bitmap bmp = bdl.getParcelable(null);
					if(bmp != null)
					{
						vhFnl.imgAlbum.setImageBitmap(bmp);
					}
					else
					{
						vhFnl.imgAlbum.setImageResource(R.drawable.disc);
					}
				}
			};
			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						URL url = new URL(imgUrl);
						HttpURLConnection con = (HttpURLConnection) url.openConnection();
						con.setRequestMethod("GET");
						con.setReadTimeout(5000);
						con.setDoInput(true);
						con.connect();
						InputStream is = con.getInputStream();
						Bitmap bmp = BitmapFactory.decodeStream(is);
						bdl.putParcelable(null, bmp);
						msg.setData(bdl);
						hdl.sendMessage(msg);
						is.close();
					}
					catch(MalformedURLException e)
					{
						e.printStackTrace();
					}
					catch(IOException e)
					{
						e.printStackTrace();
					}
				}
			}).start();
		}
		vh.txtName.setText((String) datTmp.get(SONG_NAME));
		vh.txtArtists.setText((String) datTmp.get(SONG_ARTISTS));
		vh.pgsMusic.setTag(datTmp.get(SONG_MPS_URL));
		vh.pgsMusic.setMax((Integer) datTmp.get(SONG_DURATION));

		//添加组件事件
		vh.imgAlbum.setOnClickListener(new MusicControlListener(vh.pgsMusic));
		vh.lytSoundInfo.setOnClickListener(new MusicControlListener(vh.pgsMusic));
		return convertView;
	}
}
