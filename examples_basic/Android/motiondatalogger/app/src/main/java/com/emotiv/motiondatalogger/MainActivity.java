package com.emotiv.motiondatalogger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.example.com.emotiv.eeglogger.R;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.ActivityCompat;
import android.content.pm.PackageManager;
import android.Manifest;
import android.widget.Toast;

import com.emotiv.sdk.*;
import com.emotiv.bluetooth.*;

public class MainActivity extends Activity {

	private Thread processingThread;
	private static final int REQUEST_ENABLE_BT = 1;
	private static final int MY_PERMISSIONS_REQUEST_BLUETOOTH = 0;
	private BluetoothAdapter mBluetoothAdapter;
	private boolean lock = false;
	private boolean isEnablGetData = false;
	private boolean isEnableWriteFile = false;
	int userId;

	private SWIGTYPE_p_void handleEvent;
	private SWIGTYPE_p_void emoState;
    private SWIGTYPE_p_void motionDataHandle;

	private BufferedWriter motion_writer;
	Button Start_button,Stop_button;
	IEE_MotionDataChannel_t[] Channel_list = {IEE_MotionDataChannel_t.IMD_COUNTER, IEE_MotionDataChannel_t.IMD_GYROX,IEE_MotionDataChannel_t.IMD_GYROY,
			IEE_MotionDataChannel_t.IMD_GYROZ, IEE_MotionDataChannel_t.IMD_ACCX, IEE_MotionDataChannel_t.IMD_ACCY, IEE_MotionDataChannel_t.IMD_ACCZ,
			IEE_MotionDataChannel_t.IMD_MAGX, IEE_MotionDataChannel_t.IMD_MAGY, IEE_MotionDataChannel_t.IMD_MAGZ, IEE_MotionDataChannel_t.IMD_TIMESTAMP};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final BluetoothManager bluetoothManager =
				(BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			/***Android 6.0 and higher need to request permission*****/
			if (ContextCompat.checkSelfPermission(this,
					Manifest.permission.ACCESS_FINE_LOCATION)
					!= PackageManager.PERMISSION_GRANTED) {

					ActivityCompat.requestPermissions(this,
							new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
							MY_PERMISSIONS_REQUEST_BLUETOOTH);
			}
			else{
				checkConnect();
			}
		}
		else {
			checkConnect();
		}
		Start_button = (Button)findViewById(R.id.startbutton);
		Stop_button  = (Button)findViewById(R.id.stopbutton);

		Start_button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Log.e("MotionLogger","Start Write File");
				setDataFile();
				isEnableWriteFile = true;
			}
		});
		Stop_button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Log.e("MotionLogger","Stop Write File");
				StopWriteFile();
				isEnableWriteFile = false;
			}
		});


        Emotiv.IEE_EmoInitDevice(this);
        edkJava.IEE_EngineConnect("");
        handleEvent = edkJava.IEE_EmoEngineEventCreate();
        emoState = edkJava.IEE_EmoStateCreate();
        motionDataHandle = edkJava.IEE_MotionDataCreate();

		processingThread=new Thread()
		{
			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				while(true)
				{
					try
					{
						handler.sendEmptyMessage(0);
						handler.sendEmptyMessage(1);
						if(isEnablGetData && isEnableWriteFile)handler.sendEmptyMessage(2);
						Thread.sleep(5);
					}

					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		};
		processingThread.start();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,
										   String permissions[], int[] grantResults) {
		switch (requestCode) {
			case MY_PERMISSIONS_REQUEST_BLUETOOTH: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					checkConnect();

				} else {

					// permission denied, boo! Disable the
					// functionality that depends on this permission.
					Toast.makeText(this, "App can't run without this permission", Toast.LENGTH_SHORT).show();
				}
				return;
			}

		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == REQUEST_ENABLE_BT) {
			if(resultCode == Activity.RESULT_OK){
				//Connect to emoEngine
				//IEdk.IEE_EngineConnect(this,"");
				//IEdk.IEE_MotionDataCreate();
			}
			if (resultCode == Activity.RESULT_CANCELED) {
				Toast.makeText(this, "You must be turn on bluetooth to connect with Emotiv devices"
						, Toast.LENGTH_SHORT).show();
			}
		}
	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case 0: {
                int state = edkJava.IEE_EngineGetNextEvent(handleEvent);
                if (state == edkJava.EDK_OK) {
                    IEE_Event_t eventType = edkJava.IEE_EmoEngineEventGetType(handleEvent);
                    //userId = IEdk.IEE_EmoEngineEventGetUserId();
                    SWIGTYPE_p_unsigned_int pEngineId = edkJava.new_uint_p();
                    int result = edkJava.IEE_EmoEngineEventGetUserId(handleEvent, pEngineId);
                    int tmpUserId = (int) edkJava.uint_p_value(pEngineId);
                    edkJava.delete_uint_p(pEngineId);
                    userId = tmpUserId;
                    switch (eventType) {
                        case IEE_UserAdded: {
                            Log.e("SDK", "User added");
                            isEnablGetData = true;
                        }
                        break;
                        case IEE_UserRemoved: {
                            Log.e("SDK", "User removed");
                            isEnablGetData = false;
                        }
                        break;
                    }
                }
                break;
            }
			case 1:
            {
				/*Connect device with Insight headset*/
                int number = Emotiv.IEE_GetInsightDeviceCount();
                if (number != 0) {
                    if (!lock) {
                        lock = true;
                        Emotiv.IEE_ConnectInsightDevice(0);
                    }
                } else {
                    /**************************************/
				/*Connect device with EPOC Plus headset*/
                    number = Emotiv.IEE_GetEpocPlusDeviceCount();
                    if (number != 0) {
                        if (!lock) {
                            lock = true;
                            Emotiv.IEE_ConnectEpocPlusDevice(0, false);
                        } else lock = false;
                    }
                    /**************************************/
                }
            }
			break;
			case 2: {
                if (motion_writer == null) return;
                edkJava.IEE_MotionDataUpdateHandle(userId, motionDataHandle);
                SWIGTYPE_p_unsigned_int pSamplesCount = edkJava.new_uint_p();
                int result = edkJava.IEE_MotionDataGetNumberOfSample(motionDataHandle, pSamplesCount);
                int sample = (int) edkJava.uint_p_value(pSamplesCount);
                if (sample > 0) {
                    double[][] data = new double[sample][Channel_list.length];
                    for (int j = 0; j < Channel_list.length; j++) {
                        // Get motion data by channel
                        SWIGTYPE_p_double motion_array = edkJava.new_double_array(sample);
                        edkJava.IEE_MotionDataGet(motionDataHandle, Channel_list[j], motion_array, sample);
                        for (int sampleIdx = 0; sampleIdx < sample; sampleIdx++) {
                            data[sampleIdx][j] = edkJava.double_array_getitem(motion_array, sampleIdx);
                        }
                        edkJava.delete_double_array(motion_array);
                    }
                    // Save matrix to file
                    for (int row = 0; row < sample; row++) {
                        for (int col = 0; col < Channel_list.length; col++) {
                            addData(data[col][row]);
                        }
                        try {
                            motion_writer.newLine();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    data = null;
                }
            }
			break;
			}
		}

	};

	private void setDataFile() {
		try {
			String eeg_header = "COUNTER_MEMS,GYROX,GYROY,GYROZ,ACCX,ACCY,ACCZ,MAGX,MAGY,MAGZ,TimeStamp";
			File root = Environment.getExternalStorageDirectory();
			String file_path = root.getAbsolutePath()+ "/MotionLogger/";
			File folder=new File(file_path);
			if(!folder.exists())
			{
				folder.mkdirs();
			}
			motion_writer = new BufferedWriter(new FileWriter(file_path+"raw_motion.csv"));
			motion_writer.write(eeg_header);
			motion_writer.newLine();
		} catch (Exception e) {
			Log.e("","Exception"+ e.getMessage());
		}
	}
	private void StopWriteFile(){
		try {
			motion_writer.flush();
			motion_writer.close();
			motion_writer = null;
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	/**
	 * public void addData(double data) Add  Data for write int the
	 * Motion File
	 *
	 * @param
	 *            - double motion data
	 */
	public void addData(double data) {

		if (motion_writer == null) {
			return;
		}

        String input = "";
        input += (String.valueOf(data) + ",");
        try {
            motion_writer.write(input);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}

	private void checkConnect(){
		if (!mBluetoothAdapter.isEnabled()) {
			/****Request turn on Bluetooth***************/
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
		else
		{
			//Connect to emoEngine
			//IEdk.IEE_EngineConnect(this,"");
			//IEdk.IEE_MotionDataCreate();
		}
	}


}
