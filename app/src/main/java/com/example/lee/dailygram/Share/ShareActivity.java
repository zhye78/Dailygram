package com.example.lee.dailygram.Share;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.lee.dailygram.R;
import com.example.lee.dailygram.Utils.BottomNavigationViewHelper;
import com.example.lee.dailygram.Utils.Permissions;
import com.example.lee.dailygram.Utils.SectionsPagerAdapter;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

public class ShareActivity extends AppCompatActivity{
    private static final String TAG = "ShareActivity";

    private static final int ACTIVITY_NUM=2;//액티비티 인텐트 넘버
    private static final int VERIFY_PERMISSIONS_REQUEST=1;

    private ViewPager mViewpager;

    private Context mContext = ShareActivity.this;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        Log.d(TAG,"onCreate: started !");
        if(checkPermissionsArray(Permissions.PERMISSIONS)){
            setupViewPager();
        }else{
            verifyPermissions(Permissions.PERMISSIONS);
        }
    }
    //현재 tab의 프래그먼트 넘버를 반환해준다//

    public int getCurrentTabNumber(){
        return mViewpager.getCurrentItem();

    }

    public int getTask(){
        Log.d(TAG, "getTask: Task: "+getIntent().getFlags());
        return getIntent().getFlags();
    }

    //////share페이지에서 gallery버튼과 photo버튼 눌렀을 때 프레그먼트 바뀌도록
    private void setupViewPager(){
        SectionsPagerAdapter adapter=new SectionsPagerAdapter(getSupportFragmentManager());

        adapter.addFragment(new GalleryFragment());
        adapter.addFragment(new PhotoFragment());

        mViewpager=(ViewPager)findViewById(R.id.viewpager_container);
        mViewpager.setAdapter(adapter);

        TabLayout tabLayout=(TabLayout)findViewById(R.id.tabsBottom); //layout_bottom_tabs 의 tabsBottom
        tabLayout.setupWithViewPager(mViewpager);

        tabLayout.getTabAt(0).setText(getString(R.string.gallery));
        tabLayout.getTabAt(1).setText(getString(R.string.photo));

    }




    public void verifyPermissions(String[] permissions){
        Log.d(TAG, "verifyPermissions: verifying permissions.");

        ActivityCompat.requestPermissions(
                ShareActivity.this,
                permissions,
                VERIFY_PERMISSIONS_REQUEST
        );
    }

    //permissions 배열을 체크
    public boolean checkPermissionsArray(String[] permissions){
        Log.d(TAG, "checkPermissionsArray: checking permission array.");

        for(int i=0;i<permissions.length;i++){
            String check=permissions[i];
            if(!checkPermissions(check)){
                return false;
            }
        }
        return true;
    }

    //하나의 permission 체크
    public boolean checkPermissions(String permission){
        Log.d(TAG, "checkPermissions: checking permission: "+permission);

        int permissionRequest=ActivityCompat.checkSelfPermission(ShareActivity.this,permission);

        if(permissionRequest != PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "checkPermissions: \n Permission was not granted for : "+permission);
            return false;
        }else{
            Log.d(TAG, "checkPermissions: \n Permission was granted for : "+permission);
            return true;
        }
    }

    //bottomNavigationView 셋업
    private void setupBottomNavigationView(){
        Log.d(TAG,"setupBottomNavigationView : setting up BottomNavigationView !");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx)findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext,this,bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
}
