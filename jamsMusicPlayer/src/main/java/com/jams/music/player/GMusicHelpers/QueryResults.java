package com.jams.music.player.GMusicHelpers;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//not used in the Android app
public class QueryResults implements IJsonObject<QueryResults>, IJsonArray<WebClientSongsSchema>
{

	private ArrayList<WebClientSongsSchema> mArtists;
	private ArrayList<WebClientSongsSchema> mAlbums;
	private ArrayList<WebClientSongsSchema> mWebClientSongsSchemas;

	public ArrayList<WebClientSongsSchema> getArtists()
	{
		return mArtists;
	}

	public void setArtists(ArrayList<WebClientSongsSchema> artists)
	{
		mArtists = artists;
	}

	public ArrayList<WebClientSongsSchema> getAlbums()
	{
		return mAlbums;
	}

	public void setAlbums(ArrayList<WebClientSongsSchema> albums)
	{
		mAlbums = albums;
	}

	public ArrayList<WebClientSongsSchema> getWebClientSongsSchemas()
	{
		return mWebClientSongsSchemas;
	}

	public void setWebClientSongsSchemas(ArrayList<WebClientSongsSchema> songs)
	{
		mWebClientSongsSchemas = songs;
	}

	@Override
	public QueryResults fromJsonObject(JSONObject jsonObject)
	{
		if(jsonObject != null)
		{
			JSONArray jsonArray = jsonObject.optJSONArray("artists");
			mArtists = (ArrayList<WebClientSongsSchema>) fromJsonArray(jsonArray);

			jsonArray = jsonObject.optJSONArray("albums");
			mAlbums = (ArrayList<WebClientSongsSchema>) fromJsonArray(jsonArray);

			jsonArray = jsonObject.optJSONArray("songs");
			mWebClientSongsSchemas = (ArrayList<WebClientSongsSchema>) fromJsonArray(jsonArray);
		}

		// return this object to allow chaining
		return this;
	}

	@Override
	public ArrayList<WebClientSongsSchema> fromJsonArray(JSONArray jsonArray)
	{
		ArrayList<WebClientSongsSchema> songList = new ArrayList<WebClientSongsSchema>();
		if(jsonArray != null && jsonArray.length() > 0)
		{
			for(int i = 0; i < jsonArray.length(); i++)
			{
				try
				{
					WebClientSongsSchema song = new WebClientSongsSchema().fromJsonObject(jsonArray.getJSONObject(i));
					songList.add(song);
				}
				catch(JSONException e)
				{
					e.printStackTrace();
				}
			}
		}

		return songList;
	}
}
