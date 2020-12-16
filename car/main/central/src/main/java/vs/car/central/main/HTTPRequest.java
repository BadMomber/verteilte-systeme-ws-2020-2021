package vs.car.central.main;

import java.io.BufferedReader;
import java.io.IOException;

public class HTTPRequest {
    
    //used in rest api
    public String path;
    public String method;
    public String[] params;
    
    //additional
    public String protocolversion;
    public String cache;
    public String connection;
    public String useragent;
    public String accept;
    
    public HTTPRequest(BufferedReader httpIn) throws IOException {
        String line = httpIn.readLine();
        boolean firstrow = true;
        while(line != null && !line.equals("")) {
            
            //System.out.println(line);
            if(firstrow) {
                firstrow = false;
                
                this.method = line.split(" ")[0];
                this.protocolversion = line.split(" ")[2];
                
                String req = line.split(" ")[1]; //split at whitespace, get second param
                this.path = req.indexOf("?") != -1 ? req.split("\\?")[0] : req; //get path
                
                if(req.indexOf("?") != -1) {
                    String paramlist = req.split("\\?")[1];
                    this.params = paramlist.split("&");
                } else {
                    this.params = new String[0];
                }
                
            } else {
                int seperatorIndex = line.indexOf(":");
                if(seperatorIndex != -1) {
                    String key = line.substring(0, seperatorIndex);
                    String value = line.substring(seperatorIndex +1);
                    
                    switch(key) {
                        case "Connection":
                            this.connection = value;
                            break;
                        case "User-Agent":
                            this.useragent = value;
                            break;
                        case "Accept":
                            this.accept = value;
                            break;
                        case "Cache-Control":
                            this.cache = value;
                            break;
                    }
                }
            }
            
            line = httpIn.readLine();
        }
        
    }
    
    public String getParam(String key) {
        
        for(int i = 0; i < this.params.length; i++) {
            String _key = this.params[i].split("=")[0];
            String value = this.params[i].split("=")[1];
            
            if(_key.equals(key))
                return value;
        }
        
        return null;
    }
    
}
