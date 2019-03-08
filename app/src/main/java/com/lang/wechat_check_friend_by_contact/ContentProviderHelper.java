package com.lang.wechat_check_friend_by_contact;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量添加联系人
 */

public class ContentProviderHelper {

    class ContactMan {
        private String name;
        //支持多个电话号码，用","隔开
        private String numbers;

        public String getNumbers() {
            return numbers;
        }

        public void setNumbers(String numbers) {
            this.numbers = numbers;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }


    public static boolean insertContactMen(Context context, List<ContactMan> contactManList) {
        boolean flag=false;
        if (contactManList == null) {
            return flag;
        }
        try {
            ArrayList<ContentProviderOperation> ops = new ArrayList<>();
            int rawContactInsertIndex = 0;

            for (ContactMan contactMan : contactManList) {
                //添加姓名
                String name = contactMan.getName();
                String numbers = contactMan.getNumbers();
                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(numbers)) {
                    continue;
                }
                rawContactInsertIndex = ops.size();
                //必不可少
                ops.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI).withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null).withValue(RawContacts
                        .ACCOUNT_NAME, null)
                        .withYieldAllowed(true).build());
                //添加名字
                ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI).withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex).withValue(Data.MIMETYPE,
                        StructuredName.CONTENT_ITEM_TYPE).withValue(StructuredName.DISPLAY_NAME, name).withYieldAllowed(true).build());
                //添加号码,支持多个号码
                String[] numberArr = numbers.split(",");
                for (String number : numberArr) {
                    ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI).withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex).withValue(Data.MIMETYPE, Phone
                            .CONTENT_ITEM_TYPE).withValue(Phone.NUMBER, number).withValue(Phone.TYPE, Phone.TYPE_MOBILE).withValue(Phone.LABEL, "").withYieldAllowed(true).build());
                }
            }
            //开始导入
            if (ops.size() != 0) {
                context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                flag=true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    public static void deleteContact(Context context){
        ContentResolver cr = context.getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,null, null, null, null);
        while (cur.moveToNext()) {
            try{
                String lookupKey = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
                System.out.println("The uri is " + uri.toString());
                cr.delete(uri, null, null);
            }
            catch(Exception e)
            {
                System.out.println(e.getStackTrace());
            }
        }
    }





}


