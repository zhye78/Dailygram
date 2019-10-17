package com.example.lee.dailygram.Home;

import android.content.Intent;

import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


import com.example.lee.dailygram.R;
import com.example.lee.dailygram.Share.ShareActivity;
import com.example.lee.dailygram.Utils.Permissions;

public class CameraFragment extends Fragment {
    private static final String TAG = "CameraFragment";
    private static final int CAMERA_FRAGMENT_NUM = 0;
    private static final int CAMERA_REQUEST_CODE = 5;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        Log.d(TAG, "CameraFragment: onCreateView: started ");


        int currentNum = ((HomeActivity) getActivity()).getCurrentTabNumber();
        Button btnLanchCamera = (Button) view.findViewById(R.id.btnLaunchCamera);
        Log.d(TAG, "onClick: 현재 처음 시작프래그먼트" + currentNum);
        //버튼 누르면 카메라로 인텐트!//
        btnLanchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: launching camera");
                Log.d(TAG, "onClick: 현재 프래그먼트" + ((HomeActivity) getActivity()).getCurrentTabNumber());
                if (((HomeActivity) getActivity()).getCurrentTabNumber() == CAMERA_FRAGMENT_NUM) {//카메라 프래그먼트면
                    if (((HomeActivity) getActivity()).checkPermissions(Permissions.CAMERA_PERMISSION[0])) { //퍼미션 체크하고 체킹되면
                        Log.d(TAG, "onClick: starting camera");
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //ACTION_IMAGE_CAPTURE: 카메라를 인텐트 한 후 이미지를 캡쳐해서 리턴//
                        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
                    } else {
                        Intent intent = new Intent(getActivity(), ShareActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                } else
                    Log.d(TAG, "onCreateView: fail~~~");
            }
        });

        return view;
    }


}






