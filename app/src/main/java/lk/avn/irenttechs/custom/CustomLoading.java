package lk.avn.irenttechs.custom;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;

import lk.avn.irenttechs.R;

public class CustomLoading {
    private Dialog loadingDialog;

    public CustomLoading(Context context) {
        loadingDialog = new Dialog(context);
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.setCancelable(false);
        loadingDialog.setContentView(R.layout.custom_loading_layout);
        loadingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    public void show() {
        if (loadingDialog != null && !loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }

    public void dismiss() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
}
