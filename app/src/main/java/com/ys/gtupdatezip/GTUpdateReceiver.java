package com.ys.gtupdatezip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class GTUpdateReceiver extends BroadcastReceiver {
    private static final String TAG = "GTUpdateReceiver";
    private static final String GTUPGRADE = "update.zip";
    private static final String OTG_AND_HOST_SWITCH = "SwitchOtgAndHost.txt";
    private Context context;
    private String path;
    SharedPreferences sharedPreferences;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        this.context = context;
        sharedPreferences = context.getSharedPreferences("UpdateZip",0);
        boolean isBootCompleted = sharedPreferences.getBoolean("bootComplete",false);
        Log.d(TAG, "action = " + action);
        Handler handler = new Handler();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            clearUpdateZip();
            handler.postDelayed(setisBootCompleted,1000);
        } else if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
            path = intent.getData().getPath();
            Log.d(TAG, "path = " + path);

            if (path != null ) {
                switchOtgAndHost(new File(path));
                if (isBootCompleted)
                    handler.postDelayed(findPath, 5000);
            }
        } else if ("android.intent.action.YS_UPDATE_FIRMWARE".equals(action)) {
            String path = intent.getStringExtra("img_path");

            if (!"".equals(path)) {
                Intent intent1 = new Intent(context, MainActivity.class);
                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent1.putExtra("binpath", path);
                intent1.putExtra("showDialog", false);
                context.startActivity(intent1);
            }

        }
    }

    private void clearUpdateZip() {
        String fileName = "/data/update.zip";
        File f_ota = new File(fileName);
        if(f_ota.exists())
            Log.d(TAG,"delete = " + f_ota.delete());
    }

    private Runnable findPath = new Runnable() {
        @Override
        public void run() {
            File paths = new File(path);
            findFile(paths);
        }
    };

    private void switchOtgAndHost(File file) {
        File[] files = file.listFiles();
        for (File a : files) {
            if (a.getAbsolutePath().contains(OTG_AND_HOST_SWITCH)) {
                String otgStatus = Utils.getValueFromProp("persist.sys.usb.otg.mode");
                Log.d(TAG,"otgStatus = " + otgStatus);
                if ("0".equals(otgStatus)) {
                    Utils.setValueToProp("persist.sys.usb.otg.mode", "1");
                    try {
                        Utils.writeIntFile("1", "/sys/devices/soc/soc:misc_power_en/otg");
                        SystemClock.sleep(1000);
                        Utils.writeIntFile("1", "/sys/devices/soc/soc:misc_power_en/otg_pwr");
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    Utils.setValueToProp("persist.sys.usb.otg.mode", "0");
                    try {
                        Utils.writeIntFile("0", "/sys/devices/soc/soc:misc_power_en/otg_pwr");
                        Utils.writeIntFile("0", "/sys/devices/soc/soc:misc_power_en/otg");
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    }

    public void findFile(File file) {
        File[] files = file.listFiles();
        for (File a : files) {
            if (a.getAbsolutePath().contains(GTUPGRADE)) {
                Intent intent = new Intent(context, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("binpath", a.getPath());
                intent.putExtra("showDialog",true);
                context.startActivity(intent);
            }
        }
    }

    private Runnable setisBootCompleted = new Runnable() {
        @Override
        public void run() {
            sharedPreferences.edit().putBoolean("bootComplete",true).apply();
        }
    };

}
