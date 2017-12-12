package tutoringproject.behaviors;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

//import org.json.simple.JSONValue;
//import org.json.simple.parser.JSONParser;

/**
 * Created by alexlitoiu on 6/4/2015.
 */
public class Questions {

    // define constants for all of the keys

    public static final String KEY_QUESTION_ID = "QuestionID";
    public static final String KEY_QUESTION = "Question";
    public static final String KEY_WORD_PROBLEM = "WordProblem";
    public static final String KEY_ANSWER = "Answer";
    public static final String KEY_NUMERATOR = "Numerator";
    public static final String KEY_DENOMINATOR = "Denominator";

    ArrayList<Question> questions = new ArrayList<Question>();

    public Questions(String contents) {
        try {
            JSONArray jsonQuestions = new JSONArray(contents);

            for (int i = 0; i < jsonQuestions.length(); i++) {
                JSONObject q = jsonQuestions.getJSONObject(i);

                Question newQuestion = new Question(q);
                questions.add(newQuestion);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int length(){
        return questions.size();
    }

    public Question get(int i){
        return questions.get(i);
    }
}
