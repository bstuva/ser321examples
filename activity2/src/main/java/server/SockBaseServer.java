package server;

import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.*;
import client.Player;

import buffers.RequestProtos.Request;
import buffers.RequestProtos.Logs;
import buffers.RequestProtos.Message;
import buffers.ResponseProtos.Response;
import org.json.JSONArray;
import org.json.JSONObject;

class SockBaseServer {
    static String logFilename = "logs.txt";

    ServerSocket serv = null;
    InputStream in = null;
    OutputStream out = null;
    Socket clientSocket = null;
    int port = 9099; // default port
    Game game;
    boolean connection = false;
    LeaderboardManager leaderboardManager = new LeaderboardManager();

    public SockBaseServer(Socket sock, Game game){
        this.clientSocket = sock;
        this.game = game;
        this.connection = true;
        try {
            in = clientSocket.getInputStream();
            out = clientSocket.getOutputStream();
        } catch (Exception e){
            System.out.println("Error in constructor: " + e);
        }
    }

    // Handles the communication right now it just accepts one input and then is done you should make sure the server stays open
    // can handle multiple requests and does not crash when the server crashes
    // you can use this server as based or start a new one if you prefer. 
    public void start() throws IOException {
        String name = "";
        Response response = null;
        System.out.println("Ready...");
        try {
            while(connection) {
                // read the proto object and put into new objct
                Request op = Request.parseDelimitedFrom(in);
                String result = null;

                switch(op.getOperationType()){

                    // if the operation is NAME (so the beginning then say there is a commention and greet the client)
                    case NAME:
                        // get name from proto object
                        name = op.getName();

                        Player player = new Player(name, 0);
                        leaderboardManager.addPlayer(player);


                        // writing a connect message to the log with name and CONNENCT
                        writeToLog(name, Message.CONNECT);
                        System.out.println("Got a connection and a name: " + name);
                        response = Response.newBuilder()
                                .setResponseType(Response.ResponseType.GREETING)
                                .setMessage("Hello " + name + " and welcome. \nWhat would you like to do? \n 1 - to see the leader board \n 2 - to enter a game \n 3 - to quit the game")
                                .build();
                        response.writeDelimitedTo(out);
                        break;
                    case LEADER:
                        Response leaderboardResponseBuilder = Response.newBuilder()
                                .setResponseType(Response.ResponseType.LEADER)
                                .setMessage(printLeaderboard())
                                .build();
                        leaderboardResponseBuilder.writeDelimitedTo(out);
                        break;
                    case ANSWER:
                        String clientAnswer = op.getAnswer().trim();

                        if (game.answerCheck(clientAnswer)) {
                            Response winningAnswer = Response.newBuilder()
                                    .setResponseType(Response.ResponseType.WON)
                                    .setMessage("Correct answer! Well done.")
                                    .setImage(game.revealPicture())
                                    .build();
                            winningAnswer.writeDelimitedTo(out);
                            player = leaderboardManager.getPlayerByName(name);
                            player.incrementWins();

                        } else {
                            Response failAnswer = Response.newBuilder()
                                    .setResponseType(Response.ResponseType.TASK)
                                    .setMessage("Wrong answer. Try again.")
                                    .build();
                            failAnswer.writeDelimitedTo(out);

                            Response newQuestion = Response.newBuilder()
                                    .setResponseType(Response.ResponseType.TASK)
                                    .setImage(game.getImage())
                                    .setTask(game.askQuestion())
                                    .build();
                            newQuestion.writeDelimitedTo(out);
                        }
                        break;
                    case NEW:
                        game.newGame(); // starting a new game

                        Response newGame = Response.newBuilder()
                                .setResponseType(Response.ResponseType.TASK)
                                .setImage(game.getImage())
                                .setTask(game.askQuestion())
                                .build();
                        newGame.writeDelimitedTo(out);
                        break;
                    // Build and send a response to confirm quitting
                    case QUIT:
                        System.out.println(name + " has requested to quit the game.");
                        response = Response.newBuilder()
                                .setResponseType(Response.ResponseType.BYE)
                                .setMessage("Thank you for playing. Goodbye, " + name + "!")
                                .build();
                        response.writeDelimitedTo(out);
                        connection = false; // Set connection flag to false to exit the while loop
                        break;
                    default:

                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (out != null)  out.close();
            if (in != null)   in.close();
            if (clientSocket != null) clientSocket.close();
        }
    }

    /**
     * Replaces num characters in the image. I used it to turn more than one x when the task is fulfilled
     * @param num -- number of x to be turned
     * @return String of the new hidden image
     */
    public String replace(int num){
        for (int i = 0; i < num; i++){
            if (game.getIdx()< game.getIdxMax())
                game.revealPicture();
        }
        return game.getImage();
    }


    /**
     * Writing a new entry to our log
     * @param name - Name of the person logging in
     * @param message - type Message from Protobuf which is the message to be written in the log (e.g. Connect) 
     * @return String of the new hidden image
     */
    public static void writeToLog(String name, Message message){
        try {
            // read old log file 
            Logs.Builder logs = readLogFile();

            // get current time and data
            Date date = java.util.Calendar.getInstance().getTime();

            // we are writing a new log entry to our log
            // add a new log entry to the log list of the Protobuf object
            logs.addLog(date.toString() + ": " +  name + " - " + message);

            // open log file
            FileOutputStream output = new FileOutputStream(logFilename);
            Logs logsObj = logs.build();

            // This is only to show how you can iterate through a Logs object which is a protobuf object
            // which has a repeated field "log"

            for (String log: logsObj.getLogList()){

                System.out.println(log);
            }

            // write to log file
            logsObj.writeTo(output);
        }catch(Exception e){
            System.out.println("Issue while trying to save");
        }
    }

    /**
     * Reading the current log file
     * @return Logs.Builder a builder of a logs entry from protobuf
     */
    public static Logs.Builder readLogFile() throws Exception{
        Logs.Builder logs = Logs.newBuilder();

        try {
            // just read the file and put what is in it into the logs object
            return logs.mergeFrom(new FileInputStream(logFilename));
        } catch (FileNotFoundException e) {
            System.out.println(logFilename + ": File not found.  Creating a new file.");
            return logs;
        }
    }

    private void saveLeaderboard() {
        try {
            JSONArray leaderboardArray = new JSONArray();

            for (Player player : leaderboardManager.getLeaderboard()) {
                JSONObject playerObject = new JSONObject();
                playerObject.put("name", player.getName());
                playerObject.put("wins", player.getWins());
                playerObject.put("logins", player.getLogins());

                leaderboardArray.put(playerObject);
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter("leaderboard.json"));
            writer.write(leaderboardArray.toString());
            writer.close();
        } catch (IOException e) {
            System.out.println("Error while writing leaderboard file.");
        }
    }

    private String printLeaderboard() {
        StringBuilder sb = new StringBuilder();
        List<Player> leaderboard = leaderboardManager.getLeaderboard();

        sb.append("Leaderboard:\n");
        for (int i = 0; i < leaderboard.size(); i++) {
            Player player = leaderboard.get(i);
            sb.append(i + 1).append(". ").append(player.getName())
                    .append(" - Wins: ").append(player.getWins())
                    .append(" - Logins: ").append(player.getLogins())
                    .append("\n");
        }

        return sb.toString();
    }


    public static void main (String args[]) throws Exception {
        Game game = new Game();

        if (args.length != 2) {
            System.out.println("Expected arguments: <port(int)> <delay(int)>");
            System.exit(1);
        }
        int port = 9099; // default port
        int sleepDelay = 10000; // default delay
        Socket clientSocket = null;
        ServerSocket serv = null;

        try {
            port = Integer.parseInt(args[0]);
            sleepDelay = Integer.parseInt(args[1]);
        } catch (NumberFormatException nfe) {
            System.out.println("[Port|sleepDelay] must be an integer");
            System.exit(2);
        }
        try {
            serv = new ServerSocket(port);
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(2);
        }
        System.out.println("--Waiting for Connection--");
        clientSocket = serv.accept();
        System.out.println("--Client has Connected--");
        SockBaseServer server = new SockBaseServer(clientSocket, game);
        server.start();

    }
}

