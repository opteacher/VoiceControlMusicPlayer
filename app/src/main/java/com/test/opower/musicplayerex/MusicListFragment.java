package com.test.opower.musicplayerex;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class MusicListFragment extends Fragment
{
	private ListView lstMusic = null;
	private MusicListAdapter sla = null;

	private static MusicListFragment instance = new MusicListFragment();
	public static MusicListFragment ins()	{ return instance; }

	public MusicListFragment()
	{
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View root = inflater.inflate(R.layout.fragment_music_list, container, false);

		//获取界面上的组件
		lstMusic = (ListView) root.findViewById(R.id.lstMusic);

		//初始化成员变量
		sla = new MusicListAdapter(root.getContext());

		//从传递的参数中获取歌单json
		bdl = getArguments();
		String json = bdl.getString(null);

		//从JSON中解析出所需的数据
		List<Map<String, Object>> data = new ArrayList<>();
		try
		{
			JSONTokener jsonTknr = new JSONTokener(json);
			JSONObject jsonRoot = (JSONObject) jsonTknr.nextValue();
			JSONObject jsonResult = jsonRoot.getJSONObject("result");
			JSONArray jsonTracks = jsonResult.getJSONArray("tracks");
			for(int i = 0; i < jsonTracks.length(); ++i)
			{
				Map<String, Object> itm = new HashMap<>();
				JSONObject mscIfo = jsonTracks.getJSONObject(i);
				itm.put(MusicListAdapter.SONG_IMAGE,
						mscIfo.getJSONObject("album").getString("picUrl"));
				itm.put(MusicListAdapter.SONG_NAME, mscIfo.getString("name"));
				JSONArray jsonArtists = mscIfo.getJSONArray("artists");
				String artists = "";
				for(int j = 0; j < jsonArtists.length() - 1; ++j)
				{
					artists += jsonArtists.getJSONObject(j).getString("name") + ", ";
				}
				if(jsonArtists.length() > 0)
				{
					artists += jsonArtists.getJSONObject(jsonArtists.length() - 1)
							.getString("name");
				}
				itm.put(MusicListAdapter.SONG_ARTISTS, artists);
				itm.put(MusicListAdapter.SONG_MPS_URL, mscIfo.getString("mp3Url"));
				itm.put(MusicListAdapter.SONG_DURATION, mscIfo.getInt("duration"));

				data.add(itm);
			}
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}

		//将搜集到的数据和当前上下文创建列表适配器
		sla.setData(data);
		lstMusic.setAdapter(sla);

		return root;
	}

	private Bundle bdl = new Bundle();
	public MusicListFragment setArguments(String json)
	{
		bdl.putString(null, json);
		this.setArguments(bdl);
		return this;
	}
}
