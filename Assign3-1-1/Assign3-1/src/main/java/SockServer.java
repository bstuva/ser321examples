import org.json.JSONArray;
import org.json.JSONObject;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A class to demonstrate a simple client-server connection using sockets.
 *
 */
public class SockServer {
  static Socket sock;
  static DataOutputStream os;
  static ObjectInputStream in;
  static int port = 8888;
  public static List<String> knownNames = new ArrayList<>();

  public static void main (String args[]) {

    if (args.length != 1) {
      System.out.println("Expected arguments: <port(int)>");
      System.exit(1);
    }

    try {
      port = Integer.parseInt(args[0]);
    } catch (NumberFormatException nfe) {
      System.out.println("[Port|sleepDelay] must be an integer");
      System.exit(2);
    }

    try {
      //open socket
      ServerSocket serv = new ServerSocket(8888); // create server socket on port 8888
      System.out.println("Server ready for connections");

      /**
       * Simple loop accepting one client and calling handling one request.
       *
       */

      while (true){
        System.out.println("Server waiting for a connection");
        sock = serv.accept(); // blocking wait

        // setup the object reading channel
        in = new ObjectInputStream(sock.getInputStream());

        // get output channel
        OutputStream out = sock.getOutputStream();

        // create an object output writer (Java only)
        os = new DataOutputStream(out);

        String s = (String) in.readObject();
        JSONObject req = new JSONObject(s);

        JSONObject res = testField(req, "type");
        if (!res.getBoolean("ok")) {
          overandout(res);
          continue;
        }

        // check which request it is (could also be a switch statement)
        if (req.getString("type").equals("echo")) {
          res = echo(req);
        } else if (req.getString("type").equals("add")) {
          res = add(req);
        } else if (req.getString("type").equals("addmany")) {
          res = addmany(req);
        } else if (req.getString("type").equals("concat")) {
          res = concatenate(req);
        } else if (req.getString("type").equals("names")){
          res = names(req);
        } else {
          res = wrongType(req);
        }
        overandout(res);
      }
    } catch(Exception e) {e.printStackTrace();}
  }


  /**
   * Checks if a specific field exists
   *
   */
  static JSONObject testField(JSONObject req, String key){
    JSONObject res = new JSONObject();

    // field does not exist
    if (!req.has(key)){
      res.put("ok", false);
      res.put("message", "Field " + key + " does not exist in request");
      return res;
    }
    return res.put("ok", true);
  }

  // handles the simple echo request
  static JSONObject echo(JSONObject req){
    JSONObject res = testField(req, "data");
    System.out.println(res);
    if (res.getBoolean("ok")) {
      if (!req.get("data").getClass().getName().equals("java.lang.String")){
        res.put("ok", false);
        res.put("message", "Field data needs to be of type: String");
        return res;
      }

      res.put("type", "echo");
      res.put("echo", "Here is your echo: " + req.getString("data"));
    }
    return res;
  }

  // handles the simple add request with two numbers
  static JSONObject add(JSONObject req){
    JSONObject res1 = testField(req, "num1");
    if (!res1.getBoolean("ok")) {
      return res1;
    }

    JSONObject res2 = testField(req, "num2");
    if (!res2.getBoolean("ok")) {
      return res2;
    }

    JSONObject res = new JSONObject();
    res.put("ok", true);
    res.put("type", "add");
    try {
    res.put("result", req.getInt("num1") + req.getInt("num2"));
    } catch (org.json.JSONException e){
      res.put("ok", false);
      res.put("message", "Field data needs to be of type: int");
    }
    return res;
  }

  // implement me in assignment 3
  static JSONObject concatenate(JSONObject req) {

    try {
      String s1 = req.getString("s1");
      String s2 = req.getString("s2");

      if (s1.length() < 5 || s2.length() < 5) {

        JSONObject res = new JSONObject();

        res.put("type", "concat");
        res.put("ok", false);
        res.put("message", "too short");

        return res;
      }

      String concatString = s1.concat(s2);

      JSONObject successfulResponse = new JSONObject();

      successfulResponse.put("type", "concat");
      successfulResponse.put("ok", true);
      successfulResponse.put("result", concatString);

      return successfulResponse;
    }catch(Exception e){

      JSONObject errorResponse = new JSONObject();
      errorResponse.put("type", "concat");
      errorResponse.put("ok", false);
      errorResponse.put("message", "An error occurred");
      return errorResponse;

    }
  }

  // implement me in assignment 3
  static JSONObject names(JSONObject req) {
    JSONArray allNames = new JSONArray();

    JSONObject requestResponse = new JSONObject();

    String name = req.getString("name");

    try {
      if (name != null && !name.isEmpty()) {
        if (nameExists(name)) {
            requestResponse.put("type", "names");
            requestResponse.put("ok", false);
            requestResponse.put("message", "already used");
            requestResponse.put("allNames", allNames);
        } else {
            addName(name);
            requestResponse.put("type", "names");
            requestResponse.put("ok", true);
            requestResponse.put("result", name);
            requestResponse.put("allNames", getAllNames());
        }
      } else {
        if (isListEmpty()) {
            requestResponse.put("type", "names");
            requestResponse.put("ok", false);
            requestResponse.put("message", "list empty");
        } else {
            requestResponse.put("type", "names");
            requestResponse.put("ok", true);
            requestResponse.put("allNames", getAllNames());
        }
      }
    }
    catch (Exception e){
      requestResponse.put("ok", false);
      requestResponse.put("message", "An error occurred: " + e.getMessage());
    }
    return requestResponse;
  }

  private static boolean nameExists(String name) {

    // Check if the name exists in the list
    for (String knownName : knownNames) {

      if (knownName.equals(name)) {

        return true; // Name already exists
      }
    }
    return false; // Name does not exist
  }
  private static void addName(String name) {

    // Add the name to the list
    knownNames.add(name);
  }
  private static JSONArray getAllNames() {

    // Create a JSON array to hold all the names
    JSONArray allNamesArray = new JSONArray();

    // Add each name to the JSON array
    for (String name : knownNames) {
      allNamesArray.put(name);
    }

    return allNamesArray;
  }
  private static boolean isListEmpty() {
    // Check if the list is empty
    return knownNames.isEmpty();
  }

  // handles the simple addmany request
  static JSONObject addmany(JSONObject req){
    JSONObject res = testField(req, "nums");
    if (!res.getBoolean("ok")) {
      return res;
    }

    int result = 0;
    JSONArray array = req.getJSONArray("nums");
    for (int i = 0; i < array.length(); i ++){
      try{
        result += array.getInt(i);
      } catch (org.json.JSONException e){
        res.put("ok", false);
        res.put("message", "Values in array need to be ints");
        return res;
      }
    }

    res.put("ok", true);
    res.put("type", "addmany");
    res.put("result", result);
    return res;
  }

  // creates the error message for wrong type
  static JSONObject wrongType(JSONObject req){
    JSONObject res = new JSONObject();
    res.put("ok", false);
    res.put("message", "Type " + req.getString("type") + " is not supported.");
    return res;
  }

  // sends the response and closes the connection between client and server.
  static void overandout(JSONObject res) {
    try {
      os.writeUTF(res.toString());
      // make sure it wrote and doesn't get cached in a buffer
      os.flush();

      os.close();
      in.close();
      sock.close();
    } catch(Exception e) {e.printStackTrace();}

  }
}