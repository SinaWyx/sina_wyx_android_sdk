package com.example.testgame_wyx;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
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
 * 类说明： 游戏入口
 * 
 * @date Jan 1, 2012
 * @version 1.0
 */
public class GameActivity extends FragmentActivity {

	private static final String TAG = "GameActivity";

	private Context mContext = null;

	private ProgressDialog mProgressDialog;

	private Button mButton1, mButton2, mButton3, mButton4, mButton5;
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
		getApplication().getApplicationContext();
		
		mAuthListener = new AuthDialogListener();

		mWyx = Wyx.getInstance(mContext);

		// 调用初始化方法
		mWyx.initConfig(this);

		mProgressDialog = new ProgressDialog(this);

		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgressDialog.setMessage("loading..");
		mProgressDialog.setCancelable(true);

		mButton1 = (Button) findViewById(R.id.button1);
		mButton2 = (Button) findViewById(R.id.button2);
		mButton3 = (Button) findViewById(R.id.button3);
		mButton4 = (Button) findViewById(R.id.button4);
		mButton5 = (Button) findViewById(R.id.button5);
		
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

		// 注销当前用户
		mButton2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mWyx.logout();
				setUserInfo();
			}
		});

		// 切换账号
		mButton3.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mWyx.accountSwitch(GameActivity.this, mAuthListener);
			}
		});

		// 支付
		mButton4.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				Intent intent = new Intent();
				intent.setClass(GameActivity.this, PayDemoActivity.class);
				startActivity(intent);
			}
		});
		
		//退出游戏
		mButton5.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				//TODO 在这里执行游戏的退出逻辑
				finish();
				
				//同时也要执行SDK的退出逻辑
				mWyx.destroy();
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

	// 必须实现登录回调接口实现
	class AuthDialogListener implements WyxAuthListener {

		@Override
		public void onComplete(Bundle values) {

			//必须初始化浮动窗口
			mWyx.initFloatView(GameActivity.this, values);
			
			// TODO 在此处写回调逻辑
			setUserInfo();

			String toast = "（测试信息）：登录过程完成，结果：";

			boolean isLogin = values != null ? values.getBoolean("isLogin")
					: false;
			
			if (isLogin && mWyx.isLogin(mContext)) {
				toast += "成功，" + mWyx.getUserName();
			} else {
				toast += "失败";
			}

			Toast.makeText(GameActivity.this, toast, Toast.LENGTH_SHORT).show();
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

	// 必须重写游戏入口Activity的onDestroy()方法
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		//sdk的销毁方法，必须要调用
		if (mWyx != null) {
			mWyx.destroy();
		}
		
		if (mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
	}
}