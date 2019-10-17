package com.example.lee.dailygram.Utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.example.lee.dailygram.Home.HomeActivity;
import com.example.lee.dailygram.Profile.AccountSettingsActivity;
import com.example.lee.dailygram.R;
import com.example.lee.dailygram.models.Photo;
import com.example.lee.dailygram.models.User;
import com.example.lee.dailygram.models.UserAccountSettings;
import com.example.lee.dailygram.models.UserSettings;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


////회원가입을 위한 클래스-이메일 패스워드 유저네임 등록///
public class FirebaseMethods {
    private static final String TAG = "FirebaseMethods";
    private Context mContext;
    private double mPhotoUploadProgress = 0;
    //파이어베이스
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private String userID;
    private StorageReference mStorageReference;

    public FirebaseMethods(Context context){
        mAuth=FirebaseAuth.getInstance();
        mFirebaseDatabase=FirebaseDatabase.getInstance();
        myRef=mFirebaseDatabase.getReference();
        mStorageReference= FirebaseStorage.getInstance().getReference();
        mContext=context;

        if(mAuth.getCurrentUser()!=null){
            userID=mAuth.getCurrentUser().getUid();
        }
    }

    public int getImageCount(DataSnapshot dataSnapshot){
        int count=0;
        for(DataSnapshot ds: dataSnapshot.child(mContext.getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getChildren()){

            count++;
        }
        return count;
    }

    //사진 올리기 , url 연결 , 진척도 표시 (퍼센트) 및 //
    public void uploadNewPhoto(String photoType, final String caption, int count , String imgUrl,Bitmap bm){
        Log.d(TAG, "uploadNewPhoto: 새로운 사진 올리기 시도중");
        FilePaths filePaths = new FilePaths();
        //case1) new photo
        if(photoType.equals(mContext.getString(R.string.new_photo))){
            Log.d(TAG, "uploadNewPhoto: uploading NEW photo.");

            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            final StorageReference storageReference = mStorageReference
                    .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/photo" + (count + 1));

            //convert image url to bitmap
            if(bm == null){
                bm = ImageManager.getBitmap(imgUrl);
            }

            byte[] bytes = ImageManager.getBytesFromBitmap(bm, 100);

            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //Uri firebaseUrl = taskSnapshot.getDownloadUrl();
                    //Task<Uri> firebaseUrl = storageReference.getDownloadUrl(); // 오류나서 아예 storageReference에 onSuccessListener 따로 연결!해봄//
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri firebaseUrl = uri;
                            Toast.makeText(mContext, "photo upload success", Toast.LENGTH_SHORT).show();

                            //add the new photo to 'photos' node and 'user_photos' node
                            addPhotoToDatabase(caption,firebaseUrl.toString());

                            //navigate to the main feed so the user can see their photo
                            Intent intent = new Intent(mContext, HomeActivity.class);
                            mContext.startActivity(intent);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: Photo upload failed.");
                    Toast.makeText(mContext, "Photo upload failed ", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "onProgress: ^0^");
                    double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    if(progress - 15 > mPhotoUploadProgress){
                        Toast.makeText(mContext, "photo upload progress: " + String.format("%.0f", progress) + "%", Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress = progress;
                    }
                    Log.d(TAG, "onProgress: upload progress: " + progress + "% done");
                }
            });


        }  // case 2) new profile photo
        else if(photoType.equals(mContext.getString(R.string.profile_photo))){
            Log.d(TAG, "uploadNewPhoto: uploading new profile photo");

            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            final StorageReference storageReference = mStorageReference
                    .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/profile_photo");

            //convert image url to bitmap
            if(bm == null){
                bm = ImageManager.getBitmap(imgUrl);
            }
            byte[] bytes = ImageManager.getBytesFromBitmap(bm, 100);

            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //Uri firebaseUrl = taskSnapshot.getDownloadUrl();
                    //Task<Uri> firebaseUrl = storageReference.getDownloadUrl(); // 오류나서 아예 storageReference에 onSuccessListener 따로 연결!해봄//
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri firebaseUrl = uri;
                            Toast.makeText(mContext, "profile photo upload success", Toast.LENGTH_SHORT).show();

                            //'user_account_settings' 노드 입력
                            setProfilePhoto(firebaseUrl.toString());

                            //뷰페이저를 이용해 프로필 사진을 변경하면 바로 다시 edit profile fragment로 돌아가게 설정//
                            ((AccountSettingsActivity)mContext).setViewPager(
                                    ((AccountSettingsActivity)mContext).pagerAdapter
                                            .getFragmentNumber(mContext.getString(R.string.edit_profile_fragment))
                            );
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: profile Photo upload failed.");
                    Toast.makeText(mContext, "profile Photo upload failed ", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "onProgress: ^0^");
                    double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    if(progress - 15 > mPhotoUploadProgress){
                        Toast.makeText(mContext, "profile photo upload progress: " + String.format("%.0f", progress) + "%", Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress = progress;
                    }
                    Log.d(TAG, "onProgress: profile upload progress: " + progress + "% done");
                }
            });
        }

    }
    private void setProfilePhoto(String url){
        Log.d(TAG, "setProfilePhoto: setting new profile image: " + url);
        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mContext.getString(R.string.profile_photo))
                .setValue(url);
    }
    private String getTimeStamp(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.KOREA);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        return simpleDateFormat.format(new Date());
    }
    /*
        In -> some descroptions #tag1 #tag2 #othertag
        out -> #tag1,#tag2,#tag3

     */
    //Photo 모델 객체에 데이터를 저장한 후 데이터베이스에 사진 넣기
    private void addPhotoToDatabase(String caption, String url){
        Log.d(TAG, "addPhotoToDatabase: 데이터 베이스에 사진 추가");

        String tags = StringManipulation.getTags(caption);
        String newPhotoKey = myRef.child(mContext.getString(R.string.dbname_photos)).push().getKey();
        Photo photo = new Photo();
        photo.setCaption(caption);
        photo.setDate_created(getTimeStamp());
        photo.setImage_path(url);
        photo.setTags(tags);
        photo.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        photo.setPhoto_id(newPhotoKey);

        // 데이터베이스에 넣기
        myRef.child(mContext.getString(R.string.dbname_user_photos)).
                child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(newPhotoKey).setValue(photo);
        myRef.child(mContext.getString(R.string.dbname_photos)).child(newPhotoKey).setValue(photo);


    }

    //'user_account_settings' 노드 현재 유저로 갱신
    public void updateUserAccountSettings(String displayName, String website, String description, long phoneNumber){
        Log.d(TAG, "updateUserAccountSettings: updating user account settings.");

        if(displayName!=null){
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_display_name))
                    .setValue(displayName);
        }

        if(website!=null) {
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_website))
                    .setValue(website);
        }

        if(description!=null) {
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_description))
                    .setValue(description);
        }

        if(phoneNumber!=0) {
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_phone_number))
                    .setValue(phoneNumber);
        }
    }

    ////'users' 노드와 'user_account_settings' 노드의 username을 갱신
    public void updateUsername(String username){
        Log.d(TAG, "updateUsername: updating username to: "+username);

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);

        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);
    }

    ////'users' 노드의 email을 갱신
    public void updateEmail(String email){
        Log.d(TAG, "updateEmail: updating email to: "+email);

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .child(mContext.getString(R.string.field_email))
                .setValue(email);

    }

    //이미 있는 사용자인지 체크
    /*public boolean checkIfUsernameExists(String username, DataSnapshot dataSnapshot){
        Log.d(TAG, "checkIfUsernameExists: checking if "+username+" already exists.");

        User user=new User();

        for(DataSnapshot ds: dataSnapshot.child(userID).getChildren()){
            Log.d(TAG, "checkIfUsernameExists: datasnapshot: "+ds);

            user.setUsername(ds.getValue(User.class).getUsername());
            Log.d(TAG, "checkIfUsernameExists: username: "+user.getUsername());

            if(StringManipulation.expendUsername(user.getUsername()).equals(username)){
                Log.d(TAG, "checkIfUsernameExists: FOUND A MATCH: "+user.getUsername());
                return true;
            }
        }
        return  false;
    }*/

    //파이어베이스 사용자인증에 이메일과 패스워드 추가 메소드
    public void registerNewEmail(final String email, String password, final String username){
        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete: "+task.isSuccessful());

                        if(!task.isSuccessful()){
                            Toast.makeText(mContext,R.string.auth_failed,Toast.LENGTH_SHORT).show();
                        }
                        else if(task.isSuccessful()){
                            //send verification email
                            sendVerificationEmail();

                            userID=mAuth.getCurrentUser().getUid();
                            Log.d(TAG, "onComplete: Authstate changed: "+userID);
                        }
                    }
                });
    }

    public void sendVerificationEmail(){
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();

        if(user!=null){
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

                            }else{
                                Toast.makeText(mContext,"couldn't send verification email.",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    public void addNewUser(String email,String username, String description, String website,String profile_photo){
        User user=new User(userID, 1, email, StringManipulation.condenseUsername(username));

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .setValue(user);

        UserAccountSettings settings=new UserAccountSettings(
                description,
                username,
                0,
                0,
                0,
                profile_photo,
                StringManipulation.condenseUsername(username),
                website,
                userID
        );

        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .setValue(settings);
    }

    //로그인했던 사용자 의 계정 세팅 가져옴(디비의 'user_account_settings' 노드에서)
    public UserSettings getUserSettings(DataSnapshot dataSnapshot){
        Log.d(TAG, "getUserAccountSettings: retrieving user account settings from firebase.");

        UserAccountSettings settings=new UserAccountSettings();
        User user=new User();

        for(DataSnapshot ds:dataSnapshot.getChildren()){

            //'user_account_settings' 노드
            if(ds.getKey().equals(mContext.getString(R.string.dbname_user_account_settings))) {
                Log.d(TAG, "getUserAccountSettings: datasnapshot: " + ds);

                try {
                    settings.setDisplay_name(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getDisplay_name()
                    );
                    settings.setUsername(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getUsername()
                    );
                    settings.setWebsite(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getWebsite()
                    );
                    settings.setDescription(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getDescription()
                    );
                    settings.setProfile_photo(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getProfile_photo()
                    );
                    settings.setPosts(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getPosts()
                    );
                    settings.setFollowers(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getFollowers()
                    );
                    settings.setFollowing(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getFollowing()
                    );
                    Log.d(TAG, "getUserAccountSettings: retrieved user_account_settings information "+settings.toString());

                } catch (NullPointerException e) {
                    Log.e(TAG, "getUserAccountSettings: NullPointerException: " + e.getMessage());
                }
            }

            //'users' 노드
            if(ds.getKey().equals(mContext.getString(R.string.dbname_users))) {
                Log.d(TAG, "getUserAccountSettings: datasnapshot: " + ds);

                user.setUsername(
                        ds.child(userID)
                                .getValue(User.class)
                                .getUsername()
                );
                user.setEmail(
                        ds.child(userID)
                                .getValue(User.class)
                                .getEmail()
                );
                user.setPhone_number(
                        ds.child(userID)
                                .getValue(User.class)
                                .getPhone_number()
                );
                user.setUser_id(
                        ds.child(userID)
                                .getValue(User.class)
                                .getUser_id()
                );

                Log.d(TAG, "getUserAccountSettings: retrieved users information " + user.toString());
            }
        }

        return new UserSettings(user,settings);
    }


}
