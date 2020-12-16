package vs.car.central.main;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class main {
    
    public static void main(String[] args) throws ClassNotFoundException {
        System.out.println("Station started");
        
       
        //init args
        Central s = new Central();
        try {
            s.centralname = args[0];
            s.sensorcount = Integer.parseInt(args[1]);
            s.sensorstartport = Integer.parseInt(args[2]);
            s.apiport = Integer.parseInt(args[3]);
        } catch (ArrayIndexOutOfBoundsException e) {
            s.centralname = "Generic Central";
            s.sensorcount = 4;
            s.sensorstartport = 51020;
            s.apiport = 8080;
            System.out.println("Default to Generic Central with 1 Sensor at :51020 and REST API at :8080");
        }
        
        s.init(); //open threads that listen for udp packages from sensors
        
        //init database
        Class.forName("org.sqlite.JDBC");
        String dburl = "jdbc:sqlite:./" + s.centralname.trim() + ".db";
        
        try {
            Connection conn = DriverManager.getConnection(dburl);
            if(conn != null) {
                
                String sqlTableCreate = "CREATE TABLE IF NOT EXISTS sensorvalues ("
                        + "id INTEGER PRIMARY KEY, "
                        + "sensortype VARCHAR(25) NOT NULL, "
                        + "value DOUBLE,"
                        + "time TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                        + ")";
                Statement stmt = conn.createStatement();
                stmt.execute(sqlTableCreate);
                
                Statement stmt2 = conn.createStatement();
                stmt2.execute("DELETE FROM sensorvalues");
       
                conn.close();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println("Closing because of error with database");
            return;
        }
        
        //init web api
        try {
            CarAPI api = new CarAPI(s.apiport, dburl, s.centralname);
            Thread t = new Thread(api);
            t.setName("REST API");
            t.start();
        } catch (IOException ex) {
            System.out.println("Closing because of error with api");
           return;
        }
       
    }
    
}