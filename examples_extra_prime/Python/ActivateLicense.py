import sys
import os
import platform
import time
import ctypes

from array import *
from ctypes import *

if sys.platform.startswith('win32'):
    import msvcrt
elif sys.platform.startswith('linux'):
    import atexit
    from select import select

from ctypes import *

try:
    if sys.platform.startswith('win32'):
        libEDK = cdll.LoadLibrary("../../bin/win32/edk.dll")
    elif sys.platform.startswith('linux'):
        srcDir = os.getcwd()
        if platform.machine().startswith('arm'):
            libPath = srcDir + "/../../bin/armhf/libedk.so"
        else:
            libPath = srcDir + "/../../bin/linux64/libedk.so"
            libEDK = CDLL(libPath)
    else:
        raise Exception('System not supported.')
except Exception as e:
    print 'Error: cannot load EDK lib:', e
    exit()

EDK_OK                     = int("0x0000", 16)
EDK_UNKNOWN_ERROR          = int("0x0001", 16)

EDK_LICENSE_ERROR          = int("0x2010", 16)
EDK_LICENSE_EXPIRED        = int("0x2011", 16)
EDK_LICENSE_NOT_FOUND      = int("0x2012", 16)

EDK_OVER_QUOTA             = int("0x2013", 16)
EDK_INVALID_DEBIT_ERROR    = int("0x2014", 16)
EDK_OVER_DEVICE_LIST       = int("0x2015", 16)

EDK_LICENSE_DEVICE_LIMITED = int("0x2019", 16)
EDK_LICENSE_REGISTERED     = int("0x2020", 16)
EDK_NO_ACTIVE_LICENSE      = int("0x2021", 16)

EDK_ACCESS_DENIED          = int("0x2031", 16)

userName    = "your_username"
password    = "your_password"
debitNum    = c_int(0)

class DebitInfos_t(Structure):
    _fields_ = [
        ("remainingSessions",      c_int),
        ("daily_debit_limit",      c_int),
        ("total_debit_today",      c_int),
        ("time_reset",     c_int)]

IEE_GetDebitInformation = libEDK.IEE_GetDebitInformation
IEE_GetDebitInformation.restype  = c_int
IEE_GetDebitInformation.argtypes = [c_char_p, POINTER(DebitInfos_t)]


class LicenseInfos_t(Structure):
    _fields_ = [
        ("scopes",         c_int),
        ("date_from",      c_int),
        ("date_to",        c_int),
        ("soft_limit_date",  c_int),
        ("hard_limit_date",   c_int),
        ("seat_count",     c_int),
        ("usedQuota",      c_int),
        ("quota",          c_int)]

IEE_LicenseInformation = libEDK.IEE_LicenseInformation
IEE_LicenseInformation.restype  = c_int
IEE_LicenseInformation.argtypes = [POINTER(LicenseInfos_t)]


def activateLicense_err(error_code):
    if error_code == EDK_LICENSE_NOT_FOUND: 
        print "LICENSE NOT FOUND"
    elif error_code == EDK_LICENSE_ERROR: 
        print "LICENSE ERROR"
    elif error_code == EDK_LICENSE_EXPIRED: 
        print "LICENSE EXPIRED"
    elif error_code == EDK_LICENSE_REGISTERED: 
        print "LICENSE REGISTERED"
    elif error_code == EDK_LICENSE_DEVICE_LIMITED: 
        print "DEVICE LICENSE LIMITED"
    elif error_code == EDK_UNKNOWN_ERROR: 
        print "UNKNOWN ERROR"
    elif error_code == EDK_OK:
        print "License Activated!"
        
def licenseInfos_err(error_code):
    if error_code == EDK_INVALID_DEBIT_ERROR: 
        print "INVALID DEBIT NUMBER"
    elif error_code == EDK_OVER_DEVICE_LIST: 
        print "OVER DEVICE LIST"
    elif error_code == EDK_LICENSE_EXPIRED: 
        print "LICENSE EXPIRED"
    elif error_code == EDK_OVER_QUOTA: 
        print "OVER QUOTA"
    elif error_code == EDK_ACCESS_DENIED: 
        print "ACCESS DENIED"
    elif error_code == EDK_LICENSE_ERROR: 
        print "LICENSE ERROR"
    elif error_code == EDK_NO_ACTIVE_LICENSE: 
        print "NO ACTIVE LICENSE"

def    convertEpochToTime(epoch):
    return time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(epoch))

# -------------------------------------------------------------------------
print "==================================================================="
print "The example to activate a license key"
print "==================================================================="

# -------------------------------------------------------------------------

your_license_key = ""

result = 0

#Authorize
if libEDK.IEE_EngineConnect("Emotiv Systems-5") != 0:
    print "Emotiv Engine start up failed."
    
if libEDK.EC_Connect() != 0:
    print "Cannot connect to Emotiv Cloud"
    exit()

if libEDK.EC_Login(userName, password) != 0:
    print "Your login attempt has failed. The username or password may be incorrect"
    exit()

print "Logged in as %s" % userName

print "Please request number of debit"
debitNum = int(raw_input())

#Activate license
result = libEDK.IEE_AuthorizeLicense(your_license_key, debitNum);

activateLicense_err(result)

if (result != EDK_OK) and (result != EDK_LICENSE_REGISTERED):
    print "Activate License: ", result
    exit()

licenseInfos = LicenseInfos_t(0,0,0,0,0,0,0,0)

#We can call this API any time to check current License information
result = IEE_LicenseInformation(byref(licenseInfos))
licenseInfos_err(result)

print "==================================================================="
print "License information"
if licenseInfos.scopes == 1:
    print 'License type                   = EEG', "\n"
elif licenseInfos.scopes == 2:
    print 'License type                   = PM', "\n"
elif licenseInfos.scopes == 3:
    print 'License type                   = EEG + PM', "\n"
else:
    print 'No License type', "\n"

date_from = convertEpochToTime(licenseInfos.date_from)
print 'From Date                =', date_from

date_to = convertEpochToTime(licenseInfos.date_to)
print 'To Date                  =', date_to , "\n"

soft_limit_date = convertEpochToTime(licenseInfos.soft_limit_date)
print 'Soft Limit Date         =', soft_limit_date , "\n"

hard_limit_date = convertEpochToTime(licenseInfos.hard_limit_date)
print 'Hard Limit Date         =', hard_limit_date , "\n"

print 'Number of Seat          =', licenseInfos.seat_count, "\n"

print 'Total used Quota        =', licenseInfos.usedQuota,"\n"
print 'Total Quota             =', licenseInfos.quota,"\n"


#Debit Info
print "==================================================================="
print "Debit Information"
debitInfos = DebitInfos_t(0,0,0,0)

#We can call this API any time to check current License information
result = IEE_GetDebitInformation(your_license_key, byref(debitInfos))
print 'Remain Session                   =', debitInfos.remainingSessions, "\n"

print 'Daily debit limit                =', debitInfos.daily_debit_limit, "\n"

print 'Total debit a day                =', debitInfos.total_debit_today, "\n"

print 'Remained time(seconds) before resetting debit limitation a day        =', debitInfos.time_reset, "\n"

    
