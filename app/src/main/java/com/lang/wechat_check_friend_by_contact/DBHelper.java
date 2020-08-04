package com.lang.wechat_check_friend_by_contact;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteDatabaseHook;

import java.io.File;
import java.security.MessageDigest;
import java.sql.Blob;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yuanlang on 2019/3/12.
 */

public class DBHelper {

    String bLs = "";
    public int drx = -1;
    public String exO = "";
    public int huC = 0;
    public String huz = "";
    public String iJI = "";
    long iJJ = 0;
    public String iJK = "";
    public String iJL = "";
    String iJM = "";
    String iJN = "";
    public String iJO = "";
    public int iJP = 0;
    public byte[] iJQ;
    public int iJR;
    public String iJS = "";
    public int iJT = 0;
    public String iJU = "";
    public String iJV = "";
    public String iJW = "";
    int iJX = 0;
    public String iJY = "";
    int iJZ = 0;
    int iKa = 0;
    String iKb = "";
    public String iKc = "";
    String iKd = "";
    int iKe = -1;
    String iKf = "";
    long iKg = -1;
    int iKh = -1;
    String iKi = "";
    String iKj = "";
    String iKk = "";
    public long iKl = 0;

    /**
     * 连接数据库
     *
     * @param dbFile
     */
    public String openWxDb(File dbFile, String mDbPassword, Context context) {
        String data="";
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
            data+="{\"result\":[";
            //打开数据库连接
            db= SQLiteDatabase.openOrCreateDatabase(dbFile, mDbPassword, null, hook);

            //查询所有联系人（verifyFlag!=0:公众号等类型，群里面非好友的类型为4，未知类型2）
//             Cursor c1 = db.rawQuery("select * from rcontact where verifyFlag = 0  and type != 4 and type != 2 and type !=33 limit 20, 9999", null);
//            Cursor c1 = db.rawQuery("select * from rcontact where username not like 'gh_%' and verifyFlag<>24 and verifyFlag<>29 and verifyFlag<>56 and type<>33 and type<>70 and verifyFlag=0 and type<>4 and type<>0 and showHead<>43 and type<>65536",null);

//            c1 = db.rawQuery("select * from sqlite_master; ",null);
            Map map =new HashMap<>();
            c1 = db.rawQuery("select * from img_flag; ",null);
            while (c1.moveToNext()) {
                String name = c1.getString(0);
                String img = c1.getString(3);
//                System.out.println(name+";"+img);
                map.put(name,img);
            }


            c1= db.rawQuery("select * from addr_upload2; ",null);
            int count=0;
            while (c1.moveToNext()) {

                String id = c1.getString(0);
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
                byte[] lvbuf = c1.getBlob(19);
                C8940z zVar = new C8940z();
                int c = zVar.mo14099cr(lvbuf);
                this.iJS = zVar.getString();
                // 性别
                this.iJT = zVar.getInt();
                this.iJU = zVar.getString();
                this.iJV = zVar.getString();
                this.iJW = zVar.getString();
                this.iJX = zVar.getInt();
                this.iJY = zVar.getString();
                this.iJZ = zVar.getInt();
                this.iKa = zVar.getInt();
                this.iKb = zVar.getString();
                this.iKc = zVar.getString();
                this.iKd = zVar.getString();
                this.iKe = zVar.getInt();
                this.iKf = zVar.getString();
                this.iKg = zVar.getLong();
                this.iKh = zVar.getInt();
                this.iKi = zVar.getString();
                this.iKj = zVar.getString();
                this.iKk = zVar.getString();
                this.iKl = zVar.getLong();


                String showhead = c1.getString(c1.getColumnIndex("showhead"));


                Log.i("wechat_check_friend",count +" --------"+id+" >>>  "+md5+" >>> "+peopleid+" >>> "+uploadtime+" >>> "
                        +realname+" >>> "+realnamepyinitial+" >>> "+realnamequanpin+" >>> "+username+" >>> "+nickname
                        +" >>> "+nicknamepyinitial+" >>> "+nicknamequanpin+" >>> "+type+" >>> "+moblie+" >>> "+email
                        +" >>> "+status+" >>> "+reserved1+" >>> "+reserved2+" >>> "+reserved3
                        +" >>> "+reserved4+" >>> "+lvbuf+" >>> "+showhead);
                count++;
                data+="{\"phone\":"+moblie+",\"wxid\":\""+username+"\"},";
                System.out.println(username);
                System.out.println(map.get(username));
//                System.out.println(WeChatUtils.prefile);
//                System.out.println(WeChatUtils.decryptionWechatUserAvatarImage(username, WeChatUtils.prefile));

            }
            System.out.println(map.toString());
            data=data.substring(0,data.length()-1);
            data+="]}";
            Log.i("wechat_check_friend",data);
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
        return data;
    }


    /**
     * 连接数据库
     *
     * @param dbFile
     */
    private void deleteConcate(Context context,String mDbPassword,File dbFile) {

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
}
