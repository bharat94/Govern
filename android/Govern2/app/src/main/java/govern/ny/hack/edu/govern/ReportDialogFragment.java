package govern.ny.hack.edu.govern;


import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

public class ReportDialogFragment extends DialogFragment implements TextView.OnEditorActionListener {

    private static final String ARGS_LATITUDE = "args-latitutde";
    private static final String ARGS_LONGITUDE = "args-logitude";

    private double mLatitude;
    private double mLogitude;
    private EditText mEditText;

    public static ReportDialogFragment newInstance(double latitude, double longitude) {

        Bundle args = new Bundle();
        args.putDouble(ARGS_LATITUDE, latitude);
        args.putDouble(ARGS_LONGITUDE, longitude);
        ReportDialogFragment fragment = new ReportDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mLatitude = getArguments().getDouble(ARGS_LATITUDE);
            mLogitude = getArguments().getDouble(ARGS_LONGITUDE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, container);

        mEditText = (EditText) view.findViewById(R.id.report_edit_text);

        // set this instance as callback for editor action
        mEditText.setOnEditorActionListener(this);
        mEditText.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        getDialog().setTitle("Please enter username");
        return view;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        // Return input text to activity
        ReportEditTextListener activity = (ReportEditTextListener) getActivity();
        activity.onFinishReportDialog(mEditText.getText().toString(), new LatLng(mLatitude, mLogitude));
        this.dismiss();
        return true;
    }


    public interface ReportEditTextListener {
        void onFinishReportDialog(String value, LatLng location);
    }
}
