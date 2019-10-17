package com.example.lee.dailygram.Share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lee.dailygram.R;
import com.example.lee.dailygram.Utils.FirebaseMethods;
import com.example.lee.dailygram.Utils.UniversalImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

//파이어베이스에 이미지 업로드를 담당하는 액티비티 //
public class NextActivity extends AppCompatActivity{
    private static final String TAG = "NextActivity";

    //파이어베이스
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;

    //vars
    private String mAppend ="file:/";
    private int imageCount =0;
    private String imgUrl;
    private Bitmap bitmap;
    private Intent intent;

    //위젯들
    private EditText mCaption;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);
        mFirebaseMethods = new FirebaseMethods(NextActivity.this);
        mCaption = (EditText)findViewById(R.id.caption);

        setupFirebaseAuth();

        ImageView backArrow = (ImageView)findViewById(R.id.ivBackArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing the activity.");
                finish();
            }
        });


        TextView share = (TextView)findViewById(R.id.tvShare);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to the final share screen.");
               // 파이어 베이스에 이미지 업로드하기
                Toast.makeText(NextActivity.this,"새로운 사진을 올리기 시도중",Toast.LENGTH_SHORT).show();
                String caption= mCaption.getText().toString();

                if(intent.hasExtra(getString(R.string.selected_image))){
                    imgUrl = intent.getStringExtra(getString(R.string.selected_image));
                    mFirebaseMethods.uploadNewPhoto(getString(R.string.new_photo),caption,imageCount,imgUrl,null);
                }else if(intent.hasExtra(getString(R.string.selected_bitmap))){
                    bitmap = (Bitmap)intent.getParcelableExtra(getString(R.string.selected_bitmap));
                    mFirebaseMethods.uploadNewPhoto(getString(R.string.new_photo),caption,imageCount,null,bitmap);

                }

            }
        });
        setImage();
    }

    //파이어베이스에 이미지를 업로딩 하기 위한 처음부터의 작은 설계 계획//
    private void someMethod(){
        /*  step 1: create a data model for photos
            step 2: add properties to the photo objects ( caption , data, imageUrl, photo_id , tags, user id)
            step 3: count the number of photos that the user already has.
            step 4: upload the photo to firebase storage
                a) insert into 'photos' node
                b) insert into 'user_photos' node
         */

    }
    //들어오는 인텐트에서 이미지 URL을 가져와 선택한 이미지를 표시
    private void setImage(){
        intent = getIntent();
        ImageView imageView = (ImageView)findViewById(R.id.imageShare);

        if(intent.hasExtra(getString(R.string.selected_image))){
            imgUrl = intent.getStringExtra(getString(R.string.selected_image));
            Log.d(TAG, "setImage: got new image url: "+imgUrl);
            UniversalImageLoader.setImage(imgUrl,imageView,null,mAppend);
        }else if(intent.hasExtra(getString(R.string.selected_bitmap))){
            bitmap = (Bitmap)intent.getParcelableExtra(getString(R.string.selected_bitmap));
            Log.d(TAG, "setImage: got new bitmap");
            imageView.setImageBitmap(bitmap);
        }

    }

    /*
    -------------------------------firebase ---------------------------------
     */

    //setup firebase auth object
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth");

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase=FirebaseDatabase.getInstance();
        myRef=mFirebaseDatabase.getReference();
        Log.d(TAG, "onDataChange: image count : "+imageCount);


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
                }
            }
        };

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                imageCount = mFirebaseMethods.getImageCount(dataSnapshot);
                Log.d(TAG, "onDataChange: image count : "+imageCount);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
