package vs.car.central.main;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONObject;

public class CarAPI implements Runnable {
    
    private ServerSocket socket;
    private int port;
    private String dburl; 
    private String centralname;
    
    public CarAPI(int port, String dburl, String centralname) throws IOException {
        this.dburl = dburl;
        this.centralname = centralname;
        this.port = port;
        this.socket = new ServerSocket(this.port);
    }
    
    private String writeHttpHeader(String status, String length) {
        String httpHeader = "HTTP/1.1 " + status + "\r\n";
        httpHeader += "Content-Length: " + length + "\r\n";
        httpHeader += "Access-Control-Allow-Origin: *\r\n";
        httpHeader += "Content-Type: application/json\r\n\r\n";
                
        return httpHeader;
    }
    
     private void restGetSensorMeta(Socket connection, DataOutputStream httpOut, HTTPRequest httpReq) throws IOException {
        try {
            
            Connection dbconn = DriverManager.getConnection(this.dburl);
            String sql = "select * from sensorvalues where id in (select max(id) from sensorvalues group by sensortype)";
            Statement stmt = dbconn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            JSONArray json = new JSONArray();
            while(rs.next()) {
                JSONObject record = new JSONObject();
                record.put("name", rs.getString("sensortype"));
                record.put("lastvalue", (new JSONObject()).put("timestamp", rs.getString("time")).put("value", rs.getInt("value")));
                json.put(record);
            }
            dbconn.close();

            if(json.length() == 0) { //No Sensors found or no values
                String header = this.writeHttpHeader("404 Not Found", "0");
                httpOut.writeBytes(header);
                return;
            } else { //Sensors found
                String body = json.toString();
                String header = this.writeHttpHeader("200 Ok", Integer.toString(body.length()));
                httpOut.writeBytes(header.concat(body));
                return;
            }
           
        } catch (Exception ex) {
            String header = this.writeHttpHeader("500 Internal Server Error", "0");
            httpOut.writeBytes(header);
            return;
        }
     }
    
    private void restGetSensorValues(Socket connection, DataOutputStream httpOut, HTTPRequest httpReq, int limit) throws IOException {
        try {
            String sensortype = httpReq.path.split("/")[2].replaceAll("%20", " ");
            Connection dbconn = DriverManager.getConnection(this.dburl);
            String sql = "select * from sensorvalues where sensortype = '" + sensortype + "' order by id desc";
            if(limit >= 0) sql = sql.concat(" LIMIT " + Integer.toString(limit));
            Statement stmt = dbconn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            JSONArray json = new JSONArray();
            while(rs.next()) {
                JSONObject record = new JSONObject();
                record.put("timestamp", rs.getString("time"));
                record.put("value", rs.getDouble("value"));
                json.put(record);
            }
            dbconn.close();

            if(json.length() == 0) { //Sensor not found or no values
                String header = this.writeHttpHeader("404 Not Found", "0");
                httpOut.writeBytes(header);
                return;
            } else { //Sensor found values
                String body = json.toString();
                String header = this.writeHttpHeader("200 Ok", Integer.toString(body.length()));
                httpOut.writeBytes(header.concat(body));
                return;
            }
           
        } catch (Exception ex) {
            String header = this.writeHttpHeader("500 Internal Server Error", "0");
            httpOut.writeBytes(header);
            return;
        }
    }
    
    public void run() {
        System.out.println("API running");
        boolean error = false;
        while(!error) {
           
            try {
                
                Socket connection = this.socket.accept();
                BufferedReader httpIn = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                DataOutputStream httpOut = new DataOutputStream(connection.getOutputStream());
                
                HTTPRequest httpReq = new HTTPRequest(httpIn);
                
                
                if(httpReq.method.equals("GET")) {
                    
                    if(httpReq.path.equals("/")) {
                        String body = (new JSONObject()).put("centralname", this.centralname).toString();
                        String header = this.writeHttpHeader("200 Ok", Integer.toString(body.length()));
                        httpOut.writeBytes(header.concat(body));
                    } else if(httpReq.path.equals("/sensors/")) {
                        
                        this.restGetSensorMeta(connection, httpOut, httpReq);
                        
                    } else if((boolean) Pattern.matches("\\/sensors\\/.*\\/.*\\/$", httpReq.path)) { //get n values of this sensor
                        
                        if((boolean) Pattern.matches("\\/sensors\\/.*\\/[0-9]{1,}\\/$", httpReq.path)) { //catch bad request
                            this.restGetSensorValues(connection, httpOut, httpReq, Integer.parseInt(httpReq.path.split("/")[3]));
                        } else {
                            String header = this.writeHttpHeader("401 Bad Request", "0");
                            httpOut.writeBytes(header);    
                        } 
                        
                    } else if((boolean) Pattern.matches("\\/sensors\\/.*\\/$", httpReq.path)) {  //get all values of this sensor
                       
                        this.restGetSensorValues(connection, httpOut, httpReq, -1);
                       
                    } else {
                        String header = this.writeHttpHeader("404 Not Found", "0");
                        httpOut.writeBytes(header);
                    }
             
                    
                } else if(!httpReq.method.equals("GET")) { //Method is not GET
                    String header = this.writeHttpHeader("501 Not Implemented", "0");
                    httpOut.writeBytes(header);
                }
                
                connection.close();
                
            } catch (IOException ex) {
                error = true;
            } 
        }
    }
    
}


