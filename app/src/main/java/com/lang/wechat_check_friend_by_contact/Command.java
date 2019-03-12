package com.lang.wechat_check_friend_by_contact;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.OutputStream;

/**
 * Created by yuanlang on 2019/3/12.
 */

public class Command {

    /**
     * 执行linux指令
     *
     * @param paramString
     */
    public void execRootCmd(String paramString) {
        Log.i("wechat_check_friend", "execRootCmd: "+paramString);
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
}
