package systems.v.coldwallet.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import systems.v.coldwallet.App;
import systems.v.coldwallet.R;
import systems.v.coldwallet.ui.view.MonitorActivity;
import systems.v.coldwallet.ui.view.SplashActivity;
import systems.v.coldwallet.ui.view.VerifyActivity;
import systems.v.coldwallet.ui.widget.LoadingDialog;
import systems.v.coldwallet.utils.Constants;
import systems.v.coldwallet.utils.NetworkUtil;
import systems.v.coldwallet.utils.SPUtils;
import systems.v.coldwallet.utils.ToastUtil;
import systems.v.wallet.basic.utils.FileUtil;
import systems.v.wallet.basic.wallet.Wallet;

public abstract class BaseActivity extends AppCompatActivity {

    private static final List<String> MONITOR_UNCHECK_ACTIVITIES =
            Collections.unmodifiableList(Arrays.asList(
                    MonitorActivity.class.getSimpleName(),
                    SplashActivity.class.getSimpleName()
            ));

    private static final List<String> VERIFY_UNCHECK_ACTIVITIES =
            Collections.unmodifiableList(Arrays.asList(
                    MonitorActivity.class.getSimpleName(),
                    SplashActivity.class.getSimpleName(),
                    VerifyActivity.class.getSimpleName()
            ));

    protected Activity mActivity;
    protected LoadingDialog mLoading;
    protected App mApp;
    protected Handler mHandler;
    protected Wallet mWallet;
    private boolean isAppResume = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        mLoading = new LoadingDialog(mActivity);
        mApp = App.getInstance();
        mHandler = new Handler();
        mWallet = mApp.getWallet();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        isAppResume = !mApp.isAppVisible();
    }

    @Override
    protected void onResume() {
        check();
        isAppResume = false;
        super.onResume();
    }

    private void check() {
        if (checkNetworkState()) {
            if (VERIFY_UNCHECK_ACTIVITIES.contains(getClass().getSimpleName())) {
                return;
            }
            if (!FileUtil.walletExists(mActivity)) {
                return;
            }
            if (isAppResume || mApp.getWallet() == null) {
                VerifyActivity.launch(mActivity);
            }
        }
    }

    /**
     * check network
     *
     * @return all network disabled or monitor disabled
     */
    protected boolean checkNetworkState() {
        boolean wifi = false, bluetooth, data = false;
        NetworkUtil.NetworkType type;
        boolean monitorState = SPUtils.getBoolean(Constants.NETWORK_MONITOR);
        if (!monitorState) {
            return true;
        }
        if (!NetworkUtil.bluetoothIsConnected() && !NetworkUtil.isConnected(mActivity)) {
            return true;
        }
        if (MONITOR_UNCHECK_ACTIVITIES.contains(getClass().getSimpleName())) {
            return true;
        }
        bluetooth = NetworkUtil.bluetoothIsConnected();
        type = NetworkUtil.isConnectedType(mActivity);
        switch (type) {
            case NoConnect:
                break;
            case Wifi:
                wifi = true;
                break;
            case Mobile:
                data = true;
                break;
        }
        MonitorActivity.launch(mActivity, wifi, data, bluetooth);
        return false;
    }

    protected static void openAnimVertical(Activity activity) {
        activity.overridePendingTransition(R.anim.activity_open_in_anim_vertical, R.anim.activity_anim_static);
    }

    protected void closeAnimVertical(Activity activity) {
        activity.overridePendingTransition(R.anim.activity_anim_static, R.anim.activity_close_out_anim_vertical);
    }

    protected static void openAnimHorizontal(Activity activity) {
        activity.overridePendingTransition(R.anim.activity_open_in_anim_horizontal, R.anim.activity_anim_static);
    }

    protected void closeAnimHorizontal(Activity activity) {
        activity.overridePendingTransition(R.anim.activity_anim_static, R.anim.activity_close_out_anim_horizontal);
    }

    protected static void openAlpha(Activity activity) {
        activity.overridePendingTransition(R.anim.anim_alph_start, R.anim.activity_anim_static);
    }

    protected void closeAlpha(Activity activity) {
        activity.overridePendingTransition(R.anim.activity_anim_static, R.anim.anim_alph_close);
    }

    protected void showLoading() {
        if (mLoading != null) {
            mLoading.showLoading();
        }
    }

    protected void hideLoading() {
        if (mLoading != null) {
            mLoading.dismissLoading();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLoading != null) {
            mLoading.dismissLoading();
            mLoading = null;
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    protected void showToast(@StringRes int res) {
        ToastUtil.showToast(res);
    }
}
