package org.example.API;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        Socket socket = null;
        InputStreamReader isr = null;
        OutputStreamWriter osw = null;
        BufferedReader br = null;
        BufferedWriter bw = null;

        try{
            socket = new Socket("localhost", 1234);
            isr = new InputStreamReader(socket.getInputStream());
            osw = new OutputStreamWriter(socket.getOutputStream());

            br = new BufferedReader(isr);
            bw = new BufferedWriter(osw);

            Scanner scanner = new Scanner(System.in);

            while (true)
            {
                String msgToSend = scanner.nextLine();
                bw.write(msgToSend);
                bw.newLine();
                bw.flush();

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
