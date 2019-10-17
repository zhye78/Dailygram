package com.example.lee.dailygram.Share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.lee.dailygram.Profile.AccountSettingsActivity;
import com.example.lee.dailygram.R;
import com.example.lee.dailygram.Utils.Permissions;

public class PhotoFragment extends Fragment {
    private static final String TAG = "PhotoFragment";
    private static final int PHOTO_FRAGMENT_NUM = 1;
    private static final int GALLERY_FRAGMENT_NUM = 2;
    private static final int CAMERA_REQUEST_CODE = 5;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo, container, false);
        Log.d(TAG, "onCreateView: PhotoFragment started.");
        Button btnLanchCamera = (Button) view.findViewById(R.id.btnLaunchCamera);

        //버튼 누르면 카메라로 인텐트!//
        btnLanchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: launching camera");
                if (((ShareActivity) getActivity()).getCurrentTabNumber() == PHOTO_FRAGMENT_NUM) { //사진프래그먼트면
                    if (((ShareActivity) getActivity()).checkPermissions(Permissions.CAMERA_PERMISSION[0])) { //퍼미션 체크하고 체킹되면
                        Log.d(TAG, "onClick: starting camera");
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //ACTION_IMAGE_CAPTURE: 카메라를 인텐트 한 후 이미지를 캡쳐해서 리턴//
                        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
                    } else {
                        Intent intent = new Intent(getActivity(), ShareActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        /*
                        * FLAG_ACTIVITY_NEW_TASK: 이 액티비티 플래그를 사용하여 엑티비티를 호출하게 되면 새로운 태스크를 생성하여
                        * 그 태스크안에 엑티비티를 추가함. 단, 기존에 존재하는 태스크들중에 생성하려는 엑티비티와 동일한 affinity를
                        * 가지고 있는 태스크가 있다면 그곳으로 새 엑티비티가 들어감.하나의 어플리케이션안에서는 모든 엑티비티가 기본
                        * affinity를 가지고 같은 태스크안에서 동작하는것이 기본적(물론 변경이 가능합니다)이지만 FLAG_ACTIVITY_MULTIPLE_TASK 플래그와
                        * 함께 사용하지 않을경우 무조건적으로 태스크가 새로 생성되는것은 아님을 주의.
                        * FLAG_ACTIVITY_CLEAR_TASK: 기존에 쌓여있는 스택 전부 비우기*/
                        startActivity(intent);
                    }
                }
            }
        });
        return view;
    }

    private boolean isRootTask(){
        if(((ShareActivity)getActivity()).getTask()==0){
            return true;
        }else {
            return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE) {
            Log.d(TAG, "onActivityResult: done taking a photo");
            Log.d(TAG, "onActivityResult: attempting to navigate to final share screen");

            Bitmap bitmap;
            bitmap = (Bitmap) data.getExtras().get("data");
            if(isRootTask()) {
                try{
                    Log.d(TAG, "onActivityResult: received new bitmap form camera :" +bitmap);
                    Intent intent = new Intent(getActivity(), NextActivity.class);
                    intent.putExtra(getString(R.string.selected_bitmap), bitmap);
                    startActivity(intent);
                }catch (NullPointerException e){
                    Log.d(TAG, "onActivityResult: NullPointerException: "+e.getMessage());
                }

            }else{
                try{
                    Log.d(TAG, "onActivityResult: received new bitmap form camera :" +bitmap);
                    Intent intent = new Intent(getActivity(), AccountSettingsActivity.class);
                    intent.putExtra(getString(R.string.selected_bitmap), bitmap);
                    intent.putExtra(getString(R.string.return_to_fragment),getString(R.string.edit_profile_fragment));
                    startActivity(intent);
                    getActivity().finish();
                }catch (NullPointerException e){
                    Log.d(TAG, "onActivityResult: NullPointerException: "+e.getMessage());
                }
            }

        }
    }
}