//
//  ViewController.m
//  MotionDataLogger
//
//  Created by emotiv on 4/22/15.
//  Copyright (c) 2015 emotiv. All rights reserved.
//
/** This Example to show how to log the EEG data from EmoEngine
 *  It works if you have license subscription EEG
 */

#import "ViewController.h"
#import <edk/Iedk.h>
#import <edk/IEegData.h>
#import <edk/EmotivLicense.h>

#include <string>
#include <iostream>
#include <fstream>

/*****************************************************/

/*If headset Insight only 5 channel AF3, AF4, T7, T8, Pz = O1 have value. 
               Another channels EEG is zero */

/*****************************************************/

IEE_DataChannel_t targetChannelList[] = {
    IED_COUNTER,
    IED_AF3, IED_F7, IED_F3, IED_FC5, IED_T7, IED_P7, IED_O1, IED_O2,
    IED_P8, IED_T8, IED_FC6, IED_F4, IED_F8, IED_AF4, IED_GYROX,IED_GYROY,
    IED_TIMESTAMP, IED_FUNC_ID, IED_FUNC_VALUE, IED_MARKER, IED_SYNC_SIGNAL
};

BOOL isConnected = NO;
const char header[] = "COUNTER, AF3, F7, F3, FC5, T7, P7, O1, O2, P8, T8, FC6, F4, F8, AF4, GYROX, GYROY, TIMESTAMP, FUNC_ID, FUNC_VALUE, MARKER, SYNC_SIGNAL";

@implementation ViewController

EmoEngineEventHandle eEvent;
EmoStateHandle eState;
DataHandle hData;

float secs							= 1;
bool readytocollect					= false;
int state                           = 0;
int licenseType = 0;

std::ofstream ofs;
/**
 * Set your license 
 */

- (void)viewDidLoad {
    [super viewDidLoad];
    
    eEvent	= IEE_EmoEngineEventCreate();
    eState	= IEE_EmoStateCreate();
    hData   = IEE_DataCreate();
    
    IEE_EmoInitDevice();
    IEE_EngineConnect();
    std::string fileName = "EEGDataLogger.csv";
    ofs.open(fileName,std::ios::trunc);
    ofs << header << std::endl;
    IEE_DataSetBufferSizeInSec(secs);
    
    [NSTimer scheduledTimerWithTimeInterval:1.0 target:self selector:@selector(ConnectDevice) userInfo:nil repeats:YES];
    [NSThread detachNewThreadSelector:@selector(getNextEvent) toTarget:self withObject:nil];
    // Do any additional setup after loading the view.
}

-(void) getNextEvent {
    while (true) {
        int state = IEE_EngineGetNextEvent(eEvent);
        unsigned int userID = 0;
    
        if (state == EDK_OK)
        {
            IEE_Event_t eventType = IEE_EmoEngineEventGetType(eEvent);
            IEE_EmoEngineEventGetUserId(eEvent, &userID);
        
            // Log the EmoState if it has been updated
            if (eventType == IEE_UserAdded)
            {
                NSLog(@"User Added");
                self.labelStatus.stringValue = @"Connected";
                IEE_DataAcquisitionEnable(userID, true);
                readytocollect = TRUE;
            }
            else if (eventType == IEE_UserRemoved)
            {
                NSLog(@"User Removed");
                self.labelStatus.stringValue = @"Disconnected";
                readytocollect = FALSE;
            }
            if (readytocollect)
            {
                IEE_DataUpdateHandle(userID, hData);
            
                unsigned int nSamplesTaken=0;
                IEE_DataGetNumberOfSample(hData,&nSamplesTaken);
            
                std::cout << " Updated " << nSamplesTaken << "\n";
                if (nSamplesTaken != 0)
                {
                
                    std::unique_ptr<double> ddata(new double[nSamplesTaken]);
                    for (int sampleIdx=0 ; sampleIdx<(int)nSamplesTaken ; ++sampleIdx) {
                        for (int i = 0 ; i<sizeof(targetChannelList)/sizeof(IEE_DataChannel_t) ; i++) {
                            IEE_DataGet(hData, targetChannelList[i], ddata.get(), nSamplesTaken);
                            ofs << ddata.get()[sampleIdx] << ",";
                        }
                        ofs << std::endl;
                    }
                }
            }
        }
        
    }
}

- (void)setRepresentedObject:(id)representedObject {
    [super setRepresentedObject:representedObject];

    // Update the view, if already loaded.
}

-(void)ConnectDevice{
    /**This function to connect headset in mode Bluetooth*/
    /*Connect with Insight headset in mode Bluetooth*/
        int numberDevice = IEE_GetInsightDeviceCount();
        if(numberDevice > 0 && !isConnected) {
            IEE_ConnectInsightDevice(0);
            isConnected = YES;
        }
    /************************************************/
    /*Connect with Epoc Plus headset in mode Bluetooth*/
//    int numberDevice = IEE_GetEpocPlusDeviceCount();
//    if(numberDevice > 0 && !isConnected) {
//        IEE_ConnectEpocPlusDevice(0);
//        isConnected = YES;
//    }
    /************************************************/
    else isConnected = NO;

}

@end
