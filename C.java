
// Java program to illustrate Client side 
// Implementation using DatagramSocket 
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket; 
import java.net.DatagramSocket; 
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner; 
  
public class C
{ 
    public static void main(String args[]) throws IOException, Exception
    { 
        Scanner sc = new Scanner(System.in); 

        String input = sc.nextLine();

        String[] inputAr = input.split(" ", 3);

        if (inputAr.length != 3) {
            throw new Exception("The input must follow the format w x y");
        }

        if (inputAr[2].contains(" ")) {
            throw new Exception("The input must follow the format w x y. y should not contain a space");
        }

        String webserver = inputAr[0];

        int numBytes = Math.min(Integer.parseInt(inputAr[1]), 1460);

        int timeout = Integer.parseInt(inputAr[2]);

        Socket socket = new Socket(webserver, 80);


        // Do not flush automatically, we need to write more information
        // to it befoe flushing
        PrintWriter out = new PrintWriter(socket.getOutputStream());

        out.println("GET / HTTP/1.1");
        out.println("Host: " + webserver);
        out.println("");
        out.flush();

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        DatagramSocket ds = new DatagramSocket(); 

        InetAddress ip = InetAddress.getLocalHost();

        String outStr;

        while(( outStr = in.readLine() ) != null ) {

            // Start seperate thread to print line
            WebPagePrinter webPagePrinter = new WebPagePrinter(outStr);
            Thread thread = new Thread(webPagePrinter);
            thread.run();

            // Send over UDP on main thread
            byte buf[] = null;
    
            int count = 0;
            String sendStr = "";
    
            for (int i = 0; i < outStr.length(); i++) {
    
                count += 1; 
    
                sendStr += outStr.charAt(i);
    
                // Divide the line into sub strings of length numBytes
                // Send each packet over UDP with bytes = numBytes
                // except for the last packet, where bytes = remainder
                if (count == numBytes || i == outStr.length() - 1) {
                    buf = sendStr.getBytes(); 
    
                    DatagramPacket DpSend = new DatagramPacket(buf, buf.length, ip, 12321); 
    
                    ds.send(DpSend); 

                    ds.receive(DpSend);

                    byte[] receive = new byte[65535]; 

                    System.out.println(data(receive));
    
                    sendStr = "";
                    count = 0;
                }
    
            }
        }

        System.out.println("done");
        in.close();
        out.close();
        ds.close();
  
        // Step 1:Create the socket object for 
        // carrying the data. 
        // DatagramSocket ds = new DatagramSocket(); 
  
        // InetAddress ip = InetAddress.getLocalHost(); 
        // byte buf[] = null; 
  
        // loop while user not enters "bye" 
        // while (true) 
        // { 
        //     String inp = sc.nextLine(); 
  
        //     // convert the String input into the byte array. 
        //     buf = inp.getBytes(); 
  
        //     // Step 2 : Create the datagramPacket for sending 
        //     // the data. 
        //     DatagramPacket DpSend = 
        //           new DatagramPacket(buf, buf.length, ip, 12321); 
  
        //     // Step 3 : invoke the send call to actually send 
        //     // the data. 
        //     ds.send(DpSend); 
  
        //     // break the loop if user enters "bye" 
        //     if (inp.equals("bye")) 
        //         break; 
        // } 
    } 

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


class WebPagePrinter implements Runnable {

    String webPageLine;

    WebPagePrinter(String webPageLine) {
        this.webPageLine = webPageLine;
    }

    public void run() {
        // System.out.println(this.webPageLine);
    }

}