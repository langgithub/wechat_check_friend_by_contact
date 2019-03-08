package com.lang.wechat_check_friend_by_contact;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteDatabaseHook;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    public static final String WX_ROOT_PATH = "/data/data/com.tencent.mm/";

    // U ID 文件路径
    private static final String WX_SP_UIN_PATH = WX_ROOT_PATH + "shared_prefs/auth_info_key_prefs.xml";


    private String mDbPassword;


    private static final String WX_DB_DIR_PATH = WX_ROOT_PATH + "MicroMsg";
    private List<File> mWxDbPathList = new ArrayList<>();
    private static final String WX_DB_FILE_NAME = "EnMicroMsg.db";


    private String mCurrApkPath = "";
    private static final String COPY_WX_DATA_DB = "wx_data.db";


    // 提交参数
    private int count = 0;

    private String IMEI;

    private String Uin;

    private Thread type;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCurrApkPath="/data/data/" + getPackageName() + "/";

    }

    /**
     * 执行linux指令
     *
     * @param paramString
     */
    public void execRootCmd(String paramString) {
        try {
            Process localProcess = Runtime.getRuntime().exec("su");
            Object localObject = localProcess.getOutputStream();
            DataOutputStream localDataOutputStream = new DataOutputStream((OutputStream) localObject);
            String str = String.valueOf(paramString);
            localObject = str + "\n";
            localDataOutputStream.writeBytes((String) localObject);
            localDataOutputStream.flush();
            localDataOutputStream.writeBytes("exit\n");
            localDataOutputStream.flush();
            localProcess.waitFor();
            localObject = localProcess.exitValue();
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    public void deleteContact(){

        // 获取root权限
        execRootCmd("chmod -R 777 " + WX_ROOT_PATH);
        execRootCmd("chmod  777 /data/data/com.tencent.mm/shared_prefs/auth_info_key_prefs.xml");

        // 获取微信的U id
        initCurrWxUin();


        // 获取 IMEI 唯一识别码
        TelephonyManager phone = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        IMEI = phone.getDeviceId();

//        System.out.println("IMEI"+IMEI);

        // 根据imei和uin生成的md5码，获取数据库的密码（去前七位的小写字母）
        initDbPassword(IMEI, Uin);

//        System.out.println(mDbPassword + "数据库的密码");

//        System.out.println("开始统计好友数量");


        //  递归查询微信本地数据库文件
        File wxDataDir = new File(WX_DB_DIR_PATH);
        mWxDbPathList.clear();
        searchFile(wxDataDir, WX_DB_FILE_NAME);

//        System.out.println("查询数据库文件");
        //处理多账号登陆情况
        for (int i = 0; i < mWxDbPathList.size(); i++) {
            File file = mWxDbPathList.get(i);
            String copyFilePath = mCurrApkPath + COPY_WX_DATA_DB;
            //将微信数据库拷贝出来，因为直接连接微信的db，会导致微信崩溃
            copyFile(file.getAbsolutePath(), copyFilePath);
            File copyWxDataDb = new File(copyFilePath);
            System.out.println("delete 微信db path>>>>>>>>"+copyFilePath);
            deleteConcate(copyWxDataDb);
        }
    }

    public void checkFriends(){


        // 获取root权限
        execRootCmd("chmod -R 777 " + WX_ROOT_PATH);
        execRootCmd("chmod  777 /data/data/com.tencent.mm/shared_prefs/auth_info_key_prefs.xml");


        // 获取微信的U id
        initCurrWxUin();


        // 获取 IMEI 唯一识别码
        TelephonyManager phone = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        IMEI = phone.getDeviceId();

//        System.out.println("IMEI"+IMEI);

        // 根据imei和uin生成的md5码，获取数据库的密码（去前七位的小写字母）
        initDbPassword(IMEI, Uin);

//        System.out.println(mDbPassword + "数据库的密码");

//        System.out.println("开始统计好友数量");


        //  递归查询微信本地数据库文件
        File wxDataDir = new File(WX_DB_DIR_PATH);
        mWxDbPathList.clear();
        searchFile(wxDataDir, WX_DB_FILE_NAME);

//        System.out.println("查询数据库文件");
        //处理多账号登陆情况
        for (int i = 0; i < mWxDbPathList.size(); i++) {
            File file = mWxDbPathList.get(i);
            String copyFilePath = mCurrApkPath + COPY_WX_DATA_DB;
            //将微信数据库拷贝出来，因为直接连接微信的db，会导致微信崩溃
            copyFile(file.getAbsolutePath(), copyFilePath);
            File copyWxDataDb = new File(copyFilePath);
            System.out.println("copy path>>>>>>>>"+copyFilePath);
            openWxDb(copyWxDataDb);
        }
    }

    public void delete(View v){
        //删除联系人
        ContentProviderHelper.deleteContact(MainActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * 访问接口获取数据
     * @param URL_PATH
     * @return
     */
    public static ByteArrayOutputStream getContent(String URL_PATH) {
        ByteArrayOutputStream byteArrayOutputStream=null;
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;

        try {
            //根据URL地址实例化一个URL对象，用于创建HttpURLConnection对象。
            URL url = new URL(URL_PATH);

            if (url != null) {
                //openConnection获得当前URL的连接
                httpURLConnection = (HttpURLConnection) url.openConnection();
                //设置3秒的响应超时
                httpURLConnection.setConnectTimeout(3000);
                //设置允许输入
                httpURLConnection.setDoInput(true);
                //设置为GET方式请求数据
                httpURLConnection.setRequestMethod("GET");
                //获取连接响应码，200为成功，如果为其他，均表示有问题
                int responseCode=httpURLConnection.getResponseCode();
                if(responseCode==200)
                {
                    //getInputStream获取服务端返回的数据流。
                    inputStream=httpURLConnection.getInputStream();
                    byteArrayOutputStream=new ByteArrayOutputStream();
                    int len=0;
                    byte[] data=new byte[1024];
                    while ((len=inputStream.read(data))!=-1){
                        byteArrayOutputStream.write(data,0,len);
                    }

                }
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArrayOutputStream;
    }

    //方法一：使用JSONObject
    private void parseJSONWithJSONObject(String jsonData) {
        try
        {
            JSONArray jsonArray = new JSONArray(jsonData);
            for (int i=0; i < jsonArray.length(); i++)    {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String phone = jsonObject.getString("phone");

            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    /**
     * 添加联系人
     * @param v
     */
    public void doTask(View v){
        new Thread(new Runnable() {
            @Override
            public void run() {


                //关闭微信
//                execRootCmd("am force-stop com.tencent.mm");
                //清赶紧数据库
//                File wxDataDir = new File(WX_DB_DIR_PATH);
//                deleteSearchFile(wxDataDir, WX_DB_FILE_NAME);
//                execRootCmd("am start com.tencent.mm/com.tencent.mm.ui.LauncherUI");
                deleteContact();
                Log.i("wechat_check_friend","删除微信数据库");

                //删除手机联系人
                ContentProviderHelper.deleteContact(MainActivity.this);
                Log.i("wechat_check_friend","删除短信联系人");


                //访问接口获取联系人
                String urlGetPhone="http://172.17.2.74:5000/get_phone";
                ByteArrayOutputStream buffer=getContent(urlGetPhone);
                if(buffer!=null){
                    //获取到联系人，将其添加到通讯录
                    ArrayList<ContentProviderHelper.ContactMan> contactMen = new ArrayList<>();
//                ContentProviderHelper.ContactMan c = new ContentProviderHelper().new ContactMan();
//                c.setName("周杰伦");
//                c.setNumbers("15775691981");
//                contactMen.add(c);
                    JSONArray jsonArray = null;
                    try {
                        jsonArray = new JSONArray(buffer.toString());
                        for (int i=0; i < jsonArray.length(); i++)    {
//                            JSONObject jsonObject = jsonArray.getJSONObject(i);
//                            String phone = jsonObject.getString("phone");
                            String phone=jsonArray.getString(i);
                            ContentProviderHelper.ContactMan cc = new ContentProviderHelper().new ContactMan();
                            cc.setName("p"+phone);
                            cc.setNumbers(phone);
                            contactMen.add(cc);

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }




                    boolean flag=ContentProviderHelper.insertContactMen(MainActivity.this, contactMen);
                    if (flag){
                        Log.i("wechat_check_friend","添加完成");
                        // 打开微信
                        execRootCmd("am start com.tencent.mm/com.tencent.mm.plugin.account.bind.ui.MobileFriendUI");
//                    Intent intent = new Intent();
//                    ComponentName cmp = new ComponentName("com.tencent.mm", "com.tencent.mm.plugin.account.bind.ui.MobileFriendUI");
//                    intent.setAction(Intent.ACTION_MAIN);
//                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    intent.setComponent(cmp);
//                    startActivity(intent);

                        Log.i("wechat_check_friend","回来，检测微信数据");
                        try {

                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //检查数据更新情况
                        checkFriends();




                    }else {
                        Log.i("wechat_check_friend","添加失败");
                    }

                }

            }
        }).start();
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
    private void initDbPassword(String imei, String uin) {
        if (TextUtils.isEmpty(imei) || TextUtils.isEmpty(uin)) {
            Log.e("","初始化数据库密码失败：imei或uid为空");
            return;
        }
        String md5 = getMD5(imei + uin);
//        System.out.println(imei+uin+"初始数值");
//        System.out.println(md5+"MD5");
        String password = md5.substring(0, 7).toLowerCase();
        System.out.println("加密后>>>>> "+password);
        mDbPassword = password;
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


    /**
     * 递归查询微信本地数据库文件
     *
     * @param file     目录
     * @param fileName 需要查找的文件名称
     */
    private void deleteSearchFile(File file, String fileName) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File childFile : files) {
                    deleteSearchFile(childFile, fileName);
                }
            }
        } else {
            if (fileName.equals(file.getName())) {
                String commond="rm -f "+file;
                Log.i("wechat_check_friend",commond);
                execRootCmd(commond);
            }
        }
    }

    /**
     * 递归查询微信本地数据库文件
     *
     * @param file     目录
     * @param fileName 需要查找的文件名称
     */
    private void searchFile(File file, String fileName) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File childFile : files) {
                    searchFile(childFile, fileName);
                }
            }
        } else {
            if (fileName.equals(file.getName())) {
                System.out.println("list>>>>>> "+file.getAbsoluteFile());
                mWxDbPathList.add(file);
            }
        }
    }

    /**
     * 复制单个文件
     *
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    public void copyFile(String oldPath, String newPath) {
        try {
            int byteRead = 0;
            File oldFile = new File(oldPath);
            if (oldFile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                while ((byteRead = inStream.read(buffer)) != -1) {
                    fs.write(buffer, 0, byteRead);
                }
                inStream.close();
            }
        } catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();

        }
    }
    /**
     * 连接数据库
     *
     * @param dbFile
     */
    private void deleteConcate(File dbFile) {
        Context context = getApplicationContext();
        SQLiteDatabase.loadLibs(context);
        SQLiteDatabaseHook hook = new SQLiteDatabaseHook() {
            public void preKey(SQLiteDatabase database) {
            }

            public void postKey(SQLiteDatabase database) {
                database.rawExecSQL("PRAGMA cipher_migrate;"); //兼容2.0的数据库
            }
        };

        SQLiteDatabase db=null;
        Cursor c1=null;

        try {
            //打开数据库连接
            db= SQLiteDatabase.openOrCreateDatabase(dbFile, mDbPassword, null, hook);
            db.execSQL("delete from addr_upload2");
            c1= db.rawQuery("select * from addr_upload2; ",null);
            if(!c1.moveToNext()){
                Log.i("wechat_check_friend","微信数据已清空");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (c1!=null){
                c1.close();
            }
            if (db!=null){
                db.close();
            }
        }
    }
    /**
     * 连接数据库
     *
     * @param dbFile
     */
    private void openWxDb(File dbFile) {
        Context context = getApplicationContext();
        SQLiteDatabase.loadLibs(context);
        SQLiteDatabaseHook hook = new SQLiteDatabaseHook() {
            public void preKey(SQLiteDatabase database) {
            }

            public void postKey(SQLiteDatabase database) {
                database.rawExecSQL("PRAGMA cipher_migrate;"); //兼容2.0的数据库
            }
        };

        SQLiteDatabase db=null;
        Cursor c1=null;

        try {
            //打开数据库连接
            db= SQLiteDatabase.openOrCreateDatabase(dbFile, mDbPassword, null, hook);

            //查询所有联系人（verifyFlag!=0:公众号等类型，群里面非好友的类型为4，未知类型2）
//             Cursor c1 = db.rawQuery("select * from rcontact where verifyFlag = 0  and type != 4 and type != 2 and type !=33 limit 20, 9999", null);
//            Cursor c1 = db.rawQuery("select * from rcontact where username not like 'gh_%' and verifyFlag<>24 and verifyFlag<>29 and verifyFlag<>56 and type<>33 and type<>70 and verifyFlag=0 and type<>4 and type<>0 and showHead<>43 and type<>65536",null);

//            Cursor c1 = db.rawQuery("select * from sqlite_master; ",null);
//            while (c1.moveToNext()) {
//
//                String sql = c1.getString(c1.getColumnIndex("sql"));
//                String type = c1.getString(c1.getColumnIndex("type"));
//                String name = c1.getString(c1.getColumnIndex("name"));
//                String tbl_name = c1.getString(c1.getColumnIndex("tbl_name"));
//                String rootpage = c1.getString(c1.getColumnIndex("rootpage"));
//                System.out.println(sql+";");
//
//            }




            c1= db.rawQuery("select * from addr_upload2; ",null);
            while (c1.moveToNext()) {

                String id = c1.getString(c1.getColumnIndex("id"));
                String md5 = c1.getString(c1.getColumnIndex("md5"));
                String peopleid = c1.getString(c1.getColumnIndex("peopleid"));
                String uploadtime = c1.getString(c1.getColumnIndex("uploadtime"));
                String realname = c1.getString(c1.getColumnIndex("realname"));
                String realnamepyinitial = c1.getString(c1.getColumnIndex("realnamepyinitial"));
                String realnamequanpin = c1.getString(c1.getColumnIndex("realnamequanpin"));
                String username = c1.getString(c1.getColumnIndex("username"));
                String nickname = c1.getString(c1.getColumnIndex("nickname"));
                String nicknamepyinitial = c1.getString(c1.getColumnIndex("nicknamepyinitial"));
                String nicknamequanpin = c1.getString(c1.getColumnIndex("nicknamequanpin"));
                String type = c1.getString(c1.getColumnIndex("type"));
                String moblie = c1.getString(c1.getColumnIndex("moblie"));
                String email = c1.getString(c1.getColumnIndex("email"));
                String status = c1.getString(c1.getColumnIndex("status"));
                String reserved1 = c1.getString(c1.getColumnIndex("reserved1"));
                String reserved2 = c1.getString(c1.getColumnIndex("reserved2"));
                String reserved3 = c1.getString(c1.getColumnIndex("reserved3"));
                String reserved4 = c1.getString(c1.getColumnIndex("reserved4"));
                String lvbuf = "blob";
                String showhead = c1.getString(c1.getColumnIndex("showhead"));


                System.out.println(count +" --------"+id+" >>>  "+md5+" >>> "+peopleid+" >>> "+uploadtime+" >>> "
                        +realname+" >>> "+realnamepyinitial+" >>> "+realnamequanpin+" >>> "+username+" >>> "+nickname
                        +" >>> "+nicknamepyinitial+" >>> "+nicknamequanpin+" >>> "+type+" >>> "+moblie+" >>> "+email
                        +" >>> "+status+" >>> "+reserved1+" >>> "+reserved2+" >>> "+reserved3
                        +" >>> "+reserved4+" >>> "+lvbuf+" >>> "+showhead);
                count++;

            }
            //username moblie
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (c1!=null){
                c1.close();
            }
            if (db!=null){
                db.close();
            }
        }
    }
}
