package com.fotile.c2i.ota.util;

import android.content.Context;
import android.util.Base64;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;


/**
 * 文件名称：OtaUpgradeUtil
 * 创建时间：2017-10-09
 * 文件作者：fuzya
 * 功能描述：ota升级工具类
 */
public class OtaUpgradeUtil {
    /**
     * OTA服务器地址
     */
//    public String ServerURL = "http://ota.fotile.com:8080/fotileAdminSystem/upgrade.do?";

    public static final String ServerURL = "http://ota.fotile.com:8080/fotileAdminSystem/upgrade.do?" ;
    public static final String Server_Old_URL = "http://develop.fotile.com:8080/fotileAdminSystem/upgrade.do?";
    public OtaUpgradeUtil() {

    }

    public String buildUrl(String packageName, String currentVersion, String deviceMac, Context context) {
        if(HttpUtil.isNewState(context)){
            StringBuilder sb = new StringBuilder(ServerURL);
            sb.append("package=");
            sb.append(packageName);
            sb.append('&');
            sb.append("version=");
            sb.append(currentVersion);
            sb.append('&');
            sb.append("mac=");
            sb.append(deviceMac);
            return sb.toString();
        }else {
            StringBuilder sb = new StringBuilder(Server_Old_URL);
            sb.append("package=");
            sb.append(packageName);
            sb.append('&');
            sb.append("version=");
            sb.append(currentVersion);
            sb.append('&');
            sb.append("mac=");
            sb.append(deviceMac);
            return sb.toString();
        }

    }


    /**
     * 请求OTA服务器
     */
    public static String httpGet(String strUrl) throws IOException {

        String result = null;

        URL url = new URL(strUrl);
        URLConnection urlConn = url.openConnection();
        HttpURLConnection connection = (HttpURLConnection) urlConn;
        connection.setConnectTimeout(15000);
        connection.setRequestMethod("GET");
        connection.setDoOutput(false);
        connection.setDoInput(true);
        connection.setUseCaches(false);
        connection.setInstanceFollowRedirects(true);
        InputStream is = connection.getInputStream();

        // 从响应中获取长度
        int length = connection.getContentLength();

        if (length != -1) {
            byte[] data = new byte[length];
            byte[] temp = new byte[512];// 每次读取512字节
            int readLen = 0;// 单次读取的长度
            int destPos = 0;// 总字节数
            while ((readLen = is.read(temp)) > 0) {
                System.arraycopy(temp, 0, data, destPos, readLen);
                destPos += readLen;
            }
            result = new String(data, "UTF-8"); // 响应也是UTF-8编码
        }
//         result =
//         "{\"message\":\"2xfknNM/YDiwjeB4MKa9y67gwfOYfjgqvXtGleWPgDop7jn4cUPqdNyBmGpoPB0WyLOCBfwM99Y1
// \\nZYnZQvx5xX0LS6vN2aEb9PXb/sIeYlI6yy4Mv10N7Bmj6J2PWHtoquWANaTCgqGhodccL43i/A21
// \\nXIkw3HBkEJ9iSvCMIihkn9G09T00n07m65FBhgWJeVZ4fTHvyfOTCtO1O2OVKu9/6jnSDESGy3e1
// \\nvv364OWZzyZaAOr4N2v54RVZL04SfNlTszBli0MF2/Hs19sU27Fk320+UOCxW+1TA7l6RANoj1cc\\nQLciFQ==\"}";
        return result;
    }

    /**
     * DES解密操作
     *
     * @param src      密文
     * @param password 密钥
     * @return 明文
     * @throws Exception 异常
     */
    public static String Decrypt(String src, String password) throws Exception {
        byte[] ss = Base64.decode(src, Base64.DEFAULT);

        /* DES算法要求有一个可信任的随机数源 */
        SecureRandom random = new SecureRandom();
        /* 创建一个DESKeySpec对象 */
        DESKeySpec desKey = new DESKeySpec(password.getBytes());
        /* 创建一个密匙工厂 */
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        /* 将DESKeySpec对象转换成SecretKey对象 */
        SecretKey securekey = keyFactory.generateSecret(desKey);
        /* Cipher对象实际完成解密操作 */
        Cipher cipher = Cipher.getInstance("DES");
        /* 用密匙初始化Cipher对象 */
        cipher.init(Cipher.DECRYPT_MODE, securekey, random);
        /* 真正开始解密操作 */
        byte[] dec = cipher.doFinal(ss);
        return new String(dec, "UTF-8");
    }

    /**
     * 生成文件的md5用于校验
     *
     * @param filename 文件名称
     * @return 文件md5
     */
    public static String md5sum(String filename) {
        InputStream fis;
        byte[] buffer = new byte[1024];
        int numRead = 0;
        MessageDigest md5;
        try {
            fis = new FileInputStream(filename);
            md5 = MessageDigest.getInstance("MD5");
            while ((numRead = fis.read(buffer)) > 0) {
                md5.update(buffer, 0, numRead);
            }
            fis.close();
            return toHexString(md5.digest());
        } catch (Exception e) {
            System.out.println("error");
            return "error";//这里不返回null，防止出现空指针
        }
    }
    /**
     * 生成文件的md5用于校验
     *
     * @param filename 文件名称
     * @return 文件md5
     */
    public static String fileToMd5(String filename) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(filename);
            byte[] buffer = new byte[1024];
            MessageDigest digest = MessageDigest.getInstance("MD5");
            int numRead = 0;
            while (numRead != -1) {
                numRead = inputStream.read(buffer);
                if (numRead > 0) {
                    digest.update(buffer, 0, numRead);
                }
            }
            byte[] md5Bytes = digest.digest();
            return convertHashToString(md5Bytes);
        } catch (Exception ignored) {
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    OtaLog.LOGOta(" md5 geterror", "file to md5 failed");
                }
            }
        }
    }
    private static String convertHashToString(byte[] md5Bytes) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++) {
            buf.append(Integer.toString((md5Bytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        return buf.toString().toUpperCase();
    }
    /**
     * 十六进制转换
     *
     * @param b 数字流
     * @return 转换成的16进制
     */
    public static String toHexString(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            sb.append(HEX_DIGITS[(b[i] & 0xf0) >>> 4]);
            sb.append(HEX_DIGITS[b[i] & 0x0f]);
        }
        return sb.toString();
    }

    private static final char HEX_DIGITS[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
            'E', 'F'};
}

