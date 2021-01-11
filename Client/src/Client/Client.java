import java.io.*;
import java.net.*;
//import java.security.cert.CRL;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;



public class Client {
    //Main Method:- called when running the class file.
    public static void main(String[] args){ 
        
        int portNumber = 5000;
        String serverIP = "localhost";   
        
        try{
        //Create a new socket for communication
            Socket soc = new Socket(serverIP,portNumber);
        // use a semaphpre for thread synchronisation
            AtomicBoolean isDATA = new AtomicBoolean(false);
        // create new instance of the client writer thread, intialise it and start it running
            ClientReader clientRead = new ClientReader(soc, isDATA);
            Thread clientReadThread = new Thread(clientRead);
        //Thread.start() is required to actually create a new thread 
        //so that the runnable's run method is executed in parallel.
        //The difference is that Thread.start() starts a thread that calls the run() method,
        //while Runnable.run() just calls the run() method on the current thread
            clientReadThread.start();
            
        // create new instance of the client writer thread, intialise it and start it running
            ClientWriter clientWrite = new ClientWriter(soc, isDATA);
            Thread clientWriteThread = new Thread(clientWrite);
            clientWriteThread.start();
        }
        catch (Exception except){
            //Exception thrown (except) when something went wrong, pushing message to the console
            System.out.println("Error in SMTP_Client --> " + except.getMessage());
        }
    }
}



//This thread is responcible for writing messages
class ClientReader implements Runnable
{
    public static String ClientDomainName = "MyTestDomain.gr";
    public static String CRLF = "\r\n";
    public static String LF = "\n";
    public static String EC = " ";
    
    Socket crSocket = null;
    AtomicBoolean isDATAflag;
    String BYTESin= "";
    String sDataToServer;
    
    public ClientReader (Socket inputSoc, AtomicBoolean isDATA){
        crSocket = inputSoc;
        this.isDATAflag = isDATA;
    }
  
    //Decrypt

    private static String secretKey="kdfslksdnflsdfsd";
    private static final String ALGORITHM = "Blowfish";
    private static final String MODE = "Blowfish/CBC/PKCS5Padding";
    private static final String IV = "abcdefgh";


    public static  String decrypt(String value) throws Exception{
        byte[] values = Base64.getDecoder().decode(value);
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(MODE);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(IV.getBytes()));
        return new String(cipher.doFinal(values));
      }


    public void run(){

        while(!crSocket.isClosed()){
        // while connection is open and NOT IN DATA exchange STATE
            try
            {
                DataInputStream dataIn = new DataInputStream(crSocket.getInputStream());
                BYTESin = decrypt(dataIn.readUTF());
                System.out.println("Message from Server:" + BYTESin.replace("EC", ""));
                

                // Every error message for each case...
                if (BYTESin.contains("221")) {
                    System.out.println("SERVER response: " + BYTESin);
                    System.out.println("...closing socket");
                    crSocket.close();
                    return;
                } else if (BYTESin.contains("101")) {
                    System.out.println("The server is unable to connect.");
                } else if (BYTESin.contains("111")) {
                    System.out.println("Connection refused or inability to open an SMTP stream.");
                    System.out.println("SERVER response: " + BYTESin);
                } else if (BYTESin.contains("211")) {
                    System.out.println("System status message or help reply.");
                    System.out.println("SERVER response: " + BYTESin);
                } else if (BYTESin.contains("214")) {
                    System.out.println("SERVER response: " + BYTESin);
                } else if (BYTESin.contains("220")) {
                    System.out.println(ConsoleColors.GREEN + "The server is ready." + ConsoleColors.RESET);
                    System.out.println("SERVER response: " + BYTESin);
                } else if (BYTESin.contains("250")) {
                    System.out.println("OK -> CLIENT going to state SUCCESS");
                    // Replacing DATAflag to false...
                    isDATAflag.set(false);
                } else if (BYTESin.contains("251"))
                    System.out.println("SERVER Error--> User not local will forward");
                else if (BYTESin.contains("252"))
                    System.out.println(
                            "SERVER Error--> The server cannot verify the user, but it will try to deliver the message anyway.");
                else if (BYTESin.contains("354")) {
                    System.out.println("OK -> CLIENT going to state I (wait for data)");
                    //System.out.println(DataMail);
                    isDATAflag.set(true);
                } else if (BYTESin.contains("420"))
                    System.out.println("SERVER Error--> Timeout connection problem.");
                else if (BYTESin.contains("421"))
                    System.out.println("SERVER Error-->Service not available, closing transmission channel");
                else if (BYTESin.contains("422"))
                    System.out.println("SERVER Error--> The recipient’s mailbox has exceeded its storage limit.");
                else if (BYTESin.contains("431"))
                    System.out.println("Not enough space on the disk, out of memory");
                else if (BYTESin.contains("432"))
                    System.out.println("The recipient’s Exchange Server incoming mail queue has been stopped.");
                else if (BYTESin.contains("441"))
                    System.out.println("The recipient’s server is not responding.");
                else if (BYTESin.contains("442"))
                    System.out.println("The connection was dropped during the transmission.");
                else if (BYTESin.contains("446"))
                    System.out.println(
                            "The maximum hop count was exceeded for the message: an internal loop has occurred.");
                else if (BYTESin.contains("447"))
                    System.out.println(
                            "Your outgoing message timed out because of issues concerning the incoming server.");
                else if (BYTESin.contains("449"))
                    System.out.println("A routing error.");
                else if (BYTESin.contains("450"))
                    System.out.println("Requested action not taken – The user’s mailbox is unavailable.");
                else if (BYTESin.contains("451")) {
                    System.out.println("SERVER response: " + BYTESin);
                    System.out.println(
                            ConsoleColors.RED + "SERVER Error-->Requested action aborted – Local error in processing"
                                    + ConsoleColors.RESET);
                } 
                else if (BYTESin.contains("452")) {
                    System.out.println("SERVER response: " + BYTESin);
                    System.out.println(ConsoleColors.RED + "SERVER Error-->Too many emails sent or too many recipients."
                            + ConsoleColors.RESET);
                } 
                else if (BYTESin.contains("471")) {
                    System.out.println("SERVER response: " + BYTESin);
                    System.out.println(ConsoleColors.RED
                            + "SERVER Error-->An error of your mail server, often due to an issue of the local anti-spam filter."
                            + ConsoleColors.RESET);
                } 
                else if (BYTESin.contains("500"))
                {
                    System.out.println("SERVER Error--> Syntax error, command unrecognized");                           
                }

                else if (BYTESin.contains("501"))
                {
                    System.out.println("SERVER Error--> Syntax error in parameters or arguments");                            
                }
                else if (BYTESin.contains("502"))
                    System.out.println(ConsoleColors.RED + "SERVER Error--> The command is not implemented."
                            + ConsoleColors.RESET);
                else if (BYTESin.contains("503"))
                    System.out.println(ConsoleColors.RED
                            + "SERVER Error--> The server has encountered a bad sequence of commands, or it requires an authentication."
                            + ConsoleColors.RESET);
                else if (BYTESin.contains("504"))
                {
                    System.out.println("SERVER Error--> Command parameter not implemented");
                }
                else if (BYTESin.contains("510") || BYTESin.contains("511"))
                    System.out.println(ConsoleColors.RED + "SERVER Error--> Bad email address." + ConsoleColors.RESET);
                else if (BYTESin.contains("512"))
                    System.out.println(ConsoleColors.RED
                            + "SERVER Error--> A DNS error: the host server for the recipient’s domain name cannot be found."
                            + ConsoleColors.RESET);
                else if (BYTESin.contains("513"))
                    System.out.println(
                            ConsoleColors.RED + "SERVER Error--> Address type is incorrect." + ConsoleColors.RESET);
                else if (BYTESin.contains("523"))
                    System.out.println(ConsoleColors.RED
                            + "SERVER Error--> The total size of your mailing exceeds the recipient server’s limits."
                            + ConsoleColors.RESET);
                else if (BYTESin.contains("530"))
                    System.out.println(ConsoleColors.RED
                            + "SERVER Error--> Normally, an authentication problem. But sometimes it’s about the recipient’s server blacklisting yours, or an invalid email address."
                            + ConsoleColors.RESET);
                else if (BYTESin.contains("541"))
                    System.out.println(ConsoleColors.RED
                            + "SERVER Error--> The recipient address rejected your message: normally, it’s an error caused by an anti-spam filter."
                            + ConsoleColors.RESET);
                else if (BYTESin.contains("550")) {
                    System.out.println(ConsoleColors.RED
                            + "SERVER Error--> It usually defines a non-existent email address on the remote side."
                            + ConsoleColors.RESET);
                } else if (BYTESin.contains("551"))
                    System.out.println(
                            ConsoleColors.RED + "SERVER Error--> User not local or invalid address – Relay denied."
                                    + ConsoleColors.RESET);
                else if (BYTESin.contains("553"))
                    System.out.println(
                            ConsoleColors.RED + "SERVER Error--> Requested action not taken – Mailbox name invalid."
                                    + ConsoleColors.RESET);
                else if (BYTESin.contains("554"))
                    System.out.println(
                            ConsoleColors.RED + "SERVER Error--> the transaction has failed." + ConsoleColors.RESET);
                 
                
            }  
            catch (Exception except){
              //Exception thrown (except) when something went wrong, pushing message to the console
              System.out.println("Error in ClientReader --> " + except.getMessage());
            }
        }
    }
}


class ClientWriter implements Runnable
{
    public static String CRLF = "\r\n";
    public static String LF = "\n";   
    public static String EC = " ";
    public static String ClientDomainName = "MyTestDomain.gr";
    public static String ClientEmailAddress = "myEmail@"+ClientDomainName;
    
    
    Socket cwSocket = null;
    AtomicBoolean isDATAflag;
    
    public ClientWriter (Socket outputSoc, AtomicBoolean isDATA){
        cwSocket = outputSoc;
        this.isDATAflag=isDATA;
    }
    
// Encryption

private static String secretKey="kdfslksdnflsdfsd";
private static final String ALGORITHM = "Blowfish";
private static final String MODE = "Blowfish/CBC/PKCS5Padding";
private static final String IV = "abcdefgh";

public static  String encrypt(String value) throws Exception{
  SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
  Cipher cipher = Cipher.getInstance(MODE);
  cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(IV.getBytes()));
  byte[] values = cipher.doFinal(value.getBytes());
  return Base64.getEncoder().encodeToString(values);
}







    public void run(){
        String msgToServer ="";
        String BYTESin= "";
        String ClientDomainName = "MyTestDomain.gr";
        String RectMail = "receiptmail@mail.com";
        

        
    
        try{

            /*-------------------------------------------
            
            SENDER MAIL
            
            -------------------------------------------*/

            Scanner scan= new Scanner(System.in); //System.in is a standard input stream  
            DataOutputStream dataOut = new DataOutputStream(cwSocket.getOutputStream());

            System.out.print(ConsoleColors.BLUE + "Enter your email:" + ConsoleColors.RESET);  
            String mail= scan.nextLine();    //reads string 
            ClientDomainName = mail;
            // Actually allowing the server to read the mail (*Sender*)
            dataOut.writeUTF(encrypt(ClientDomainName));
            // Placing the newly entered string inside the ClientDomainName variable
            dataOut.flush(); // Verify user mail...

            
            /*-------------------------------------------
            
            RECIPIENT MAIL
            
            -------------------------------------------*/



            // Storing the recipient's mail
            // Server stores it inside an array
            Scanner scanRcpt= new Scanner(System.in);  
            DataOutputStream dataOutRcpt = new DataOutputStream(cwSocket.getOutputStream());
            System.out.print(ConsoleColors.BLUE + "Where would you like to send your mail? " + "\n" + ConsoleColors.RESET);  
            String mailRcpt = scan.nextLine();    //reads string 
            RectMail = mailRcpt;
            // Actually allowing the server to read the mail (* Sender *)
            dataOut.writeUTF(encrypt(RectMail));
            // Placing the newly entered string inside the ClientDomainName variable
            dataOut.flush(); // Display user mail...


            


            System.out.print(CRLF + "BASIC SENDER & RECIPIENT INFORMATION" + CRLF);
            System.out.print("-----------------------------------------------" + CRLF);
            System.out.print("Your email address is:"+mail + CRLF);
            System.out.print("You are sending to:"+RectMail + CRLF);
            System.out.print("-----------------------------------------------" + CRLF);
            System.out.println ("CLIENT WRITER: SELECT NUMBER CORRESPONDING TO SMTP COMMAND " +
            ConsoleColors.BLUE + "1...HELO " + 
            ConsoleColors.YELLOW + "2...MAIL TO " +
            ConsoleColors.PURPLE + "3...RCPT TO " +
            ConsoleColors.GREEN + "4...DATA " +
            ConsoleColors.WHITE + "5...RSET " +
            ConsoleColors.RED + "6...HELP " +
            ConsoleColors.BLUE + "7...QUIT " +
            ConsoleColors.YELLOW + "8...NOOP");


            while (!cwSocket.isClosed() && !isDATAflag.get()) {
                Scanner user_input = new Scanner(System.in);

                TimeUnit.SECONDS.sleep(2); // Leaving the server "edit" each command for two seconds...

                switch(user_input.next()){

                    // HELO Command
                    case "1": {
                        System.out.println("CLIENT WRITER SENDING HELO");
                        System.out.println("--------------------------");
                        System.out.println(ConsoleColors.BLUE+"Sending..."+ConsoleColors.RESET+
                                "HELO"+EC+ClientDomainName+CRLF);
                        msgToServer = "HELO"+EC+ClientDomainName+CRLF;
                        //dataOut.writeUTF(msgToServer);
                        dataOut.writeUTF(encrypt(msgToServer));
                        dataOut.flush();                         
                        break;
                    }

                    // MAIL Command
                    case "2": {
                        
                            System.out.println("CLIENT WRITER SET SENDER DATA");
                            System.out.println("-----------------------------");
                            // RFC 821: MAIL <SP> FROM:<reverse-path> <CRLF>
                            msgToServer = "MAIL" + EC + "FROM:" + "<" + ClientDomainName + ">" + CRLF;
                            System.out.println(ConsoleColors.BLUE + "Sending..." + EC + ConsoleColors.RESET + msgToServer);
                            //dataOut.writeUTF(msgToServer);
                            dataOut.writeUTF(encrypt(msgToServer));
                            dataOut.flush();
                        break;
                    }    
                    // RCPT Command                
                    case "3": {
                        
                        // console (RFC 5321 --> Page 35 4.1.1.3.)
                                     System.out.println("CLIENT WRITER RCPT TO");
                                     System.out.println("-----------------------------");
                                     //msgToServer = "MAIL FROM: " + ClientEmailAddress + CRLF;  
                                     //msgToServer = "RCPT TO:" + EC + RectMail + CRLF;
                                     System.out.println(ConsoleColors.BLUE + "Sending..." + EC + ConsoleColors.RESET + msgToServer);
                                     // RFC 821: RCPT <SP> TO:<forward-path> <CRLF>
                                     msgToServer = "RCPT" + EC + "TO:" + "<" + RectMail + ">" + CRLF;
                                     System.out.println(msgToServer);
                                        //dataOut.writeUTF(msgToServer);
                                        dataOut.writeUTF(encrypt(msgToServer));
                                        dataOut.flush();
                        break;
                    } 
                    // DATA Command
                    case "4": {
                        System.out.println("CLIENT WRITER DATA COMMAND");
                        System.out.println("--------------------------------");
                        msgToServer = "DATA" + CRLF;
                        //dataOut.writeUTF(ClientDomainName + " " + msgToServer); // Displays DATA
                        dataOut.writeUTF(encrypt(ClientDomainName + " " + msgToServer));
                        dataOut.flush();
                        break;                        
                                 }
                    // RSET Command
                    case "5":{
                        System.out.println("CLIENT WRITER CONVERSATION RESET");
                        System.out.println("--------------------------------");
                        //dataOut.writeUTF("RSET" + CRLF);
                        dataOut.writeUTF(encrypt("RSET" + CRLF));
                        // RFC 5321 Page 38 4.1.1.5.
                        msgToServer = "RSET" + CRLF;



                        System.out.println("Client: " + ClientEmailAddress);
                        System.out.println("Recipient: " + RectMail);
                        // Going back to empty values for each mail...
                        ClientEmailAddress = "";
                        RectMail = "";
                        System.out.println("Successfully resetted everything...");
                        System.out.println("Client: " + ClientEmailAddress);
                        System.out.println("Recipient: " + RectMail);
                        dataOut.flush();

                        // Prompting user to enter new  sender mail...

                        Scanner scan2= new Scanner(System.in); 

                        // Sender mail
                        System.out.print(ConsoleColors.BLUE + "Enter your email:" + ConsoleColors.RESET);  
                        String mail2= scan.nextLine();    //reads string 
                        ClientDomainName = mail2;
                        // Actually allowing the server to read the mail (*Sender*)
                        //dataOut.writeUTF(ClientDomainName);
                        dataOut.writeUTF(encrypt(ClientDomainName));
                        // Placing the newly entered string inside the ClientDomainName variable
                        dataOut.flush(); // Verify user mail...

                        // Prompting user to enter new  recipient mail...
                        Scanner scanRcpt2= new Scanner(System.in);   
                        DataOutputStream dataOutRcpt2 = new DataOutputStream(cwSocket.getOutputStream());
                        System.out.print(ConsoleColors.BLUE + "Where would you like to send your mail? " + "\n" + ConsoleColors.RESET);  
                        String mailRcpt2 = scan.nextLine();    //reads string 
                        RectMail = mailRcpt2;
                        // Actually allowing the server to read the mail (* Sender *)
                        dataOut.writeUTF(RectMail);
                        // Placing the newly entered string inside the ClientDomainName variable
                        dataOut.flush(); // Verify user mail...


                        


                        // Reusing the Navigation
                        System.out.println ("CLIENT WRITER: SELECT NUMBER CORRESPONDING TO SMTP COMMAND " +
                        ConsoleColors.BLUE + "1...HELO " + 
                        ConsoleColors.YELLOW + "2...MAIL TO " +
                        ConsoleColors.PURPLE + "3...RCPT TO " +
                        ConsoleColors.GREEN + "4...DATA " +
                        ConsoleColors.WHITE + "5...RSET " +
                        ConsoleColors.RED + "6...HELP " +
                        ConsoleColors.BLUE + "7...QUIT " +
                        ConsoleColors.YELLOW + "8...NOOP");
                        break;
                    }
                    // HELP Command
                    case "6": {
                        System.out.println("CLIENT WRITER SET HELP");
                        System.out.println("----------------------");
                        //dataOut.writeUTF("HELP" + CRLF);
                        dataOut.writeUTF(encrypt("HELP" + CRLF));
                        dataOut.flush();
                        break;
                    }
                    // QUIT Command
                    case "7": {
                        System.out.print("CLIENT : QUITing");                      
                        msgToServer = ("QUIT"+CRLF);
                        //dataOut.writeUTF(msgToServer);
                        dataOut.writeUTF(encrypt(msgToServer));
                        dataOut.flush();                         
                        System.out.println("...closing socket ");
                        return;
                    }
                    // NOOP Command 
                    case "8": {
                        System.out.println("CLIENT WRITER SET NOOP");
                        System.out.println("----------------------");
                        //dataOut.writeUTF("NOOP" + CRLF);
                        dataOut.writeUTF(encrypt("NOOP" + CRLF));
                        dataOut.flush();
                        break;
                    }
                    // SEND Command
                    default: {
                    System.out.print("There is no number like that!");
                }
                }        
            }                   
        }            
        catch (Exception except){
            //Exception thrown (except) when something went wrong, pushing message to the console
            System.out.println("Error in ClientWriter --> " + except.getMessage());
        }
    }

    
}
