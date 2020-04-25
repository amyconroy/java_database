import Exceptions.IncorrectSQLException;
import Exceptions.InvalidQueryException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.*;

public class DBServer {
    final static char EOT = 4;

    public static void main(String[] args) throws IOException {
        new DBServer(8888);
    }

    public DBServer(int portNumber) throws IOException {
        while(true) {
            try {
                System.out.println("Server Listening");
                ServerSocket ss = new ServerSocket(portNumber);
                Socket socket = ss.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                String line;
                DBController dbController = new DBController();
                while ((line = in.readLine()) != null) {
                    processQuery(line, out, in, dbController);
                }
                out.close();
                in.close();
                socket.close();
                ss.close();
            }
            catch (IOException ioe) {
                System.err.println(ioe);
            }
        }
    }

    private void processQuery(String line, BufferedWriter out, BufferedReader in, DBController dbController) throws IOException {
       try{
           System.out.println("SQL : " + line);
           String output = dbController.preformQuery(line);
           out.write(line + "\n" + EOT + "\n");
           out.flush();
       }
        catch (IncorrectSQLException | InvalidQueryException e){
           out.write("ERROR:" + e + "\n" + EOT + "\n");
           out.flush();
           System.err.println(e);
        }
    }
}

