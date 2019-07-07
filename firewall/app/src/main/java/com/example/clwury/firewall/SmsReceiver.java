package com.example.clwury.firewall;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SmsReceiver extends BroadcastReceiver {
    SQLiteDatabase db;
    String body;
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Sql sql=new Sql(context);
        //Toast.makeText(context,"啊啊啊啊啊啊啊啊啊啊啊啊啊",Toast.LENGTH_SHORT).show();
        //短信的信息封装在Intent中
        Bundle bundle = intent.getExtras();
        Object[] objects = (Object[]) bundle.get("pdus");
        //if(objects.length>1){
            //Toast.makeText(context,"啊啊啊啊啊啊啊啊啊啊啊啊啊",Toast.LENGTH_SHORT).show();
        //}
        SmsMessage sms;
        SmsMessage[] messages = new SmsMessage[objects.length];
        for(int m=0;m<objects.length;m++){
            messages[m] = SmsMessage.createFromPdu((byte[]) objects[m]);
        }
        body="  ";
        for(int n=0;n<objects.length;n++){
            body=body+ messages[n].getMessageBody();
        }
        Date date=null;
        SimpleDateFormat format=null;
        String time=null;
        String address=null;
        for (Object obj : objects) {
            sms = SmsMessage.createFromPdu((byte[])obj);
            //获取信息
            //String body1 = sms.getMessageBody();
            //获取发件人
            address = sms.getOriginatingAddress();
            //获取系统当前时间
            date = new Date(System.currentTimeMillis());
            format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            time = format.format(date);
        }
            //查询白名单
            int flite=1;
            db=sql.getReadableDatabase();
            Cursor cursor2=db.query("whitelist",null,null,null,null,null,null);
            if(cursor2.getCount()!=0){
                cursor2.moveToFirst();
                if(address.equals(cursor2.getString(1))){
                    flite=0;
                }
            }
            while (cursor2.moveToNext()){
                if(address.equals(cursor2.getString(1))){
                    flite=0;
                    break;
                }
            }
            cursor2.close();
            db.close();
            int fliter = 0;
            if(flite==1) {
                 //查询过滤内容
                 db = sql.getReadableDatabase();
                 Cursor cursor1 = db.query("smsfilter", null, null, null, null, null, null);
                 if (cursor1.getCount() != 0) {
                     cursor1.moveToFirst();
                     if (body.indexOf(cursor1.getString(1)) != -1) {
                         fliter = 1;
                     }
                 }
                 while (cursor1.moveToNext()) {
                     if (body.indexOf(cursor1.getString(1)) != -1) {
                         fliter = 1;
                         break;
                     }
                 }
                 cursor1.close();
                 db.close();
            }
            //查询黑名单
            int blackflag=0;
            db=sql.getReadableDatabase();
            Cursor cursor=db.query("blacklist",null,null,null,null,null,null);
            if(cursor.getCount()!=0){
                cursor.moveToFirst();
                if(address.equals(cursor.getString(1))){
                    blackflag=1;
                }
            }
            while (cursor.moveToNext()){
                if(address.equals(cursor.getString(1))){
                    blackflag=1;
                    break;
                }
            }
            cursor.close();
            db.close();
            //广播拦截
            if(blackflag==1||fliter==1){
                //添加到垃圾箱数据库
                db=sql.getWritableDatabase();
                ContentValues values=new ContentValues();
                values.put("number",address);
                values.put("time",time);
                values.put("context",body);
                db.insert("smsbox",null,values);
                Toast.makeText(context,"已将短信收至垃圾箱！！！",Toast.LENGTH_SHORT).show();
                db.close();
                abortBroadcast();
                //Toast.makeText(context,body,Toast.LENGTH_SHORT).show();
                //Toast.makeText(context,"啊啊啊啊啊啊啊啊啊啊啊啊啊",Toast.LENGTH_SHORT).show();
            }

    }
}
