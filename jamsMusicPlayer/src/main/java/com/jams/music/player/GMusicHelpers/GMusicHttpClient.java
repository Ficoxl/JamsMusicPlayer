package com.jams.music.player.GMusicHelpers;

import org.apache.http.HttpEntity;

import android.content.Context;

import com.loopj.android.http.SyncHttpClient;

public class GMusicHttpClient extends SyncHttpClient
{

	public GMusicHttpClient()
	{
		super();
	}

	public String post(Context context, String url, HttpEntity entity, String contentType)
	{
		post(context, url, entity, contentType, responseHandler);
		return result;
	}

	@Override
	public String onRequestFailed(Throwable error, String content)
	{
		return null;
	}
}
