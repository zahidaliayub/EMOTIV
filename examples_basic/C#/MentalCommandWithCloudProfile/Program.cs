/****************************************************************************
**
** Copyright 2016 by Emotiv. All rights reserved
** Example - Mental command with  cloud profile
** This example demonstrates how to detect mental command detection with 
** a profile.
** This example work on single connection
****************************************************************************/

using System;
using System.Collections.Generic;
using Emotiv;
using System.IO;
using System.Threading;

namespace MentalCommandWithCloudProfile
{
    class Program
    {
        static System.IO.StreamWriter cogLog = new System.IO.StreamWriter("MentalCommand.log");

        static int userCloudID = 0;
        static int userID = -1;
        static string userName = "Your account name";
        static string password = "Your password";        
        static string profileName = "Profile_1";
        static int version = -1; // Lastest version

        static Boolean enableLoger = false;

        static void engine_EmoEngineConnected(object sender, EmoEngineEventArgs e)
        {
            Console.WriteLine("Emoengine connected");
        }

        static void engine_EmoEngineDisconnected(object sender, EmoEngineEventArgs e)
        {
            Console.WriteLine("Emoengine disconnected");
        }
        static void engine_UserAdded(object sender, EmoEngineEventArgs e)
        {
            Console.WriteLine("user added ({0})", e.userId);
            userID = (int)e.userId;
        }
        static void engine_UserRemoved(object sender, EmoEngineEventArgs e)
        {
            Console.WriteLine("user removed");
        }

        static void engine_EmoStateUpdated(object sender, EmoStateUpdatedEventArgs e)
        {
            EmoState es = e.emoState;

            Single timeFromStart = es.GetTimeFromStart();
        }

        static void engine_EmoEngineEmoStateUpdated(object sender, EmoStateUpdatedEventArgs e)
        {
            EmoState es = e.emoState;

            Single timeFromStart = es.GetTimeFromStart();

            Int32 headsetOn = es.GetHeadsetOn();

            EdkDll.IEE_SignalStrength_t signalStrength = es.GetWirelessSignalStatus();
            Int32 chargeLevel = 0;
            Int32 maxChargeLevel = 0;
            es.GetBatteryChargeLevel(out chargeLevel, out maxChargeLevel);            
        }

        static void engine_MentalCommandEmoStateUpdated(object sender, EmoStateUpdatedEventArgs e)
        {
            EmoState es = e.emoState;

            Single timeFromStart = es.GetTimeFromStart();

            EdkDll.IEE_MentalCommandAction_t cogAction = es.MentalCommandGetCurrentAction();
            Single power = es.MentalCommandGetCurrentActionPower();
            Boolean isActive = es.MentalCommandIsActive();

            cogLog.WriteLine( "{0},{1},{2},{3}", timeFromStart, cogAction, power, isActive);
            cogLog.Flush();
            if(enableLoger)
                 Console.WriteLine("{0},{1},{2},{3}", timeFromStart, cogAction, power, isActive);
        }

        static void engine_MentalCommandTrainingStarted(object sender, EmoEngineEventArgs e)
        {
            Console.WriteLine("Start MentalCommand Training");
        }

        static void engine_MentalCommandTrainingSucceeded(object sender, EmoEngineEventArgs e)
        {
            Console.WriteLine("MentalCommand Training Success. (A)ccept/Reject?");
            ConsoleKeyInfo cki = Console.ReadKey(true);
            if (cki.Key == ConsoleKey.A)
            {
                Console.WriteLine("Accept!!!");
                EmoEngine.Instance.MentalCommandSetTrainingControl((uint)userID, EdkDll.IEE_MentalCommandTrainingControl_t.MC_ACCEPT);
            }
            else
            {
                EmoEngine.Instance.MentalCommandSetTrainingControl((uint)userID, EdkDll.IEE_MentalCommandTrainingControl_t.MC_REJECT);
            }
        }

        static void engine_MentalCommandTrainingCompleted(object sender, EmoEngineEventArgs e)
        {
            Console.WriteLine("MentalCommand Training Completed");
            //Console.WriteLine("");
        }

        static void engine_MentalCommandTrainingRejected(object sender, EmoEngineEventArgs e)
        {
            Console.WriteLine("MentalCommand Training Rejected");
            //Console.WriteLine("");
        }

        static void SavingLoadingFunction(int mode)
        {
            int getNumberProfile = EmotivCloudClient.EC_GetAllProfileName(userCloudID);

            if (mode == 0)
            {
                int profileID = -1;
                EmotivCloudClient.EC_GetProfileId(userCloudID, profileName, ref profileID);

                if (profileID >= 0)
                {
                    Console.WriteLine("Profile with " + profileName + " is existed");
                    Console.WriteLine("Updating....");
                    if (EmotivCloudClient.EC_UpdateUserProfile(userCloudID, (int)userID, profileID) == EdkDll.EDK_OK)
                    {
                        Console.WriteLine("Updating finished");
                    }
                    else Console.WriteLine("Updating failed");
                }
                else
                {
                    Console.WriteLine("Saving...");

                    if (EmotivCloudClient.EC_SaveUserProfile(userCloudID, (int)userID, profileName,
                    EmotivCloudClient.profileFileType.TRAINING) == EdkDll.EDK_OK)
                    {
                        Console.WriteLine("Saving finished");
                    }
                    else Console.WriteLine("Saving failed");
                }

                return;
            }
            if (mode == 1)
            {                
                if (getNumberProfile > 0)
                {
                    Console.WriteLine("Loading...");

                    int profileID = -1;
                    EmotivCloudClient.EC_GetProfileId(userCloudID, profileName, ref profileID);

                    if (EmotivCloudClient.EC_LoadUserProfile(userCloudID, (int)userID, profileID, version) == EdkDll.EDK_OK)
                        Console.WriteLine("Loading finished");
                    else
                        Console.WriteLine("Loading failed");

                }

                return;
            }
        }

        static void keyHandler(ConsoleKey key)
        {
            switch (key)
            {
                case ConsoleKey.F1:
                    {
                        ulong action1 = (ulong)EdkDll.IEE_MentalCommandAction_t.MC_LEFT;
                        ulong action2 = (ulong)EdkDll.IEE_MentalCommandAction_t.MC_RIGHT;
                        ulong listAction = action1 | action2;
                        EmoEngine.Instance.MentalCommandSetActiveActions((uint)userID, listAction);
                        Console.WriteLine("Setting MentalCommand active actions for user");
                        break;
                    }
                case ConsoleKey.F2:
                    EmoEngine.Instance.MentalCommandSetTrainingAction((uint)userID, EdkDll.IEE_MentalCommandAction_t.MC_NEUTRAL);
                    EmoEngine.Instance.MentalCommandSetTrainingControl((uint)userID, EdkDll.IEE_MentalCommandTrainingControl_t.MC_START);
                    break;
                case ConsoleKey.F3:
                    EmoEngine.Instance.MentalCommandSetTrainingAction((uint)userID, EdkDll.IEE_MentalCommandAction_t.MC_RIGHT);
                    EmoEngine.Instance.MentalCommandSetTrainingControl((uint)userID, EdkDll.IEE_MentalCommandTrainingControl_t.MC_START);
                    break;
                case ConsoleKey.F4:
                    EmoEngine.Instance.MentalCommandSetTrainingAction((uint)userID, EdkDll.IEE_MentalCommandAction_t.MC_LEFT);
                    EmoEngine.Instance.MentalCommandSetTrainingControl((uint)userID, EdkDll.IEE_MentalCommandTrainingControl_t.MC_START);
                    break;
                case ConsoleKey.F5:
                    EmoEngine.Instance.MentalCommandSetActivationLevel((uint)userID, 2);
                    Console.WriteLine("MentalCommand Activation level set to {0}", EmoEngine.Instance.MentalCommandGetActivationLevel((uint)userID));
                    break;
                case ConsoleKey.F6:
                    Console.WriteLine("MentalCommand Activation level is {0}", EmoEngine.Instance.MentalCommandGetActivationLevel((uint)userID));
                    break;
                case ConsoleKey.F7:
                    Console.WriteLine("Get the current overall skill rating: {0}", EmoEngine.Instance.MentalCommandGetOverallSkillRating((uint)userID));
                    break;
                case ConsoleKey.F8:
                    SavingLoadingFunction(0);
                    break;
                case ConsoleKey.F9:
                    SavingLoadingFunction(1);
                    break;
                case ConsoleKey.F10:
                    enableLoger = !enableLoger;
                    break;
                default:
                    break;
            }
        }

        static void Main(string[] args)
        {
            EmoEngine engine = EmoEngine.Instance;

            engine.EmoEngineConnected +=
                new EmoEngine.EmoEngineConnectedEventHandler(engine_EmoEngineConnected);
            engine.EmoEngineDisconnected +=
                new EmoEngine.EmoEngineDisconnectedEventHandler(engine_EmoEngineDisconnected);

            engine.UserAdded +=
                new EmoEngine.UserAddedEventHandler(engine_UserAdded);
            engine.UserRemoved +=
                new EmoEngine.UserRemovedEventHandler(engine_UserRemoved);

            engine.EmoStateUpdated +=
                new EmoEngine.EmoStateUpdatedEventHandler(engine_EmoStateUpdated);                       
            engine.EmoEngineEmoStateUpdated +=
                new EmoEngine.EmoEngineEmoStateUpdatedEventHandler(engine_EmoEngineEmoStateUpdated);

            engine.MentalCommandEmoStateUpdated +=
                new EmoEngine.MentalCommandEmoStateUpdatedEventHandler(engine_MentalCommandEmoStateUpdated);            
            engine.MentalCommandTrainingStarted +=
                new EmoEngine.MentalCommandTrainingStartedEventEventHandler(engine_MentalCommandTrainingStarted);
            engine.MentalCommandTrainingSucceeded +=
                new EmoEngine.MentalCommandTrainingSucceededEventHandler(engine_MentalCommandTrainingSucceeded);
            engine.MentalCommandTrainingCompleted +=
                new EmoEngine.MentalCommandTrainingCompletedEventHandler(engine_MentalCommandTrainingCompleted);
            engine.MentalCommandTrainingRejected +=
                new EmoEngine.MentalCommandTrainingRejectedEventHandler(engine_MentalCommandTrainingRejected);            

            engine.Connect();

            Console.WriteLine("===========================================================================");
            Console.WriteLine("Example to show how to detect mental command detection with a profile.");
            Console.WriteLine("Please use hotkeys for training.");
            Console.WriteLine("F1: MetalCommand: Set Active Action - LEFT and RIGHT");
            Console.WriteLine("F2: MetalCommand: Training Action   - NEUTRAL");
            Console.WriteLine("F3: MetalCommand: Training Action   - RIGHT");
            Console.WriteLine("F4: MetalCommand: Training Action   - LEFT");
            Console.WriteLine("F5: MetalCommand: Set Active Level  - 2");
            Console.WriteLine("F6: MetalCommand: Get Active Level");
            Console.WriteLine("F7: MetalCommand: Get OverSkills Rating");
            Console.WriteLine("F8: Update profile");
            Console.WriteLine("F9: Load profile");
            Console.WriteLine("F10:Enable Logger");
            Console.WriteLine("===========================================================================");

            ConsoleKeyInfo cki = new ConsoleKeyInfo();
                       
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

            while (true)
            {
                try
                {
                    if (Console.KeyAvailable)
                    {
                        cki = Console.ReadKey(true);
                        keyHandler(cki.Key);

                        if (cki.Key == ConsoleKey.X)
                        {
                            break;
                        }
                    }
                    engine.ProcessEvents(5);
                }
                catch (EmoEngineException e)
                {
                    Console.WriteLine("{0}", e.ToString());
                }
                catch (Exception e)
                {
                    Console.WriteLine("{0}", e.ToString());
                }
            }
            engine.Disconnect();
        }
    }
}
