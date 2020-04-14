package com.konka.kksdtr069.handler;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public static final String TAG = DBHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "tr069.db";
    private static final int DATABASE_VERSION = 1; // 数据库版本，数据库需要更新时版本号加1

    private static final String contents[][] = {
            {"Device.DeviceSummary", "This product belongs to Shenzhen Konka information network company", "string(1024)", "0", "0", "0"},
            {"Device.DeviceType", "STB", "string(64)", "0", "0", "0"},
            {"Device.DeviceInfo.Manufacturer", "MIGU", "string(64)", "0", "0", "0"},
            {"Device.DeviceInfo.OUI", "000003", "string(6)", "0", "0", "0"},
            {"Device.DeviceInfo.ManufacturerOUI", "000003", "string(6)", "0", "0", "0"},
            {"Device.DeviceInfo.ModelName", "MG101-K_7", "string(64)", "0", "0", "0"},
            {"Device.DeviceInfo.ModelID", "", "string(64)", "0", "0", "0"},
            {"Device.DeviceInfo.Description", "p201_iptv-user 4.4.2 KOT49H 1.1.12 test-keys", "string(256)", "0", "0", "0"},
            {"Device.DeviceInfo.ProductClass", "", "string(64)", "0", "0", "0"},
            {"Device.DeviceInfo.SerialNumber", "", "string(64)", "0", "0", "0"},
            {"Device.DeviceInfo.HardwareVersion", "1.0", "string(64)", "0", "0", "0"},
            {"Device.DeviceInfo.SoftwareVersion", "", "string(64)", "0", "0", "2"},
            {"Device.DeviceInfo.EnabledOptions", "", "string(1024)", "0", "0", "0"},
            {"Device.DeviceInfo.AdditionalHardwareVersion", "1.0", "string(64)", "0", "0", "0"},
            {"Device.DeviceInfo.AdditionalSoftwareVersion", "Android release version 4.4.2", "string(64)", "0", "0", "0"},
            {"Device.DeviceInfo.ProvisioningCode", "", "string(64)", "1", "0", "2"},
            {"Device.DeviceInfo.DeviceStatus", "", "string(64)", "0", "0", "2"},
            {"Device.DeviceInfo.UpTime", "", "unsignedInt", "0", "0", "0"},
            {"Device.DeviceInfo.FirstUseDate", "", "DateTime", "0", "0", "0"},
            {"Device.DeviceInfo.DeviceLog", "", "string(32K)", "0", "0", "0"},

            {"Device.DeviceInfo.MemoryStatus.Total", "", "unsignedInt", "0", "0", "0"},
            {"Device.DeviceInfo.MemoryStatus.Free", "", "unsignedInt", "0", "0", "0"},

            {"Device.DeviceInfo.ProcessStatus.CPUUsage", "", "unsignedInt[:100]", "0", "0", "0"},

            {"Device.ManagementServer.URL", "http://101.96.131.47:10086/ACS-server/acs", "string(256)", "1", "0", "2"},
            {"Device.ManagementServer.URLBackup", "", "string(256)", "1", "0", "2"},
            {"Device.ManagementServer.URLModifyFlag", "", "int", "1", "0", "0"},
            {"Device.ManagementServer.Username", "cpe", "string(256)", "1", "0", "0"},
            {"Device.ManagementServer.Password", "cpe", "string(256)", "1", "0", "0"},
            {"Device.ManagementServer.PeriodicInformEnable", "", "boolean", "1", "0", "0"},
            {"Device.ManagementServer.PeriodicInformInterval", "", "unsignedInt[1:]", "1", "0", "0"},
            {"Device.ManagementServer.PeriodicInformTime", "", "DateTime", "1", "0", "0"},
            {"Device.ManagementServer.ParameterKey", "", "string(32)", "0", "0", "0"},
            {"Device.ManagementServer.ConnectionRequestURL", "", "string(256)", "0", "0", "2"},
            {"Device.ManagementServer.ConnectionRequestUsername", "", "string(256)", "1", "0", "2"},
            {"Device.ManagementServer.ConnectionRequestPassword", "", "string(256)", "1", "0", "2"},
            {"Device.ManagementServer.UpgradesManaged", "", "boolean", "1", "0", "0"},
            {"Device.ManagementServer.KickURL", "", "string(256)", "0", "0", "0"},
            {"Device.ManagementServer.DownloadProgressURL", "", "string(256)", "0", "0", "0"},
            {"Device.ManagementServer.UDPConnectionRequestURL", "", "string(256)", "0", "0", "2"},
            {"Device.ManagementServer.UDPConnectionRequestAddress", "", "string(256)", "0", "0", "2"},
            {"Device.ManagementServer.UDPConnectionRequestAddressNotificationLimit", "", "unsignedInt", "1", "0", "0"},
            {"Device.ManagementServer.STUNEnable", "false", "boolean", "1", "0", "0"},
            {"Device.ManagementServer.STUNServerAddress", "", "string(256)", "1", "0", "0"},
            {"Device.ManagementServer.STUNServerPort", "", "unsignedInt[0:65535]", "1", "0", "0"},
            {"Device.ManagementServer.STUNUsername", "", "string(256)", "1", "0", "0"},
            {"Device.ManagementServer.STUNPassword", "", "string(256)", "1", "0", "0"},
            {"Device.ManagementServer.STUNMaximumKeepAlivePeriod", "", "int[-1:]", "1", "0", "0"},
            {"Device.ManagementServer.STUNMinimumKeepAlivePeriod", "", "unsignedInt", "1", "0", "0"},
            {"Device.ManagementServer.NATDetected", "false", "boolean", "0", "0", "2"},

            {"Device.GatewayInfo.ManufacturerOUI", "", "string(6)", "0", "0", "0"},
            {"Device.GatewayInfo.ProductClass", "", "string(32)", "0", "0", "0"},
            {"Device.GatewayInfo.SerialNumber", "", "string(64)", "0", "0", "0"},

            {"Device.Config.PersistentData", "", "string(256)", "1", "0", "0"},
            {"Device.Config.ConfigFile", "", "string(32K)", "1", "0", "0"},

            {"Device.Time.NTPServer", "", "string(64)", "1", "0", "0"},
            {"Device.Time.NTPServer1", "", "string(64)", "1", "0", "0"},
            {"Device.Time.NTPServer2", "", "string(64)", "1", "0", "0"},
            {"Device.Time.NTPServer3", "", "string(64)", "1", "0", "0"},
            {"Device.Time.NTPServer4", "", "string(64)", "1", "0", "0"},
            {"Device.Time.NTPServer5", "", "string(64)", "1", "0", "0"},
            {"Device.Time.CurrentLocalTime", "", "DateTime", "0", "0", "0"},
            {"Device.Time.LocalTimeZone", "", "string(256)", "1", "0", "2"},

            {"Device.UserInterface.PasswordRequired", "", "boolean", "1", "0", "0"},
            {"Device.UserInterface.PasswordUserSelectable", "", "boolean", "1", "0", "0"},
            {"Device.UserInterface.UpgradeAvailable", "", "boolean", "1", "0", "0"},
            {"Device.UserInterface.WarrantyDate", "", "DateTime", "1", "0", "0"},
            {"Device.UserInterface.ISPName", "", "string(64)", "1", "0", "0"},
            {"Device.UserInterface.ISPHelpDesk", "", "string(32)", "1", "0", "0"},
            {"Device.UserInterface.ISPHomePage", "", "string(256)", "1", "0", "0"},
            {"Device.UserInterface.ISPHelpPage", "", "string(256)", "1", "0", "0"},
            {"Device.UserInterface.ISPLogo", "", "base64(5460)", "1", "0", "0"},
            {"Device.UserInterface.ISPLogoSize", "", "unsignedInt[0:4095]", "1", "0", "0"},
            {"Device.UserInterface.ISPMailServer", "", "string(256)", "1", "0", "0"},
            {"Device.UserInterface.ISPNewsServer", "", "string(256)", "1", "0", "0"},
            {"Device.UserInterface.TextColor", "", "string(6)", "1", "0", "0"},
            {"Device.UserInterface.BackgroundColor", "", "string(6)", "1", "0", "0"},
            {"Device.UserInterface.ButtonColor", "", "string(6)", "1", "0", "0"},
            {"Device.UserInterface.ButtonTextColor", "", "string(6)", "1", "0", "0"},
            {"Device.UserInterface.AutoUpdateServer", "", "string(256)", "1", "0", "0"},
            {"Device.UserInterface.UserUpdateServer", "", "string(256)", "1", "0", "0"},
            {"Device.UserInterface.AvailableLanguages", "", "string(256)", "0", "0", "0"},
            {"Device.UserInterface.CurrentLanguage", "", "string(16)", "1", "0", "2"},

            {"Device.LAN.AddressingType", "", "string(15)", "1", "0", "2"},
            {"Device.LAN.IPAddress", "", "string(15)", "1", "0", "2"},
            {"Device.LAN.SubnetMask", "", "string(15)", "0", "0", "0"},
            {"Device.LAN.DefaultGateway", "", "string(15)", "1", "0", "0"},
            {"Device.LAN.DNSServers", "", "string(256)", "1", "0", "0"},
            {"Device.LAN.DNSServers2", "", "string(256)", "0", "0", "0"},
            {"Device.LAN.MACAddress", "", "string(17)", "0", "0", "2"},
            {"Device.LAN.MACAddressOverride", "", "boolean", "0", "0", "0"},
            {"Device.LAN.DHCPOptionNumberOfEntries", "", "unsignedInt", "0", "0", "0"},
            {"Device.LAN.IsSupportIPV6", "", "boolean", "0", "0", "0"},

            {"Device.LAN.Stats.ConnectionUpTime", "", "unsignedInt", "0", "0", "0"},
            {"Device.LAN.Stats.TotalBytesSent", "", "unsignedInt", "0", "0", "0"},
            {"Device.LAN.Stats.TotalBytesReceived", "", "unsignedInt", "0", "0", "0"},
            {"Device.LAN.Stats.TotalPacketsSent", "", "unsignedInt", "0", "0", "0"},
            {"Device.LAN.Stats.TotalPacketsReceived", "", "unsignedInt", "0", "0", "0"},
            {"Device.LAN.Stats.CurrentDayInterval", "", "unsignedInt", "0", "0", "0"},
            {"Device.LAN.Stats.CurrentDayBytesSent", "", "unsignedInt", "0", "0", "0"},
            {"Device.LAN.Stats.CurrentDayBytesReceived", "", "unsignedInt", "0", "0", "0"},
            {"Device.LAN.Stats.CurrentDayPacketsSent", "", "unsignedInt", "0", "0", "0"},
            {"Device.LAN.Stats.CurrentDayPacketsReceived", "", "unsignedInt", "0", "0", "0"},
            {"Device.LAN.Stats.QuarterHourInterval", "", "unsignedInt", "0", "0", "0"},
            {"Device.LAN.Stats.QuarterHourBytesSent", "", "unsignedInt", "0", "0", "0"},
            {"Device.LAN.Stats.QuarterHourBytesReceived", "", "unsignedInt", "0", "0", "0"},
            {"Device.LAN.Stats.QuarterHourPacketsSent", "", "unsignedInt", "0", "0", "0"},
            {"Device.LAN.Stats.QuarterHourPacketsReceived", "", "unsignedInt", "0", "0", "0"},

            {"Device.LAN.IPPingDiagnostics.DiagnosticsState", "None", "string(256)", "0", "0", "0"},
            {"Device.LAN.IPPingDiagnostics.Host", "", "string(256)", "1", "0", "0"},
            {"Device.LAN.IPPingDiagnostics.NumberOfRepetitions", "3", "unsignedInt[1:]", "1", "0", "0"},
            {"Device.LAN.IPPingDiagnostics.Timeout", "3000", "unsignedInt[1:]", "1", "0", "0"},
            {"Device.LAN.IPPingDiagnostics.DataBlockSize", "32", "unsignedInt[1:65535]", "1", "0", "0"},
            {"Device.LAN.IPPingDiagnostics.DSCP", "0", "unsignedInt[0:64]", "1", "0", "0"},
            {"Device.LAN.IPPingDiagnostics.SuccessCount", "", "unsignedInt", "0", "0", "0"},
            {"Device.LAN.IPPingDiagnostics.FailureCount", "", "unsignedInt", "0", "0", "0"},
            {"Device.LAN.IPPingDiagnostics.AverageResponseTime", "", "unsignedInt", "0", "0", "0"},
            {"Device.LAN.IPPingDiagnostics.MinimumResponseTime", "", "unsignedInt", "0", "0", "0"},
            {"Device.LAN.IPPingDiagnostics.MaximumResponseTime", "", "unsignedInt", "0", "0", "0"},

            {"Device.LAN.TraceRouteDiagnostics.DiagnosticsState", "None", "string(256)", "0", "0", "0"},
            {"Device.LAN.TraceRouteDiagnostics.Host", "", "string(256)", "1", "0", "0"},
            {"Device.LAN.TraceRouteDiagnostics.Timeout", "3000", "unsignedInt[1:]", "1", "0", "0"},
            {"Device.LAN.TraceRouteDiagnostics.DataBlockSize", "32", "unsignedInt[1:65535]", "1", "0", "0"},
            {"Device.LAN.TraceRouteDiagnostics.MaxHopCount", "10", "unsignedInt[1:64]", "1", "0", "0"},
            {"Device.LAN.TraceRouteDiagnostics.DSCP", "0", "unsignedInt[0:64]", "1", "0", "0"},
            {"Device.LAN.TraceRouteDiagnostics.ResponseTime", "", "unsignedInt", "0", "0", "0"},
            {"Device.LAN.TraceRouteDiagnostics.NumberOfRouteHops", "", "unsignedInt", "0", "0",
                    "0"},
            {"Device.LAN.TraceRouteDiagnostics.FailureCount", "", "unsignedInt", "0", "0", "0"},
            {"Device.LAN.TraceRouteDiagnostics.AverageResponseTime", "", "unsignedInt", "0", "0", "0"},
            {"Device.LAN.TraceRouteDiagnostics.MinimumResponseTime", "", "unsignedInt", "0", "0", "0"},
            {"Device.LAN.TraceRouteDiagnostics.MaximumResponseTime", "", "unsignedInt", "0", "0", "0"},

            {"Device.LAN.TraceRouteDiagnostics.RouteHops.", "", "string(256)", "0", "0", "0"},

            {"Device.STBService.StreamingControlProtocols", "", "string(64)", "0", "0", "0"},
            {"Device.STBService.StreamingTransportProtocols", "", "string(64)", "0", "0", "0"},
            {"Device.STBService.StreamingTransportControlProtocols", "", "string(64)", "0", "0", "0"},
            {"Device.STBService.DownloadTransportProtocols", "", "string(64)", "0", "0", "0"},
            {"Device.STBService.MultiplexTypes", "", "string(64)", "0", "0", "0"},
            {"Device.STBService.MaxDejitteringBufferSize", "", "int[-1:]", "0", "0", "0"},
            {"Device.STBService.AudioStandards", "", "string(256)", "0", "0", "0"},
            {"Device.STBService.VideoStandards", "", "string(256)", "0", "0", "0"},

            {"Device.X_CMCC_OTV.STBInfo.STBID", "", "string(32)", "0", "0", "0"},
            {"Device.X_CMCC_OTV.STBInfo.PhyMemSize", "", "string(32)", "0", "0", "0"},
            {"Device.X_CMCC_OTV.STBInfo.StorageSize", "", "string(32)", "0", "0", "0"},
            {"Device.X_CMCC_OTV.STBInfo.AreaCode", "", "string(16)", "0", "0", "2"},
            {"Device.X_CMCC_OTV.STBInfo.Platform", "", "string(16)", "0", "0", "2"},
            {"Device.X_CMCC_OTV.STBInfo.QRCodeMessage", "", "string(64)", "1", "0", "2"},
            {"Device.X_CMCC_OTV.STBInfo.PPPoEID", "", "string(36)", "0", "0", "0"},
            {"Device.X_CMCC_OTV.STBInfo.PPPoEPassword", "", "string(16)", "0", "0", "0"},

            {"Device.X_CMCC_OTV.ServiceInfo.PPPoEID", "", "string(36)", "1", "0", "2"},
            {"Device.X_CMCC_OTV.ServiceInfo.PPPoEPassword", "", "string(16)", "1", "0", "0"},
            {"Device.X_CMCC_OTV.ServiceInfo.DHCPID", "", "string(36)", "1", "0", "0"},
            {"Device.X_CMCC_OTV.ServiceInfo.DHCPPassword", "", "string(16)", "1", "0", "0"},
            {"Device.X_CMCC_OTV.ServiceInfo.UserID", "", "string(36)", "1", "0", "2"},
            {"Device.X_CMCC_OTV.ServiceInfo.Password", "", "string(16)", "1", "0", "0"},
            {"Device.X_CMCC_OTV.ServiceInfo.AuthURL", "", "string(256)", "1", "0", "2"},
            {"Device.X_CMCC_OTV.ServiceInfo.SilentUpgrade", "", "Int(32)", "1", "0", "0"},
            {"Device.X_CMCC_OTV.ServiceInfo.UserInstallApplication", "", "Int(32)", "1", "0", "0"},
            {"Device.X_CMCC_OTV.ServiceInfo.PlatformURL", "", "string(256)", "1", "0", "0"},
            {"Device.X_CMCC_OTV.ServiceInfo.PlatformURLBackup", "", "string(256)", "1", "0", "0"},
            {"Device.X_CMCC_OTV.ServiceInfo.HDCURL", "", "string(256)", "1", "0", "0"},

            {"Device.X_CMCC_OTV.ServiceInfo.USBPermitInstalledAPP.NumofAPP", "", "Int(32)", "0", "0", "0"},
            {"Device.X_CMCC_OTV.ServiceInfo.AuthSucTime", "", "long", "0", "0", "2"},
            {"Device.X_CMCC_OTV.ServiceInfo.USBPermitInstalledAPP.UsbInstalledAppList.1.PackageName", "", "string",
                    "1", "0", "0"},
            {"Device.X_CMCC_OTV.ServiceInfo.USBPermitInstalledAPP.UsbInstalledAppList.1.ClassName", "", "string", "1"
                    , "0", "0"},

            {"Device.X_CMCC_OTV.Extention.AppAutoRunBlackListFlag", "", "boolean", "1", "0", "0"},
            {"Device.X_CMCC_OTV.Extention.NumOfAppAutoRunBlackList", "", "Int(32)", "0", "0", "0"},
            {"Device.X_CMCC_OTV.Extention.AppAutoRunBlackList.1.PackageName", "", "string", "1", "0", "0"},
            {"Device.X_CMCC_OTV.Extention.AppAutoRunBlackList.1.ClassName", "", "string", "1", "0", "0"},

            {"Device.X_CMCC_OTV.BandwidthDiagnostics.DiagnosticsState", "", "string", "1", "0", "0"},
            {"Device.X_CMCC_OTV.ServiceInfo.WiFiEnable", "", "Int", "1", "0", "2"},
            {"Device.X_00E0FC.AlarmSwitch", "", "Boolean", "1", "0", "0"},
            {"Device.X_00E0FC.AlarmReportLevel", "", "Int", "1", "0", "0"},
            {"Device.X_00E0FC.CPUAlarmValue", "", "string(16)", "1", "0", "0"},
            {"Device.X_00E0FC.MemoryAlarmValue", "", "string(16)", "1", "0", "0"},
            {"Device.X_00E0FC.DiskAlarmValue", "", "Int", "1", "0", "0"},
            {"Device.X_00E0FC.BandwidthAlarmValue", "", "string(16)", "1", "0", "0"},
            {"Device.X_00E0FC.PacketsLostAlarmValue", "", "string(16)", "1", "0", "0"},
            {"Device.X_00E0FC.TelnetEnable", "", "Boolean", "1", "0", "0"},
            {"Device.X_00E0FC.IsEncryptMark", "", "Int", "0", "0", "0"},
            {"Device.X_00E0FC.RecoveryMode", "", "Int", "0", "0", "0"},
            {"Device.X_00E0FC.ConnectMode", "", "Int", "0", "0", "0"},
            {"Device.X_00E0FC.HDMIConnect", "", "Boolean", "0", "0", "0"},
            {"Device.X_00E0FC.RemoteControlEnable", "", "Int", "1", "0", "0"},
            {"Device.X_00E0FC.Domain", "", "string(16)", "1", "0", "0"},
            {"Device.X_00E0FC.STBMonitorAddress", "", "string(32)", "1", "0", "0"},
            {"Device.X_00E0FC.Option125Enable", "", "Int", "1", "0", "0"},
            {"Device.X_00E0FC.AutoSwitchAccessModeEnable", "", "Int", "1", "0", "0"},
            {"Device.X_00E0FC.SoftwareVersionList", "", "String(32k)", "0", "0", "2"},
            {"Device.X_00E0FC.ErrorCodeSwitch", "", "Boolean", "1", "0", "0"},
            {"Device.X_00E0FC.ErrorCodeInterval", "", "Int", "1", "0", "0"},
            {"Device.X_00E0FC.STBID", "", "string(32)", "0", "0", "0"},
            {"Device.X_00E0FC.PhyMemSize", "", "string(32)", "0", "0", "0"},
            {"Device.X_00E0FC.StorageSize", "", "string(32)", "0", "0", "0"},
            {"Device.X_00E0FC.ForceUpgrade", "", "Boolean", "1", "0", "0"},
            {"Device.X_00E0FC.ParameterRestore", "", "string(256)", "1", "0", "0"},
            {"Device.X_00E0FC.TimeIntervalForLogToFlash", "", "unsignedInt[3600,86400]", "1", "0", "0"},
            {"Device.X_00E0FC.ErrorLogToFlashEnable", "", "Boolean", "0", "0", "0"},
            {"Device.X_00E0FC.ANRLogToFlashEnable", "", "Boolean", "0", "0", "0"},
            {"Device.X_00E0FC.PerfKPILogToFlashEnable", "", "Boolean", "0", "0", "0"},
            {"Device.X_00E0FC.IPv6Enable", "", "Boolean", "0", "0", "0"},
            {"Device.X_00E0FC.ErrorCodeLogToFlashEnable", "", "Boolean", "0", "0", "0"},
            {"Device.X_00E0FC.DoubleMACEnable", "", "Boolean", "1", "0", "0"},

            {"Device.X_00E0FC.ErrorCode.1.ErrorCodeTime", "", "DateTime", "1", "0", "0"},
            {"Device.X_00E0FC.ErrorCode.1.ErrorCodeValue", "", "string(32)", "1", "0", "0"},
            {"Device.X_00E0FC.ErrorCode.1.ErrorCodeSwitch", "", "Boolean", "1", "0", "0"},
            {"Device.X_00E0FC.ErrorCode.1.ErrorCodeInterval", "", "int", "1", "0", "0"},
            {"Device.X_00E0FC.ErrorCode.1.ErrorDesc", "", "string(512)", "1", "0", "0"},
            {"Device.X_00E0FC.ErrorCode.1.ErrorSource", "", "Int(8)", "1", "0", "0"},
            {"Device.X_00E0FC.ErrorCode.1.ErrorURL", "", "string(512)", "1", "0", "0"},

            {"Device.X_00E0FC.PlayDiagnostics.DiagnosticsState", "", "string(256)", "1", "0", "0"},
            {"Device.X_00E0FC.PlayDiagnostics.PlayURL", "", "string(256)", "1", "0", "0"},
            {"Device.X_00E0FC.PlayDiagnostics.PlayState", "", "int", "1", "0", "0"},

            {"Device.X_00E0FC.LogParaConfiguration.LogType", "0", "int", "1", "0", "0"},
            {"Device.X_00E0FC.LogParaConfiguration.LogLevel", "0", "int", "1", "0", "0"},
            {"Device.X_00E0FC.LogParaConfiguration.LogOutPutType", "0", "int", "1", "0", "0"},
            {"Device.X_00E0FC.LogParaConfiguration.SyslogServer", "", "string(512)", "1", "0", "0"},
            {"Device.X_00E0FC.LogParaConfiguration.SyslogStartTime", "", "DateTime", "1", "0", "0"},
            {"Device.X_00E0FC.LogParaConfiguration.SyslogContinueTime", "15", "unsignedInt[1:4320]", "1", "0", "0"},
            {"Device.X_00E0FC.LogParaConfiguration.LogTimer", "", "int", "1", "0", "0"},
            {"Device.X_00E0FC.LogParaConfiguration.LogFtpServer", "", "string", "1", "0", "0"},

            {"Device.X_00E0FC.AutoOnOffConfiguration.IsAutoPowerOn", "", "int", "1", "0", "0"},
            {"Device.X_00E0FC.AutoOnOffConfiguration.AutoPowerOnTime", "", "string", "1", "0", "0"},
            {"Device.X_00E0FC.AutoOnOffConfiguration.AutoShutdownTime", "", "string", "1", "0", "0"},

            {"Device.X_00E0FC.BandwidthDiagnostics.DiagnosticsState", "", "int", "1", "0", "0"},
            {"Device.X_00E0FC.BandwidthDiagnostics.DownloadURL", "", "string(512)", "1", "0", "0"},
            {"Device.X_00E0FC.BandwidthDiagnostics.Username", "", "string(128)", "1", "0", "0"},
            {"Device.X_00E0FC.BandwidthDiagnostics.Password", "", "string(128)", "1", "0", "0"},
            {"Device.X_00E0FC.BandwidthDiagnostics.ErrorCode", "", "string(128)", "0", "0", "0"},
            {"Device.X_00E0FC.BandwidthDiagnostics.AvgSpeed", "", "int", "0", "0", "0"},
            {"Device.X_00E0FC.BandwidthDiagnostics.MaxSpeed", "", "int", "0", "0", "0"},
            {"Device.X_00E0FC.BandwidthDiagnostics.MinSpeed", "", "int", "0", "0", "0"},

            {"Device.X_00E0FC.PacketCapture.State", "1", "int", "1", "0", "0"},
            {"Device.X_00E0FC.PacketCapture.Duration", "", "int", "1", "0", "0"},
            {"Device.X_00E0FC.PacketCapture.IP", "", "string(15)", "1", "0", "0"},
            {"Device.X_00E0FC.PacketCapture.Port", "", "int", "1", "0", "0"},
            {"Device.X_00E0FC.PacketCapture.UploadURL", "", "string(256)", "1", "0", "0"},
            {"Device.X_00E0FC.PacketCapture.Username", "", "string(32)", "1", "0", "0"},
            {"Device.X_00E0FC.PacketCapture.Password", "", "string(256)", "1", "0", "0"},

            {"Device.X_00E0FC.Upgrade.BandwidthMonitorPeriod", "", "int", "1", "0", "0"},
            {"Device.X_00E0FC.Upgrade.BandwidthMonitorPeriodNumber", "", "int", "1", "0", "0"},
            {"Device.X_00E0FC.Upgrade.StartDownloadBandwidth", "", "int", "1", "0", "0"},
            {"Device.X_00E0FC.Upgrade.StopDownloadBandwidth", "", "int", "1", "0", "0"},
            {"Device.X_00E0FC.Upgrade.DownloadBitrate", "", "int", "1", "0", "0"},
            {"Device.X_00E0FC.Upgrade.MaxDownloadTime", "", "int", "1", "0", "0"},

            {"Device.X_00E0FC.DebugInfo.Action", "", "int", "1", "0", "0"},
            {"Device.X_00E0FC.DebugInfo.State", "", "int", "0", "0", "0"},
            {"Device.X_00E0FC.DebugInfo.CapCommand", "", "String(1024)", "1", "0", "0"},
            {"Device.X_00E0FC.DebugInfo.UploadURL", "", "String(1024)", "1", "0", "0"},
            {"Device.X_00E0FC.DebugInfo.Username", "", "String(32)", "1", "0", "0"},
            {"Device.X_00E0FC.DebugInfo.Password", "", "String(32)", "1", "0", "0"},

            {"Device.X_00E0FC.StartupInfo.Action", "", "int", "1", "0", "0"},
            {"Device.X_00E0FC.StartupInfo.State", "", "int", "0", "0", "0"},
            {"Device.X_00E0FC.StartupInfo.MaxSize", "", "int", "1", "0", "0"},
            {"Device.X_00E0FC.StartupInfo.UploadURL", "", "string(1024)", "1", "0", "0"},
            {"Device.X_00E0FC.StartupInfo.Username", "", "string(32)", "1", "0", "0"},
            {"Device.X_00E0FC.StartupInfo.Password", "", "string(32)", "1", "0", "0"},

            {"Device.UserInterface.Logo.X_CT-COM_StartPicURL", "", "string(256)", "1", "0", "0"},
            {"Device.UserInterface.Logo.X_CT-COM_StartPic_Enable", "", "boolean", "1", "0", "0"},
            {"Device.UserInterface.Logo.X_CT-COM_StartPic_Result", "", "unsignedInt", "0", "0", "0"},
            {"Device.UserInterface.Logo.X_CT-COM_BootPicURL", "", "string(256)", "1", "0", "0"},
            {"Device.UserInterface.Logo.X_CT-COM_BootPic_Enable", "", "boolean", "1", "0", "0"},
            {"Device.UserInterface.Logo.X_CT-COM_BootPic_Result", "", "unsignedInt", "0", "0", "0"},
            {"Device.UserInterface.Logo.X_CT-COM_AuthenticatePicURL", "", "string(256)", "1", "0", "0"},
            {"Device.UserInterface.Logo.X_CT-COM_AuthenticatePic_Enable", "", "boolean", "1", "0", "0"},
            {"Device.UserInterface.Logo.X_CT-COM_AuthenticatePic_Result", "", "unsignedInt", "0", "0", "0"},
    };

    private static String sql = "INSERT INTO datamodel(name,value,type,writable,secure,notification) " +
            "VALUES ('%s','%s','%s','%s','%s','%s');";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS datamodel(_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "name TEXT not null unique,"
                + "value TEXT,"
                + "type TEXT,"
                + "writable INTEGER,"
                + "secure INTEGER,"
                + "notification INTEGER);");
        for (int i = 0; i < contents.length; i++) {
            db.execSQL(String.format(sql, contents[i][0], contents[i][1], contents[i][2],
                    contents[i][3], contents[i][4], contents[i][5]));
        }
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS datamodel;");
        onCreate(db);
    }


}