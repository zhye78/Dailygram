package com.example.lee.dailygram.Home;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.example.lee.dailygram.Login.LoginActivity;
import com.example.lee.dailygram.R;
import com.example.lee.dailygram.Utils.BottomNavigationViewHelper;
import com.example.lee.dailygram.Utils.MainfeedListAdapter;
import com.example.lee.dailygram.Utils.SectionsPagerAdapter;
import com.example.lee.dailygram.Utils.UniversalImageLoader;
import com.example.lee.dailygram.Utils.ViewCommentsFragment;
import com.example.lee.dailygram.models.Photo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nostra13.universalimageloader.core.ImageLoader;

/* 홈 액티비티
    - 홈
    - 상단 카메라 버튼
    - 상단 메시지 버튼
 */
public class HomeActivity extends AppCompatActivity implements
        MainfeedListAdapter.OnLoadMoreItemsListener{

    @Override
    public void onLoadMoreItems() {
        Log.d(TAG, "onLoadMoreItems: displaying more photos");
        HomeFragment fragment = (HomeFragment)getSupportFragmentManager()
                .findFragmentByTag("android:switcher:" + R.id.viewpager_container + ":" + mViewPager.getCurrentItem());
        if(fragment != null){
            fragment.displayMorePhotos();
        }
    }

    private static final String TAG="HomeActivity";
    private static final int ACTIVITY_NUM=0;//액티비티 인텐트 넘버
    private static final int HOME_FRAGMENT = 1;

    private Context mContext = HomeActivity.this;

    //파이어베이스
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    //widgets
    private ViewPager mViewPager;
    private FrameLayout mFrameLayout;
    private RelativeLayout mRelativeLayout;

    //로그인 체그 검사 플래그
    public int islogined = 0; //0 로그인x 1 로그인o

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Log.d(TAG,"onCreate : starting !");

        mViewPager = (ViewPager) findViewById(R.id.viewpager_container);
        mFrameLayout = (FrameLayout) findViewById(R.id.container);
        mRelativeLayout = (RelativeLayout) findViewById(R.id.relLayoutParent);

        setupFirebaseAuth();

        initImageLoader();
        setupBottomNavigationView();
        setupViewPager();

        //mAuth.signOut();

    }

    public void onCommentThreadSelected(Photo photo, String callingActivity){
        Log.d(TAG, "onCommentThreadSelected: selected a coemment thread");

        ViewCommentsFragment fragment  = new ViewCommentsFragment();
        Bundle args = new Bundle();

        args.putParcelable(getString(R.string.photo), photo);
        args.putString(getString(R.string.home_activity), getString(R.string.home_activity));

        fragment.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.view_comments_fragment));
        transaction.commit();
    }

    public void hideLayout(){
        Log.d(TAG, "hideLayout: hiding layout");

        mRelativeLayout.setVisibility(View.GONE);
        mFrameLayout.setVisibility(View.VISIBLE);
    }

    public void showLayout(){
        Log.d(TAG, "hideLayout: showing layout");

        mRelativeLayout.setVisibility(View.VISIBLE);
        mFrameLayout.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if(mFrameLayout.getVisibility() == View.VISIBLE){
            showLayout();
        }
    }

     /*
    -------------------------ㅋㅏ메라 프래그먼트를 위한 퍼미션체크, 프래그먼트 넘버반환----------
     */

    //현재 tab의 프래그먼트 넘버를 반환해준다//
    public int getCurrentTabNumber() {
        Log.d(TAG, "getCurrentTabNumber: current item: " + mViewPager.getCurrentItem());
        return mViewPager.getCurrentItem();
    }
    //하나의 permission 체크
    public boolean checkPermissions(String permission) {
        Log.d(TAG, "checkPermissions: checking permission: " + permission);

        int permissionRequest = ActivityCompat.checkSelfPermission(HomeActivity.this, permission);

        if (permissionRequest != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "checkPermissions: \n Permission was not granted for : " + permission);
            return false;
        } else {
            Log.d(TAG, "checkPermissions: \n Permission was granted for : " + permission);
            return true;
        }
    }

    /*
   -------------------------ㅋㅏ메라 프래그먼트를 위한 퍼미션체크, 프래그먼트 넘버반환----------
    */
    private void initImageLoader(){
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }
    /*
        상단 바 프래그먼트를 위한 어댑터 세팅
        -3가지 tab (camra, home ,message)
     */
    private void setupViewPager(){
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new CameraFragment()); //index 0
        adapter.addFragment(new HomeFragment()); //index 1
        adapter.addFragment(new MessageFragment()); //index 2
        mViewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs); //layout_top_tabs의 tabs
        tabLayout.setupWithViewPager(mViewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_camera); //camera
        //tabLayout.getTabAt(1).setIcon(R.drawable.ic_dailygram); //home 사이즈가 너무작다..
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_arrow); // message
        //dailygram 배너를 위한 커스텀 설정
        //View view = getLayoutInflater().inflate(R.layout.layout_top_tabs_dailyicon,null);
        //view.findViewById(R.id.icon).setBackgroundResource(R.drawable.ic_dailygram);
        //tabLayout.addTab(tabLayout.newTab().setCustomView(view));
        tabLayout.getTabAt(1).setText("Dailygram");
        //tabLayout.getTabAt(1).setCustomView(view);

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



    /*
    -------------------------------firebase ---------------------------------
     */

    //'user'라는 param이 로그인된상태인지 검사하는 메소드
    private void checkCurrentUser(FirebaseUser user){
        Log.d(TAG, "checkCurrentUser: checking if user is logged in");

        //만약 user가 아직 로그인 안한 상태면 로그인화면으로 이동
        if(user==null){
            Intent intent=new Intent(mContext,LoginActivity.class);
            startActivity(intent);
        }
    }


    //setup firebase auth object
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth");

        mAuth = FirebaseAuth.getInstance();

        mAuthListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user=firebaseAuth.getCurrentUser();

                //사용자가 로그인상태인지 체크
                checkCurrentUser(user);

                if(user!=null){
                    //user는 등록된아이임
                    Log.d(TAG,"onAuthStateChanged: signed_in: "+user.getUid());
                    islogined=1; //로그인 상태
                }
                else{
                    //user는 등록되지않은아이임
                    Log.d(TAG, "onAuthStateChanged: signed_out");
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        mViewPager.setCurrentItem(HOME_FRAGMENT);
        checkCurrentUser(mAuth.getCurrentUser());
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mAuthListener!=null)
            mAuth.removeAuthStateListener(mAuthListener);
    }
}
