package com.ys.gtupdatezip;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;

public class NotifyDeleteActivity extends Activity {
    private static String TAG = "NotifyDeleteActivity";
    private Context mContext;
    private String mPath;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "bind rkupdateservice completed!");
        }

        public void onServiceDisconnected(ComponentName className) {
        }
    };

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestory.........");
        mContext.unbindService(mConnection);
        super.onDestroy();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.notify_dialog);
        //getWindow().setTitle("This is just a test");
        getWindow().addFlags(WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG);
        getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
                android.R.drawable.ic_dialog_alert);
        setFinishOnTouchOutside(false);
        Intent startIntent = getIntent();
        TextView text = (TextView) this.findViewById(R.id.notify);
        int flag = startIntent.getIntExtra("flag", 0);
        mPath = startIntent.getStringExtra("path");
        if (flag == GTUpdateService.UPDATE_SUCCESS) {
            text.setText(getString(R.string.update_success) + getString(R.string.ask_delete_package));
        } else if (flag == GTUpdateService.UPDATE_FAILED) {
            text.setText(getString(R.string.update_failed) + getString(R.string.ask_delete_package));
        }

        mContext.bindService(new Intent(mContext, GTUpdateService.class), mConnection, Context.BIND_AUTO_CREATE);

        Button btn_ok = (Button) this.findViewById(R.id.button_ok);
        Button btn_cancel = (Button) this.findViewById(R.id.button_cancel);
        btn_ok.setFocusable(false);
        btn_ok.setClickable(false);
        btn_cancel.setFocusable(false);
        btn_cancel.setClickable(false);

        btn_ok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                deletePackage(mPath);
                finish();

            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void deletePackage(String path) {
        LOG("try to deletePackage...");
        if (path.startsWith("@")) {
            String fileName = "/data/update.zip";
            LOG("ota was maped, so try to delete path = " + path);
            File f_ota = new File(fileName);
            if (f_ota.exists()) {
                f_ota.delete();
                LOG("delete complete! path=" + fileName);
            }
        }

        File f = new File(path);
        if (f.exists()) {
            f.delete();
            LOG("path= complete! path=" + path);
        } else {
            LOG("path=" + path + " ,file not exists!");
        }
    }

    private static void LOG(String msg) {
        Log.d(TAG, msg);
    }
}
