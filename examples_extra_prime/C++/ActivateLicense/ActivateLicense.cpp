/****************************************************************************
**
** Copyright 2017 by Emotiv. All rights reserved
** Example - ActivateLicense
** How to activate a license key and Get infomation of that license 
**
****************************************************************************/

#ifdef _WIN32
    #include <windows.h>
    #include <conio.h>
#endif

#if __linux__ || __APPLE__
#include <unistd.h>
#include <termios.h>
int _getch(void);
#endif

#include <iostream>
#include <sstream>
#include <time.h>
#include <stdio.h>
#include <ctime>

#include "Iedk.h"
#include "IedkErrorCode.h"
#include "EmotivCloudClient.h"

std::string convertEpochToTime(time_t epochTime, std::string format = "%Y-%m-%d %H:%M:%S");
std::string convertEpochToTime(time_t epochTime, std::string format)
{
    if (format == "")
        format = "%Y-%m-%d %H:%M:%S";


    char timestamp[64] = { 0 };
    strftime(timestamp, sizeof(timestamp), format.c_str(), localtime(&epochTime));
    return timestamp;
}

std::string intToHex(int x)
{
    std::stringstream stream;
    stream << std::hex << x;
    std::string result(stream.str());
    result = "0x" + result;

    return result;
}

void printLicenseInformation(IEE_LicenseInfos_t& licenseInfos)
{
    int licenseType = 0;

    std::cout << std::endl;
    std::cout << "From Date         : " << convertEpochToTime(licenseInfos.date_from) << std::endl;
    std::cout << "To   Date         : " << convertEpochToTime(licenseInfos.date_to) << std::endl;
    std::cout << std::endl;

    std::cout << std::endl;
    std::cout << "Soft Limit Date   : " << convertEpochToTime(licenseInfos.soft_limit_date) << std::endl;
    std::cout << "Hard Limit Date   : " << convertEpochToTime(licenseInfos.hard_limit_date) << std::endl;
    std::cout << std::endl;

    std::cout << "Number of Seats   : " << licenseInfos.seat_count << std::endl;
    std::cout << std::endl;

    std::cout << "Total Quota       : " << licenseInfos.quota << std::endl;
    std::cout << "Total used Quota  : " << licenseInfos.usedQuota << std::endl;
    std::cout << std::endl;

    switch (licenseInfos.scopes)
    {
    case IEE_EEG:
        licenseType = IEE_LicenseType_t::IEE_EEG;

        std::cout << "License type  : " << "EEG" << std::endl;
        std::cout << std::endl;
        break;
    case IEE_EEG_PM:
        licenseType = IEE_LicenseType_t::IEE_EEG_PM;

        std::cout << "License type  : " << "EEG + PM" << std::endl;
        std::cout << std::endl;
        break;
    case IEE_PM:
        licenseType = IEE_LicenseType_t::IEE_PM;
        std::cout << "License type  : " << "PM" << std::endl;
        std::cout << std::endl;
        break;
    default:
        std::cout << "License type  : " << "No type" << std::endl;
        std::cout << std::endl;
        break;
    }
}

std::string const LICENSE_KEY = "";
                                 

int main(int argc, char** argv)
{

    //Login request
    std::string userName = "";
    std::string password = "";
    int result = 0;

    if (IEE_EngineConnect() != EDK_OK) {
        std::cout << "Emotiv Driver start up failed.";
        return -1;
    }

    if (EC_Connect() != EDK_OK)
    {
        std::cout << "Cannot connect to Emotiv Cloud";
        return -1;
    }
    //Login
    if (EC_Login(userName.c_str(), password.c_str()) != EDK_OK)
    {
        std::cout << "Login failed. The username or password may be incorrect" << std::endl;
        return -1;
    }

    //get Debit info
    IEE_DebitInfos_t debitInfos;
    debitInfos.remainingSessions = 0;
    result = IEE_GetDebitInformation(LICENSE_KEY.c_str(), &debitInfos);

    if (debitInfos.total_session_inYear == 0)
    {
        std::cout << std::endl;
        std::cout << "Remaining Sessions in month : " << debitInfos.remainingSessions << std::endl;
        std::cout << "Daily debit limitation      : " << debitInfos.daily_debit_limit << std::endl;
        std::cout << std::endl;

        std::cout << std::endl;
        std::cout << "Total debit today           : " << debitInfos.total_debit_today << std::endl;
        std::cout << "Remaining time before resetting daily debit limitation: " << debitInfos.time_reset << "(seconds)" << std::endl;
        std::cout << std::endl;
    }    
    else {
        std::cout << std::endl;
        std::cout << "Remaining Sessions in year : " << debitInfos.remainingSessions << std::endl;
        std::cout << "the total number of session can be debitable in year : " << debitInfos.total_session_inYear << std::endl;
        std::cout << std::endl;
    }


    //Active license with debit
    unsigned int debitNum = 0; //default value

    //Get number of debit as input
    std::string input;
    std::cout << "Please give number of debits : " << std::endl;

    std::getline(std::cin, input, '\n');
    debitNum = atoi(input.c_str());

    result = IEE_AuthorizeLicense(LICENSE_KEY.c_str(), debitNum);

    //show result after authorizing
    switch (result)
    {
    case EDK_INVALID_DEBIT_NUMBER:
    case EDK_INVALID_DEBIT_ERROR:
        std::cout << "Invalid number of Debits" << std::endl;
        break;
    case EDK_INVALID_PARAMETER:
        std::cout << "Invalid user information" << std::endl;
        break;
    case EDK_NO_INTERNET_CONNECTION:
        std::cout << "No Internet Connection" << std::endl;
        break;
    case EDK_LICENSE_EXPIRED:
        std::cout << "Expired license" << std::endl;
        break;
    case EDK_OVER_DEVICE_LIST:
        std::cout << "Over device list" << std::endl;
        break;  
    case EDK_DAILY_DEBIT_LIMITED:
        std::cout << "Over daily number of debits" << std::endl;
        break;
    case EDK_ACCESS_DENIED:
        std::cout << "Access denied" << std::endl;
        break;
    case EDK_LICENSE_REGISTERED:
        std::cout << "The License has registered" << std::endl;
        break;
    case EDK_LICENSE_ERROR:
        std::cout << "Error License" << std::endl;
        break;
    case EDK_LICENSE_NOT_FOUND:
        std::cout << "License not found" << std::endl;
        break;
    case EDK_UNKNOWN_ERROR:
        break;
    default:
        break;
    }

    if (!(result == EDK_OK || result == EDK_LICENSE_REGISTERED))
        return result;
    
    IEE_LicenseInfos_t licenseInfos;

    // We can call this API any time to check current License information
    result = IEE_LicenseInformation(&licenseInfos);

    std::cout << std::endl << "IEE_LicenseInfomation result = " << intToHex(result) << std::endl;

    switch (result)
    {
        case EDK_LICENSE_EXPIRED:
            std::cout << "Expired license" << std::endl;
            break;
        case EDK_OVER_QUOTA:
            std::cout << "Over quota" << std::endl;
            break;
        case EDK_ACCESS_DENIED:
            std::cout << "Access denined" << std::endl;
            break;
        case EDK_LICENSE_ERROR:
            std::cout << "Error License" << std::endl;
            break;
        case EDK_NO_ACTIVE_LICENSE:
            std::cout << "No active license" << std::endl;
            break;
        default:
            break;
    }
    printLicenseInformation(licenseInfos);

    _getch();

}
#ifdef __APPLE__
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
