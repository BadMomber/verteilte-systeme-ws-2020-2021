package udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;


public class UDPReceiver {
    
    private int port;
    
    private DatagramSocket socket;
    
    public UDPReceiver (int port) throws SocketException {
        this.port = port;
        this.socket = new DatagramSocket(this.port);
    }
    
    public void listen() throws IOException {
        byte[] received = new byte[1024];
        while(true) {
            DatagramPacket rec = new DatagramPacket(received, received.length);
            this.socket.receive(rec);
            
            this.receivedMessage(new String(rec.getData(), 0, rec.getLength()), this.port, rec.getAddress().toString());
        }
    }
    
    public void receivedMessage(String msg, int port, String ip) {
        System.out.println(msg);
    }
    
}
