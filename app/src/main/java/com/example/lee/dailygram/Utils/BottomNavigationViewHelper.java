package com.example.lee.dailygram.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.view.MenuItem;

import com.example.lee.dailygram.Home.HomeActivity;
import com.example.lee.dailygram.Likes.LikesActivity;
import com.example.lee.dailygram.Profile.ProfileActivity;
import com.example.lee.dailygram.R;
import com.example.lee.dailygram.Search.SearchActivity;
import com.example.lee.dailygram.Share.ShareActivity;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
/*
    github에서 받아온(BottomNavigationViewEx)를 이용해 bottomNavigationView를 자세히 설정하는 클래스
*/
public class BottomNavigationViewHelper {
    private static final String TAG = "BottomNavigationViewHel";

    public static void setupBottomNavigationView(BottomNavigationViewEx bottomNavigationViewEx){
        Log.d(TAG,"setupBottomNavigationView : bottom 네비게이션 뷰 설정");
        bottomNavigationViewEx.enableAnimation(false);
        bottomNavigationViewEx.enableItemShiftingMode(false);
        bottomNavigationViewEx.enableShiftingMode(false);
        bottomNavigationViewEx.setTextVisibility(false);

    }

    //네비게이션 바랑 액티비티 인텐트로 연결//
    public static void enableNavigation(final Context context, final Activity callingActivity,BottomNavigationViewEx view){
        view.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.ic_house:
                        Intent intent1 = new Intent(context, HomeActivity.class); //ACTIVITY_NUM=0; HOME연결
                        context.startActivity(intent1);
                        callingActivity.overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                        break;
                    case R.id.ic_search:
                        Intent intent2 = new Intent(context, SearchActivity.class);//ACTIVITY_NUM=1; SEARCH연결
                        context.startActivity(intent2);
                        callingActivity.overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                        break ;
                    case R.id.ic_circle:
                        Intent intent3 = new Intent(context, ShareActivity.class);//ACTIVITY_NUM=2; SHARE연결
                        context.startActivity(intent3);
                        callingActivity.overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                        break;
                    case R.id.ic_alert:
                        Intent intent4 = new Intent(context, LikesActivity.class);//ACTIVITY_NUM=3; LIKES연결
                        context.startActivity(intent4);
                        callingActivity.overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                        break;
                    case R.id.ic_android:
                        Intent intent5 = new Intent(context, ProfileActivity.class);//ACTIVITY_NUM=4; PROFILE연결
                        context.startActivity(intent5);
                        callingActivity.overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                        break;

                }

                return false;
            }
        });
    }
}
