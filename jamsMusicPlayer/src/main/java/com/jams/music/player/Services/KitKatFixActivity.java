package com.jams.music.player.Services;

import android.app.Activity;
import android.os.Bundle;

/* KitKat introduced a new bug: swiping away the app from the 
 * "Recent Apps" list causes all background services to shut 
 * down. To circumvent this issue, this dummy activity will 
 * launch momentarily and close (it will be invisible to the 
 * user). This will fool the OS into thinking that the service 
 * still has an Activity bound to it, and prevent it from being 
 * killed.
 * 
 * ISSUE #53313:
 * https://code.google.com/p/android/issues/detail?id=53313
 * 
 * ISSUE #63618:
 * https://code.google.com/p/android/issues/detail?id=63618
 * 
 * General discussion thread:
 * https://groups.google.com/forum/#!topic/android-developers/LtmA9xbrD5A
 */
public class KitKatFixActivity extends Activity {
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		finish();
	}
	
}
