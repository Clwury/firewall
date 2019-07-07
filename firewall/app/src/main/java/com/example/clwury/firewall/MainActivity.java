package com.example.clwury.firewall;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btn1;
    private Button btn2;
    private Button btn3;
    private SmsReceiver smsReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_PHONE_STATE)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_PHONE_STATE,Manifest.permission.PROCESS_OUTGOING_CALLS,Manifest.permission.CALL_PHONE,Manifest.permission.RECEIVE_SMS,Manifest.permission.READ_SMS},1);
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        smsReceiver = new SmsReceiver();
        registerReceiver(smsReceiver,filter);


        //初始化数据库
        Sql sql=new Sql(this);
        btn1=(Button) findViewById(R.id.black);
        btn2=(Button) findViewById(R.id.message);
        btn3=(Button) findViewById(R.id.phone);
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btn3.setOnClickListener(this);
    }
    public void onRequestPermissionsResult(int requestCode,String[]permissions,int[] grantResults){
        switch (requestCode){
            case 1:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"成功申请到权限",Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(this,"You denied the permission",Toast.LENGTH_LONG).show();
                }
                break;
            default:
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(smsReceiver);
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.black:
                Intent intent1=new Intent(this,blackActivity.class);
                startActivity(intent1);
                break;
            case R.id.message:
                Intent intent2=new Intent(this,Smsbox.class);
                startActivity(intent2);
                break;
            case R.id.phone:
                Intent intent3=new Intent(this,Phoneinter.class);
                startActivity(intent3);
                break;
        }
    }
}
class Sql extends SQLiteOpenHelper {
    public Sql(Context context) {
        super(context, "database.db", null, 2);
    }
    public void onCreate(SQLiteDatabase db){
        db.execSQL("create table blacklist(_id integer primary key autoincrement,number varchar(20))");
        db.execSQL("create table smsbox(_id integer primary key autoincrement,number varchar(20),time varchar(20),context text)");
        db.execSQL("create table Phone(_id integer primary key autoincrement,number varchar(20),time varchar(20))");
        db.execSQL("create table whitelist(_id integer primary key autoincrement,number varchar(20))");
        db.execSQL("create table smsfilter(_id integer primary key autoincrement,filtercontext text)");
    }
    public void onUpgrade(SQLiteDatabase db,int oldVersion, int newVersion){

    }
}
