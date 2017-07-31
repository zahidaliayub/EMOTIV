//
//  ViewController.m
//  ActivateLicense
//
//  Created by duytan on 7/7/17.
//  Cocoa GUI program demostrate activating Emotiv License
//  Compatible with SDK v3.5
//

#import "ViewController.h"


@implementation ViewController

bool loggedIn = false;
bool isOnline = false;

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    // IEE_EngineConnect and EC_Connect is required before activating license.
    IEE_EngineConnect();
    _lblResult.stringValue = @"You need to login before authorizing license";
    self.view.autoresizesSubviews = NO;
}


- (void)setRepresentedObject:(id)representedObject {
    [super setRepresentedObject:representedObject];

    // Update the view, if already loaded.
    self.view.autoresizesSubviews = NO;
}

- (void)login {
    // Conect to Emotiv Cloud
    if (EC_Connect() == EDK_OK) {
        isOnline = true;
    } else {
        _lblResult.stringValue = @"Failed to connect to Emotiv";
        _lblResult.textColor = [NSColor redColor];
    }
    
    NSInteger result = EC_Login([_txtUsername.stringValue UTF8String], [_txtPassword.stringValue UTF8String]);
    if (result == EDK_OK) {
        loggedIn = true;
        _lblResult.stringValue = @"Logged in successfully";
        _lblResult.textColor = [NSColor greenColor];
    } else {
        _lblResult.stringValue = @"Failed to login";
        _lblResult.textColor = [NSColor redColor];
    }
}



- (IBAction)loginEmotivCloud:(id)sender {
    @autoreleasepool {
        [NSThread detachNewThreadSelector:@selector(login) toTarget:self withObject:nil];
    }
}

- (NSString *)timeFormatted:(int)totalSeconds {
    int minutes = (totalSeconds / 60) % 60;
    int hours = totalSeconds / 3600;
    
    return [NSString stringWithFormat:@"%02d:%02d",hours, minutes];
}

- (void) authorizeLicense {
    if (isOnline) {
        _lblResult.textColor = [NSColor blackColor];
        _lblResult.stringValue = @"Authorizing license ... Please wait.";
        NSString * license = _txtAuthorizeLicenseKey.stringValue;
        unsigned int debitNum = (_txtDebitNum.integerValue > 0) ? (unsigned int)_txtDebitNum.integerValue : 0;
        NSInteger result = IEE_AuthorizeLicense([license UTF8String], debitNum);
        
        if (result == EDK_OK && loggedIn) {
            _lblResult.stringValue = @"Activated Successful";
            _lblResult.textColor = [NSColor greenColor];
            
            // Get debit information
            NSString * license = _txtAuthorizeLicenseKey.stringValue;
            IEE_DebitInfos_t debitInfos;
            IEE_GetDebitInformation([license UTF8String], &debitInfos);
            
            _remainingSession.integerValue = debitInfos.remainingSessions;
            _dailyDebitLimit.integerValue = debitInfos.daily_debit_limit;
            _todayDebit.integerValue = debitInfos.total_debit_today;
            _dailyQuotaReset.stringValue = [self timeFormatted:(debitInfos.time_reset)];
            
            @autoreleasepool {
                [NSThread detachNewThreadSelector:@selector(showLicenseInfo) toTarget:self withObject:nil];
            }
        } else if (!loggedIn || [_txtUsername.stringValue isEqual:@""] || [_txtPassword.stringValue isEqual:@""] ) {
            _lblResult.stringValue = @"You have to login first";
            _lblResult.textColor = [NSColor redColor];
        } else {
            switch (result)
            {
                case EDK_INVALID_DEBIT_NUMBER:
                case EDK_INVALID_DEBIT_ERROR:
                    _lblResult.stringValue = @"EDK_INVALID_DEBIT_ERROR";
                    break;
                case EDK_INVALID_PARAMETER:
                    _lblResult.stringValue = @"EDK_INVALID_PARAMETER";
                    break;
                case EDK_NO_INTERNET_CONNECTION:
                    _lblResult.stringValue = @"EDK_NO_INTERNET_CONNECTION";
                    break;
                case EDK_LICENSE_EXPIRED:
                    _lblResult.stringValue = @"EDK_LICENSE_EXPIRED";
                    break;
                case EDK_OVER_DEVICE_LIST:
                    _lblResult.stringValue = @"EDK_OVER_DEVICE_LIST";
                    break;
                case EDK_DAILY_DEBIT_LIMITED:
                    _lblResult.stringValue = @"EDK_DAILY_DEBIT_LIMITED";
                    break;
                case EDK_ACCESS_DENIED:
                    _lblResult.stringValue = @"EDK_ACCESS_DENIED";
                    break;
                case EDK_LICENSE_REGISTERED:
                    _lblResult.stringValue = @"EDK_LICENSE_REGISTERED";
                    break;
                case EDK_LICENSE_ERROR:
                    _lblResult.stringValue = @"EDK_LICENSE_ERROR";
                    break;
                case EDK_LICENSE_NOT_FOUND:
                    _lblResult.stringValue = @"EDK_LICENSE_NOT_FOUND";
                    break;
                case EDK_UNKNOWN_ERROR:
                    _lblResult.stringValue = @"EDK_UNKNOWN_ERROR";
                    break;
                default:
                    break;
            }
            _lblResult.textColor = [NSColor redColor];
        }
    } else {
        _lblResult.stringValue = @"You need to login before authorizing license";
        _lblResult.textColor = [NSColor redColor];
    }
    
    
}

- (void) showLicenseInfo {
    IEE_LicenseInfos_t licenseInfos;
    NSInteger result = IEE_LicenseInformation(&licenseInfos);
    if (result != EDK_OK) {
        switch (result)
        {
            case EDK_LICENSE_EXPIRED:
                _lblResult.stringValue = @"EDK_LICENSE_EXPIRED";
                break;
            case EDK_OVER_QUOTA:
                _lblResult.stringValue = @"EDK_OVER_QUOTA";
                break;
            case EDK_ACCESS_DENIED:
                _lblResult.stringValue = @"EDK_ACCESS_DENIED";
                break;
            case EDK_LICENSE_ERROR:
                _lblResult.stringValue = @"EDK_LICENSE_ERROR";
                break;
            case EDK_NO_ACTIVE_LICENSE:
                _lblResult.stringValue = @"EDK_NO_ACTIVE_LICENSE";
                break;
            case EDK_OK:
                _lblResult.stringValue = @"EDK_OK";
                break;
            default:
                _lblResult.stringValue = @"Cannot get license information";
                break;
        }
        _lblResult.textColor = [NSColor redColor];
    }
    _validFrom.stringValue = [[NSDate dateWithTimeIntervalSince1970:licenseInfos.date_from] description];
    _validTo.stringValue = [[NSDate dateWithTimeIntervalSince1970:licenseInfos.date_to] description];
    
    _softLimitDate.stringValue = [[NSDate dateWithTimeIntervalSince1970:licenseInfos.soft_limit_date] description];
    _hardLimitDate.stringValue = [[NSDate dateWithTimeIntervalSince1970:licenseInfos.hard_limit_date] description];
    
    _seatCount.integerValue = licenseInfos.seat_count;
    _quota.integerValue = licenseInfos.quota;
    _usedQuota.integerValue = licenseInfos.usedQuota;
    
    _lblResult.stringValue = @"License information retrieved successfully";
    _lblResult.textColor = [NSColor greenColor];
    
    switch (licenseInfos.scopes)
    {
        case IEE_EEG:
            _scope.stringValue = @"EEG";
            break;
        case IEE_EEG_PM:
            _scope.stringValue = @"EEG + PM";
            break;
        case IEE_PM:
            _scope.stringValue = @"PM";
            break;
        default:
            _scope.stringValue = @"Error!";
            _lblResult.stringValue = @"No license is activated on this machine";
            _lblResult.textColor = [NSColor redColor];
            break;
    }

}



- (IBAction)authorizeLicense:(id)sender {
    if (![_txtAuthorizeLicenseKey.stringValue isEqual: @""]) {
        @autoreleasepool {
            [NSThread detachNewThreadSelector:@selector(authorizeLicense) toTarget:self withObject:nil];
        }

    }
    _lblResult.stringValue = @"";
}


- (IBAction)showLicenseInformation:(id)sender {
    @autoreleasepool {
        [NSThread detachNewThreadSelector:@selector(showLicenseInfo) toTarget:self withObject:nil];
    }
}

- (IBAction)clearStatus:(id)sender {
    _lblResult.stringValue = @"";
    _lblResult.textColor = [NSColor blackColor];
}



- (void) activateLicense {
    _lblResult.stringValue = @"";
    if (IEE_SetActiveLicense([_txtActivateLicenseKey.stringValue UTF8String]) == EDK_OK) {
        _lblResult.stringValue = @"Activate license successfully";
        _lblResult.textColor = [NSColor greenColor];
    } else {
        _lblResult.stringValue = @"Activate license failed, have you authorized this License Key?";
        sleep(3);
        _lblResult.textColor = [NSColor redColor];
    }
    @autoreleasepool {
        [NSThread detachNewThreadSelector:@selector(showLicenseInfo) toTarget:self withObject:nil];
    }
}

- (IBAction)activateLicense:(id)sender {
    @autoreleasepool {
        [NSThread detachNewThreadSelector:@selector(activateLicense) toTarget:self withObject:nil];
    }
}

@end
