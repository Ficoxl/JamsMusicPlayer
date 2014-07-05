package com.jams.music.player.Helpers;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import com.jams.music.player.R;
import com.jams.music.player.Utils.Common;

/**
 * This class contains the static methods that return the appropriate UI 
 * elements/colors based on the selected theme (light or dark).
 *
 * @author Saravan Pantham
 */
public class UIElementsHelper {
	
	private static Common mApp;
	
	private static final String SELECTED_THEME = "SELECTED_THEME";
	private static final String DARK_THEME = "DARK_THEME";
	private static final String LIGHT_THEME = "LIGHT_THEME";
	private static final String DARK_CARDS_THEME = "DARK_CARDS_THEME";
	private static final String LIGHT_CARDS_THEME = "LIGHT_CARDS_THEME";
	
	private static final String NOW_PLAYING_COLOR = "NOW_PLAYING_COLOR";
	private static final String BLUE = "BLUE";
	private static final String RED = "RED";
	private static final String GREEN = "GREEN";
	private static final String ORANGE = "ORANGE";
	private static final String PURPLE = "PURPLE";
	private static final String MAGENTA = "MAGENTA";
	private static final String GRAY = "GRAY";
	private static final String WHITE = "WHITE";
	private static final String BLACK = "BLACK";
	
	/**
	 * Text color.
	 */
	public static int getTextColor(Context context) {
		
		mApp = (Common) context.getApplicationContext();
		int color;
		
		//The gray theme needs its own colors regardless of the application theme.
		if (mApp.getSharedPreferences().getString(NOW_PLAYING_COLOR, "BLUE").equals("GRAY")) {
			
			color = Color.parseColor("#FFFFFF");
			
		} else if (mApp.getSharedPreferences().getString(NOW_PLAYING_COLOR, "BLUE").equals("WHITE")) {
			
			color = Color.parseColor("#0F0F0F");
			
		} else {
			
			if (mApp.getSharedPreferences().getString(SELECTED_THEME, LIGHT_CARDS_THEME).equals(DARK_THEME) || 
				mApp.getSharedPreferences().getString(SELECTED_THEME, LIGHT_CARDS_THEME).equals(DARK_CARDS_THEME)) {
				color = Color.parseColor("#FFFFFF");
			} else {
				color = Color.parseColor("#5F5F5F");
			}
			
		}
		
		return color;
		
	}
	
	/**
	 *  Text color. Plain and simple.
	 */
	public static int getThemeBasedTextColor(Context context) {
		mApp = (Common) context.getApplicationContext();
		int color;
		
		if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME") ||
			mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_THEME")) {
			color = Color.parseColor("#FFFFFF");
		} else {
			color = Color.parseColor("#2F2F2F");
		}
		
		return color;
		
	}
	
	/**
	 * Small text color.
	 */
	public static int getSmallTextColor(Context context) {
		
		mApp = (Common) context.getApplicationContext();
		int color;
			
		if (mApp.getSharedPreferences().getString(SELECTED_THEME, LIGHT_CARDS_THEME).equals(DARK_THEME) || 
			mApp.getSharedPreferences().getString(SELECTED_THEME, LIGHT_CARDS_THEME).equals(DARK_CARDS_THEME)) {
			color = Color.parseColor("#999999");
		} else {
			color = Color.parseColor("#5F5F5F");
		}

		return color;
		
	}
	
	/**
	 * Return a resource icon based on the current theme.
	 * If the theme is LIGHT_THEME, return an icon resource with the same file name.
	 * If the theme is DARK_THEME, return an icon resource with "_light" appended
	 * to the file name. Note that the actual theme that is applied and the suffix
	 * of the file name are flipped: DARK_THEME uses "xxx_light.png" while LIGHT_THEME
	 * uses "xxx.png".
	 */
	public static int getIcon(Context context, String iconName) {
		
		int resourceID = 0;
		mApp = (Common) context.getApplicationContext();
		
		if (!iconName.equals("")) {
				
            //We're using "cloud" and "pin" in the settings page so we don't want them to be affected by the player color.
            if (iconName.equals("cloud_settings") || iconName.equals("pin_settings") || iconName.equals("equalizer_settings")) {
                if (iconName.equals("cloud_settings")) {
                    iconName = "cloud";
                } else if (iconName.equals("pin_settings")) {
                    iconName = "pin";
                } else if (iconName.equals("equalizer_settings")) {
                    iconName = "equalizer";
                }

                if (mApp.getSharedPreferences().getString(SELECTED_THEME, LIGHT_CARDS_THEME).equals(DARK_THEME) ||
                    mApp.getSharedPreferences().getString(SELECTED_THEME, LIGHT_CARDS_THEME).equals(DARK_CARDS_THEME)) {
                    resourceID = context.getResources().getIdentifier(iconName + "_light", "drawable", context.getPackageName());
                } else {
                    resourceID = context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
                }

            } else {

                if (mApp.getSharedPreferences().getString(SELECTED_THEME, LIGHT_CARDS_THEME).equals(DARK_THEME) ||
                    mApp.getSharedPreferences().getString(SELECTED_THEME, LIGHT_CARDS_THEME).equals(DARK_CARDS_THEME)) {
                    resourceID = context.getResources().getIdentifier(iconName + "_light", "drawable", context.getPackageName());
                } else {
                    resourceID = context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
                }

            }
			
		}
		
		return resourceID;
		
	}

	/**
	 * Background gradient in the flipped fragments' listviews.
	 * 
	 * NOTE: The dark theme now uses the default background. 
	 * 		 Only the light theme will use a custom background.
	 */
	public static Drawable getBackgroundGradientDrawable(Context context) {
		
		Drawable backgroundDrawable;
		mApp = (Common) context.getApplicationContext();
		
		if (mApp.getSharedPreferences().getString(SELECTED_THEME, LIGHT_CARDS_THEME).equals(DARK_THEME) || 
			mApp.getSharedPreferences().getString(SELECTED_THEME, LIGHT_CARDS_THEME).equals(DARK_CARDS_THEME)) {
			backgroundDrawable = context.getResources().getDrawable(R.drawable.dark_gray_gradient);
		} else {
			backgroundDrawable = context.getResources().getDrawable(R.drawable.holo_white_selector);
		}
		
		return backgroundDrawable;
	}
	
	/**
	 * Returns the resource ID for the music player controls background
	 * in NowPlayingActivity.java.
	 */
	public static int getNowPlayingControlsBackground(Context context) {
		
		int resourceID = 0;
		mApp = (Common) context.getApplicationContext();
		
		if (mApp.getSharedPreferences().getString(SELECTED_THEME, LIGHT_CARDS_THEME).equals(DARK_THEME) || 
			mApp.getSharedPreferences().getString(SELECTED_THEME, LIGHT_CARDS_THEME).equals(DARK_CARDS_THEME)) {
			resourceID = context.getResources().getIdentifier("now_playing_controls_background", "drawable", context.getPackageName());
		} else {
			resourceID = context.getResources().getIdentifier("now_playing_controls_background_light", "drawable", context.getPackageName());
		}
		
		return resourceID;
		
	}

	/**
	 * Returns the resource ID for the music player info (song, artist, album)
	 * background in NowPlayingActivity.java.
	 */
	public static int getNowPlayingInfoBackground(Context context) {
		
		int resourceID = 0;
		mApp = (Common) context.getApplicationContext();
		
		if (mApp.getSharedPreferences().getString(SELECTED_THEME, LIGHT_CARDS_THEME).equals(DARK_CARDS_THEME) || 
			mApp.getSharedPreferences().getString(SELECTED_THEME, LIGHT_CARDS_THEME).equals(DARK_THEME)) {
			resourceID = context.getResources().getIdentifier("now_playing_title_background", "drawable", context.getPackageName());
		} else {
			resourceID = context.getResources().getIdentifier("now_playing_title_background_light", "drawable", context.getPackageName());
		}
		
		return resourceID;
		
	}
	
	/**
	 * Returns the background color for the Now Playing elements in NowPlayingQueueFragment. 
	 */
	public static int getNowPlayingQueueBackground(Context context) {
		int hexColor;
		mApp = (Common) context.getApplicationContext();
		
		if (mApp.getSharedPreferences().getString(SELECTED_THEME, LIGHT_CARDS_THEME).equals(DARK_THEME) || 
			mApp.getSharedPreferences().getString(SELECTED_THEME, LIGHT_CARDS_THEME).equals(DARK_CARDS_THEME)) {
			hexColor = 0xFF3A3A3A;
		} else {
			hexColor = 0xFFDCDCDC;
		}
		
		return hexColor;
	}
	
	/**
	 * Returns the ActionBar color based on the selected color theme (not used for the player).
	 */
	public static Drawable getGeneralActionBarBackground(Context context) {
		
		mApp = (Common) context.getApplicationContext();
		
		Drawable drawable = null;
		if (mApp.getSharedPreferences().getString(NOW_PLAYING_COLOR, BLUE).equals(BLUE)) {
			drawable = new ColorDrawable(0xFF0099CC);
			
		} else if (mApp.getSharedPreferences().getString(NOW_PLAYING_COLOR, BLUE).equals(RED)) {
			drawable = new ColorDrawable(0xFFCC0000);
			
		} else if (mApp.getSharedPreferences().getString(NOW_PLAYING_COLOR, BLUE).equals(GREEN)) {
			drawable = new ColorDrawable(0xFF669900);
			
		} else if (mApp.getSharedPreferences().getString(NOW_PLAYING_COLOR, BLUE).equals(ORANGE)) {
			drawable = new ColorDrawable(0xFFFF8800);
			
		} else if (mApp.getSharedPreferences().getString(NOW_PLAYING_COLOR, BLUE).equals(PURPLE)) {
			drawable = new ColorDrawable(0xFF9933CC);
			
		} else if (mApp.getSharedPreferences().getString(NOW_PLAYING_COLOR, BLUE).equals(MAGENTA)) {
			drawable = new ColorDrawable(0xFFCE0059);
			
		} else if (mApp.getSharedPreferences().getString(NOW_PLAYING_COLOR, BLUE).equals(GRAY)) {
			drawable = new ColorDrawable(0xFFAAAAAA);
			
		}  else if (mApp.getSharedPreferences().getString(NOW_PLAYING_COLOR, BLUE).equals(WHITE)) {
			drawable = context.getResources().getDrawable(R.drawable.ab_solid_light_holo);
			
		} else if (mApp.getSharedPreferences().getString(NOW_PLAYING_COLOR, BLUE).equals(BLACK)) {
			drawable = context.getResources().getDrawable(R.drawable.holo_gray_selector);
			
		} else {
			drawable = context.getResources().getDrawable(R.drawable.holo_gray_selector);
		}
		
		return drawable;
		
	}
	
	/**
	 * Returns an array of color values for the QuickScroll view.
	 */
	public static int[] getQuickScrollColors(Context context) {
		int[] colors = new int[3];
		
		mApp = (Common) context.getApplicationContext();
		
		if (mApp.getSharedPreferences().getString(NOW_PLAYING_COLOR, BLUE).equals(BLUE)) {
			colors[0] = 0xFF0099CC;
			colors[1] = 0x990099CC;
			colors[2] = Color.WHITE;
			
		} else if (mApp.getSharedPreferences().getString(NOW_PLAYING_COLOR, BLUE).equals(RED)) {
			colors[0] = 0xFFCC0000;
			colors[1] = 0x99CC0000;
			colors[2] = Color.WHITE;
			
		} else if (mApp.getSharedPreferences().getString(NOW_PLAYING_COLOR, BLUE).equals(GREEN)) {
			colors[0] = 0xFF669900;
			colors[1] = 0x99669900;
			colors[2] = Color.WHITE;
			
		} else if (mApp.getSharedPreferences().getString(NOW_PLAYING_COLOR, BLUE).equals(ORANGE)) {
			colors[0] = 0xFFFF8800;
			colors[1] = 0x99FF8800;
			colors[2] = Color.WHITE;
			
		} else if (mApp.getSharedPreferences().getString(NOW_PLAYING_COLOR, BLUE).equals(PURPLE)) {
			colors[0] = 0xFF9933CC;
			colors[1] = 0x999933CC;
			colors[2] = Color.WHITE;
			
		} else if (mApp.getSharedPreferences().getString(NOW_PLAYING_COLOR, BLUE).equals(MAGENTA)) {
			colors[0] = 0xFFA60056;
			colors[1] = 0x99A60056;
			colors[2] = Color.WHITE;
			
		} else if (mApp.getSharedPreferences().getString(NOW_PLAYING_COLOR, BLUE).equals(GRAY)) {
			colors[0] = 0xFF707070;
			colors[1] = 0x99707070;
			colors[2] = Color.WHITE;
			
		}  else if (mApp.getSharedPreferences().getString(NOW_PLAYING_COLOR, BLUE).equals(WHITE)) {
			colors[0] = 0xFFBBBBBB;
			colors[1] = 0x99EEEEEE;
			colors[2] = Color.BLACK;
			
		} else if (mApp.getSharedPreferences().getString(NOW_PLAYING_COLOR, BLUE).equals(BLACK)) {
			colors[0] = 0xFF0A0A0A;
			colors[1] = 0x990A0A0A;
			colors[2] = Color.WHITE;
			
		} else {
			colors[0] = 0xFFEEEEEE;
			colors[1] = 0x99EEEEEE;
			colors[2] = Color.BLACK;
		}
		
		return colors;
	}

    /**
     * Returns the correct empty color patch drawable based on the selected theme.
     */
    public static int getEmptyColorPatch(Context context) {
        mApp = (Common) context.getApplicationContext();

        int resourceID = 0;
        if (mApp.getSharedPreferences().getString(SELECTED_THEME, LIGHT_CARDS_THEME).equals(DARK_CARDS_THEME) ||
                mApp.getSharedPreferences().getString(SELECTED_THEME, LIGHT_CARDS_THEME).equals(DARK_THEME)) {
            resourceID = context.getResources().getIdentifier("empty_color_patch", "drawable", context.getPackageName());
        } else {
            resourceID = context.getResources().getIdentifier("empty_color_patch_light", "drawable", context.getPackageName());
        }

        return resourceID;
    }

}
