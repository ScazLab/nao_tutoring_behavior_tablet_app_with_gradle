package tutoringproject.behaviors;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Aleksandra Zakrzewska on 9/16/17.
 */

public class MathControl {

    public static String QUESTIONMESSAGE = "QUESTION";
    public static String STARTTICTACTOE = "TICTACTOE";
    public static String SHOWTEXTHINT = "SHOWTEXTHINT";
    public static String SHOWEXAMPLE = "SHOWEXAMPLE";
    public static String SHOWLESSON = "SHOWLESSON";
    public static String SHOWQUESTION ="QUESTION";
    public static String SHOWSTRUCTURE = "SHOWSTRUCTURE";
    public static String SHOWSTRUCTUREFORTUTORIAL = "SHOWSTRUCTURE-TUTORIAL";
    public static String SHOWEASYTUTORIAL = "SHOWEASYTUTORIAL";
//    public static String SHOWHARDTUTORIAL = "SHOWHARDTUTORIAL";
    public static String FILLSTRUCTURE = "FILLSTRUCTURE";
    public static int TICTACTOEREQUEST = 1;
    public static int EXAMPLEREQUEST = 1;
    public static int MAX_NUM_DIGITS = 6;
    public static int NUM_DIGITS_FOR_STRUCTURE = 1;

    private Questions questions;
    private Questions level1_questions;
    private Questions level2_questions;
    private Questions level3_questions;
    private Questions level4_questions;
    private Questions level5_questions;
    private Context myContext;

    public MathControl(Context context) {

        myContext = context;
        //Load JSON file
        String json_file1 = "level1.json";
        String json_file2 = "level2.json";
        String json_file3 = "level3.json";
        String json_file4 = "level4.json";
        String json_file5 = "level5.json";


        String json1 = "";
        String json2 = "";
        String json3 = "";
        String json4 = "";
        String json5 = "";

        try {
            json1 = AssetJSONFile(json_file1);
            json2 = AssetJSONFile(json_file2);
            json3 = AssetJSONFile(json_file3);
            json4 = AssetJSONFile(json_file4);
            json5 = AssetJSONFile(json_file5);

        } catch (IOException e) {
            e.printStackTrace();
        }
        level1_questions = new Questions(json1);
        level2_questions = new Questions(json2);
        level3_questions = new Questions(json3);
        level4_questions = new Questions(json4);
        level5_questions = new Questions(json5);
        questions = level1_questions; //default start at level 1
    }

    public String AssetJSONFile (String filename) throws IOException {
        AssetManager manager = myContext.getAssets();
        InputStream file = manager.open(filename);
        byte[] formArray = new byte[file.available()];
        file.read(formArray);
        file.close();

        return new String(formArray);
    }

    public Question getQuestion(String level, String question) {
        return getQuestion(Integer.parseInt(level), Integer.parseInt(question));
    }

    public Question getQuestion(int level, int question) {
        if (level == 1) {
            return level1_questions.get(question);
        } else if (level == 2) {
            return level2_questions.get(question);
        }
        else if (level == 3) {
            return level3_questions.get(question);
        }
        else if (level == 4) {
            return level4_questions.get(question);
        }
        else if (level == 5) {
            return level5_questions.get(question);
        }
        else {
            return null;
        }
    }


}
