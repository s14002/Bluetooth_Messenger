package jp.ac.it_college.std.s14002.android.bluetoothmessenger;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by s14002 on 16/01/29.
 */
public class MyAlertDialogFragment extends DialogFragment {
    public static MyAlertDialogFragment newInstance(int title, int message) {
        MyAlertDialogFragment fragment = new MyAlertDialogFragment();
        Bundle args = new Bundle();
        args.putInt("title", title);
        args.putInt("message", message);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int title = getArguments().getInt("title");
        int message = getArguments().getInt("message");

        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                ((MainActivity)getActivity()).doPositiveClick();
                            }
                        }
                )
                .create();
    }
}
