package com.my365day.ui;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.my365day.Constant;
import com.my365day.R;
import com.my365day.util.CompressImgUtil;
import com.my365day.util.Tool;

public class UploadActivity extends Activity implements OnClickListener,OnCheckedChangeListener{

	private static String TAG = UploadActivity.class.getSimpleName();
	
	private static final File PHOTO_DIR = new File(Environment.getExternalStorageDirectory()+"/tmp/");
	private String fileName;
	private File photoFile;
	private static final int CAMERA_WITH_DATA = 3023;
	private ImageView imageViewPhoto;
	private LoadingHandler loadingHandler;
	public ProgressDialog proDialog;
	private Bitmap resizeBitMap;
	
	private int mood = -1;
	private EditText remarkText;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
		String status = Environment.getExternalStorageState();
		//check is external storage mounted.
		if (status.equals(Environment.MEDIA_MOUNTED)) {
			doTakePhoto();
		} else {
			Toast.makeText(UploadActivity.this, 
			        getResources().getString(R.string.insert_sdcard), Toast.LENGTH_LONG).show();
			finish();
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_upload_activity);
		loadingHandler = new LoadingHandler();
		findView();
    }
    
    public void findView(){
    	imageViewPhoto = (ImageView)findViewById(R.id.imageView);
    	
    	Button uploadBtn = (Button)findViewById(R.id.upload_btn);
    	uploadBtn.setOnClickListener(this);

    	RadioGroup moodList = (RadioGroup)findViewById(R.id.mood_list);
    	moodList.setOnCheckedChangeListener(this);
    	
    	remarkText = (EditText)findViewById(R.id.remark_text);
    }
    
    //start camera to take photo
	private void doTakePhoto() {
		try {
			if (!PHOTO_DIR.exists()) {
				PHOTO_DIR.mkdirs();
			}
			fileName = System.currentTimeMillis() + ".jpg";
			photoFile = new File(PHOTO_DIR, fileName);
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
			startActivityForResult(intent, CAMERA_WITH_DATA);
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
		}
	}

	public int uploadpy(){
		String url = Constant.DOMAIN+"/image/upload";
		HttpClient httpClient = new HttpClient();
		PostMethod postMethod = new PostMethod(url);
		try {
			photoFile.delete();
			FileOutputStream out = new FileOutputStream(photoFile);
			resizeBitMap.compress(Bitmap.CompressFormat.JPEG, 80, out);
			out.close();
			
		    //SharedPreferences getWeightAndAgeStore = getSharedPreferences("com.my365day_preferences", Context.MODE_PRIVATE);
	        //Log.d(TAG,"auth_code="+getWeightAndAgeStore.getString("auth_code", "0"));
			
			StringPart s1=new StringPart("loginCode",Constant.AUTH_CODE);
			StringPart s2=new StringPart("mood",String.valueOf(mood));
			StringPart s3=new StringPart("remark","remark");
			FilePart fp=new FilePart("file",photoFile);
			
			Part[] parts = {s1,s2,s3,fp};
			postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));
			httpClient.executeMethod(postMethod);
			int statusCode=postMethod.getStatusCode();
			Log.d(TAG,"http statusCode ="+statusCode);
			
			if (statusCode == HttpStatus.SC_OK) {
			    //get result from server: sucess
				String result = postMethod.getResponseBodyAsString();
				Log.d(TAG,"result="+result);
				if(result.equals("success")){
					Tool.delAllFile(PHOTO_DIR.getAbsolutePath());
					return 1;
				}
				else{
					return 0;
				}
			} else {
				return -1;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			postMethod.releaseConnection();
		}
		return -1;
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
        	proDialog = new ProgressDialog(UploadActivity.this);
            proDialog.show();
            proDialog.setContentView(R.layout.custom_progress_dialog);
            //compress photo file and show.
            new Thread(new Runnable() {
                @Override
                public void run() {
                    getBitMap();
                    UploadActivity.this.loadingHandler.sendEmptyMessage(0);
                }
            }).start();
        }
        else{
        	finish();
        }
    }

   private void getBitMap() {
        Bitmap bm;
        File f = new File(PHOTO_DIR,fileName);
        if (f.exists()) {
            bm = BitmapFactory.decodeFile(f.getAbsolutePath());
        } else {
            Toast.makeText(this, getResources().getString(R.string.file_not_exist),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        resizeBitMap = CompressImgUtil.extractMiniThumb(bm,800,600);
        Log.d(TAG,"raw photo size:"+bm.getWidth() +".."+bm.getHeight());
        Log.d(TAG,"resize photo size:"+resizeBitMap.getWidth() +".."+resizeBitMap.getHeight());
        
        f.delete();
    }
   
	public class LoadingHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			imageViewPhoto.setImageBitmap(resizeBitMap);
			proDialog.cancel();
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.upload_btn://upload
        	proDialog = new ProgressDialog(UploadActivity.this);
            proDialog.show();
            proDialog.setContentView(R.layout.custom_progress_dialog);
            TextView tv = (TextView)(proDialog.findViewById(R.id.oaprogresstitle));
            tv.setText(getResources().getString(R.string.uploading_please_wait));
            new UploadTask().execute();
			break;
		default:
			break;
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
		case R.id.happy_button:
			mood=1;
			break;
		case R.id.blue_button:
			mood=2;
			break;
		case R.id.angry_button:
			mood=3;
			break;
		case R.id.sick_button:
			mood=4;
			break;
		default:
			break;
		}
	}
	
	//upload image task
	class UploadTask extends AsyncTask<String,String,String> {

	    @Override
	    protected String doInBackground(String... params) {
	        int ret = uploadpy();
	        if(ret==1){
	            publishProgress("success");
	        }
	        else if(ret==0){
	            publishProgress("fail");
	        }
	        else{
	            System.out.println("ret="+ret);
	            publishProgress("con fail");
	        }
	        return null;
	    }

	    @Override
	    protected void onProgressUpdate(String... values) {
	        super.onProgressUpdate(values);
	        if(values[0].equals("fail")){
	            Toast.makeText(UploadActivity.this, UploadActivity.this.getResources().getString(R.string.upload_fail), Toast.LENGTH_SHORT).show();
	        }
	        else if(values[0].equals("success")){
	            Toast.makeText(UploadActivity.this, UploadActivity.this.getResources().getString(R.string.upload_success), Toast.LENGTH_SHORT).show();
	            UploadActivity.this.finish();
	        }
	        else{
	            Toast.makeText(UploadActivity.this, UploadActivity.this.getResources().getString(R.string.con_fail), Toast.LENGTH_SHORT).show();
	        }
	        proDialog.cancel();
	    }

	}
	
}
