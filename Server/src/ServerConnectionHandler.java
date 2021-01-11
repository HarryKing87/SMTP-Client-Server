import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Base64;

public class ServerConnectionHandler implements Runnable {
    public static String CRLF = "\r\n";
    public static String LF = "\n";
    public static String EC = " ";
    public static String ServerDomainName = "ServerDomain.gr";
    socketManager _socketMngObjVar = null;
    ArrayList<socketManager> _active_clients = null;

    public ServerConnectionHandler(ArrayList<socketManager> inArrayListVar, socketManager inSocMngVar) {
        _socketMngObjVar = inSocMngVar;
        _active_clients = inArrayListVar;
    }

    public void run() {
        try {
            System.out.println("0 Client " + _socketMngObjVar.soc.getPort() + " Connected");
            System.out.println("0 SERVER : active clients : " + _active_clients.size());

            if (!_socketMngObjVar.soc.isClosed()) {
                ArrayList<String> Emails = new ArrayList<String>();
                Emails.add("Alice@ThatDomain.gr");
                Emails.add("Bob@MyTestDomain.gr");
                Emails.add("Mike@ServerDomain.gr");
                Emails.add("babishh96@hotmail.com");

                ArrayList<String> CommandStack = new ArrayList<String>();

                

                // ArrayList<String> CommandStack = new ArrayList<String>(); // Commands that
                // user selects
                //ArrayList<String> mail_data_buffer = new ArrayList<String>(); // The actual message
                //ArrayList<String> forward_path_buffer = new ArrayList<String>(); // Recipient
                //ArrayList<String> reverse_path_buffer = new ArrayList<String>(); // Mail From

                while (!_socketMngObjVar.soc.isClosed()) {
                    String clientMSG = _socketMngObjVar.recv();
                    System.out.println(
                            "SERVER : message FROM CLIENT : " + _socketMngObjVar.soc.getPort() + " --> " + clientMSG);

                    if (clientMSG.contains("QUIT")) {
                        System.out.println("5 SERVER : quiting client");
                        //
                        // SYNTAX (page 12 RFC 821)
                        // QUIT <SP> <SERVER domain> <SP> Service closing transmission channel<CRLF>
                        //
                        _socketMngObjVar.send(
                                "221" + LF + ServerDomainName + LF + " Service closing transmission channel" + CRLF);
                        _active_clients.remove(_socketMngObjVar);
                        System.out.print("5 SERVER : active clients : " + _active_clients.size());
                        CommandStack.clear();

                        return; // exiting thread
                    }

                    Server_SMTP_Handler(_socketMngObjVar, clientMSG, CommandStack);
                } // while socket NOT CLOSED
            }
        } catch (Exception except) {
            // Exception thrown (except) when something went wrong, pushing clientMSG to the
            // console
            System.out.println("Error in Server Connection Handler --> " + except.getMessage());
        }

    }

    private void Server_SMTP_Handler(socketManager sm, String clientMSG, ArrayList<String> CommandStack) {

        final String setPlainText = "\033[0;0m";
        final String setBoldText = "\033[0;1m";
        boolean REQUESTED_DOMAIN_NOT_AVAILABLE = false;
        String ServerDomainName = "ServerDomain.gr";
        //boolean SMTP_OUT_OF_STORAGE = false;
        //boolean SMTP_INSUFFICIENT_STORAGE = false;
        //boolean SMTP_LOCAL_PROCESSING_ERROR = false;
        boolean SUCCESS_STATE = false;
        //boolean WAIT_STATE = true;
        String sResponceToClient = "";
        String test = "";
        String cleanAddress = "";
        String crlfRemover = "";

        ArrayList<String> UsersInServerDomain = new ArrayList<String>();
        UsersInServerDomain.add("Alice");
        UsersInServerDomain.add("Bob");
        UsersInServerDomain.add("Mike");

        ArrayList<String> KnownDomains = new ArrayList<String>();
        KnownDomains.add("ThatDomain.gr");
        KnownDomains.add("MyTestDomain.gr");
        KnownDomains.add("ServerDomain.gr");

        /*-------------------------------------------
            
            EMAILS
            
        -------------------------------------------*/
        // These are the trusted emails that can both send and receive an email...
        ArrayList<String> Emails = new ArrayList<String>();
        Emails.add("babishh96@hotmail.com");
        Emails.add("ch.kynigopoulos@mc-class.gr");
        Emails.add("Alice@ThatDomain.gr");
        Emails.add("Bob@MyTestDomain.gr");
        Emails.add("Mike@ServerDomain.gr");

        // The recipient list (Only using it to verify each recipient for the RCPT TO command)
        ArrayList<String> Recipients = new ArrayList<String>();
        Recipients.add("ch.kynigopoulos@mc-class.gr");
        Recipients.add("Alice@ThatDomain.gr");
        Recipients.add("Bob@MyTestDomain.gr");
        Recipients.add("Mike@ServerDomain.gr");
        Recipients.add("babishh96@hotmail.com");
        

        /*-------------------------------------------
            
            MAIL DATA
            
        -------------------------------------------*/

        // CONTENTS FOR BABISHH96@HOTMAIL.COM
        ArrayList<String> BabismailContents = new ArrayList<String>();
        BabismailContents.add("Hello babishh96! How are you?");
        BabismailContents.add("Privacy Policy");
        BabismailContents.add("Good day Babis! We have some new offers for you!");

        // CONTENTS FOR CH.KYNIGOPOULOS@MC-CLASS.GR
        ArrayList<String> ChKynigopoulos = new ArrayList<String>();
        ChKynigopoulos.add("Hello ch.kynigo! How are you?");
        ChKynigopoulos.add("Test 1 for mails");
        ChKynigopoulos.add("Example for mails");

        // CONTENTS FOR ALICE@THATDOMAIN.GR
        ArrayList<String> Alice = new ArrayList<String>();
        Alice.add("Hello Alice! How are you?");
        Alice.add("Alice's Report");
        Alice.add("Your new delivery");

        // CONTENTS FOR BOB@MYTESTDOMAIN.GR
        ArrayList<String> Bob = new ArrayList<String>();
        Bob.add("Hello Bob! How are you?");
        Bob.add("New deadline!");
        Bob.add("Order Delivery");

        // CONTENTS FOR MIKE@SERVERDOMAIN.GR
        ArrayList<String> Mike = new ArrayList<String>();
        Mike.add("Hello Mike! How are you?");
        Mike.add("Order Return");
        Mike.add("Manchester United News");



        boolean GO_ON_CHECKS = true;

        try {
            if (clientMSG.contains(CRLF)) {

                if (clientMSG.contains("QUIT")) {
                    GO_ON_CHECKS = false;
                    CommandStack.clear();
                } else if (clientMSG.length() > 512 && GO_ON_CHECKS) {
                    sResponceToClient = "500" + CRLF;
                    System.out.println("error 500 -> Line too long");
                    SUCCESS_STATE = false;
                    GO_ON_CHECKS = false;
                }
                // error 501 -> Syntax error in parameters or arguments
                else if (clientMSG.split(" ").length < 1 && GO_ON_CHECKS) {
                    sResponceToClient = "501" + CRLF;
                    System.out.println("error 501 -> Syntax error in parameters or arguments");
                    SUCCESS_STATE = false;
                    GO_ON_CHECKS = false;
                }
                // error 504 -> Command parameter not implemented
                else if (clientMSG.length() < 4 && GO_ON_CHECKS) {
                    sResponceToClient = "504" + CRLF;
                    System.out.println("error 504 -> Command parameter not implemented");
                    SUCCESS_STATE = false;
                    GO_ON_CHECKS = false;
                }
                // error 421 -> <domain> Service not available
                else if (REQUESTED_DOMAIN_NOT_AVAILABLE && GO_ON_CHECKS) {
                    sResponceToClient = "421" + CRLF;
                    String domain_not_found = clientMSG.replaceAll("HELO ", "");
                    domain_not_found = domain_not_found.replaceAll(CRLF, "");
                    System.out.println("error 421 -> " + domain_not_found + " Service not available");
                    SUCCESS_STATE = false;
                    GO_ON_CHECKS = false;
                }

                else if (clientMSG.contains("HELO") && GO_ON_CHECKS) {

                    // Exporting the sender's mail from the Client's message...
                    test = clientMSG.replace("HELO", "");
                    crlfRemover = test.replace(CRLF, "");
                    cleanAddress = crlfRemover.replace(EC, "");

                    // Actually verifying if the user input address is valid and contained inside
                    // the Emails
                    // array...
                    if (Emails.contains(cleanAddress)) {
                        System.out.print(CommandStack);
                        sResponceToClient = "250 OK" + LF + ServerDomainName + CRLF;
                        System.out.println("SERVER response: " + sResponceToClient);
                        SUCCESS_STATE = true;
                        GO_ON_CHECKS = false;
                        CommandStack.add("HELO");
                        System.out.println(CommandStack.get(0)); // Takes the first sequential item (Every command that's being used) from the list and
                                            // displays it...

                                            
                    }

                    else {
                        System.out.print("The address inserted is not verified as a trusted one.");
                        sResponceToClient = (cleanAddress + " is not verified as a trusted one.");
                    }

                }

                // MAIL FROM Command

                else if (clientMSG.contains("MAIL FROM") && GO_ON_CHECKS) {

                    if (CommandStack.contains("HELO")) {
                        sResponceToClient = "250 OK" + LF + ServerDomainName + CRLF;
                        System.out.println("SERVER response: " + sResponceToClient);
                        SUCCESS_STATE = true;
                        GO_ON_CHECKS = false;
                        CommandStack.add("MAIL FROM");
                        System.out.println(CommandStack.get(1)); // Takes the second sequential item from the list and
                                                                 // displays it...
                    } else {
                        sResponceToClient = "Please use command HELO first";
                        System.out.println("Please use command HELO first");
                    }

                }

                else if (clientMSG.contains("RCPT TO") && GO_ON_CHECKS)

                {

                    // Verifying if the recipient's mail is "valid" (Included in the Recipients array)
                    if (Recipients.contains("ch.kynigopoulos@mc-class.gr") || Recipients.contains("babishh96@hotmail.com") || Recipients.contains("Alice@ThatDomain.gr") ||
                    Recipients.contains("Bob@MyTestDomain.gr") || Recipients.contains("Mike@ServerDomain.gr")) 
                    {
                        
                        // Preventing user from entering a case without using HELO
                        if (CommandStack.contains("MAIL FROM"))
                        {
                            sResponceToClient = "250 OK" + LF + ServerDomainName + CRLF;
                            System.out.println("SERVER response: " + sResponceToClient);
                            SUCCESS_STATE = true;
                            GO_ON_CHECKS = false;
                            CommandStack.add("RCPT TO");
                            System.out.println(CommandStack.get(2)); // Takes the third sequential item from the list
                                                                                            // and displays it...
                        }
                        
                        else 
                        {
                            sResponceToClient = "Please use command HELO first";
                            System.out.println("Please use command HELO first");
                        }
                    
                    }
                    else 
                        {
                            sResponceToClient = "This mail is not verified, please use another verified mail.";
                            System.out.println("This mail is not verified, please use another verified mail.");
                        }
                    

                }
                // Implement 354 for DATA... (Case Number 4)

                else if (clientMSG.contains("DATA") && GO_ON_CHECKS) {

                    // Checking if previous commands have worked, then the program
                    // can move to the next step...
                    if (CommandStack.contains("RCPT TO")) {
                        // This says that we have BytesIn = 354
                        sResponceToClient = "354" + LF + ServerDomainName + CRLF;
                        sm.send(sResponceToClient);
                        sm.output.flush(); // Display
                        System.out.println("SERVER response: " + sResponceToClient);
                        SUCCESS_STATE = true;
                        GO_ON_CHECKS = false;
                        CommandStack.add("DATA");
                        System.out.println(CommandStack.get(3));

                        // Exporting the Sender's mail from the Client's message...
                        sResponceToClient = "250 OK" + LF + ServerDomainName + CRLF;
                        // Displaying message contents for each user (Shared Mailbox)
                        Emails.removeAll(Emails);
                        //clientMSG = "";
                        test = clientMSG.replace("DATA", "");
                        cleanAddress = test;
                        Emails.add(cleanAddress);

                        // Checking the starting mails for each case...
                        boolean babis = Emails.stream().anyMatch((a) -> a.startsWith("babishh96@hotmail.com"));
                        boolean chKynigo = Emails.stream().anyMatch((a) -> a.startsWith("ch.kynigopoulos@mc-class.gr"));
                        boolean alice = Emails.stream().anyMatch((a) -> a.startsWith("Alice@ThatDomain.gr"));
                        boolean bob = Emails.stream().anyMatch((a) -> a.startsWith("Bob@MyTestDomain.gr"));
                        boolean mike = Emails.stream().anyMatch((a) -> a.startsWith("Mike@ServerDomain.gr"));

                        // Displaying all messages for Babis 
                        if (babis)
                        {
                            // RFC 821 Syntax <CRLF> . <CRLF>
                            for(int i = 0; i < BabismailContents.size(); i++)
                            {
                                sm.send(setBoldText + BabismailContents.get(i) + setPlainText + CRLF + "." + CRLF); // Displaying the first message...
                                sm.output.flush(); // Display
                                System.out.print(BabismailContents.get(i) + CRLF);
                            }
                        }

                        // Displaying all messages for ch.kynigo 
                        else if (chKynigo)
                        {
                            for(int i = 0; i < ChKynigopoulos.size(); i++)
                            {
                                // RFC 821 Syntax <CRLF> . <CRLF>
                                sm.send(setBoldText + ChKynigopoulos.get(i) + setPlainText  + CRLF + "." + CRLF); // Displaying the first message...
                                sm.output.flush(); // Display
                                System.out.print(ChKynigopoulos.get(i) + CRLF);
                            }
                        }

                        // Displaying all messages for Alice 
                        else if (alice)
                        {
                            for(int i = 0; i < Alice.size(); i++)
                            {
                                // RFC 821 Syntax <CRLF> . <CRLF>
                                sm.send(setBoldText + Alice.get(i) + setPlainText  + CRLF + "." + CRLF); // Displaying the first message...
                                sm.output.flush(); // Display
                                System.out.print(Alice.get(i) + CRLF);
                            }
                        }

                        // Displaying all messages for Bob 
                        else if (bob)
                        {
                            for(int i = 0; i < Bob.size(); i++)
                            {
                                // RFC 821 Syntax <CRLF> . <CRLF>
                                sm.send(setBoldText + Bob.get(i) + setPlainText  + CRLF + "." + CRLF); // Displaying the first message...
                                sm.output.flush(); // Display
                                System.out.print(Bob.get(i) + CRLF);
                            }
                        }

                        // Displaying all messages for Mike 
                        else if (mike)
                        {
                            for(int i = 0; i < Mike.size(); i++)
                            {
                                // RFC 821 Syntax <CRLF> . <CRLF>
                                sm.send(setBoldText + Mike.get(i) + setPlainText  + CRLF + "." + CRLF); // Displaying the first message...
                                sm.output.flush(); // Display
                                System.out.print(Mike.get(i) + CRLF);
                            }
                        }

                        else {
                            sm.send("This email doesn't exist or doesn't have any mails.");
                            sm.output.flush();
                            System.out.print("This email doesn't exist or doesn't have any mails.");
                        }
                        
                    } 
                    else {
                        sResponceToClient = "Please use command RCPT TO first";
                        System.out.println("Please use command RCPT TO first");
                    }
                }

                // NOOP Simple Implementation
                else if (clientMSG.contains("NOOP") && GO_ON_CHECKS) {
                    if (CommandStack.contains("DATA")) {
                        sResponceToClient = "250 OK" + LF + ServerDomainName + CRLF;
                        System.out.println("SERVER response: " + sResponceToClient);
                        SUCCESS_STATE = true;
                        GO_ON_CHECKS = false;
                        CommandStack.add("NOOP");
                        System.out.println(CommandStack.get(4));
                        System.out.println(CommandStack);
                    }

                    else {
                        sResponceToClient = "421 Please use command DATA first";
                        System.out.println("Please use command DATA first");

                    }

                }

                // RSET Command
                
                else if (clientMSG.contains("RSET") && GO_ON_CHECKS) {
                    // Flushing everything related to stored values...?!
                    Emails.removeAll(Emails);
                    Recipients.removeAll(Recipients);
                    // Sending OK message after clearing each value...
                    sResponceToClient = "250" + LF + ServerDomainName + CRLF;
                    System.out.println("SERVER response: " + sResponceToClient);
                    SUCCESS_STATE = true;
                    GO_ON_CHECKS = false;
                    CommandStack.add("RSET");
                    System.out.println(CommandStack.get(5));
                    System.out.println(CommandStack);


                    // REUSING HELO
                    // Exporting the sender's mail from the Client's message...
                    test = clientMSG.replace("HELO", "");
                    crlfRemover = test.replace(CRLF, "");
                    cleanAddress = crlfRemover.replace(EC, "");

                    // Actually verifying if the user input address is valid and contained inside
                    // the Emails
                    // array...
                    if (Emails.contains(cleanAddress)) {
                        System.out.print(CommandStack);
                        sResponceToClient = "250 OK" + LF + ServerDomainName + CRLF;
                        System.out.println("SERVER response: " + sResponceToClient);
                        SUCCESS_STATE = true;
                        GO_ON_CHECKS = false;
                        CommandStack.add("HELO");
                        System.out.println(CommandStack.get(0)); // Takes the first sequential item from the list and displays it...

                                            
                    }

                    else {
                        System.out.print("The address inserted is not verified as a trusted one.");
                        sResponceToClient = (cleanAddress + " is not verified as a trusted one.");
                    }
                }

                else if (clientMSG.contains("HELP") && GO_ON_CHECKS) {
                    if (CommandStack.contains("NOOP") && GO_ON_CHECKS) {
                        // sm.output.flush();
                        sResponceToClient = CRLF + "HELP: This command causes the receiver to send helpful information "
                                + "to the sender of the HELP command." + CRLF
                                + "HELO: At the time the transmission channel is opened there is an "
                                + "exchange to ensure that the hosts are communicating with the hosts "
                                + "they think they are." + CRLF + "MAIL FROM: The transaction "
                                + "is started with a MAIL command which gives the sender " + "identification." + CRLF
                                + "RCPT TO: This command gives a forward-path identifying one recipient." + CRLF
                                + "NOOP: This command has no effect on any of the reverse-path "
                                + "buffer, the forward-path buffer, or the mail data buffer." + CRLF
                                + "RSET: This command specifies that the current mail transaction is "
                                + "to be aborted.  Any stored sender, recipients, and mail data "
                                + "must be discarded, and all buffers and state tables cleared." + CRLF
                                + "DATA: This command causes the mail data "
                                + "from this command to be appended to the mail data buffer." + CRLF
                                + "QUIT: This command specifies that the receiver must send an OK "
                                + "reply, and then close the transmission channel.";
                        System.out.println("SERVER response: " + sResponceToClient);
                        SUCCESS_STATE = true;
                        GO_ON_CHECKS = false;
                        CommandStack.add("HELP");
                        System.out.println(CommandStack);
                    }

                    else {
                        sResponceToClient = "421 Please use command NOOP first"; // Response to Client
                        System.out.println("Please use command NOOP first"); // Response to Server
                    }

                }

                clientMSG = "";
            }

            sm.send(sResponceToClient);
        } catch (Exception except) {
            // Exception thrown (except) when something went wrong, pushing message to the
            // console
            System.out.println("Error --> " + except.getMessage());
        }
    }
}