package no.ntnu.item.arctis.android.erlendga.wifidirect.fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import no.ntnu.item.arctis.android.erlendga.wifidirect.listener.DeviceActionListener;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

public class CameraFragment extends Fragment {	
	
	protected static final String TAG = "camerafragment";
	private Camera camera = null;
//	private boolean inPreview = false;
	private Preview preview = null;
	private int cameraCurrentlyLocked;
	private int defaultCameraId;
	private int numberOfCameras;
    
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	preview = new Preview(this.getActivity());
    	numberOfCameras = Camera.getNumberOfCameras();
    	
    	CameraInfo cameraInfo = new CameraInfo();
    	for (int i = 0; i < numberOfCameras; i++) {
			Camera.getCameraInfo(i, cameraInfo);
			if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
				defaultCameraId = i;
			}
		}
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return preview;
    }
    
    public void onResume() {
    	super.onResume();
    	
    	camera = Camera.open();
        cameraCurrentlyLocked = defaultCameraId;
        preview.setCamera(camera);
    }
    
    public void onPause() {
	    super.onPause();
	    
	    if (camera != null) {
			preview.setCamera(null);
			camera.release();
			camera = null;
		}
    }
    
    public void switchCamera() {
    	if (numberOfCameras == 1) {
			Builder builder = new Builder(this.getActivity());
			builder.setMessage("This device contains only one camera!").setNeutralButton("Close", null);
			
			AlertDialog alert = builder.create();
			alert.show();
		}
    	
    	if (camera != null) {
			camera.stopPreview();
			preview.setCamera(null);
			camera.release();
			camera = null;
		}
    	
    	camera = Camera.open((cameraCurrentlyLocked + 1) % numberOfCameras);
    	cameraCurrentlyLocked = (cameraCurrentlyLocked + 1) % numberOfCameras;
    	preview.switchCamera(camera);
    	camera.startPreview();
    }
    
    public void takePicture() {
        camera.takePicture(shutterCallback, null, photoCallback);
    }
    
    public View getCameraView() {
    	return preview;
    }
    
    Camera.PictureCallback photoCallback = new Camera.PictureCallback() {
    	
    	public void onPictureTaken(byte[] data, Camera camera) {
    		File photo = new File(Environment.getExternalStorageDirectory(), "photo.jpg");
    		try {
      	        FileOutputStream fos = new FileOutputStream(photo.getPath());
      	
      	        fos.write(data);
      	        fos.close();
      	  	} catch (java.io.IOException e) {
      	        Log.e(TAG, e.getMessage());
      	    }
            ((DeviceActionListener) getActivity()).sendPhoto(photo.toURI());
            switchCamera();
	        switchCamera();
        }
    };
    
    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
		
		public void onShutter() {
			// Plays sound
		}
	};

    private class Preview extends ViewGroup implements Callback {
      	
      	private SurfaceView surfaceView;
  		private SurfaceHolder holder;
  		private Camera camera;
  		private Size previewSize;
  		private List<Size> supportedPreviewSizes;

  		public Preview(Context context) {
  			super(context);
  			
  			surfaceView = new SurfaceView(context);
  			addView(surfaceView);
  			
  			holder = surfaceView.getHolder();
  			holder.addCallback(this);
  			holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
  		}
  		
  		public void setCamera(Camera camera) {
  	    	this.camera = camera;
  	    	if (camera != null) {
  				supportedPreviewSizes = camera.getParameters().getSupportedPreviewSizes();
  				requestLayout();
  			}
  	    }
  		
  		public void switchCamera(Camera camera) {
  			setCamera(camera);
  			try {
  				camera.setPreviewDisplay(holder);
  			} catch (IOException e) {
  				Log.e(TAG, e.getMessage());
  			}
  			Parameters parameters = camera.getParameters();
  			parameters.setPreviewSize(previewSize.width, previewSize.height);
  			requestLayout();
  			
  			Display display = ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
  			camera.setParameters(parameters);
  			camera.setDisplayOrientation((display.getOrientation() + 90) % 360);
  		}
  		
  		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
  	        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
  	        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
  	        setMeasuredDimension(width, height);

  	        if (supportedPreviewSizes != null) {
  	            previewSize = getOptimalPreviewSize(supportedPreviewSizes, width, height);
  	        }
  	    }

  		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
  			Camera.Parameters parameters = camera.getParameters();
  	        parameters.setPreviewSize(previewSize.width, previewSize.height);
  	        requestLayout();
  	        
  	        Display display = ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

  	        camera.setParameters(parameters);
  	        camera.setDisplayOrientation((display.getOrientation() + 90) % 360);
  	        camera.startPreview();
  		}

  		public void surfaceCreated(SurfaceHolder holder) {
  			try {
  	            if (camera != null) {
  	                camera.setPreviewDisplay(holder);
  	            }
  	        } catch (IOException e) {
  	            Log.e(TAG, e.getMessage());
  	        }
  		}

  		public void surfaceDestroyed(SurfaceHolder holder) {
  			if (camera != null) {
  	            camera.stopPreview();
  	        }
  		}

  		protected void onLayout(boolean changed, int l, int t, int r, int b) {
  			if (changed && getChildCount() > 0) {
  	            final View child = getChildAt(0);

  	            final int width = r - l;
  	            final int height = b - t;

  	            int previewWidth = width;
  	            int previewHeight = height;
  	            if (previewSize != null) {
  	                previewWidth = previewSize.width;
  	                previewHeight = previewSize.height;
  	            }

  	            if (width * previewHeight > height * previewWidth) {
  	                final int scaledChildWidth = previewWidth * height / previewHeight;
  	                child.layout((width - scaledChildWidth) / 2, 0, (width + scaledChildWidth) / 2, height);
  	            } else {
  	                final int scaledChildHeight = previewHeight * width / previewWidth;
  	                child.layout(0, (height - scaledChildHeight) / 2, width, (height + scaledChildHeight) / 2);
  	            }
  	        }
  		}
  		
  		private Size getOptimalPreviewSize(List<Size> sizes, int width, int height) {
  	        final double ASPECT_TOLERANCE = 0.1;
  	        double targetRatio = (double) width / height;
  	        if (sizes == null) return null;

  	        Size optimalSize = null;
  	        double minDiff = Double.MAX_VALUE;

  	        int targetHeight = height;

  	        for (Size size : sizes) {
  	            double ratio = (double) size.width / size.height;
  	            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
  	            if (Math.abs(size.height - targetHeight) < minDiff) {
  	                optimalSize = size;
  	                minDiff = Math.abs(size.height - targetHeight);
  	            }
  	        }

  	        if (optimalSize == null) {
  	            minDiff = Double.MAX_VALUE;
  	            for (Size size : sizes) {
  	                if (Math.abs(size.height - targetHeight) < minDiff) {
  	                    optimalSize = size;
  	                    minDiff = Math.abs(size.height - targetHeight);
  	                }
  	            }
  	        }
  	        return optimalSize;
  	    }
      	
      }
}