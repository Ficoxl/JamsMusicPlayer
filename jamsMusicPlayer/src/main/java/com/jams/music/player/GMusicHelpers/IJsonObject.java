package com.jams.music.player.GMusicHelpers;

import org.json.JSONObject;

public interface IJsonObject<T> {
	T fromJsonObject(JSONObject jsonObject);
}
