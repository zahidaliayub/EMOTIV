## SDK License

* Allow a user can get EEG data and PM from Emotiv Advance Library.
* Allow a application linked to Advance Edk library can work with EEG and PM from Emotiv headset.
* If user dont have a license or license expried, they can use Advance library as a Community library.
* You need loggining to Emotiv Cloud to authorize a SDK license with available debit number or check 
  debit information.
* You can get the current license information and set a authorized license activating without logging.


* Information of SDK license:
    * Date_from and Date_to : period of a license.
    * Scopes : type of license: 2 type:
        * EEG: Enable EEG data  -  ADVANCED
        * EEG - PM : Enable EEG data and Performance Metric -  PRIME
    * Seat number : number of seat
    * Soft_limit_date and Hard_limit_date: 
        * A 7-day grace period after the license reach to the end of monthly license subscription cycle. 
        * You need to connect to Emotiv Cloud to re-authorize the license.
        * If you do not authorize before hard_limit_date you will no longer be able to use the library 
          for EEG stream or PM(full rate).
        
* Debit information:
    * Remaining Sessions: number of remaining session of the license in the Emotiv Cloud.
    * Daily debit limit : the maximum number of debitable sessions per day.
    * Total debit today : the number of debited sessions today.
    * Time to reset     : remaining time to reset daily debit limitation(seconds).

 * 1 session = 30 minutes.
    * For Basic, Advanced license: Performance Metric updated each 10 seconds.
    * For Advanced license: Start counting session at the first call IEE_DataUpdateHandle()
                            Stop counting session at call IEE_EngineDisconnect();
    * For Prime license: allows full rate of Performance Metric.
                            Start counting session at the first call IEE_DataUpdateHandle()
                            or any APIs in IEmoStatePerformanceMetric class.
                            Stop counting session at call IEE_EngineDisconnect();

