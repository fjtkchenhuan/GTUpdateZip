package com.ys.gtupdatezip;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class GTUpdateService extends Service {
    private static final String TAG = "GTUpdateService";
    private volatile boolean mIsFirstStartUp = true;
    private static final String COMMAND_FLAG_SUCCESS = "success";
    private static final String COMMAND_FLAG_UPDATING = "updating";

    public static final int UPDATE_SUCCESS = 1;
    public static final int UPDATE_FAILED = 2;
    private Context mContext;
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.mContext = this;
        sharedPreferences = getSharedPreferences("UpdateZip",0);
        if (mIsFirstStartUp) {
            LOG("first startup!!!");
            mIsFirstStartUp = false;
            String command = GTRecoverySystem.readFlagCommand();
            String path;
            if (command != null) {
                LOG("command = " + command);
                if (command.contains("$path")) {
                    sharedPreferences.edit().putBoolean("bootFromUpdate",true).apply();
                    path = command.substring(command.indexOf('=') + 1);
                    LOG("last_flag: path = " + path);

                    if (command.startsWith(COMMAND_FLAG_SUCCESS)) {
                        LOG("now try to start notifydialog activity!");
                        Intent intent = new Intent(mContext, NotifyDeleteActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("flag", UPDATE_SUCCESS);
                        intent.putExtra("path", path);
                        startActivity(intent);
                        return;
                    }
                    if (command.startsWith(COMMAND_FLAG_UPDATING)) {
                        Intent intent = new Intent(mContext, NotifyDeleteActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("flag", UPDATE_FAILED);
                        intent.putExtra("path", path);
                        startActivity(intent);
                        return;
                    }
                }
            }
        }

    }

    private static void LOG(String msg) {
        Log.d(TAG, msg);
    }
}
