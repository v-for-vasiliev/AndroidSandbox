package ru.vasiliev.sandbox.visionlabs.presentation.registration;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.vasiliev.sandbox.R;

public class FaceNotFoundFragment extends Fragment {

    public enum Reason {
        NOT_FOUND, LIVENESS;
    }

    public interface Listener {

        void onRetryWhenFaceNotFound();
    }

    public static final String TAG = FaceNotFoundFragment.class.getName();

    @BindView(R.id.reasonTextView)
    TextView reasonTextView;

    private Listener mListener;

    private Reason reason = Reason.NOT_FOUND;

    public FaceNotFoundFragment() {

    }

    public static FaceNotFoundFragment newInstance() {
        return new FaceNotFoundFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_face_not_found, container, false);
        ButterKnife.bind(this, view);

        String reasonText = reason == Reason.NOT_FOUND ? getResources()
                .getString(R.string.error_face_not_found)
                : getResources().getString(R.string.error_liveness);

        reasonTextView.setText(reasonText);

        return view;
    }

    @Override
    public String toString() {
        return "FaceNotFoundFragment";
    }

    @OnClick(R.id.retry)
    public void onClick() {
        if (mListener != null) {
            mListener.onRetryWhenFaceNotFound();
        }
    }

    public void setReason(Reason reason) {
        this.reason = reason;
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }
}
