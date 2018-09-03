import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    boolean disFlag = false;
    public static void main(String args[]){

        String joined = Arrays.toString(args);
        String split[] = joined.split(" ", 3);

        String ip = split[0].substring(1, split[0].length()-1);
        int port = Integer.parseInt(split[1].substring(0,split[1].length()-1));
        String welcomeMessage = split[2].substring(0,split[2].length()-1).replaceAll(",","");


        ChatServer chatS = new ChatServer();
        chatS.go(port,welcomeMessage);
    }

    public void go(int port,String welcomeMessage){

        while(true) {

            System.out.println("Waiting for new client to connect\n\n");
            try (

                    ServerSocket serverSocket = new ServerSocket(port);                                 //Create server socket.
                    Socket clientSocket = serverSocket.accept();                                        //Creates a socket for the client.
                    PrintWriter out =
                            new PrintWriter(clientSocket.getOutputStream(), true);            //PrintWriter chains high level outputs down to byte code for the client socket to read.
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(clientSocket.getInputStream()));                      //BufferedReader chains the byte code input from the client up to high level strings.
            )

            {
                String inputLine, input = "", output;
                SimpleChatProtocol scp = new SimpleChatProtocol();
                scp.setWelcomeMessage(welcomeMessage);

                while ((inputLine = in.readLine()) != null) {
                    if (!inputLine.equals("")) {
                        input += inputLine + "\n";
                    }


                    if (input.equals("SCP ACKNOWLEDGE\nSCP END\n")) {
                        System.out.println("\nClient:\n" + input);
                        System.out.println("Client has been disconnected");
                        in.close();
                        clientSocket.close();
                        break;
                    }

                    if (inputLine.equals("SCP END")) {

                        System.out.println("\nClient:\n" + input);

                        if (firstLine(input).equals("SCP DISCONNECT")) {
                            out.println("SCP ACKNOWLEDGE\nSCP END\n");
                            System.out.println("Client has been disconnected");
                            in.close();
                            clientSocket.close();
                            break;
                        }
                        output = scp.processInput(input);
                        out.println(output);
                        if(firstLine(output).equals("SCP CHAT")) System.out.println("Other user is typing...");
                        input = "";

                        if (firstLine(output).equals("SCP REJECT")) {
                            System.out.println("Server has closed the socket\n");
                            out.close();
                            clientSocket.close();
                            break;
                        }
                    }

                }
            }catch (IOException ex){
                ex.printStackTrace();
            }
        }



    }

    private String firstLine(String message){
        String theFirstLine;

        String[] theSplitMessage = message.split("\\n");
        theFirstLine = theSplitMessage[0];

        return theFirstLine;
    }


}
