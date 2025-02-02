import org.json.JSONArray;
import org.json.JSONObject;

import java.net.*;
import java.io.*;
import java.util.Scanner;

/**
 */
class SockClient {
  static Socket sock = null;
  static String host = "localhost";
  static int port = 8888;
  static OutputStream out;
  // Using and Object Stream here and a Data Stream as return. Could both be the same type I just wanted
  // to show the difference. Do not change these types.
  static ObjectOutputStream os;
  static DataInputStream in;
  public static void main (String args[]) {

    if (args.length != 2) {
      System.out.println("Expected arguments: <host(String)> <port(int)>");
      System.exit(1);
    }

    try {
      host = args[0];
      port = Integer.parseInt(args[1]);
    } catch (NumberFormatException nfe) {
      System.out.println("[Port|sleepDelay] must be an integer");
      System.exit(2);
    }

    try {
      connect(host, port); // connecting to server

      System.out.println("Client connected to server. What would you like to do: 1 - echo, 2 - add, 3 - addmany, 4 - concat, 5 - names");
      Scanner scanner = new Scanner(System.in);
      int choice = Integer.parseInt(scanner.nextLine());

      // You can assume the user put in a correct input, you do not need to handle errors here

      // You can assume the user inputs a String when asked and an int when asked. So you do not have to handle user input checking
      JSONObject json = new JSONObject(); // request object
      switch(choice) {
        case 1:
          System.out.println("Choose echo, which String do you want to send?");
          String message = scanner.nextLine();
          json.put("type", "echo");
          json.put("data", message);
          break;
        case 2:
          System.out.println("Choose add, enter first number:");
          String num1 = scanner.nextLine();
          json.put("type", "add");
          json.put("num1", num1);

          System.out.println("Enter second number:");
          String num2 = scanner.nextLine();
          json.put("num2", num2);
          break;
        case 3:
          System.out.println("Choose addmany, enter as many numbers as you like, when done choose 0:");
          JSONArray array = new JSONArray();
          String num = "1";
          while (!num.equals("0")) {
            num = scanner.nextLine();
            array.put(num);
            System.out.println("Got your " + num);
          }
          json.put("type", "addmany");
          json.put("nums", array);
          break;
        case 4:
          System.out.println("You have selected concatenation, enter the first string being greater than 5 characters");
          String s1 = scanner.nextLine();
          System.out.println();
          System.out.println("Now enter the second string that is greater than 5 characters");
          String s2 = scanner.nextLine();

          json.put("type", "concat");
          json.put("s1", s1);
          json.put("s2", s2);
          break;
        case 5:
          System.out.println("What is the name you would like to add?");
          System.out.println("First name only can be take up, so add a last name or last initial");
          String name = scanner.nextLine();

          json.put("type", "names");
          json.put("name", name);
      }
      // write the whole message
      os.writeObject(json.toString());
      // make sure it wrote and doesn't get cached in a buffer
      os.flush();

      // handle the response
      String i = (String) in.readUTF();
      JSONObject res = new JSONObject(i);
      System.out.println("Got response: " + res);
      if (res.getBoolean("ok")){
        if (res.getString("type").equals("echo")) {
          System.out.println(res.getString("echo"));
        } else {
          if (res.has("result")) {
            Object result = res.get("result");

            if (result instanceof Integer) {
              System.out.println(res.getInt("result"));
            } else if (result instanceof String) {
              System.out.println(res.getString("result"));
            } else {
              System.out.println("Unsupported data type for 'result' key");
            }
          }
        }
      } else {
        System.out.println(res.getString("message"));
      }

      overandout();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void overandout() throws IOException {
    //closing things, could
    in.close();
    os.close();
    sock.close(); // close socked after sending
  }

  public static void connect(String host, int port) throws IOException {
    // open the connection
    sock = new Socket(host, port); // connect to host and socket on port 8888

    // get output channel
    out = sock.getOutputStream();

    // create an object output writer (Java only)
    os = new ObjectOutputStream(out);

    in = new DataInputStream(sock.getInputStream());
  }
}