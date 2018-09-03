
import java.util.Scanner;
import java.util.concurrent.TimeUnit;


public class SimpleChatProtocol {
    private static final int WAITING = 0;
    private static final int WAITINGACK = 1;
    private static final int CHAT = 2;
    private static final int DISCONNECT = 3;
    private int state = WAITING;                                                        //Initially the server should be waiting.
    String username;
    String clientAddress;
    int clientPort;
    String welcomeMessage;

    public String processInput(String theInput) {

        String theOutput = null;
        String[] lines  = theInput.split("\\r\\n");
        String[] usernameLine;
        String[] portSplit;


        if (state == WAITING) {

            switch(lines[0]){

                case "SCP CONNECT":                                     //Server

                    long timeDiff;
                    clientAddress ="N/A";

                    String[] time = lines[3].split(" ",2);
                    long timeSent = Long.parseLong(time[1]);

                    usernameLine = lines[4].split(" ",2);
                    username = usernameLine[1];

                    portSplit = lines[2].split(" ");
                    clientPort = Integer.parseInt(portSplit[1]);

                    try{
                        TimeUnit.SECONDS.sleep(1);              //TESTING FOR TIME OUT.
                    }catch(InterruptedException ex) {
                        ex.printStackTrace();
                    }


                    if((timeDiff = System.currentTimeMillis() - timeSent) >= 5000 ){
                        theOutput = rejectionMessage(timeDiff,clientAddress);
                    }
                    else{
                        theOutput = acceptMessage(username,clientAddress,clientPort);
                    }


                    state = WAITINGACK;
                break;

                case "SCP ACCEPT":
                    //Send acknowledgement message


                    usernameLine = lines[1].split(" ");
                    username = usernameLine[1];

                    clientAddress ="N/A";

                    portSplit = lines[3].split(" ");
                    clientPort = Integer.parseInt(portSplit[1]);

                    theOutput = acknowledgementMessage(username, clientAddress, clientPort);

                    state = CHAT;

                break;
            }
        }
        else if (state == WAITINGACK) {

            //Pre chat response from server.
            theOutput = chatMessage(welcomeMessage);

            state = CHAT;

        }else if(state == CHAT) {

                theOutput = chatMessage();

        }
        return theOutput;
    }










    private String rejectionMessage(Long timeDiff, String clientAddress){

        String theMessage = ("SCP REJECT\r\n" +
                             "TIMEDIFFERENTIAL " + timeDiff + "\r\n" +
                             "REMOTEADDRESS " + clientAddress + "\r\n" +
                             "SCP END\r\n");
        return theMessage;
    }

    private String acceptMessage(String username, String clientAddress, int clientPort){
        String theMessage = ("SCP ACCEPT\r\n" +
                             "USERNAME " + username + "\r\n" +
                             "REMOTEADDRESS " + clientAddress + "\r\n" +
                             "SERVERPORT " + clientPort + "\r\n" +
                             "SCP END\r\n");
        return theMessage;
    }

    private String acknowledgementMessage(String username, String serverAddress, int serverPort){
        String message;
        message = "SCP ACKNOWLEDGE\r\nUSERNAME "+username+"\r\nSERVERADDRESS "+serverAddress+"\r\nSERVERPORT "+serverPort+"\r\nSCP END\r\n";
        return message;
    }

    private String chatMessage(){
        String message;
        String userInput;
        Scanner scan = new Scanner(System.in);
        message = "SCP CHAT\r\nREMOTE ADDRESS "+ clientAddress + "\r\nREMOTEPORT " + clientPort + "\r\nMESSAGECONTENT\n\n";
        System.out.println("Enter your message:");
        userInput = scan.nextLine();
        if(userInput.equals("DISCONNECT") ){
            message = exitMessage();
        }else {

            message += userInput;
            message += "\r\n" +
                    "SCP END\r\n";

        }
        return message;
    }

    private String chatMessage(String wM){
        String message;
        message = "SCP CHAT\r\nREMOTE ADDRESS "+ clientAddress + "\r\nREMOTEPORT " + clientPort + "\r\nMESSAGECONTENT\n\n"+ wM +"\r\nSCP END\r\n";
        return message;
    }

    public void setWelcomeMessage(String wM){
        welcomeMessage = wM;
    }

    private String exitMessage(){
        String theMessage;

        theMessage = "SCP DISCONNECT\r\nSCP END\r\n";

        return theMessage;
    }

    private String ackExit(){
        String theMessage;

        theMessage = "SCP ACKNOWLEDGE\r\nSCP END\r\n";

        return theMessage;
    }


}