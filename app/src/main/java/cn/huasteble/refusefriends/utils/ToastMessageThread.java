package cn.huasteble.refusefriends.utils;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;

import cn.huasteble.refusefriends.MainActivity;


/**
 * @author teble
 * @date 2019/9/1 17:20
 */
public class ToastMessageThread extends Thread {
    private Handler handler;

    @Override
    @SuppressLint("HandlerLeak")
    public void run() {
        Looper.prepare();
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Toast.makeText(MainActivity.getContext(), JSON.toJSONString(msg.obj), Toast.LENGTH_SHORT).show();
            }
        };
        Looper.loop();
    }

    public void sendMessage(Object msg) {
        Message message = new Message();
        message.obj = msg;
        handler.sendMessage(message);
    }
}
