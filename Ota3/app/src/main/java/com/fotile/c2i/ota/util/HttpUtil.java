package com.fotile.c2i.ota.util;

import android.content.Context;
import android.os.Environment;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * @author ： panyw .
 * @date ：2018/2/5 18:05
 * @COMPANY ： Fotile智能厨电研究院
 * @description ：
 */

public class HttpUtil {
    private static String NEW_STATE_SHARED = "new_state_shared";
    private static String NEW_STATE_NAME = "now_state";
    public final static String FILE_STATE_DIR =   Environment.getExternalStorageDirectory().getPath() +"/ota";
    public final static String FILE_STATE_NAME =  "/info.txt";
    public final static String DEPART = "######";
    /**当前ota状态**/
    public final static String OTA_STATE = "ota_state";
    public final static String RECIPES_URL = "recipes_url";
    public static boolean NEW_STATE = false;
    /** 当前版本号*/
    public final static String VERSION = "version";
    public static int version_code = -1;
    // 默认状态
    public final static String DEFLUT_STATE = "false";



    /**获取otaflag**/
    public static boolean isNewState(final Context context){
       InfoBean infoBean =HttpUtil.getStateFromFile();
        return infoBean.isOta_state();

    }

    /**获取是否Debag**/
    public static boolean isDeBag(final Context context){
        InfoBean infoBean =HttpUtil.getStateFromFile();
        return infoBean.isIs_debug();

    }
    /**获取是否Debag**/
    public static boolean isDeBag(){
        InfoBean infoBean =HttpUtil.getStateFromFile();
        return infoBean.isIs_debug();

    }
    public static boolean setStateToFile(boolean ota_flag,String recipes_url,int version_code){
        File file = new File(FILE_STATE_DIR);
        if(!file.exists()){
            OtaLog.LOGOta("===当前文件状态","创建文件夹");
            file.mkdirs();
        }else {
            OtaLog.LOGOta("===当前文件状态","文件夹已经存在");
        }
        File file1 = new File(FILE_STATE_DIR+FILE_STATE_NAME);
        if(!file1.exists()){

            try {
                OtaLog.LOGOta("===当前文件状态","创建文件"+file1);
                file1.createNewFile();
            } catch (IOException e) {
                OtaLog.LOGOta("===当前文件状态","创建文件失败"+file1);
                e.printStackTrace();
            }
        } else {
            OtaLog.LOGOta("===当前文件状态","文件已经存在"+file1);
        }
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        try {
            //文件输出流
            fos = new FileOutputStream(file1);
            osw = new OutputStreamWriter(fos,"utf-8");
            InfoBean infoBean = new InfoBean(ota_flag,recipes_url,version_code);

            String fileString = new Gson().toJson(infoBean,InfoBean.class);
            //写数据
            osw.write(fileString);
            osw.flush();
            fos.flush();
            //关闭文件流
            osw.close();
            fos.close();
            OtaLog.LOGOta("===当前文件状态","写入成功======");
            getStateFromFile();
            return true;
        } catch (Exception e) {
            OtaLog.LOGOta("===当前文件状态","失败1111");
            e.printStackTrace();
            OtaLog.LOGOta("===当前文件状态","失败1111222");
            return false;
        }finally {
            if(fos != null){
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(osw != null){
                try {
                    osw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public static boolean setStateToFile(InfoBean infoBean){
        File file = new File(FILE_STATE_DIR);
        if(!file.exists()){
            OtaLog.LOGOta("===当前文件状态","创建文件夹");
            file.mkdirs();
        }else {
            OtaLog.LOGOta("===当前文件状态","文件夹已经存在");
        }
        File file1 = new File(FILE_STATE_DIR+FILE_STATE_NAME);
        if(!file1.exists()){

            try {
                OtaLog.LOGOta("===当前文件状态","创建文件"+file1);
                file1.createNewFile();
            } catch (IOException e) {
                OtaLog.LOGOta("===当前文件状态","创建文件失败"+file1);
                e.printStackTrace();
            }
        } else {
            OtaLog.LOGOta("===当前文件状态","文件已经存在"+file1);
        }
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        try {
            //文件输出流
            fos = new FileOutputStream(file1);
            osw = new OutputStreamWriter(fos,"utf-8");

            String fileString = new Gson().toJson(infoBean,InfoBean.class);
            //写数据
            osw.write(fileString);
            osw.flush();
            fos.flush();
            //关闭文件流
            osw.close();
            fos.close();
            OtaLog.LOGOta("===当前文件状态","写入成功======");
            getStateFromFile();
            return true;
        } catch (Exception e) {
            OtaLog.LOGOta("===当前文件状态","失败1111");
            e.printStackTrace();
            OtaLog.LOGOta("===当前文件状态","失败1111222");
            return false;
        }finally {
            if(fos != null){
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(osw != null){
                try {
                    osw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean setStateToFile(boolean isDebag){
        InfoBean infoBean = getStateFromFile();
        File file = new File(FILE_STATE_DIR);
        if(!file.exists()){
            OtaLog.LOGOta("===当前文件状态","创建文件夹");
            file.mkdirs();
        }else {
            OtaLog.LOGOta("===当前文件状态","文件夹已经存在");
        }
        File file1 = new File(FILE_STATE_DIR+FILE_STATE_NAME);
        if(!file1.exists()){

            try {
                OtaLog.LOGOta("===当前文件状态","创建文件"+file1);
                file1.createNewFile();
            } catch (IOException e) {
                OtaLog.LOGOta("===当前文件状态","创建文件失败"+file1);
                e.printStackTrace();
            }
        } else {
            OtaLog.LOGOta("===当前文件状态","文件已经存在"+file1);
        }
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        try {
            //文件输出流
            fos = new FileOutputStream(file1);
            osw = new OutputStreamWriter(fos,"utf-8");
            infoBean.setIs_debug(isDebag);
            String fileString = new Gson().toJson(infoBean,InfoBean.class);
            //写数据
            osw.write(fileString);
            osw.flush();
            fos.flush();
            //关闭文件流
            osw.close();
            fos.close();
            OtaLog.LOGOta("===当前文件状态","写入成功======");
            getStateFromFile();
            return true;
        } catch (Exception e) {
            OtaLog.LOGOta("===当前文件状态","失败1111");
            e.printStackTrace();
            OtaLog.LOGOta("===当前文件状态","失败1111222");
            return false;
        }finally {
            if(fos != null){
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(osw != null){
                try {
                    osw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     返回文件内容
     *
     * **/
    public static InfoBean getStateFromFile(){
        File file = new File(FILE_STATE_DIR);
        if(!file.exists()){
            OtaLog.LOGOta("===当前文件状态","创建文件夹2222");
            file.mkdirs();
        }else {
            OtaLog.LOGOta("===当前文件状态","文件夹2222存在");
        }
        File file1 = new File(FILE_STATE_DIR+FILE_STATE_NAME);
        if(!file1.exists()){
            try {
                boolean fk = file1.createNewFile();
                OtaLog.LOGOta("===当前文件状态","创建文件2222"+fk);

            } catch (IOException e) {
                OtaLog.LOGOta("===当前文件状态","创建文件2222失败");
                e.printStackTrace();
            }
        }
        FileInputStream fis = null;
        InputStreamReader isr = null;
        try {
            //输入流
            fis = new FileInputStream(file1);
            isr = new InputStreamReader(fis,"utf-8");

            char input[] = new char[fis.available()];
            isr.read(input);
            //读取文件中的内容
            String result = new String(input);
            isr.close();
            fis.close();
            //拆分成String[]
            if(result == null ||  result.length() ==0){
                OtaLog.LOGOta("===当前文件状态","当前文件读取出来的数据为空");
               return new InfoBean();
            }
            InfoBean infoBean = new Gson().fromJson(result,InfoBean.class);
            return infoBean;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            OtaLog.LOGOta("===当前文件状态","进入异常读取");
            OtaLog.LOGOta("===当前文件状态","当前ota状态 false  出现读取异常");
            File file12 = new File(FILE_STATE_DIR+FILE_STATE_NAME);
            if(!file12.exists()){
                try {
                    boolean result = file12.createNewFile();
                    OtaLog.LOGOta("===当前文件状态","创建文件3333"+result);

                } catch (IOException e1) {
                    e1.printStackTrace();


                }
            }

            return new InfoBean();
        }finally {
            if( fis != null){
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(isr != null){
                try {
                    isr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public static void RemoveInfo(){
        File file1 = new File(FILE_STATE_DIR+FILE_STATE_NAME);
        if(!file1.exists()){
            final boolean delete = file1.delete();
        }
    }


}
