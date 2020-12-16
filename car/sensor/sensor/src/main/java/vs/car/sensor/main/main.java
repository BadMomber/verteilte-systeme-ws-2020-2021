
package vs.car.sensor.main;

import java.io.IOException;
import java.util.Random;
import udp.UDPTransmitter;



public class main {
    
    public static void main(String[] args) {
        
       Sensor s = new Sensor();
       try {
            
            s.ip = args[0];
            s.port = Integer.parseInt(args[1]);
            s.sensortype = args[2];
            //System.out.println(s.ip + " " + s.port + " " + s.sensortype);
            
        } catch (ArrayIndexOutOfBoundsException e) {
            s.ip = "127.0.0.1";
            s.port = 51020;
            s.sensortype = "Generic Sensor";
            System.out.println("Defaulting to 127.0.0.1:51020 [Generic Sensor]");
        }
        
        //

            s.udp = new UDPTransmitter(s.ip, s.port);
            s.run();
        
    }
    
}

class Sensor {
    
    // params
    public String ip;
    public int port;
    public String sensortype;
    double prevValue = 0.0;
    
    // udp
    public UDPTransmitter udp;
    
    public void run() {
        Random rand = new Random();
        double index = 0;
        int tank = 0;
        String[] verkehr = {"frei", "maessiger_Verkehr", "starker_Verkehr", "Stau"};
        while(true) {
            try {
                index++;
                index = index % 100; //max 500
                Thread.sleep(1000); //wait for one second
                String sensorvalue;
                switch(this.sensortype) {
                    default:
                        sensorvalue = Integer.toString(rand.nextInt(101) - 50); //-50 to 50
                        break;
                    case "Tank":
                        if(prevValue == 0.0) {
                            prevValue = 100.0;
                        }
                        prevValue -= 0.01;
                        sensorvalue = String.valueOf(prevValue);
                        break;
                    case "Durchschnittsgeschwindigkeit":
                        if(prevValue == 0.0) {
                            prevValue = 20.0;
                        }
                        prevValue = prevValue + rand.nextDouble();
                        sensorvalue = String.valueOf(prevValue);
                        break;
                    case "Kilometerstand":
                        prevValue = prevValue + 1;
                        sensorvalue = String.valueOf(prevValue);
                        break;
                    case "Verkehrslage":
                        sensorvalue = verkehr[rand.nextInt(3)];
                        break;
                }

                udp.sendMessage("[" + this.sensortype + "] " + sensorvalue);
                //System.out.println(index + " " + sensorvalue);
                
            } catch (InterruptedException ex) {
                return;
            } catch (IOException ex) {
                return;
            }
        }
    }
    
}