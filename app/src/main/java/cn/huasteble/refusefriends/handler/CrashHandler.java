package cn.huasteble.refusefriends.handler;

import android.annotation.SuppressLint;
import android.content.Context;

import java.util.Properties;

/**
 * @author teble
 * @date 2019/9/1 15:23
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {
    public static final boolean DEBUG = true;
    @SuppressLint("StaticFieldLeak")
    private static CrashHandler INSTANCE;
    private static final String TAG = "CrashHandler";
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    private Properties mDeviceCrashInfo = new Properties();
    private static final Object SYNC_ROOT = new Object();

    private CrashHandler() {}

    public static CrashHandler getInstance() {
        if (INSTANCE == null) {
            synchronized (SYNC_ROOT) {
                if(INSTANCE == null) {
                    INSTANCE = new CrashHandler();
                }
            }
        }
        return INSTANCE;
    }

    public void init() {
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        mDefaultHandler.uncaughtException(t, e);
    }
}
