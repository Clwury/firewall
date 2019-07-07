package com.example.clwury.firewall;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class Phoneinter extends AppCompatActivity implements AdapterView.OnItemLongClickListener{
    List<Phone> list;
    ListView numlist;
    Sql sql=new Sql(this);
    SQLiteDatabase db;
    MyAdapter adapter;
    private SwipeRefreshLayout swipe;

    public boolean onCreateOptionsMenu(Menu menu) {
        // 使用inflate方法来把布局文件中的定义的菜单 加载给 第二个参数所对应的menu对象
        getMenuInflater().inflate(R.menu.navigation, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.delete:
                if(!list.isEmpty()){
                    new AlertDialog.Builder(this).setTitle("确定删除所有拦截记录？")
                            .setIcon(android.R.drawable.sym_def_app_icon)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //按下确定键后的事件
                                    //从数据库删除
                                    db=sql.getWritableDatabase();
                                    db.execSQL("delete from Phone");
                                    db.close();
                                    list.clear();
                                    adapter.notifyDataSetChanged();
                                    Toast.makeText(getApplicationContext(),"已删除所有记录",Toast.LENGTH_SHORT).show();
                                }
                            }).setNegativeButton("取消",null).show();
                }
                break;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smsbox);
        list= new ArrayList<Phone>();
        numlist=(ListView) findViewById(R.id.lvsms);
        swipe=(SwipeRefreshLayout) findViewById(R.id.swipe);
        swipe.setColorSchemeResources(R.color.colorAccent,R.color.colorPrimary,R.color.colorPrimaryDark);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        list= new ArrayList<Phone>();
                        //查询拦截记录
                        db=sql.getReadableDatabase();
                        Cursor cursor=db.query("Phone",null,null,null,null,null,null);
                        if(cursor.getCount()==0) {
                            Toast.makeText(getApplicationContext(), "无拦截电话！！！", Toast.LENGTH_SHORT).show();
                        }else{
                            cursor.moveToFirst();
                            Phone sms=new Phone(cursor.getString(1),cursor.getString(2));
                            list.add(sms);
                        }
                        while (cursor.moveToNext()){
                            Phone sms=new Phone(cursor.getString(1),cursor.getString(2));
                            list.add(sms);
                        }
                        cursor.close();
                        db.close();
                        adapter = new MyAdapter(Phoneinter.this,0,list);
                        numlist.setAdapter(adapter);
                        swipe.setRefreshing(false);//取消刷新
                    }
                },1000);
            }
        });

        //查询拦截记录
        db=sql.getReadableDatabase();
        Cursor cursor=db.query("Phone",null,null,null,null,null,null);
        if(cursor.getCount()==0) {
            Toast.makeText(this, "无拦截电话！！！", Toast.LENGTH_SHORT).show();
        }else{
            cursor.moveToFirst();
            Phone sms=new Phone(cursor.getString(1),cursor.getString(2));
            list.add(sms);
        }
        while (cursor.moveToNext()){
            Phone sms=new Phone(cursor.getString(1),cursor.getString(2));
            list.add(sms);
        }
        cursor.close();
        db.close();
        adapter = new MyAdapter(Phoneinter.this,0,list);
        numlist.setAdapter(adapter);
        numlist.setOnItemLongClickListener(this);
    }
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id){

        new AlertDialog.Builder(this).setTitle("确定删除该条记录？")
                .setIcon(android.R.drawable.sym_def_app_icon)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //按下确定键后的事件
                        String time=list.get(position).getTime();
                        //从数据库删除
                        db=sql.getWritableDatabase();
                        db.execSQL("delete from Phone where time=?",new Object[]{time});
                        db.close();
                        list.remove(position);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(getApplicationContext(),"已删除",Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton("取消",null).show();
    return true;
    }
    /*private List<Phone> getData(){
        List<Phone> list= new ArrayList<Phone>();
        Phone sms=new Phone("18390216081","2077.11.1 19:30");
        list.add(sms);
        Phone sms1=new Phone("17680257612","2077.11.1 12:59:59");
        list.add(sms1);
        return list;
    }*/

    class MyAdapter extends ArrayAdapter<Phone> {

        private int resourceId;
        private List<Phone> objects;
        private Context context;


        public MyAdapter(Context context, int resourceId, List<Phone> objects) {
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
        public Phone getItem(int position) {
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
            Phone sms = objects.get(position);
            if(null!=sms)
            {
                viewHolder.number.setText(sms.getNumber());
                viewHolder.time.setText(sms.getTime());
            }
            return convertView;
        }
    }

}
