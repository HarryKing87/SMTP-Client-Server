import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Server {
    
    //Main Method:- called when running the class file.
    public static void main(String[] args){ 
        
        //Portnumber:- number of the port we wish to connect on.
        int portNumber = 5000;
        try{
            //Setup the socket for communication 
            ServerSocket serverSoc = new ServerSocket(portNumber);
            ArrayList<socketManager> clients = new ArrayList<socketManager>();
            
            while (true){
                
                //accept incoming communication
                System.out.println("Waiting for client");
                Socket soc = serverSoc.accept();
                socketManager temp = new socketManager(soc);
                clients.add(temp);
                //create a new thread for the connection and start it.
                ServerConnectionHandler sch = new ServerConnectionHandler(clients, temp);
                Thread schThread = new Thread(sch);
                schThread.start();
            }
            
        }
        catch (Exception except){
            //Exception thrown (except) when something went wrong, pushing message to the console
            System.out.println("Error --> " + except.getMessage());
        }
    }   
}