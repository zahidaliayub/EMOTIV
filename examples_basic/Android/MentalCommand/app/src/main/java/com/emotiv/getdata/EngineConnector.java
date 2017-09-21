package com.emotiv.getdata;

import java.util.Timer;
import java.util.TimerTask;

import com.emotiv.sdk.*;
import com.emotiv.bluetooth.*;

import com.emotiv.mentalcommand.ActivityTrainning;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentSender.SendIntentException;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class EngineConnector {

	public static Context context;
	public static EngineConnector engineConnectInstance;
	private Timer timer;
	private TimerTask timerTask;

	public boolean isConnected = false;
	public boolean bleInUse = false;
	private int state;
	private int userId=-1;

	private SWIGTYPE_p_void handleEvent;
	private SWIGTYPE_p_void emoState;

	/* ============================================ */
	protected static final int TYPE_USER_ADD = 16;
	protected static final int TYPE_USER_REMOVE = 32;
	protected static final int TYPE_EMOSTATE_UPDATE = 64;
	protected static final int TYPE_METACOMMAND_EVENT = 256;
	/* ============================================ */
	protected static final int HANDLER_TRAIN_STARTED = 1;
	protected static final int HANDLER_TRAIN_SUCCEED = 2;
	protected static final int HANDLER_TRAIN_FAILED = 3;
	protected static final int HANDLER_TRAIN_COMPLETED = 4;
	protected static final int HANDLER_TRAIN_ERASED = 5;
	protected static final int HANDLER_TRAIN_REJECTED = 6;
	protected static final int HANDLER_ACTION_CURRENT = 7;
	protected static final int HANDLER_USER_ADD = 8;
	protected static final int HANDLER_USER_REMOVE = 9;
	protected static final int HANDLER_TRAINED_RESET = 10;

	public EngineInterface delegate;

	public static void setContext(Context context) {
		EngineConnector.context = context;
	}

	public static EngineConnector shareInstance() {
		if (engineConnectInstance == null) {
			engineConnectInstance = new EngineConnector();
		}
		return engineConnectInstance;
	}

	public EngineConnector() {
		connectEngine();
	}


	private void connectEngine(){
		Emotiv.IEE_EmoInitDevice(EngineConnector.context);
		edkJava.IEE_EngineConnect("Emotiv Systems-5");
		handleEvent = edkJava.IEE_EmoEngineEventCreate();
		emoState = edkJava.IEE_EmoStateCreate();

		timer = new Timer();
		intTimerTask();
		timer.schedule(timerTask , 0, 10);
	}

	public void enableMentalcommandActions(IEE_MentalCommandAction_t _MetalcommandAction) {
		//long MetaCommandActions;
		SWIGTYPE_p_unsigned_long uActiveAction = edkJava.new_ulong_p();
		int result = edkJava.IEE_MentalCommandGetActiveActions(userId, uActiveAction);
		if (result == edkJava.EDK_OK) {
			long _currentActiveActions = edkJava.ulong_p_value(uActiveAction);
			long y = _currentActiveActions & _MetalcommandAction.swigValue();
			if (y == 0) {
				long MetaCommandActions;
				MetaCommandActions = _currentActiveActions | _MetalcommandAction.swigValue();
				edkJava.IEE_MentalCommandSetActiveActions(userId, MetaCommandActions);
			}
		}
		edkJava.delete_ulong_p(uActiveAction);
	}

	public boolean checkTrained(IEE_MentalCommandAction_t action) {
		boolean res = false;
		SWIGTYPE_p_unsigned_long uSignAction = edkJava.new_ulong_p();
		int result = edkJava.IEE_MentalCommandGetTrainedSignatureActions(userId, uSignAction);
		if (result == edkJava.EDK_OK) {
			long _currentActiveActions = edkJava.ulong_p_value(uSignAction);
			long y = _currentActiveActions & action.swigValue();
			return (y == action.swigValue());
		}
		return res;
	}

	public void trainningClear(IEE_MentalCommandAction_t _MetalcommandAction) {
		edkJava.IEE_MentalCommandSetTrainingAction(userId, _MetalcommandAction);
		if (edkJava.IEE_MentalCommandSetTrainingControl(userId,
				IEE_MentalCommandTrainingControl_t.MC_ERASE) == edkJava.EDK_OK) {
		}
	}

	public boolean startTrainingMetalcommand(Boolean isTrain, IEE_MentalCommandAction_t MetaCommandAction) {
		if (!isTrain) {
			if (edkJava.IEE_MentalCommandSetTrainingAction(userId, MetaCommandAction) == edkJava.EDK_OK) {
				if (edkJava.IEE_MentalCommandSetTrainingControl(userId,
						IEE_MentalCommandTrainingControl_t.MC_START) == edkJava.EDK_OK) {
					return true;
				}
			}
		} else {
			if (edkJava.IEE_MentalCommandSetTrainingControl(userId, IEE_MentalCommandTrainingControl_t.MC_RESET) == edkJava.EDK_OK) {
				return false;
			}
		}
		return false;
	}

	public void setTrainControl(IEE_MentalCommandTrainingControl_t type) {
		if (edkJava.IEE_MentalCommandSetTrainingControl(userId, type) == edkJava.EDK_OK) {
		}

	}

	public int getEventEngineId(SWIGTYPE_p_void hEvent)
	{
		SWIGTYPE_p_unsigned_int pEngineId = edkJava.new_uint_p();
		int result = edkJava.IEE_EmoEngineEventGetUserId(handleEvent, pEngineId);
		int tmpUserId = (int)edkJava.uint_p_value(pEngineId);
		edkJava.delete_uint_p(pEngineId);
		return tmpUserId;
	}

	public void intTimerTask() {
		if (timerTask != null)
			return;
		timerTask = new TimerTask() {

			@Override
			public void run() {
				int numberDevice=Emotiv.IEE_GetInsightDeviceCount();
				if (numberDevice != 0){
					if (!bleInUse) {
						bleInUse = true;
						Emotiv.IEE_ConnectInsightDevice(0);
					}
				}
				else {
					numberDevice = Emotiv.IEE_GetEpocPlusDeviceCount();
					if (numberDevice != 0){
						if (!bleInUse) {
							bleInUse = true;
							Emotiv.IEE_ConnectEpocPlusDevice(0, false);
						}
					} else {
						bleInUse = false;
					}
				}
				state = edkJava.IEE_EngineGetNextEvent(handleEvent);
				if (state == edkJava.EDK_OK) {
					IEE_Event_t eventType = edkJava.IEE_EmoEngineEventGetType(handleEvent);
					int tmpUserId = getEventEngineId(handleEvent);
					switch (eventType) {
						case IEE_UserAdded:
						Log.e("connect", "User Added");
						isConnected = true;
						bleInUse = true;
						userId = tmpUserId;
						hander.sendEmptyMessage(HANDLER_USER_ADD);
						break;
						case IEE_UserRemoved:
						Log.e("disconnect", "User Removed");
						isConnected = false;
						bleInUse = false;
						userId=-1;
						hander.sendEmptyMessage(HANDLER_USER_REMOVE);
						break;
						case IEE_EmoStateUpdated:
						if (!isConnected)
							break;
							edkJava.IEE_EmoEngineEventGetEmoState(handleEvent, emoState);
						Log.e("MentalCommand", "EmoStateUpdated");
						hander.sendMessage(hander
								.obtainMessage(HANDLER_ACTION_CURRENT));

						break;
						case IEE_MentalCommandEvent:
						IEE_MentalCommandEvent_t type = edkJava.IEE_MentalCommandEventGetType(handleEvent);
						if (type == IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingStarted) {
							Log.e("MentalCommand", "training started");
							hander.sendEmptyMessage(HANDLER_TRAIN_STARTED);
						} else if (type == IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingSucceeded) {
							Log.e("MentalCommand", "training Succeeded");
							hander.sendEmptyMessage(HANDLER_TRAIN_SUCCEED);
						} else if (type == IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingCompleted) {
							Log.e("MentalCommand", "training Completed");
							hander.sendEmptyMessage(HANDLER_TRAIN_COMPLETED);
						} else if (type == IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingDataErased) {
							Log.e("MentalCommand", "training erased");
							hander.sendEmptyMessage(HANDLER_TRAIN_ERASED);

						} else if (type == IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingFailed) {
							Log.e("MentalCommand", "training failed");
							hander.sendEmptyMessage(HANDLER_TRAIN_FAILED);

						} else if (type == IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingRejected) {
							Log.e("MentalCommand", "training rejected");
							hander.sendEmptyMessage(HANDLER_TRAIN_REJECTED);
						} else if (type == IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingReset) {
							Log.e("MentalCommand", "training Reset");
							hander.sendEmptyMessage(HANDLER_TRAINED_RESET);
						}
						//	IEE_MentalCommandAutoSamplingNeutralCompleted,
						//	IEE_MentalCommandSignatureUpdated;
						break;
						case IEE_FacialExpressionEvent:
							break;
					default:
						break;
					}
				}
			}
		};
	}

	Handler hander = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLER_USER_ADD:
				if (delegate != null)
					delegate.userAdd(userId);
				break;
			case HANDLER_USER_REMOVE:
				if (delegate != null)
					delegate.userRemoved();
				break;
			case HANDLER_ACTION_CURRENT:
				if (delegate != null)
					delegate.currentAction(edkJava.IS_MentalCommandGetCurrentAction(emoState), edkJava.IS_MentalCommandGetCurrentActionPower(emoState));
				break;
			case HANDLER_TRAIN_STARTED:
				if (delegate != null)
					delegate.trainStarted();
				break;
			case HANDLER_TRAIN_SUCCEED:
				if (delegate != null)
					delegate.trainSucceed();
				break;
            case HANDLER_TRAIN_FAILED:
                if(delegate != null)
                    delegate.trainFailed();
                break;
			case HANDLER_TRAIN_COMPLETED:
				if (delegate != null)
					delegate.trainCompleted();
				break;
			case HANDLER_TRAIN_ERASED:
				if (delegate != null)
					delegate.trainErased();
				break;
			case HANDLER_TRAIN_REJECTED:
				if (delegate != null)
					delegate.trainRejected();
				break;
			case HANDLER_TRAINED_RESET:
				if (delegate != null)
					delegate.trainReset();
				break;
			default:
				break;
			}
		}
	};
}
