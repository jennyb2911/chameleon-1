package prj.chameleon.m4399;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.unipay.unipay_sdk.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import cn.m4399.operate.OperateCenter;
import cn.m4399.operate.OperateCenterConfig;
import cn.m4399.operate.User;
import cn.m4399.operate.OperateCenter.*;
import cn.m4399.operate.OperateCenterConfig.PopLogoStyle;
import cn.m4399.operate.OperateCenterConfig.PopWinPosition;
import cn.m4399.operate.UpgradeInfo;
import cn.m4399.operate.ui.activity.LoginActivity;


import cn.m4399.recharge.RechargeCenter;
import cn.m4399.recharge.RechargeSettings;
import prj.chameleon.channelapi.ApiCommonCfg;
import prj.chameleon.channelapi.Constants;
import prj.chameleon.channelapi.IAccountActionListener;
import prj.chameleon.channelapi.IDispatcherCb;
import prj.chameleon.channelapi.JsonMaker;
import prj.chameleon.channelapi.SingleSDKChannelAPI;




public final class M4399ChannelAPI extends SingleSDKChannelAPI.SingleSDK {

    OperateCenter mOpeCenter;
    OperateCenterConfig mOpeConfig;

    private String mAppName;
    private String mAppKey;
    private int mScreenOrientation;
    boolean mIsLandscape;
    boolean mIsDebug;


    private IAccountActionListener mAccountActionListener;

    public final static class M4399_CONSTANTS{
        public final static int m4399_ope_no_network=-2;
        public final static int m4399_ope_login_success = 16;
        public final static int m4399_ope_login_failed_error_known = 25;
        public final static int m4399_ope_sdk_login_failed_unable_access_oauth2 = 17;
        public final static int m4399_ope_login_error_key = 19;
        public final static int m4399_ope_login_already_login = 20;
        public final static int m4399_ope_login_failed_user_cancelled = 18  ;

        public final static int m4399_ope_logout_success = 32 ;
        public final static int m4399_ope_logout_failed_repeated = 33;
        public final static int m4399_ope_logout_failed_not_logging = 2;
        public final static int m4399_ope_logout_failed_error_unknown = 35;

        public final static int m4399_ope_sdk_update_success = 48;
        public final static int m4399_ope_update_result_no_update = 49;
        public final static int m4399_ope_update_result_user_canclled = 50;
        public final static int m4399_ope_update_result_update_now = 51 ;
        public final static int m4399_ope_update_result_download_success = 52 ;
        public final static int m4399_ope_update_result_check_error = 53 ;
        public final static int m4399_ope_update_result_download_error = 54 ;

        public final static int m4399_ope_pay_failed_init_error = 66 ;
        public final static int m4399_ope_pay_failed_fetch_token = 68 ;
    }

    public void initCfg(ApiCommonCfg commCfg, Bundle cfg) {

//        mAppId = cfg.getString("appId");
        mAppKey = cfg.getString("appKey");
        mChannel = commCfg.mChannel;
        mIsLandscape = commCfg.mIsLandscape;
        mIsDebug = commCfg.mIsDebug;
        mAppName = commCfg.mAppName;
        if (mIsLandscape){
            mScreenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        }else{
            mScreenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }

    }

    public static final String T_PREFFIX = "[M4399ChannelAPI]";
    private void showInToast(Activity activity, String msg) {
        Toast.makeText(activity, T_PREFFIX + msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void init(final Activity activity, final IDispatcherCb cb) {
            mOpeCenter = OperateCenter.getInstance();
            mOpeConfig = new OperateCenterConfig.Builder(activity)
            .setGameKey(mAppKey)
            .setGameName(mAppName)
            .setDebugEnabled(mIsDebug)
            .setOrientation(mScreenOrientation)
            .setSupportExcess(true)
            .setTestRecharge(false)
            .setPopLogoStyle(PopLogoStyle.POPLOGOSTYLE_ONE)
            .setPopWinPosition(PopWinPosition.POS_LEFT)
            .build();
            mOpeCenter.setConfig(mOpeConfig);
            //初始化SDK，在这个过程中会读取各种配置和检查当前帐号是否在登录中
            //只有在init之后， isLogin()返回的状态才可靠
            mOpeCenter.init(activity, new OperateCenter.OnInitGloabListener() {

                // 初始化结束执行后回调
                @Override
                public void onInitFinished(boolean isLogin, User userInfo) {
                    if (isLogin == mOpeCenter.isLogin()){
                        cb.onFinished(Constants.ErrorCode.ERR_OK, null);
                    }
                    else{
                        cb.onFinished(Constants.ErrorCode.ERR_FAIL, null);
                    }
                }

                // 注销帐号的回调， 包括个人中心里的注销和logout()注销方式
                @Override
                public void onUserAccountLogout(boolean fromUserCenter, int resultCode) {
                    if (fromUserCenter){
                        switch(resultCode){
                            case M4399_CONSTANTS.m4399_ope_logout_success:
                                cb.onFinished(Constants.ErrorCode.ERR_OK, null);
                                break;
                            default:
                                cb.onFinished(Constants.ErrorCode.ERR_FAIL, null);
                        }
                    }
                    else{
                        mAccountActionListener.onAccountLogout();
                    }
                }

                // 个人中心里切换帐号的回调
                @Override
                public void onSwitchUserAccountFinished(User userInfo) {


                }
            });

        mOpeCenter.setSupportExcess(false);

//        cb.onFinished(Constants.ErrorCode.ERR_OK, null);
    }

    @Override
    public void login(final Activity activity, final IDispatcherCb cb, final IAccountActionListener accountActionListener) {

        mOpeCenter.login(activity, new OnLoginFinishedListener(){
            public void onLoginFinished(boolean success, int resultCode, User userInfo) {
                if(success){
                    cb.onFinished(Constants.ErrorCode.ERR_OK, JsonMaker.makeLoginResponse(userInfo.getState(), userInfo.getUid(), mChannel));
                }
                else{
                    if(resultCode == M4399_CONSTANTS.m4399_ope_login_failed_user_cancelled){
                        cb.onFinished(Constants.ErrorCode.ERR_CANCEL, null);
                    }
                    else if (resultCode == M4399_CONSTANTS.m4399_ope_login_failed_error_known){
                        cb.onFinished(Constants.ErrorCode.ERR_UNKNOWN, null);
                    }
                    else{
                        showInToast(activity, OperateCenter.getResultMsg(resultCode)+ "GameKey is : "+OperateCenter.getGameKey());

                        cb.onFinished(Constants.ErrorCode.ERR_FAIL, null);
                    }
                }
            }
        });

        mAccountActionListener = accountActionListener;
    }

    @Override
    public boolean isSupportSwitchAccount() {
        return true;
    }

    @Override
    public boolean switchAccount(Activity activity, IDispatcherCb cb) {
        if (mAccountActionListener != null){
            mAccountActionListener.preAccountSwitch();
        }
        mOpeCenter.switchAccount(activity, new OnLoginFinishedListener(){
            public void onLoginFinished(boolean b, int i, User user) {
                try{
                    JSONObject obj = new JSONObject(user.toString());
                    mAccountActionListener.afterAccountSwitch(i, obj);
                }catch (Exception e){

                }
            }
        });

        return true;
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        super.onActivityResult(activity, requestCode, resultCode, data);
    }

    @Override
    public void logout(final Activity activity) {
        mOpeCenter.logout();
    }

    @Override
    public void charge(Activity activity,
                       String orderId,
                       String uidInGame,
                       String userNameInGame,//用户名
                       String serverId,
                       String currencyName,//货币名称
                       String payInfo,
                       int rate,//单价
                       int realPayMoney,//总价
                       boolean allowUserChange,
                       final IDispatcherCb cb) {
        int je = 0;
        je = (realPayMoney+99)/100;
        showInToast(activity, "invoke charge api..");

        mOpeCenter.recharge(activity, je, //充值金额（元）
                orderId, //游戏方订单号
                currencyName, //商品名称
                new OnRechargeFinishedListener() {
                    @Override
                    public void onRechargeFinished(boolean success, int resultCode, String msg)
                    {
                        if(success){
                            //请求游戏服，获取充值结果
                            cb.onFinished(Constants.ErrorCode.ERR_OK, null);
                        }else{
                            //充值失败逻辑
                            if (resultCode == M4399_CONSTANTS.m4399_ope_pay_failed_init_error)
                                cb.onFinished(Constants.ErrorCode.ERR_PAY_FAIL, null);
                            else if (resultCode == M4399_CONSTANTS.m4399_ope_pay_failed_fetch_token)
                                cb.onFinished(Constants.ErrorCode.ERR_PAY_SESSION_INVALID, null);
                            else
                                cb.onFinished(Constants.ErrorCode.ERR_PAY_UNKNOWN, null);
                        }
                    }
                });
    }

    @Override
    public void buy(Activity activity,
                    String orderId,
                    String uidInGame,
                    String userNameInGame,
                    String serverId,
                    String productName,
                    String productID,
                    String payInfo,
                    int productCount,//个数
                    int realPayMoney,
                    final IDispatcherCb cb) {
        int je = 0;
        je = (realPayMoney+99)/100;

        showInToast(activity, "invoke buy api..");

                mOpeCenter.recharge(activity, je, //充值金额（元）
            orderId, //游戏方订单号
            productName, //商品名称
            new OnRechargeFinishedListener() {
                @Override
                public void onRechargeFinished(boolean success, int resultCode, String msg)
                {
                    if(success){
                        //请求游戏服，获取充值结果
                        cb.onFinished(Constants.ErrorCode.ERR_OK, null);
                    }else{
                        //充值失败逻辑
                        if (resultCode == M4399_CONSTANTS.m4399_ope_pay_failed_init_error)
                            cb.onFinished(Constants.ErrorCode.ERR_PAY_FAIL, null);
                        else if (resultCode == M4399_CONSTANTS.m4399_ope_pay_failed_fetch_token)
                            cb.onFinished(Constants.ErrorCode.ERR_PAY_SESSION_INVALID, null);
                        else
                            cb.onFinished(Constants.ErrorCode.ERR_PAY_UNKNOWN, null);
                    }
                }
            });
    }

    @Override
    public String getUid() {
        if (!mOpeCenter.isLogin())
            return null;

        return mOpeCenter.getCurrentAccount().getUid();
    }

    @Override
    public String getToken() {
        if (!mOpeCenter.isLogin())
            return null ;

        return mOpeCenter.getCurrentAccount().getState();
    }

    @Override
    public boolean isLogined() {
        return mOpeCenter.isLogin();
    }

    @Override
    public String getId() {
        return "m4399";
    }

    @Override
    public void exit(final Activity activity, final IDispatcherCb cb) {
        mOpeCenter.shouldQuitGame(activity , new OnQuitGameListener() {
            @Override
            public void onQuitGame(boolean shouldQuit) {
                // 点击“退出游戏”时，shouldQuit 为true，游戏处理自己的退出业务逻辑
                // 点击“前往游戏圈”时，shouldQuit 为false，SDK 会进入游戏圈或者下载
                // 游戏盒子界面，游戏可以不做处理。
                // 点击“留在游戏”时，shouldQuit 为false，SDK 和游戏都不做任何处理
                if (shouldQuit) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            cb.onFinished(Constants.ErrorCode.ERR_LOGIN_GAME_EXIT_NOCARE, null);
                        }
                    });
                    activity.finish();
//                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            }
        });
    }

    @Override
    public void onDestroy(Activity activity) {
        super.onDestroy(activity);
        mOpeCenter.destroy();
        mOpeCenter = null;
    }
}
