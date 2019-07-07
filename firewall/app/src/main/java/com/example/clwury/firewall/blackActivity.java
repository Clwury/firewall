package com.example.clwury.firewall;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class blackActivity extends AppCompatActivity {
    private ListView mListView;
    private CustomDialog dialog;
    private ArrayAdapter mAdapter;
    String number;
    Sql sql=new Sql(this);
    SQLiteDatabase db;
    private ArrayList<String> mData;
    //private String[] numbers={"110","120","17680251712"};
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 使用inflate方法来把布局文件中的定义的菜单 加载给 第二个参数所对应的menu对象
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_settings:
                final EditText et = new EditText(this);
                new AlertDialog.Builder(this).setTitle("请输入电话号码")
                        .setIcon(android.R.drawable.sym_def_app_icon)
                        .setView(et)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //按下确定键后的事件
                                String number=et.getText().toString().trim();
                                if (!"".equals(number)) {
                                    //则字符串不为空或空格,则添加数据
                                    db=sql.getWritableDatabase();
                                    ContentValues values=new ContentValues();
                                    values.put("number",number);
                                    db.insert("blacklist",null,values);
                                    mData.add(number);
                                    mAdapter.notifyDataSetChanged();
                                    Toast.makeText(getApplicationContext(),"已加入黑名单",Toast.LENGTH_SHORT).show();
                                    db.close();
                                }
                            }
                        }).setNegativeButton("取消",null).show();
                break;
        }
        return true;
    }
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_black);
        mData = new ArrayList<String>();
        //查询黑名单
        db=sql.getReadableDatabase();
        Cursor cursor=db.query("blacklist",null,null,null,null,null,null);
        //int i=cursor.getCount();
        if(cursor.getCount()==0){
            Toast.makeText(this,"黑名单为空",Toast.LENGTH_SHORT).show();
        }else {
            cursor.moveToFirst();
            mData.add(cursor.getString(1));
            //number[0]=cursor.getString(1);
        }
        while (cursor.moveToNext()){
            mData.add(cursor.getString(1));
        }
        cursor.close();
        db.close();
        mListView=(ListView) findViewById(R.id.lv);
        mAdapter = new ArrayAdapter<>(this, R.layout.list_item,R.id.item_number,mData);
        //设置Adapter
        mListView.setAdapter(mAdapter);
        //长按删除item
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //得到item
                number=mData.get(position);
                showLostFindDialog(position);
                return false;
            }
        });
    }

    /**
     * 显示提示窗口
     *
     * @param position
     */
    protected void showLostFindDialog(final int position) {
        dialog = new CustomDialog(this, R.style.mystyle,
                R.layout.dialog, position);
        dialog.show();

    }

    /**
     * 自定义dialog
     */
    class CustomDialog extends Dialog implements
            View.OnClickListener {
        /**
         * 布局文件
         **/
        int layoutRes;
        /**
         * 上下文对象
         **/
        Context context;

        /**
         * 取消按钮
         **/
        private Button bt_cancal;
        /**
         * 删除按钮
         **/
        private Button bt_delect;

        /**
         * 收获地址id
         */
        private int postion;
        public CustomDialog(Context context) {
            super(context);
            this.context = context;
        }
        /**
         * 自定义布局的构造方法
         *
         * @param context
         * @param resLayout
         */
        public CustomDialog(Context context, int resLayout) {
            super(context);
            this.context = context;
            this.layoutRes = resLayout;
        }
        /**
         * 自定义主题及布局的构造方法
         *
         * @param context
         * @param theme
         * @param resLayout
         * @param postion
         */
        public CustomDialog(Context context, int theme, int resLayout,
                            int postion) {
            super(context, theme);
            this.context = context;
            this.layoutRes = resLayout;
            this.postion = postion;
        }
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // 指定布局
            this.setContentView(layoutRes);

            // 根据id在布局中找到控件对象
            bt_cancal = (Button) findViewById(R.id.bt_cancal);
            bt_delect = (Button) findViewById(R.id.bt_delect);

            // 为按钮绑定点击事件监听器
            bt_cancal.setOnClickListener(this);
            bt_delect.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                // 删除按钮
                case R.id.bt_delect:
                    // 删除数据
                    deleteItem(postion);
                    dialog.dismiss();
                    break;
                // 取消按钮
                case R.id.bt_cancal:
                    dialog.dismiss();
                default:
                    break;
            }
        }
    }

    /**
     * 删除ListView中的数据
     *
     * @param postion item的位置
     */
    private void deleteItem(int postion) {
        mData.remove(postion);
        //从数据库删除
        db=sql.getWritableDatabase();
        db.execSQL("delete from blacklist where number=?",new Object[]{number});
        Toast.makeText(this,"已从黑名单移除",Toast.LENGTH_SHORT).show();
        mAdapter.notifyDataSetChanged();
        db.close();
    }
}

