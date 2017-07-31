//
//  ViewController.h
//  ActivateLicense
//
//  Created by duytan on 7/7/17.
//  Cocoa GUI program demostrate activating Emotiv License
//  Compatible with SDK v3.5
//

#import <Cocoa/Cocoa.h>
#import <edk/Iedk.h>
#import <edk/EmotivCloudClient.h>
#import <edk/IedkErrorCode.h>
#import "NSTextFieldAdvanced.h"


@interface ViewController : NSViewController

@property (weak) IBOutlet NSTextField *txtUsername;
@property (weak) IBOutlet NSSecureTextField *txtPassword;
@property (weak) IBOutlet NSButton *btnLogin;
@property (weak) IBOutlet NSTextField *txtDebitNum;

@property (weak) IBOutlet NSTextField *txtAuthorizeLicenseKey;
@property (weak) IBOutlet NSButton *btnActivate;
@property (weak) IBOutlet NSTextField *lblResult;


@property (weak) IBOutlet NSTextField *remainingSession;
@property (weak) IBOutlet NSTextField *dailyDebitLimit;
@property (weak) IBOutlet NSTextField *dailyQuotaReset;
@property (weak) IBOutlet NSTextField *todayDebit;

@property (weak) IBOutlet NSTextField *scope;
@property (weak) IBOutlet NSTextField *seatCount;
@property (weak) IBOutlet NSTextField *quota;
@property (weak) IBOutlet NSTextField *usedQuota;

@property (weak) IBOutlet NSTextField *validFrom;
@property (weak) IBOutlet NSTextField *validTo;

@property (weak) IBOutlet NSTextField *softLimitDate;
@property (weak) IBOutlet NSTextField *hardLimitDate;

@property (weak) IBOutlet NSTextField *txtActivateLicenseKey;

- (IBAction)activateLicense:(id)sender;

- (IBAction)loginEmotivCloud:(id)sender;
- (IBAction)authorizeLicense:(id)sender;

- (IBAction)showLicenseInformation:(id)sender;
- (IBAction)clearStatus:(id)sender;
- (NSString *)timeFormatted:(int)totalSeconds;

@end

