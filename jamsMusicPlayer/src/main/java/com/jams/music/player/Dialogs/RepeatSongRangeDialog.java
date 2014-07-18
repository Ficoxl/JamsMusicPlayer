package com.jams.music.player.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jams.music.player.R;
import com.jams.music.player.DBHelpers.DBAccessHelper;
import com.jams.music.player.Helpers.TypefaceHelper;
import com.jams.music.player.Services.AudioPlaybackService;
import com.jams.music.player.Utils.Common;
import com.jams.music.player.Utils.RangeSeekBar;
import com.jams.music.player.Utils.RangeSeekBar.OnRangeSeekBarChangeListener;

public class RepeatSongRangeDialog extends DialogFragment {
	
	private Context mContext;
	private Common mApp;
	
	private int repeatPointA;
	private int repeatPointB;
	private int currentSongIndex;
	private int currentSongDuration;
	private SharedPreferences sharedPreferences;
	private BroadcastReceiver receiver;
	
	private TextView repeatSongATime;
	private TextView repeatSongBTime;
	private SeekBar seekBar;
	private RangeSeekBar<Integer> rangeSeekBar;
	private ViewGroup viewGroup;
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		mContext = getActivity().getApplicationContext();
		mApp = (Common) mContext;
		
        receiver = new BroadcastReceiver() {
        	
            @Override
            public void onReceive(Context context, Intent intent) {
            	initRepeatSongRangeDialog();
            	
            }
            
        };
		
		sharedPreferences = getActivity().getSharedPreferences("com.jams.music.player", Context.MODE_PRIVATE);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_repeat_song_range_dialog, null);
        currentSongIndex = mApp.getService().getCurrentSongIndex();

        repeatSongATime = (TextView) view.findViewById(R.id.repeat_song_range_A_time);
        repeatSongBTime = (TextView) view.findViewById(R.id.repeat_song_range_B_time);

        currentSongDuration = (int) (mApp.getService().getCurrentSong().getDuration());
        
        //Remove the placeholder seekBar and replace it with the RangeSeekBar.
        seekBar = (SeekBar) view.findViewById(R.id.repeat_song_range_placeholder_seekbar);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) seekBar.getLayoutParams();
        viewGroup = (ViewGroup) seekBar.getParent();
        viewGroup.removeView(seekBar);
        
        rangeSeekBar = new RangeSeekBar<Integer>(0, currentSongDuration, getActivity());
        rangeSeekBar.setLayoutParams(params);
        viewGroup.addView(rangeSeekBar);
		
        if (sharedPreferences.getInt("REPEAT_MODE", 0)==3) {
        	repeatSongATime.setText(convertMillisToMinsSecs(mApp.getService().getRepeatSongRangePointA()));
        	repeatSongBTime.setText(convertMillisToMinsSecs(mApp.getService().getRepeatSongRangePointB()));
        	rangeSeekBar.setSelectedMinValue(mApp.getService().getRepeatSongRangePointA());
        	rangeSeekBar.setSelectedMaxValue(mApp.getService().getRepeatSongRangePointB());
        	repeatPointA = mApp.getService().getRepeatSongRangePointA();
        	repeatPointB = mApp.getService().getRepeatSongRangePointB();
        } else {
        	repeatSongATime.setText("0:00");
        	repeatSongBTime.setText(convertMillisToMinsSecs(currentSongDuration));
        	repeatPointA = 0;
        	repeatPointB = currentSongDuration;
        }
        
        rangeSeekBar.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener<Integer>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
            	repeatPointA = minValue;
            	repeatPointB = maxValue;
            	repeatSongATime.setText(convertMillisToMinsSecs(minValue));
            	repeatSongBTime.setText(convertMillisToMinsSecs(maxValue));
            }
            
        });
        
        //Set the dialog title.
        builder.setTitle(R.string.a_b_repeat);
        builder.setView(view);
        builder.setNegativeButton(R.string.cancel, new OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				
			}

        });
        
        builder.setPositiveButton(R.string.repeat, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if ((currentSongDuration - repeatPointB) < 6) {
					//Remove the crossfade handler.
					mApp.getService().getHandler().removeCallbacks(mApp.getService().startCrossFadeRunnable);
					mApp.getService().getHandler().removeCallbacks(mApp.getService().crossFadeRunnable);
				}
				
				mApp.broadcastUpdateUICommand(new String[] { Common.UPDATE_PLAYBACK_CONTROLS }, 
											  new String[] { "" });
				mApp.getService().setRepeatSongRange(repeatPointA, repeatPointB);
				mApp.getService().setRepeatMode(AudioPlaybackService.A_B_REPEAT);
				
			}
        	
        });

        return builder.create();
    }
	
	public void initRepeatSongRangeDialog() {

		mApp.getService().getCursor().moveToPosition(currentSongIndex+1);
        currentSongDuration = mApp.getService().getCursor().getInt(mApp.getService().getCursor().getColumnIndex(DBAccessHelper.SONG_DURATION));
        
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) rangeSeekBar.getLayoutParams();
        viewGroup = (ViewGroup) rangeSeekBar.getParent();
        viewGroup.removeView(rangeSeekBar);
        
        rangeSeekBar = new RangeSeekBar<Integer>(0, currentSongDuration, getActivity());
        rangeSeekBar.setLayoutParams(params);
        viewGroup.addView(rangeSeekBar);
		
        if (sharedPreferences.getInt("REPEAT_MODE", 0)==3) {
        	repeatSongATime.setText(convertMillisToMinsSecs(mApp.getService().getRepeatSongRangePointA()));
        	repeatSongBTime.setText(convertMillisToMinsSecs(mApp.getService().getRepeatSongRangePointB()));
        	rangeSeekBar.setSelectedMinValue(mApp.getService().getRepeatSongRangePointA());
        	rangeSeekBar.setSelectedMaxValue(mApp.getService().getRepeatSongRangePointB());
        	repeatPointA = mApp.getService().getRepeatSongRangePointA();
        	repeatPointB = mApp.getService().getRepeatSongRangePointB();
        } else {
        	repeatSongATime.setText("0:00");
        	repeatSongBTime.setText(convertMillisToMinsSecs(currentSongDuration));
        	repeatPointA = 0;
        	repeatPointB = currentSongDuration;
        }
        
        rangeSeekBar.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener<Integer>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
            	repeatPointA = minValue;
            	repeatPointB = maxValue;
            	repeatSongATime.setText(convertMillisToMinsSecs(minValue));
            	repeatSongBTime.setText(convertMillisToMinsSecs(maxValue));
            }
            
        });
        
	}
	
	//Convert millisseconds to hh:mm:ss format.
    public static String convertMillisToMinsSecs(long milliseconds) {
    	
    	int secondsValue = (int) (milliseconds / 1000) % 60 ;
    	int minutesValue = (int) ((milliseconds / (1000*60)) % 60);
    	int hoursValue  = (int) ((milliseconds / (1000*60*60)) % 24);
    	
    	String seconds = "";
    	String minutes = "";
    	String hours = "";
    	
    	if (secondsValue < 10) {
    		seconds = "0" + secondsValue;
    	} else {
    		seconds = "" + secondsValue;
    	}

    	if (minutesValue < 10) {
    		minutes = "0" + minutesValue;
    	} else {
    		minutes = "" + minutesValue;
    	}
    	
    	if (hoursValue < 10) {
    		hours = "0" + hoursValue;
    	} else {
    		hours = "" + hoursValue;
    	}
    	
    	String output = "";
    	
    	if (hoursValue!=0) {
    		output = hours + ":" + minutes + ":" + seconds;
    	} else {
    		output = minutes + ":" + seconds;
    	}
    	
    	return output;
    }
    
	@Override
	public void onStart() {
	    super.onStart();
	    LocalBroadcastManager.getInstance(getActivity().getApplicationContext())
	    					 .registerReceiver((receiver), new IntentFilter("com.jams.music.player.UPDATE_NOW_PLAYING"));
	
	}

	@Override
	public void onStop() {
	    LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).unregisterReceiver(receiver);
	    super.onStop();
	    
	}
	
}
