/****************************************************************************
** Copyright 2015 by Emotiv. All rights reserved
** Example - Multi Dongle EEGLogger
** This sample is to get synchronized eeg data from two headsets.
** The data is only written to files as two headsets are in the good condition
** ( without noise, full of battery, ... )
** It works if you have license subscription EEG
** Example ActivateLicense need to run at least one time on your computer to active your license before run this example.
****************************************************************************/

#include <iostream>
#include <fstream>
#include <sstream>
#include <map>
#include <stdexcept>
#include <thread>
#include <chrono>
#include <vector>

#ifdef _WIN32
#include <windows.h>
#include <conio.h>
#endif

#include "IEmoStateDLL.h"
#include "Iedk.h"
#include "IEegData.h"
#include "IedkErrorCode.h"

using namespace std;

// Set the channels
IEE_DataChannel_t targetChannelList[] = {
		IED_COUNTER,
        IED_INTERPOLATED,
        IED_RAW_CQ,
        IED_AF3,
        IED_T7,
        IED_Pz,
        IED_T8,
        IED_AF4,
        IED_TIMESTAMP,
        IED_MARKER,
        IED_SYNC_SIGNAL
	};

// This will go at the top of the output files
const char header[] = "COUNTER, INTERPOLATED, RAW_CQ, AF3,"
	"T7, Pz, T8, AF4, TIMESTAMP, MARKER, SYNC_SIGNAL";

bool IsHeadset1On  = false;
bool IsHeadset2On  = false;
bool onetime       = true;
double* data1[100]     ;
double* data2[100]     ;

unsigned int numberOfSample1 = 0;
unsigned int numberOfSample2 = 0;

#if __linux__ || __APPLE__
    #include <unistd.h>
    #include <termios.h>
    int _kbhit(void);
    int _getch(void);
#endif

int main(int argc,char** argv)
{
	// Create some structures to hold the data
	EmoEngineEventHandle eEvent = IEE_EmoEngineEventCreate();
	EmoStateHandle eState = IEE_EmoStateCreate();

	// Initialize the users
	// NOTE: Only expecting two for now
	unsigned int userID = 0;
    std::vector<std::pair<unsigned int, std::string>> userLists;

    std::cout << "===================================================================" << std::endl;
    std::cout << "Example to show how to log the EEG data from multi dongles. \n";
    std::cout << "This example is used for single headset connection.\n";
    std::cout << "Please remove all obsolete output files(.csv) before starting.\n";
    std::cout << "===================================================================" << std::endl;
	// Make sure we're connect
    if (IEE_EngineConnect() != EDK_OK) {
        throw std::runtime_error("Emotiv Driver start up failed.");
    }

	while(!_kbhit()) 
	{
		// Grab the next event.
		// We seem to mainly care about user adds and removes
		int state = IEE_EngineGetNextEvent(eEvent); 
		if( state == EDK_OK ) 
		{
			// Grab some info about the event
			IEE_Event_t eventType = IEE_EmoEngineEventGetType(eEvent);			
			IEE_EmoEngineEventGetUserId(eEvent, &userID);
				
			// Add the user to the list, if necessary				
			if (eventType == IEE_UserAdded)	
			{
				std::cout << "User added: " << userID << endl;
				IEE_DataAcquisitionEnable(userID,true);
                //add to list
                userLists.push_back(std::make_pair(userID, std::to_string(userID).append("_data.csv")));

                if (userLists.size() > 2)
                {
                    throw std::runtime_error("Too many users on demo!");
                }				
			} 
			else if (eventType == IEE_UserRemoved)
			{
				cout << "User removed: " << userID << endl;

                for (auto iter = userLists.begin(); iter != userLists.end(); ++iter)
                {
                    if (iter->first == userID)
                    {
                        userLists.erase(iter);
                        break;
                    }
                }
			}
		}
        //if user_added received -> datahandle
        for (auto iter = userLists.begin(); iter != userLists.end(); ++iter)
        {
            DataHandle eData = IEE_DataCreate();
            int result = IEE_DataUpdateHandle(iter->first, eData);           

            if (result == EDK_OK)
            {
                unsigned int nSamplesTaken = 0;
                IEE_DataGetNumberOfSample(eData, &nSamplesTaken);

                if (nSamplesTaken != 0) {
                    std::cout << "Received number of sample: " << nSamplesTaken << std::endl;
                    
                    std::ofstream ofs(iter->second, std::ios::app);
                    ofs.seekp(0, std::ios::end);
                    if (!ofs.tellp())
                        ofs << header << std::endl; //write header

                    if (ofs.is_open())
                    {
                        
                        double* data = new double[nSamplesTaken];
                        for (int sampleIdx = 0; sampleIdx < (int)nSamplesTaken; ++sampleIdx) {
                            for (int i = 0; i < sizeof(targetChannelList) / sizeof(IEE_DataChannel_t); i++) {
                                IEE_DataGet(eData, targetChannelList[i], data, nSamplesTaken);
                                ofs << data[sampleIdx] << ",";
                            }

                            ofs << std::endl;
                        }
                        ofs.close();
                        delete[] data;
                    }
                }
            }
            IEE_DataFree(eData);            
        }
        std::this_thread::sleep_for(std::chrono::milliseconds(50));
	}

	IEE_EngineDisconnect();
	IEE_EmoStateFree(eState);
	IEE_EmoEngineEventFree(eEvent);	
	return 0;
}

#ifdef __linux__
int _kbhit(void)
{
	struct timeval tv;
	fd_set read_fd;

	tv.tv_sec=0;
	tv.tv_usec=0;

	FD_ZERO(&read_fd);
	FD_SET(0,&read_fd);

	if(select(1, &read_fd,NULL, NULL, &tv) == -1)
		return 0;

	if(FD_ISSET(0,&read_fd))
		return 1;

	return 0;
}

int _getch(void)
{
	struct termios oldattr, newattr;
	int ch;

	tcgetattr(STDIN_FILENO, &oldattr);
	newattr = oldattr;
	newattr.c_lflag &= ~(ICANON | ECHO);
	tcsetattr(STDIN_FILENO, TCSANOW, &newattr);
	ch = getchar();
	tcsetattr(STDIN_FILENO, TCSANOW, &oldattr);

	return ch;
}
#endif
#ifdef __APPLE__
int _kbhit(void)
{
	struct timeval tv;
	fd_set rdfs;

	tv.tv_sec = 0;
	tv.tv_usec = 0;

	FD_ZERO(&rdfs);
	FD_SET(STDIN_FILENO, &rdfs);

	select(STDIN_FILENO + 1, &rdfs, NULL, NULL, &tv);
	return FD_ISSET(STDIN_FILENO, &rdfs);
}

int _getch(void)
{
	int r;
	unsigned char c;
	if ((r = read(0, &c, sizeof(c))) < 0)
	{
		return r;
	}
	else
	{
		return c;
	}
}
#endif
