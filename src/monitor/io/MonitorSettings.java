package monitor.io;

import MinRi2.ModCore.io.*;

/**
 * @author minri2
 * Create by 2025/4/2
 */
public class MonitorSettings{
    public static MinModSettings settings;

    public static void init(){
        settings = MinModSettings.registerSettings("landmark-monitor");
    }
}
