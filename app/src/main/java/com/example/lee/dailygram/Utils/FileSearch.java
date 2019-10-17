package com.example.lee.dailygram.Utils;

import java.io.File;
import java.util.ArrayList;

public class FileSearch {

    // 디렉토리 서치 & 디렉토리 리스트 반환 //
    public static ArrayList<String> getDirectoryPaths(String directory){
        ArrayList<String> pathArray = new ArrayList<>();
        File file = new File(directory);
        File[] listfiles = file.listFiles();
        for(int i = 0; i < listfiles.length; i++){
            if(listfiles[i].isDirectory()){
                pathArray.add(listfiles[i].getAbsolutePath());
            }
        }
        return pathArray;
    }

    // 파일 서치 & 파일 리스트 반환 //
    public static ArrayList<String> getFilePaths(String directory){
        ArrayList<String> pathArray = new ArrayList<>();
        File file = new File(directory);
        File[] listfiles = file.listFiles();
        for(int i = 0; i < listfiles.length; i++){
            if(listfiles[i].isFile()){
                pathArray.add(listfiles[i].getAbsolutePath());
            }
        }
        return pathArray;
    }
}
