package com.example.lee.dailygram.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.lee.dailygram.Login.LoginActivity;
import com.example.lee.dailygram.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignOutFragment extends Fragment {
    private static final String TAG = "SignOutFragment";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private ProgressBar mProgressBar;
    private TextView tvSignout,tvSigningOut;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signout,container,false);

        tvSignout=(TextView)view.findViewById(R.id.tvConfSignout);
        mProgressBar=(ProgressBar)view.findViewById(R.id.progressBar);
        tvSigningOut=(TextView)view.findViewById(R.id.tvSigningOut);
        Button btnConfigSignout=(Button)view.findViewById(R.id.btnConfirmSignout);

        mProgressBar.setVisibility(View.GONE);
        tvSigningOut.setVisibility(View.GONE);

        setupFirebaseAuth();

        btnConfigSignout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: attempting to sign out.");

                mProgressBar.setVisibility(View.VISIBLE);
                tvSigningOut.setVisibility(View.VISIBLE);

                mAuth.signOut();
                getActivity().finish();
            }
        });

        return  view;
    }




    /*
    -------------------------------firebase ---------------------------------
     */

    //setup firebase auth object
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth");

        mAuth = FirebaseAuth.getInstance();

        mAuthListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user=firebaseAuth.getCurrentUser();

                if(user!=null){
                    //user는 등록된아이임
                    Log.d(TAG,"onAuthStateChanged: signed_in: "+user.getUid());
                }
                else{
                    //user는 등록되지않은아이임
                    Log.d(TAG, "onAuthStateChanged: signed_out");

                    //로그아웃하면 홈액티비티로 이동
                    //-홈액티비티가 user가 null인것을 확인하고 다시 로그인액티비티로 이동할것임
                    Log.d(TAG, "onAuthStateChanged: navigating back to login screen.");
                    Intent intent=new Intent(getActivity(),LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mAuthListener!=null)
            mAuth.removeAuthStateListener(mAuthListener);
    }
}
