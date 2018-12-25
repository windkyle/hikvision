package cn.ipanel.apps.dj.hikvision.service;

import cn.ipanel.apps.dj.hikvision.HCNetSDK;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

import java.util.concurrent.BlockingQueue;

public class Callback {
    public class FMSGCallBack_V31 implements HCNetSDK.FMSGCallBack_V31
    {
        private BlockingQueue<AlarmBean> alarmQueue;

        public FMSGCallBack_V31(BlockingQueue<AlarmBean> alarmQueue) {
            this.alarmQueue = alarmQueue;
        }

        public boolean invoke(NativeLong lCommand, HCNetSDK.NET_DVR_ALARMER pAlarmer, Pointer pAlarmInfo, int dwBufLen, Pointer pUser)
        {
            MyService.AlarmDataHandle(lCommand, pAlarmer, pAlarmInfo, dwBufLen, pUser, alarmQueue);
            return true;
        }

    }
    public class FMSGCallBack implements HCNetSDK.FMSGCallBack
    {
        private BlockingQueue<AlarmBean> alarmQueue;

        public void invoke(NativeLong lCommand, HCNetSDK.NET_DVR_ALARMER pAlarmer, Pointer pAlarmInfo, int dwBufLen, Pointer pUser)
        {
            MyService.AlarmDataHandle(lCommand, pAlarmer, pAlarmInfo, dwBufLen, pUser, alarmQueue);
        }
    }
}
