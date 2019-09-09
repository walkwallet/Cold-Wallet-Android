package systems.v.coldwallet.ui.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.List;

import androidx.databinding.DataBindingUtil;
import systems.v.coldwallet.R;
import systems.v.coldwallet.databinding.ActivityPageScanBinding;
import systems.v.coldwallet.utils.UIUtil;
import systems.v.wallet.basic.utils.QRCodeUtil;

public class PageScanActivity extends Activity implements View.OnClickListener{
    public static final int REQUEST_CODE = 0x0000aadf;
    public static final String RESULT = "SCAN_RESULT";

    public static void launch(Activity from, String firstSeg) {
        Intent intent = new Intent(from, PageScanActivity.class);
        intent.putExtra("Seg", firstSeg);
        from.startActivityForResult(intent, REQUEST_CODE);
    }

    private ActivityPageScanBinding mBinding;
    private List<QRCodeUtil.QRCode> mQrCodes = new ArrayList<>();
    private int current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_page_scan);
        String segStr = getIntent().getStringExtra("Seg");

        QRCodeUtil.QRCode qrcode = QRCodeUtil.parsePageMessage(segStr);
        if (qrcode == null){
            UIUtil.showUnsupportQrCodeDialog(this);
            return ;
        }
        setSegment(qrcode);
        initView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_CANCELED){
            return;
        }
        if(data == null){
            UIUtil.showUnsupportQrCodeDialog(PageScanActivity.this);
            return ;
        }
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null && result.getContents() != null) {
            String qrContents = result.getContents();
            if (!qrContents.startsWith(QRCodeUtil.Prefix)){
                UIUtil.showUnsupportQrCodeDialog(this);
                return ;
            }

            QRCodeUtil.QRCode qrCode = QRCodeUtil.parsePageMessage(qrContents);
            if (qrCode == null || !qrCode.getChecksum().equals(mQrCodes.get(0).getChecksum()) ||
                    qrCode.getCurrent() - 1 != current){
                UIUtil.showWrongQrCodeDialog(this);
                return ;
            }

            setSegment(qrCode);
        }
    }

    private void initView(){
        mBinding.setClick(this);
        if(mQrCodes.size() > 0) {
            mBinding.setCurrent(mQrCodes.get(0).getCurrent());
            mBinding.setTotal(mQrCodes.get(0).getTotal());
        }
    }

    private void setSegment(QRCodeUtil.QRCode qrCode){
        if(mQrCodes.size() > current){
            mQrCodes.set(current, qrCode);
        }else{
            mQrCodes.add(current, qrCode);
        }
        current = qrCode.getCurrent();
        mBinding.setCurrent(current);
    }

    public void onNext(){
        Intent intent = new Intent();
        intent.putExtra(RESULT, QRCodeUtil.getBodyConcatStr(mQrCodes));
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onCancel(){
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_continue:
                if (current < mBinding.getTotal()){
                    ScannerActivity.launch(PageScanActivity.this);
                }else{
                    onNext();
                }
                break;
            case R.id.tv_back:
                if(current > 1){
                    mBinding.setCurrent(--current);
                }else{
                    onCancel();
                }
                break;
            case R.id.iv_close:
                onCancel();
                break;
        }

    }
}
