package systems.v.coldwallet.ui.view.main.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import systems.v.coldwallet.App;
import systems.v.coldwallet.R;
import systems.v.coldwallet.databinding.FragmentWalletBinding;
import systems.v.coldwallet.service.BackupService;
import systems.v.coldwallet.ui.view.ScannerActivity;
import systems.v.coldwallet.ui.view.main.adapter.AccountAdapter;
import systems.v.coldwallet.utils.Constants;
import systems.v.coldwallet.utils.SPUtils;
import systems.v.wallet.basic.utils.FileUtil;
import systems.v.wallet.basic.wallet.Account;
import systems.v.wallet.basic.wallet.Wallet;

public class WalletFragment extends Fragment {
    public static WalletFragment newInstance() {
        return new WalletFragment();
    }

    private Activity mActivity;
    private FragmentWalletBinding mBinding;
    private AccountAdapter mAdapter;
    private List<Account> mData = new ArrayList<>();
    private Wallet mWallet;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_wallet, container, false);
        initView();
        initListener();
        initData();
        return mBinding.getRoot();
    }

    private void initListener() {
        mAdapter.setOnAddClickListener(new AccountAdapter.onAddClickListener() {
            @Override
            public void onAddClick() {
                mWallet.append(1);
                mData.clear();
                FileUtil.save(mActivity, mWallet);
                if (SPUtils.getBoolean(Constants.AUTO_BACKUP)) {
                    BackupService.start(mActivity, mWallet);
                }
                mData.addAll(mWallet.getAccounts());
                mData.add(new Account());
                mAdapter.notifyDataSetChanged();
                mBinding.rvAccounts.getLayoutManager().scrollToPosition(mData.size() - 1);
            }
        });
        mBinding.ivScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScannerActivity.launch(mActivity);
            }
        });

    }

    private void initData() {
        mData.clear();

        mWallet = App.getInstance().getWallet();
        if (mWallet == null) {
            return;
        }
        mData.addAll(mWallet.getAccounts());
        mData.add(new Account());
        mAdapter.notifyDataSetChanged();
    }

    private void initView() {
        mBinding.rvAccounts.setLayoutManager(new LinearLayoutManager(mActivity));
        mAdapter = new AccountAdapter(mActivity, mData);
        mBinding.rvAccounts.setAdapter(mAdapter);
    }
}
