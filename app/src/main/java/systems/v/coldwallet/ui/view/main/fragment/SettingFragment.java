package systems.v.coldwallet.ui.view.main.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import systems.v.coldwallet.App;
import systems.v.coldwallet.R;
import systems.v.coldwallet.databinding.FragmentSettingsBinding;
import systems.v.coldwallet.service.BackupService;
import systems.v.coldwallet.ui.view.main.AboutActivity;
import systems.v.coldwallet.ui.view.wallet.GenerateSeedActivity;
import systems.v.coldwallet.ui.view.wallet.WalletInitActivity;
import systems.v.coldwallet.ui.widget.VerifyDialogFragment;
import systems.v.coldwallet.utils.Constants;
import systems.v.coldwallet.utils.SPUtils;
import systems.v.wallet.basic.AlertDialog;
import systems.v.wallet.basic.utils.FileUtil;
import systems.v.wallet.basic.wallet.Wallet;

public class SettingFragment extends Fragment {
    public static SettingFragment newInstance() {
        return new SettingFragment();
    }

    private FragmentSettingsBinding mBinding;
    private Activity mActivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false);
        initView();
        initListener();
        return mBinding.getRoot();
    }

    private void initView() {
        mBinding.switchNetwork.setChecked(SPUtils.getBoolean(Constants.NETWORK_MONITOR));
        mBinding.switchBackup.setChecked(SPUtils.getBoolean(Constants.AUTO_BACKUP));
        if (App.getInstance().getWallet() == null) {
            return;
        }
        String networkType = App.getInstance().getWallet().getNetwork();
        if (Wallet.MAIN_NET.equals(networkType)) {
            mBinding.ciNetwork.setRightText(R.string.network_main_net);
        } else {
            mBinding.ciNetwork.setRightText(R.string.network_test_net);
        }
    }

    private void initListener() {
        mBinding.switchBackup.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SPUtils.setBoolean(Constants.AUTO_BACKUP, isChecked);
                if (isChecked) {
                    if (App.getInstance().getWallet() != null) {
                        BackupService.start(mActivity, App.getInstance().getWallet());
                    }
                }
            }
        });
        mBinding.switchNetwork.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SPUtils.setBoolean(Constants.NETWORK_MONITOR, isChecked);
            }
        });
        mBinding.ciBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new VerifyDialogFragment.Builder((AppCompatActivity) mActivity)
                        .setTitle(getString(R.string.word_backup_word))
                        .setOnNextListener(new VerifyDialogFragment.OnNextListener() {
                            @Override
                            public void onInput(String password) {

                            }

                            @Override
                            public void onNext() {
                                GenerateSeedActivity.launch(mActivity, App.getInstance().getWallet().getSeed());
                            }
                        }).show();
            }
        });
        mBinding.ciAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AboutActivity.launch(mActivity);
            }
        });
        mBinding.ciLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.BasicAlertDialog_Dark);
                builder.setTitle(R.string.log_out_wallet_title)
                        .setMessage(R.string.log_out_wallet_tips)
                        .setIcon(R.drawable.basic_ico_alert)
                        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SPUtils.clear();
                                FileUtil.deleteWallet(mActivity);
                                App.getInstance().setWallet(null);
                                WalletInitActivity.launch(mActivity);
                                App.getInstance().finishAllActivities(WalletInitActivity.class);
                            }
                        }).autoDismiss().confirm();
            }
        });
    }
}
