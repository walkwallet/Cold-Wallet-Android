package systems.v.coldwallet.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.alibaba.fastjson.JSON;

import androidx.annotation.Nullable;
import systems.v.wallet.basic.utils.FileUtil;
import systems.v.wallet.basic.wallet.Wallet;

public class BackupService extends IntentService {

    private static final String TAG = "BackupService";

    public static void start(Context context, Wallet wallet) {
        Intent intent = new Intent(context, BackupService.class);
        intent.putExtra("data", JSON.toJSONString(wallet));
        context.startService(intent);
    }

    public BackupService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            String walletStr = intent.getStringExtra("data");
            Wallet wallet = JSON.parseObject(walletStr, Wallet.class);
            if (wallet != null) {
                FileUtil.backup(wallet);
            }
        }
    }
}
