package tutoringproject.behaviors;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.IntegerRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;


import org.w3c.dom.Text;

import java.nio.channels.OverlappingFileLockException;


public class QuestionActivity extends AppCompatActivity implements TCPClientOwner {

    int expGroup = 1; // TODO change

    MathControl mathContol;
    TextView numerator;
    TextView denominator;
    Button submitButton;
    Button nextButton;
    Button checkAnswers;
    NoImeEditText answerText;
    TextView wordProblem;
    TextView resultText;
    Question currentQuestion;
    Question nextQuestion;

    NoImeEditText remainderBox;
    TextView remainderR;

    String nextAction;

    TextView rBoxes[][] = new TextView[10][10];
    String rBoxAnswers[][] = new String[10][10];
    TextView[] answerBoxes = new TextView[7];
    TextView[] structNumBoxes = new TextView[7];
    LinearLayout[][] arrows = new LinearLayout[3][6];

    int showingHint = 0;
    int exampleStep = 0;
    boolean keyboardEnabled = true;


    private KeyboardView mKeyboardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        if (TCPClient.singleton != null)
            TCPClient.singleton.setSessionOwner(this);

        // find and initialize variables that will be used in various functions
        numerator = (TextView) findViewById(R.id.Numerator);
        denominator = (TextView) findViewById(R.id.Denominator);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        int level = extras.getInt("QuestionLevel");
        final int number = extras.getInt("QuestionNumber");

        // these are the possible backgrounds for the answer box
        final Drawable correct_border = ContextCompat.getDrawable(this, R.drawable.answer_correct);
        final Drawable incorrect_border = ContextCompat.getDrawable(this, R.drawable.answer_incorrect);
        final Drawable correct_remainder = ContextCompat.getDrawable(this, R.drawable.remainder_correct); // these are the same backgrounds as for the main answer box
        final Drawable incorrect_remainder = ContextCompat.getDrawable(this, R.drawable.remainder_incorrect); // but using the same drawable file causes the answer to change size. I have no idea why.
        final Drawable nextButtonCorrect = ContextCompat.getDrawable(this, R.drawable.answer_correct_border_focused);

        Keyboard mKeyboard = new Keyboard(getApplicationContext(), R.xml.numbers_keyboard);
        mKeyboardView = (KeyboardView) findViewById(R.id.keyboardview);
        mKeyboardView.setKeyboard(mKeyboard);
        mKeyboardView.setPreviewEnabled(false);
        mKeyboardView.setOnKeyboardActionListener(new KeyboardView.OnKeyboardActionListener() {
            @Override
            public void onPress(int i) {

            }

            @Override
            public void onRelease(int i) {

            }

            @Override
            public void onKey(int primaryCode, int[] keyCodes) {
                if (keyboardEnabled) {
                    //Here check the primaryCode to see which key is pressed
                    //based on the android:codes property
                    EditText target = (EditText) getWindow().getCurrentFocus();

                    if (primaryCode >= 0 && primaryCode <= 9) {
                        if (target.getText().toString().length() < MathControl.MAX_NUM_DIGITS)
                            target.setText(target.getText().toString() + primaryCode + "");
                    } else if (primaryCode == -1) {
                        if (target.getText().toString().length() > 0) {
                            String old_string = target.getText().toString();
                            int string_length = old_string.length();

                            String new_string = old_string.substring(0, string_length - 1);

                            target.setText(new_string);
                        }
                    }
                }
            }

            @Override
            public void onText(CharSequence charSequence) {

            }

            @Override
            public void swipeLeft() {

            }

            @Override
            public void swipeRight() {

            }

            @Override
            public void swipeDown() {

            }

            @Override
            public void swipeUp() {

            }
        });

        // get the first question and display it
        mathContol = new MathControl(this);
        if (currentQuestion == null)
            currentQuestion = mathContol.getQuestion(level, number);

        numerator.setText(currentQuestion.numerator.replace("", "   ").trim());
        denominator.setText(currentQuestion.denominator);

        TCPClient.singleton.sendMessage("SHOWING-QUESTION");

        // these are views in the main question pane
        submitButton = (Button) findViewById(R.id.submitButton);
        nextButton = (Button) findViewById(R.id.nextButton);
        answerText = (NoImeEditText) findViewById(R.id.answer); // box for user to input answer
        resultText = (TextView) findViewById(R.id.MathAnswer);  // tells you if correct or incorrect
        remainderBox = (NoImeEditText) findViewById(R.id.Remainder); // this and the R appear only if there
        remainderR = (TextView) findViewById(R.id.R);           // is a remainder
        wordProblem = (TextView) findViewById(R.id.WordQuestion);

        for (int i = 0; i < 7; i++){
            for (int j=0; j< 4 ; j++) {
                rBoxes[i][j] = null;
                rBoxAnswers[i][j] = "0";
            }
        }

        for (int i = 0; i < 2; i++){
            for (int j=0; j< 6 ; j++) {
                arrows[i][j] = null;
            }
        }

        rBoxes[1][1] = (NoImeEditText) findViewById(R.id.R11);           // initialize and find the rBoxes
        rBoxes[1][2] = (NoImeEditText) findViewById(R.id.R12);
        rBoxes[2][1] = (NoImeEditText) findViewById(R.id.R21);           // rBoxes are the boxes that are
        rBoxes[2][2] = (NoImeEditText) findViewById(R.id.R22);           // filled in during the structure
        rBoxes[2][3] = (NoImeEditText) findViewById(R.id.R23);           // tutorial. rBoxAnswers
        rBoxes[3][1] = (NoImeEditText) findViewById(R.id.R31);           // ( initialized above) store the
        rBoxes[3][2] = (NoImeEditText) findViewById(R.id.R32);           //  correct values corresponding
        rBoxes[3][3] = (NoImeEditText) findViewById(R.id.R33);           // to rBoxes. They are used in
        rBoxes[4][2] = (NoImeEditText) findViewById(R.id.R42);           // interactive tutorials to verify
        rBoxes[4][3] = (NoImeEditText) findViewById(R.id.R43);           // answers
        rBoxes[4][4] = (NoImeEditText) findViewById(R.id.R44);
        rBoxes[5][2] = (NoImeEditText) findViewById(R.id.R52);
        rBoxes[5][3] = (NoImeEditText) findViewById(R.id.R53);
        rBoxes[5][4] = (NoImeEditText) findViewById(R.id.R54);
        rBoxes[6][3] = (NoImeEditText) findViewById(R.id.R63);
        rBoxes[6][4] = (NoImeEditText) findViewById(R.id.R64);
        rBoxes[6][5] = (NoImeEditText) findViewById(R.id.R65);
        rBoxes[7][3] = (NoImeEditText) findViewById(R.id.R73);
        rBoxes[7][4] = (NoImeEditText) findViewById(R.id.R74);
        rBoxes[7][5] = (NoImeEditText) findViewById(R.id.R75);
        rBoxes[8][4] = (NoImeEditText) findViewById(R.id.R84);
        rBoxes[8][5] = (NoImeEditText) findViewById(R.id.R85);

        answerBoxes[0] = (NoImeEditText) findViewById(R.id.AnsBox1);     // these are the answer boxes above
        answerBoxes[1] = (NoImeEditText) findViewById(R.id.AnsBox2);     // the division bar in the structure
        answerBoxes[2] = (NoImeEditText) findViewById(R.id.AnsBox3);
        answerBoxes[3] = (NoImeEditText) findViewById(R.id.AnsBox4);
        answerBoxes[4] = (NoImeEditText) findViewById(R.id.AnsBox5);
        answerBoxes[5] = (NoImeEditText) findViewById(R.id.AnsBox6);
        answerBoxes[6] = (NoImeEditText) findViewById(R.id.AnsBox7);

        structNumBoxes[0] = (TextView) findViewById(R.id.NumBox1);   // these are the boxes holding
        structNumBoxes[1] = (TextView) findViewById(R.id.NumBox2);   // digits of the numerator in
        structNumBoxes[2] = (TextView) findViewById(R.id.NumBox3);   // the structure
        structNumBoxes[3] = (TextView) findViewById(R.id.NumBox4);
        structNumBoxes[4] = (TextView) findViewById(R.id.NumBox5);

        arrows[1][2] = (LinearLayout) findViewById(R.id.arrow12);    // having all the boxes and arrows
        arrows[1][3] = (LinearLayout) findViewById(R.id.arrow13);    // preinitialized here allows us to easily
        arrows[1][4] = (LinearLayout) findViewById(R.id.arrow14);    // loop through to add, remove or resize them
        arrows[1][5] = (LinearLayout) findViewById(R.id.arrow15);

        arrows[2][3] = (LinearLayout) findViewById(R.id.arrow23);
        arrows[2][4] = (LinearLayout) findViewById(R.id.arrow24);
        arrows[2][5] = (LinearLayout) findViewById(R.id.arrow25);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // parse the input given as an answer
                if (answerText.getText().length() > 0) {
                    int answer = Integer.parseInt(answerText.getText().toString());
                    int remainder_answer = 0;
                    if (remainderBox.getText().length()> 0 ) {
                        remainder_answer = Integer.parseInt(remainderBox.getText().toString());
                    }

                    if (answer == Integer.parseInt(currentQuestion.numerator) / Integer.parseInt(currentQuestion.denominator)) {
                        int remainder = Integer.parseInt(currentQuestion.numerator) % Integer.parseInt(currentQuestion.denominator);
                        // if both the answer and remainder are correct (0 if there is no remainder) indicate that.
                        // show the next question button, and send a message to the server
                        // also change the border of the answer to green to indicate it is correct
                        if (remainder == 0 || remainder == remainder_answer) {
                            String message = "CA;" + answer + ";";

                            TCPClient.singleton.sendMessage(message);
                            answerText.setBackground(correct_border);
                            remainderBox.setBackground(correct_remainder);
                            nextButton.setBackground(nextButtonCorrect);

                            resultText.setText("That is correct.");
                            resultText.setVisibility(View.VISIBLE);
                            nextButton.setVisibility(View.VISIBLE);
                            submitButton.setVisibility(View.INVISIBLE);
                        } else {
                            // if the remainder is not correct, only change that border, but
                            // still send a message indicating an incorrect answer
                            String message = "IA;" + answer + ";";
                            TCPClient.singleton.sendMessage(message);
                            resultText.setText(remainderBox.getText() + " is incorrect remainder.");
                            answerText.setBackground(correct_border);
                            remainderBox.setBackground(incorrect_remainder);
                            resultText.setVisibility(View.VISIBLE);
                            remainderBox.setText("");
                        }

                    } else {
                        // if the answer is incorrect, indicate so in colors and send a message to
                        // the server saying so
                        String message = "IA;" + answer + ";";
                        TCPClient.singleton.sendMessage(message);
                        resultText.setText(answerText.getText() + " is incorrect.");
                        answerText.setBackground(incorrect_border);
                        remainderBox.setBackground(incorrect_remainder);
                        resultText.setVisibility(View.VISIBLE);
                        answerText.setText("");
                        remainderBox.setText("");
                    }

                    disableButtons();       // disable the buttons even if the robot is not
                                            // talking so the user cannot progress to the
                                            // next question before we know what it is or if a
                                            // tutoring behavior should be given first
                }
            }
        });


        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToNextQuestion();
        }});

        enableButtons();
    }

    public void goToNextQuestion() {
        final Drawable normal_answer = ContextCompat.getDrawable(this, R.drawable.answer_background);
        final Drawable normal_remainder = ContextCompat.getDrawable(this, R.drawable.remainder_background);

        // progress to the next question and display it
        answerText.setText("");         // start by clearing everything
        remainderBox.setText("");
        resultText.setText("");
        resultText.setVisibility(View.INVISIBLE);
        nextButton.setVisibility(View.INVISIBLE);
        submitButton.setVisibility(View.VISIBLE);

        // if the next question has text for being a word question, display that instead
        // of the math notation
        if (nextQuestion.wordProblem != null && !nextQuestion.wordProblem.equals("")) {
            numerator.setVisibility(View.INVISIBLE);
            denominator.setVisibility(View.INVISIBLE);
            ImageView division_bar = (ImageView) findViewById(R.id.divisionBar);
            division_bar.setVisibility(View.INVISIBLE);
            wordProblem.setVisibility(View.VISIBLE);
            wordProblem.setText(nextQuestion.wordProblem);
        } else {
            // otherwise, set the numerator and denominator to show the new numbers
            numerator.setVisibility(View.VISIBLE);
            denominator.setVisibility(View.VISIBLE);
            ImageView division_bar = (ImageView) findViewById(R.id.divisionBar);
            division_bar.setVisibility(View.VISIBLE);
            numerator.setText(nextQuestion.numerator.replace("", " ").trim()); // space out the the digits in numerator
            denominator.setText(nextQuestion.denominator);
            numerator.setVisibility(View.VISIBLE);
            denominator.setVisibility(View.VISIBLE);
            wordProblem.setVisibility(View.INVISIBLE);
            wordProblem.setText("");
        }

        remainderBox.setBackground(normal_remainder);
        answerText.setBackground(normal_answer);    //   make sure the answer background is neutral

        // show the remainder only if the answer has one
        System.out.println("remainder for question " + Integer.toString(Integer.parseInt(nextQuestion.numerator) % Integer.parseInt(nextQuestion.denominator)));
        if (Integer.parseInt(nextQuestion.numerator) % Integer.parseInt(nextQuestion.denominator) != 0) {
            remainderBox.setVisibility(View.VISIBLE);
            remainderBox.setEnabled(true);
            remainderR.setVisibility(View.VISIBLE);
        } else {
            remainderBox.setVisibility(View.INVISIBLE);
            remainderR.setVisibility(View.INVISIBLE);
        }

        clearView(); // clear out any hints, structure or examples from previous question



        currentQuestion = nextQuestion;

        // alert server that we are showing the next question
        TCPClient.singleton.sendMessage("SHOWING-QUESTION");

    }

    public void disableButtons() {
        System.out.println("MATHACTIVITY: IN disableButtons method!");
        submitButton.setEnabled(false);
        nextButton.setEnabled(false);
        keyboardEnabled = false;
        return;
    }

    public void enableButtons() {
        System.out.println("MATHACTIVITY: IN endableButtons method!");
        submitButton.setEnabled(true);
        nextButton.setEnabled(true);
        keyboardEnabled = true;
        return;
    }

    public void showHints(String hintText) {                // displays any text hint in three boxes
        System.out.println("MATHACTIVITY: IN showHints method! " + hintText);

        if (showingHint == 0) {
            clearView();
        }

        hintText.replace('/', '÷');
        hintText.replace('x', '×');

        showingHint = showingHint + 1;

        TextView hint1 = (TextView) findViewById(R.id.Hint1);
        TextView hint2 = (TextView) findViewById(R.id.Hint2);
        TextView hint3 = (TextView) findViewById(R.id.Hint3);

        if (showingHint == 1) {
            hint1.setText(hintText);
            hint1.setVisibility(View.VISIBLE);
        }
        if (showingHint == 2) {
            hint1.setVisibility(View.VISIBLE);
            hint2.setText(hintText);
            hint2.setVisibility(View.VISIBLE);
        }
        if (showingHint == 3) {
            hint1.setVisibility(View.VISIBLE);
            hint2.setVisibility(View.VISIBLE);
            hint3.setText(hintText);
            hint3.setVisibility(View.VISIBLE);
        }
    }

    public void hideHints() {           // hide the views corresponding to hint boxes
            TextView hint1 = (TextView) findViewById(R.id.Hint1);
            TextView hint2 = (TextView) findViewById(R.id.Hint2);
            TextView hint3 = (TextView) findViewById(R.id.Hint3);
            hint1.setVisibility(View.INVISIBLE);
            hint2.setVisibility(View.INVISIBLE);
            hint3.setVisibility(View.INVISIBLE);

            showingHint = 0;
    }

    public void hideEasyTutorial() {    // hide the views corresponding to an easy tutorial
        LinearLayout boxLayout = (LinearLayout) findViewById(R.id.easyExampleBoxes);
        boxLayout.removeAllViews();

        TextView multiplicationAnswer = (TextView) findViewById(R.id.quotientInMultiplication);
        TextView divisionAnswer = (TextView) findViewById(R.id.quotientInDivision);

        multiplicationAnswer.setText("");
        divisionAnswer.setText("");

        Drawable normal_input = getResources().getDrawable(R.drawable.input_background);
        multiplicationAnswer.setBackground(normal_input);
        divisionAnswer.setBackground(normal_input);

        RelativeLayout easyTutorialLayout = (RelativeLayout) findViewById(R.id.easyExampleLayout);
        easyTutorialLayout.setVisibility(View.INVISIBLE);
    }

    public void startEasyTutorial (String message) {
        String[] parsed = message.split("-");
        if (parsed.length > 1) {
            showEasyTutorial(Integer.parseInt(parsed[0]), Integer.parseInt(parsed[1]));
        }
    }

    // show balls and boxes to illustrate a simple division problem
    public void showEasyTutorial(int numerator, int denominator) {
        clearView();                    // clear out all previous views from help panel

        // there are a lot of nested views here
        // the top view contains the whole illustration
        // boxLayout holds all the boxes, but since there may be more of them than fits in one box
        // it contains one or more rowOfBoxes layout
        LinearLayout boxLayout = (LinearLayout) findViewById(R.id.easyExampleBoxes);
        LinearLayout rowOfBoxes = new LinearLayout(this);
        rowOfBoxes.setOrientation(LinearLayout.HORIZONTAL);

        // these hold the questions and answers to the questions given in the tutorial
        TextView multiplicationText1 = (TextView) findViewById(R.id.multiplicationText1);
        TextView multiplicationText2 = (TextView) findViewById(R.id.multiplicationText2);
        TextView numView = (TextView) findViewById(R.id.easyExampleNumerator);
        TextView denView = (TextView) findViewById(R.id.easyExampleDenominator);
        TextView divisionText = (TextView) findViewById(R.id.divisionText);
        final TextView multiplicationAnswer = (TextView) findViewById(R.id.quotientInMultiplication);
        final TextView divisionAnswer = (TextView) findViewById(R.id.quotientInDivision);

        final Drawable tutorial_ball = ContextCompat.getDrawable(this, R.drawable.circle); // the balls that will be in boxes

        // create layouts for the boxes that will contain balls and format them correctly
        LinearLayout.LayoutParams newBoxParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        newBoxParams.setMargins(20,0,0,20);
        newBoxParams.gravity = Gravity.CENTER_HORIZONTAL;
        LinearLayout.LayoutParams boxRowParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        boxRowParams.gravity = Gravity.CENTER_HORIZONTAL;
        LinearLayout.LayoutParams ballParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ballParams.setMargins(5,5,5,5);
        LinearLayout.LayoutParams rowOfBoxesParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowOfBoxesParams.gravity = Gravity.CENTER_HORIZONTAL;

        rowOfBoxes.setLayoutParams(rowOfBoxesParams);

        final int quotient = numerator / denominator;

        // this is to space out the balls better based on the numbers (e.g. so there is not only one box on a line)
        int boxOverflow = 4;
        int ballOverflow = 3;
        if (denominator == 5 ) {
            boxOverflow = 3;
            ballOverflow = 4;
        }
        if (denominator > 8) {
            boxOverflow = 5;
        }

        // create and add boxes with balls
        for (int i = 0; i < denominator; i++ ) {

            if (i % boxOverflow == 0) {  // if have hit boxOverflow number, make a new row of boxes
                boxLayout.addView(rowOfBoxes);      // append the old one to the top boxLayout
                rowOfBoxes = new LinearLayout(this);    // and create a new rowOfBoxes layout with the same params
                rowOfBoxes.setOrientation(LinearLayout.HORIZONTAL);
                rowOfBoxes.setLayoutParams(rowOfBoxesParams);
            }

            // create a layout with background color to be a box. Each box is a newBox layout
            // but since they may contain more than one row of balls, each has one or more
            // boxRow layouts
            LinearLayout newBox = new LinearLayout(this);
            LinearLayout boxRow = new LinearLayout(this); // boxRow is a layout inside newBox to arrange balls in rows
            newBox.setBackgroundColor(getResources().getColor(R.color.lesson_background));
            newBox.setOrientation(LinearLayout.VERTICAL);

            newBox.setLayoutParams(newBoxParams);
            boxRow.setLayoutParams(boxRowParams);

            // add balls to the current row of balls
            for (int j = 0; j < quotient; j++) {
                if (j % ballOverflow == 0) {  // if have hit overflow, create a new row
                    newBox.addView(boxRow);
                    boxRow = new LinearLayout(this);
                    boxRow.setLayoutParams(boxRowParams);
                }

                ImageView view = new ImageView(this);
                view.setBackground(tutorial_ball);
                view.setLayoutParams(ballParams);
                boxRow.addView(view);
            }

            newBox.addView(boxRow);
            rowOfBoxes.addView(newBox);
        }

        boxLayout.addView(rowOfBoxes);

        // set texts to display the correct problem
        numView.setText(Integer.toString(numerator));
        denView.setText(Integer.toString(denominator));
        multiplicationText1.setText(Integer.toString(denominator) + " \u00D7 " );
        multiplicationText1.setVisibility(View.VISIBLE);
        multiplicationText2.setText(" = " + Integer.toString(numerator) );
        multiplicationText2.setVisibility(View.VISIBLE);
        divisionText.setText(Integer.toString(numerator) + " \u00F7 " + Integer.toString(denominator) + " = ");
        divisionText.setVisibility(View.VISIBLE);
        multiplicationAnswer.setVisibility(View.VISIBLE);
        divisionAnswer.setVisibility(View.VISIBLE);

        // show and implement button to check answers
        final Drawable correct_input = ContextCompat.getDrawable(this, R.drawable.input_correct);
        final Drawable incorrect_input = ContextCompat.getDrawable(this, R.drawable.input_incorrect);
        checkAnswers = (Button) findViewById(R.id.easyExampleAnswerCheck);
        checkAnswers.setClickable(true);
        checkAnswers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // when the user clicks Check Answers, look through the answers to see if they have
                // completed the step or gotten anything wrong
                // then send the appropriate message to indicate the step was correct, incorrect, or incomplete
                Boolean any_incorrect = false;
                Boolean finished = true;

                if (!multiplicationAnswer.getText().toString().equals("")) {
                    if (multiplicationAnswer.getText().toString().equals(Integer.toString(quotient))) {
                        multiplicationAnswer.setBackground(correct_input);
                    } else {
                        multiplicationAnswer.setBackground(incorrect_input);
                        any_incorrect = true;
                    }
                } else {
                    finished = false;
                }
                if (!divisionAnswer.getText().toString().equals("")) {
                    if (divisionAnswer.getText().toString().equals(Integer.toString(quotient))) {
                        divisionAnswer.setBackground(correct_input);
                    } else {
                        divisionAnswer.setBackground(incorrect_input);
                        any_incorrect = true;
                    }
                } else {
                    finished = false;
                }

                String message;
                if (any_incorrect) {
                    message = "TUTORIAL-STEP-EASY;INCORRECT";
                }
                else {
                    if (finished) {
                        message = "TUTORIAL-STEP-EASY;CORRECT";
                        enableButtons();
                    }
                    else {
                        message = "TUTORIAL-STEP-EASY;INCOMPLETE";
                    }
                }
                TCPClient.singleton.sendMessage(message);
            }
        });

        RelativeLayout easyTutorialLayout = (RelativeLayout) findViewById(R.id.easyExampleLayout);
        easyTutorialLayout.setVisibility(View.VISIBLE);
    }

    public void clearView() {           // this function hides all hints, examples and tutorials
        hideHints();
        hideExample();
        hideTutorials();
    }

    public void hideTutorials() {       // hide anything related to a tutorial
        hideStructure();
        checkAnswers = (Button) findViewById(R.id.hardExampleAnswerCheck);
        checkAnswers.setVisibility(View.INVISIBLE);
        checkAnswers.setClickable(false);
        hideEasyTutorial();
    }


    public void hideStructure() {       // hide the structure
        exampleStep = 0;
        System.out.println("Hiding structure");

        TextView denominatorBox = (TextView) findViewById(R.id.structureDenominator);
        ImageView divisionBar = (ImageView) findViewById(R.id.structureDivisionBar);
        ImageView bar1 = (ImageView) findViewById(R.id.bar1);
        ImageView bar2 = (ImageView) findViewById(R.id.bar2);
        ImageView bar3 = (ImageView) findViewById(R.id.bar3);
        ImageView subbar1 = (ImageView) findViewById(R.id.subtractionbar1);
        ImageView subbar2 = (ImageView) findViewById(R.id.subtractionbar2);
        ImageView subbar3 = (ImageView) findViewById(R.id.subtractionbar3);

        Drawable normal_input = getResources().getDrawable(R.drawable.input_background);

        resetBoxSize();

        // make everything invisible and erase any text
        for (int i = 0; i < structNumBoxes.length; i++ ){
            if (structNumBoxes[i] != null) {
                structNumBoxes[i].setText("");
                structNumBoxes[i].setVisibility(View.INVISIBLE);
            }
        }

        for (int i = 0; i < answerBoxes.length; i++ ){
            if (answerBoxes[i] != null) {
                answerBoxes[i].setText("");
                answerBoxes[i].setVisibility(View.INVISIBLE);
                answerBoxes[i].setBackground(normal_input);

            }
        }

        for (int i = 0; i < rBoxes.length; i++ ){
            for (int j=0; j <rBoxes[i].length; j++) {
                if (rBoxes[i][j] != null) {
                    rBoxes[i][j].setText("");
                    rBoxes[i][j].setVisibility(View.INVISIBLE);
                    rBoxes[i][j].setBackground(normal_input);

                }
            }
        }

        denominatorBox.setText("");
        denominatorBox.setVisibility(View.INVISIBLE);

        bar1.setVisibility(View.INVISIBLE);
        bar2.setVisibility(View.INVISIBLE);
        bar3.setVisibility(View.INVISIBLE);
        subbar1.setVisibility(View.INVISIBLE);
        subbar2.setVisibility(View.INVISIBLE);
        subbar3.setVisibility(View.INVISIBLE);

        for (int i = 1; i< arrows.length; i++) {
            for (int j = 1; j<arrows[i].length; j++) {
                if (arrows[i][j]!=null) {
                    arrows[i][j].setVisibility(View.INVISIBLE);
                }
            }
        }

        divisionBar.setVisibility(View.INVISIBLE);

    }

    public void hideExample() {
        hideStructure();
    }

    public void structureHint() {       // to show a structure hint, show structure with the current question numbers
        showStructure(Integer.parseInt(currentQuestion.numerator), Integer.parseInt(currentQuestion.denominator), false, "");
    }

    public  void focusNextStepInTutorial () { // enable the next boxes in the structure for input
        exampleStep += 1;
        Drawable current_input = getResources().getDrawable(R.drawable.input_background);

        int row_to_focus = 0;

        for (int i = 0; i < answerBoxes.length; i++) {
            if (!answerBoxes[i].isEnabled() && !(answerBoxes[i].getText().toString().equals("R"))) {
                    answerBoxes[i].setEnabled(true);
                    answerBoxes[i].setBackground(current_input);
                    break;
            }
        }

        for (int i = 0; i < rBoxes.length ; i++) {
            for (int j = 0; j <rBoxes.length; j++) {
                if (rBoxes[i][j]!=null && !rBoxes[i][j].isEnabled()) {
                    row_to_focus = i;
                    break;
                }

                if(row_to_focus != 0) {
                    break;
                }
            }
        }

        System.out.println("enabling answer" + Integer.toString(row_to_focus));

        for (int i = row_to_focus; i< row_to_focus + 3 && i<rBoxes.length; i++) {
            for (int j = 0; j<rBoxes[i].length; j++) {
                if (rBoxes[i][j]!=null && !rBoxes[i][j].isEnabled()) {
                    rBoxes[i][j].setEnabled(true);
                    rBoxes[i][j].setBackground(current_input);
                }
            }
        }

        if (row_to_focus == 0){
            checkAnswers.setClickable(false);
            TCPClient.singleton.sendMessage("TUTORIAL-STEP;DONE");
            answerText.setEnabled(true);
            submitButton.setEnabled(true);
        }
    }

    // show the box structure for the given numbers. If this is for a tutorial, additionally
    // format the structure for that purpose
    public void showStructure(int numerator, int denominator, Boolean tutorial, String answers) {

        System.out.println("showing structure");
        clearView();

        TextView denominatorBox = (TextView) findViewById(R.id.structureDenominator);
        ImageView divisionBar = (ImageView) findViewById(R.id.structureDivisionBar);
        ImageView bar1 = (ImageView) findViewById(R.id.bar1);
        ImageView bar2 = (ImageView) findViewById(R.id.bar2);
        ImageView bar3 = (ImageView) findViewById(R.id.bar3);
        ImageView bar4 = (ImageView) findViewById(R.id.bar4);
        ImageView subbar1 = (ImageView) findViewById(R.id.subtractionbar1);
        ImageView subbar2 = (ImageView) findViewById(R.id.subtractionbar2);
        ImageView subbar3 = (ImageView) findViewById(R.id.subtractionbar3);
        ImageView subbar4 = (ImageView) findViewById(R.id.subtractionbar4);

        int num_digits = 1;
        int den_digits = 1;

        if (denominator > 9) {
            den_digits = 2; // there are no questions with 3 digit denominators
        }

        if (numerator > 10000) {
            num_digits = 5;
        } else if (numerator > 1000) {
            num_digits = 4;
        } else if (numerator > 100) {
            num_digits = 3;
        }
        else if (numerator > 10) {
            num_digits = 2;
        }

        if (num_digits > 3) {           // the structure must be smaller if there are more digits
            shrinkBoxes();
        }


        // this just the numbers of digits to determine how many
        // boxes to show in each row. This way, it also doesn't give extra hints about the problem
        // by tailoring the number of boxes
        denominatorBox.setText(Integer.toString(denominator));
        denominatorBox.setVisibility(View.VISIBLE);
        divisionBar.setVisibility(View.VISIBLE);

        rBoxes[1][1].setVisibility(View.VISIBLE);
        rBoxes[2][2].setVisibility(View.VISIBLE);

        bar1.setVisibility(View.VISIBLE);
        subbar1.setVisibility(View.VISIBLE);

        answerBoxes[0].setVisibility(View.VISIBLE);
        answerBoxes[1].setVisibility(View.VISIBLE);

        int numparts = numerator;
        for (int i = num_digits - 1; i >= 0; i--) {
            String numdigit = Integer.toString(numparts % 10);

            if (i == 0 && (numparts % 10 < denominator)) {
                den_digits = 2;
            }

            numparts = numparts / 10;
            if (structNumBoxes[i] != null) {
                structNumBoxes[i].setVisibility(View.VISIBLE);
                structNumBoxes[i].setText(numdigit);
            }
            if (answerBoxes[i] != null) {
                answerBoxes[i].setVisibility(View.VISIBLE);
            }
        }

        // show remainder box if this answer has a remainder -- use the next two answer boxes not used
        if (numerator % denominator != 0) {
            answerBoxes[num_digits].setVisibility(View.VISIBLE); // this is the R for "remainder"
            answerBoxes[num_digits+1].setVisibility(View.VISIBLE); // this is for the actual remainder
            answerBoxes[num_digits].setText("R");
            answerBoxes[num_digits].setEnabled(false);
        }


        if (den_digits == 2) {
            rBoxes[1][1].setVisibility(View.VISIBLE);
            rBoxes[1][2].setVisibility(View.VISIBLE);
            rBoxes[2][1].setVisibility(View.VISIBLE);
            rBoxes[2][2].setVisibility(View.VISIBLE);

            if (num_digits > 2) {
                rBoxes[2][3].setVisibility(View.VISIBLE);
                rBoxes[3][1].setVisibility(View.VISIBLE);
                rBoxes[3][2].setVisibility(View.VISIBLE);
                rBoxes[3][3].setVisibility(View.VISIBLE);
                rBoxes[4][2].setVisibility(View.VISIBLE);
                rBoxes[4][3].setVisibility(View.VISIBLE);
                arrows[2][3].setVisibility(View.VISIBLE);

                bar2.setVisibility(View.VISIBLE);
                subbar2.setVisibility(View.VISIBLE);
            }

            if (num_digits > 3) {
                rBoxes[4][4].setVisibility(View.VISIBLE);
                rBoxes[5][2].setVisibility(View.VISIBLE);
                rBoxes[5][3].setVisibility(View.VISIBLE);
                rBoxes[5][4].setVisibility(View.VISIBLE);
                rBoxes[6][3].setVisibility(View.VISIBLE);
                rBoxes[6][4].setVisibility(View.VISIBLE);

                bar3.setVisibility(View.VISIBLE);
                subbar3.setVisibility(View.VISIBLE);
                arrows[2][4].setVisibility(View.VISIBLE);

            }

            if (num_digits > 4) {
                rBoxes[6][5].setVisibility(View.VISIBLE);
                rBoxes[7][3].setVisibility(View.VISIBLE);
                rBoxes[7][4].setVisibility(View.VISIBLE);
                rBoxes[7][5].setVisibility(View.VISIBLE);
                rBoxes[8][4].setVisibility(View.VISIBLE);
                rBoxes[8][5].setVisibility(View.VISIBLE);

                bar4.setVisibility(View.VISIBLE);
                subbar4.setVisibility(View.VISIBLE);
                arrows[2][5].setVisibility(View.VISIBLE);
            }

        }

        if (den_digits == 1) {
            rBoxes[1][1].setVisibility(View.VISIBLE);
            rBoxes[2][1].setVisibility(View.VISIBLE);
            rBoxes[2][2].setVisibility(View.VISIBLE);
            rBoxes[3][1].setVisibility(View.VISIBLE);
            rBoxes[3][2].setVisibility(View.VISIBLE);
            rBoxes[4][2].setVisibility(View.VISIBLE);

            arrows[1][2].setVisibility(View.VISIBLE);

            bar2.setVisibility(View.VISIBLE);
            subbar2.setVisibility(View.VISIBLE);

            if (num_digits > 2) {
                rBoxes[4][3].setVisibility(View.VISIBLE);
                rBoxes[5][2].setVisibility(View.VISIBLE);
                rBoxes[5][3].setVisibility(View.VISIBLE);
                rBoxes[6][3].setVisibility(View.VISIBLE);

                bar3.setVisibility(View.VISIBLE);
                subbar3.setVisibility(View.VISIBLE);
                arrows[1][3].setVisibility(View.VISIBLE);

            }

            if (num_digits > 3) {
                rBoxes[6][4].setVisibility(View.VISIBLE);
                rBoxes[7][3].setVisibility(View.VISIBLE);
                rBoxes[7][4].setVisibility(View.VISIBLE);
                rBoxes[8][4].setVisibility(View.VISIBLE);

                bar4.setVisibility(View.VISIBLE);
                subbar4.setVisibility(View.VISIBLE);
                arrows[1][4].setVisibility(View.VISIBLE);

            }

            if (num_digits > 4) {
                rBoxes[8][5].setVisibility(View.VISIBLE);

                bar4.setVisibility(View.VISIBLE);
                subbar4.setVisibility(View.VISIBLE);
                arrows[1][5].setVisibility(View.VISIBLE);
            }
        }
        if (tutorial) {
            // if this is a tutorial, we must do a few more things
            exampleStep = 1;
            System.out.println("starting tutorial");
            resetRBoxAnswers();
            // extract answers for all of the boxes to be able to check them later
            final String[] answerList = answers.split(":");
            for (int i = 0; i < answerList.length; i++) {
                String[] answer = answerList[i].split("-");

                if (answer.length > 2) {
                    int row = Integer.parseInt(answer[0]);
                    int col = Integer.parseInt(answer[1]);
                    String val = answer[2];
                    rBoxAnswers[row][col]= val;
                }
            }

            // change color of current boxes to highlight them
            Drawable current_input = getResources().getDrawable(R.drawable.input_background);
            Drawable disabled_input = getResources().getDrawable(R.drawable.input_disabled);

            // disable all boxes except for first set
            for (int i=2; i<rBoxes.length; i++) {
                for (int j = 1; j <rBoxes[i].length; j++ ) {
                    if (rBoxes[i][j] != null) {
                        rBoxes[i][j].setEnabled(false);
                        rBoxes[i][j].setBackground(disabled_input);
                    }
                }
            }

            for (int i = 1; i < answerBoxes.length; i++) {
                if (answerBoxes[i] != null) {
                    answerBoxes[i].setEnabled(false);
                    answerBoxes[i].setBackground(disabled_input);
                }
            }

            rBoxes[1][1].setBackground(current_input);
            rBoxes[1][2].setBackground(current_input);
            rBoxes[2][1].setBackground(current_input);
            rBoxes[2][2].setBackground(current_input);
            answerBoxes[0].setBackground(current_input);

            rBoxes[1][1].setEnabled(true);
            rBoxes[1][2].setEnabled(true);
            rBoxes[2][1].setEnabled(true);
            rBoxes[2][2].setEnabled(true);
            answerBoxes[0].setEnabled(true);

            if (rBoxAnswers[0][1].equals("")) {
                answerBoxes[1].setBackground(current_input);
                answerBoxes[1].setEnabled(true);
            }


            // add button to check current step
            final Drawable correct_input = ContextCompat.getDrawable(this, R.drawable.input_correct);
            final Drawable incorrect_input = ContextCompat.getDrawable(this, R.drawable.input_incorrect);

            checkAnswers = (Button) findViewById(R.id.hardExampleAnswerCheck);
            checkAnswers.setVisibility(View.VISIBLE);
            checkAnswers.setClickable(true);
            checkAnswers.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Boolean any_incorrect = false;
                    Boolean finished = true;

                    // when Check Answers is pressed, go through the boxes that are enabled to see
                    // if the values match the ones in the answers
                    // if anything is empty or incorrect, note that
                    for (int i = 0; i<answerBoxes.length; i++) {
                        if (answerBoxes[i] != null) {
                            if (answerBoxes[i].isFocusable() && answerBoxes[i].getVisibility()==View.VISIBLE && answerBoxes[i].isEnabled()) {
                                if (answerBoxes[i].getText().toString().equals("") && !rBoxAnswers[0][i+1].equals("")) {
                                    finished = false;
                                } else {
                                    if (answerBoxes[i].getText().toString().equals(rBoxAnswers[0][i+1]) ||
                                            (rBoxAnswers[0][i+1].equals("") && answerBoxes[i].getText().toString().equals("0"))) {
                                        answerBoxes[i].setBackground(correct_input);
                                    } else {
                                        answerBoxes[i].setBackground(incorrect_input);
                                        any_incorrect = true;
                                        System.out.println("answer incorrect at "  + Integer.toString(i) + "should be " + rBoxAnswers[0][i+1] + " is " + answerBoxes[i]);

                                    }
                                }
                            }
                        }
                    }
                    for (int i = 0; i<rBoxes.length ; i++) {
                        for (int j = 0; j < rBoxes[i].length; j++) {
                            if (rBoxes[i][j] != null) {
                                if (rBoxes[i][j].isFocusable() && rBoxes[i][j].getVisibility()==View.VISIBLE && rBoxes[i][j].isEnabled()) {
                                    if (rBoxes[i][j].getText().toString().equals("") && !rBoxAnswers[i][j].equals("")) {
                                        finished = false;
                                        System.out.println("empty box" + Integer.toString(i) + Integer.toString(j));

                                    } else {
                                        if (rBoxes[i][j].getText().toString().equals(rBoxAnswers[i][j]) ||
                                                (rBoxAnswers[i][j].equals("") && rBoxes[i][j].getText().toString().equals("0"))) {
                                            rBoxes[i][j].setBackground(correct_input);
                                        } else {
                                            rBoxes[i][j].setBackground(incorrect_input);
                                            any_incorrect = true;

                                        }
                                    }
                                }
                            }
                        }
                    }

                    // send a message back to server to indicate if this was a successful attempt or not
                    String message;
                    if (finished) {
                        if (any_incorrect) {
                             message = "TUTORIAL-STEP;INCORRECT";
                        } else {
                            message = "TUTORIAL-STEP;CORRECT";
                            focusNextStepInTutorial();
                        }
                    } else {
                         message = "TUTORIAL-STEP;INCOMPLETE";
                    }

                    TCPClient.singleton.sendMessage(message);
                }
            });
        } else {
            // otherwise, if not for tutorial
            // make sure all are focusable and have correct background
            // change color of current boxes to highlight them
            Drawable normal_input = getResources().getDrawable(R.drawable.input_background);

            for (int i = 1; i< rBoxes.length; i ++) {
                for (int j=1; j< rBoxes[i].length; j++) {
                    if (rBoxes[i][j] != null) {             // note: when looping always check if null because not all rBoxes exist
                        rBoxes[i][j].setBackground(normal_input);
                        rBoxes[i][j].setEnabled(true);
                    }
                }
            }
            for (int i = 0; i<answerBoxes.length; i++) {
                if (answerBoxes[i] != null) {
                    answerBoxes[i].setBackground(normal_input);
                    answerBoxes[i].setEnabled(true);
                }
            }
        }
    }

    public void resetRBoxAnswers() {
        for (int i = 0; i< rBoxAnswers.length; i++) {
            for (int j = 0; j < rBoxAnswers.length; j++) {
                rBoxAnswers[i][j] = "";
            }
        }
    }
    public void fillInEasy(String message_text) {       // fill in the answers for the easy tutorial
        String[] parsed = message_text.split("-");
        TextView current_step_box;
        if (parsed[0].equals("1")) {
            current_step_box = (TextView) findViewById(R.id.quotientInMultiplication);
        } else {
            current_step_box = (TextView) findViewById(R.id.quotientInDivision);
        }

        current_step_box.setText(parsed[1]);
    }

    public void fillInBoxes() {
        // if no string given to specify which boxes to fill in, fill in all currently enabled boxes
        // this is used in tutorials to fill in the current step -- otherwise it will fill in the whole
        // structure

        for (int i = 0; i<answerBoxes.length; i++) {
            if (answerBoxes[i] != null) {
                if (answerBoxes[i].isFocusable() && answerBoxes[i].getVisibility()==View.VISIBLE && answerBoxes[i].isEnabled()) {
                    answerBoxes[i].setText(rBoxAnswers[0][i+1]);
                } else {
                    System.out.println(Integer.toString(i) + " was not focusable, visible or enabled");

                }
            }
        }
        for (int i = 0; i<rBoxes.length ; i++) {
            for (int j = 0; j < rBoxes[i].length; j++) {
                if (rBoxes[i][j] != null) {
                    if (rBoxes[i][j].isFocusable() && rBoxes[i][j].getVisibility()==View.VISIBLE && rBoxes[i][j].isEnabled()) {
                        rBoxes[i][j].setText(rBoxAnswers[i][j]);
                    } else {
                        System.out.println(Integer.toString(i)+ Integer.toString(j) + " was not focusable, visible or enabled");
                    }
                }
            }
        }

    }

    public void fillInBoxes(String message_text) {      // fill in boxes in the structure

        // parse the message text to find which box and what answer
        String[] boxSpecs = message_text.split(":");    // each boxSpec is one box

        for (int i = 0; i < boxSpecs.length; i++) {
            String[] boxData = boxSpecs[i].split("-");

            if (boxData.length > 2) {
                int row = Integer.parseInt(boxData[0]);
                int col = Integer.parseInt(boxData[1]);
                String val = boxData[2];

                if (row == 0) {                         // row == 0 means its an answer row box
                    answerBoxes[col-1].setText(val);
                }

                else if (rBoxes[row][col] != null) {
                    rBoxes[row][col].setText(val);
                }
            }
        }
    }

    public void messageReceived(String message){            // receive messages from the tablet
        System.out.println("IN MATHACTIVITY, message received from server is: " + message);
        if (message == null){
            //possibly catch a null pointer exception?
            System.out.println("null message received from server");
        }
        else {
            if (message.equals("SPEAKING-END")) {           // if robot is not speaking, buttons can be enabled
                enableButtons();
            }
            else if(message.equals("SPEAKING-START")) {     // disable buttons when robot speaking
                disableButtons();
            }
            else {
                String[] separatedMessage = message.split(";");
                if (separatedMessage[0].equals(MathControl.QUESTIONMESSAGE)     // set the next question to the specified question
                        && separatedMessage.length == 3) {
                    nextQuestion = mathContol.getQuestion(separatedMessage[1],separatedMessage[2]);
                    nextAction = MathControl.SHOWQUESTION;
                    submitButton.setVisibility(View.INVISIBLE);
                    final Drawable nextButtonBackground = ContextCompat.getDrawable(this, R.color.focused_answer);
                    nextButton.setVisibility(View.VISIBLE);
                    nextButton.setBackground(nextButtonBackground);
                }
                else if (separatedMessage[0].equals(MathControl.STARTTICTACTOE)) {  // start a game of tictactoe
                    startTicTacToe();
                }
                else if (separatedMessage[0].equals(MathControl.SHOWTEXTHINT)) {        // show text
                    showHints(separatedMessage[1]);
                }
                else if (separatedMessage[0].equals(MathControl.SHOWSTRUCTURE)) {   // show the box structure
                    if (separatedMessage.length > 1) {                              // if numbers are given, use those
                        int num = Integer.parseInt(separatedMessage[1].split("-")[0]);
                        int den = Integer.parseInt(separatedMessage[1].split("-")[1]);
                        showStructure(num, den, false, "");
                    } else {                                                        // otherwise show the structure of the current question
                        structureHint();
                    }
                }
                else if (separatedMessage[0].equals(MathControl.SHOWSTRUCTUREFORTUTORIAL)) {    // if for tutorial
                    int num = Integer.parseInt(separatedMessage[1].split("-")[0]);              // call the same showStructure() function
                    int dem = Integer.parseInt(separatedMessage[1].split("-")[1]);              // for the given numbers
                    showStructure(num, dem, true, separatedMessage[2]);                         // but give the answers and set tutorial to true
                }
                else if (separatedMessage[0].equals(MathControl.FILLSTRUCTURE)) {               // fill in steps for a tutorial
                    if (separatedMessage.length > 2 && separatedMessage[1].equals("EASY")) {    // whether easy or structure
                        fillInEasy(separatedMessage[2]);
                    } else {
                        if (separatedMessage.length > 1) {
                            fillInBoxes(separatedMessage[1]);
                        } else {
                            fillInBoxes();
                        }
                    }
                }
                else if (separatedMessage[0].equals(MathControl.SHOWEASYTUTORIAL)) {            // show balls in boxes for an easy tutorial
                    startEasyTutorial(separatedMessage[1]);
                }
            }
        }

    }

    public void startTicTacToe() {
        Intent intent = new Intent(this, TicTacToeActivity.class);
        intent.putExtra("expGroup", "" + expGroup);
        startActivityForResult(intent, MathControl.TICTACTOEREQUEST);

        enableButtons();

    }

    public void resetBoxSize() {
        final float scale = getResources().getDisplayMetrics().density;
        int pixels = (int) (60 * scale + 0.5f);
        int textSize = 45;

        setBoxSize(pixels, textSize);
    }

    public void shrinkBoxes() {
        final float scale = getResources().getDisplayMetrics().density;
        int pixels = (int) (40 * scale + 0.5f);
        int textSize = 30;

        setBoxSize(pixels, textSize);
    }

    public void setBoxSize(int pixels, int textSize) {

        for (int i = 0; i < rBoxes.length; i++) {
            for (int j = 0; j <rBoxes[i].length; j++) {
                if (rBoxes[i][j]!=null) {
                    ViewGroup.LayoutParams params= rBoxes[i][j].getLayoutParams();
                    params.width= pixels;
                    rBoxes[i][j].setLayoutParams(params);
                    rBoxes[i][j].setTextSize(textSize);
                }
            }
        }

        for (int i = 1; i < arrows.length; i++) {
            for (int j=1; j<arrows[i].length; j++) {
                if (arrows[i][j] != null) {
                    ViewGroup.LayoutParams params= arrows[i][j].getLayoutParams();
                    params.width= pixels;
                    arrows[i][j].setLayoutParams(params);
                }
            }
        }

        for (int i = 0; i < answerBoxes.length; i++) {
            if (answerBoxes[i]!=null) {
                ViewGroup.LayoutParams params= answerBoxes[i].getLayoutParams();
                params.width= pixels;
                answerBoxes[i].setLayoutParams(params);
                answerBoxes[i].setTextSize(textSize);
            }
        }
        TextView denominatorBox = (TextView) findViewById(R.id.structureDenominator);
        denominatorBox.setTextSize(textSize);
        denominatorBox.setWidth(pixels);

        for (int i = 0; i < structNumBoxes.length; i++) {
            if (structNumBoxes[i]!=null) {
                ViewGroup.LayoutParams params= structNumBoxes[i].getLayoutParams();
                params.width= pixels;
                structNumBoxes[i].setLayoutParams(params);
                structNumBoxes[i].setTextSize(textSize);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (TCPClient.singleton != null)
            TCPClient.singleton.setSessionOwner(this);

        if (data.hasExtra("nextLevel") && data.hasExtra("nextNumber")) {
            nextQuestion = mathContol.getQuestion(data.getExtras().getInt("nextLevel"), data.getExtras().getInt("nextNumber"));
            goToNextQuestion();
        }

        enableButtons();
    }
}