package com.selab;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.*;
import java.util.Date;
import java.util.Queue;
import java.util.LinkedList;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.*;
import com.google.gson.stream.JsonReader;

public class ServerManager
{
    private ServerSocket socket = null;

    //대기중인 커맨드를 저장하는 큐
    private Queue<String> fromPhoneCommandList = new LinkedList<>();
    private Queue<String> fromDeviceCommandList = new LinkedList<>();


    public ServerManager(int port) throws IOException
    {
        socket = new ServerSocket(port);
    }

    public void log(String text)
    {
        System.out.println("@시간: "+new Date().toString()+" ## "+text);
    }

    public void run()
    {

        while(true)
        {
            System.out.println("## Server is running");
            try
            {
                Socket client = socket.accept();
                System.out.println("접속"+client.getLocalSocketAddress());
                var inputStream = client.getInputStream();

                byte[] buffer = new byte[1024];
                inputStream.read(buffer);

                String encodedString = new String(buffer, "UTF-8");

                var jsonObject = JsonParser.parseString(encodedString.trim()).getAsJsonObject();

                var sender = jsonObject.get("sender").getAsString();

                if(sender.equals("phone")) {
                    //폰에게서 받은 처리
                    var command = jsonObject.get("cmd").getAsString();

                    if (command.equals("notify")) {
                        client.close();
                        fromPhoneCommandList.add("notify");
                        log("notify 커맨드 등록(fromPhone)");
                        continue;
                    }
                    else if(command.equals("check"))
                    {
                        log("check");
                        if(fromDeviceCommandList.isEmpty())
                        {
                            log("비어있음");
                            client.getOutputStream().write("no".getBytes("UTF-8"));
                            client.close();
                            continue;
                        }
                        else if(fromDeviceCommandList.poll().equals("notify"))
                        {
                            var outputStream= client.getOutputStream();
                            outputStream.write("notify".getBytes("UTF-8"));
                            client.close();
                            continue;
                        }
                    }
                }
                else if(sender.equals("device"))
                {
                    //장치로부터 받은 처리
                    var command = jsonObject.get("cmd").getAsString();
                    if(command.equals("check"))
                    {
                        log("check");
                        if(fromPhoneCommandList.isEmpty()) //대기중인 명령어 없음
                        {
                            log("비어있음");
                            client.getOutputStream().write("no".getBytes("UTF-8"));
                            client.close();
                            continue;
                        }
                        else if(fromPhoneCommandList.poll().equals("notify"))
                        {
                            var outputStream= client.getOutputStream();
                            outputStream.write("notify".getBytes("UTF-8"));
                            client.close();
                            continue;
                        }
                    }else if (command.equals("notify"))
                    {
                        client.close();
                        fromDeviceCommandList.add("notify");
                        log("notify 커맨드 등록(fromDevice)");
                        continue;
                    }
                }
                else
                {
                    //오류 발생
                    continue;
                }
            }
            catch(JsonSyntaxException e)
            {
                System.out.println(e.getCause());
            }
            catch (Exception e)
            {
                System.out.println("error");
                e.printStackTrace();
            }

        }
    }
}
