package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.util.ViewHolder;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.adapters.finishing.FinishListPhotoAdapter;
import com.sap.sailing.racecommittee.app.ui.views.DividerItemDecoration;
import com.sap.sailing.racecommittee.app.utils.CameraHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PhotoListFragment extends BaseFragment {

    private final static String TAG = PhotoListFragment.class.getName();
    private final static int SHOOTPHOTO = 9000;

    private ArrayList<Uri> mPhotos;
    private FinishListPhotoAdapter mAdapter;
    private RecyclerView mPhotoList;
    private String mRaceId;

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
                    Uri photoUri = CameraHelper.on(getActivity())
                        .getOutputMediaFileUri(CameraHelper.MEDIA_TYPE_IMAGE, mRaceId);
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    PackageManager manager = getActivity().getPackageManager();
                    List<ResolveInfo> activities = manager.queryIntentActivities(intent, 0);
                    if (activities.size() > 0) {
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                        startActivityForResult(intent, SHOOTPHOTO);
                    } else {
                        Toast.makeText(getActivity(), getString(R.string.no_camera), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        mPhotoList = ViewHolder.get(layout, R.id.photo_list);
        if (mPhotoList != null) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            mPhotoList.setLayoutManager(layoutManager);
//            mPhotoList.setHasFixedSize(true);
            mPhotoList.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        }
        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();

        mAdapter = new FinishListPhotoAdapter(mPhotos);
        mPhotoList.setAdapter(mAdapter);
        mRaceId = null;
        refreshPhotoList();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        switch (requestCode) {
        case SHOOTPHOTO:
            refreshPhotoList();
            ExLog.i(getActivity(), TAG, "Returned from Photo");
            break;

        default:
            break;
        }
    }

    private void refreshPhotoList() {
        mPhotos.clear();
        File folder = CameraHelper.on(getActivity()).getOutputMediaFolder(mRaceId);
        File[] files = folder.listFiles();
        for (File file : files) {
            if (file.getName().endsWith(".jpg") || file.getName().endsWith(".mp4")) {
                mPhotos.add(Uri.fromFile(file));
            }
        }
        Collections.sort(mPhotos, new Comparator<Uri>() {
            @Override
            public int compare(Uri lhs, Uri rhs) {
                return lhs.getEncodedPath().compareTo(rhs.getEncodedPath());
            }
        });
        mAdapter.notifyDataSetChanged();
    }
}
