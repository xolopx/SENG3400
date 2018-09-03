
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
        String[] lines  = theInput.split("\\n");
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

        String theMessage = ("SCP REJECT\n" +
                             "TIMEDIFFERENTIAL " + timeDiff + "\n" +
                             "REMOTEADDRESS " + clientAddress + "\n" +
                             "SCP END\n");
        return theMessage;
    }

    private String acceptMessage(String username, String clientAddress, int clientPort){
        String theMessage = ("SCP ACCEPT\n" +
                             "USERNAME " + username + "\n" +
                             "REMOTEADDRESS " + clientAddress + "\n" +
                             "SERVERPORT " + clientPort + "\n" +
                             "SCP END\n");
        return theMessage;
    }

    private String acknowledgementMessage(String username, String serverAddress, int serverPort){
        String message;
        message = "SCP ACKNOWLEDGE\nUSERNAME "+username+"\nSERVERADDRESS "+serverAddress+"\nSERVERPORT "+serverPort+"\nSCP END\n";
        return message;
    }

    private String chatMessage(){
        String message;
        String userInput;
        Scanner scan = new Scanner(System.in);
        message = "SCP CHAT\nREMOTE ADDRESS "+ clientAddress + "\nREMOTEPORT " + clientPort + "\nMESSAGECONTENT\n\n";
        System.out.println("Enter your message:");
        userInput = scan.nextLine();
        if(userInput.equals("DISCONNECT") ){
            message = exitMessage();
        }else {

            message += userInput;
            message += "\n" +
                    "SCP END\n";

        }
        return message;
    }

    private String chatMessage(String wM){
        String message;
        message = "SCP CHAT\nREMOTE ADDRESS "+ clientAddress + "\nREMOTEPORT " + clientPort + "\nMESSAGECONTENT\n"+ wM +"\nSCP END\n";
        return message;
    }

    public void setWelcomeMessage(String wM){
        welcomeMessage = wM;
    }

    private String exitMessage(){
        String theMessage;

        theMessage = "SCP DISCONNECT\nSCP END\n";

        return theMessage;
    }

    private String ackExit(){
        String theMessage;

        theMessage = "SCP ACKNOWLEDGE\nSCP END\n";

        return theMessage;
    }


}