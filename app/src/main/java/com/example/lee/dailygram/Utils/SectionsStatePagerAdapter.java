package com.example.lee.dailygram.Utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SectionsStatePagerAdapter extends FragmentStatePagerAdapter{
    private final List<Fragment> mFragmentList = new ArrayList<>();
                    //프래그먼트오브젝트, 프래그먼트넘버//
    private final HashMap<Fragment,Integer> mFragments = new HashMap<>();
                    //프래그먼트네임,프래그먼트넘버//
    private final HashMap<String,Integer> mFragmentNumbers = new HashMap<>();
                    //프레그먼트넘버,프래그먼트네임//
    private final HashMap<Integer,String> mFragmentNames = new HashMap<>();

    public SectionsStatePagerAdapter(FragmentManager fm) {
        super(fm);
    }


    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }
    public void addFragment(Fragment fragment, String fragmentName){
        mFragmentList.add(fragment);
        mFragments.put(fragment,mFragmentList.size()-1);
        mFragmentNumbers.put(fragmentName,mFragmentList.size()-1);
        mFragmentNames.put(mFragmentList.size()-1,fragmentName);
    }
    // 프래그먼트와 파라미터의 이름을 같이 리턴
    public Integer getFragmentNumber(String fragmentName){
        if(mFragmentNumbers.containsKey(fragmentName)){
            return mFragmentNumbers.get(fragmentName);
        }else{
            return null;
        }
    }

    public Integer getFragmentNumber(Fragment fragment){
        if(mFragmentNumbers.containsKey(fragment)){
            return mFragmentNumbers.get(fragment);
        }else{
            return null;
        }
    }

    public String getFragmentName(Integer fragmentNumber){
        if(mFragmentNames.containsKey(fragmentNumber)){
            return mFragmentNames.get(fragmentNumber);
        }else{
            return null;
        }
    }
}
