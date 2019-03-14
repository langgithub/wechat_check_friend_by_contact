# description
   因微信平台对搜索接口管理较严，出现搜索频繁只能sleep等待
   故通过手机通讯录添加好友来检查微信是否注册
   
### 思路
1. 批量添加手机联系人，删除手机联系人（ContentProviderHelper.java）
2. 使用monitor轨迹追踪找到了微信打开 com.tencent.mm.plugin.account.bind.ui.MobileFriendUI activity会加载读取手机通讯录，等待一定时间就会出现待添加联系人
3. 调研得知通讯录中微信好友会出现在微信数据库EnMicroMsg.db 表addr_upload2中，读取好友表获取微信好友信息


### 效果
![image](https://github.com/langgithub/wechat_check_friend_by_contact/blob/master/wx.gif)


### 难点 
破解微信数据库登陆密码
1. 调研得知微信数据库登陆密码由IMEI Uin组成 ， 源码initDbPassword(IMEI, Uin)获取微信登陆密码
2. IMEI 与手机硬件相关 (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE).getDeviceId();
3. Uin 来至\data\data\com.tencent.mm\shared_prefs\auth_info_key_prefs.xml

### 遗留问题
1. 无法做到实时，因为com.tencent.mm.plugin.account.bind.ui.MobileFriendUI activity启动读取通讯录本身是一个后台操作，需要有网络访问



总：无法实时返回数据,但是不封号，但是有时返回不了数据，但是比UIAutomation快。可以线下撞库和线上辅助UIAutomation



