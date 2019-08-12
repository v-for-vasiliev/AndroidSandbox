package ru.vasiliev.sandbox.visionlabs.presentation.registration;


import com.makeramen.roundedimageview.RoundedImageView;

import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.vasiliev.sandbox.R;

public class SavePhotoFragment extends Fragment {

    public interface Listener {

        void onRetryWhenPhotoAccepted();

        void onRegisterUser();
    }

    public static final String TAG = SavePhotoFragment.class.getName();

    @BindView(R.id.photo)
    RoundedImageView mPhotoPreview;

    Bitmap mBitmap;

    Listener mListener;

    @BindView(R.id.save)
    Button mButtonSave;

    @BindView(R.id.progressBar)
    ProgressBar mProgress;

    public SavePhotoFragment() {

    }

    public static SavePhotoFragment newInstance() {
        return new SavePhotoFragment();
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    public void setPhotoPreview(Bitmap bitmap) {
        this.mBitmap = bitmap;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_save_photo, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPhotoPreview.setImageBitmap(mBitmap);
        mBitmap = null;
        mProgress.getIndeterminateDrawable()
                .setColorFilter(ContextCompat.getColor(getContext(), R.color.accentColor),
                        PorterDuff.Mode.MULTIPLY);
    }

    @OnClick({R.id.retry})
    public void onRetryClick() {
        if (mListener != null) {
            mListener.onRetryWhenPhotoAccepted();
        }
    }

    @OnClick({R.id.save})
    public void onClick() {
        mButtonSave.setEnabled(false);
        mProgress.setVisibility(View.VISIBLE);
        if (mListener != null) {
            mListener.onRegisterUser();
        }
    }
}
