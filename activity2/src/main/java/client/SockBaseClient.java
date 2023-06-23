package client;

import java.net.*;
import java.io.*;
import org.json.*;
import buffers.RequestProtos.Request;
import buffers.ResponseProtos.Response;
import buffers.ResponseProtos.Entry;
import java.util.*;
import java.util.stream.Collectors;

class SockBaseClient {

    private static Request.OperationType getUserInputOperationType(BufferedReader reader) throws IOException {
        String userInput = reader.readLine();

        switch (userInput) {
            case "1":
                return Request.OperationType.LEADER;
            case "2":
                return Request.OperationType.NEW;
            case "3":
                return Request.OperationType.QUIT;
            default:
                return null;
        }
    }

    public static void main(String args[]) throws Exception {
        boolean connection = false;
        Socket serverSock = null;
        OutputStream out = null;
        InputStream in = null;
        int i1 = 0, i2 = 0;
        int port = 9099; // default port

        // Make sure two arguments are given
        if (args.length != 2) {
            System.out.println("Expected arguments: <host(String)> <port(int)>");
            System.exit(1);
        }

        String host = args[0];

        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException nfe) {
            System.out.println("[Port] must be an integer");
            System.exit(2);
        }

        // Ask the user for their username
        System.out.println("Please provide your name for the server. ( ͡❛ ͜ʖ ͡❛)");
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        String strToSend = stdin.readLine();

        // Connect to the server and send the name
        try (Socket socket = new Socket(host, port);
             OutputStream outputStream = socket.getOutputStream();
             InputStream inputStream = socket.getInputStream()) {

            serverSock = socket;
            out = outputStream;
            in = inputStream;

            System.out.println("Connected to Server");
            connection = true;

            // Build the request object including the name
            Request op = Request.newBuilder()
                    .setOperationType(Request.OperationType.NAME)
                    .setName(strToSend).build();

            op.writeDelimitedTo(out);

            while(connection) {
                // Read the server response
                Response response = Response.parseDelimitedFrom(in);

                if (response != null) {
                    switch (response.getResponseType()) {
                        case GREETING:
                            System.out.println(response.getMessage());

                            Request.OperationType operationType = getUserInputOperationType(stdin);

                            Request optionResponse = Request.newBuilder()
                                    .setOperationType(operationType).build();
                            optionResponse.writeDelimitedTo(out);
                            break;

                        case TASK:

                            if(response.hasMessage()){
                                System.out.println(response.getMessage());
                            }
                            else{
                                System.out.println("Task: " + response.getResponseType());
                                System.out.println("Image:\n" + response.getImage());
                                System.out.println("Task:\n" + response.getTask());
                            }
                            // On the client side you would receive a Response object which

                            String answer = stdin.readLine();

                            Request answerRESPONSE = Request.newBuilder()
                                    .setOperationType(Request.OperationType.ANSWER)
                                    .setAnswer(answer).build();
                            answerRESPONSE.writeDelimitedTo(out);
                            break;

                        case LEADER:
                            // Handle LEADER response
                            List<Entry> leaderList = response.getLeaderList();
                            System.out.println("Leaderboard:");
                            System.out.println(response.getMessage());
                            break;

                        case WON:
                            System.out.println(response.getMessage());
                            System.out.println(response.getImage());
                            System.out.println();
                            System.out.println("What would you like to do?\n1 - to see the leaderboard\n2 - to enter a game\n3 - to quit the game");

                            Request.OperationType operationType2 = getUserInputOperationType(stdin);

                            Request optionResponse2 = Request.newBuilder()
                                    .setOperationType(operationType2).build();
                            optionResponse2.writeDelimitedTo(out);
                            break;

                        case ERROR:
                            // Handle ERROR response
                            break;

                        case BYE:
                            System.out.println(response.getMessage());
                            connection = false; // Set connection flag to false to exit the loop
                            break;

                        default:
                            System.out.println("Unknown response type received");
                            break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


