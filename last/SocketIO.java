package com.selab;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.Charset;

public class SocketIO
{
    private static int INPUT_BUFFER_SIZE = 1024;
    private static int OUTPUT_BUFFER_SIZE = 1024;
    private static Charset ENCODING = Charset.forName("UTF-8");

    //UTF-8로 읽어와서 반환합니다.
    public static String read(Socket client) throws IOException
    {
        byte[] buffer = new byte[INPUT_BUFFER_SIZE];
        client.getInputStream().read(buffer);
        return new String(buffer, ENCODING).trim();
    }

    //UTF-8로 전송합니다.
    public static void write(Socket client, String text) throws IOException
    {
        client.getOutputStream().write(text.getBytes(ENCODING));
    }


    public static int getInputBufferSize()
    {
        return INPUT_BUFFER_SIZE;
    }
    public static void setInputBufferSize(int size)
    {
        INPUT_BUFFER_SIZE = size;
    }
    public static int getOutputBufferSize()
    {
        return OUTPUT_BUFFER_SIZE;
    }
    public static void setOUTputBufferSize(int size)
    {
        OUTPUT_BUFFER_SIZE = size;
    }

    public static Charset getENCODING()
    {
        return SocketIO.ENCODING;
    }
    public static void setENCODING(Charset encoding)
    {
        ENCODING = encoding;
    }
}
