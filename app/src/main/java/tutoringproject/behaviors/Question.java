package tutoringproject.behaviors;

import org.json.JSONObject;

/**
 * Created by Aleksandra Zakrzewska on 9/15/17.
 */

public class Question {
    public int questionID;
    public String denominator;
    public String  numerator;
    public int answerText;
    public String wordProblem;

    public Question (JSONObject question){
        try {
            // these are the keys that should be in the json giveing questions
            questionID = question.getInt(Questions.KEY_QUESTION_ID);
            denominator = question.getString(Questions.KEY_DENOMINATOR);
            numerator = question.getString(Questions.KEY_NUMERATOR);
            answerText = question.getInt(Questions.KEY_ANSWER);
            wordProblem = question.getString(Questions.KEY_WORD_PROBLEM); // this string is empty unless the problem is a word problem

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}