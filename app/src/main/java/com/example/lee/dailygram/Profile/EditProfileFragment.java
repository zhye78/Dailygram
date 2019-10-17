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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lee.dailygram.R;
import com.example.lee.dailygram.Share.ShareActivity;
import com.example.lee.dailygram.Utils.FirebaseMethods;
import com.example.lee.dailygram.Utils.UniversalImageLoader;
import com.example.lee.dailygram.dialogs.ConfirmPasswordDialog;
import com.example.lee.dailygram.models.User;
import com.example.lee.dailygram.models.UserAccountSettings;
import com.example.lee.dailygram.models.UserSettings;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileFragment extends Fragment implements ConfirmPasswordDialog.OnConfirmPasswordListener{

    @Override
    public void onConfirmPassword(String password) {
        Log.d(TAG, "onConfirmPassword: got the password: "+password);

        ////firebase 예제코드 긁어옴
        AuthCredential credential=EmailAuthProvider
                .getCredential(mAuth.getCurrentUser().getEmail(),password);
        mAuth.getCurrentUser().reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "User re-authenticated.");

                            ///////이메일이 DB에 이미 있는지 체크
                            mAuth.fetchProvidersForEmail(mEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
                                @Override
                                public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                                    if(task.isSuccessful()){
                                        try{
                                            if(task.getResult().getProviders().size()==1){
                                                Log.d(TAG, "onComplete: that email is already in use.");

                                                Toast.makeText(getActivity(),"that email is already in use",Toast.LENGTH_SHORT).show();

                                            }else{
                                                Log.d(TAG, "onComplete: that email is available.");

                                                ////이메일 사용 가능하고 업데이트 할것임
                                                mAuth.getCurrentUser().updateEmail(mEmail.getText().toString())
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    Log.d(TAG, "User email address updated.");
                                                                    Toast.makeText(getActivity(),"email updated",Toast.LENGTH_SHORT).show();
                                                                    mFirebaseMethods.updateEmail(mEmail.getText().toString());
                                                                }
                                                            }
                                                        });
                                            }
                                        }catch (NullPointerException e){
                                            Log.d(TAG, "onComplete: NullPointerException: "+e.getMessage());
                                        }
                                    }
                                }
                            });
                        }else{
                            Log.d(TAG, "onComplete: re-authenticated failed.");
                        }
                        
                    }
                });
    }

    private static final String TAG = "EditProfileFragment";

    //파이어베이스
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;
    private String userID;

    //EditProfile 프레그먼트 위젯
    private EditText mDisplayName, mUsername, mWebsite, mDescription, mEmail, mPhoneNumber;
    private TextView mChangeProfilePhoto;
    private CircleImageView mProfilePhoto;

    //variable sections
    private UserSettings mUserSettings;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editprofile,container,false);
        mProfilePhoto = (CircleImageView)view.findViewById(R.id.profile_photo);
        mDisplayName=(EditText)view.findViewById(R.id.display_name);
        mUsername=(EditText)view.findViewById(R.id.username);
        mWebsite=(EditText)view.findViewById(R.id.website);
        mDescription=(EditText)view.findViewById(R.id.description);
        mEmail=(EditText)view.findViewById(R.id.email);
        mPhoneNumber=(EditText)view.findViewById(R.id.phoneNumber);
        mChangeProfilePhoto=(TextView)view.findViewById(R.id.changeProfilePhoto);
        mFirebaseMethods=new FirebaseMethods(getActivity());

        //setProfileImage();
        setupFirebaseAuth();

        //profile 액티비티로 돌아가기
        ImageView backArrow=(ImageView)view.findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back to ProfileActivity");
                getActivity().finish();
            }
        });

        ImageView checkmark=(ImageView)view.findViewById(R.id.saveChanges);
        checkmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfileSettings();
                Log.d(TAG, "onClick: attempting to save changes.");
            }
        });

        return  view;
    }

    //위젯의 데이터를 가져오고 DB로 보냄
    private void saveProfileSettings(){
        Log.d(TAG, "saveProfileSettings: ok");
        
        final String displayName=mDisplayName.getText().toString();
        final String username=mUsername.getText().toString();
        final String website=mWebsite.getText().toString();
        final String description=mDescription.getText().toString();
        final String email=mEmail.getText().toString();
        final long phoneNumber=Long.parseLong(mPhoneNumber.getText().toString());


        //case 1: 유저가 유저네임 바꿀때
        if(!mUserSettings.getUser().getUsername().equals(username)){
            checkIfUsernameExists(username);
        }
        //case 2: 유저가 이메일 바꿀때
        if(!mUserSettings.getUser().getEmail().equals(email)){

            //step 1: Reauthenticate(재증명)-password, email 확인
            ConfirmPasswordDialog dialog=new ConfirmPasswordDialog();
            dialog.show(getFragmentManager(),getString(R.string.confirm_password_dialog));
            dialog.setTargetFragment(EditProfileFragment.this,1);

            //step 2: 이메일이 이미 있는지 체크-'fetchProviderForEmail(String email)'
            //step 3: 이메일 change-DB계정관리에 새 이메일 전송
        }

        //세팅된 정보들 변경-DB의 'user_account_settings' 노드의 내용이 실시간으로 변경됨
        if(!mUserSettings.getSettings().getDisplay_name().equals(displayName)){
            //displayName 항목 갱신
            mFirebaseMethods.updateUserAccountSettings(displayName,null,null,0);

        }
        if(!mUserSettings.getSettings().getWebsite().equals(website)){
            //website 항목 갱신
            mFirebaseMethods.updateUserAccountSettings(null,website,null,0);

        }
        if(!mUserSettings.getSettings().getDescription().equals(description)){
            //desciption 항목 갱신
            mFirebaseMethods.updateUserAccountSettings(null,null,description,0);

        }
        if(!mUserSettings.getSettings().getProfile_photo().equals(phoneNumber)){
            //phoneNumber 항목 갱신
            mFirebaseMethods.updateUserAccountSettings(null,null,null,phoneNumber);

        }
    }

    //username이 이미 데이터베이스에 있는지 검사
    private void checkIfUsernameExists(final String username) {
        Log.d(TAG, "checkIfUsernameExists: Checking if "+username+" already exists.");

        DatabaseReference reference=FirebaseDatabase.getInstance().getReference();
        Query query=reference
                .child(getString(R.string.dbname_users))
                .orderByChild(getString(R.string.field_username))
                .equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    //username 추가
                    mFirebaseMethods.updateUsername(username);
                    Toast.makeText(getActivity(),"saved username.",Toast.LENGTH_SHORT).show();
                }
                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    if(singleSnapshot.exists()){
                        Log.d(TAG, "checkIfUsernameExists: FOUND A MATCH: "+singleSnapshot.getValue(User.class).getUsername());
                        Toast.makeText(getActivity(),"That username already exists.",Toast.LENGTH_SHORT).show();

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setProfileWidgets(UserSettings userSettings){
        Log.d(TAG, "setProfileWidgets: setting widgets with data retrieving from firebase database: "+userSettings.toString());
        Log.d(TAG, "setProfileWidgets: setting widgets with data retrieving from firebase database: "+userSettings.getUser().getEmail());
        Log.d(TAG, "setProfileWidgets: setting widgets with data retrieving from firebase database: "+userSettings.getUser().getPhone_number());

        mUserSettings=userSettings;
        //User user=userSettings.getUser();

        UserAccountSettings settings=userSettings.getSettings();

        UniversalImageLoader.setImage(settings.getProfile_photo(),mProfilePhoto,null,"");

        mDisplayName.setText(settings.getDisplay_name());
        mUsername.setText(settings.getUsername());
        mWebsite.setText(settings.getWebsite());
        mDescription.setText(settings.getDescription());
        mEmail.setText(userSettings.getUser().getEmail());
        mPhoneNumber.setText(String.valueOf(userSettings.getUser().getPhone_number()));

        //change profile photo 버튼 누를시 이벤트 리스너//
        mChangeProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: changing profile photo");
                Intent intent = new Intent(getActivity(), ShareActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //268435456
                getActivity().startActivity(intent);
                getActivity().finish();
            }
        });

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
        userID=mAuth.getCurrentUser().getUid();

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

                //데이터베이스에서 사용자정보 가져와서 프로필의 위젯들 내용으로 출력
                setProfileWidgets(mFirebaseMethods.getUserSettings(dataSnapshot));

                //이미지 가져옴
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
