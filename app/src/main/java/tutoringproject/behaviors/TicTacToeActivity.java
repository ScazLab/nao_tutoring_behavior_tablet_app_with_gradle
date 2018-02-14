package tutoringproject.behaviors;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by arsalan on 4/13/16.
 */
public class TicTacToeActivity extends Activity implements TCPClientOwner {

    private enum SquareState { EMPTY, X, O }
    //private enum ExpGroup { FIXED, REWARD, FRUSTRATION }

    private SquareState[][] board = new SquareState[3][3];
    private int expGroup = 0;
    private Random gen = new Random();
    private long startTime = System.currentTimeMillis();

    // XML element variables
    private Button[][] boardButtons = new Button[3][3];
    private TextView instructions;
    private Button returnButton;

    // Public variables ============================================================================

    // The larger the depth, the better Nao will play. In short, Nao will think depth - 1 moves
    // ahead. Please don't use a depth of 0! This will cause the game to malfunction.
    public int MINIMAX_DEPTH = 2;

    // The break will end after this time limit (represented in seconds) is passed and the current
    // game is finished.
    public long TIME_LIMIT = 30;// aditi: changing to 30 seconds for adaptive help study

    // Speech strings
    /* These strings are no longer necessary. The start speech is now generated in nao_server.py.
    public HashMap<ExpGroup, String> START_MSGS = new HashMap<ExpGroup, String>() {{
        put(ExpGroup.FIXED,
            "Let's take a break and play a game of tic tac toe. You will be exes, and I will be " +
            "ohs. You can go first. Click any square on the board.");
        put(ExpGroup.REWARD,
            "You've been doing so well! You deserve a break. Let's play a game of tic tac toe. " +
            "You will be exes, and I will be ohs. You can go first. Click any square on the " +
            "board.");
        put(ExpGroup.FRUSTRATION,
            "Why don't we take a break and play a game of tic tac toe. You will be exes, and I " +
            "will be ohs. You can go first. Click any square on the board.");
    }};
    */
    public String[] WIN_MSGS = {
            "Looks like you won! Congrats!"
    };
    public String[] TIE_MSGS = {
            "Looks like it's a tie! Good game!"
    };
    public String[] LOSS_MSGS = {
            "Looks like I won this time, but it was super close!"
    };
    public String[] NAO_TURN_MSGS = {
            "Alright, my turn now.",
            "Nice move! Let me think.",
            "Okay, my turn."
    };
    public String[] STUDENT_TURN_MSGS = {
            "Your turn again.",
            "Done! Back to you.",
            "All set!"
    };
    public String[] RESTART_MSGS = {
            "Let's play again! You can go first.",
            "How about one more game. Press any square on the board."
    };
    public String END_MSG =
            "That was fun! Press the button on the screen to move on!";

    public String START_MSG =
            "Lets take a break and play a game of tic-tac-toe. You will be exes, and I will be ohs. You can go first. Press any square on the board.";

    // Tablet text strings
    public String SQUARE_OCCUPIED_TEXT =
            "Sorry!\nThis square already has something in it.\nTry picking another square.";
    public String CLICK_RETURN_BUTTON_TEXT =
            "Press the button below to return to the tutoring session.";

    // Constructor =================================================================================

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tic_tac_toe);

        // <expGroup> is no longer used in this class. But I'm going to leave it here just in case
        // we find a use for it in the future.
        Bundle extras = getIntent().getExtras();
        expGroup = Integer.parseInt(extras.getString("expGroup"));
        int breakInitiatedByModel = extras.getInt("breakInitiatedByModel");
//        if (expGroupIndex == 1) {
//            expGroup = ExpGroup.FIXED;
//        } else if (expGroupIndex == 2) {
//            expGroup = ExpGroup.REWARD;
//        } else if (expGroupIndex == 3) {
//            expGroup = ExpGroup.FRUSTRATION;
//        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = SquareState.EMPTY;
            }
        }

        boardButtons[0][0] = (Button)   findViewById(R.id.boardButton0);
        boardButtons[0][1] = (Button)   findViewById(R.id.boardButton1);
        boardButtons[0][2] = (Button)   findViewById(R.id.boardButton2);
        boardButtons[1][0] = (Button)   findViewById(R.id.boardButton3);
        boardButtons[1][1] = (Button)   findViewById(R.id.boardButton4);
        boardButtons[1][2] = (Button)   findViewById(R.id.boardButton5);
        boardButtons[2][0] = (Button)   findViewById(R.id.boardButton6);
        boardButtons[2][1] = (Button)   findViewById(R.id.boardButton7);
        boardButtons[2][2] = (Button)   findViewById(R.id.boardButton8);
        instructions       = (TextView) findViewById(R.id.instructions);
        returnButton       = (Button)   findViewById(R.id.returnButton);

        returnButton.setVisibility(View.INVISIBLE); //start out with button being invisible

        // Transfer control of TCP client from MathActivity to this activity.
        if (TCPClient.singleton != null ) {
            TCPClient.singleton.setSessionOwner(this);
        }

        if (TCPClient.singleton != null) {
            if (expGroup==0 && breakInitiatedByModel==0) {
                TCPClient.singleton.sendMessage("TICTACTOE-START;-1;-1;"+START_MSG);
            }
            else {
                TCPClient.singleton.sendMessage("TICTACTOE-START;-1;-1;");
            }
        }
    }

    // Button handlers =============================================================================

    public void boardButtonPressed(View view) {
        instructions.setText("");

        // Identify the button that was pressed.
        Button button = (Button)view;
        int buttonRow = 0;
        int buttonCol = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (button == boardButtons[i][j]) {
                    buttonRow = i;
                    buttonCol = j;
                    break;
                }
            }
        }

        // Check if the square is already occupied.
        if (board[buttonRow][buttonCol] != SquareState.EMPTY) {
            instructions.setText(SQUARE_OCCUPIED_TEXT);

            // If not,
        } else {
            // Place an X in the square.
            board[buttonRow][buttonCol] = SquareState.X;
            button.setTextColor(Color.RED);
            button.setText("X");

            // Send a message to nao_server.py based on the state of the game.
            //
            // nao_server.py will send a message back to us containing the type of the message that
            // we sent. Our messageReceived() method will process the returned message and direct
            // the course of the activity accordingly.
            if (won(SquareState.X)) {
                if (TCPClient.singleton != null) {
                    TCPClient.singleton.sendMessage(
                            "TICTACTOE-WIN;-1;-1;" + getRandomMsg(WIN_MSGS));
                }
            } else if (full()) {
                if (TCPClient.singleton != null) {
                    TCPClient.singleton.sendMessage(
                            "TICTACTOE-TIE;-1;-1;" + getRandomMsg(TIE_MSGS));
                }
            } else {
                if (TCPClient.singleton != null) {
                    TCPClient.singleton.sendMessage(
                            "TICTACTOE-NAOTURN;-1;-1;" + getRandomMsg(NAO_TURN_MSGS));
                }
            }
        }
    }

    public void returnButtonPressed(View view) {
        finish();
    }

    // Game-playing methods ========================================================================

    /**
     * This method simulates Nao's turn. It's the complement to boardButtonPressed().
     */
    public void naoTurn() {
        // Pick an available square for Nao.
        int selection = minimaxMain(SquareState.O, MINIMAX_DEPTH)[0];
        int row = selection / 3;
        int col = selection % 3;

        SystemClock.sleep(4000);
        // Place an O in the square.
        board[row][col] = SquareState.O;
        boardButtons[row][col].setTextColor(Color.GREEN);
        boardButtons[row][col].setText("O");

        // Send a message to nao_server.py based on the state of the game.
        //
        // nao_server.py will send a message back to us containing the type of the message that we
        // sent. Our messageReceived() method will process the returned message and direct the
        // course of the activity accordingly.
        if (won(SquareState.O)) {
            if (TCPClient.singleton != null) {
                TCPClient.singleton.sendMessage("TICTACTOE-LOSS;-1;-1;" + getRandomMsg(LOSS_MSGS));
            }
        } else if (full()) {
            if (TCPClient.singleton != null) {
                TCPClient.singleton.sendMessage("TICTACTOE-TIE;-1;-1;" + getRandomMsg(TIE_MSGS));
            }
        } else {
            if (TCPClient.singleton != null) {
                TCPClient.singleton.sendMessage(
                        "TICTACTOE-STUDENTTURN;-1;-1;" + getRandomMsg(STUDENT_TURN_MSGS));
            }
        }
    }

    /**
     * This is the parent method for the minimax tic-tac-toe playing strategy.
     */
    public int[] minimaxMain(SquareState player, int depth) {
        // Base case
        if (gameOver() || depth == 0) {
            return new int[]{-1, minimaxScore()};
        }

        // Recursive case
        int[] scores = new int[9];
        for (int i = 0; i < 9; i++) {
            scores[i] = -1;
        }
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == SquareState.EMPTY) {
                    board[i][j] = player;
                    scores[i * 3 + j] = minimaxMain(opponent(player), depth - 1)[1];
                    board[i][j] = SquareState.EMPTY;
                }
            }
        }
        if (player == SquareState.X) {
            return maxScore(scores);
        } else {
            return minScore(scores);
        }
    }

    /**
     * This is a helper function for minimaxMain().
     */
    public int minimaxScore() {
        if (won(SquareState.X)) return  10;
        if (won(SquareState.O)) return -10;
        return 0;
    }

    /**
     * This is a helper function for minimaxMain(). It returns the index with the maximum value in
     * <scores> along with the value. If multiple indexes have this value, one is randomly selected.
     */
    public int[] maxScore(int[] scores) {
        ArrayList<Integer> maxIndexes = new ArrayList<Integer>();
        int max = -11;
        for (int i = 0; i < 9; i++) {
            if (scores[i] != -1) {
                if (scores[i] > max) {
                    maxIndexes.clear();
                    maxIndexes.add(i);
                    max = scores[i];
                } else if (scores[i] == max) {
                    maxIndexes.add(i);
                }
            }
        }
        int maxIndex = maxIndexes.get(gen.nextInt(maxIndexes.size()));
        return new int[]{maxIndex, max};
    }

    /**
     * This is a helper function for minimaxMain(). It returns the index with the minimum value in
     * <scores> along with the value. If multiple indexes have this value, one is randomly selected.
     */
    public int[] minScore(int[] scores) {
        ArrayList<Integer> minIndexes = new ArrayList<Integer>();
        int min = 11;
        for (int i = 0; i < 9; i++) {
            if (scores[i] != -1) {
                if (scores[i] < min) {
                    minIndexes.clear();
                    minIndexes.add(i);
                    min = scores[i];
                } else if (scores[i] == min) {
                    minIndexes.add(i);
                }
            }
        }
        int minIndex = minIndexes.get(gen.nextInt(minIndexes.size()));
        return new int[]{minIndex, min};
    }

    /**
     * This helper method returns true if the current state of <board> corresponds to a finished
     * game.
     */
    public boolean gameOver() {
        return won(SquareState.X) || won(SquareState.O) || full();
    }

    /**
     * This helper method returns true if the specified player (X or O) has won.
     */
    public boolean won(SquareState player) {
        // Check rows.
        for (int i = 0; i < 3; i++) {
            if (board[i][0] == player && board[i][1] == player && board[i][2] == player) {
                return true;
            }
        }
        // Check columns.
        for (int j = 0; j < 3; j++) {
            if (board[0][j] == player && board[1][j] == player && board[2][j] == player) {
                return true;
            }
        }
        // Check diagonals.
        if (   (board[0][0] == player && board[1][1] == player && board[2][2] == player)
                || (board[0][2] == player && board[1][1] == player && board[2][0] == player)) {
            return true;
        }
        return false;
    }

    /**
     * This helper method returns true if the board is full.
     */
    public boolean full() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == SquareState.EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * This helper method returns the opponent of the specified player (X or O).
     */
    public SquareState opponent(SquareState player) {
        if (player == SquareState.X) return SquareState.O;
        if (player == SquareState.O) return SquareState.X;
        return SquareState.EMPTY;
    }

    // Incoming message handler ====================================================================

    /**
     * This method directs the course of the activity based on the messages that it receives from
     * nao_server.py.
     */
    public void messageReceived(String msg) {
        System.out.println("[ TicTacToeActivity ] Received the following message: " + msg);

        if (msg.equals("SPEAKING-START")){
            disableBoardButtons();
            if (returnButton.getVisibility()==View.VISIBLE)
                disableReturnButton();
        }
        else if (msg.equals("SPEAKING-END")) {

            if (returnButton.getVisibility()==View.VISIBLE) {
                enableReturnButton();
            }
            else{
                enableBoardButtons();
            }
        }
        else if (   msg.equals("TICTACTOE-START")
                || msg.equals("TICTACTOE-STUDENTTURN")) {
            enableBoardButtons();

        } else if (   msg.equals("TICTACTOE-WIN")
                || msg.equals("TICTACTOE-TIE")
                || msg.equals("TICTACTOE-LOSS")) {

            disableBoardButtons();
            if (System.currentTimeMillis() - startTime > TIME_LIMIT * 1000) {
                if (TCPClient.singleton != null) {
                    TCPClient.singleton.sendMessage("TICTACTOE-END;-1;-1;" + END_MSG);
                }
            } else {
                // Depending on whether the student won or lost, make Nao play better or worse.
                if (msg.equals("TICTACTOE-WIN")) {
                    MINIMAX_DEPTH++;
                } else if (msg.equals("TICTACTOE-LOSS")) {
                    if (MINIMAX_DEPTH > 1) {
                        MINIMAX_DEPTH--;
                    }
                }
                if (TCPClient.singleton != null) {
                    TCPClient.singleton.sendMessage(
                            "TICTACTOE-RESTART;-1;-1;" + getRandomMsg(RESTART_MSGS));
                }
            }

        } else if (msg.equals("TICTACTOE-NAOTURN")) {
            naoTurn();

        } else if (msg.equals("TICTACTOE-RESTART")) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    board[i][j] = SquareState.EMPTY;
                    boardButtons[i][j].setText("");
                }
            }
            enableBoardButtons();

        } else if (msg.contains("TICTACTOE-END") || msg.contains("QUESTION")) {
            String[] separatedMsg = msg.split(";");
            Intent intent = new Intent();
            if (separatedMsg.length > 2) {
                intent.putExtra("nextLevel", Integer.parseInt(separatedMsg[1]));
                intent.putExtra("nextNumber", Integer.parseInt(separatedMsg[2]));
                setResult(Activity.RESULT_OK, intent);
            }

            disableBoardButtons();
            returnButton.setVisibility(View.VISIBLE);
            enableReturnButton();
            instructions.setText(CLICK_RETURN_BUTTON_TEXT);
        }
    }

    // Button disabling and enabling methods =======================================================

    public void disableButtons() {
        disableBoardButtons();
        disableReturnButton();
    }

    public void disableBoardButtons() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                boardButtons[i][j].setEnabled(false);
            }
        }
    }

    public void enableBoardButtons() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                boardButtons[i][j].setEnabled(true);
            }
        }
    }

    public void disableReturnButton() {
        returnButton.setEnabled(false);
    }

    public void enableReturnButton() {
        returnButton.setEnabled(true);
    }

    // Other helper methods ========================================================================

    public String getRandomMsg(String[] msgList) {
        return msgList[gen.nextInt(msgList.length)];
    }
}
