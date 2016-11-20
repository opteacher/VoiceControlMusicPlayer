package com.test.opower.musicplayerex;


import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * A simple {@link Fragment} subclass.
 */
public class SearchControlFragment extends Fragment implements View.OnClickListener
{
	private final static String TAG_FOR_NET = "MusicPlayer[Net]";
	private final static String MUSIC_TABLE_URL = "http://music.163.com/api/playlist/detail?id=$id#&updateTime=-1";
	private final static String MUSIC_TABLE_ID = "id";

	private EditText edtMscTbl = null;
	private Button btnConfirm = null;


	private static SearchControlFragment instance = new SearchControlFragment();
	public static SearchControlFragment ins()	{ return instance; }

	public SearchControlFragment()
	{
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View root = inflater.inflate(R.layout.fragment_search_control, container, false);

		//获取界面上的组件
		edtMscTbl = (EditText) root.findViewById(R.id.edtMusicTable);
		btnConfirm = (Button) root.findViewById(R.id.btnConfirm);


		//配置组件
		btnConfirm.setOnClickListener(this);

		return root;
	}

	public EditText getEdtMscTbl()
	{
		return edtMscTbl;
	}

	public Button getBtnConfirm()
	{
		return btnConfirm;
	}

	private Bundle bdl = new Bundle();
	private Message msg = new Message();
	private Handler hdl = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{

			//如果没有获取到JSON，中断线程
			String json = msg.getData().getString(null);
			if(json.isEmpty())
			{
				return;
			}

			//画面迁移
			SearchControlFragment.this.getFragmentManager()
					.beginTransaction()
					.replace(R.id.lytMainContainer,
							 MusicListFragment.ins().setArguments(json))
					.commit();
		}
	};

	@Override
	public void onClick(View v)
	{
		if(v.getId() == R.id.btnConfirm)
		{
			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					String urlTxt = MUSIC_TABLE_URL.replace(
							"$" + MUSIC_TABLE_ID + "#", edtMscTbl.getText());

					HttpURLConnection conn = null;
					try
					{
						URL url = new URL(urlTxt);

						conn = (HttpURLConnection) url.openConnection();

						conn.setConnectTimeout(6000);
						conn.setRequestProperty("Cookie", "appver=3.7.3.153912");
						conn.setRequestProperty("Referer", "http://music.163.com");

						int rc = conn.getResponseCode();
						if(rc != 200)
						{
							throw new RuntimeException("请求url失败，回应码：" + rc);
						}

						InputStream is = conn.getInputStream();

						BufferedReader br = new BufferedReader(new InputStreamReader(is));

						StringBuffer sb = new StringBuffer();
						for(String ln = br.readLine(); ln != null; ln = br.readLine())
						{
							sb.append(ln);
						}

						bdl.putString(null, sb.toString());
						msg.setData(bdl);
						hdl.sendMessage(msg);

						//txtGetJson.setText(sb.toString());
					}
					catch(MalformedURLException e)
					{
						Log.d(TAG_FOR_NET, "url的格式发生异常，请输入正确的歌单号");
					}
					catch(IOException e)
					{
						Log.d(TAG_FOR_NET, "获取URL时发生异常，请检查网络连接");
					}
					catch(RuntimeException e)
					{
						e.printStackTrace();
					}
					finally
					{
						if(conn != null)
						{
							conn.disconnect();
						}
					}
				}
			}).start();
		}
	}
}
