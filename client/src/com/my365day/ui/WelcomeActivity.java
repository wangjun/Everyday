package com.my365day.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.my365day.R;

public class WelcomeActivity extends Activity {
	
	private LoginHandler loginHandler = new LoginHandler();

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_welcome_activity);
		loginThread.start();
	}
	
	Thread loginThread = new Thread(new Runnable() {
		@Override
		public void run() {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			WelcomeActivity.this.loginHandler.sendEmptyMessage(0);
		}
	});
	
	class LoginHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Intent intent = new Intent(WelcomeActivity.this,MainActivity.class);
			startActivity(intent);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		finish();
	}
	
}
