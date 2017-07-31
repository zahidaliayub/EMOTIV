%this example show you how to active your license key and get information
%
%of the license.
%edit by Nguyen Vinh Binh; binh@emoviv.com 
% example done with matlab 32 bit
%update 2017.07.21: use functions sdk 3.5 to active (need debit number , login to cloud incomparison with 3.4 )
%--------------------------------------------------------------------------
clc;
warning ('off','all');
[nf, w] = loadlibrary('../../bin/win32/edk','../../include/Iedk.h','addheader','EmotivLicense.h','addheader','EmotivCloudClient.h','alias','libs'); 
%[nf, w] = loadlibrary('../../bin/win32/edk','../../include/EmotivCloudClient.h','alias','Cloud'); 
loadlibrary('../../bin/win32/edk','../../include/EmotivCloudClient.h','alias','Cloud'); 

EDK_OK=0;
 userName = 'yourusername'
 password = 'yourpassword';
 result = -1;
 default = int8(['Emotiv Systems-5' 0]);
 AllOK = calllib('libs','IEE_EngineConnect', default); % success means this value is 0;2

 result= calllib('Cloud','EC_Connect');
    if (result~= EDK_OK)    
        disp('Cannot connect to Emotiv Cloud');
    end
   result = calllib('Cloud','EC_Login',userName,password);
   if (result~= EDK_OK)
    
        disp('Login failed. The username or password may be incorrect');
        quit;
   else
     disp('Login OK');  
   end
   
% SDK License
LICENSE=''; 
%define license type according to IEE_LicenseType (EmotivLicense.h)
IEE_EEG=1;
IEE_PM=2;
IEE_EEG_PM=3;
%Enter value of debit you want to get
debit_num=0;

%get Debit info
    
    disp('INFORMATION BEFORE GETTING DEBIT');
    disp(' ');
 IEE_DebitInfos= struct('remainingSessions',0,'daily_debit_limit',0,'total_debit_today',0,'time_reset',0);
 sp = libpointer('IEE_DebitInfos_struct',IEE_DebitInfos);
 [xobj,xval]= calllib('libs','IEE_GetDebitInformation',LICENSE,sp);
 %print result
     X=['Remaining Sessions       ',num2str(sp.Value.remainingSessions)];
     disp(X);
     X=['Daily debit limitation   ' ,num2str(sp.Value.daily_debit_limit)];
     disp(X);
     X=['Total debit today        ' ,num2str(sp.Value.total_debit_today)];
     disp(X);
    X=['Remaining time(second)  before resetting debit daily limitation:    ' ,num2str(sp.Value.time_reset)];
     disp(X);
     
    disp(' ');
     
     prompt = 'Please enter number of debit you want to get: ';
     debit_num =input(prompt);
     
     
    
     
%% The license error. 
EDK_LICENSE_ERROR                =hex2dec('2010');

%The license expried
EDK_LICENSE_EXPIRED               =hex2dec('2011');

% The license was not found
EDK_LICENSE_NOT_FOUND             =hex2dec('2012');

%% The license is over quota
EDK_OVER_QUOTA                    =hex2dec('2013');

%% Debit number is invalid
EDK_INVALID_DEBIT_ERROR           =hex2dec('2014');

%% Device list of the license is over
EDK_OVER_DEVICE_LIST              =hex2dec('2015');

EDK_APP_QUOTA_EXCEEDED             =hex2dec('2016');

EDK_APP_INVALID_DATE               =hex2dec('2017');

%% Application register device number is exceeded. 
EDK_LICENSE_DEVICE_LIMITED         =hex2dec('2019');

%% The license registered with the device. 
EDK_LICENSE_REGISTERED              =hex2dec('2020');

%% No license is activated
EDK_NO_ACTIVE_LICENSE              =hex2dec('2021');

% The license is updated
 EDK_UPDATE_LICENSE                 =hex2dec('2023');

% Session debit number is more then max of remaining session number
 EDK_INVALID_DEBIT_NUMBER           = hex2dec('2024');

% Session debit is limited today
 EDK_DAILY_DEBIT_LIMITED            = hex2dec('2025');
 %! One of the parameters supplied to the function is invalid
EDK_INVALID_PARAMETER               =hex2dec('0302');    

EDK_NO_INTERNET_CONNECTION          =hex2dec('2100');

EDK_ACCESS_DENIED                 = hex2dec('2031');


EDK_UNKNOWN_ERROR         = hex2dec('0001');
     
result = calllib('libs','IEE_AuthorizeLicense',LICENSE,debit_num) ;  

switch (result)
    
    case EDK_INVALID_DEBIT_NUMBER
        disp('Invalid Debit number');
    case EDK_INVALID_DEBIT_ERROR
        disp('Invalid number of Debit');
      
    case EDK_INVALID_PARAMETER
        disp('Invalid user info') ;
      
    case EDK_NO_INTERNET_CONNECTION
        disp('Internet Connection');
      
    case EDK_LICENSE_EXPIRED
        disp('License expired') ;
      
    case EDK_OVER_DEVICE_LIST
        disp('Over device list') ;
        
    case EDK_DAILY_DEBIT_LIMITED
        disp('Over daily debit number') ;
      
    case EDK_ACCESS_DENIED
        disp('Access denied') ;
      
    case EDK_LICENSE_REGISTERED
        disp('The License has registered') ;
      
    case EDK_LICENSE_ERROR
        disp('Error License') ;
      
    case EDK_LICENSE_NOT_FOUND
        disp('License not found') ;
      
    case EDK_UNKNOWN_ERROR
        disp('unknown error') ;
      
    otherwise
      
end;
    disp('************************************');
if((result==EDK_OK)||(result ==EDK_LICENSE_REGISTERED))    
   
   IEE_LicenseInfos= struct('scopes',0,'date_from',0,'date_to',0,'soft_limit_date',0,'hard_limit_date',0,'seat_count',0,'usedQuota',0,'quota',0);
    sp = libpointer('IEE_LicenseInfos_struct',IEE_LicenseInfos);
    [xobj,xval]= calllib('libs','IEE_LicenseInformation',sp);
   
     licensetype = '';
     if (sp.Value.scopes==IEE_EEG)
         licensetype='EEG';
     elseif (sp.Value.scopes==IEE_PM)
         licensetype='PM';
     elseif (sp.Value.scopes==IEE_EEG_PM)
         licensetype='EEG+PM';
     end
     X=['License type ',licensetype];
     disp(X);
     %convert time
     utc = sp.Value.date_from;
              
     X=['From date         : ',datestr(datenum([1970, 1, 1, 0, 0, utc])),' GMT'];
     disp(X);
     utc = sp.Value.date_to;
     X=['To date           : ',datestr(datenum([1970, 1, 1, 0, 0, utc])),' GMT'];
     disp(X);
     
     utc = sp.Value.soft_limit_date;     
     X=['Soft Limit Date   : ', datestr(datenum([1970, 1, 1, 0, 0, utc])),' GMT'];
     disp(X);
     
     
     utc = sp.Value.hard_limit_date;     
     X=['Hard Limit Date   : ', datestr(datenum([1970, 1, 1, 0, 0, utc])),' GMT'];
     disp(X);
      
     
     X=['Number of seat    : ',num2str(sp.Value.seat_count)];
     disp(X);
         
     X=['Used Quota        : ' ,num2str(sp.Value.usedQuota)];
     disp(X);
     X=['Total Quotas      : ' ,num2str(sp.Value.quota)];
     disp(X);
end
