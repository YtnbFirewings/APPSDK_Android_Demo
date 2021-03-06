package cn.com.broadlink.blappsdkdemo.data.push;


import cn.com.broadlink.blappsdkdemo.BLApplication;
import cn.com.broadlink.blappsdkdemo.data.BaseHeadParam;
import cn.com.broadlink.blappsdkdemo.mvp.model.PushModel;
import cn.com.broadlink.blappsdkdemo.service.BLLocalFamilyManager;

/**
 * Created by YeJin on 2017/7/12.
 */

public class PushReportHeader extends BaseHeadParam {
    private String appid;
    private String loginsession;
    private boolean testuser = false;

    public PushReportHeader() {
        super();
        setAppid(PushModel.APPID);
        setLoginsession(BLApplication.mBLUserInfoUnits.getLoginsession());
        setFamilyid(BLLocalFamilyManager.getInstance().getCurrentFamilyInfo().getFamilyid());
    }

    public boolean isTestuser() {
        return testuser;
    }

    public void setTestuser(boolean testuser) {
        this.testuser = testuser;
    }

    public String getLoginsession() {
        return loginsession;
    }

    public void setLoginsession(String loginsession) {
        this.loginsession = loginsession;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }
}
