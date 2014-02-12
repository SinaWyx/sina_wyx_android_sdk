package com.example.testgame_wyx;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.view.WindowManager;
import cn.sina.youxi.pay.sdk.Wyx;

/**
 * 类说明： 游戏启动页
 * 
 * @date Jan 1, 2014
 * @version 1.0
 */
public class SplashActivity extends Activity{
	
	private Context mContext;
	private Wyx mWyx;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.activity_splash);
		
		mContext = this.getApplicationContext();
		mWyx = Wyx.getInstance(mContext);
		
		// 调用初始化方法
		mWyx.initConfig(SplashActivity.this);
		
		//TODO 模拟游戏的载入动画
		Thread thread = new Thread(){

			@Override
			public void run() {
				super.run();
				
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				mHandler.sendEmptyMessage(-1);
			}
		};
		
		thread.start();
	}
	
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			finish();
			
			Intent intent = new Intent();
			intent.setClass(SplashActivity.this, GameActivity.class);
			startActivity(intent);
		}
	};
}