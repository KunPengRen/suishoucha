package kunpenmg.com.suishoucha;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipData.Item;
import android.content.ClipboardManager;
import android.content.ClipboardManager.OnPrimaryClipChangedListener;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import  android.os.Handler;

import java.util.Scanner;


public class BackFanyi extends Service {
    public static final String OPERATION = "operation";
    public static final int OPERATION_SHOW = 100;
    public static final int OPERATION_HIDE = 101;
    private ClipBoardReceiver mBoardReceiver;
    private  Handler handler =new  Handler(){
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {
                case 0:
                    String word = msg.getData().getString("word");
                    Intent mIntent = new Intent();
                    mIntent.setAction("com.cybertron.dict.ClipBoardReceiver");
                    mIntent.putExtra("clipboardvalue", word);
                    sendBroadcast(mIntent);

                    Log.e(this.getClass().getSimpleName(), "========翻译结果:"+word);

                    break;

                default:
                    break;
            }
        }

    };
    private Runnable myRunnable;
    public BackFanyi() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public void onCreate() {
        super.onCreate();
        mBoardReceiver = new ClipBoardReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.cybertron.dict.ClipBoardReceiver");
        registerReceiver(mBoardReceiver, filter);

        final ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        cm.addPrimaryClipChangedListener(new OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {


                ClipData data = cm.getPrimaryClip();
                Item item = data.getItemAt(0);
                String reslut = item.getText().toString();
                ClipThread(reslut);



            }
        });

    }


            @Override
            public void onDestroy() {
                super.onDestroy();
            }

            public String Fanyi(String s) {
                return MainActivity.getMainActivity().BaiduApi(s,MainActivity.BaiduFanyi);
            }

            public void ClipThread(final String s) {
                new Thread() {
                    public void run() {
                        String s1 =s;
                        FanyiEnTo(s1);
                    }
                }.start();
            }

            public void FanyiEnTo(String s) {
               // Looper.prepare();

                        Message msg = new Message();
                        msg.what = 0;
                        Bundle bun = new Bundle();
                        String s1 = Fanyi(s);
                        bun.putString("word", s1);
                        msg.setData(bun);
                        handler.sendMessage(msg);


            }
    private static View getRootView(Activity context)
    {
        return ((ViewGroup)context.findViewById(android.R.id.content)).getChildAt(0);
    }
    class ClipBoardReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if(bundle != null){
                String value = (String) bundle.get("clipboardvalue");
                Intent show = new Intent(BackFanyi.this, FloatingWindowService.class);
                show.putExtra(FloatingWindowService.OPERATION,FloatingWindowService.OPERATION_SHOW);
                show.putExtra("copyValue", value);
                BackFanyi.this.startService(show);
            }
        }
    }
}
