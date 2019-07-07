package com.example.clwury.firewall;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class Screencontext extends AppCompatActivity {
    TextView screen;
    Sql sql=new Sql(this);
    SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_context);
        screen=(TextView) findViewById(R.id.screen);
        Intent intent=getIntent();
        String Screen=intent.getStringExtra("Screentime");
        //查询垃圾箱
        db=sql.getReadableDatabase();
        Cursor cursor=db.query("smsbox",null,"time=?",new String[]{Screen},null,null,null);
        if(cursor.getCount()!=0) {
            cursor.moveToFirst();
            screen.setText(cursor.getString(3));
            //cursor.getString(2);
            //Toast.makeText(this, "无垃圾短信", Toast.LENGTH_SHORT).show();
        }
        cursor.close();
        db.close();
        //Toast.makeText(this,Screen,Toast.LENGTH_SHORT).show();
    }
}
