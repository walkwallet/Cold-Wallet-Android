package systems.v.coldwallet.ui.view.wallet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.databinding.DataBindingUtil;
import systems.v.coldwallet.R;
import systems.v.coldwallet.databinding.ActivityGenerateSeedBinding;
import systems.v.coldwallet.ui.BaseActivity;
import systems.v.coldwallet.utils.UIUtil;
import systems.v.wallet.basic.utils.QRCodeUtil;

public class GenerateSeedActivity extends BaseActivity {

    public static void launch(Activity from, String seed) {
        Intent intent = new Intent(from, GenerateSeedActivity.class);
        intent.putExtra("seed", seed);
        from.startActivity(intent);
        openAnimHorizontal(from);
    }

    private ActivityGenerateSeedBinding mBinding;
    private String mSeeds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_generate_seed);
        mSeeds = getIntent().getStringExtra("seed");
        initData();
        initListener();
    }

    @Override
    public void finish() {
        super.finish();
        closeAnimHorizontal(mActivity);
    }

    private void initData() {
        mBinding.tvWord.setText(formatSeed(mSeeds));
    }

    private void initListener() {
        mBinding.tvNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConfirmSeedActivity.launch(mActivity, mSeeds);
            }
        });
        mBinding.tvExportQrcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIUtil.showQRCodeDialog(GenerateSeedActivity.this, QRCodeUtil.getSeedStr(mSeeds), 0, 0);
            }
        });
        mBinding.ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private String formatSeed(String seed) {
        String[] words = seed.split(" ");
        StringBuilder formattedSeed = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            formattedSeed.append(words[i]).append("    ");
        }
        return formattedSeed.toString();
    }
}
