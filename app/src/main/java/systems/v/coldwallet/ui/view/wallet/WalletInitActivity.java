package systems.v.coldwallet.ui.view.wallet;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import systems.v.coldwallet.App;
import systems.v.coldwallet.R;
import systems.v.coldwallet.databinding.ActivityWalletInitBinding;
import systems.v.coldwallet.ui.BaseActivity;
import systems.v.coldwallet.ui.view.main.MainActivity;
import systems.v.coldwallet.ui.widget.VerifyDialogFragment;
import systems.v.coldwallet.utils.ToastUtil;
import systems.v.wallet.basic.AlertDialog;
import systems.v.wallet.basic.utils.FileUtil;
import systems.v.wallet.basic.wallet.Wallet;

public class WalletInitActivity extends BaseActivity implements View.OnClickListener {

    public static void launch(Activity from) {
        Intent intent = new Intent(from, WalletInitActivity.class);
        from.startActivity(intent);
        openAlpha(from);
    }

    private ActivityWalletInitBinding mBinding;
    private AsyncTask mLoadBackupTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_wallet_init);
        mLoading.setCancelable(false);
        initView();
        initListener();
        showWarningDialog();
    }

    @Override
    public void finish() {
        super.finish();
        closeAnimVertical(mActivity);
    }

    private void initView() {
        if (!hasBackUp()) {
            mBinding.tvBackupTips.setVisibility(View.GONE);
        }
    }

    private void initListener() {
        mBinding.llBackupImport.setOnClickListener(this);
        mBinding.tvWordImport.setOnClickListener(this);
        mBinding.tvCreateWallet.setOnClickListener(this);
        mBinding.tvImportWallet.setOnClickListener(this);
        mBinding.ivCloseImport.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_backup_import:
                if (hasBackUp()) {
                    new VerifyDialogFragment.Builder((AppCompatActivity) mActivity)
                            .setTitle(getString(R.string.wallet_backup_word_import))
                            .setOnNextListener(new VerifyDialogFragment.OnNextListener() {
                                @Override
                                public void onInput(String password) {
                                    mLoadBackupTask = new LoadBackupTask().execute(password);
                                }

                                @Override
                                public void onNext() {
                                }
                            }).show();
                } else {
                    new AlertDialog.Builder(mActivity, R.style.BasicAlertDialog_Dark)
                            .setIcon(R.drawable.basic_ico_alert)
                            .setTitle(R.string.wallet_no_backup_tips)
                            .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .alert();
                }

                break;
            case R.id.tv_import_wallet:
                // import
                mBinding.llWalletInitContainer.setVisibility(View.GONE);
                mBinding.llImportContainer.setVisibility(View.VISIBLE);
                break;
            case R.id.tv_word_import:
                // import seed
                NetworkActivity.launch(mActivity, NetworkActivity.TYPE_IMPORT);
                break;
            case R.id.tv_create_wallet:
                // create wallet
                NetworkActivity.launch(mActivity, NetworkActivity.TYPE_CREATE);
                break;
            case R.id.iv_close_import:
                mBinding.llWalletInitContainer.setVisibility(View.VISIBLE);
                mBinding.llImportContainer.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (mBinding.llImportContainer.getVisibility() == View.VISIBLE
                && mBinding.llWalletInitContainer.getVisibility() == View.GONE) {
            mBinding.llWalletInitContainer.setVisibility(View.VISIBLE);
            mBinding.llImportContainer.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (mLoadBackupTask != null && mLoadBackupTask.getStatus() != AsyncTask.Status.FINISHED) {
            mLoadBackupTask.cancel(true);
        }
        super.onDestroy();
    }

    private void showWarningDialog() {
        new AlertDialog.Builder(mActivity, R.style.BasicAlertDialog_Dark)
                .setIcon(R.drawable.basic_ico_alert)
                .setTitle(R.string.official_warning)
                .setMessage(R.string.wallet_init_official_warning_dialog_msg)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mBinding.llWalletInitContainer.setVisibility(View.VISIBLE);
                    }
                })
                .alert();
    }

    private class LoadBackupTask extends AsyncTask<String, Void, Wallet> {

        @Override
        protected Wallet doInBackground(String... params) {
            String password = params[0];
            if (FileUtil.backupExists()) {
                return FileUtil.loadBackup(password);
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoading();
        }

        @Override
        protected void onPostExecute(Wallet wallet) {
            if (wallet != null) {
                FileUtil.save(mActivity, wallet);
            }
            hideLoading();
            if (wallet != null) {
                wallet.generateAccounts();
                mApp.setWallet(wallet);
                MainActivity.launch(mActivity, true);
            } else {
                ToastUtil.showToast(R.string.password_wrong);
            }
        }
    }

    private boolean hasBackUp() {
        boolean sdCard = FileUtil.sdCardMountedExists();
        boolean backupExists = FileUtil.backupExists();
        if (!sdCard || !backupExists) {
            return false;
        }
        return true;
    }
}
