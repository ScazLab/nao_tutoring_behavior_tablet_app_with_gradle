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

    public Question (JSONObject question){
        try {
            // these are the keys that should be in the json giving questions
            questionID = question.getInt(Questions.KEY_QUESTION_ID);
            denominator = question.getString(Questions.KEY_DENOMINATOR);
            numerator = question.getString(Questions.KEY_NUMERATOR);
            answerText = question.getString(Questions.KEY_ANSWER);
            spokenQuestion = question.getString(Questions.KEY_SPOKEN_QUESTION);
            wordProblem = question.getBoolean(Questions.KEY_WORD_PROBLEM); // this string is empty unless the problem is a word problem

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}