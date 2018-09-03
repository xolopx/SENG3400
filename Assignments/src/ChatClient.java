import java.net.*;
import java.io.*;
import java.util.*;

public class ChatClient {

    boolean disFlag = false;

    public static void main(String args[]){

        String joined = Arrays.toString(args);
        String split[] = joined.split(" ", 2);

        String hostname = split[0].substring(1, split[0].length()-1);
        int port = Integer.parseInt(split[1].substring(0,split[1].length()-1));

        ChatClient chatC =  new ChatClient();
        chatC.go(hostname,port);

    }

    private void go(String hostname, int port) {

        try (
            Socket sock = new Socket(hostname, port);                                                       //Client socket to server.
            PrintWriter out = new PrintWriter(sock.getOutputStream(), true);                      //Chaining Outputs.
            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));           //Chaining Inputs.
        )
        {
            String inputLine, input = "", output;
            output = initialMessage(hostname,port);
            out.println(output);

            SimpleChatProtocol scp = new SimpleChatProtocol();

            //Lives in here while running.
            while((inputLine = in.readLine()) != null) {

                if(!inputLine.equals("")){
                    input += inputLine + "\r\n";
                }



                if(input.equals("SCP ACKNOWLEDGE\r\nSCP END\r\n")){
                    System.out.println("\nServer:\n" + input);
                    System.out.println("You have been disconnected from server");
                    in.close();
                    sock.close();
                    break;
                }



                if(inputLine.equals("SCP END")){
                    System.out.println("\nServer:\n" + input);

                    if(firstLine(input).equals("SCP DISCONNECT")) {
                        out.println("SCP ACKNOWLEDGE\r\nSCP END\r\n");
                        System.out.println("You have been disconnected from server");
                        in.close();
                        sock.close();
                        break;
                    }
                    output = scp.processInput(input);
                    out.println(output);
                    if(firstLine(output).equals("SCP CHAT")) System.out.println("Other user is typing...");
                    input = "";
                }
            }
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
    }

    public String initialMessage(String hostname, int port){
        String message = "SCP CONNECT\r\n" + "SERVERADDRESS " + hostname + "\r\n" + "SERVERPORT " + port + "\r\n";
        String username;
        Scanner scan = new Scanner(System.in);

        System.out.println("Please enter your username: ");
        username = scan.nextLine();
        System.out.println("\n");

        message += "REQUESTCREATED " + System.currentTimeMillis() + "\r\n" + "USERNAME \"" + username + "\"\r\n" + "SCP END\r\n";

        return message;
    }

    private String firstLine(String message){
        String theFirstLine;

        String[] theSplitMessage = message.split("\\r\\n");                     //The regex preserves the lines of text as god intended.
        theFirstLine = theSplitMessage[0];

        return theFirstLine;
    }
}
