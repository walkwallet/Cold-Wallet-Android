package systems.v.coldwallet.ui.view.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.google.android.material.tabs.TabLayout;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.Util;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import systems.v.coldwallet.App;
import systems.v.coldwallet.R;
import systems.v.coldwallet.databinding.ActivityMainBinding;
import systems.v.coldwallet.ui.BaseActivity;
import systems.v.coldwallet.ui.view.ConfirmTxActivity;
import systems.v.coldwallet.ui.view.PageScanActivity;
import systems.v.coldwallet.ui.view.main.fragment.SettingFragment;
import systems.v.coldwallet.ui.view.main.fragment.WalletFragment;
import systems.v.coldwallet.utils.UIUtil;
import systems.v.wallet.basic.utils.JsonUtil;
import systems.v.wallet.basic.utils.QRCodeUtil;
import systems.v.wallet.basic.utils.TxUtil;
import systems.v.wallet.basic.wallet.Account;
import systems.v.wallet.basic.wallet.Operation;
import systems.v.wallet.basic.wallet.Transaction;

public class MainActivity extends BaseActivity {

    public static void launch(Activity from, boolean closeOther) {
        if (closeOther) {
            App.getInstance().finishAllActivities(MainActivity.class);
        }
        Intent intent = new Intent(from, MainActivity.class);
        from.startActivity(intent);
    }

    private static final String TAG = "MainActivity";

    private List<Fragment> mFragments = new ArrayList<>();
    private ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        initView();
        initListener();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_CANCELED){
            return;
        }
        String qrContents = "";
        switch (requestCode) {
            case IntentIntegrator.REQUEST_CODE:
                if(data == null){
                    UIUtil.showUnsupportQrCodeDialog(MainActivity.this);
                    return ;
                }
                IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if (result != null && result.getContents() != null) {
                    qrContents = result.getContents();
                    Log.d(TAG, "scan result is " + qrContents);
                }
            break;
            case PageScanActivity.REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    qrContents = data.getStringExtra(PageScanActivity.RESULT);
                    Log.d(TAG, "page scan result is " + qrContents);
                }
            break;
        }
        if (JsonUtil.isJsonString(qrContents)) {
            Operation op = Operation.parse(qrContents);
            if (op == null) {
                UIUtil.showUnsupportQrCodeDialog(this);
                return;
            }
            if (op.validate(Operation.TRANSACTION)) {
                Transaction tx = JSON.parseObject(qrContents, Transaction.class);
                if (tx == null || !Transaction.validate(tx.getTransactionType())) {
                    Log.w(TAG, "scan result is unsupported transaction");
                    UIUtil.showUnsupportQrCodeDialog(this);
                    return;
                }
                if (tx.getInvoice() != null && !tx.getInvoice().isEmpty() &&
                        (tx.getAttachment() == null || tx.getAttachment().isEmpty())){
                    tx.setAttachment(tx.getInvoice());
                }

                Account sender = mWallet.getAccount(tx.getSenderPublicKey());

                if (sender == null) {
                    UIUtil.showSenderNotFoundDialog(this);
                    return;
                }

                tx.setAttachment(TxUtil.decodeAttachment(tx.getAttachment()));
                ConfirmTxActivity.launch(this, tx);
            } else if(op.validate(Operation.CONTRACT)){
                Transaction tx = JSON.parseObject(qrContents, Transaction.class);
                if (tx == null) {
                    Log.w(TAG, "scan result is unsupported transaction");
                    UIUtil.showUnsupportQrCodeDialog(this);
                    return;
                }
                Account sender = mWallet.getAccountByAddress(tx.getAddress());

                if (sender == null) {
                    UIUtil.showSenderNotFoundDialog(this);
                    return;
                }
                tx.setTransactionType(Transaction.CONTRACT_REGISTER);

                ConfirmTxActivity.launch(this, tx);
            } else if(op.validate(Operation.FUNCTION)){
                Transaction tx = JSON.parseObject(qrContents, Transaction.class);
                if (tx == null) {
                    Log.w(TAG, "scan result is unsupported transaction");
                    UIUtil.showUnsupportQrCodeDialog(this);
                    return;
                }
                if (tx.getInvoice() != null && !tx.getInvoice().isEmpty() &&
                        (tx.getAttachment() == null || tx.getAttachment().isEmpty())){
                    tx.setAttachment(tx.getInvoice());
                }
                Account sender = mWallet.getAccountByAddress(tx.getAddress());

                if (sender == null) {
                    UIUtil.showSenderNotFoundDialog(this);
                    return;
                }
                tx.setTransactionType(Transaction.CONTRACT_EXECUTE);
                tx.setAttachment(TxUtil.decodeAttachment(tx.getAttachment()));
                ConfirmTxActivity.launch(this, tx);
            } else {
                UIUtil.showUnsupportQrCodeDialog(this);
                return;
            }
        } else if (qrContents.startsWith(QRCodeUtil.Prefix)) {
            if(!qrContents.startsWith(QRCodeUtil.Prefix+ "1/")){
                UIUtil.showWrongQrCodeDialog(this);
                return;
            }
            PageScanActivity.launch(this, qrContents);
        } else {
            Log.w(TAG, "scan result is unsupported transaction");
            UIUtil.showUnsupportQrCodeDialog(this);
            return;
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private void initView() {
        mFragments.add(WalletFragment.newInstance());
        mFragments.add(SettingFragment.newInstance());
        FragmentPagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mFragments.get(position);
            }

            @Override
            public int getCount() {
                return mFragments.size();
            }
        };
        mBinding.vpMain.setAdapter(adapter);
        TabLayout tabLayout = mBinding.tabMain;
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.getTabAt(0).setCustomView(getTab(R.string.wallet_label, R.drawable.tab_wallet));
        tabLayout.getTabAt(1).setCustomView(getTab(R.string.setting_label, R.drawable.tab_setting));
    }

    private void initListener() {
        final TabLayout tabLayout = mBinding.tabMain;
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mBinding.vpMain.setCurrentItem(tab.getPosition());
                int pos = tab.getPosition();
                for (int i = 0; i < tabLayout.getTabCount(); i++) {
                    View view = tabLayout.getTabAt(i).getCustomView();
                    TextView textView = view.findViewById(R.id.tv_text);
                    if (pos == i) {
                        textView.setTextColor(ContextCompat.getColor(mActivity, R.color.text_strong));
                    } else {
                        textView.setTextColor(ContextCompat.getColor(mActivity, R.color.text_weak));
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        mBinding.vpMain.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                tabLayout.getTabAt(position).select();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private View getTab(@StringRes int name, int iconID) {
        View tabView = LayoutInflater.from(this).inflate(R.layout.layout_item_tab, null);
        TextView tv = tabView.findViewById(R.id.tv_text);
        tv.setText(name);
        ImageView im = tabView.findViewById(R.id.iv_icon);
        im.setImageResource(iconID);
        return tabView;
    }
}
