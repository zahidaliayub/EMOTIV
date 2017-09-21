package com.example.profilecloudexample;

import com.emotiv.sdk.*;
import com.emotiv.bluetooth.*;

import android.os.Build;
//import android.support.v4.content.ContextCompat;
//import android.support.v4.app.ActivityCompat;
import android.content.pm.PackageManager;
import android.Manifest;
import android.widget.Toast;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

	private Thread processingThread;
	private static final int REQUEST_ENABLE_BT = 1;
	private static final int MY_PERMISSIONS_REQUEST_BLUETOOTH = 0;
	private BluetoothAdapter mBluetoothAdapter;
	private boolean lock = false;
	int userId;
	boolean cloudConnected                 = false;
	boolean headsetConnected               = false;
	int engineUserID           			   = 0;
	int  userCloudID                       = -1;
	private SWIGTYPE_p_void handleEvent;
	private SWIGTYPE_p_void emoState;

	Button Save_profile,Load_profile,Delete_profile,Login_btn;
	EditText user_name, pass_word;
	TextView status;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final BluetoothManager bluetoothManager =
				(BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();
//		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//			/***Android 6.0 and higher need to request permission*****/
//			if (ContextCompat.checkSelfPermission(this,
//					Manifest.permission.ACCESS_FINE_LOCATION)
//					!= PackageManager.PERMISSION_GRANTED) {
//
//				ActivityCompat.requestPermissions(this,
//						new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//						MY_PERMISSIONS_REQUEST_BLUETOOTH);
//			}
//			else{
//				checkConnect();
//			}
//		}
//		else {
			checkConnect();
//		}

		Emotiv.IEE_EmoInitDevice(this);
		edkJava.IEE_EngineConnect("Emotiv Systems-5");
		handleEvent = edkJava.IEE_EmoEngineEventCreate();
		emoState = edkJava.IEE_EmoStateCreate();

		Save_profile   = (Button)findViewById(R.id.button2);
        Load_profile   = (Button)findViewById(R.id.button3);
        Delete_profile = (Button)findViewById(R.id.button1);
        Login_btn      = (Button)findViewById(R.id.button4);
        user_name      = (EditText)findViewById(R.id.editText_name);
        pass_word      = (EditText)findViewById(R.id.editText_pass);
        status         = (TextView)findViewById(R.id.textView4);

        Save_profile.setEnabled(false);
        Load_profile.setEnabled(false);
        Delete_profile.setEnabled(false);

        Save_profile.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				 if(!headsetConnected) {
					 status.setText("Connect headset first");
				        return;
				    }
				    if(userCloudID < 0) {
				    	status.setText("Login first");
				        return;
				    }

				    if(edkJava.EC_SaveUserProfile(userCloudID, engineUserID, "test", profileFileType.TRAINING ) == edkJava.EDK_OK) {
				    	status.setText("Save new profile successfully");
				    }
				    else {
				    	status.setText("Profile is existed or can't create new profile");
				    }

			}
		});
        Load_profile.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(!headsetConnected) {
					status.setText("Connect headset first");
			        return;
			    }
			    if(userCloudID < 0) {
			    	status.setText("Login first");
			        return;
			    }
				SWIGTYPE_p_int pProfileId = edkJava.new_int_p();
				edkJava.EC_GetProfileId(userCloudID, "test", pProfileId);
			    int profileID = edkJava.int_p_value(pProfileId);
				edkJava.delete_int_p(pProfileId);
			    if ( profileID < 0) {
			    	status.setText("Profile isnt existed");
			        return;
			    }
			    if(edkJava.EC_LoadUserProfile(userCloudID, engineUserID, profileID,-1) == edkJava.EDK_OK) {
			    	status.setText("Load profile successfully");
			    }
			    else {
			    	status.setText("Cant load this profile");
			    }

			}
		});
        Delete_profile.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(!headsetConnected) {
					status.setText("Connect headset first");
			        return;
			    }
			    if(userCloudID < 0) {
			    	status.setText("Login first");
			        return;
			    }
				SWIGTYPE_p_int pProfileId = edkJava.new_int_p();
				edkJava.EC_GetProfileId(userCloudID, "test", pProfileId);
				int profileID = edkJava.int_p_value(pProfileId);
				edkJava.delete_int_p(pProfileId);
			    if ( profileID < 0) {
			    	status.setText("Profile isnt existed");
			        return;
			    }
			    if(edkJava.EC_DeleteUserProfile(userCloudID, profileID) == edkJava.EDK_OK){
			    	status.setText("Remove profile successfully");
			    }
			    else {
			    	status.setText("Cant load this profile");
			    }
			}
		});
        Login_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(!cloudConnected) {
					int connectResult = edkJava.EC_Connect();
			        cloudConnected = ( connectResult == edkJava.EDK_OK);
			        if(!cloudConnected) {
			            status.setText("Please check internet connection and connect again");
						Log.e("EmoProfile", "EC_Connect error:" + connectResult);
			            return;
			        }
			    }
			    if(user_name.getTextSize() == 0 || pass_word.getTextSize() == 0) {
			    	status.setText("Enter username and password");
			        return;
			    }
			    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	            imm.hideSoftInputFromWindow(Login_btn.getWindowToken(),
	                                      InputMethodManager.RESULT_UNCHANGED_SHOWN);
			    if(edkJava.EC_Login(user_name.getText().toString(),pass_word.getText().toString()) == edkJava.EDK_OK) {
			    	status.setText("Login successfully");
					SWIGTYPE_p_int pUserId = edkJava.new_int_p();
			        int result  = edkJava.EC_GetUserDetail(pUserId);
			        if(result  == edkJava.EDK_OK) {
						userCloudID = edkJava.int_p_value(pUserId);
			            Save_profile.setEnabled(true);
			            Load_profile.setEnabled(true);
			            Delete_profile.setEnabled(true);
			        }
			        else {
			        	status.setText("Cant get user detail. Please try again");
			        }
					edkJava.delete_int_p(pUserId);
			    }
			    else {
			    	status.setText("Username or password is wrong. Check again");
			    }
			}
		});

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

      	Handler handler = new Handler() {
      		@Override
      		public void handleMessage(Message msg) {
      			switch (msg.what) {

      			case 0:
      				int state = edkJava.IEE_EngineGetNextEvent(handleEvent);
      				if (state == edkJava.EDK_OK) {
      					IEE_Event_t eventType = edkJava.IEE_EmoEngineEventGetType(handleEvent);
						SWIGTYPE_p_unsigned_int pEngineId = edkJava.new_uint_p();
						int result = edkJava.IEE_EmoEngineEventGetUserId(handleEvent, pEngineId);
						int tmpUserId = (int)edkJava.uint_p_value(pEngineId);
						edkJava.delete_uint_p(pEngineId);

      					if(eventType == IEE_Event_t.IEE_UserAdded){
      						Log.e("SDK","User added");
      						headsetConnected = true;
							userId = tmpUserId;
						}
      					if(eventType == IEE_Event_t.IEE_UserRemoved){
      						Log.e("SDK","User removed");
      						headsetConnected = false;
      					}
      				}

      				break;
      			case 1:
					/*Connect device with Insight headset*/
      				int number = Emotiv.IEE_GetInsightDeviceCount();
      				if(number != 0) {
      					if(!lock){
      						lock = true;
							Emotiv.IEE_ConnectInsightDevice(0);
      					}
      				} else {
						/*Connect device with Epoc Plus headset*/
						number = Emotiv.IEE_GetEpocPlusDeviceCount();
						if(number != 0) {
							if(!lock){
								lock = true;
								Emotiv.IEE_ConnectEpocPlusDevice(0,false);
							}
						} else {
							lock = false;
						}
					}
      				break;
      			}
      		   }
      		};

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
				edkJava.IEE_EngineConnect("");
			}
			if (resultCode == Activity.RESULT_CANCELED) {
				Toast.makeText(this, "You must be turn on bluetooth to connect with Emotiv devices"
						, Toast.LENGTH_SHORT).show();
			}
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void checkConnect(){
		if (!mBluetoothAdapter.isEnabled()) {
			/****Request turn on Bluetooth***************/
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
	}

}
