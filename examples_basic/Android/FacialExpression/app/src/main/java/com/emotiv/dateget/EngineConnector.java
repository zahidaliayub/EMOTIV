package com.emotiv.dateget;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import com.emotiv.bluetooth.*;
import com.emotiv.sdk.*;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.SyncStateContract.Constants;
import android.util.Log;

public class EngineConnector {
	public static Context context;
	private static      EngineConnector instance;
	private Timer 		timer;
	private TimerTask 	timerTask;
	public boolean 		isConnected = false;
	public boolean 		bleInUse = false;

	private int userId= 0;
	private SWIGTYPE_p_void handleEvent;
	private SWIGTYPE_p_void emoState;
    private SWIGTYPE_p_void motionDataHandle;

	protected static final int HANDLER_USER_ADDED 		= 1;
	protected static final int HANDLER_USER_REMOVED 	= 2;
	protected static final int HANDLER_EMOSTATE_UPDATE 	= 3;
	protected static final int HANDLER_FACIAL_EVENT 	= 4; // Facial
	protected static final int HANDLER_MENTAL_EVENT 	= 5; // Mental Command
    protected static final int HANDLER_PM_EVENT         = 6; // Performance Metric

    // Facial Training Events
	protected static final int HANDLER_FETRAIN_START    = 11;
	protected static final int HANDLER_FETRAIN_SUCCEED  = 12;
	protected static final int HANDLER_FETRAIN_COMPLETE = 13;
    protected static final int HANDLER_FETRAIN_FAILURED = 14;
    protected static final int HANDLER_FETRAIN_ERASED   = 15;
	protected static final int HANDLER_FETRAIN_REJECT     = 16;
	protected static final int HANDLER_FETRAIN_RESET 	  = 17;


    // Mental Command Training Events
	protected static final int HANDLER_MCTRAIN_STARTED      = 21;
	protected static final int HANDLER_MCTRAIN_SUCCEED      = 22;
	protected static final int HANDLER_MCTRAIN_FAILURED     = 23;
	protected static final int HANDLER_MCTRAIN_COMPLETED    = 24;
	protected static final int HANDLER_MCTRAIN_ERASED       = 25;
	protected static final int HANDLER_MCTRAIN_REJECTED     = 26;
	protected static final int HANDLER_MCTRAIN_RESET        = 27;
    protected static final int HANDLER_MCTRAIN_SIGN_UPDATE 	= 28;
    protected static final int HANDLER_MCTRAIN_AUTO_SAMPLING = 29;

    // Data Events
    protected static final int HANDLER_MOTION_DATA      = 41;
    protected static final int HANDLER_EEG_DATA         = 42;
    protected static final int HANDLER_BAND_POWER_DATA  = 43;

    IEE_MotionDataChannel_t[]motionChannel_list;

    IEE_DataChannel_t[] dataChannel_list;

    String[] dataName_Channel = {"AF3","T7","Pz","T8","AF4"};

	public EngineInterface	delegate;

	public static void setContext(Context context){
		EngineConnector.context = context;
	}

    public static EngineConnector shareInstance(){
		if (instance == null){
			instance = new EngineConnector();
		}
		return instance;
	}

    public EngineConnector(){
       connectEngine();
    }

	private void connectEngine(){
        Emotiv.IEE_EmoInitDevice(EngineConnector.context);
        edkJava.IEE_EngineConnect("Emotiv Systems-5");
        handleEvent = edkJava.IEE_EmoEngineEventCreate();
        emoState = edkJava.IEE_EmoStateCreate();
        motionDataHandle = edkJava.IEE_MotionDataCreate();

        IEE_MotionDataChannel_t []motionChannelTmp = {IEE_MotionDataChannel_t.IMD_COUNTER, IEE_MotionDataChannel_t.IMD_GYROX,IEE_MotionDataChannel_t.IMD_GYROY,
                IEE_MotionDataChannel_t.IMD_GYROZ, IEE_MotionDataChannel_t.IMD_ACCX, IEE_MotionDataChannel_t.IMD_ACCY, IEE_MotionDataChannel_t.IMD_ACCZ,
                IEE_MotionDataChannel_t.IMD_MAGX, IEE_MotionDataChannel_t.IMD_MAGY, IEE_MotionDataChannel_t.IMD_MAGZ, IEE_MotionDataChannel_t.IMD_TIMESTAMP};
        IEE_DataChannel_t []dataChannelTmp = {IEE_DataChannel_t.IED_AF3, IEE_DataChannel_t.IED_T7,IEE_DataChannel_t.IED_Pz,
            IEE_DataChannel_t.IED_T8,IEE_DataChannel_t.IED_AF4};

        motionChannel_list = motionChannelTmp;
        dataChannel_list = dataChannelTmp;
        timer = new Timer();
        intTimerTask();
        timer.schedule(timerTask , 10, 10);
	}

	public void saveProfile(){

	}

	public void loadProfile(){

	}

	public boolean startFacialExpression(Boolean isTrain,IEE_FacialExpressionAlgo_t FacialExpressionAction) {
		if (!isTrain) {
			if (edkJava.IEE_FacialExpressionSetTrainingAction(userId, FacialExpressionAction) == edkJava.EDK_OK) {
				if (edkJava.IEE_FacialExpressionSetTrainingControl(userId, IEE_FacialExpressionTrainingControl_t.FE_START) == edkJava.EDK_OK) {
					return true;
				}
			}
		} else {
			if (edkJava.IEE_FacialExpressionSetTrainingControl(userId,IEE_FacialExpressionTrainingControl_t.FE_RESET) == edkJava.EDK_OK) {
				return false;
			}
		}
		return false;
	}
   	public void trainningClear(IEE_FacialExpressionAlgo_t _FacialdAction) {
	   edkJava.IEE_FacialExpressionSetTrainingAction(userId, _FacialdAction);
		if (edkJava.IEE_FacialExpressionSetTrainingControl(userId,IEE_FacialExpressionTrainingControl_t.FE_ERASE) == edkJava.EDK_OK) {
		}
	}

   	public void setTrainControl(IEE_FacialExpressionTrainingControl_t type) {
		if (edkJava.IEE_FacialExpressionSetTrainingControl(userId, type) == edkJava.EDK_OK) {
		}
	}

   	public boolean checkTrained(IEE_FacialExpressionAlgo_t action){
	   boolean res = false;
	   SWIGTYPE_p_unsigned_long uValue = edkJava.new_ulong_p();
	   int result = edkJava.IEE_FacialExpressionGetTrainedSignatureActions(userId, uValue);
	   if (result == edkJava.EDK_OK) {
			long _currentTrainedActions = edkJava.ulong_p_value(uValue);
		    long y = _currentTrainedActions & action.swigValue();
			res = (y == action.swigValue());
	   }
	   edkJava.delete_ulong_p(uValue);
	   return res;
	}

	public int getEventEngineId(SWIGTYPE_p_void hEvent)
	{
		SWIGTYPE_p_unsigned_int pEngineId = edkJava.new_uint_p();
		int result = edkJava.IEE_EmoEngineEventGetUserId(handleEvent, pEngineId);
		int tmpUserId = (int)edkJava.uint_p_value(pEngineId);
		edkJava.delete_uint_p(pEngineId);
		return tmpUserId;
	}

   private void intTimerTask(){
	   if (timerTask != null) return;
	   timerTask = new TimerTask() {
		@Override
		public void run() {
            // TODO Auto-generated method stub`
			/*Connect device with Insight headset*/
            int numberDevice = Emotiv.IEE_GetInsightDeviceCount();
            if (numberDevice != 0) {
                if (!bleInUse) {
                    Emotiv.IEE_ConnectInsightDevice(0);
                    bleInUse = true;
                }
            } else {
                numberDevice = Emotiv.IEE_GetEpocPlusDeviceCount();
                if (numberDevice != 0) {
                    if (!bleInUse) {
                        Emotiv.IEE_ConnectEpocPlusDevice(0, false);
                        bleInUse = true;
                    }
                }
            }
            int state = edkJava.IEE_EngineGetNextEvent(handleEvent);
            if (state == edkJava.EDK_OK) {
                IEE_Event_t eventType = edkJava.IEE_EmoEngineEventGetType(handleEvent);
                SWIGTYPE_p_unsigned_int pEngineId = edkJava.new_uint_p();
                int result = edkJava.IEE_EmoEngineEventGetUserId(handleEvent, pEngineId);
                int tmpUserId = (int) edkJava.uint_p_value(pEngineId);
                edkJava.delete_uint_p(pEngineId);
                switch (eventType) {
                    case IEE_UserAdded:
                        Log.e("FacialExpression", "User Added");
                        isConnected = true;
                        bleInUse = true;
                        userId = tmpUserId;
                        mHandler.sendMessage(mHandler.obtainMessage(HANDLER_USER_ADDED));
                        break;
                    case IEE_UserRemoved:
                        Log.e("FacialExpression", "User Removed");
                        isConnected = false;
                        bleInUse = false;
                        userId = -1;
                        mHandler.sendMessage(mHandler.obtainMessage(HANDLER_USER_REMOVED));
                        break;
                    case IEE_EmoStateUpdated:
                        if (!isConnected) break;
                        edkJava.IEE_EmoEngineEventGetEmoState(handleEvent, emoState);
                        mHandler.sendMessage(mHandler.obtainMessage(HANDLER_EMOSTATE_UPDATE));
                        mHandler.sendMessage(mHandler.obtainMessage(HANDLER_FACIAL_EVENT));
                        mHandler.sendMessage(mHandler.obtainMessage(HANDLER_MENTAL_EVENT));
                        mHandler.sendMessage(mHandler.obtainMessage(HANDLER_PM_EVENT));

                        break;
                    case IEE_FacialExpressionEvent:
                        IEE_FacialExpressionEvent_t feType = edkJava.IEE_FacialExpressionEventGetType(handleEvent);
                        if (feType == IEE_FacialExpressionEvent_t.IEE_FacialExpressionTrainingStarted) {
                            Log.e("FacialExpression", "training started");
                            mHandler.sendMessage(mHandler.obtainMessage(HANDLER_FETRAIN_START));
                        } else if (feType == IEE_FacialExpressionEvent_t.IEE_FacialExpressionTrainingCompleted) {
                            Log.e("FacialExpression", "training completed");
                            mHandler.sendMessage(mHandler.obtainMessage(HANDLER_FETRAIN_COMPLETE));
                        } else if (feType == IEE_FacialExpressionEvent_t.IEE_FacialExpressionTrainingDataErased) {
                            Log.e("FacialExpression", "training erased");
                            mHandler.sendMessage(mHandler.obtainMessage(HANDLER_FETRAIN_ERASED));
                        } else if (feType == IEE_FacialExpressionEvent_t.IEE_FacialExpressionTrainingFailed) {
                            Log.e("FacialExpression", "training failed");
                            mHandler.sendMessage(mHandler.obtainMessage(HANDLER_FETRAIN_FAILURED));
                        } else if (feType == IEE_FacialExpressionEvent_t.IEE_FacialExpressionTrainingRejected) {
                            Log.e("FacialExpression", "training rejected");
                            mHandler.sendMessage(mHandler.obtainMessage(HANDLER_FETRAIN_REJECT));
                        } else if (feType == IEE_FacialExpressionEvent_t.IEE_FacialExpressionTrainingReset) {
                            Log.e("FacialExpression", "training reseted");
                            mHandler.sendMessage(mHandler.obtainMessage(HANDLER_FETRAIN_RESET));
                        } else if (feType == IEE_FacialExpressionEvent_t.IEE_FacialExpressionTrainingSucceeded) {
                            Log.e("FacialExpression", "training succeeded");
                            mHandler.sendMessage(mHandler.obtainMessage(HANDLER_FETRAIN_SUCCEED));
                        }
                        break;
                    case IEE_MentalCommandEvent:
                        IEE_MentalCommandEvent_t mcType = edkJava.IEE_MentalCommandEventGetType(handleEvent);
                        if (mcType == IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingStarted) {
                            Log.e("MentalCommand", "training started");
                            mHandler.sendEmptyMessage(HANDLER_MCTRAIN_STARTED);
                        } else if (mcType == IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingSucceeded) {
                            Log.e("MentalCommand", "training Succeeded");
                            mHandler.sendEmptyMessage(HANDLER_MCTRAIN_SUCCEED);
                        } else if (mcType == IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingCompleted) {
                            Log.e("MentalCommand", "training Completed");
                            mHandler.sendEmptyMessage(HANDLER_MCTRAIN_COMPLETED);
                        } else if (mcType == IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingDataErased) {
                            Log.e("MentalCommand", "training erased");
                            mHandler.sendEmptyMessage(HANDLER_MCTRAIN_ERASED);
                        } else if (mcType == IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingFailed) {
                            Log.e("MentalCommand", "training failed");
                            mHandler.sendEmptyMessage(HANDLER_MCTRAIN_FAILURED);
                        } else if (mcType == IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingRejected) {
                            Log.e("MentalCommand", "training rejected");
                            mHandler.sendEmptyMessage(HANDLER_MCTRAIN_REJECTED);
                        } else if (mcType == IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingReset) {
                            Log.e("MentalCommand", "training Reset");
                            mHandler.sendEmptyMessage(HANDLER_MCTRAIN_RESET);
                        } else if (mcType == IEE_MentalCommandEvent_t.IEE_MentalCommandAutoSamplingNeutralCompleted) {
                            mHandler.sendEmptyMessage(HANDLER_MCTRAIN_AUTO_SAMPLING);
                        } else if (mcType == IEE_MentalCommandEvent_t.IEE_MentalCommandSignatureUpdated) {
                            mHandler.sendEmptyMessage(HANDLER_MCTRAIN_SIGN_UPDATE);
                        }
                        break;
                    default:
                        break;
                }

            }// End IEE_EngineGetNextEvent

            if (!isConnected) return;
            // 1. Read motion data
            // 2. Read band power data
            // 3. Read eeg data, advanced licenses

            // Motion data
            edkJava.IEE_MotionDataUpdateHandle(userId, motionDataHandle);
            SWIGTYPE_p_unsigned_int pSamplesCount = edkJava.new_uint_p();
            int result = edkJava.IEE_MotionDataGetNumberOfSample(motionDataHandle, pSamplesCount);
            int sample = (int) edkJava.uint_p_value(pSamplesCount);
            edkJava.delete_uint_p(pSamplesCount);
            if (sample > 0) {
                double[][] motionData = new double[sample][motionChannel_list.length];
                SWIGTYPE_p_double motion_array = edkJava.new_double_array(sample);
                for (int j = 0; j < motionChannel_list.length; j++) {                    // Get motion data by channel
                    edkJava.IEE_MotionDataGet(motionDataHandle, motionChannel_list[j], motion_array, sample);
                    for (int sampleIdx = 0; sampleIdx < sample; sampleIdx++) {
                        motionData[sampleIdx][j] = edkJava.double_array_getitem(motion_array, sampleIdx);
                    }
                }
                edkJava.delete_double_array(motion_array);
                // TODO: Save motion data matrix to file ...
                // See motiondatalogger example
                for (int row = 0; row < sample; ++row) {
                    for (int col = 0; col < motionChannel_list.length; ++col) {
                        double value = motionData[row][col];
                        //addData(motionData[row][col]);
                    }
                    try {
                        //motion_writer.newLine();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                // TODO: Notify delegate -> new motion data is comming
                mHandler.sendEmptyMessage(HANDLER_MOTION_DATA);
                motionData = null;
            }
            try {
                if (dataChannel_list == null) return;
                if (dataChannel_list.length == 0) return;
                // Band Power Data
                // See FFTSample example
                boolean bpDataAvailable = false;
                SWIGTYPE_p_double ptheta = edkJava.new_double_p();
                SWIGTYPE_p_double palpha = edkJava.new_double_p();
                SWIGTYPE_p_double plow_beta = edkJava.new_double_p();
                SWIGTYPE_p_double phigh_beta = edkJava.new_double_p();
                SWIGTYPE_p_double pgamma = edkJava.new_double_p();
                for (int i = 0; i < dataChannel_list.length; ++i) {
                    int resultBandPower = -1;
                    //resultBandPower = edkJava.IEE_GetAverageBandPowers(userId, dataChannel_list[i], ptheta, palpha, plow_beta, phigh_beta, pgamma);
                    if (resultBandPower == edkJava.EDK_OK) {
                        bpDataAvailable = true;
                        try {
//                        bp_writer.write(dataName_Channel[i] + ",");
//                        addData(edkJava.double_p_value(ptheta));
//                        addData(edkJava.double_p_value(palpha));
//                        addData(edkJava.double_p_value(plow_beta));
//                        addData(edkJava.double_p_value(phigh_beta));
//                        addData(edkJava.double_p_value(pgamma));
//                        bp_writer.newLine();
                            // TODO: Save band power data
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (bpDataAvailable) {
                    // Notify delegate -> new band power data
                    mHandler.sendEmptyMessage(HANDLER_BAND_POWER_DATA);
                }
                edkJava.delete_double_p(ptheta);
                edkJava.delete_double_p(palpha);
                edkJava.delete_double_p(plow_beta);
                edkJava.delete_double_p(phigh_beta);
                edkJava.delete_double_p(pgamma);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
	};

   }

   // handler send to delegate
   public Handler mHandler = new Handler() {
       public void handleMessage(Message msg) {
            if (delegate == null) {
                return;
            }
       switch (msg.what) {
            case HANDLER_USER_ADDED:
                if (delegate!= null) {
                   delegate.userAdded(userId);
                }
                break;
            case HANDLER_USER_REMOVED:
                if (delegate!= null) {
                   delegate.userRemove();
                }
                break;
            case HANDLER_EMOSTATE_UPDATE:
                if (delegate!= null) {
                   // Do nothing
                    Log.e("FacialExpression", "EmoStateUpdate");
                }
                break;
            case HANDLER_FACIAL_EVENT:
                IEE_FacialExpressionAlgo_t feLowerAction = edkJava.IS_FacialExpressionGetLowerFaceAction(emoState);
                float feLowerPower = edkJava.IS_FacialExpressionGetLowerFaceActionPower(emoState);

                IEE_FacialExpressionAlgo_t feUpperAction = edkJava.IS_FacialExpressionGetUpperFaceAction(emoState);
                float feUpperPower = edkJava.IS_FacialExpressionGetUpperFaceActionPower(emoState);

                if (delegate != null) {
                   delegate.detectedActionLowerFace(feLowerAction, feLowerPower);
                }
                if (edkJava.IS_FacialExpressionIsBlink(emoState) == 1) {
                   Log.e("FacialExpression", "Blink");
                }
                if (edkJava.IS_FacialExpressionIsEyesOpen(emoState) == 1){
                   Log.e("FacialExpression", "EyesOpen");
                }
                break;
            case HANDLER_MENTAL_EVENT:
                IEE_MentalCommandAction_t action = edkJava.IS_MentalCommandGetCurrentAction(emoState);
                float power = edkJava.IS_MentalCommandGetCurrentActionPower(emoState);
                if (delegate!= null) {
                   //delegate.onCurrentAction(action, power);
                }
                break;
            case HANDLER_PM_EVENT:
                {
                   // Read Performance Metrics Data, advanced licenses
                   double rawScore=0;
                   double minScale=0;
                   double maxScale=0;
                   SWIGTYPE_p_double prawScore = edkJava.new_double_p();
                   SWIGTYPE_p_double pminScale = edkJava.new_double_p();
                   SWIGTYPE_p_double pmaxScale = edkJava.new_double_p();

                   // Uncomment to obtains PM data
                   //edkJava.IS_PerformanceMetricGetStressModelParams(emoState, prawScore, pminScale, pmaxScale);
                   rawScore = edkJava.double_p_value(prawScore);
                   minScale = edkJava.double_p_value(pminScale);
                   maxScale = edkJava.double_p_value(pmaxScale);

                   // --- Similar above code
                   //edkJava.IS_PerformanceMetricGetEngagementBoredomModelParams(emoState, prawScore, pminScale, pmaxScale);
                   rawScore = edkJava.double_p_value(prawScore);
                   minScale = edkJava.double_p_value(pminScale);
                   maxScale = edkJava.double_p_value(pmaxScale);

                   //edkJava.IS_PerformanceMetricGetRelaxationModelParams(emoState, prawScore, pminScale, pmaxScale);
                   rawScore = edkJava.double_p_value(prawScore);
                   minScale = edkJava.double_p_value(pminScale);
                   maxScale = edkJava.double_p_value(pmaxScale);

                   //edkJava.IS_PerformanceMetricGetInstantaneousExcitementModelParams(emoState, prawScore, pminScale, pmaxScale);
                   rawScore = edkJava.double_p_value(prawScore);
                   minScale = edkJava.double_p_value(pminScale);
                   maxScale = edkJava.double_p_value(pmaxScale);

                   //edkJava.IS_PerformanceMetricGetInterestModelParams(emoState, prawScore, pminScale, pmaxScale);
                   rawScore = edkJava.double_p_value(prawScore);
                   minScale = edkJava.double_p_value(pminScale);
                   maxScale = edkJava.double_p_value(pmaxScale);

                   edkJava.delete_double_p(prawScore);
                   edkJava.delete_double_p(pminScale);
                   edkJava.delete_double_p(pmaxScale);
                }
                break;
// Facial Training Event
            case HANDLER_FETRAIN_START:
				if (delegate!= null) {
	   	        	delegate.trainStarted();
				}
				break;
			case HANDLER_FETRAIN_SUCCEED:
				if (delegate!= null) {
	   	        	delegate.trainSucceed();
				}
				break;
			case HANDLER_FETRAIN_ERASED:
				if (delegate!= null) {
	   	        	delegate.trainErased();
				}
				break;
		   case HANDLER_FETRAIN_REJECT:
			   if(delegate != null)
			   {
				   delegate.trainRejected();
			   }
			   break;
           case HANDLER_FETRAIN_COMPLETE:
				if (delegate!= null) {
	   	        	delegate.trainCompleted();
				}
				break;
           case HANDLER_FETRAIN_FAILURED:
               if (delegate!= null) {
                   //delegate.trainFailured();
               }
               break;
           case HANDLER_FETRAIN_RESET :
				if (delegate!= null) {
	   	        	delegate.trainReset();
				}
				break;
// Mental Command Training Event
           case HANDLER_MCTRAIN_STARTED:
           case HANDLER_MCTRAIN_SUCCEED:
           case HANDLER_MCTRAIN_FAILURED:
           case HANDLER_MCTRAIN_COMPLETED:
           case HANDLER_MCTRAIN_ERASED:
           case HANDLER_MCTRAIN_REJECTED:
           case HANDLER_MCTRAIN_RESET:
           case HANDLER_MCTRAIN_AUTO_SAMPLING:
           case HANDLER_MCTRAIN_SIGN_UPDATE:
               break;
// Data Events
           case HANDLER_MOTION_DATA:
               Log.e("FacialExpression", "Motion Data is comming");
               break;
           case HANDLER_EEG_DATA:
               break;
           case HANDLER_BAND_POWER_DATA:
               Log.e("FacialExpression", "Band Power Data is comming");
               break;
           default:
			  break;
		  }
       }
   } ;
}
