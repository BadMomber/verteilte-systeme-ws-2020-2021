
package udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;


public class UDPTransmitter {
    
    private DatagramSocket socket;
    private InetAddress address;
    
    private String ip;
    private int port;
    
    public UDPTransmitter(String ip, int port)  {
        this.port = port;
        this.ip = ip;
    }
    
    public void sendMessage(String msg) throws IOException {
        this.socket = new DatagramSocket();
        this.address = InetAddress.getByName(ip);
        byte[] buf = new byte[1024];
        buf = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        this.socket.close();
    }
    
    public void close() {
        this.socket.close();
    }
}
