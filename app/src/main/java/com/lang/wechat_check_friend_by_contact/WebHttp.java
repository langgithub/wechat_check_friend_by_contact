package com.lang.wechat_check_friend_by_contact;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by yuanlang on 2019/3/12.
 */

public class WebHttp {

    /**
     * 访问接口获取数据
     * @param URL_PATH
     * @return
     */
    public static String getContent(String URL_PATH) {
        Log.i("wechat_check_friend",URL_PATH);
        ByteArrayOutputStream byteArrayOutputStream=null;
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        String content=null;

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
            content=byteArrayOutputStream.toString();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

}
