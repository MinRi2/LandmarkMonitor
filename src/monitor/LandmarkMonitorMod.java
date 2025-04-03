package monitor;

import mindustry.mod.*;
import monitor.io.*;
import monitor.ui.*;

public class LandmarkMonitorMod extends Mod{
    public static final String modName = "landmark-monitor";

    @Override
    public void init(){
        super.init();

        MonitorSettings.init();
        MonitorUI.init();
    }
}
