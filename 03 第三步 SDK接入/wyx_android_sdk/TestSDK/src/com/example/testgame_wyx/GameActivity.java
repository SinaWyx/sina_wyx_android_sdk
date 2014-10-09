package com.example.testgame_wyx;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import cn.sina.youxi.pay.sdk.Wyx;
import cn.sina.youxi.pay.sdk.WyxAuthListener;
import cn.sina.youxi.util.ResponseListener;

/**
 * 类说明： 游戏主页
 * 
 * @date Jan 1, 2012
 * @version 1.0
 */
public class GameActivity extends FragmentActivity {

	private static final String TAG = "GameActivity";

	private Context mContext = null;

	private ProgressDialog mProgressDialog;

	private Button mButton1, mButton2, mButton3, mButton4;
	private TextView textView1;

	private AuthDialogListener mAuthListener = null;

	private Wyx mWyx = null;
   
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_main);

		mContext = getApplicationContext();

		mAuthListener = new AuthDialogListener();

		mWyx = Wyx.getInstance(mContext);
		
		//TODO 说明
		//因为在SplashActivity中已经调用过initConfig()方法了，因此，在这里就不需要调用了
//		调用初始化方法
//		mWyx.initConfig(this);

		mProgressDialog = new ProgressDialog(this);

		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgressDialog.setMessage("loading..");
		mProgressDialog.setCancelable(true);

		mButton1 = (Button) findViewById(R.id.button1);
		mButton2 = (Button) findViewById(R.id.button2);
		mButton3 = (Button) findViewById(R.id.button3);
		mButton4 = (Button) findViewById(R.id.button4);

		textView1 = (TextView) findViewById(R.id.textView1);

		setUserInfo();

		// 调用登录接口
		mWyx.authorize(this, mAuthListener);

		mButton1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mWyx.authorize(GameActivity.this, mAuthListener);
			}
		});

		// 支付
		mButton2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				Intent intent = new Intent();
				intent.setClass(GameActivity.this, PayDemoActivity.class);
				startActivity(intent);
			}
		});

		// 注销当前用户
		mButton3.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mWyx.logout();
				setUserInfo();
			}
		});

		// 退出游戏
		mButton4.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				
				//同时也要执行SDK的退出逻辑
				mWyx.destroy(GameActivity.this);
			}
		});
	}

	/**
	 * 设置用户信息
	 */
	private void setUserInfo() {

		StringBuffer sb = new StringBuffer();
		sb.append("userName：").append(mWyx.getUserName());
		sb.append("；userId：").append(mWyx.getUserId());
		sb.append("；token：").append(mWyx.getToken());

		textView1.setText(sb.toString());
	}

	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			if(msg.what==1){
				
				Toast.makeText(GameActivity.this, "（测试信息）：token验证中...",
						Toast.LENGTH_SHORT).show();
				
				verifyToken( new VerifyTokenCallback(){

					@Override
					public void onSuccess(String response) {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								setUserInfo();
								Toast.makeText(GameActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
								
								// 必须初始化浮动窗口
								mWyx.initFloatView(GameActivity.this, true);
							}
						});
					}

					@Override
					public void onFail(Exception e) {
						
					}
				});
			}else{
				Toast.makeText(GameActivity.this, "登录失败！", Toast.LENGTH_SHORT).show();
			}
		}
	};
			
	// 必须实现登录回调接口实现
	class AuthDialogListener implements WyxAuthListener {

		@Override
		public void onComplete(Bundle values) {
			
			boolean isLogin = values != null ? values.getBoolean("isLogin")
					: false;
			
			if (isLogin) {
				mHandler.sendEmptyMessage(1);
			} else {
				mHandler.sendEmptyMessage(11);
			}
		}

		@Override
		public void onFail(Exception e) {
			e.printStackTrace();
		}

		@Override
		public void onCancel() {

			// TODO 测试信息
			Toast.makeText(GameActivity.this, "（测试信息）：登录过程取消",
					Toast.LENGTH_SHORT).show();
		}
	}

	ResponseListener responseListener = new ResponseListener() {
		@Override
		public void onComplete(String response) {
			if (mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}

			final String ret = response;

			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					DialogFragment newFragment = MyAlertDialogFragment
							.newInstance("返回结果", ret);

					newFragment.show(getSupportFragmentManager(), "dialog");
				}
			});
		}

		@Override
		public void onFail(Exception e) {
			e.printStackTrace();
			if (mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
		}
	};

	// 必须重写onActivityResult方法
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (mWyx != null) {
			mWyx.authorizeCallBack(requestCode, resultCode, data);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			mWyx.destroy(GameActivity.this);
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}
	
	// 必须重写游戏入口Activity的onDestroy()方法
	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
	}
	
	/**
	 * 将token、uid、MD5后的手机串号、ip地址上传到游戏服务器
	 * 
	 * @param callback
	 */
	private void verifyToken(final VerifyTokenCallback callback){
		
		//TODO
		//需要上传的参数
		final String token = mWyx.getToken();
		final String userId = mWyx.getUserId();
		final String machineid = mWyx.getEncryptIMEI();
		final String ip = mWyx.getIPAddress();
		
		//TODO
		//模拟启动一个验证token的线程
		Thread thread = new Thread(){

			@Override
			public void run() {
				super.run();
				
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				String result = "success";
				callback.onSuccess(result);
			}
		};
		
		thread.start();
	}
	
	interface VerifyTokenCallback{
		
		/**
		 * 请求正常完成
		 * @param response
		 */
		public void onSuccess(String response);

		/**
		 * 请求过程发生异常
		 * @param e
		 */
		public void onFail(Exception e);
	}
}