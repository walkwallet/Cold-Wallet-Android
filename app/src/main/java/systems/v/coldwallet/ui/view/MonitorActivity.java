package systems.v.coldwallet.ui.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import systems.v.coldwallet.R;
import systems.v.coldwallet.databinding.ActivityMonitorBinding;
import systems.v.coldwallet.ui.BaseActivity;
import systems.v.coldwallet.ui.widget.CommonItem;
import systems.v.coldwallet.utils.NetworkUtil;

public class MonitorActivity extends BaseActivity {
    public static void launch(Activity from, boolean wifi, boolean mobile, boolean bluetooth) {
        Intent intent = new Intent(from, MonitorActivity.class);
        intent.putExtra("wifi", wifi);
        intent.putExtra("mobile", mobile);
        intent.putExtra("bluetooth", bluetooth);
        intent.putExtra("from", from.getClass().getSimpleName());
        from.startActivity(intent);
        openAnimVertical(from);
    }

    private ActivityMonitorBinding mBinding;
    private boolean mWifiEnable, mMobileEnable, mBluetoothEnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_monitor);
        mWifiEnable = getIntent().getBooleanExtra("wifi", false);
        mMobileEnable = getIntent().getBooleanExtra("mobile", false);
        mBluetoothEnable = getIntent().getBooleanExtra("bluetooth", false);
        mBinding.tvRecheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });
        initView();
    }

    @Override
    public void finish() {
        super.finish();
        closeAnimVertical(mActivity);
    }

    private void refresh() {
        if (!NetworkUtil.bluetoothIsConnected() && !NetworkUtil.isConnected(mActivity)) {
            mBluetoothEnable = false;
            mMobileEnable = false;
            mWifiEnable = false;
        } else {
            mBluetoothEnable = NetworkUtil.bluetoothIsConnected();
            NetworkUtil.NetworkType type = NetworkUtil.isConnectedType(mActivity);
            switch (type) {
                case NoConnect:
                    mWifiEnable = false;
                    mMobileEnable = false;
                    break;
                case Wifi:
                    mWifiEnable = true;
                    break;
                case Mobile:
                    mMobileEnable = true;
                    break;
            }
        }
        initView();
    }

    private void initView() {
        if (mBluetoothEnable || mMobileEnable || mWifiEnable) {
            setState(mBinding.ciWifi, mWifiEnable);
            setState(mBinding.ciMobile, mMobileEnable);
            setState(mBinding.ciBluetooth, mBluetoothEnable);
        } else {
            finish();
        }
    }

    private void setState(CommonItem view, boolean state) {
        int colorId, textId;
        if (state) {
            colorId = R.color.text_green;
            textId = R.string.monitor_connected;
        } else {
            colorId = R.color.text_red;
            textId = R.string.monitor_unconnected;
        }
        view.setRightTextColor(ContextCompat.getColor(mActivity, colorId));
        view.setRightText(getString(textId));
    }

    @Override
    public void onBackPressed() {
    }
}
