package ru.vasiliev.sandbox.visionlabs.presentation.auth;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.vasiliev.sandbox.R;
import ru.vasiliev.sandbox.visionlabs.domain.model.AuthFailReason;

public class FaceNotRecognizedFragment extends Fragment {

    public interface Listener {

        void onRetryWhenFaceNotRecognized();
    }

    public static final String TAG = FaceNotRecognizedFragment.class.getName();

    @BindView(R.id.retry)
    Button mButtonRetry;

    @BindView(R.id.timeText)
    TextView mTextVerificationTime;

    private Listener mListener;

    private int mVerificationTime = 0;

    private AuthFailReason mFailReason;

    public FaceNotRecognizedFragment() {

    }

    public static FaceNotRecognizedFragment newInstance() {
        return new FaceNotRecognizedFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_face_not_recognized, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mVerificationTime != 0) {
            mTextVerificationTime.setText(getString(R.string.verification_time, mVerificationTime));
        }
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    public void setVerificationTime(int timeMs) {
        mVerificationTime = timeMs;
    }

    public void setFailReason(AuthFailReason reason) {
        mFailReason = reason;
    }

    @OnClick(R.id.retry)
    public void onClick() {
        if (mListener != null) {
            mListener.onRetryWhenFaceNotRecognized();
        }
    }
}
