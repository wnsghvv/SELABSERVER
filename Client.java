

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import com.google.gson.*;

public class Client {
    public static void main(String[] arg)
    {
        var buffer =new byte[1024];

        while(true)
        {
            try {
                Socket socket = new Socket("localhost",4107);

                var object =new JsonObject();
                object.addProperty("sender", "device");
                object.addProperty("cmd", "check");

                socket.getOutputStream().write(object.toString().getBytes("UTF-8"));

                socket.getInputStream().read(buffer);
                var read = new String(buffer, "UTF-8");
                System.out.println(read);
            }
            catch(IOException e)
            {}
        }
    }
}

