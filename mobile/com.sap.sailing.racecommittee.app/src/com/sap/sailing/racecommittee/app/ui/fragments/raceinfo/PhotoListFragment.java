package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.util.ViewHolder;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.adapters.finishing.FinishListPhotoAdapter;
import com.sap.sailing.racecommittee.app.utils.CameraHelper;

import java.util.ArrayList;

public class PhotoListFragment extends BaseFragment {

    private final static String TAG = PhotoListFragment.class.getName();
    private final static int SHOOTPHOTO = 9000;

    private Uri mPhotoUri;
    private ArrayList<Uri> mPhotos;
    private FinishListPhotoAdapter mAdapter;
    private RecyclerView mPhotoList;

    public PhotoListFragment() {
        mPhotos = new ArrayList<>();
    }

    public static PhotoListFragment newInstance(Bundle args) {
        PhotoListFragment fragment = new PhotoListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.photo_list, container, false);

        ImageView button = ViewHolder.get(layout, R.id.photo_button);
        if (button != null) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String raceId = null;
                    mPhotoUri = CameraHelper.on(getActivity())
                            .getOutputMediaFileUri(CameraHelper.MEDIA_TYPE_IMAGE, raceId);
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);
                    startActivityForResult(intent, SHOOTPHOTO);
                }
            });
        }

        mPhotoList = ViewHolder.get(layout, R.id.photo_list);
        if (mPhotoList != null) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            mPhotoList.setLayoutManager(layoutManager);
            mPhotoList.setHasFixedSize(true);
        }
        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAdapter = new FinishListPhotoAdapter(mPhotos);
        mPhotoList.setAdapter(mAdapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case SHOOTPHOTO:
                mPhotos.add(mPhotoUri);
                mAdapter.notifyDataSetChanged();
                ExLog.i(getActivity(), TAG, "Returned from Photo");
                break;

            default:
                break;
        }
    }
}
