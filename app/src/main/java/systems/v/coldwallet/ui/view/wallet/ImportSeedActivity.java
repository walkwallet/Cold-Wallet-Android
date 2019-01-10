package systems.v.coldwallet.ui.view.wallet;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import systems.v.coldwallet.R;
import systems.v.coldwallet.databinding.ActivityImportSeedBinding;
import systems.v.coldwallet.ui.BaseActivity;
import systems.v.coldwallet.ui.view.ScannerActivity;
import systems.v.coldwallet.utils.UIUtil;
import systems.v.wallet.basic.AlertDialog;
import systems.v.wallet.basic.wallet.Operation;
import systems.v.wallet.basic.wallet.Wallet;

public class ImportSeedActivity extends BaseActivity {
    public static void launch(Activity from) {
        Intent intent = new Intent(from, ImportSeedActivity.class);
        from.startActivity(intent);
        openAnimHorizontal(from);
    }

    private static final String TAG = "ImportSeedActivity";

    private ActivityImportSeedBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_import_seed);
        initView();
        initListener();
    }

    @Override
    public void finish() {
        super.finish();
        closeAnimHorizontal(mActivity);
    }

    private void initView() {
        mBinding.tvNext.setEnabled(false);
    }

    private void initListener() {
        mBinding.ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mBinding.etWord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String word = s.toString();
                if (TextUtils.isEmpty(word)) {
                    mBinding.tvNext.setEnabled(false);
                    mBinding.tvNext.setBackground(ContextCompat.getDrawable(mActivity, R.drawable.bg_unable_no_stroke_6));
                    mBinding.tvNext.setTextColor(ContextCompat.getColor(mActivity, R.color.text_unable));
                } else {
                    mBinding.tvNext.setEnabled(true);
                    mBinding.tvNext.setBackground(ContextCompat.getDrawable(mActivity, R.drawable.bg_orange_radius_6));
                    mBinding.tvNext.setTextColor(ContextCompat.getColor(mActivity, R.color.text_strong));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mBinding.tvNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String word = mBinding.etWord.getText().toString();
                if (!Wallet.validateSeedPhrase(word)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.BasicAlertDialog_Dark);
                    builder.setTitle(R.string.not_office_seed_title)
                            .setMessage(R.string.not_office_seed_tips)
                            .setIcon(R.drawable.basic_ico_alert)
                            .setPositiveButton(R.string.text_continue, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    SetPasswordActivity.launch(mActivity, word);
                                }
                            }).autoDismiss().confirm();
                } else {
                    SetPasswordActivity.launch(mActivity, word);
                }
            }
        });
        mBinding.ivScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScannerActivity.launch(ImportSeedActivity.this);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            String qrContents = result.getContents();
            Log.d(TAG, "scan result is " + qrContents);
            try {
                Operation op = Operation.parse(qrContents);
                if (op.validate(Operation.SEED)) {
                    String seed = (String) op.get("seed");
                    mBinding.etWord.setText(seed);
                }
            } catch (Exception e) {
                Log.e(TAG, "unsupported seed");
                UIUtil.showUnsupportQrCodeDialog(this);
            }
        }
    }
}
