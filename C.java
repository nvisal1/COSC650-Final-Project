import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket; 
import java.net.DatagramSocket; 
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
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

        StringBuilder totalBytesSent = new StringBuilder();

        boolean isFirstSend = true;
        long startTime = 0;

        while (!((outStr = in.readLine()).equals("0"))) {
   
            // Start seperate thread to print line
            WebPagePrinter webPagePrinter = new WebPagePrinter(outStr);
            Thread thread = new Thread(webPagePrinter);
            thread.run();

            // Send over UDP on main thread
            byte buf[] = null;
    
            int count = 0;
            String sendStr = "";
    
            for (int i = 0; i < outStr.length(); i++) {

                boolean didResend = false;
    
                count += 1; 
    
                sendStr += outStr.charAt(i);
    
                // Divide the line into sub strings of length numBytes
                // Send each packet over UDP with bytes = numBytes
                // except for the last packet, where bytes = remainder
                if (count == numBytes || i == outStr.length() - 1) {
                    buf = sendStr.getBytes(); 
    
                    DatagramPacket DpSend = new DatagramPacket(buf, buf.length, ip, 12321); 
    
                    // Start timer if first send
                    if (isFirstSend) {
                        startTime = System.nanoTime();
                        isFirstSend = false;
                    }

                    ds.send(DpSend); 

                    // Get ACK and respond to timeout
                    
                    byte[] receive = new byte[65535]; 

                    DatagramPacket DpReceive = new DatagramPacket(receive, receive.length); 

                    ds.setSoTimeout(timeout);
                    while(true) {
                        try {
                            ds.receive(DpReceive);
                            if (( totalBytesSent = data(receive)) != null) {
                                break;
                            }

                        } catch (SocketTimeoutException e) {
                            if (!didResend) {
                                ds.send(DpSend);
                                didResend = true;
                            } else {
                                // Failed again after retrying, send message FAIL and quit
                                outStr = "FAIL";

                                buf = outStr.getBytes();

                                DpSend = new DatagramPacket(buf, buf.length, ip, 12321);

                                ds.send(DpSend);

                                throw new Exception("Failed to get ACK before timeout after resend");
                            }
                        }
                    }

                    System.out.println("ACK");

                    sendStr = "";
                    count = 0;
                }
    
            }
        }

        System.out.println("DONE");

        long endTime = System.nanoTime();
        long timeInMilli = (endTime - startTime) / 1000000;
        System.out.println(timeInMilli);

        System.out.println(totalBytesSent);

        in.close();
        out.close();
        ds.close();
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
        System.out.println(this.webPageLine);
    }

}