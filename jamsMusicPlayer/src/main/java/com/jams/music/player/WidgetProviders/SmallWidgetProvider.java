package com.jams.music.player.WidgetProviders;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

import com.jams.music.player.AsyncTasks.AsyncUpdateSmallWidgetTask;

public class SmallWidgetProvider extends AppWidgetProvider {
	
	private Context mContext;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);

	}
	
	@Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		mContext = context;
        final int N = appWidgetIds.length;

        AsyncUpdateSmallWidgetTask task = new AsyncUpdateSmallWidgetTask(mContext, N, appWidgetIds, appWidgetManager);
        task.execute();
 
    }
    
}
