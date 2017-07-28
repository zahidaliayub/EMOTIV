package com.emotiv.examples.ActivateLicense;

import java.text.SimpleDateFormat;
import java.util.Scanner;

import com.emotiv.Iedk.*;

public class ActivateLicense {

    public static void main(String[] args) {
        
        System.out.println("Activate SDK License" );
        int result = 0;
        
        Scanner input = new Scanner(System.in);
        
        System.out.println("username: ");
        String userName = input.nextLine();
        
        System.out.println("password: ");
        String password = input.nextLine();
        
        if (Edk.INSTANCE.IEE_EngineConnect("Emotiv Systems-5") != EdkErrorCode.EDK_OK
                .ToInt()) {
            System.out.println("Emotiv Engine start up failed.");
            return;
        }
        if(EmotivCloudClient.INSTANCE.EC_Connect() != EdkErrorCode.EDK_OK.ToInt())
        {
            System.out.println("Cannot connect to Emotiv Cloud");
            return;
        }

        if(EmotivCloudClient.INSTANCE.EC_Login(userName, password) != EdkErrorCode.EDK_OK.ToInt())
        {            
            System.out.println("Your login attempt has failed. The username or password may be incorrect");
            return;
        }
        
        System.out.println("Logged in as " + userName);
        
        //Please put license here
        String license_ID = "";
        
        
       //Debit Information
        Edk.DebitInfos_t.ByReference debitInfos = new Edk.DebitInfos_t.ByReference();
        
        //Get Debit Information
        result = Edk.INSTANCE.IEE_GetDebitInformation(license_ID, debitInfos);
        if (result == EdkErrorCode.EDK_OK.ToInt())
        {          
            System.out.println("Remaining Sessions     : " + (debitInfos.remainingSessions & 0xffffffffL)); //convert from signed int to unsigned long
            System.out.println("Daily debit limitation : " + debitInfos.daily_debit_limit);
            System.out.println();

            System.out.println("Total debits today     : " + debitInfos.total_debit_today);
            System.out.println("Remaining time (seconds) before resetting daily debit limitation: " + debitInfos.time_reset );
            System.out.println();
        }
        else
        {
        	System.out.println("Get Debit Information unsuccessfully");
        }
        
        System.out.println("Authorize a license with number of debit");
        
        System.out.println("Number of debits: ");
        String debitNum = input.nextLine();
        input.close();
        
        //Authorize License with debit number
        result = Edk.INSTANCE.IEE_AuthorizeLicense(license_ID, Integer.parseInt(debitNum));
        
        if(result == EdkErrorCode.EDK_LICENSE_NOT_FOUND.ToInt())
        {
            System.out.println("AuthorizeLicense: EDK_LICENSE_NOT_FOUND" );
            System.out.println();
        }
        else if (result == EdkErrorCode.EDK_LICENSE_ERROR.ToInt())
        {
            System.out.println("AuthorizeLicense: EDK_LICENSE_ERROR" );
            System.out.println();
        }
        else if (result == EdkErrorCode.EDK_LICENSE_EXPIRED.ToInt())
        {
            System.out.println("AuthorizeLicense: EDK_LICENSE_EXPIRED" );
            System.out.println();
        }
        else if (result == EdkErrorCode.EDK_LICENSE_REGISTERED.ToInt())
        {
            System.out.println("AuthorizeLicense: EDK_LICENSE_REGISTERED" );
            System.out.println();
        }
        else if (result == EdkErrorCode.EDK_LICENSE_DEVICE_LIMITED.ToInt())
        {
            System.out.println("AuthorizeLicense: EDK_LICENSE_DEVICE_LIMITED" );
            System.out.println();
        }
        else if (result == EdkErrorCode.EDK_UNKNOWN_ERROR.ToInt())
        {
            System.out.println("AuthorizeLicense: EDK_UNKNOWN_ERROR" );
            System.out.println();
        }
        else
        {
            System.out.println();
        }
        
        if (!(result == EdkErrorCode.EDK_OK.ToInt() || result == EdkErrorCode.EDK_LICENSE_REGISTERED.ToInt()))
            return ;
        
        System.out.println("Get current license information");
        
        Edk.LicenseInfos_t.ByReference licenseInfos = new Edk.LicenseInfos_t.ByReference();
        
        //Get License Information
        result = Edk.INSTANCE.IEE_LicenseInformation(licenseInfos);       

        if (result == EdkErrorCode.EDK_LICENSE_EXPIRED.ToInt())
        {
            System.out.println("LicenseInformation: EDK_LICENSE_EXPIRED" );
            System.out.println();
        }
        else if (result == EdkErrorCode.EDK_OVER_QUOTA.ToInt())
        {
            System.out.println("LicenseInformation: EDK_OVER_QUOTA" );
            System.out.println();
        }
        else if (result == EdkErrorCode.EDK_ACCESS_DENIED.ToInt())
        {
            System.out.println("LicenseInformation: EDK_ACCESS_DENIED" );
            System.out.println();
        }
        else if (result == EdkErrorCode.EDK_LICENSE_ERROR.ToInt())
        {
            System.out.println("LicenseInformation: EDK_LICENSE_ERROR" );
            System.out.println();
        }
        else if (result == EdkErrorCode.EDK_NO_ACTIVE_LICENSE.ToInt())
        {
            System.out.println("LicenseInformation: EDK_NO_ACTIVE_LICENSE" );
            System.out.println();
        }
        else
        {
            System.out.println();
        }
        //Convert to Epoc time to Date time UTC
        String date_from = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(licenseInfos.date_from*1000L)); //multiple 1000 because convert to milisecond
        String date_to = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(licenseInfos.date_to*1000L));
        String soft_limit_date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(licenseInfos.soft_limit_date*1000L));
        String hard_limit_date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(licenseInfos.hard_limit_date*1000L));

        System.out.println("From date              : " + date_from );
        System.out.println();
        System.out.println("To   date              : " + date_to );
        System.out.println();

        System.out.println("Number of seats        : " + licenseInfos.seat_count );
        System.out.println();

        System.out.println("Total quotas           : " + licenseInfos.quota );
        System.out.println("Total used quotas      : " + licenseInfos.usedQuota );
        System.out.println();

        System.out.println("Soft Limit Date        : " + soft_limit_date );
        System.out.println("Hard Limit Date        : " + hard_limit_date );
        System.out.println();

        if(licenseInfos.scopes == 1)
        {
            System.out.println("License type       : " + "EEG" );
            System.out.println();
        }
        else if (licenseInfos.scopes == 2)
        {
             System.out.println("License type      : " + "PM" );
                System.out.println();
        }
        else if (licenseInfos.scopes == 3)
        {
            System.out.println("License type       : " + "EEG + PM" );
            System.out.println();
        }
        else
        {
            System.out.println("License type       : " + "No type" );
            System.out.println();
        }
               
    }

}
