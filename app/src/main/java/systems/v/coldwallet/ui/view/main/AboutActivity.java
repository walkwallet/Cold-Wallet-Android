package systems.v.coldwallet.ui.view.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.databinding.DataBindingUtil;
import systems.v.coldwallet.BuildConfig;
import systems.v.coldwallet.R;
import systems.v.coldwallet.databinding.ActivityAboutBinding;
import systems.v.coldwallet.ui.BaseActivity;

public class AboutActivity extends BaseActivity implements View.OnClickListener {
    public static void launch(Activity from) {
        Intent intent = new Intent(from, AboutActivity.class);
        from.startActivity(intent);
        openAnimHorizontal(from);
    }

    private ActivityAboutBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_about);
        mBinding.setClick(this);
        mBinding.tvVersion.setText(getString(R.string.version, BuildConfig.VERSION_NAME));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
        }
    }

    @Override
    public void finish() {
        super.finish();
        closeAnimHorizontal(mActivity);
    }
}
