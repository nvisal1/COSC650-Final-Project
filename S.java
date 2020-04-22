// Java program to illustrate Server side 
// Implementation using DatagramSocket 
import java.io.IOException; 
import java.net.DatagramPacket; 
import java.net.DatagramSocket; 
import java.net.InetAddress; 
import java.net.SocketException; 
  
public class S
{ 
    public static void main(String[] args) throws IOException 
    { 
        // Step 1 : Create a socket to listen at port 1234 
        DatagramSocket ds = new DatagramSocket(12321); 
        byte[] receive = new byte[65535]; 

        int totalReceived = 0;
  
        DatagramPacket DpReceive = null; 
        while (true) 
        { 
  
            // Step 2 : create a DatgramPacket to receive the data. 
            DpReceive = new DatagramPacket(receive, receive.length); 
  
            // Step 3 : revieve the data in byte buffer. 
            ds.receive(DpReceive); 
  
            System.out.println("Client:-" + data(receive)); 

            // Send ACK to client
            totalReceived += receive.length;

            byte[] totalReceivedBytes = Integer.toString(totalReceived).getBytes();

            InetAddress ip = DpReceive.getAddress();
            int port = DpReceive.getPort();

            DpReceive = new DatagramPacket(totalReceivedBytes, totalReceivedBytes.length, ip, port);

            ds.send(DpReceive);
  
            // Clear the buffer after every message. 
            receive = new byte[65535]; 
        } 
    } 
  
    // A utility method to convert the byte array 
    // data into a string representation. 
    public static StringBuilder data(byte[] a) 
    { 
        if (a == null) 
            return null; 
        StringBuilder ret = new StringBuilder(); 
        int i = 0; 
        while (a[i] != 0) 
        { 
            ret.append((char) a[i]); 
            i++; 
        } 
        return ret; 
    } 
}

