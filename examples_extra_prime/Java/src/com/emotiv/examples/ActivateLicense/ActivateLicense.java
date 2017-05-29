package com.emotiv.examples.ActivateLicense;

import java.text.SimpleDateFormat;
import java.util.Scanner;

import com.emotiv.Iedk.*;
import com.emotiv.Iedk.EdkErrorCode;

public class ActivateLicense {

    public static void main(String[] args) {
        
        System.out.println("Activate SDK License" );
        
        Scanner input = new Scanner(System.in);
        
        System.out.println("username: ");
        String userName = input.nextLine();
        
        System.out.println("password: ");
        String password = input.nextLine();
        
        System.out.println("number of debit: ");
        String debitNum = input.nextLine();
        input.close();
        
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

        
        String license_ID = "";
        
        int result = Edk.INSTANCE.IEE_AuthorizeLicense(license_ID, Integer.parseInt(debitNum));
        
        if(result == EdkErrorCode.EDK_LICENSE_NOT_FOUND.ToInt())
        {
            System.out.println("EDK_LICENSE_NOT_FOUND" );
            System.out.println();
        }
        else if (result == EdkErrorCode.EDK_LICENSE_ERROR.ToInt())
        {
            System.out.println("EDK_LICENSE_ERROR" );
            System.out.println();
        }
        else if (result == EdkErrorCode.EDK_LICENSE_EXPIRED.ToInt())
        {
            System.out.println("EDK_LICENSE_EXPIRED" );
            System.out.println();
        }
        else if (result == EdkErrorCode.EDK_LICENSE_REGISTERED.ToInt())
        {
            System.out.println("EDK_LICENSE_REGISTERED" );
            System.out.println();
        }
        else if (result == EdkErrorCode.EDK_LICENSE_DEVICE_LIMITED.ToInt())
        {
            System.out.println("EDK_LICENSE_DEVICE_LIMITED" );
            System.out.println();
        }
        else if (result == EdkErrorCode.EDK_UNKNOWN_ERROR.ToInt())
        {
            System.out.println("EDK_UNKNOWN_ERROR" );
            System.out.println();
        }
        else
        {
            System.out.println();
        }
        
        if (!(result == EdkErrorCode.EDK_OK.ToInt() || result == EdkErrorCode.EDK_LICENSE_REGISTERED.ToInt()))
            return ;
        
        Edk.LicenseInfos_t.ByReference licenseInfos = new Edk.LicenseInfos_t.ByReference();
        
        //Get License Information
        result = Edk.INSTANCE.IEE_LicenseInformation(licenseInfos);
        

        if (result == EdkErrorCode.EDK_LICENSE_EXPIRED.ToInt())
        {
            System.out.println("EDK_LICENSE_EXPIRED" );
            System.out.println();
        }
        else if (result == EdkErrorCode.EDK_OVER_QUOTA.ToInt())
        {
            System.out.println("EDK_OVER_QUOTA" );
            System.out.println();
        }
        else if (result == EdkErrorCode.EDK_ACCESS_DENIED.ToInt())
        {
            System.out.println("EDK_ACCESS_DENIED" );
            System.out.println();
        }
        else if (result == EdkErrorCode.EDK_LICENSE_ERROR.ToInt())
        {
            System.out.println("EDK_LICENSE_ERROR" );
            System.out.println();
        }
        else if (result == EdkErrorCode.EDK_NO_ACTIVE_LICENSE.ToInt())
        {
            System.out.println("EDK_NO_ACTIVE_LICENSE" );
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

        System.out.println("Date From               : " + date_from );
        System.out.println();
        System.out.println("Date To                 : " + date_to );
        System.out.println();

        System.out.println("Seat number             : " + licenseInfos.seat_count );
        System.out.println();

        System.out.println("Total Quota             : " + licenseInfos.quota );
        System.out.println("Total quota used        : " + licenseInfos.usedQuota );
        System.out.println();

        System.out.println("Soft Limit Date         : " + soft_limit_date );
        System.out.println("Hard Limit Date         : " + hard_limit_date );
        System.out.println();

        if(licenseInfos.scopes == 1)
        {
            System.out.println("License type        : " + "EEG" );
            System.out.println();
        }
        else if (licenseInfos.scopes == 2)
        {
             System.out.println("License type       : " + "PM" );
                System.out.println();
        }
        else if (licenseInfos.scopes == 3)
        {
            System.out.println("License type        : " + "EEG + PM" );
            System.out.println();
        }
        else
        {
            System.out.println("License type        : " + "No type" );
            System.out.println();
        }
        
        //Debit Information
        Edk.DebitInfos_t.ByReference debitInfos = new Edk.DebitInfos_t.ByReference();
        
        //Get Debit Information
        result = Edk.INSTANCE.IEE_GetDebitInformation(license_ID, debitInfos);
        

        System.out.println("Remain Session          : " + debitInfos.remainingSessions);
        System.out.println("Daily debit limit       : " + debitInfos.daily_debit_limit);
        System.out.println();

        System.out.println("Total debit a day       : " + debitInfos.total_debit_today);
        System.out.println("Remained time (seconds) before resetting debit limitation a day: " + debitInfos.time_reset );
        System.out.println();
                
    }

}
