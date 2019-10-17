package com.example.lee.dailygram.Login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lee.dailygram.Home.HomeActivity;
import com.example.lee.dailygram.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG="LoginActivity";

    //파이어베이스
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private Context mContext;
    private ProgressBar mProgressBar;
    private EditText mEmail, mPassword;
    private TextView mPleaseWait;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mProgressBar=(ProgressBar)findViewById(R.id.progressbar);
        mPleaseWait=(TextView)findViewById(R.id.pleaseWait);
        mEmail=(EditText)findViewById(R.id.input_email);
        mPassword=(EditText)findViewById(R.id.input_password);
        mContext=LoginActivity.this;

        Log.d(TAG,"onCreate: started");

        mPleaseWait.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);

        setupFirebaseAuth();
        init();
    }
    //사용자가 입력을 했는지 안했는지 판단하는 메서드//
    private boolean isStringNull(String stirng){
        Log.d(TAG, "isStringNull: checking string if null");

        if(stirng.equals(""))
            return true;
        else
            return false;
    }

    /*
    -------------------------------firebase ---------------------------------
     */

    private void init(){
        //로그인버튼 초기화
        Button btnLogin=(Button)findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: attempting to log in");

                String email=mEmail.getText().toString();
                String password=mPassword.getText().toString();

                //사용자가 모든 항목(아이디,비밀번호) 안채웠을 경우
                if(isStringNull(email) && isStringNull(password)){
                    Toast.makeText(mContext,"모든 항목을 다 채워주세요",Toast.LENGTH_SHORT).show();
                }
                else{ //채웠을경우 프로그래스바,wait VISIBLE 한 후 로그인 처리
                    mProgressBar.setVisibility(View.VISIBLE);
                    mPleaseWait.setVisibility(View.VISIBLE);

                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    Log.d(TAG, "signInWithEmail: onComplete: "+task.isSuccessful());

                                    FirebaseUser user=mAuth.getCurrentUser();
                                    // 로그인 실패시
                                    if (!task.isSuccessful()) {
                                        Log.d(TAG, "signInWithEmail:failed");
                                        Toast.makeText(LoginActivity.this,getString(R.string.auth_failed),
                                                Toast.LENGTH_SHORT).show();
                                        mProgressBar.setVisibility(View.GONE);
                                        mPleaseWait.setVisibility(View.GONE);
                                    }
                                    else{ //로그인 성공시
                                        try{
                                            if(user.isEmailVerified()){
                                                Log.d(TAG, "onComplete: success email is verified.");
                                                Intent intent=new Intent(LoginActivity.this,HomeActivity.class);
                                                startActivity(intent);
                                            }
                                            else{
                                                Toast.makeText(mContext,"Email is not verified \n check your email inbox",Toast.LENGTH_SHORT).show();
                                                mProgressBar.setVisibility(View.GONE);
                                                mPleaseWait.setVisibility(View.GONE);
                                                mAuth.signOut();
                                            }
                                        }catch (NullPointerException e){
                                            Log.d(TAG, "onComplete: NullPointerException: "+e.getMessage());
                                        }
                                    }
                                }
                            });
                }
            }
        });
        //linkSignUP Button 누르면 RegisterActivity로 인텐트//
        TextView linkSignUp=(TextView)findViewById(R.id.link_signup);
        linkSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to register screen");
                Intent intent=new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });

        //로그인 성공시 HomeActivity로 인텐트//
        if(mAuth.getCurrentUser()!=null){
            Intent intent=new Intent(LoginActivity.this,HomeActivity.class);
            startActivity(intent);
            finish();
        }
    }

    //setup firebase auth object
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting upfirebase auth");

        mAuth = FirebaseAuth.getInstance();

        mAuthListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user=firebaseAuth.getCurrentUser();

                if(user!=null){
                    Log.d(TAG,"onAuthStateChanger: signed_in: "+user.getUid());
                }
                else{
                    Log.d(TAG, "onAuthStateChanged: signed_out");
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
    protected void onStop() {
        super.onStop();
        if(mAuthListener!=null)
            mAuth.removeAuthStateListener(mAuthListener);
    }
}
