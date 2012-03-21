package no.ntnu.item.arctis.android.erlendga.wifidirect.fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import no.ntnu.item.arctis.android.erlendga.wifidirect.listener.DeviceActionListener;
import android.app.Fragment;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

public class CameraFragment extends Fragment {	
	
	protected static final String TAG = "camerafragment";
	private SurfaceView preview = null;
	private SurfaceHolder previewHolder = null;
	private Camera camera = null;
	private boolean inPreview = false;
    
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

    	preview = new SurfaceView(this.getActivity());
        previewHolder = preview.getHolder();
        previewHolder.addCallback(surfaceCallback);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return preview;
    }
    
    public void onResume() {
    	super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
          Camera.CameraInfo info = new Camera.CameraInfo();

          for (int i=0; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, info);

            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
              camera = Camera.open(i);
            }
          }
        }

        if (camera == null) {
          camera = Camera.open();
        }
    }
    
    public void takePicture() {
    	if (inPreview) {
            camera.takePicture(null, null, photoCallback);
            inPreview=false;
    	}
    }
    
    public View getCameraView() {
    	return preview;
    }
    
    Camera.PictureCallback photoCallback = new Camera.PictureCallback() {
    	
    	public void onPictureTaken(byte[] data, Camera camera) {
    		File photo = new File(Environment.getExternalStorageDirectory(), "photo.jpg");
    		inPreview=true;
    		
    		try {
      	        FileOutputStream fos = new FileOutputStream(photo.getPath());
      	
      	        fos.write(data);
      	        fos.close();
      	  	} catch (java.io.IOException e) {
      	        Log.e(TAG, e.getMessage());
      	    }
            
    		inPreview=true;
            ((DeviceActionListener) getActivity()).takePhoto(photo.toURI());
        }
    };
    
    public void onPause() {
    	if (inPreview) {
    		camera.stopPreview();
	    }

	    camera.release();
	    camera = null;
	    inPreview = false;

	    super.onPause();
    }
    
    private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
		Camera.Size result=null;
		
		for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
			if (size.width <= width && size.height <= height) {
				if (result == null) {
					result=size;
				}
				else {
					int resultArea=result.width * result.height;
					int newArea=size.width * size.height;
					
					if (newArea > resultArea) {
						result=size;
					}
				}
			}
		}
		
		return(result);
	}
    
    SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        public void surfaceCreated(SurfaceHolder holder) {
            try {
				camera.setPreviewDisplay(previewHolder);
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			}
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			Camera.Parameters parameters = camera.getParameters();
			Camera.Size size = getBestPreviewSize(width, height, parameters);
			Camera.Size pictureSize = getSmallestPictureSize(parameters);
			
			if (inPreview) {
				camera.stopPreview();
			}
			
			Display display = ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

	        if(display.getRotation() == Surface.ROTATION_0)
	        {
	            parameters.setPreviewSize(height, width);                           
	            camera.setDisplayOrientation(90);
	        }

	        if(display.getRotation() == Surface.ROTATION_90)
	        {
	            parameters.setPreviewSize(width, height);                           
	        }

	        if(display.getRotation() == Surface.ROTATION_180)
	        {
	            parameters.setPreviewSize(height, width);               
	        }

	        if(display.getRotation() == Surface.ROTATION_270)
	        {
	            parameters.setPreviewSize(width, height);
	            camera.setDisplayOrientation(180);
	        }
			
			if (size != null && pictureSize != null) {
			  parameters.setPreviewSize(size.width, size.height);
			  parameters.setPictureSize(pictureSize.width, pictureSize.height);
			  parameters.setPictureFormat(ImageFormat.JPEG);
			
			  camera.setParameters(parameters);
			  camera.startPreview();
			  inPreview = true;
			}
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
          // no-op
        }
      };
      
      private Camera.Size getSmallestPictureSize(Camera.Parameters parameters) {
    	  Camera.Size result = null;

		  for (Camera.Size size : parameters.getSupportedPictureSizes()) {
			  if (result == null) {
			    result = size;
			  }
			  else {
				  int resultArea = result.width * result.height;
				  int newArea = size.width * size.height;
				
			      if (newArea < resultArea) {
			    	  result = size;
			      }
			  }
		  }

		  return(result);
      }
}