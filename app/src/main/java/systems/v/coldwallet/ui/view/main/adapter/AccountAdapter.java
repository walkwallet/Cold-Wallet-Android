package systems.v.coldwallet.ui.view.main.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import systems.v.coldwallet.R;
import systems.v.coldwallet.utils.UIUtil;
import systems.v.wallet.basic.AlertDialog;
import systems.v.wallet.basic.utils.QRCodeUtil;
import systems.v.wallet.basic.wallet.Account;


public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountHolder> {
    private Context mContext;
    private List<Account> mData = new ArrayList<>();
    private onAddClickListener mListener;

    public AccountAdapter(Context context, List<Account> data) {
        this.mContext = context;
        this.mData = data;
    }

    private int[] mDrawables = new int[]{R.drawable.bg_circle_1_orange, R.drawable.bg_circle_2_yellow,
            R.drawable.bg_circle_3_purple, R.drawable.bg_circle_4_blue, R.drawable.bg_circle_5_green,
            R.drawable.bg_circle_6_purple, R.drawable.bg_circle_7_blue, R.drawable.bg_circle_8_green};

    @Override
    @NonNull
    public AccountHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AccountHolder(LayoutInflater.from(mContext).inflate(R.layout.item_account, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AccountHolder holder, final int position) {
        if (position == mData.size() - 1) {
            holder.flAccount.setVisibility(View.GONE);
            holder.llAdd.setVisibility(View.VISIBLE);
            holder.llAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.BasicAlertDialog_Dark);
                        builder.setTitle(R.string.home_add_account_dialog_title)
                                .setMessage(R.string.home_add_account_dialog_msg)
                                .setIcon(R.drawable.basic_ico_alert)
                                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mListener.onAddClick();
                                    }
                                }).autoDismiss().confirm();
                    }
                }
            });
            return;
        }
        holder.flAccount.setVisibility(View.VISIBLE);
        holder.llAdd.setVisibility(View.GONE);
        holder.tvAccount.setText(mContext.getString(R.string.wallet_number, position + 1));
        holder.circle.setBackgroundResource(getIndexDrawable(position));
        holder.tvIndex.setText(position + 1 + "");
        holder.tvAddress.setText(UIUtil.getMutatedAddress(mData.get(position).getAddress()));
        holder.flAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mData.get(position) != null) {
                    final Dialog dialog = new Dialog(mContext, R.style.Dialog);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.dialog_qr_code);
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
                    TextView address = dialog.findViewById(R.id.tv_address);
                    TextView title = dialog.findViewById(R.id.tv_account);
                    ImageView ivClose = dialog.findViewById(R.id.iv_close);
                    TextView tvCircle = dialog.findViewById(R.id.tv_circle);
                    TextView tvIndex = dialog.findViewById(R.id.tv_index);
                    tvIndex.setText(position + 1 + "");
                    tvCircle.setBackgroundResource(getIndexDrawable(position));
                    String msg = QRCodeUtil.getExportAddressStr(mData.get(position).getAddress(), mData.get(position).getPublicKey());
                    qrCode.setImageBitmap(QRCodeUtil.generateQRCode(msg, 800));
                    address.setText(mData.get(position).getAddress());
                    title.setText(mContext.getString(R.string.wallet_address, position + 1));
                    ivClose.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    static class AccountHolder extends RecyclerView.ViewHolder {
        TextView tvAccount;
        TextView tvAddress;
        ImageView ivQrCode;
        TextView circle;
        TextView tvIndex;
        FrameLayout flAccount;
        LinearLayout llAdd;

        public AccountHolder(View itemView) {
            super(itemView);
            tvAccount = itemView.findViewById(R.id.tv_account);
            tvAddress = itemView.findViewById(R.id.tv_address);
            ivQrCode = itemView.findViewById(R.id.iv_qr_code);
            tvIndex = itemView.findViewById(R.id.tv_index);
            circle = itemView.findViewById(R.id.circle);
            flAccount = itemView.findViewById(R.id.fl_account);
            llAdd = itemView.findViewById(R.id.ll_add_more_account);
        }
    }

    private int getIndexDrawable(int index) {
        int pos = index % mDrawables.length;
        return mDrawables[pos];
    }

    public interface onAddClickListener {
        void onAddClick();
    }

    public void setOnAddClickListener(onAddClickListener listener) {
        mListener = listener;
    }
}
