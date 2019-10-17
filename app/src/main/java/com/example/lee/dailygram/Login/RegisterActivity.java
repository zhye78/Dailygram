package com.example.lee.dailygram.Login;

import android.content.Context;
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

import com.example.lee.dailygram.R;
import com.example.lee.dailygram.Utils.FirebaseMethods;
import com.example.lee.dailygram.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG="RegisterActivity";

    private Context mContext;
    private String email, username, password;
    private EditText mEmail, mPassword, mUsername;
    private TextView loadingPleaseWait;
    private Button btnRegister;
    private ProgressBar mProgressBar;

    //파이어베이스
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseMethods firebaseMethods;

    //파이어베이스 데이터베이스
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    private String append="";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mContext=RegisterActivity.this;
        firebaseMethods=new FirebaseMethods(mContext);
        Log.d(TAG,"onCreate: started");

        initWidgets();
        setupFirebaseAuth();
        init();
    }

    private void init(){
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email=mEmail.getText().toString();
                username=mUsername.getText().toString();
                password=mPassword.getText().toString();

                if(checkInputs(email,username,password)){
                    mProgressBar.setVisibility(View.VISIBLE);
                    loadingPleaseWait.setVisibility(View.VISIBLE);

                    firebaseMethods.registerNewEmail(email,password,username);
                }
            }
        });
    }
    //입력란들 공백인지 체크//
    private boolean checkInputs(String email, String username, String password){
        Log.d(TAG, "checkInputs: checking inputs for null values");
        if(email.equals("") || username.equals("") || password.equals("")){
            Toast.makeText(mContext,"모든 항목을 입력해주세요",Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    //위젯들 초기화 설정//
    private void initWidgets(){
        Log.d(TAG, "initWidgets: Initializing Widgets");

        mUsername=(EditText)findViewById(R.id.input_username);
        btnRegister=(Button)findViewById(R.id.btn_register);
        mProgressBar=(ProgressBar)findViewById(R.id.progressbar);
        loadingPleaseWait=(TextView)findViewById(R.id.loadingPleaseWait);
        mEmail=(EditText)findViewById(R.id.input_email);
        mPassword=(EditText)findViewById(R.id.input_password);
        mContext=RegisterActivity.this;

        mProgressBar.setVisibility(View.GONE);
        loadingPleaseWait.setVisibility(View.GONE);
    }

    private boolean isStringNull(String stirng){
        Log.d(TAG, "isStringNull: checking string if null");

        if(stirng.equals(""))
            return true;
        else
            return false;
    }


    //--------------setup firebase auth object------------

    //username이 이미 데이터베이스에 있는지 검사
    private void checkIfUsernameExists(final String username) {
        Log.d(TAG, "checkIfUsernameExists: Checking if "+username+"already exists.");

        DatabaseReference reference=FirebaseDatabase.getInstance().getReference();
        Query query=reference
                .child(getString(R.string.dbname_users))
                .orderByChild(getString(R.string.field_username))
                .equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    if(singleSnapshot.exists()){
                        Log.d(TAG, "checkIfUsernameExists: FOUND A MATCH: "+singleSnapshot.getValue(User.class).getUsername());
                        append=myRef.push().getKey().substring(3,10);
                        Log.d(TAG, "onDataChange: username already exists. Appending random string to name: "+append);
                    }
                }

                String mUsername="";
                mUsername = username + append;

                //데이터베이스에 'users' 항목에 추가
                firebaseMethods.addNewUser(email,mUsername,"","","");
                Toast.makeText(mContext,"Signup successful, Sending verification email.",Toast.LENGTH_SHORT).show();

                mAuth.signOut();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setupFirebaseAuth(){
        Log.d(TAG, "setup FirebaseAuth: setting up firebase auth");

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase=FirebaseDatabase.getInstance();
        myRef=mFirebaseDatabase.getReference();

        mAuthListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user=firebaseAuth.getCurrentUser();

                if(user!=null){
                    Log.d(TAG,"onAuthStateChanger: signed_in: "+user.getUid());

                    /////데이터베이스관련....
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            //첫번째 체크 (이미 있는 아이디인지 체크//
                            checkIfUsernameExists(username);
                            //새로운 유저를 데이터베이스에 추가//
                            //새로운 account setting을 데이터 베이스에 추가//

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    finish();
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
        // Check if user is signed in (non-null) and update UI accordingly.
        mAuth.addAuthStateListener(mAuthListener);

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mAuthListener!=null)
            mAuth.removeAuthStateListener(mAuthListener);
    }
}
