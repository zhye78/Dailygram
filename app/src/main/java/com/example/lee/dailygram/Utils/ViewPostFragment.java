package com.example.lee.dailygram.Utils;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.lee.dailygram.R;
import com.example.lee.dailygram.models.Comment;
import com.example.lee.dailygram.models.Like;
import com.example.lee.dailygram.models.Photo;
import com.example.lee.dailygram.models.User;
import com.example.lee.dailygram.models.UserAccountSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class ViewPostFragment extends Fragment{
    private static final String TAG = "ViewPostFragment";

    public interface OnCommentThreadSelectedListener{
        void onCommentThreadSelectedListener(Photo photo);
    }
    OnCommentThreadSelectedListener mOnCommentThreadSelectedListener;

    public ViewPostFragment(){
        super();
        setArguments(new Bundle());
    }

    //파이어베이스//
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;

    //위젯들//
    private SquareImageView mPostImage;
    private BottomNavigationViewEx bottomNavigationView;
    private TextView mBackLabel,mCaption,mUsername,mTimestamp,mLikes,mComments;
    private ImageView mBackArrow, mEllipses, mHeartRed,mHeartWhite,mProfileImage,mMorebutton,mComment;

    //vars
    private Photo mPhoto;
    private int mActivityNumber=0;
    private String photoUsername="";
    private String photoUrl="";
    private UserAccountSettings mUserAccountSettings;
    private GestureDetector mGestureDetector;
    private Heart mHeart;
    private Boolean mLikedByCurrentUser;
    private StringBuilder mUsers;
    private String mLikesString="";
    private User mCurrentUser;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_post,container,false);
        mPostImage = (SquareImageView) view.findViewById(R.id.post_image);
        bottomNavigationView = (BottomNavigationViewEx) view.findViewById(R.id.bottomNavViewBar);
        mBackArrow = (ImageView) view.findViewById(R.id.backArrow);
        mBackLabel = (TextView) view.findViewById(R.id.tvBackLabel);
        mCaption = (TextView) view.findViewById(R.id.image_caption);
        mUsername = (TextView) view.findViewById(R.id.username);
        mTimestamp = (TextView) view.findViewById(R.id.image_time_posted);
        mMorebutton = (ImageView) view.findViewById(R.id.ivMoreButton);
        mHeartRed = (ImageView) view.findViewById(R.id.image_heart_red);
        mHeartWhite = (ImageView) view.findViewById(R.id.image_heart);
        mProfileImage = (ImageView) view.findViewById(R.id.profile_photo);
        mLikes=(TextView)view.findViewById(R.id.image_likes);
        mComment=(ImageView)view.findViewById(R.id.speech_bubble);
        mComments=(TextView)view.findViewById(R.id.image_comment_link);

        mHeart=new Heart(mHeartWhite,mHeartRed);
        mGestureDetector = new GestureDetector(getActivity(), new GestureListener());

        setupFirebaseAuth();
        setupBottomNavigationView();

        return view;

    }
    private void init() {
        try {
            //mPhoto = getPhotoFromBundle();
            UniversalImageLoader.setImage(getPhotoFromBundle().getImage_path(), mPostImage, null, "");
            mActivityNumber = getActivityNumFromBundle();
            String photo_id = getPhotoFromBundle().getPhoto_id();

            Query query = FirebaseDatabase.getInstance().getReference()
                    .child(getString(R.string.dbname_photos))
                    .orderByChild(getString(R.string.field_photo_id))
                    .equalTo(photo_id);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        Photo newPhoto = new Photo();
                        Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                        newPhoto.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                        newPhoto.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                        newPhoto.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                        newPhoto.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                        newPhoto.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                        newPhoto.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());

                        List<Comment> commentsList = new ArrayList<Comment>();
                        for (DataSnapshot dSnapshot : singleSnapshot
                                .child(getString(R.string.field_comments)).getChildren()) {
                            Comment comment = new Comment();
                            comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                            comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                            comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
                            commentsList.add(comment);
                        }
                        newPhoto.setComments(commentsList);

                        mPhoto = newPhoto;
                        getCurrentUser();
                        getPhotoDetails();
                        //getLikesString();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        } catch (NullPointerException e) {
            Log.e(TAG, "onCreateView: NullPointerException: " + e.getMessage());
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if(isAdded()){
            init();
        }
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mOnCommentThreadSelectedListener=(OnCommentThreadSelectedListener) getActivity();
        }catch (ClassCastException e){
            Log.e(TAG, "onAttach: ClassCastException: "+e.getMessage() );
        }
    }

    private void getLikesString(){
        Log.d(TAG, "getLikesString: getting likes string ");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes));
        Log.d(TAG, "getLikesString: mPhoto.getPhoto_id(): "+mPhoto.getPhoto_id()); //photo_id 잘찍힘
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers=new StringBuilder();
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    Log.d(TAG, "getLikesString-onDataChange: for 1"); //잘찍힘

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                    Query query = reference
                            .child(getString(R.string.dbname_users))
                            .orderByChild(getString(R.string.field_user_id))
                            .equalTo(singleSnapshot.getValue(Like.class).getUser_id());
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                                Log.d(TAG, "getLikesString-onDataChange: for 2"); //잘찍힘
                                Log.d(TAG, "getLikesString-onDataChange: found like: "+
                                        singleSnapshot.getValue(User.class).getUsername()); //username 잘찍힘

                                mUsers.append(singleSnapshot.getValue(User.class).getUsername());
                                mUsers.append(",");
                            }

                            String[] splitUsers = mUsers.toString().split(",");
                            Log.d(TAG, "getLikesString-onDataChange: splitUsers[0]: "+splitUsers[0]); //잘찍힘

                            //이 if문의 mUserAccountSettings가 제대로 할당이 안돼서 돌아가지 않음..
                            //if(mUsers.toString().contains(mUserAccountSettings.getUsername()+",")){
                            if(mUsers.toString().contains(mCurrentUser.getUsername() + ",")){
                                mLikedByCurrentUser=true;
                            }else{
                                mLikedByCurrentUser=false;
                            }

                            int length=splitUsers.length;
                            if(length==1){
                                mLikesString="Likes by "+splitUsers[0];
                            }else if(length==2){
                                mLikesString="Likes by "+splitUsers[0]
                                        + " and "+splitUsers[1];
                            }else if(length==3){
                                mLikesString="Likes by "+splitUsers[0]
                                        + " , "+splitUsers[1]
                                        +" and "+splitUsers[2];
                            }else if(length==4){
                                mLikesString="Likes by " +splitUsers[0]
                                        + " , "+splitUsers[1]
                                        + " , "+splitUsers[2]
                                        + " and "+splitUsers[3];
                            }else  if(length>4){
                                mLikesString="Likes by " +splitUsers[0]
                                        + " , "+splitUsers[1]
                                        + " , "+splitUsers[2]
                                        + " and " + (splitUsers.length-3) + " others";
                            }
                            Log.d(TAG, "onDataChange: likes string: "+mLikesString);
                            setupWidgets();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                if(!dataSnapshot.exists()){
                    mLikesString="";
                    mLikedByCurrentUser=false;
                    setupWidgets();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void getCurrentUser(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_users))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for ( DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                    mCurrentUser = singleSnapshot.getValue(User.class);
                }
                getLikesString();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled.");
            }
        });
    }
    public class GestureListener extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.d(TAG, "onDoubleTap: double tap detected.");

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference
                    .child(getString(R.string.dbname_photos))
                    .child(mPhoto.getPhoto_id())
                    .child(getString(R.string.field_likes));
            Log.d(TAG, "GestureListener-onDoubleTap: mPhoto.getPhoto_id(): "+mPhoto.getPhoto_id());

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){

                        String keyID=singleSnapshot.getKey();
                        //case 1: 유저가 이미 좋아요 누른 상태일때
                        if(mLikedByCurrentUser &&
                                singleSnapshot.getValue(Like.class).getUser_id()
                                .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){

                            myRef.child(getString(R.string.dbname_photos))
                                    .child(mPhoto.getPhoto_id())
                                    .child(getString(R.string.field_likes))
                                    .child(keyID)
                                    .removeValue();

                            myRef.child(getString(R.string.dbname_user_photos))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(mPhoto.getPhoto_id())
                                    .child(getString(R.string.field_likes))
                                    .child(keyID)
                                    .removeValue();

                            mHeart.toggleLike();
                            getLikesString();
                        }

                        //case 2: 좋아요 아직 누르지 않은 사진일때
                        else if(!mLikedByCurrentUser){
                            //add new like
                            Log.d(TAG, "onDataChange: !mLikedByCurrentUser");
                            addNewLike();
                            break;
                        }
                    }
                    if(!dataSnapshot.exists()){
                        //add new like
                        Log.d(TAG, "onDataChange: !dataSnapshot.exists()");
                        addNewLike();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            return true;
        }
    }

    private void addNewLike(){
        Log.d(TAG, "addNewLike: adding new like");

        String newLikeID=myRef.push().getKey();
        Like like=new Like();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        myRef.child(getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like);

        myRef.child(getString(R.string.dbname_user_photos))
                .child(mPhoto.getUser_id())
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like);

        mHeart.toggleLike();
        getLikesString();
    }

    private void getPhotoDetails(){
        Log.d(TAG, "getPhotoDetails: retrieving photo details.");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_user_account_settings))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(mPhoto.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: ");

                //이 방법은 안됨
                //mUserAccountSettings = dataSnapshot.getValue(UserAccountSettings.class);

                //임의추가-얘는 되긴 하지만 모든게 null로 세팅됨->이후 좋아요 기능에 문제 발생..
                /*mUserAccountSettings=mFirebaseMethods.getUserSettings(dataSnapshot).getSettings();
                Log.d(TAG, "onDataChange: userAccountSettings: "+mUserAccountSettings.toString());
                Log.d(TAG, "onDataChange: userAccountSettings: "+mUserAccountSettings.getUser_id());*/

                ////////////아주 큰 문제///////////////
                //아래 for문 실행 안되는듯-로그 안찍힘-mUserAccountSettings 할당 못함
                for ( DataSnapshot singleSnapshot : dataSnapshot.getChildren() ){
                    Log.d(TAG, "onDataChange: mUserAccountSettings setting before");
                    mUserAccountSettings = singleSnapshot.getValue(UserAccountSettings.class);
                    Log.d(TAG, "onDataChange: mUserAccountSettings setting complete!!!");

                    //임의추가
                    //mUserAccountSettings=mFirebaseMethods.getUserSettings(dataSnapshot).getSettings();
                }

                ///원래 setupWidgets() 여기서 호출하는거 아님-mAccountSettings 해결되면 getLikesString에서 호출
                //setupWidgets();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled.");
            }
        });
    }

    private void setupWidgets(){
        String timestampDiff = getTimestampDifference();
        if(!timestampDiff.equals("0")){
            mTimestamp.setText(timestampDiff + " DAYS AGO");
        }else{
            mTimestamp.setText("TODAY");
        }

        ////mUserAccountSettings 할당해야 돌아가는 코드-일단 주석
        UniversalImageLoader.setImage(mUserAccountSettings.getProfile_photo(), mProfileImage, null, "");
        Log.d(TAG, "mUserAccountSettings.getUsername(): "+mUserAccountSettings.getUsername());
        mUsername.setText(mUserAccountSettings.getUsername());
        mLikes.setText(mLikesString);
        mCaption.setText(mPhoto.getCaption());

        Log.d(TAG, "setupWidgets: mPhoto.getComments().size(): " + mPhoto.getComments().size());
        if(mPhoto.getComments().size() > 0){
            mComments.setText("View all " + mPhoto.getComments().size() + " coments");
        }else{
            mComments.setText("");
        }
        
        mComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to comments thread");

                mOnCommentThreadSelectedListener.onCommentThreadSelectedListener(mPhoto);
            }
        });

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back");
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        mComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back");
                mOnCommentThreadSelectedListener.onCommentThreadSelectedListener(mPhoto);
            }
        });


        /////////이 부분도 결국 mUserAccountSettings 할당 못해서 생기는 오류-좋아요 못누름ㅠㅠ
        if(mLikedByCurrentUser){
            mHeartWhite.setVisibility(View.GONE);
            mHeartRed.setVisibility(View.VISIBLE);
            mHeartRed.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Log.d(TAG, "onTouch: red heart touch detected.");
                    return mGestureDetector.onTouchEvent(event);
                }
            });
        }
        else {
            mHeartWhite.setVisibility(View.VISIBLE);
            mHeartRed.setVisibility(View.GONE);
            mHeartWhite.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Log.d(TAG, "onTouch: white heart touch detected.");
                    return mGestureDetector.onTouchEvent(event);
                }
            });
        }

    }

    /**
     * Returns a string representing the number of days ago the post was made
     * @return
     */
    private String getTimestampDifference(){
        Log.d(TAG, "getTimestampDifference: getting timestamp difference.");

        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CANADA);
        sdf.setTimeZone(TimeZone.getTimeZone("Canada/Pacific"));//google 'android list of timezones'
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        final String photoTimestamp = mPhoto.getDate_created();
        try{
            timestamp = sdf.parse(photoTimestamp);
            difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24 )));
        }catch (ParseException e){
            Log.e(TAG, "getTimestampDifference: ParseException: " + e.getMessage() );
            difference = "0";
        }
        return difference;
    }

    //retrieve the activity number from the incoming bundle from profileActivity interface
    private int getActivityNumFromBundle(){
        Log.d(TAG, "getActivityNumFromBundle: arguments :"+getArguments());

        Bundle bundle = this.getArguments();
        if(bundle !=null){
            return bundle.getInt(getString(R.string.activity_number));
        }else{
            return 0;
        }
    }

    //retrieve the photo from the incoming bundle from profileActivity interface
    private Photo getPhotoFromBundle(){
        Log.d(TAG, "getPhotoFromBundle: arguments :"+getArguments());

        Bundle bundle = this.getArguments();
        if(bundle !=null){
            return bundle.getParcelable(getString(R.string.photo));
        }else{
            return null;
        }
    }

    //bottomNavigationView 셋업 (하단부 네비게이션 애니메이션 포함 세팅)
    private void setupBottomNavigationView(){
        Log.d(TAG,"setupBottomNavigationView : setting up BottomNavigationView !");
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationView);
        BottomNavigationViewHelper.enableNavigation(getActivity(),getActivity(),bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(mActivityNumber);
        menuItem.setChecked(true);
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
