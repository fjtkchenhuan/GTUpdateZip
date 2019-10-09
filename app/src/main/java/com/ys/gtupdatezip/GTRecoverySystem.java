/*************************************************************************
	> File Name: GTRecoverySystem.java
	> Author: jkand.huang
	> Mail: jkand.huang@rock-chips.com
	> Created Time: Wed 02 Nov 2016 03:10:47 PM CST
 ************************************************************************/
package com.ys.gtupdatezip;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.RecoverySystem;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;


public class GTRecoverySystem  {
	private static final String TAG = "GTRecoverySystem";
	private static File RECOVERY_DIR = new File("/cache/recovery");
	private static File UPDATE_FLAG_FILE = new File(RECOVERY_DIR, "last_flag");


	public static void installPackage(Context context, File packageFile)throws IOException {
		SharedPreferences sharedPreferences = context.getSharedPreferences("UpdateZip",0);
		sharedPreferences.edit().putBoolean("bootComplete",false).apply();
        String filename = packageFile.getCanonicalPath();
//		writeFlagCommand(filename);
        RecoverySystem.installPackage(context, packageFile);

		String[] commands;
		commands = new String[4];
		commands[0] = "cp  " + filename + " /data/";
		commands[1] = "touch /cache/recovery/command";
		commands[2] = "echo \"--update_package=/data/update.zip\" > /cache/recovery/command";
		commands[3] = "reboot recovery";
//		for (String command : commands)
//			execForRoot(command);
	}

	public static String readFlagCommand() {
		if(UPDATE_FLAG_FILE.exists()) {
			Log.d(TAG, "UPDATE_FLAG_FILE is exists");
			char[] buf = new char[128];
			int readCount = 0;;
			try {
				FileReader reader = new FileReader(UPDATE_FLAG_FILE);
				readCount = reader.read(buf, 0, buf.length);
				Log.d(TAG, "readCount = " + readCount + " buf.length = " + buf.length);
			}catch (IOException e) {
				Log.e(TAG, "can not read /cache/recovery/last_flag!");
			}finally {
				UPDATE_FLAG_FILE.delete();
				
			}
			
			StringBuilder sBuilder = new StringBuilder();
			for(int i = 0; i < readCount; i++) {
				if(buf[i] == 0) {
					break;
				}
				sBuilder.append(buf[i]);	
			}
			return sBuilder.toString();
		}else {
			return null;
		}
	}
	
	public static void writeFlagCommand(String path) throws IOException {
		RECOVERY_DIR.mkdirs();
		UPDATE_FLAG_FILE.delete();
		FileWriter writer = new FileWriter(UPDATE_FLAG_FILE);
		try {
			writer.write("updating$path=" + path);
		}finally {
			writer.close();
		}
	}

	public static boolean execForRoot(String command) {
		Log.d("execFor7", "command = " + command);
		boolean result = false;
		DataOutputStream dataOutputStream = null;
		BufferedReader errorStream = null;
		try {
			// 申请su权限
			Process process = Runtime.getRuntime().exec("su");
			dataOutputStream = new DataOutputStream(process.getOutputStream());
			// 执行pm install命令
			String s = command + "\n";
			dataOutputStream.write(s.getBytes(Charset.forName("utf-8")));
			dataOutputStream.flush();
			dataOutputStream.writeBytes("exit\n");
			dataOutputStream.flush();
			process.waitFor();
			errorStream = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			String msg = "";
			String line;
			// 读取命令的执行结果
			while ((line = errorStream.readLine()) != null) {
				msg += line;
			}
			Log.d("execFor7", "execFor7 msg is " + msg);
			// 如果执行结果中包含Failure字样就认为是安装失败，否则就认为安装成功
			if (!msg.contains("Failure")) {
				result = true;
			}
		} catch (Exception e) {
			Log.e("execFor7", e.getMessage(), e);
		} finally {
			try {
				if (dataOutputStream != null) {
					dataOutputStream.close();
				}
				if (errorStream != null) {
					errorStream.close();
				}
			} catch (IOException e) {
				Log.e("TAG", e.getMessage(), e);
			}
		}
		return result;
	}
}
