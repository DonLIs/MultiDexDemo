package me.donlis.multidexdemo;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final class HotFixManager {

    private static String path;

    public static void installFixedDex(Context context){
        if(context == null){
            return;
        }

        try {
            //存放补丁包的文件路径
            path = Environment.getExternalStorageDirectory().getPath() + "/fixed.dex";

            //查找文件
            File fixedDexFile = new File(path);
            //判断文件是否存在，不存在就退出
            if(!fixedDexFile.exists()){
                return;
            }

            //获取ClassLoader下的pathList字段
            Field pathListField = ReflectUtil.findField(context.getClassLoader(), "pathList");
            Object dexPathList = pathListField.get(context.getClassLoader());

            //获取ClassLoader下的makeDexElements方法
            Method makeDexElements;
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
                makeDexElements = ReflectUtil.findMethod(dexPathList, "makePathElements", List.class, File.class, List.class, ClassLoader.class);
            }else{
                makeDexElements = ReflectUtil.findMethod(dexPathList, "makeDexElements", ArrayList.class, File.class, ArrayList.class);
            }

            //创建列表存放补丁包
            ArrayList<File> filesToBeInstalled = new ArrayList<>();
            if(fixedDexFile.exists()){
                filesToBeInstalled.add(fixedDexFile);
            }

            //创建补丁包的dexElements文件的存放路径
            File optimizedDir = new File(context.getFilesDir(), "fixed_dex");
            if(!optimizedDir.exists()){
                optimizedDir.mkdir();
            }
            ArrayList<IOException> suppressedException = new ArrayList<>();

            //创建补丁包的dexElements文件
            Object[] extraElements;
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
                extraElements = (Object[]) makeDexElements.invoke(dexPathList, filesToBeInstalled, optimizedDir, suppressedException, context.getClassLoader());
            }else{
                extraElements = (Object[]) makeDexElements.invoke(dexPathList, filesToBeInstalled, optimizedDir, suppressedException);
            }

            //获取系统原有的dexElements文件
            Field dexElementsField = ReflectUtil.findField(dexPathList, "dexElements");
            Object[] originElements = (Object[]) dexElementsField.get(dexPathList);

            int len = originElements.length + extraElements.length;

            //创建长度为 补丁文件数 + 原有文件数 的数组
            Object[] elements = (Object[]) Array.newInstance(originElements.getClass().getComponentType(), len);

            //使用系统的数组拷贝方法，先添加补丁文件，再添加原有文件
            System.arraycopy(extraElements,0,elements,0,extraElements.length);
            System.arraycopy(originElements,0,elements,extraElements.length,originElements.length);

            //对dexElements重新赋值
            dexElementsField.set(dexPathList,elements);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
