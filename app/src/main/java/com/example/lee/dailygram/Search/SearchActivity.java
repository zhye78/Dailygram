package com.example.lee.dailygram.Search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.example.lee.dailygram.Profile.ProfileActivity;
import com.example.lee.dailygram.R;
import com.example.lee.dailygram.Utils.BottomNavigationViewHelper;
import com.example.lee.dailygram.Utils.UserListAdapter;
import com.example.lee.dailygram.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/*
* 회원을 검색하는 액티비티
* */
public class SearchActivity extends AppCompatActivity{
    private static final String TAG = "SearchActivity";
    private static final int ACTIVITY_NUM=1;//액티비티 인텐트 넘버
    private Context mContext = SearchActivity.this;

    //widgets
    private EditText mSearchParam;
    private ListView mListView;
    //vars
    private List<User> mUserList;
    private UserListAdapter mAdapter; //등록된 회원을 리스트해서 가져옴

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        mSearchParam = (EditText) findViewById(R.id.search); //검색할 키워드
        mListView = (ListView) findViewById(R.id.listView);
        Log.d(TAG,"onCreate: started !");
        hideSoftKeyboard();
        setupBottomNavigationView();
        initTextListener();
    }

    //검색할 키워드를 가져와서 서치하기 전에 초기화//
    private void initTextListener(){
        Log.d(TAG, "initTextListener: initializing");
        mUserList = new ArrayList<>();
        mSearchParam.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                String text = mSearchParam.getText().toString().toLowerCase(Locale.getDefault());
                searchForMatch(text);
            }
        });
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

    private void hideSoftKeyboard(){
        if(getCurrentFocus() != null){
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    /*
    * 쿼리를 통해 회원을 가져와서 입력한 키워드와 매치(비교)
    * */
    private void searchForMatch(String keyword){
        Log.d(TAG, "searchForMatch: searching for a match: " + keyword);
        mUserList.clear();
        //update the users list view
        if(keyword.length() ==0){

        }else{
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            final Query query = reference.child(getString(R.string.dbname_users))
                    .orderByChild(getString(R.string.field_username)).equalTo(keyword);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                //회원을 가져와 mUserList에 등록
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onDataChange: test");
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        Log.d(TAG, "onDataChange: found user:" + singleSnapshot.getValue(User.class).toString());
                        mUserList.add(singleSnapshot.getValue(User.class));
                        //update the users list view
                        updateUsersList();
                    }
                    Log.d(TAG, "onDataChange: mUserList : "+mUserList);
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG, "onCancelled: failed");
                }
            });
        }
    }

    //검색한 회원을 클릭했을때 이벤트//
    private void updateUsersList(){
        Log.d(TAG, "updateUsersList: updating users list");
        mAdapter = new UserListAdapter(SearchActivity.this, R.layout.layout_user_listitem, mUserList);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: selected user: " + mUserList.get(position).toString());
                //navigate to profile activity

                Intent intent =  new Intent(SearchActivity.this, ProfileActivity.class);
                intent.putExtra(getString(R.string.calling_activity), getString(R.string.search_activity));
                intent.putExtra(getString(R.string.intent_user), mUserList.get(position));
                startActivity(intent);
            }
        });
    }

}
