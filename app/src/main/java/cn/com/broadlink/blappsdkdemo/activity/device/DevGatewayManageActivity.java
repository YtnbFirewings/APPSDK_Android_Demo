package cn.com.broadlink.blappsdkdemo.activity.device;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.List;

import cn.com.broadlink.base.BLBaseResult;
import cn.com.broadlink.blappsdkdemo.BLApplication;
import cn.com.broadlink.blappsdkdemo.R;
import cn.com.broadlink.blappsdkdemo.activity.base.TitleActivity;
import cn.com.broadlink.blappsdkdemo.common.BLCommonUtils;
import cn.com.broadlink.blappsdkdemo.common.BLConstants;
import cn.com.broadlink.blappsdkdemo.common.BLToastUtils;
import cn.com.broadlink.blappsdkdemo.view.BLAlert;
import cn.com.broadlink.blappsdkdemo.view.OnSingleClickListener;
import cn.com.broadlink.blappsdkdemo.view.recyclerview.adapter.BLBaseRecyclerAdapter;
import cn.com.broadlink.blappsdkdemo.view.recyclerview.adapter.BLBaseViewHolder;
import cn.com.broadlink.blappsdkdemo.view.recyclerview.divideritemdecoration.BLDividerUtil;
import cn.com.broadlink.sdk.BLLet;
import cn.com.broadlink.sdk.data.controller.BLDNADevice;
import cn.com.broadlink.sdk.result.controller.BLSubDevAddResult;
import cn.com.broadlink.sdk.result.controller.BLSubDevListResult;

public class DevGatewayManageActivity extends TitleActivity {

    private EditText mTvResult;
    private Button mBtScanStart;
    private Button mBtScanStop;
    private Button mBtGetScanNewList;
    private Button mBtGetList;
    private Button mBtQueryAddResult;
    private RecyclerView mRvList;
    private EditText mEtPid;
    
    private BLDNADevice mDNADevice;
    private List<BLDNADevice> mSubDeviceList = new ArrayList<>();
    private SubDevAdapter mAdapter;
    private boolean mIsNewSubListType = true;
    private String mPid = null;
    private int mSelectedIndex = -1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dev_gateway_manage);
        setBackWhiteVisible();
        setTitle("Gateway Management");
        
        mDNADevice = getIntent().getParcelableExtra(BLConstants.INTENT_PARCELABLE);
        
        findView();

        initView();

        setListener();
    }

    private boolean checkPid(){
        final boolean ret = !TextUtils.isEmpty(mEtPid.getText());
        
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(!ret){
                        BLToastUtils.show("Input the pid to add first!");
                        mEtPid.requestFocus();
                    }else{
                        if(mEtPid.getText().length()==32){
                            mPid = mEtPid.getText().toString();
                        }else{
                            mPid = BLCommonUtils.deviceType2Pid(mEtPid.getText().toString());
                        }
                    }
                    Log.d("mPid", mPid);
                }
            });
        return ret;
    }
    
    private void setListener() {
        mBtScanStart.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                if (checkPid())new StartScanTask().execute();
            }
        });

        mBtScanStop.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                if (checkPid())new StopScanTask().execute();
            }
        });

        mBtGetScanNewList.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                if (checkPid()) new GetNewSubDevsTask().execute();
            }
        });

        mBtGetList.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                new GetAddedSubDevsTask().execute();
            }
        });

        mBtQueryAddResult.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                if(mSelectedIndex >=0 && mSelectedIndex < mSubDeviceList.size()){
                    new QueryAddResult().executeOnExecutor(BLApplication.FULL_TASK_EXECUTOR, mSelectedIndex);
                }else{
                    final String msg = "Scan Sub Devices List, Click One Of Them To Add, Then You Can Query The Add Result.";
                    BLToastUtils.show(msg);
                }
            }
        });

        mAdapter.setOnItemClickListener(new BLBaseRecyclerAdapter.OnClickListener() {
            @Override
            public void onClick(int position, int viewType) {
                if(mIsNewSubListType){
                    if (checkPid()) {
                        mSelectedIndex = position;
                        new AddSubDevTask().execute(position);
                    }
                }else{
                    BLCommonUtils.toActivity(mActivity, DevDnaStdControlActivity.class, mSubDeviceList.get(position));
                }
            }
        });

        mAdapter.setOnItemLongClickListener(new BLBaseRecyclerAdapter.OnLongClickListener() {
            @Override
            public boolean onLongClick(final int position, int viewType) {
                if(!mIsNewSubListType){
                    BLAlert.showDialog(mActivity, "Confirm to delete this sub device?", new BLAlert.DialogOnClickListener() {
                        @Override
                        public void onPositiveClick() {
                            new DelSubDevTask().execute(position);
                        }

                        @Override
                        public void onNegativeClick() {

                        }
                    });
                    return true;
                }
                
                return false;
            }
        });
    }

    private void initView() {
        mAdapter = new SubDevAdapter();
        mRvList.setLayoutManager(new LinearLayoutManager(mActivity));
        mRvList.setAdapter(mAdapter);
        mRvList.addItemDecoration(BLDividerUtil.getDefault(mActivity, mSubDeviceList));
    }

    private void showResult(Object result) {
        if(result == null){
            mTvResult.setText("Return null");
        }else{
            if(result instanceof String){
                mTvResult.setText((String)result);
            }else{
                mTvResult.setText(JSON.toJSONString(result, true));
            }

        }
    }
    
    private void findView() {
        mTvResult = (EditText) findViewById(R.id.et_result);
        mBtScanStart = (Button) findViewById(R.id.bt_scan_start);
        mBtScanStop = (Button) findViewById(R.id.bt_scan_stop);
        mBtQueryAddResult = (Button) findViewById(R.id.bt_query_add_result);
        mBtGetScanNewList = (Button) findViewById(R.id.bt_get_scan_new_list);
        mBtGetList = (Button) findViewById(R.id.bt_get_list);
        mRvList = (RecyclerView) findViewById(R.id.rv_list);
        mEtPid = (EditText) findViewById(R.id.et_pid);
    }

    class SubDevAdapter extends BLBaseRecyclerAdapter<BLDNADevice> {

        public SubDevAdapter() {
            super(mSubDeviceList, R.layout.item_family_room);
        }

        @Override
        public void onBindViewHolder(BLBaseViewHolder holder, final int position) {
            super.onBindViewHolder(holder, position);
            holder.setText(R.id.tv_name, "Pid: " + mBeans.get(position).getPid());
            holder.setText(R.id.tv_mac, "Did: " + mBeans.get(position).getDid());
        }
    }

    private class StartScanTask extends AsyncTask<String, Void, BLBaseResult> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
          showProgressDialog("Scan...");
        }

        @Override
        protected BLBaseResult doInBackground(String... params) {
            return BLLet.Controller.subDevScanStart(mDNADevice.getDid(), mPid);
        }

        @Override
        protected void onPostExecute(BLBaseResult blBaseResult) {
            super.onPostExecute(blBaseResult);
            dismissProgressDialog();
            showResult(blBaseResult);
        }
    }
    
    private class StopScanTask extends AsyncTask<String, Void, BLBaseResult> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
          showProgressDialog("Stop Scan...");
        }

        @Override
        protected BLBaseResult doInBackground(String... params) {
            return BLLet.Controller.subDevScanStop(mDNADevice.getDid());
        }

        @Override
        protected void onPostExecute(BLBaseResult blBaseResult) {
            super.onPostExecute(blBaseResult);
            dismissProgressDialog();
            showResult(blBaseResult);
        }
    }
    
    private class GetNewSubDevsTask extends AsyncTask<String, Void, BLSubDevListResult> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
          showProgressDialog("Get Scanned List...");
        }

        @Override
        protected BLSubDevListResult doInBackground(String... params) {
            return BLLet.Controller.devSubDevNewListQuery(mDNADevice.getDid(), mPid,0, 5);
        }

        @Override
        protected void onPostExecute(BLSubDevListResult blBaseResult) {
            super.onPostExecute(blBaseResult);
            dismissProgressDialog();
            showResult(blBaseResult);

            if (blBaseResult != null && blBaseResult.succeed() && blBaseResult.getData() != null) {
                mIsNewSubListType = true;
                mSubDeviceList.clear();
                mSubDeviceList.addAll(blBaseResult.getData().getList());
                
                mAdapter.notifyDataSetChanged();
            }
        }
    }
    
    private class GetAddedSubDevsTask extends AsyncTask<String, Void, BLSubDevListResult> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
          showProgressDialog("Get Added List...");
        }

        @Override
        protected BLSubDevListResult doInBackground(String... params) {

            int index = 0;
            final int QUERY_MAX_COUNT = 5;
            final String getwayDid = mDNADevice.getDid();
            BLSubDevListResult result = BLLet.Controller.devSubDevListQuery(getwayDid, index, QUERY_MAX_COUNT);

            if (result != null && result.succeed() && result.getData() != null && result.getData().getList() != null && !result.getData().getList().isEmpty()) {
                int total = result.getData().getTotal();
                int currListSize = result.getData().getList().size();
                if(currListSize < total){
                    while (true){
                        index = result.getData().getList().size();
                        BLSubDevListResult queryResult = null;
                        //循环最多查询3次，成功之后跳出循环
                        for (int i = 0; i < 3; i++) {
                            queryResult = BLLet.Controller.devSubDevListQuery(getwayDid, index, QUERY_MAX_COUNT);
                            if (queryResult != null && queryResult.succeed()) {
                                break;
                            }
                        }

                        if (queryResult != null && queryResult.succeed()) {
                            if (queryResult.getData() != null && queryResult.getData().getList() != null && !result.getData().getList().isEmpty()) {
                                result.getData().getList().addAll(queryResult.getData().getList());
                                result.getData().setIndex(queryResult.getData().getIndex());

                                if(result.getData().getList().size() >= total){
                                    return result;
                                }
                            }else{
                                return queryResult;
                            }
                        }else{
                            return queryResult;
                        }
                    }
                }
            }
            return result;

        }

        @Override
        protected void onPostExecute(BLSubDevListResult blBaseResult) {
            super.onPostExecute(blBaseResult);
            dismissProgressDialog();
            showResult(blBaseResult);

            if (blBaseResult != null && blBaseResult.succeed()) {
                mIsNewSubListType = false;
                mSubDeviceList.clear();
                
                if(blBaseResult.getData() != null){
                    for (BLDNADevice dev  : blBaseResult.getData().getList() ) {
                        dev.setpDid(mDNADevice.getDid());
                        mSubDeviceList.add(dev);
                        BLLet.Controller.addDevice(dev);
                    } 
                }
              
                mAdapter.notifyDataSetChanged();
            }
        }
    }
    
    
    private class AddSubDevTask extends AsyncTask<Integer, Void, BLBaseResult> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
          showProgressDialog("Add...");
        }

        @Override
        protected BLBaseResult doInBackground(Integer... params) {
            
            return BLLet.Controller.subDevAdd(mDNADevice.getDid(), mSubDeviceList.get(params[0]));
        }

        @Override
        protected void onPostExecute(BLBaseResult blBaseResult) {
            super.onPostExecute(blBaseResult);
            dismissProgressDialog();
            showResult(blBaseResult);
        }
    }
    
    
    private class DelSubDevTask extends AsyncTask<Integer, Void, BLBaseResult> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
          showProgressDialog("Add...");
        }

        @Override
        protected BLBaseResult doInBackground(Integer... params) {
            return BLLet.Controller.subDevDel(mDNADevice.getDid(), mSubDeviceList.get(params[0]).getDid());
        }

        @Override
        protected void onPostExecute(BLBaseResult blBaseResult) {
            super.onPostExecute(blBaseResult);
            dismissProgressDialog();
            showResult(blBaseResult);
            if(blBaseResult != null && blBaseResult.succeed()){
                new GetAddedSubDevsTask().execute();
            }
        }
    }

    private class QueryAddResult extends AsyncTask<Integer, Void, BLSubDevAddResult> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog("Query Add Result...");
        }

        @Override
        protected BLSubDevAddResult doInBackground(Integer... params) {
            BLSubDevAddResult downloadResult = new BLSubDevAddResult();
            
            for (int i = 0; i < 10; i++) {
                SystemClock.sleep(1000);
                downloadResult = BLLet.Controller.devSubDevAddResultQuery(mDNADevice.getDid(), mSubDeviceList.get(params[0]).getDid());
                if(downloadResult != null && downloadResult.succeed() && downloadResult.getSubdevStatus() == 0 && downloadResult.getDownload_status() == 0){
                    BLLet.Controller.subDevScanStop(mDNADevice.getDid());

                    return downloadResult;
                }
            }
            return downloadResult;
        }

        @Override
        protected void onPostExecute(BLSubDevAddResult downloadResult) {
            super.onPostExecute(downloadResult);
            dismissProgressDialog();
            showResult(downloadResult);
        }
    }
    
}
