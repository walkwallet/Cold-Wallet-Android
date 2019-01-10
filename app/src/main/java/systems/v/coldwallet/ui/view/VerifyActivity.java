package systems.v.coldwallet.ui.view;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.databinding.DataBindingUtil;
import systems.v.coldwallet.App;
import systems.v.coldwallet.R;
import systems.v.coldwallet.databinding.ActivityVerifyBinding;
import systems.v.coldwallet.ui.BaseActivity;
import systems.v.coldwallet.ui.view.main.MainActivity;
import systems.v.coldwallet.ui.view.wallet.WalletInitActivity;
import systems.v.coldwallet.utils.SPUtils;
import systems.v.wallet.basic.AlertDialog;
import systems.v.wallet.basic.utils.FileUtil;
import systems.v.wallet.basic.utils.KeyboardUtil;
import systems.v.wallet.basic.wallet.Wallet;

public class VerifyActivity extends BaseActivity {

    public static void launch(Activity from) {
        Intent intent = new Intent(from, VerifyActivity.class);
        from.startActivity(intent);
        openAlpha(from);
    }

    private ActivityVerifyBinding mBinding;
    private AsyncTask mLoadBackupTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_verify);
        initView();
    }

    private void initView() {
        mBinding.ivEnter.setEnabled(false);
        mBinding.etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() > 0) {
                    mBinding.ivEnter.setImageResource(R.drawable.ico_enter_activited);
                    mBinding.ivEnter.setEnabled(true);
                } else {
                    mBinding.ivEnter.setImageResource(R.drawable.ico_enter_normal);
                    mBinding.ivEnter.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mBinding.ivEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = mBinding.etPassword.getText().toString();
                if (mWallet == null) {
                    mLoadBackupTask = new LoadBackupTask().execute(password);
                } else {
                    if (password.equals(mWallet.getPassword())) {
                        finish();
                        return;
                    }
                    mBinding.etPassword.setText("");
                    showToast(R.string.password_wrong);
                }
            }
        });
    }


    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        showKeyboard();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public void finish() {
        super.finish();
        closeAlpha(mActivity);
    }

    @Override
    protected void onDestroy() {
        if (mLoadBackupTask != null && mLoadBackupTask.getStatus() != AsyncTask.Status.FINISHED) {
            mLoadBackupTask.cancel(true);
        }
        super.onDestroy();
    }

    private void showKeyboard() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                KeyboardUtil.show(mActivity, mBinding.etPassword);
            }
        }, 50);
    }

    private void showLogoutTips() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.BasicAlertDialog_Dark);
        builder.setTitle(R.string.password_wrong)
                .setIcon(R.drawable.basic_ico_alert)
                .setPositiveButton(R.string.continue_input, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mBinding.etPassword.setText(null);
                        showKeyboard();
                    }
                })
                .setNegativeButton(R.string.setting_logout, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SPUtils.clear();
                        FileUtil.deleteWallet(mActivity);
                        mApp.setWallet(null);
                        WalletInitActivity.launch(mActivity);
                        App.getInstance().finishAllActivities(WalletInitActivity.class);
                    }
                })
                .autoDismiss().confirm();
    }

    private class LoadBackupTask extends AsyncTask<String, Void, Wallet> {

        @Override
        protected Wallet doInBackground(String... params) {
            String password = params[0];
            return FileUtil.load(mApp, password);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoading();
        }

        @Override
        protected void onPostExecute(Wallet wallet) {
            hideLoading();
            if (wallet != null) {
                wallet.generateAccounts();
                mApp.setWallet(wallet);
                MainActivity.launch(mActivity, false);
                mApp.finishAllActivities();
            } else {
                showLogoutTips();
            }
        }
    }
}
