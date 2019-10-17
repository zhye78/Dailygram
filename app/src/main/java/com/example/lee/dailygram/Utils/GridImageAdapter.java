package com.example.lee.dailygram.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;

import com.example.lee.dailygram.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;

public class GridImageAdapter extends ArrayAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private int layoutResource;
    private String mAppend;
    private ArrayList<String> imgURLs;

    public GridImageAdapter(Context context, int layoutResource, String mAppend, ArrayList<String> imgURLs) {
        super(context,layoutResource, imgURLs);
        this.mContext = context;
        this.mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.layoutResource = layoutResource;
        this.mAppend = mAppend;
        this.imgURLs = imgURLs;
    }

    private static class ViewHolder{
        SquareImageView profileImage;
        ProgressBar mProgressBar;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder viewHolder;
        // 뷰홀더 패턴 //
        if(convertView == null){
            convertView =mInflater.inflate(layoutResource,parent,false);
            viewHolder= new ViewHolder();
            viewHolder.mProgressBar = (ProgressBar)convertView.findViewById(R.id.gridImageProgressbar);
            viewHolder.profileImage = (SquareImageView)convertView.findViewById(R.id.gridImageView);

            convertView.setTag(viewHolder);

        }
        else{
            viewHolder = (ViewHolder)convertView.getTag();
        }
        //타비앙이랑 다르다 -> 근데 ok//
        String imgURL = (String)getItem(position);
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(mAppend + imgURL, viewHolder.profileImage, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                if(viewHolder.mProgressBar != null){
                    viewHolder.mProgressBar.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                if(viewHolder.mProgressBar != null){
                    viewHolder.mProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                if(viewHolder.mProgressBar != null){
                    viewHolder.mProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                if(viewHolder.mProgressBar != null){
                    viewHolder.mProgressBar.setVisibility(View.GONE);
                }
            }
        });

        return convertView;
    }
}
