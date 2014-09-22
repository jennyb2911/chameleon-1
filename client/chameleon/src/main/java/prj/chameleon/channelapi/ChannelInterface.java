package prj.chameleon.channelapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 *  ChannelInterface is the only interface for client to use
 */
public class ChannelInterface {

    /**
     * init the SDK
     * @param activity the activity to give the real SDK
     *
     * @deprecated @param isDebug (deprecated) whether set sdk to debug mode
     * @param cb callback function when the request is finished, the JSON object is null
     */
	public static void init(final Activity activity,
                            boolean isDebug,
			  		        final IDispatcherCb cb) {
        Log.d(Constants.TAG, "on init from channel interface");
        _plugins.init(activity, cb);
	}

    /**
     * get channel user id
     * @return channel user id
     */
    public static String getUin() {
        return _plugins.mUserApi.getUid();
    }

    /**
     * user have loggined or not
     * @return true if the user have already logged in
     */
    public static boolean isLogined() {
        return _plugins.mUserApi.isLogined();
    }

    /**
     * get the token of this session
     * @return the token of the channel
     */
    public static String getToken() {
        return _plugins.mUserApi.getToken();
    }

    /**
     * get the pay token of this session
     * @return the pay token of this channel
     */
    public static String getPayToken() {
        return _plugins.mPayApi.getPayToken();
    }

    /**
     * feed the login rsp from the chameleon server to SDK
     * @param rsp the login rsp from chameleon server
     * @return true if login rsp succeeds, false otherwise
     */
    public static boolean onLoginRsp(String rsp) {
        return _plugins.mUserApi.onLoginRsp(rsp);
    }

    /**
     * login as a guest
     * @param activity the activity to give the real SDK
     * @param loginCallback callback when login guest if finished ,JSON object will have one or three fields
     *                      guest : if this is non-zero, then the user login as a guest, following two
     *                              fields will not exists
     *                      token : the access token from the channel
     *                      others: a segment of json string for SDK server
     * @param accountActionListener listener of the user account actions, refer to the interface definition
     */
    public static void loginGuest(Activity activity,
                                  IDispatcherCb loginCallback,
                                  IAccountActionListener accountActionListener) {
        _plugins.mUserApi.loginGuest(activity, loginCallback, accountActionListener);
    }

    /**
     * register guest, if the user is not login as a guest, this function does nothing
     * @param activity  the activity to give the real SDK
     * @param tips the tips for the register, not all channel support customize the tips
     * @param cb callback of the binding request
     *
     * @return boolean, true when user login as a guest and the register can continue, otherwise false
     */
    public static boolean registGuest(Activity activity, String tips, IDispatcherCb cb) {
        return _plugins.mUserApi.registGuest(activity, tips, cb);
    }

    /**
     * user login to channel
     * @param activity the activity to give the real SDK
     * @param cb JSON object will have two fields
     *           token : the access token from the channel
     *           others: a segment of json string for SDK server
     * @param accountActionListener listener of the user account actions, refer to the interface definition
     */
    public static void login(Activity activity,
                             IDispatcherCb cb,
                             IAccountActionListener accountActionListener) {
		_plugins.mUserApi.login(activity, cb, accountActionListener);
	}


    /**
     * user charge the currency in the game
     * @param activity
     * @param orderId the order id from server
     * @param uidInGame player id in the game
     * @param userNameInGame  player name in the game
     * @param serverId  current server id
     * @param currencyName the currency name
     * @param payInfo the additional payinfo from chameleon server
     * @param rate the rate of the game currency to RMB, e.g. ￥1.0 can buy 10 game currency, then
     *             rate = 10
     * @param realPayMoney the real money to pay
     * @param allowUserChange can user change the amnout he paid
     * @param cb JSON object will be null
     */
    public static void charge(Activity activity,
                              String orderId,
                              String uidInGame,
                              String userNameInGame,
                              String serverId,
                              String currencyName,
                              String payInfo,
                              int rate,
                              int realPayMoney,
                              boolean allowUserChange,
                              IDispatcherCb cb) {
		_plugins.mPayApi.charge(activity, orderId, uidInGame, userNameInGame,
                serverId, currencyName, payInfo, rate, realPayMoney,
                allowUserChange, cb);
	}

    /**
     *  user buy a product
     * @param activity the activity to give the real SDK
     * @param orderId the order id from server
     * @param uidInGame player id in the game
     * @param userNameInGame player name in the game
     * @param serverId  current server id
     * @param productName the name of the product
     * @param productID the id of the product
     * @param payInfo the additional payinfo from chameleon server
     * @param productCount the count of product
     * @param realPayMoney the real money to pay
     * @param cb JSON object will be null
     */
    public static void buy(android.app.Activity activity,
                           String orderId,
                           String uidInGame,
                           String userNameInGame,
                           String serverId,
                           String productName,
                           String productID,
                           String payInfo,
                           int productCount,
                           int realPayMoney,
                           IDispatcherCb cb) {
        _plugins.mPayApi.buy(activity, orderId, uidInGame, userNameInGame,
                serverId, productName, productID, payInfo, productCount,
                realPayMoney, cb);
    }

    /**
     * user logout
     * @param activity the activity to give the real SDK
     */
    public static void logout(Activity activity) {
        _plugins.mUserApi.logout(activity);
    }

    public static boolean isSupportSwitchAccount() {
        return _plugins.mUserApi.isSupportSwitchAccount();
    }
    /**
     * for user to switch the account, to many channel it performs logout then login
     * @param activity the activity to give the real SDK
     * @param cb callback when switch done, the ret value is the same as login
     * @return boolean, whether the switch account starts
     */
    public static boolean switchAccount(Activity activity, IDispatcherCb cb) {
        return _plugins.mUserApi.switchAccount(activity, cb);
    }

    /**
     * create the float tool bar ( required by 91, UC)
     * @param activity the activity to give the real SDK
     * @param position refer to Constant.Toolbar*
     */
    public static void createToolBar(Activity activity, int position) {
        _plugins.mUserApi.createToolBar(activity, position);
    }

    /**
     *  show or hide the float tool bar (required by 91, UC)
     * @param activity the activity to give the real SDK
     * @param visible true for show, false for hide
     */
    public static void showFloatBar(Activity activity, boolean visible) {
        _plugins.mUserApi.showFloatBar(activity, visible);
    }

    /**
     *  destroy the tool bar
     * @param activity the activity to give the real SDK
     */
    public static void destroyToolBar(Activity activity) {
        _plugins.mUserApi.destroyToolBar(activity);
    }

    /**
     *  when the app is activate from the background( refer to 91 doc, only required by 91)
     * @param activity the activity to give the real SDK
     * @param cb JSON object will be null
     */
    public static void onResume(Activity activity, IDispatcherCb cb) {
        _plugins.mUserApi.onResume(activity, cb);
    }

    /**
     *  when the app is stopped
     * @param activity the activity to give the real SDK
     */
    public static void onPause(Activity activity) {
        _plugins.mUserApi.onPause(activity);
    }

    /**
     *  check if the user is adult, if the channel doesn't provide this interface, user will be
     *  treated as adult
     * @param activity the activity to give the real SDK
     * @param cb JSON object will receive flag:
     *           ANTI_ADDICTION_ADULT
     *           ANTI_ADDICTION_CHILD
     *           ANTI_ADDICTION_UNKNOWN
     */
    public static void antiAddiction(Activity activity,
                                     IDispatcherCb cb) {
        _plugins.mUserApi.antiAddiction(activity, cb);
    }

    /**
     * destroy the sdk instance
     * @param activity
     */
    public static void exit(Activity activity, final IDispatcherCb cb) {
        _plugins.exit(activity, new IDispatcherCb() {

            @Override
            public void onFinished(int retCode, JSONObject data) {
                cb.onFinished(retCode, data);
            }
        });
    }

    /**
     * run additional protocol
     * @param activity
     * @param protocol the additional protocol
     * @param message the input message of the protocol
     * @param cb can be null, otherwise it will called when the sdk is desctoryed, JSON will be null
     */
    public static boolean runProtocol(Activity activity,
                               String protocol,
                               String message,
                               IDispatcherCb cb) {
        return _plugins.mUserApi.runProtocol(activity, protocol, message, cb);
    }

    /**
     * 当前SDK是否支持该函数
     * @param protocol protocol名称
     * @return
     */
    public static boolean isSupportProtocol(String protocol) {
        return _plugins.mUserApi.isSupportProtocol(protocol);
    }

    /**
     * submit player login info, for uc, oppo
     * @param activity activity
     * @param roleId player id
     * @param roleName player name
     * @param roleLevel player level
     * @param zoneId zone id
     * @param zoneName zone name
     */
    public static void submitPlayerInfo(Activity activity,
                                        String roleId,
                                        String roleName,
                                        String roleLevel,
                                        int zoneId,
                                        String zoneName) {
        _plugins.mUserApi.submitPlayerInfo(activity, roleId, roleName, roleLevel,
                zoneId, zoneName);
    }

    /**
     *
     * @param event refer to Constants.ApplicationEvent
     * @param arguments the var-arguments for this event
     */
    public static void onApplicationEvent(int event, Object... arguments) {
        _plugins.onApplicationEvent(event, arguments);
    }

    /**
     *
     * @return get current channel name
     */
	public static String getChannelName() {
		return _plugins.getChannelName();
	}

    /**
     * on activity result, the parameter is the same as Activity.onActivityResult
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public static void onActivityResult(int requestCode, int resultCode, Intent data) {
        _plugins.onActivityResult(requestCode, resultCode, data);
    }

    /**
     *  the channel implementation for current package
     */
    private static class Plugins {
        private ArrayList<APIGroup> mApiGroups = new ArrayList<APIGroup>();
        private IChannelUserAPI mUserApi;
        private IChannelPayAPI mPayApi;
        private String mChannelName;

        public void onResume(final Activity activity, final IDispatcherCb cb) {
            final Iterator<APIGroup> iterator = mApiGroups.iterator();
            final Runnable initProc = new Runnable() {
                @Override
                public void run() {
                    if (!iterator.hasNext()) {
                        cb.onFinished(Constants.ErrorCode.ERR_OK, null);
                        return;
                    }
                    APIGroup group = iterator.next();
                    group.onResume(activity, new IDispatcherCb() {
                        @Override
                        public void onFinished(int retCode, JSONObject data) {
                            if (retCode != Constants.ErrorCode.ERR_OK) {
                                cb.onFinished(retCode, null);
                                return;
                            }
                            run();
                        }
                    });
                }
            };
            initProc.run();
        }

        public void onPause(Activity activity) {
            for (APIGroup group : mApiGroups) {
                group.onPause(activity);
            }
        }

        public void init(final Activity activity, final IDispatcherCb cb) {
            final Iterator<APIGroup> iterator = mApiGroups.iterator();
            final Runnable initProc = new Runnable() {
                @Override
                public void run() {
                    if (!iterator.hasNext()) {
                        cb.onFinished(Constants.ErrorCode.ERR_OK, null);
                        return;
                    }
                    APIGroup group = iterator.next();
                    group.init(activity, new IDispatcherCb() {
                        @Override
                        public void onFinished(int retCode, JSONObject data) {
                            if (retCode != Constants.ErrorCode.ERR_OK) {
                                cb.onFinished(retCode, null);
                                return;
                            }
                            run();
                        }
                    });
                }
            };
            initProc.run();
        }

        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            for (APIGroup group : mApiGroups) {
                group.onActivityResult(requestCode, resultCode, data);
            }
        }

        public void onApplicationEvent(int event, Object... arguments) {
            for (APIGroup group : mApiGroups) {
                group.onApplicationEvent(event, arguments);
            }
        }

        public void exit(final Activity activity, final IDispatcherCb cb) {
            // only user api can blocks the exit, other callback will be ignored
            mUserApi.exit(activity, new IDispatcherCb() {
                @Override
                public void onFinished(int retCode, JSONObject data) {
                    if (retCode != Constants.ErrorCode.ERR_OK) {
                        cb.onFinished(retCode, null);
                    }
                }
            });
            final Iterator<APIGroup> iterator = mApiGroups.iterator();
            final Runnable initProc = new Runnable() {
                @Override
                public void run() {
                    if (!iterator.hasNext()) {
                        cb.onFinished(Constants.ErrorCode.ERR_OK, null);
                        return;
                    }
                    APIGroup group = iterator.next();
                    if (group == mUserApi) {
                        run();
                        return;
                    }
                    group.exit(activity, new IDispatcherCb() {
                        @Override
                        public void onFinished(int retCode, JSONObject data) {
                            run();
                        }
                    });
                }
            };
            initProc.run();
        }

        private void addApiGroup(APIGroup group) {
            if (group == null) {
                throw new RuntimeException("empty api group");
            }
            if (group.testType(Constants.PluginType.USER_API)) {
                if (mUserApi != null) {
                    throw new RuntimeException("user api is already registered");
                }
                mUserApi = (IChannelUserAPI) group.getApi();
            }
            if (group.testType(Constants.PluginType.PAY_API)) {
                if (mPayApi != null) {
                    throw new RuntimeException("pay api is already registered");
                }
                mPayApi = (IChannelPayAPI) group.getApi();
            }
            mApiGroups.add(group);
        }

        public String getChannelName() {
            return mChannelName;
        }
    }
    private static Plugins _plugins = new Plugins();

    public static void addApiGroup(APIGroup apiGroup) {
        _plugins.addApiGroup(apiGroup);
    }

}
