package kunpenmg.com.suishoucha;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class FloatingWindowService extends Service {

	public static final String OPERATION = "operation";
	public static final int OPERATION_SHOW = 100;
	public static final int OPERATION_HIDE = 101;

	private boolean isAdded = false; // 是否已增加悬浮窗
	
	private static WindowManager wm;
	
	private static WindowManager.LayoutParams params;
	
	private View floatView;

	private float startX = 0;
	
	private float startY = 0;
	
	private float x;
	
	private float y;
	
	private String copyValue;
	private Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
				case 0:
					if(isAdded==true) {
						wm.removeView(floatView);
						isAdded = false;
					}
					break;
				default:
					break;
				}
		}
	};
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		createFloatView();		
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		if (intent != null) {
			int operation = intent.getIntExtra(OPERATION, OPERATION_SHOW);
			switch (operation) {
			case OPERATION_SHOW:
				if (!isAdded) {
					wm.addView(floatView, params);
					new Thread(new Runnable() {

						public void run() {

							try {
								Thread.sleep(5000);
								Message msg = new Message();
								msg.what = 0;
								handler.sendMessage(msg);

							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}

					}).start();
					isAdded = true;
				}
					break;
				case OPERATION_HIDE:
				if (isAdded) {
					wm.removeView(floatView);
					isAdded = false;
				}
				break;
			}
			copyValue = intent.getStringExtra("copyValue");
			setupCellView(floatView);
			Log.e(this.getClass().getSimpleName(), "=====copyValue :" + copyValue);
		}
	}
	
	/**
	 * 创建悬浮窗
	 */
	private void createFloatView() {
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		floatView = layoutInflater.inflate(R.layout.dict_popup_window, null);
		
		wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		params = new WindowManager.LayoutParams();

		// 设置window type
		params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
		
		/*
		 * 如果设置为params.type = WindowManager.LayoutParams.TYPE_PHONE; 那么优先级会降低一些,
		 * 即拉下通知栏不可见
		 */
		params.format = PixelFormat.RGBA_8888; // 设置图片格式，效果为背景透明

		// 设置Window flag
		params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
				| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		/*
		 * 下面的flags属性的效果形同“锁定”。 悬浮窗不可触摸，不接受任何事件,同时不影响后面的事件响应。
		 * wmParams.flags=LayoutParams.FLAG_NOT_TOUCH_MODAL |
		 * LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_NOT_TOUCHABLE;
		 */

		// 设置悬浮窗的长得宽
		params.width = getResources().getDimensionPixelSize(R.dimen.float_width);
		params.height = WindowManager.LayoutParams.WRAP_CONTENT;

		params.gravity = Gravity.LEFT | Gravity.TOP;
		params.x = 0;
		params.y = 0;
		
		// 设置悬浮窗的Touch监听
		floatView.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				x = event.getRawX();
				y = event.getRawY();
				
				switch(event.getAction()){
				case MotionEvent.ACTION_DOWN:
					startX = event.getX();
					startY = event.getY();
					break;
				case MotionEvent.ACTION_MOVE:
					params.x = (int)( x - startX);  
					params.y = (int) (y - startY);  
					wm.updateViewLayout(floatView, params);
					break;
				case MotionEvent.ACTION_UP:
					startX = startY = 0;  
					break;
				}
				return true;
			}
		});
		
		wm.addView(floatView, params);
		new Thread(new Runnable() {

			public void run() {

				try {
					Thread.sleep(5000);
					Message msg = new Message();
					msg.what = 0;
					handler.sendMessage(msg);

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}).start();
		isAdded = true;
	}

	/**
	 * 设置浮窗view内部子控件
	 * @param rootview
	 */
	private void setupCellView(View rootview) {
		ImageView closedImg = (ImageView) rootview.findViewById(R.id.float_window_closed);
		TextView titleText = (TextView) rootview.findViewById(R.id.float_window_title);
		titleText.setText(copyValue);		
		closedImg.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (isAdded) {
					wm.removeView(floatView);
					isAdded = false;
				}
			}
		});
		floatView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

			}
		});
	}


}
