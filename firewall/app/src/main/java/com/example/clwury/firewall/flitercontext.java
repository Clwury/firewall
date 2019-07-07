package com.example.clwury.firewall;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class flitercontext extends AppCompatActivity implements AdapterView.OnItemLongClickListener{
    //手指按下的点为(x1, y1)手指离开屏幕的点为(x2, y2)
    float x1 = 0;
    float x2 = 0;
    float y1 = 0;
    float y2 = 0;

    ListView fliter;
    Sql sql=new Sql(this);
    SQLiteDatabase db;
    private ArrayList<String> mData;
    private ArrayAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_black);
        fliter=(ListView) findViewById(R.id.lv);
        mData = new ArrayList<String>();
        //查询过滤内容
        db=sql.getReadableDatabase();
        Cursor cursor=db.query("smsfilter",null,null,null,null,null,null);
        //int i=cursor.getCount();
        if(cursor.getCount()==0) {
            Toast.makeText(this, "无过滤内容", Toast.LENGTH_SHORT).show();
        }else {
            cursor.moveToFirst();
            mData.add(cursor.getString(1));
        }
        while (cursor.moveToNext()){
            mData.add(cursor.getString(1));
        }
        cursor.close();
        db.close();
        mAdapter = new ArrayAdapter<>(this, R.layout.list_item,R.id.item_number,mData);
        //设置Adapter
        fliter.setAdapter(mAdapter);
        fliter.setOnItemLongClickListener(this);
    }
    //长按删除
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

        new AlertDialog.Builder(this).setTitle("确定删除该条屏蔽内容？")
                .setIcon(android.R.drawable.sym_def_app_icon)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //按下确定键后的事件
                        String context=mData.get(position);
                        //从数据库删除
                        db=sql.getWritableDatabase();
                        db.execSQL("delete from smsfilter where filtercontext=?",new Object[]{context});
                        db.close();
                        mData.remove(position);
                        mAdapter.notifyDataSetChanged();
                        Toast.makeText(getApplicationContext(),"已删除该条屏蔽内容",Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton("取消",null).show();
        return true;
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //继承了Activity的onTouchEvent方法，直接监听点击事件
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            //当手指按下的时候
            x1 = event.getX();
            y1 = event.getY();
        }
        if(event.getAction() == MotionEvent.ACTION_UP) {
            //当手指离开的时候
            x2 = event.getX();
            y2 = event.getY();
            if(y1 - y2 > 50) {
                //Toast.makeText(this, "向上滑", Toast.LENGTH_SHORT).show();
            } else if(y2 - y1 > 50) {
                //Toast.makeText(this, "向下滑", Toast.LENGTH_SHORT).show();
            } else if(x1 - x2 > 50) {
                //Toast.makeText(this, "向左滑", Toast.LENGTH_SHORT).show();
            } else if(x2 - x1 > 50) {
                //显示垃圾箱
                Intent intent=new Intent();
                intent.setClass(flitercontext.this, Smsbox.class);
                startActivity(intent);
                //设置动画
                overridePendingTransition(R.anim.in_from_left,R.anim.out_to_right);
                //Toast.makeText(this, "向右滑", Toast.LENGTH_SHORT).show();
            }
        }
        return super.onTouchEvent(event);
    }

}
