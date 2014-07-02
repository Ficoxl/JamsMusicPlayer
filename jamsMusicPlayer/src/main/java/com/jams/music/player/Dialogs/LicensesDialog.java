package com.jams.music.player.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.jams.music.player.R;
import com.jams.music.player.Helpers.TypefaceHelper;

public class LicensesDialog extends DialogFragment {

	private Activity parentActivity;
	private DialogFragment dialogFragment;
	private View rootView;
	private TextView creativeCommonsLink;
	private TextView creativeCommonsInfo;
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		parentActivity = getActivity();
		dialogFragment = (DialogFragment) getFragmentManager().findFragmentByTag("licensesDialog");
		
		rootView = (View) parentActivity.getLayoutInflater().inflate(R.layout.licenses_dialog_layout, null);
		
		creativeCommonsLink = (TextView) rootView.findViewById(R.id.creative_commons_link);
		creativeCommonsLink.setTypeface(TypefaceHelper.getTypeface(parentActivity, "RobotoCondensed-Light"));
		creativeCommonsLink.setText(Html.fromHtml("<a href=\"http://creativecommons.org/licenses/by-sa/3.0/legalcode\">Creative Commons ShareALike 3.0 License</a> "));
		creativeCommonsLink.setMovementMethod(LinkMovementMethod.getInstance());
		
		creativeCommonsInfo = (TextView) rootView.findViewById(R.id.licenses_text);
		creativeCommonsInfo.setTypeface(TypefaceHelper.getTypeface(parentActivity, "RobotoCondensed-Light"));
		
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //Set the dialog title.
        builder.setTitle(R.string.licenses);
        builder.setView(rootView);
        builder.setPositiveButton(R.string.done, new OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				dialogFragment.dismiss();
				getActivity().finish();
				
			}
        	
        });

        return builder.create();
    }
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		getActivity().finish();
		
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		getActivity().finish();
		
	}
	
}
