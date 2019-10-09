package com.ys.gtupdatezip;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RecoverySystem;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.Formatter;
import java.util.Locale;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static StringBuilder sFormatBuilder = new StringBuilder();
    private static String filePath;
    private boolean showDialog;
    private static Formatter sFormatter = new Formatter(sFormatBuilder, Locale.getDefault());

    private CircleProgressView mCircleProgressView;
    private LinearLayout btnLayout;
    private RelativeLayout layout;
    private TextView title;
    private Button confirm;
    private Button cancel;

    private int currentProgress = 0;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            if (msg.what == 1) {
                sendEmptyMessageDelayed(2, 100);
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        boolean isZipOk = doesOtaPackageMatchProduct(filePath);
                        if (isZipOk) {
                            mCircleProgressView.setText(getResources().getString(R.string.updating));

                            sendEmptyMessageDelayed(3,500);
                        }else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mCircleProgressView.setVisibility(View.GONE);
                                    title.setVisibility(View.VISIBLE);
                                    btnLayout.setVisibility(View.VISIBLE);
                                    confirm.setVisibility(View.GONE);
                                    cancel.setText(getString(R.string.confirm));
                                    layout.setBackground(getDrawable(R.drawable.dialog_background));
                                    title.setText(getString(R.string.checkout_zip_fail));

                                    cancel.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            finish();
                                        }
                                    });
                                }
                            });
                        }
                    }
                }.start();
            } else if (msg.what == 2) {
                if (currentProgress <= 100) {
                    mCircleProgressView.setCurrentProgress(currentProgress);
                    currentProgress++;
                } else
                    currentProgress = 0;
                sendEmptyMessageDelayed(2, 100);
            } else if (msg.what == 3) {
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            GTRecoverySystem.installPackage(MainActivity.this,new File(filePath));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCircleProgressView = findViewById(R.id.circle_view);
        btnLayout = findViewById(R.id.btn_layout);
        layout = findViewById(R.id.layout);


        IntentFilter filter = new IntentFilter("android.intent.action.MEDIA_UNMOUNTED");
        filter.addDataScheme("file");
        registerReceiver(unMountedReceiver, filter);

        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        getWindow().setGravity(Gravity.CENTER);

        filePath = getIntent().getStringExtra("binpath");
        showDialog = getIntent().getBooleanExtra("showDialog",true);

        title = (TextView) findViewById(R.id.dialog_title);
        confirm = findViewById(R.id.confirm);
        cancel = findViewById(R.id.cancel);
        confirm.setPressed(true);

        String messageFormat = getString(R.string.updating_message_formate);
        sFormatBuilder.setLength(0);
        sFormatter.format(messageFormat, filePath);
        title.setText(sFormatBuilder.toString());

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (filePath != null && !"".equals(filePath))
                    beginToUpdate();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (!showDialog)
            confirm.performClick();
    }


    private BroadcastReceiver unMountedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("sky", "action = " + intent.getAction());
            if ("android.intent.action.MEDIA_UNMOUNTED".equals(intent.getAction())) {
                finish();
            }
        }
    };

    @Override
    protected void onDestroy() {
        unregisterReceiver(unMountedReceiver);
        super.onDestroy();
    }

    private void beginToUpdate() {
        mCircleProgressView.setVisibility(View.VISIBLE);
        title.setVisibility(View.GONE);
        btnLayout.setVisibility(View.GONE);
        layout.setBackground(getDrawable(R.drawable.dialog_background_tran));
        mCircleProgressView.setText(getResources().getString(R.string.checking));


        mHandler.sendEmptyMessageDelayed(1, 500);
    }



    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getKeyCode() == KeyEvent.KEYCODE_BACK ) {
            //do something.
            return true;
        }else {
            return super.dispatchKeyEvent(event);
        }
    }

    public boolean doesOtaPackageMatchProduct(String imagePath) {
        LOG("doesImageMatchProduct(): start verify package , imagePath = " + imagePath);

        try{
            RecoverySystem.verifyPackage(new File(imagePath), null, null);
        }catch(GeneralSecurityException e){
            LOG("doesImageMatchProduct(): verifaPackage faild!");
            return false;
        }catch(IOException exc) {
            LOG("doesImageMatchProduct(): verifaPackage faild!");
            return false;
        }
        return true;
    }

    private static void LOG(String msg) {
        Log.d(TAG, msg);
    }
}
