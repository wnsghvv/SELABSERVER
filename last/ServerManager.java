package com.selab;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Queue;
import java.util.LinkedList;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import static com.selab.Constants.*;
import java.io.InputStream;

public class ServerManager
{
    private ServerSocket socket = null;

    //대기중인 커맨드를 저장하는 큐
    private Queue<String> fromPhoneCommandQueue = new LinkedList<>();
    private Queue<String> fromDeviceCommandQueue = new LinkedList<>();
    private Queue<GpsInfo> GpsInfoQueue = new LinkedList<>();


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
                System.out.println("접속"+client.getRemoteSocketAddress());
                var inputStream = client.getInputStream();

                byte[] buffer = new byte[1024];
                inputStream.read(buffer);

                String encodedString = new String(buffer, "UTF-8");

                var jsonObject = JsonParser.parseString(encodedString.trim()).getAsJsonObject();

                var sender = jsonObject.get("sender").getAsString();

                if(sender.equals(PHONE)) {
                    //폰에게서 받은 처리
                    var command = jsonObject.get(COMMAND).getAsString();

                    if (command.equals(NOTIFY)) {
                        client.close();
                        fromPhoneCommandQueue.add(NOTIFY);
                        log("notify 커맨드 등록(fromPhone)");
                        continue;
                    }
                    else if(command.equals(CHECK))
                    {
                        log(CHECK);
                        if(fromDeviceCommandQueue.isEmpty())
                        {
                            log("비어있음");
                            client.getOutputStream().write(NO_COMMAND.getBytes("UTF-8"));
                            client.close();
                            continue;
                        }
                        else if(fromDeviceCommandQueue.poll().equals(NOTIFY))
                        {
                            var outputStream= client.getOutputStream();
                            outputStream.write(NOTIFY.getBytes("UTF-8"));
                            client.close();
                            continue;
                        }
                    }
                    else if(command.equals(GET_GPS)) {
                        client.close();
                        fromPhoneCommandQueue.add(GET_GPS);
                        log("getGps 커맨드 등록(fromPhone)");
                        continue;
                    }
                    else if(command.equals(CHECK_GPS))
                    {
                        log("CHECK_GPS 커맨드 등록(fromPhone)");
                        if(GpsInfoQueue.isEmpty())
                        {
                            log(GPS_NOT_YET);
                            client.getOutputStream().write(GPS_NOT_YET.getBytes("UTF-8"));
                            client.close();
                            continue;
                        }
                        else
                        {
                            var gpsInfo = GpsInfoQueue.poll();
                            var LATITUDE = jsonObject.get("lat").getAsDouble();
                            var LONGITUDE = jsonObject.get("lon").getAsDouble();
                            var object = new JsonObject();
                            object.addProperty(Constants.LATITUDE,gpsInfo.latitude);
                            object.addProperty(Constants.LONGITUDE,gpsInfo.longitude);
                            client.getOutputStream().write(object.toString().getBytes("UTF-8"));
                            client.close();
                            continue;
                        }
                    }
                    else if(command.equals(GET_PIC)) //사진을 찍어달라는 메시지
                    {
                        client.close();
                        fromPhoneCommandQueue.add(GET_PIC);
                        log("getpic 커맨드 등록(fromPhone)");
                        continue;
                    }
                    else if(command.equals(CHECK_PIC))
                    {
                        fromPhoneCommandQueue.add(CHECK_PIC);
                        if(fromDeviceCommandQueue.isEmpty())
                        {
                            log(PIC_NOT_YET);
                            client.getOutputStream().write(PIC_NOT_YET.getBytes("UTF-8"));
                            client.close();
                            continue;
                        }
                        else
                        {

                            Path filepath = new File("C:\\Users\\SELAB\\IdeaProjects\\Image\\tmp.jpg").toPath();
                            var data = Files.readAllBytes(filepath);

                            var out = client.getOutputStream();
                            out.write(data);

                            client.close();
                            continue;
                        }
                    }
                    else
                    {
                        log("알수없는 명령어: from phone");
                    }
                }
                else if(sender.equals(DEVICE))
                {
                    //장치로부터 받은 처리
                    var command = jsonObject.get(COMMAND).getAsString();
                    if(command.equals(CHECK))
                    {
                        log("check");
                        if(fromPhoneCommandQueue.isEmpty()) //대기중인 명령어 없음
                        {
                            log("비어있음");
                            client.getOutputStream().write(NO_COMMAND.getBytes("UTF-8"));
                            client.close();
                            continue;
                        }
                        else if(fromPhoneCommandQueue.poll().equals(NOTIFY))
                        {
                            var outputStream= client.getOutputStream();
                            outputStream.write(NOTIFY.getBytes("UTF-8"));
                            client.close();
                            continue;
                        }
                        //폰이 위치를 요청한게 있을 경우
                        else if(fromPhoneCommandQueue.poll().equals(GET_GPS))
                        {
                            var outputStream = client.getOutputStream();
                            outputStream.write(GET_GPS.getBytes("UTF-8"));

                            var readed = SocketIO.read(client);
                            var json = JsonParser.parseString(readed).getAsJsonObject();
                            var lat = json.get(LATITUDE).getAsDouble();
                            var lon = json.get(LONGITUDE).getAsDouble();
                            GpsInfoQueue.add(new GpsInfo(lon, lat));
                        }
                        else if(fromPhoneCommandQueue.poll().equals(GET_PIC))
                        {
                            var outputStream = client.getOutputStream();
                            outputStream.write(GET_PIC.getBytes("UTF-8"));

                            var in = client.getInputStream();
                            var imageBuffer = new byte[IMAGE_INPUT_BUFFER_SIZE];
                            in.read(imageBuffer);
                            in.close();
                            Files.write(new File("C:\\Users\\SELAB\\IdeaProjects\\Image\\temp.jpg").toPath(), imageBuffer);

                            continue;
                        }
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
