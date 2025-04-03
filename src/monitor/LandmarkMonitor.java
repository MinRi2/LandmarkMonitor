package monitor;

import mindustry.mod.*;
import monitor.io.*;
import monitor.ui.*;

public class LandmarkMonitor extends Mod{

    @Override
    public void init(){
        super.init();

        MonitorSettings.init();
        MonitorUI.init();
    }
}
