package tutoringproject.behaviors;

import org.json.JSONObject;

/**
 * Created by Aleksandra Zakrzewska on 9/15/17.
 */

public class Question {
    public int questionID;
    public String denominator;
    public String  numerator;
    public String answerText;
    public String spokenQuestion;
    public boolean wordProblem;
    //public boolean zerosProblem;

    public Question (JSONObject question){
        try {
            // these are the keys that should be in the json giving questions
            questionID = question.getInt(Questions.KEY_QUESTION_ID);
            denominator = question.getString(Questions.KEY_DENOMINATOR);
            numerator = question.getString(Questions.KEY_NUMERATOR);
            answerText = question.getString(Questions.KEY_ANSWER);
            spokenQuestion = question.getString(Questions.KEY_SPOKEN_QUESTION);
            wordProblem = question.getBoolean(Questions.KEY_WORD_PROBLEM); // this is a boolean indicating whether this problem is a word problem
            //zerosProblem = question.getBoolean(Questions.KEY_ZEROS_PROBLEM); // boolean for L5 questions indicating one question type

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}