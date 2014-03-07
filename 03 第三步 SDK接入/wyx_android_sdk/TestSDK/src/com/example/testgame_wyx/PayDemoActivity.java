package com.example.testgame_wyx;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import cn.sina.youxi.pay.sdk.Wyx;
import cn.sina.youxi.util.ResponseListener;

/**
 * 类说明： 支付demo
 * 支付之前先检验是否已登录，没有登录则先登录，后支付
 * 
 * @date Jan 1, 2012
 * @version 1.0
 */
public class PayDemoActivity extends FragmentActivity {

	private static final String TAG = "PayDemoActivity";

	private Context mContext;
	private ProgressDialog mProgressDialog;

	private Button mButton1;
	private TextView textView1;
	
	private Wyx mWyx = null;
	
	//当前订单号
	private String mOrderId = "";
	
	//下订单回调
	private PlaceOrderListener mPlaceOrderListener = null;
	
	//支付窗口关闭回调
	private PayDialogDismissListener mPayDialogDismissListener = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.activity_pay);

		mContext = this.getApplicationContext();
		mWyx = Wyx.getInstance(this);
		
		mPlaceOrderListener = new PlaceOrderListener();
		mPayDialogDismissListener = new PayDialogDismissListener();
		
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgressDialog.setMessage("loading..");
		mProgressDialog.setCancelable(true);

		mButton1 = (Button) findViewById(R.id.button1);
		textView1 = (TextView) findViewById(R.id.textView1);

		setUserInfo();
		
		// 支付测试
		mButton1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
	
				if (mWyx.isLogin(mContext)) {
					mProgressDialog.show();
					
					mWyx.placeOrder(PayDemoActivity.this, 1, "一次霸气的测试",
							"自定义参数", mPlaceOrderListener,
							mPayDialogDismissListener);
				} else {
					Toast.makeText(mContext, "请先登录", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
	
	//必须实现下订单回调接口
	class PlaceOrderListener implements ResponseListener {

		@Override
		public void onComplete(String response) {
			//下订单完成之后会立即回调接口
			
			if (mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
			
			JSONObject jsonObj = JSONUtils
					.parse2JSONObject(response);
			String code = JSONUtils.getString(jsonObj, "code");

			if (!TextUtils.isEmpty(code)) {
				Log.e(TAG, "placeOrder，下订单过程中报错：" + response);
			}
			else {
				mOrderId= JSONUtils.getString(
						jsonObj, "order_id");
				
				Log.i(TAG, "placeOrder，下订单之后获取到订单号："+ mOrderId);
			}
		}

		@Override
		public void onFail(Exception e) {
			if (mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
		}
	} 
	
	// TODO
	// 支付窗口关闭时会回调该接口，可以在该回调中查询游戏服务器，得到订单状态，并来改变客户端金币、元宝的数量
	// 由于网络请求的不确定性，用户发起查询的时候，回调过程可能尚未结束，因此开发商的需要发起多次查询，直到查询到当前订单状态。
	// 例如，开发商可以维护一个定时器，定时到游戏服务器上查询。
	class PayDialogDismissListener implements OnDismissListener {
		
		@Override
		public void onDismiss(DialogInterface dialog) {
			if (mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
			
			// TODO
			Log.i(TAG, "支付窗口关掉，当前订单号：" + mOrderId);
			Toast.makeText(PayDemoActivity.this, "（测试信息）：支付窗口关掉，当前订单号："+ mOrderId, Toast.LENGTH_SHORT)
					.show();
			
			// TODO
		}
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
	
	/**
	 * 展示提示信息
	 * 
	 * @param title
	 * @param message
	 */
	private void showDialog(String title, String message) {

		if (TextUtils.isEmpty(title) || TextUtils.isEmpty(message)) {
			return;
		}

		DialogFragment newFragment = MyAlertDialogFragment.newInstance(title,
				message);

		newFragment.show(getSupportFragmentManager(), "dialog");
	}

	private Handler mHandler = new Handler() {

		@Override
		public void dispatchMessage(Message msg) {
			super.dispatchMessage(msg);

			Bundle data = msg.getData();
			String response = data.getString("response");
			showDialog("返回结果", response);
		}
	};
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
	}
}