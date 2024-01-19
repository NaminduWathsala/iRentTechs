package lk.avn.irenttechs.custom;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import lk.avn.irenttechs.LoginActivity;
import lk.avn.irenttechs.R;

public class Logout {
    private static String errorName;
    public static void logout(Context context) {

            SharedPreferences preferences = context.getSharedPreferences("AuthActivity", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(context, LoginActivity.class);
            context.startActivity(intent);

    }

}
