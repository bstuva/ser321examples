package server;

import java.util.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Class: Game 
 * Description: Game class that can load an ascii image
 * Class can be used to hold the persistent state for a game for different threads
 * synchronization is not taken care of .
 * You can change this Class in any way you like or decide to not use it at all
 * I used this class in my SockBaseServer to create a new game and keep track of the current image evenon differnt threads. 
 * My threads each get a reference to this Game
 */

public class Game {
    private int idx = 0; // current index where x could be replaced with original
    private int idxMax; // max index of image
    private char[][] original; // the original image
    private char[][] hidden; // the hidden image
    private int col; // columns in original, approx
    private int row; // rows in original and hidden
    private boolean won; // if the game is won or not
    private List<String> files = new ArrayList<String>(); // list of files, each file has one image
    public String currentFile = null;
    private Map<String, List<String>> imageQuestions = null;

    private int x = 0;


    public Game() {
        // you can of course add more or change this setup completely. You are totally free to also use just Strings in your Server class instead of this class
        won = true; // setting it to true, since then in newGame() a new image will be created
        files.add("pig.txt");
        files.add("snail.txt");
        files.add("duck.txt");
        files.add("crab.txt");
        files.add("cat.txt");
//        files.add("joke1.txt");
//        files.add("joke2.txt");
//        files.add("joke3.txt");
        imageQuestions = createImageQuestions();
    }

    /**
     * Sets the won flag to true
     *
     * @param args Unused.
     * @return Nothing.
     */
    public void setWon() {
        won = true;
    }

    /**
     * Method loads in a new image from the specified files and creates the hidden image for it.
     *
     * @return Nothing.
     */
    public void newGame(){

        int randInt = 0;

        if (won) {
            idx = 0;
            won = false;
            List<String> rows = new ArrayList<String>();

            try{
                // loads one random image from list
                Random rand = new Random();
                col = 0;
                randInt = rand.nextInt(files.size());
                File file = new File(
                        Game.class.getResource("/"+files.get(randInt)).getFile()
                );
                currentFile = files.get(randInt);
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                while ((line = br.readLine()) != null) {
                    if (col < line.length()) {
                        col = line.length();
                    }
                    rows.add(line);
                }
            }
            catch (Exception e){
                System.out.println("File load error"); // extremely simple error handling, you can do better if you like.
            }

            // this handles creating the orinal array and the hidden array in the correct size
            String[] rowsASCII = rows.toArray(new String[0]);

            row = rowsASCII.length;

            // Generate original array by splitting each row in the original array.
            original = new char[row][col];
            for(int i = 0; i < row; i++) {
                char[] splitRow = rowsASCII[i].toCharArray();
                for (int j = 0; j < splitRow.length; j++) {
                    original[i][j] = splitRow[j];
                }
            }

            // Generate Hidden array with X's (this is the minimal size for columns)
            hidden = new char[row][col];
            for(int i = 0; i < row; i++){
                for(int j = 0; j < col; j++){
                    hidden[i][j] = 'X';
                }
            }
            setIdxMax(col * row);
        }
        else {
        }
    }

    /**
     * Method returns the String of the current hidden image
     * @return String of the current hidden image
     */
    public String getImage(){
        StringBuilder sb = new StringBuilder();
        for (char[] subArray : hidden) {
            sb.append(subArray);
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Method changes the next idx of the hidden image to the character in the original image
     * You can change this method if you want to turn more than one x to the original
     * @return String of the current hidden image
     */
//    public String replaceOneCharacter() {
//        int colNumber = idx%col;
//        int rowNumber = idx/col;
//        hidden[rowNumber][colNumber] = original[rowNumber][colNumber];
//        idx++;
//        return(getImage());
//    }

    public String revealPicture() {
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                hidden[i][j] = original[i][j];
            }
        }
        idx = row * col; // Set idx to the maximum index to indicate that all characters have been revealed
        return getImage();
    }

    public int getIdxMax() {
        return idxMax;
    }
    public void setIdxMax(int idxMax) {
        this.idxMax = idxMax;
    }
    public int getIdx() {
        return idx;
    }
    public void setIdx(int idx) {
        this.idx = idx;
    }

    public String askQuestion() {
        List<String> questions = imageQuestions.getOrDefault(currentFile, new ArrayList<>());

        String question;
        if (!questions.isEmpty() && x < questions.size()) {
            question = questions.get(x);
            x++;
        } else {
            question = "Unknown question for file: " + currentFile;
        }
        return question;
    }
    public boolean answerCheck(String clientAnswer) {
        String cutFileName = currentFile.replace(".txt", "");

        System.out.println(cutFileName);

        return clientAnswer.equals(cutFileName);
    }
    private Map<String, List<String>> createImageQuestions() {
        Map<String, List<String>> map = new HashMap<>();
        // Add image-file to question-list mappings
        map.put("pig.txt", createPigQuestions());
        map.put("snail.txt", createSnailQuestions());
        map.put("duck.txt", createDuckQuestions());
        map.put("crab.txt", createCrabQuestions());
        map.put("cat.txt", createCatQuestions());
        map.put("joke1.txt", createJokeQuestions());
        map.put("joke2.txt", createJokeQuestions());
        map.put("joke3.txt", createJokeQuestions());
        return map;
    }
    private List<String> createPigQuestions() {
        List<String> questions = new ArrayList<>();
        // Add pig-related questions to the list
        questions.add("What animal has a snout and rolls in mud?");
        questions.add("Which creature is associated with the saying 'as happy as a ___'?");
        questions.add("What animal symbolizes greed in some cultures?");
        questions.add("What creature is known for its intelligence and being pink?");
        questions.add("Which animal is often depicted as being messy?");
        questions.add("What is the name of the animal that says 'oink'?");
        questions.add("What animal's meat is commonly called 'pork'?");
        return questions;
    }
    private List<String> createSnailQuestions() {
        List<String> questions = new ArrayList<>();
        // Add snail-related questions to the list
        questions.add("What creature has a shell and moves very slowly?");
        questions.add("Which animal leaves behind a trail of slime as it moves?");
        questions.add("What is the name of the animal that withdraws into its shell when threatened?");
        questions.add("Which creature is known for its slow and leisurely pace?");
        questions.add("What animal is often used as a metaphor for someone who is slow?");
        questions.add("What is the name of the mollusk that moves on a single muscular foot?");
        questions.add("Which creature is associated with the saying 'slow and steady wins the race'?");

        return questions;
    }
    private List<String> createDuckQuestions() {
        List<String> questions = new ArrayList<>();
        // Add duck-related questions to the list
        questions.add("What bird has webbed feet and quacks?");
        questions.add("Which animal can swim, fly, and walk on land?");
        questions.add("What is the name of the bird that says 'quack'?");
        questions.add("Which creature is associated with the saying 'like water off a ___'?");
        questions.add("What animal is commonly depicted as a rubber bath toy?");
        questions.add("What is the name of the bird that appears in various children's stories?");
        questions.add("What animal's meat is sometimes called 'poultry'?");
        return questions;
    }
    private List<String> createCrabQuestions() {
        List<String> questions = new ArrayList<>();
        // Add crab-related questions to the list
        questions.add("Which creature has a hard exoskeleton and scuttles sideways?");
        questions.add("What animal is known for its pincers and walking in a zigzag pattern?");
        questions.add("What is the name of the crustacean that lives near the shore and burrows in the sand?");
        questions.add("Which creature is associated with the saying 'crabby' when someone is in a bad mood?");
        questions.add("What animal is often depicted as moving sideways and inhabiting seashores?");
        questions.add("What is the name of the animal that has ten legs and can regenerate its limbs?");
        questions.add("Which creature is commonly featured in seafood dishes like crab cakes?");
        return questions;
    }
    private List<String> createCatQuestions() {
        List<String> questions = new ArrayList<>();
        // Add cat-related questions to the list
        questions.add("What animal is known for its independent and aloof nature?");
        questions.add("Which creature is associated with the saying 'curiosity killed the ___'?");
        questions.add("What is the name of the animal that purrs and meows?");
        questions.add("Which animal is often depicted as being graceful and agile?");
        questions.add("What is the name of the creature that is often kept as a pet and chases mice?");
        questions.add("Which animal is known for its ability to land on its feet when it falls?");
        questions.add("What animal's meat is sometimes called 'feline'?");
        return questions;
    }
    private List<String> createJokeQuestions() {
        List<String> questions = new ArrayList<>();
        questions.add("Why did the chicken cross the road?");
        // Add more joke questions if desired
        return questions;
    }


}
