package com.example.clwury.firewall;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
public class PhoneReceiver extends BroadcastReceiver {
    Sql sql;
    SQLiteDatabase db;
    int flag=0;
    String time;
    String TAG = "PhoneReceiver";
    private static TelephonyManager manager;
    public void onReceive(Context context, Intent intent) {
        flag=0;
        sql=new Sql(context);

        //sharedPreferences = context.getSharedPreferences(MainActivity.ROLE_DATA, Context.MODE_PRIVATE);
        if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            // 如果是去电（拨出）
        } else {
            manager = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
            // 设置一个监听器
            manager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
        }


    }
    PhoneStateListener listener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            // state 当前状态 incomingNumber,貌似没有去电的API
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                //手机空闲了
                case TelephonyManager.CALL_STATE_IDLE:
                    break;
                //电话被挂起
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    break;
                // 当电话呼入时
                case TelephonyManager.CALL_STATE_RINGING:
                    Log.e(TAG, "来电号码是："+ incomingNumber);
                    //获取系统当前时间
                    Date date = new Date(System.currentTimeMillis());
                    SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
                    time = format.format(date);
                    db=sql.getReadableDatabase();
                    Cursor cursor=db.query("blacklist",null,null,null,null,null,null);
                    if(cursor.getCount()!=0){
                        //Toast.makeText(context,"黑名单为空呀呀呀呀呀呀",Toast.LENGTH_SHORT).show();
                        cursor.moveToFirst();
                        //mData.add(cursor.getString(1));
                        if(cursor.getString(1).equals(incomingNumber)){
                            flag=1;
                        }
                    }
                    while (cursor.moveToNext()){
                        //mData.add(cursor.getString(1));
                        if(cursor.getString(1).equals(incomingNumber)){
                            //setResultData(null);//挂断
                            flag=1;
                        }
                    }
                    cursor.close();
                    db.close();
                    if(flag==1) {
                        stopCall();
                        db=sql.getWritableDatabase();
                        ContentValues values=new ContentValues();
                        values.put("number",incomingNumber);
                        values.put("time",time);
                        db.insert("Phone",null,values);
                        db.close();
                        flag=0;
                    }

                    break;
            }
        }
    };
    public void stopCall() {

        try {
            Method getITelephonyMethod = TelephonyManager.class.getDeclaredMethod("getITelephony", (Class[]) null);
            getITelephonyMethod.setAccessible(true);
            ITelephony telephony = (ITelephony) getITelephonyMethod.invoke(manager,
                    (Object[]) null);
            // 拒接来电
            telephony.endCall();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
