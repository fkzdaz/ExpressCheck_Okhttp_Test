package com.example.fkz.expresscheck_okhttp_test;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "FFFFFFFF";
    @InjectView(R.id.spinner)
    Spinner mSpinner;
    @InjectView(R.id.edtnum)
    EditText mEdnum;
    @InjectView(R.id.btn)
    Button mBtn;
    @InjectView(R.id.listview)
    ListView mListview;

    ExpressInformationBean mExpressInformationBean;
    Gson mGson;
    Handler mHandler=new Handler();
    private String mCom;
    private String[] mMap ={"STO","EMS","ZTO","SF","TT","GT","HT","YD","YT"};
    private String mHttpArg;
    private String mHttpUri;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        mGson=new Gson();
        mListview.setAdapter(mBaseAdapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCom = mMap[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mCom = mMap[0];
            }
        });
    }
    public void LoadData(String url){
        OkHttpClient okHttpClient=new OkHttpClient();
        final Request request=new Request.Builder()
                .url(url)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.d(TAG, "onFailure: "+e.getLocalizedMessage());
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "你输入的单号错误，请输入正确的单号", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                mExpressInformationBean=new ExpressInformationBean();
                String mResult=response.body().string();
                mExpressInformationBean=mGson.fromJson(mResult,ExpressInformationBean.class);

                if (mExpressInformationBean.getResultcode().equals("200")){
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mBaseAdapter.notifyDataSetChanged();
                        }
                    });
                }
                else {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "未查询到此单号信息。请核实后再输入", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
    BaseAdapter mBaseAdapter=new BaseAdapter() {
        private ViewHolder mHolder=null;
        @Override
        public int getCount() {
            if (mExpressInformationBean==null){
                return 0;
            }
            if (mExpressInformationBean.getResultcode().equals("200")){
                return mExpressInformationBean.getResult().getList().size();

            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView==null){
                convertView=View.inflate(MainActivity.this,R.layout.list_item,null);
                mHolder=new ViewHolder(convertView);
                convertView.setTag(mHolder);
            }else {
                mHolder= (ViewHolder) convertView.getTag();
            }
            ExpressInformationBean.ResultBean.ListBean Bean=mExpressInformationBean.getResult().getList().get(position);
            mHolder.text.setText("Time"+Bean.getDatetime()+"\nAddress"+Bean.getRemark()+"\n");
            return convertView;
        }
    };
    @OnClick(R.id.btn)
    public void onClick(){
        hideKeyboard();
        String num=mEdnum.getText().toString();
        mHttpUri="http://v.juhe.cn/exp/index?key=64b973df262d45cd6f1cb17bb323a755&com="+mCom+"&no"+num;
        LoadData(mHttpUri);
    }
            public class ViewHolder{
                private TextView text;
                ViewHolder(View root){
                    text= (TextView) root.findViewById(R.id.text);
                }
            }
    void hideKeyboard(){
        InputMethodManager inutMethodManager= (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inutMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
    }
}
