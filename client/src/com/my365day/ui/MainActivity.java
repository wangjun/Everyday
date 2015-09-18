package com.my365day.ui;

import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.MenuItem;

import com.my365day.NotificationReceiver;
import com.my365day.R;
import com.my365day.TimePreference;


public class MainActivity extends Activity {

    private String TAG = MainActivity.class.getSimpleName();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();
        fragmentTransaction.replace(android.R.id.content, new SettingsFragment());
        fragmentTransaction.commit();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        }
        return false;
    }
    

    @SuppressLint("ValidFragment")
    class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener{
        private CheckBoxPreference alarmEnablePreference;
        private TimePreference timePreference;
        
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
            
            alarmEnablePreference = (CheckBoxPreference)findPreference("alarm_enable");
            timePreference = (TimePreference)findPreference("alarm_time");
            
            alarmEnablePreference.setOnPreferenceChangeListener(this);
            timePreference.setOnPreferenceChangeListener(this);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if(preference==alarmEnablePreference){
                alarmEnablePreference.setChecked(!alarmEnablePreference.isChecked());
                if(alarmEnablePreference.isChecked()){//checked
                    Calendar c = timePreference.getCalendar();
                    System.out.println("hour:"+c.get(Calendar.HOUR_OF_DAY)+" mins="+c.get(Calendar.MINUTE));
                    setAlarm(c,true);
                }else{//unchecked
                    setAlarm(null, false);
                }
            }else if(preference==timePreference){
                Calendar calendar = timePreference.getCalendar();
                setAlarm(calendar,true);
            }
            return false;
        }
        
        private void setAlarm(Calendar c,boolean enable){
            
            Intent intent = new Intent(getBaseContext(), NotificationReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            
            if(enable){
                //cancel first
                alarmManager.cancel(pendingIntent);

                Calendar cal=Calendar.getInstance();
                cal.setTime(new Date());
                
                cal.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY));
                cal.set(Calendar.MINUTE, c.get(Calendar.MINUTE));
                cal.set(Calendar.SECOND, 0);
                if(cal.getTimeInMillis()<System.currentTimeMillis()){
                    Log.d(TAG,"set next day alarm");
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                }
                Log.d(TAG,"set alarm= "+cal.getTime().toLocaleString());
                //alarmManager.set(AlarmManager.RTC, c.getTimeInMillis(), pendingIntent);
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                        cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
                
            }else{
                Log.d(TAG,"cancel alarm");
                alarmManager.cancel(pendingIntent);
            }
        }
        
    }
}
