## Emotiv SDK Community Edition Changelog

SDK CHANGELOGS

#### Version 3.5.0
* New features and improvement:
    * Renamed error code `EDK_PROFILE_CLOUD_EXISTED` to `EDK_CLOUD_PROFILE_EXISTS`
    * Removed `EDK_OVER_QUOTA_IN_DAY` and `EDK_OVER_QUOTA_IN_MONTH` error codes
    * Improved samples interpolation during packet lost
    * Improved noise handling in Performance Metric detections with Insight headset
    * Updated results of `IEE_GetAverageBandPowers()` from 2Hz to 8Hz
    * Improved accuracy of timestamp in EEG / EmoState data streams
    * New API: `IEE_DataEnableBufferForMarker()` which enables EEG/Motion buffer and allows user to set marker with a specific timestamp
    * Fixed EdfHeaderConverter tool to work with new EDF version (20170703)
      https://github.com/Emotiv/community-sdk/tree/master/tools/EEGLAB_EDF_FIXED
    * Data stream type now included in `userID` as a bit mask:
    ```
      DONGLE_STREAM_MASK   = 0x1000;
      BTLE_STREAM_MASK     = 0x2000;
      EDF_STREAM_MASK      = 0x3000;
      CSV_STREAM_MASK      = 0x4000;
      MEM_STREAM_MASK      = 0x5000;       
      EXTENDER_STREAM_MASK = 0x6000;
     ```
    * Support new licensing model - for details https://www.emotiv.com/developer/
* Platforms: 
    * SDK v3.5.0 support Windows, MAC OS
    * Linux, Android, IOS: still use SDK version 3.3.3
    
#### Version 3.4.0
* Bug fixes:
    * Fixed bug: cannot login with special symbol in EmotivID or password
    * Fixed bug: battery level is not stable	
	
* New features and improvement:
    * Support legacy EDK, which includes `EE_` API
    * New License API in `EmotivLicense.h`- allow accessing EEG and Performance Metric with license key
    * New Local Training Profile API in `IEmotivProfile.h` - allow saving/loading a training profile to local device
    * New Cloud Training Profile API in `EmotivCloudClient.h`
    * Improved timestamp accuracy for EEG and Motion data
    * Improved marker timestamp accuracy
    * Changed `EC_UpdateUserProfile` to return `EDK_` error code
    * New API: `IEE_HeadsetGetSerialNumber` and `IEE_DisconnectDevice()`
    * Merged `EC_` error code in `EmotivCloudErrorCode.h` into `EDK_` error code in `IedkErrorCode.h`
    * Deprecated `EmotivCloudErrorCode.h`
    * Removed `IEE_GetInsightSignalStrength()` and `IEE_GetEpocPlusSignalStrength()`
    * Added `PM_INTEREST` and `PM_FOCUS` for `IS_PerformanceMetricIsActive()`
	
* Known issues:
    * BTLE connection is not stable on Windows 10 due to intermediate packet lost
    
* Platforms: 
    * SDK v3.4.0 support Windows, MAC OS
    * Linux, Android, IOS: still use SDK version 3.3.3

#### Version 3.3.3
* Bug fixes:
  * Fixed bug in `IS_GetHeadsetOn` and `IS_GetBatteryChargeLevel` which always return 1
  * Fixed bug that prevents connecting headset with firmware version `0x5xx`
  * Fixed `IEE_EngineRemoteConnect()` connection from iOS to Mac (pending on other platforms)
* New features and improvement:
  * New API: `IEE_HeadsetGetSerialNumber`
  * Speed optimization with EmotivCloudClient API and it now returns error code for easier debugging
  * Support iOS Simulator (x86_64 and i386 architectures)

#### Version 3.3.2
* Bug fixes:
  * Fixed unexpected failure in Mental Commands training
  * Fixed connection error for examples on Mac
* Improvement:
  * Support cmake on Ubuntu and Debian/armhf in examples
  * Add BlueAvatar and EmoCube for examples on Mac

#### Version 3.3.1
* New features and improvement:
  * Improved BTLE connection on all platforms
  * Improved noise tolerance for Mental Commands with Insight
  * Added `IEE_GetHeadsetSettings` and `IEE_SetHeadsetSettings` for EPOC+
  * Added `EC_ReconnectEngine` and `EC_DisconnectEngine` in EmotivCloudClient.h
  * Added `FE_LAUGH`, `FE_SMIRK_LEFT`, `FE_SMIRK_RIGHT` into detection list of Facial Expression
  * Added `IS_FacialExpressionIsLookingLeft`, `IS_FacialExpressionIsLookingRight` for EPOC/EPOC+.
  * Removed param `profileName` from `EC_UpdateUserProfile`
  * Added `IEE_GetInsightDeviceState` and `IEE_GetEpocPlusDeviceState` (Mac, iOS, Android)
  * Added multi-app support (Mac, iOS, Android)
  * Supports Ubuntu 15.10 64-bit
  * Supports Debian 8.3 Jessie (armhf) on Raspberry Pi 2
* Bug fixes:
  * Fixed bug when uploading/downloading profile on 64-bit platform
  * Fixed incorrect battery level display with Insight (require new firmware )
  * Fixed bug with Surprise and Frown detections
  * Fixed motion data values retrieved from EPOC+
  * Fixed bug that prevent Insight to work with Extender on Mac
  * Fixed bug that prevent connecting to headset via BTLE on Linux
  * Swapped Gyro Y and Z in motion data with Insight
* Known Issues:
  * EPOC+ motion data may have packet lost on Android
  * EPOC+ only works with dongle version `0x06ff`, not with `0x1fff` or `0x0565`
  * Changing the settings on EPOC+ with firmware `0x610` may fail and eventually turns the headset into a brick (!). Please contact hello@emotiv.com to arrange for a firmware upgrade

#### Version 3.3.0
* Supports saving/loading profile to Emotiv Cloud (see EmotivCloudClient.h)
* New API `IEE_GetAverageBandPowers` to get average band powers for:
  * theta (4-8 Hz)
  * alpha (8-12 Hz)
  * low-beta (12-16 Hz)
  * high-beta (16-25 Hz)
  * gamma (25-45 Hz)
* Supports Ubuntu 14.04 64-bit

#### Version 3.2.0
* Supports EPOC (over Emotiv USB dongle)
* Supports Insight and EPOC+ (over BTLE and Emotiv USB dongle)
