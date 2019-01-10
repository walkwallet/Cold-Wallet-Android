package systems.v.coldwallet.utils;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Objects;

import androidx.appcompat.app.AlertDialog;
import systems.v.coldwallet.R;
import systems.v.wallet.basic.utils.QRCodeUtil;

public class UIUtil {
    public static void showUnsupportQrCodeDialog(final Activity activity) {
        new AlertDialog.Builder(activity)
                .setMessage(R.string.unsupported_qrcode)
                .setPositiveButton(R.string.confirm, null)
                .show();
    }

    public static void showSenderNotFoundDialog(final Activity activity) {
        new AlertDialog.Builder(activity)
                .setMessage(R.string.sender_not_found)
                .setPositiveButton(R.string.confirm, null)
                .show();
    }

    public static void showSignatureDialog(final Activity activity, String msg) {
        final Dialog dialog = new Dialog(activity, R.style.Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_signature);
        dialog.setCancelable(true);
        Window window = dialog.getWindow();
        window.setGravity(Gravity.CENTER);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.getDecorView().setPadding(0, 0, 0, 0);
        window.setAttributes(params);
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        ImageView qrCode = dialog.findViewById(R.id.iv_qr_code);
        ImageView ivClose = dialog.findViewById(R.id.iv_close);
        TextView tvComplete = dialog.findViewById(R.id.tv_complete);
        qrCode.setImageBitmap(QRCodeUtil.generateQRCode(msg, 800));
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        tvComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.finish();
            }
        });
        dialog.show();
    }

    public static String getMutatedAddress(String address) {
        if (TextUtils.isEmpty(address)) {
            return "";
        }
        String start, middle, end;
        int len = address.length();

        if (len > 6) {
            start = address.substring(0, 6);
            middle = "******";
            end = address.substring(len - 6, len);
            return start + middle + end;
        } else {
            return "";
        }
    }
}
