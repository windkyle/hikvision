package cn.ipanel.apps.dj.hikvision.service;

import cn.ipanel.apps.dj.hikvision.HCNetSDK;
import cn.ipanel.apps.dj.hikvision.config.HikvisionClientConfig;
import cn.ipanel.apps.dj.hikvision.config.SystemConfig;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

@Service
public class MyService {

    private static Logger logger = LoggerFactory.getLogger(MyService.class);

    HCNetSDK hCNetSDK;
    HCNetSDK.NET_DVR_DEVICEINFO_V30 m_strDeviceInfo;//设备信息

    int iPort = 8000;

    List<NativeLong> users = new ArrayList<>();
    //用户句柄
    NativeLong lUserID;
    //报警布防句柄
    NativeLong lAlarmHandle;
    List<NativeLong> lAlarmHandleList = new ArrayList<>();
    //报警监听句柄
    NativeLong lListenHandle;

    HCNetSDK.FMSGCallBack fMSFCallBack;//报警回调函数实现
    HCNetSDK.FMSGCallBack_V31 fMSFCallBack_V31;//报警回调函数实现

    private BlockingQueue<AlarmBean> alarmQueue;
    private static SystemConfig config;
    private static List<HikvisionClientConfig> hikvisionConfigs;
    private static Map<String,String> IUserIDToDeviceMap = new HashMap<>();

    @Autowired
    public MyService(BlockingQueue<AlarmBean> alarmQueue, SystemConfig config) {
        this.alarmQueue = alarmQueue;
        MyService.config = config;
    }

    static {
        hikvisionConfigs=HikvisionClientConfig.getClientConfig();
    }


    /**
     * 监听多个指纹打卡机
     * @throws Exception
     */
    public void initDevice() throws Exception {
        try {
            if (config.getRuntime() == 32) {
                hCNetSDK = (HCNetSDK) Native.loadLibrary( "HCNetSDK",HCNetSDK.class);
            } else if (config.getRuntime() == 64) {
                hCNetSDK = (HCNetSDK) Native.loadLibrary( "hcnetsdk",HCNetSDK.class);
            } else {
                throw new Exception("invalid runtime");
            }

            // 初始化
            boolean initSuc = hCNetSDK.NET_DVR_Init();
            if (!initSuc) {
                throw new Exception("init failed - " + hCNetSDK.NET_DVR_GetLastError());
            }
            if (config.getWriteLog().equals("true")) {
                hCNetSDK.NET_DVR_SetLogToFile(true, "/var/tmp/" + config.getDevice(), false);
            }

            // 设置连接超时时间与重连功能
            hCNetSDK.NET_DVR_SetConnectTime(2000, 1);
            hCNetSDK.NET_DVR_SetReconnect(1000, true);

            m_strDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V30();
            //注册所有设备
            for(HikvisionClientConfig config:hikvisionConfigs){
                lUserID = hCNetSDK.NET_DVR_Login_V30(config.getHikvision_ip(),
                        (short) iPort, config.getHikvision_user_name(), config.getHikvision_password(), m_strDeviceInfo);
                ////
                int userID = lUserID.intValue();
                logger.info("userId:"+userID);
                if (userID < 0) {//if (userID == -1) {
                    logger.info(config.getHikvision_ip()+"**login failed");
                    continue;
                    //hCNetSDK.NET_DVR_Cleanup();
                    //throw new Exception("login failed - " + hCNetSDK.NET_DVR_GetLastError());
                }
                users.add(lUserID);
                IUserIDToDeviceMap.put(lUserID.toString(),config.getDevice());
                logger.info("login success: ip:"+config.getDevice()+"***ip"+config.getHikvision_ip()+"***userId :"+lUserID.toString());
            }
            if(null == users || users.size() == 0){
                hCNetSDK.NET_DVR_Cleanup();
                throw new Exception("all hikvision client login failed - " + hCNetSDK.NET_DVR_GetLastError());
            }
            fMSFCallBack_V31 = new Callback().new FMSGCallBack_V31(alarmQueue);
            Pointer pUser = null;
            if (!hCNetSDK.NET_DVR_SetDVRMessageCallBack_V31(fMSFCallBack_V31, pUser)) {
                throw new Exception("set fallback failed - " + hCNetSDK.NET_DVR_GetLastError());
            }

            HCNetSDK.NET_DVR_SETUPALARM_PARAM m_strAlarmInfo = new HCNetSDK.NET_DVR_SETUPALARM_PARAM();
            m_strAlarmInfo.dwSize=m_strAlarmInfo.size();
            m_strAlarmInfo.byLevel=1;
            m_strAlarmInfo.byAlarmInfoType=1;
            m_strAlarmInfo.write();
            for(NativeLong userId:users){
                logger.info("{}", userId);
                logger.info("{}", m_strAlarmInfo);
                lAlarmHandle = hCNetSDK.NET_DVR_SetupAlarmChan_V41(userId, m_strAlarmInfo);
                if (lAlarmHandle.intValue() == -1) {
                    throw new Exception("set alarm chan failed - " + hCNetSDK.NET_DVR_GetLastError());
                }if(null != lAlarmHandle){
                    lAlarmHandleList.add(lAlarmHandle);
                }
            }
        } catch (Exception e) {
            logger.error("init error: {}", e.getMessage(), e);
            logOut();
            throw e;
        }
    }

   /* public void initDeviceOld() throws Exception {
        try {
            if (config.getRuntime() == 32) {
                hCNetSDK = (HCNetSDK) Native.loadLibrary( "HCNetSDK",HCNetSDK.class);
            } else if (config.getRuntime() == 64) {
                hCNetSDK = (HCNetSDK) Native.loadLibrary( "hcnetsdk",HCNetSDK.class);
            } else {
                throw new Exception("invalid runtime");
            }

            // 初始化
            boolean initSuc = hCNetSDK.NET_DVR_Init();
            if (!initSuc) {
                throw new Exception("init failed - " + hCNetSDK.NET_DVR_GetLastError());
            }
            if (config.getWriteLog().equals("true")) {
                hCNetSDK.NET_DVR_SetLogToFile(true, "/var/tmp/" + config.getDevice(), false);
            }

            // 设置连接超时时间与重连功能
            hCNetSDK.NET_DVR_SetConnectTime(2000, 1);
            hCNetSDK.NET_DVR_SetReconnect(1000, true);

            m_strDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V30();
            lUserID = hCNetSDK.NET_DVR_Login_V30(config.getM_sDeviceIP(),
                    (short) iPort, config.getM_sUserName(), config.getM_sPassword(), m_strDeviceInfo);
            long userID = lUserID.longValue();
            if (userID == -1) {
                hCNetSDK.NET_DVR_Cleanup();
                throw new Exception("login failed - " + hCNetSDK.NET_DVR_GetLastError());
            }

            fMSFCallBack_V31 = new Callback().new FMSGCallBack_V31(alarmQueue);
            Pointer pUser = null;
            if (!hCNetSDK.NET_DVR_SetDVRMessageCallBack_V31(fMSFCallBack_V31, pUser)) {
                throw new Exception("set fallback failed - " + hCNetSDK.NET_DVR_GetLastError());
            }

            HCNetSDK.NET_DVR_SETUPALARM_PARAM m_strAlarmInfo = new HCNetSDK.NET_DVR_SETUPALARM_PARAM();
            m_strAlarmInfo.dwSize=m_strAlarmInfo.size();
            m_strAlarmInfo.byLevel=1;
            m_strAlarmInfo.byAlarmInfoType=1;
            m_strAlarmInfo.write();
            logger.info("{}", lUserID);
            logger.info("{}", m_strAlarmInfo);
            lAlarmHandle = hCNetSDK.NET_DVR_SetupAlarmChan_V41(lUserID, m_strAlarmInfo);
            if (lAlarmHandle.intValue() == -1) {
                throw new Exception("set alarm chan failed - " + hCNetSDK.NET_DVR_GetLastError());
            }
        } catch (Exception e) {
            logger.error("init error: {}", e.getMessage(), e);
            logOut();
            throw e;
        }
    }*/

    private void logOut() throws Exception {
        for(NativeLong userId:users){
            if (userId.longValue() > -1) {
                if (!hCNetSDK.NET_DVR_Logout(userId)) {
                    throw new Exception("logout failed - " + hCNetSDK.NET_DVR_GetLastError());
                }
            }
        }
        users.clear();
        lUserID = new NativeLong(-1);
        hCNetSDK.NET_DVR_Cleanup();
    }

    public void initAgain()throws Exception {
        for(NativeLong lAlarmHandle1:lAlarmHandleList){
            if (lAlarmHandle1.intValue() > -1) {
                if ( hCNetSDK.NET_DVR_CloseAlarmChan_V30(lAlarmHandle1)) {
                } else {
                    throw new Exception("close alarm chan failed - " + hCNetSDK.NET_DVR_GetLastError());
                }
            }
        }
        lAlarmHandle = new NativeLong(-1);
        lAlarmHandleList.clear();
        logOut();
        Thread.sleep(2 * 1000);
        initDevice();
    }

    public static void AlarmDataHandle(NativeLong lCommand, HCNetSDK.NET_DVR_ALARMER pAlarmer, Pointer pAlarmInfo, int dwBufLen, Pointer pUser, BlockingQueue<AlarmBean> alarmQueue)
    {
        //lCommand是传的报警类型
        switch (lCommand.intValue()) {
            case HCNetSDK.COMM_ALARM_ACS: //门禁主机报警信息
                HCNetSDK.NET_DVR_ACS_ALARM_INFO strACSInfo = new HCNetSDK.NET_DVR_ACS_ALARM_INFO();
                strACSInfo.write();
                Pointer pACSInfo = strACSInfo.getPointer();
                pACSInfo.write(0, pAlarmInfo.getByteArray(0, strACSInfo.size()), 0, strACSInfo.size());
                strACSInfo.read();
                if (strACSInfo.dwMinor == 38) {
                    String Iuser = pAlarmer.lUserID.toString();
                    String ip = strACSInfo.struRemoteHostAddr.toString();
                    logger.info("alarmDataHandle ip:"+ip);
                    AlarmBean bean = new AlarmBean(new String(strACSInfo.struAcsEventInfo.byCardNo).trim(), strACSInfo.struTime,IUserIDToDeviceMap.get(Iuser));
                    try {
                        alarmQueue.put(bean);
                    } catch (InterruptedException e) {
                        logger.error("put queue error:{}", bean.toString(), e);
                    }
                }
                break;
        }
    }
}
