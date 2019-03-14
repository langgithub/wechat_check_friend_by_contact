package com.lang.wechat_check_friend_by_contact;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //linux 执行脚步
    private Command command = null;
    //微信工具
    private WeChatUtils weChatUtils = null;
    //接口访问
    private WebHttp webHttp = null;
    //我也不知道
    private TelephonyManager phone = null;
    private String ip="http://47.96.170.1:55555";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String mCurrApkPath = "/data/data/" + getPackageName() + "/";
        this.weChatUtils = new WeChatUtils(mCurrApkPath);
        this.command = new Command();
        this.webHttp = new WebHttp();
        this.phone = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
    }

    /**
     * 读取微信数据库
     *
     * @param v
     */
    public void readWeChatDB(View v) {
        weChatUtils.checkFriends(getApplicationContext(), phone);
    }

    /**
     * 删除通讯录
     *
     * @param v
     */
    public void deleteContact(View v) {
        //删除联系人
        ContentProviderHelper.deleteContact(MainActivity.this);
    }

    /**
     * 拿去一个手机号码
     *
     * @param v
     */
    public void addContact(View v) {
        Toast toast=Toast.makeText(getApplicationContext(), "addContact", Toast.LENGTH_SHORT);
        toast.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String urlGetPhone = ip+"/get_one";
                String buffer = webHttp.getContent(urlGetPhone);
                if (buffer != null) {
                    //获取到联系人，将其添加到通讯录
                    ArrayList<ContentProviderHelper.ContactMan> contactMen = new ArrayList<>();
                    JSONArray jsonArray = null;
                    try {
                        jsonArray = new JSONArray(buffer);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            String phone = jsonArray.getString(i);
                            ContentProviderHelper.ContactMan cc = new ContentProviderHelper().new ContactMan();
                            cc.setName("p" + phone);
                            cc.setNumbers(phone);
                            contactMen.add(cc);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    ContentProviderHelper.insertContactMen(MainActivity.this, contactMen);
                }
            }
        }).start();

    }

    /**
     * 点击微信
     *
     * @param v
     */
    public void openMobileFriendUI(View v) {
        command.execRootCmd("am start com.tencent.mm/com.tencent.mm.plugin.account.bind.ui.MobileFriendUI");
    }

    /**
     * 添加联系人
     *
     * @param v
     */
    public void doTask(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    //删除手机联系人
                    ContentProviderHelper.deleteContact(MainActivity.this);

                    //删除复制的微信数据库
                    command.execRootCmd("rm -rf /data/data/com.lang.wechat_check_friend_by_contact/wx_data.db");

                    boolean refreshCount=false;

                    boolean running = true;
                    while (running) {
                        //访问接口获取联系人
                        String urlGetPhone = ip+"/get_phone";
                        String buffer = webHttp.getContent(urlGetPhone);
//                        String buffer ='["15922605010", "13193118499", "13553428187", "15917528651", "13802950250", "13911115083", "18688386944", "13643070615", "15027713709", "15699082020"]';
                        if (buffer != null) {
                            //获取到联系人，将其添加到通讯录
                            ArrayList<ContentProviderHelper.ContactMan> contactMen = new ArrayList<>();
                            JSONArray jsonArray = new JSONArray(buffer);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                String phone = jsonArray.getString(i);
                                ContentProviderHelper.ContactMan cc = new ContentProviderHelper().new ContactMan();
                                cc.setName("p" + phone);
                                cc.setNumbers(phone);
                                contactMen.add(cc);
                            }

                            boolean flag = ContentProviderHelper.insertContactMen(MainActivity.this, contactMen);
                            if (flag) {
                                // 打开微信
                                command.execRootCmd("am start com.tencent.mm/com.tencent.mm.plugin.account.bind.ui.MobileFriendUI");
                                Log.i("wechat_check_friend", "目前是4s一个手机号，当前休息40秒");
                                Thread.sleep(40000);

                                String data=weChatUtils.checkFriends(getApplicationContext(), phone);
                                Log.i("wechat_check_friend", "sleep2000 防止微信网络请求异常导致微信好友添加没刷新完");
                                Thread.sleep(2000);
                                if (!"".equals(data)){
                                    String saveUrl = ip+"/save_data?data="+data;
                                    String result = webHttp.getContent(saveUrl);
                                    if (result==null){
                                        running = false;
                                        break;
                                    }
                                    Log.i("wechat_check_friend",result);
                                }

                                if (refreshCount){
                                    //模拟返回键
                                    //adb shell input keyevent 4
                                    command.execRootCmd("input keyevent 4");
                                }

                                ContentProviderHelper.deleteContact(MainActivity.this);
                                data=weChatUtils.checkFriends(getApplicationContext(), phone);
                                Log.i("wechat_check_friend", "deleteContactsleep2000,激活触发微信添加更多activity");
                                Thread.sleep(2000);
                                if (!"".equals(data)){
                                    String saveUrl = ip+"/save_data?data="+data;
                                    String result = webHttp.getContent(saveUrl);
                                    if (result==null){
                                        running = false;
                                        break;
                                    }
                                    Log.i("wechat_check_friend",result);
                                }

                                refreshCount=true;
                            } else {
                                Log.i("wechat_check_friend", "添加联系人失败，退出！！！");
                                running = false;
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
