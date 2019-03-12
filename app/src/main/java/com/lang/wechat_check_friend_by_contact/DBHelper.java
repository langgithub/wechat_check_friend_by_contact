package com.lang.wechat_check_friend_by_contact;

import android.content.Context;
import android.util.Log;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteDatabaseHook;

import java.io.File;

/**
 * Created by yuanlang on 2019/3/12.
 */

public class DBHelper {

    /**
     * 连接数据库
     *
     * @param dbFile
     */
    public void openWxDb(File dbFile,String mDbPassword,Context context) {
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
            int count=0;
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


                Log.i("wechat_check_friend",count +" --------"+id+" >>>  "+md5+" >>> "+peopleid+" >>> "+uploadtime+" >>> "
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
