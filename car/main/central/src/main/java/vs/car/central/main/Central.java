package vs.car.central.main;

import java.io.IOException;

public class Central {
    
    //params
    public String centralname;
    public int sensorcount;
    public int sensorstartport;
    public int apiport;
    
    // udp
    public UDPListenerThread[] udplts;
    
    public void init() {
        
        this.udplts = new UDPListenerThread[this.sensorcount];
        
        int i = 0;
        for(UDPListenerThread udp : this.udplts) {
            try {
                udp = new UDPListenerThread(this.sensorstartport + i, "jdbc:sqlite:./" + this.centralname.trim() + ".db", this.centralname);
                Thread t = new Thread(udp);
                t.setName("Sensor :" + this.sensorstartport + i);
                t.start();
            } catch (IOException ex) {
               return;
            }
            i++;
        }
        
    }
}
