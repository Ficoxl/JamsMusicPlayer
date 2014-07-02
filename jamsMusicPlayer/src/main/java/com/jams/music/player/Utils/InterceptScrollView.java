package com.jams.music.player.Utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

/******************************************************
 * Custom ScrollView implementation that disables touch 
 * event consumption. All touch events/gestures are 
 * passed on to the views underneath the ScrollView in 
 * the parent View hierarchy.
 * 
 * @author Saravan Pantham
 ******************************************************/
public class InterceptScrollView extends ScrollView {

	public InterceptScrollView(Context context, AttributeSet attrs) {
	    super(context, attrs);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
	    return true;
	}

}
