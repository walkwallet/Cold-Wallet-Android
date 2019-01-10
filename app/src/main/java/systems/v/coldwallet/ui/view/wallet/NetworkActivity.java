package systems.v.coldwallet.ui.view.wallet;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;

import androidx.databinding.DataBindingUtil;
import systems.v.coldwallet.R;
import systems.v.coldwallet.databinding.ActivityNetworkBinding;
import systems.v.coldwallet.ui.BaseActivity;
import systems.v.coldwallet.utils.Constants;
import systems.v.coldwallet.utils.SPUtils;
import systems.v.wallet.basic.AlertDialog;
import systems.v.wallet.basic.wallet.Wallet;

public class NetworkActivity extends BaseActivity implements View.OnClickListener {

    public static final int TYPE_CREATE = 1;
    public static final int TYPE_IMPORT = 2;

    public static void launch(Activity from, int type) {
        Intent intent = new Intent(from, NetworkActivity.class);
        intent.putExtra("type", type);
        from.startActivity(intent);
        openAnimHorizontal(from);
    }

    private ActivityNetworkBinding mBinding;
    private String mNetworkType = Wallet.MAIN_NET;
    private int mType = TYPE_CREATE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_network);
        mType = getIntent().getIntExtra("type", TYPE_CREATE);
        mBinding.setClick(this);
        mBinding.tvSwitch.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        mBinding.tvSwitch.getPaint().setAntiAlias(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_switch:
                if (mNetworkType.equals(Wallet.MAIN_NET)) {
                    showWarningDialog();
                } else {
                    mNetworkType = Wallet.MAIN_NET;
                    mBinding.llMainNet.setVisibility(View.VISIBLE);
                    mBinding.llTestNet.setVisibility(View.GONE);
                    mBinding.tvSwitch.setText(R.string.network_switch_to_test_net);
                }
                break;
            case R.id.tv_next:
                SPUtils.setString(Constants.NETWORK_ENVIRONMENT, mNetworkType);
                if (mType == TYPE_CREATE) {
                    //create new wallet
                    SetPasswordActivity.launch(mActivity);
                } else {
                    //import wallet
                    ImportSeedActivity.launch(mActivity);
                }
                break;
        }
    }

    private void showWarningDialog() {
        new AlertDialog.Builder(mActivity, R.style.BasicAlertDialog_Dark)
                .setIcon(R.drawable.basic_ico_alert)
                .setTitle(R.string.network_switch_warning)
                .setPositiveButton(R.string.basic_alert_dialog_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mNetworkType = Wallet.TEST_NET;
                        mBinding.llMainNet.setVisibility(View.GONE);
                        mBinding.llTestNet.setVisibility(View.VISIBLE);
                        mBinding.tvSwitch.setText(R.string.network_switch_to_main_net);
                    }
                })
                .confirm();
    }
}
