package org.example.API;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static void main(String[] args) throws IOException {

        Socket socket = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        OutputStreamWriter osw = null;
        BufferedWriter bw = null;

        ServerSocket ss = new ServerSocket(1234);

        while (true) {
            try {
                socket = ss.accept();

                isr = new InputStreamReader(socket.getInputStream());
                osw = new OutputStreamWriter(socket.getOutputStream());

                br = new BufferedReader(isr);
                bw = new BufferedWriter(osw);

                while (true) {
                    String msgReceived = br.readLine();

                    System.out.println("Client : " + msgReceived);
                    bw.newLine();
                    bw.flush();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}