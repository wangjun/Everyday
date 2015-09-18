package com.my365day;

import android.app.Application;

public class My365dayApplication extends Application{
	
	private static My365dayApplication instance;
	
	public static My365dayApplication getInstance(){
		return instance;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
	}
}
