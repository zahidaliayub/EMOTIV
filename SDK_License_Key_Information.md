## SDK License

* Allow a user / developer can get EEG data and PM from Emotiv Advance Library.
* Allow a application which is linked to Advance Edk library can work with EEG and PM from emotiv headset.
* Information of SDK license key:
    * Date_from and Date_to : period of a license.
    * Scopes : type of license: 2 type:
        * EEG      : Enable EEG data  -  BASIC
        * EEG - PM : Enable EEG data and Performance Metric -  PRIME
    * Seat_count : number of seat
    * Seat_info  : information of a seat

* If user dont have a license. or license expried,  they can use Advance library as a Community library.
* Need connect to the Internet once time to activate a SDK license
* 1 session = 30 min. Counting start from connect to EmoEngine.  Stop when disconnect to EmoEngine
* Allow a user / developer can get EEG data and PM from Emotiv Advance Library.
* Allow a application which is linked to Advance Edk library can work with EEG and PM from emotiv headset.
* If user dont have a license. or license expried,  they can use Advance library as a Community library.       
* Need connect to the Internet to authorize a SDK license

* Information of SDK license:
    * Date_from and Date_to : period of a license.
    * Scopes : type of license: 2 type:
        * EEG: Enable EEG data  -  ADVANCED
        * EEG - PM : Enable EEG data and Performance Metric -  PRIME
    * Seat number : number of seat
    * Soft limit date: When you reach the end of your monthly subscription cycle, the library needs to 
	    check the status of your subscription online to reauthorize your license. We recognize that our 
		users may not be able to go online at all times conveniently, so we have built in a 7-day grace 
		period where you can continue to use SDK library as usual after the end of the current license 
		period is reached.
		After you authorize the license, your current quota will be reset to the debit number.
    * Hard limit date: If you do not authorize before your grace period expires (7 days) you will no 
	    longer be able to use the library for EEG stream or PM(full rate).
        In order to continue getting EEG or PM(full rate) you will need to authorize license with a 
		number of debit.
		
* Debit information:
    * Remaining Sessions: remaining session number of the license in the Cloud.
    * Daily debit limit : the maximum of session debit number per day.
    * Total debit today : the number of session debited today.
    * Time to reset     : remaining time to reset daily debit (seconds).

 * 1 session = 30 min.
    * For Basic, Advanced license: Performance Metric updated each 10 seconds.
    * For Advanced license: start counting session at the first call IEE_DataUpdateHandle()
        Stop counting session at call IEE_EngineDisconnect();
    * For Prime license: allows full rate of Performance Metric.
        Start counting session at the first call IEE_DataUpdateHandle()
        or any APIs in IEmoStatePerformanceMetric class.
        Stop counting session at call IEE_EngineDisconnect();

