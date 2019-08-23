package info.bcdev.librarysdkew.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class ToastMsg {

    public void Short(Context context, String msg){
        Log.e("ToastMsg", "Short: " + msg);
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public void Long(Context context, String msg){
        Log.e("ToastMsg", "Long: " + msg);
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }
}
