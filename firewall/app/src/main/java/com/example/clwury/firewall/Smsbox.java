package com.example.clwury.firewall;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class Smsbox extends AppCompatActivity implements AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener{
    //手指按下的点为(x1, y1)手指离开屏幕的点为(x2, y2)
    float x1 = 0;
    float x2 = 0;
    float y1 = 0;
    float y2 = 0;

    private ListView smslist;
    List<Sms> list;
    MyAdapter adapter;
    Sql sql=new Sql(this);
    SQLiteDatabase db;
    private SwipeRefreshLayout swipe;
    public boolean onCreateOptionsMenu(Menu menu) {
        // 使用inflate方法来把布局文件中的定义的菜单加载给第二个参数所对应的menu对象
        getMenuInflater().inflate(R.menu.sms_menu, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action:
                final EditText et = new EditText(this);
                new AlertDialog.Builder(this).setTitle("请输入屏蔽内容")
                        .setIcon(android.R.drawable.sym_def_app_icon)
                        .setView(et)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String context=et.getText().toString().trim();
                                //按下确定键后的事件
                                if (!"".equals(context)) {
                                    db = sql.getWritableDatabase();
                                    ContentValues values = new ContentValues();
                                    values.put("filtercontext", context);
                                    db.insert("smsfilter", null, values);
                                    Toast.makeText(getApplicationContext(),"成功添加屏蔽内容", Toast.LENGTH_LONG).show();
                                }
                                //Toast.makeText(getApplicationContext(), et.getText().toString(),Toast.LENGTH_LONG).show();
                            }
                        }).setNegativeButton("取消",null).show();
                break;
            case R.id.white:
                final EditText et1 = new EditText(this);
                new AlertDialog.Builder(this).setTitle("输入短信白名单")
                        .setIcon(android.R.drawable.sym_def_app_icon)
                        .setView(et1)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String number=et1.getText().toString().trim();
                                //按下确定键后的事件
                                if (!"".equals(number)) {
                                    db = sql.getWritableDatabase();
                                    ContentValues values = new ContentValues();
                                    values.put("number", number);
                                    db.insert("whitelist", null, values);
                                    Toast.makeText(getApplicationContext(), "已将" + number + "加入短信白名单", Toast.LENGTH_LONG).show();
                                }
                            }
                        }).setNegativeButton("取消",null).show();
                break;
        }
        return true;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smsbox);
        swipe=(SwipeRefreshLayout) findViewById(R.id.swipe);
        swipe.setColorSchemeResources(R.color.colorAccent,R.color.colorPrimary,R.color.colorPrimaryDark);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        list= new ArrayList<Sms>();
                        //查询垃圾箱
                        db=sql.getReadableDatabase();
                        Cursor cursor=db.query("smsbox",null,null,null,null,null,null);
                        if(cursor.getCount()==0) {
                            Toast.makeText(getApplicationContext(), "无垃圾短信", Toast.LENGTH_SHORT).show();
                        }else{
                            cursor.moveToFirst();
                            Sms sms=new Sms(cursor.getString(1),cursor.getString(2));
                            list.add(sms);
                        }
                        while (cursor.moveToNext()){
                            Sms sms=new Sms(cursor.getString(1),cursor.getString(2));
                            list.add(sms);
                        }
                        cursor.close();
                        db.close();
                        smslist=(ListView) findViewById(R.id.lvsms);
                        adapter = new MyAdapter(Smsbox.this,0,list);
                        smslist.setAdapter(adapter);
                        swipe.setRefreshing(false);//取消刷新
                    }
                },1000);
            }
        });
        list= new ArrayList<Sms>();
        //查询垃圾箱
        db=sql.getReadableDatabase();
        Cursor cursor=db.query("smsbox",null,null,null,null,null,null);
        if(cursor.getCount()==0) {
            Toast.makeText(this, "无垃圾短信", Toast.LENGTH_SHORT).show();
        }else{
            cursor.moveToFirst();
            Sms sms=new Sms(cursor.getString(1),cursor.getString(2));
            list.add(sms);
        }
        while (cursor.moveToNext()){
            Sms sms=new Sms(cursor.getString(1),cursor.getString(2));
            list.add(sms);
        }
        cursor.close();
        db.close();
        smslist=(ListView) findViewById(R.id.lvsms);
        adapter = new MyAdapter(Smsbox.this,0,list);
        smslist.setAdapter(adapter);
        smslist.setOnItemClickListener(this);
        smslist.setOnItemLongClickListener(this);
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
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
                //显示过滤内容
                /*Intent intent=new Intent();
                intent.setClass(Smsbox.this, flitercontext.class);
                startActivity(intent);
                //设置动画
                overridePendingTransition(R.anim.in_from_up, R.anim.out_to_down);*/
                //Toast.makeText(this, "向上滑", Toast.LENGTH_SHORT).show();
            } else if(y2 - y1 > 50) {
                //显示白名单
                /*Intent intent=new Intent();
                intent.setClass(Smsbox.this, whitelist.class);
                startActivity(intent);
                //设置动画
                overridePendingTransition(R.anim.in_from_down, R.anim.out_to_up);*/
                //Toast.makeText(this, "向下滑", Toast.LENGTH_SHORT).show();
            } else if(x1 - x2 > 50) {
                //显示过滤内容
                Intent intent=new Intent();
                intent.setClass(Smsbox.this, flitercontext.class);
                startActivity(intent);
                //设置动画
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                //Toast.makeText(this, "向左滑", Toast.LENGTH_SHORT).show();
            } else if(x2 - x1 > 50) {
                //显示白名单
                Intent intent=new Intent();
                intent.setClass(Smsbox.this, whitelist.class);
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                //Toast.makeText(this, "向右滑", Toast.LENGTH_SHORT).show();
            }
        }
        return super.dispatchTouchEvent(event);
    }
    public void onItemClick(AdapterView<?> parent, View view, int position, long id){
          //String screentime=list.get(position).getTime();
          /*Intent intent=new Intent(this,Screencontext.class);
          intent.putExtra("Screentime",screentime);
          startActivity(intent);*/
        //弹出自定义dialog
        CustomDialog dialog = new CustomDialog(this,R.layout.dialog_context,position);
        dialog.show();
    }
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

        new AlertDialog.Builder(this).setTitle("确定删除该条拦截短信？")
                .setIcon(android.R.drawable.sym_def_app_icon)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //按下确定键后的事件
                        String time=list.get(position).getTime();
                        //从数据库删除
                        db=sql.getWritableDatabase();
                        db.execSQL("delete from smsbox where time=?",new Object[]{time});
                        db.close();
                        list.remove(position);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(getApplicationContext(),"已删除该短信",Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton("取消",null).show();
        return true;
    }
    class CustomDialog extends Dialog {

        public CustomDialog(Context context,int layoutId,int position) {

            //使用自定义Dialog样式
            super(context, R.style.context_dialog);

            //指定布局
            setContentView(layoutId);

            //点击外部可消失
            setCancelable(true);
            String screentime=list.get(position).getTime();
            TextView context0=(TextView) findViewById(R.id.screen) ;
            context0.setMovementMethod(ScrollingMovementMethod.getInstance());
            //查询垃圾箱
            db=sql.getReadableDatabase();
            Cursor cursor=db.query("smsbox",null,"time=?",new String[]{screentime},null,null,null);
            if(cursor.getCount()!=0) {
                cursor.moveToFirst();
                context0.setText(cursor.getString(3));
                //cursor.getString(2);
                //Toast.makeText(this, "无垃圾短信", Toast.LENGTH_SHORT).show();
            }
            cursor.close();
            db.close();
        }
    }

    class MyAdapter extends ArrayAdapter<Sms> {

        private int resourceId;
        private List<Sms> objects;
        private Context context;


        public MyAdapter(Context context, int resourceId, List<Sms> objects) {
            super(context, resourceId, objects);
            // TODO Auto-generated constructor stub
            this.objects=objects;
            this.context=context;

        }
        private  class ViewHolder
        {
            TextView number;
            TextView time;
        }
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return objects.size();
        }
        @Override
        public Sms getItem(int position) {
            // TODO Auto-generated method stub
            return objects.get(position);
        }
        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            ViewHolder viewHolder = null;
            if(convertView==null)
            {
                viewHolder=new ViewHolder();
                LayoutInflater mInflater=LayoutInflater.from(context);
                convertView = mInflater.inflate(R.layout.sms_item, null);
                viewHolder.number = (TextView) convertView.findViewById(R.id.number);
                viewHolder.time = (TextView) convertView.findViewById(R.id.time);
                convertView.setTag(viewHolder);
            }
            else
            {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            Sms sms = objects.get(position);
            if(null!=sms)
            {
                viewHolder.number.setText(sms.getNumber());
                viewHolder.time.setText(sms.getTime());
            }
            return convertView;
        }
    }
}
