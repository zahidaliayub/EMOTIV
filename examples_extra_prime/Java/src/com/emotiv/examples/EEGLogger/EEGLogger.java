package com.emotiv.examples.EEGLogger;
import java.io.IOException;
import java.io.PrintWriter;

import com.emotiv.Iedk.Edk;
import com.emotiv.Iedk.EdkErrorCode;
import com.emotiv.Iedk.IEegData;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;


public class EEGLogger {
	public static void main(String[] args) {
		Pointer eEvent = Edk.INSTANCE.IEE_EmoEngineEventCreate();
		Pointer eState = Edk.INSTANCE.IEE_EmoStateCreate();
		IntByReference userID = null;
		IntByReference nSamplesTaken = null;
		short composerPort = 1726;
		int option = 1;
		int state = 0;
		float secs = 1;
		boolean readytocollect = false;
		
		userID = new IntByReference(0);
		nSamplesTaken = new IntByReference(0);

		switch (option) {
		case 1: {
			if (Edk.INSTANCE.IEE_EngineConnect("Emotiv Systems-5") != EdkErrorCode.EDK_OK.ToInt()) {
				System.out.println("Emotiv Engine start up failed.");
				return;
			}
			break;
		}
		case 2: {
			System.out.println("Target IP of EmoComposer: [127.0.0.1] ");

			if (Edk.INSTANCE.IEE_EngineRemoteConnect("127.0.0.1", composerPort,
					"Emotiv Systems-5") != EdkErrorCode.EDK_OK.ToInt()) {
				System.out
						.println("Cannot connect to EmoComposer on [127.0.0.1]");
				return;
			}
			System.out.println("Connected to EmoComposer on [127.0.0.1]");
			break;
		}
		default:
			System.out.println("Invalid option...");
			return;
		}

		Pointer hData = IEegData.INSTANCE.IEE_DataCreate();
		IEegData.INSTANCE.IEE_DataSetBufferSizeInSec(secs);
		System.out.print("Buffer size in secs: ");
		System.out.println(secs);

		System.out.println("Start receiving EEG Data!");
		
		PrintWriter fout     = null;
		
        try {
        	 // create file
        	fout= new PrintWriter("EEGLogger.csv");
		    String headerFile = "IED_COUNTER, IED_INTERPOLATED, IED_RAW_CQ, IED_AF3, IED_F7, IED_F3, IED_FC5, IED_T7, " +
		                        "IED_P7, IED_O1, IED_O2, IED_P8, IED_T8, IED_FC6, IED_F4, IED_F8, IED_AF4, " + 
		                        "IED_GYROX, IED_GYROY,IED_TIMESTAMP";
		    // Writes the header to the file
		    fout.print(headerFile);
		    fout.println();
		    
		    while (true) {
				state = Edk.INSTANCE.IEE_EngineGetNextEvent(eEvent);

				// New event needs to be handled
				if (state == EdkErrorCode.EDK_OK.ToInt()) {
					int eventType = Edk.INSTANCE.IEE_EmoEngineEventGetType(eEvent);
					Edk.INSTANCE.IEE_EmoEngineEventGetUserId(eEvent, userID);

					if (eventType == Edk.IEE_Event_t.IEE_UserAdded.ToInt())
						if (userID != null) {
							System.out.println("User added");
							IEegData.INSTANCE.IEE_DataAcquisitionEnable(userID.getValue(), true);
							readytocollect = true;
						}
				} else if (state != EdkErrorCode.EDK_NO_EVENT.ToInt()) {
					System.out.println("Internal error in Emotiv Engine!");
					break;
				}

				if (readytocollect) {
					
					IEegData.INSTANCE.IEE_DataUpdateHandle(userID.getValue(), hData);

					IEegData.INSTANCE.IEE_DataGetNumberOfSample(hData, nSamplesTaken);

					if (nSamplesTaken != null) {
						if (nSamplesTaken.getValue() != 0) {

							System.out.print("Updated: ");
							System.out.println(nSamplesTaken.getValue());

							double[] data = new double[nSamplesTaken.getValue()];
							for (int sampleIdx = 0; sampleIdx < nSamplesTaken.getValue(); ++sampleIdx) {
								for (int i = 0; i < 20; i++) {

									IEegData.INSTANCE.IEE_DataGet(hData, i, data, nSamplesTaken.getValue());
									fout.printf("%f",data[sampleIdx]);
									fout.printf(",");
								}
								fout.printf("\n");

							}
						}
					}
				}
			}
		    fout.flush();    
		} catch (IOException e) {
			System.out.print("Exception");
		}finally{
			fout.close();	         
	    }
		
		Edk.INSTANCE.IEE_EngineDisconnect();
		Edk.INSTANCE.IEE_EmoStateFree(eState);
		Edk.INSTANCE.IEE_EmoEngineEventFree(eEvent);
		System.out.println("Disconnected!");
	}
}
