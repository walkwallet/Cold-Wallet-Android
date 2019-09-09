package systems.v.coldwallet.ui.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.alibaba.fastjson.JSON;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import androidx.annotation.StringRes;
import androidx.databinding.DataBindingUtil;
import systems.v.coldwallet.R;
import systems.v.coldwallet.databinding.ActivityConfirmTxBinding;
import systems.v.coldwallet.databinding.ItemTxInfoBinding;
import systems.v.coldwallet.ui.BaseActivity;
import systems.v.coldwallet.utils.UIUtil;
import systems.v.wallet.basic.utils.Base58;
import systems.v.wallet.basic.utils.CoinUtil;
import systems.v.wallet.basic.utils.QRCodeUtil;
import systems.v.wallet.basic.utils.TxUtil;
import systems.v.wallet.basic.wallet.Account;
import systems.v.wallet.basic.wallet.Transaction;
import vsys.Contract;

public class ConfirmTxActivity extends BaseActivity {

    public static void launch(Activity from, Transaction tx) {
        Intent intent = new Intent(from, ConfirmTxActivity.class);
        intent.putExtra("tx", JSON.toJSONString(tx));
        from.startActivity(intent);
    }

    private static final String TAG = "ConfirmTxActivity";

    private ActivityConfirmTxBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_confirm_tx);

        String txStr = getIntent().getStringExtra("tx");
        final Transaction tx = JSON.parseObject(txStr, Transaction.class);
        final Account sender;
        if(tx.getSenderPublicKey() != null && !tx.getSenderPublicKey().isEmpty()){
            sender = mApp.getWallet().getAccount(tx.getSenderPublicKey());
        }else{
            sender = mApp.getWallet().getAccountByAddress(tx.getAddress());
        }

        initView(tx, sender);
        mBinding.tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tx.sign(sender);
                String signature = tx.getSignature();
                String msg = QRCodeUtil.getSignatureStr(signature);
                UIUtil.showSignatureDialog(mActivity, msg);
            }
        });
        mBinding.ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initView(Transaction tx, Account sender) {
        mBinding.llInfo.removeAllViews();
        int type = tx.getTransactionType();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
        String fee = CoinUtil.formatWithUnit(tx.getFee());
        String time = dateFormat.format(new Date(tx.getTimestamp()));
        switch (type) {
            case Transaction.CONTRACT_REGISTER:
                addItem(R.string.confirmtx_item_my_address, sender.getAddress());
                addItem(R.string.confirmtx_item_type, TxUtil.getTypeText(this, type));
                addItem(R.string.confirmtx_item_time, String.format("%s (%s)", time, TimeZone.getDefault().getDisplayName()));
                addItem(R.string.confirmtx_item_fee, fee);
                if (!TextUtils.isEmpty(tx.getDescription())) {
                    addItem(R.string.confirmtx_item_attachment, tx.getDescription());
                }
                addItem(R.string.confirmtx_item_explain, tx.getContractInitExplain());
                break;
            case Transaction.CONTRACT_EXECUTE:
                addItem(R.string.confirmtx_item_my_address, sender.getAddress());
                addItem(R.string.confirmtx_item_type, TxUtil.getTypeText(this, type));
                addItem(R.string.confirmtx_item_time, String.format("%s (%s)", time, TimeZone.getDefault().getDisplayName()));
                addItem(R.string.confirmtx_item_fee, fee);
                addItem(R.string.confirmtx_item_contract_id, tx.getContractId());
                addItem(R.string.confirmtx_item_function_id, Short.toString(tx.getFunctionId()));
                if (!TextUtils.isEmpty(tx.getAttachment())) {
                    addItem(R.string.confirmtx_item_attachment, tx.getAttachment());
                }
                addItem(R.string.confirmtx_item_explain, tx.getFunctionExplain());
                break;
            default:
                addItem(R.string.confirmtx_item_my_address, sender.getAddress());

                int titleId = type == Transaction.PAYMENT
                        ? R.string.confirmtx_item_send_to : R.string.confirmtx_item_lease_to;
                addItem(titleId, tx.getRecipient());

                addItem(R.string.confirmtx_item_type, TxUtil.getTypeText(this, type));

                if (type == Transaction.PAYMENT || type == Transaction.LEASE) {
                    String amount = CoinUtil.formatWithUnit(tx.getAmount());
                    addItem(R.string.confirmtx_item_amount, amount);
                }
                addItem(R.string.confirmtx_item_fee, fee);
                addItem(R.string.confirmtx_item_time, String.format("%s (%s)", time, TimeZone.getDefault().getDisplayName()));
                if (type == Transaction.PAYMENT && !TextUtils.isEmpty(tx.getAttachment())) {
                    addItem(R.string.confirmtx_item_attachment, tx.getAttachment());
                }
        }
    }

    private ItemTxInfoBinding addItem(@StringRes int resId, String text) {
        ItemTxInfoBinding binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.item_tx_info, null, false);
        binding.tvTitle.setText(resId);
        binding.tvText.setText(text);
        mBinding.llInfo.addView(binding.getRoot());
        return binding;
    }
}
