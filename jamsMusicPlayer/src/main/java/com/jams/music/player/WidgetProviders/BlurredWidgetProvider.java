package com.jams.music.player.WidgetProviders;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

import com.jams.music.player.AsyncTasks.AsyncUpdateBlurredWidgetTask;

public class BlurredWidgetProvider extends AppWidgetProvider {
	
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

        AsyncUpdateBlurredWidgetTask task = new AsyncUpdateBlurredWidgetTask(mContext, N, appWidgetIds, appWidgetManager);
        task.execute();
 
    }
    
}
