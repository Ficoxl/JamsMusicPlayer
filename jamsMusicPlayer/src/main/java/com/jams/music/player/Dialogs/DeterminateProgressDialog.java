package com.jams.music.player.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jams.music.player.R;
import com.jams.music.player.Helpers.TypefaceHelper;

public class DeterminateProgressDialog extends DialogFragment {

	private Activity parentActivity;
	public static DeterminateProgressDialog dialog;
	public static TextView progressText;
	public static ProgressBar progressBar;
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

		parentActivity = getActivity();
		dialog = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        String title = getArguments().getString("TITLE");
        String text = getArguments().getString("TEXT");
        
        View progressView = parentActivity.getLayoutInflater().inflate(R.layout.determinate_progress_dialog, null);
        progressText = (TextView) progressView.findViewById(R.id.determinate_progress_dialog_text);
        progressText.setTypeface(TypefaceHelper.getTypeface(parentActivity, "RobotoCondensed-Light"));
        progressText.setPaintFlags(progressText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        progressText.setText(text);
        
        progressBar = (ProgressBar) progressView.findViewById(R.id.determinate_progress_dialog_bar);
        
        builder.setTitle(title);
        builder.setView(progressView);

        return builder.create();
    }
	
}
