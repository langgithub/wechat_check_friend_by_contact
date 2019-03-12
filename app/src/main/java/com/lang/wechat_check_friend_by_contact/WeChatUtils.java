package com.lang.wechat_check_friend_by_contact;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Created by yuanlang on 2019/3/12.
 */

public class WeChatUtils {

    public static final String WX_ROOT_PATH = "/data/data/com.tencent.mm/";

    // U ID 文件路径
    private static final String WX_SP_UIN_PATH = WX_ROOT_PATH + "shared_prefs/auth_info_key_prefs.xml";

    private static final String WX_DB_DIR_PATH = WX_ROOT_PATH + "MicroMsg";
    private static final String WX_DB_FILE_NAME = "EnMicroMsg.db";


    private static final String COPY_WX_DATA_DB = "wx_data.db";
    private String mCurrApkPath="";

    // 提交参数
    private String IMEI;

    private String Uin;

    private Command c=new Command();
    private FileUtils f=new FileUtils();
    private DBHelper dbHelper=new DBHelper();

    public WeChatUtils(String mCurrApkPath){
        this.mCurrApkPath=mCurrApkPath;
    }

    /**
     * 获取微信的uid
     * 微信的uid存储在SharedPreferences里面
     * 存储位置\data\data\com.tencent.mm\shared_prefs\auth_info_key_prefs.xml
     */
    private void initCurrWxUin() {
        Uin = null;
        File file = new File(WX_SP_UIN_PATH);
        try {
            FileInputStream in = new FileInputStream(file);
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(in);
            Element root = document.getRootElement();
            List<Element> elements = root.elements();
            for (Element element : elements) {
                if ("_auth_uin".equals(element.attributeValue("name"))) {
                    Uin = element.attributeValue("value");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("wechat_check_friend","获取微信uid失败，请检查auth_info_key_prefs文件权限");
        }
    }
    /**
     * 根据imei和uin生成的md5码，获取数据库的密码（去前七位的小写字母）
     *
     * @param imei
     * @param uin
     * @return
     */
    private String initDbPassword(String imei, String uin) {
        if (TextUtils.isEmpty(imei) || TextUtils.isEmpty(uin)) {
            Log.e("","初始化数据库密码失败：imei或uid为空");
            return "";
        }
        String md5 = getMD5(imei + uin);
        String password = md5.substring(0, 7).toLowerCase();
        System.out.println("加密后>>>>> "+password);
        return password;
    }

    public String getMD5(String info)
    {
        try
        {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(info.getBytes("UTF-8"));
            byte[] encryption = md5.digest();

            StringBuffer strBuf = new StringBuffer();
            for (int i = 0; i < encryption.length; i++)
            {
                if (Integer.toHexString(0xff & encryption[i]).length() == 1)
                {
                    strBuf.append("0").append(Integer.toHexString(0xff & encryption[i]));
                }
                else
                {
                    strBuf.append(Integer.toHexString(0xff & encryption[i]));
                }
            }

            return strBuf.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
            return "";
        }
        catch (UnsupportedEncodingException e)
        {
            return "";
        }
    }

    /**
     * md5加密
     *
     * @param content
     * @return
     */
    private String md5(String content) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            md5.update(content.getBytes("UTF-8"));
            byte[] encryption = md5.digest();//加密
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < encryption.length; i++) {
                if (Integer.toHexString(0xff & encryption[i]).length() == 1) {
                    sb.append("0").append(Integer.toHexString(0xff & encryption[i]));
                } else {
                    sb.append(Integer.toHexString(0xff & encryption[i]));
                }
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public String checkFriends(Context context,TelephonyManager phone){

        Log.i("wechat_check_friend", "读取微信数据库");

        // 获取root权限
        c.execRootCmd("chmod -R 777 " + WX_ROOT_PATH);
        c.execRootCmd("chmod  777 /data/data/com.tencent.mm/shared_prefs/auth_info_key_prefs.xml");

        // 获取微信的U id
        initCurrWxUin();

        // 获取 IMEI 唯一识别码
//        TelephonyManager phone = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return "";
        }
        IMEI = phone.getDeviceId();

        String password=initDbPassword(IMEI, Uin);
        if (!"".equals(password)){
            //  递归查询微信本地数据库文件
            File wxDataDir = new File(WX_DB_DIR_PATH);
            FileUtils.mWxDbPathList.clear();
            f.searchFile(wxDataDir, WX_DB_FILE_NAME);

            //处理多账号登陆情况
            for (int i = 0; i < FileUtils.mWxDbPathList.size(); i++) {
                File file = FileUtils.mWxDbPathList.get(i);
                String copyFilePath = mCurrApkPath + COPY_WX_DATA_DB;
                //将微信数据库拷贝出来，因为直接连接微信的db，会导致微信崩溃
                f.copyFile(file.getAbsolutePath(), copyFilePath);
                File copyWxDataDb = new File(copyFilePath);
                System.out.println("copy path>>>>>>>>"+copyFilePath);
                //直接返回，没有多账号登陆
                return dbHelper.openWxDb(copyWxDataDb,password,context);
            }
        }
        return "";
    }

}
