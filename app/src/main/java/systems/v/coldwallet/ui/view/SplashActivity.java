package systems.v.coldwallet.ui.view;

import android.os.Bundle;

import systems.v.coldwallet.R;
import systems.v.coldwallet.ui.BaseActivity;
import systems.v.coldwallet.ui.view.main.MainActivity;
import systems.v.coldwallet.ui.view.wallet.WalletInitActivity;
import systems.v.coldwallet.utils.PermissionUtil;
import systems.v.coldwallet.utils.ToastUtil;
import systems.v.wallet.basic.utils.FileUtil;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if (!PermissionUtil.permissionGranted(this)) {
            PermissionUtil.checkPermissions(this);
        } else {
            launch();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PermissionUtil.PERMISSION_REQUEST_CODE: {
                if (!PermissionUtil.permissionGranted(this)) {
                    ToastUtil.showToast(R.string.grant_permissions);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 1000);
                } else {
                    launch();
                }
            }
            break;
        }
    }

    private void launch() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (FileUtil.walletExists(mActivity)) {
                    MainActivity.launch(SplashActivity.this, false);
                } else {
                    WalletInitActivity.launch(mActivity);
                }
                finish();
            }
        }, 500);
    }
}
