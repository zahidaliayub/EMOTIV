/****************************************************************************
**
** Copyright 2015 by Emotiv. All rights reserved
** Example - ActivateLicense
** How to activate a license key
 ***
****************************************************************************/

using System;
using System.Collections.Generic;
using Emotiv;
using System.IO;
using System.Threading;
using System.Reflection;

namespace ActivateLicense
{
    class ActivateLicense
    {
        static string licenseKey = "";           // Your License Key

        static string format = "dd/MM/yyyy";
        static Int32 debitNum = 2; //example 
        static int userCloudID = 0;
        static int userID = -1;
        static string userName = "your_UserName";
        static string password = "your_Password";
        static string profileName = "Profile_1";

        static void activateLicense()
        {
            int result = EdkDll.IEE_AuthorizeLicense(licenseKey, debitNum);
            if (result == EdkDll.EDK_OK || result == EdkDll.EDK_LICENSE_REGISTERED)
            {
                Console.WriteLine("License activated.");
            }
            else Console.WriteLine("License Error. " + result);
        }

        static public DateTime FromUnixTime(uint unixTime)
        {
            var epoch = new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc);
            return epoch.AddSeconds(unixTime);
        }

        static public long ToUnixTime(DateTime date)
        {
            var epoch = new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc);
            return Convert.ToInt64((date - epoch).TotalSeconds);
        }

        static void getDebitInformation()
        {
            EdkDll.IEE_DebitInfos_t debitInfos = new EdkDll.IEE_DebitInfos_t();
            int result = EdkDll.IEE_GetDebitInformation(licenseKey, ref debitInfos);

            Console.WriteLine();
            Console.WriteLine("Total debit a day            : " + debitInfos.total_debit_today);
            Console.WriteLine("Daily debit limit            : " + debitInfos.daily_debit_limit);
            Console.WriteLine("Remain Session               : " + debitInfos.remainingSessions);
            Console.WriteLine();
            Console.WriteLine("Remained time (seconds) before resetting debit limitation a day: " + debitInfos.time_reset);
            Console.WriteLine();
        }

        static void licenseInformation()
        {
            EdkDll.IEE_LicenseInfos_t licenseInfos = new EdkDll.IEE_LicenseInfos_t();
            int result = EdkDll.IEE_LicenseInformation(ref licenseInfos);

            Console.WriteLine();
            Console.WriteLine("Date From            : " + FromUnixTime(licenseInfos.date_from).ToString(format));
            Console.WriteLine("Date To              : " + FromUnixTime(licenseInfos.date_to).ToString(format));
            Console.WriteLine();

            Console.WriteLine();
            Console.WriteLine("Soft Limit Date      : " + FromUnixTime(licenseInfos.soft_limit_date).ToString(format));
            Console.WriteLine("Hard Limit Date      : " + FromUnixTime(licenseInfos.hard_limit_date).ToString(format));
            Console.WriteLine();

            Console.WriteLine("Seat number          : " + licenseInfos.seat_count);
            Console.WriteLine();

            Console.WriteLine("Total Quota          : " + licenseInfos.quota);
            Console.WriteLine("Total quota used     : " + licenseInfos.usedQuota);
            Console.WriteLine();

            switch ((int)licenseInfos.scopes)
            {
                case (int)EdkDll.IEE_LicenseType_t.IEE_EEG:

                    Console.WriteLine("License type : EEG");
                    Console.WriteLine();
                    break;
                case (int)EdkDll.IEE_LicenseType_t.IEE_EEG_PM:

                    Console.WriteLine("License type : EEG + PM");
                    Console.WriteLine();
                    break;
                case (int)EdkDll.IEE_LicenseType_t.IEE_PM:
                    Console.WriteLine("License type : PM");
                    Console.WriteLine();
                    break;
                default:
                    Console.WriteLine("License type : No type");
                    Console.WriteLine();
                    break;
            }
        }

        static void Main(string[] args)
        {
            Console.WriteLine("===========================================");
            Console.WriteLine("The example to activate a license key.");
            Console.WriteLine("===========================================");
            
            EmoEngine engine = EmoEngine.Instance;
            engine.Connect();

            //Authorize
            if (EmotivCloudClient.EC_Connect() != EdkDll.EDK_OK)
            {
                Console.WriteLine("Cannot connect to Emotiv Cloud.");
                Thread.Sleep(2000);
                return;
            }

            if (EmotivCloudClient.EC_Login(userName, password) != EdkDll.EDK_OK)
            {
                Console.WriteLine("Your login attempt has failed. The username or password may be incorrect");
                Thread.Sleep(2000);
                return;
            }

            Console.WriteLine("Logged in as " + userName);

            if (EmotivCloudClient.EC_GetUserDetail(ref userCloudID) != EdkDll.EDK_OK)
                return;

            //Active license
            activateLicense();

            //We can call this API any time to check current License information
            licenseInformation();

            //GetDebitInfo
            getDebitInformation();

            Thread.Sleep(5000);
        }
    }
}
