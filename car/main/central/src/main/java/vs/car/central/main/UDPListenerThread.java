package vs.car.central.main;

import java.io.IOException;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import udp.UDPReceiver;

public class UDPListenerThread extends UDPReceiver implements Runnable {
    
    private String dburl;
    private String centralname;
    
    public UDPListenerThread(int port, String dburl, String centralname) throws SocketException{
        super(port);
        this.dburl = dburl;
        this.centralname = centralname;
    }
    
    @Override
    public void run() {
        try {
            this.listen();
        } catch (IOException ex) {
            return;
        }
    }
    
    @Override
    public void receivedMessage(String msg, int port, String ip) {
        try {
            //log to console
            System.out.println(msg + ";" + port + ";" + ip);

            String sensortype = msg.substring(msg.indexOf("[") +1, msg.lastIndexOf("]"));
            if(sensortype.equals("Verkehrslage")) {
                String s = msg.split(" ")[msg.split(" ").length - 1];
                double value = 0.0;
                if(s.equals("frei")) {
                    value = 0.0;
                }
                if(s.equals("maessiger_Verkehr")) {
                    value = 1.0;
                }
                if(s.equals("starker_Verkehr")) {
                    value = 2.0;
                }
                if(s.equals("Stau")) {
                    value = 3.0;
                }
                Connection conn = DriverManager.getConnection(this.dburl);
                String sql = "INSERT INTO sensorvalues(sensortype, value) VALUES(?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, sensortype);
                stmt.setDouble(2, value);
                stmt.executeUpdate();
                conn.close();
            } else {
                double value = Double.parseDouble(msg.split(" ")[msg.split(" ").length - 1]);

                //Write into database
                Connection conn = DriverManager.getConnection(this.dburl);
                String sql = "INSERT INTO sensorvalues(sensortype, value) VALUES(?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, sensortype);
                stmt.setDouble(2, value);
                stmt.executeUpdate();
                conn.close();
            }

        } catch (SQLException ex) {
            System.out.println(ex.getMessage()); 
        }
    }
}
