package com.jams.music.player.GMusicHelpers;

import java.util.ArrayList;

import org.json.JSONArray;

public interface IJsonArray<T> {
	ArrayList<T> fromJsonArray(JSONArray jsonArray);
}
