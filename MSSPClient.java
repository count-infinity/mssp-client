import java.io.*;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;

public class MSSPClient {

    private static final int IAC = 255;
    private static final int SB = 250;
    private static final int MSSP = 70;
    private static final int VAR = 1;
    private static final int VAL = 2;
    private static final int SE = 240;

    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 5555);
             InputStream in = socket.getInputStream()) {
             
            Map<String, String> msspValues = new LinkedHashMap<>();
            boolean readingMSSP = false;
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
          
           
           // Send MSSP-REQUEST
           // According to the MSSP protocol: IAC DO MSSP (IAC = 255, DO = 253, MSSP = 70)
           byte[] msspRequest = { (byte) 255, (byte) 253, (byte) 70 };
           out.write(msspRequest);
           out.flush();

            int byteRead;

            String currentKey ="";
            String currentVal ="";
            boolean done = false;
            while ((byteRead = in.read()) != -1 && !done) {
                switch (byteRead) {
                    case IAC:
                        int nextByte = in.read();
                        if (nextByte == SB) {
                            if (in.read() == MSSP) {
                                readingMSSP = true;
                            }
                        } else if (nextByte == SE && readingMSSP) {
                            readingMSSP = false;
                            done=true;
                        }
                        break;
                    case VAR:
                        if (readingMSSP) {

                            while ((byteRead=in.read()) != VAL)
                            {           
                               
                              buffer.write(byteRead);                            
                                
                            }
                            currentKey = buffer.toString();
                            buffer.reset();    

                        }
                        break;
                    case VAL:
                        if (readingMSSP) {

                            byteRead = in.read();

                            while (byteRead != IAC && byteRead != VAR)
                            {           
                               
                              buffer.write(byteRead);                            
                                
                            }
                            currentVal = buffer.toString();
                            buffer.reset();

                        }
                        break;
                    default:                      
                        break;
                }

                if (!readingMSSP && buffer.size() > 0) {
                    break; // End of MSSP block
                }
            }

            // Process MSSP Values
            System.out.println("MSSP Values:");
            msspValues.forEach((key, value) -> System.out.println(key + ": " + value));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
