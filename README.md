# nao_tutoring_behavior_tablet_app

## App Components and Flows

### QuestionActivity
Most of the activity in the app happens during the QuestionActivity activity. During this activity, there is a permanent question panel displaying a division question to the user. A keyboard along the bottom serves as the input to the answer input box.
On the right side, there is a Help panel, which is either blank or displays any visuals necessary to the current tutoring behavior provided.

### Messages

#### node_tablet to tablet app messages

node_tablet sends messages to the app via TCP messages, which just have the form of a string. Different parts of each instruction are always separated by a semicolon. The following types and formats of instructions are supported.

`"QUESTION;(level);(number)` This message triggers the tablet to display the next question, specified by the given level and question number. The tablet has access to the json containing questions, so this is all of the information that is required to show the question.

`SHOWSTRUCTURE` messages cause the tablet to display the box structure for a problem, but come in different flavors based on the other parts of the instruction
    `SHOWSTRUCTURE;` Given no other information, the tablet will display the structure of the current question
    `SHOWSTRUCTURE;numerator-denominator` will show the box structure for the problem of numerator/denominator. 
    `SHOWSTRUCTURE-TUTORIAL;numerator-denominator;[All-Answers]` will show the box structure for the problem of numerator/denominator. It will only enable the first step of boxes to be filled in because they are being used in a tutorial. 
    The All-Answers string contains information about the correct input to each box so that student iput can be verified, and is formatted as follows. Each box is specified by "line_number-box_numer-answer" and these parts are separated by colons. Thus, to specify that box(1,1) = 1, box (1,2) = 2 and box(2,1) = 3, this string would be "1-1-1-:1-2-2:2-1-3". These strings are generated in `example_generation.py`.
    This is handled by calling the showStructure(numerator, denominator, tutorial?, all-answers) function, where tutorial is true if it was in the instruction. If answers are provided, the string is parsed and a matrix called rBoxAnswers is filled to store them, so they may be used to verify student answers by looping through the boxes and provided answers.
    
`FILLSTRUCTURE` this instruction fills in boxes in the box structure but also comes in different variantions 
    `FILLSTRUCTURE;` with no other information provided, this will fill in the answers to all of the boxes that are enabled. This is used to fill in the next step in a tutorial if a student has gotten the answer incorrect enough times. The answers that are used to fill in the boxes are those that are stored in rBoxAnswers. This message should only be received in this form during a tutorial, so after a message providing the answers for this problem has been sent. This is handled by fillInBoxes(void)
    `FILLSTRUCTURE;Steps to fill in;` will fill in the indicated boxes with the provided answer. This string is formatted the same was as All-Answers above, except that it only specifies the boxes that are relevant to the current step. This message is used in worked examples. The answers string is parsed and the answers used to fill in the boxes. This is handled by fillInBoxes(String message_text)
    `FILLSTRUCTURE;EASY;part-answer;` will fill in the box of the easy tutorial corresponding to the given part number.

`SHOWTEXTHINT;(insert text here);` This will cause the tablet to display the string after the semi colon as plain text in the hint pane. 

`SHOWEASYTUTORIAL;numerator-denominator` will cause the tablet to display the easy tutorial visuals, which include denominator many boxes with numerator many balls split between them. this is handled by StartEasyTutorial(), which does some calculations to determine how many rows of boxes and balls are necessary and then generates them.

#### tablet app to node_tablet messages

The tablet sends messages to the node_tablet server as TCP string messages. 
The following types of messages are sent:

`START` This indicates that the session has started. This message can later also contain data about the session that is provided on the start screen, after the semi colons, depending on what information needs to be recorded at the start of a session. node_tablet will also pass this information on to the model and robot, causing the Nao to introduce himself.

`SHOWING-QUESTION` is sent when a question is shown (either because the session was just started or because the student hit the "Next Qustion" button. This allows the robot to wait to read the question until it is on the screen.

`CA` indicates the student entered a correct answer and is sent after they hit the "Submit" button.

`IA` indicates the student entered an incorrect answer and is sent after they hit the "Submit" button.

`TICTACTOE` these messages control the tictactoe game. This message flow is the same as in previous versions of the app, so I will not document it here. All of the computation is handled by the tablet in TicTacToeActivity.
  
`TUTORIAL-STEP;result` messages indicate progress on an interactive tutorial. `result` can be the string "CORRECT", "INCORRECT" or "INCOMPLETE". If the first part of the message is actually "TUTORIAL-STEP-EASY" it refers to an easy (level 1) tutorial. These are sent after the user hits the "Check Answers" button in a tutorial. INCOMPLETE is sent if any box in the step is empty. If all are filled, INCORRECT is sent if any are wrong.

### Examples and Tutorials

#### Showing structure
The showstructure(...) function displays the boxes for the given problem. It computes the number of necessary boxes purely based on the number of digits in the numerator and denominator. The denominator length determines the number of boxes per level and the numerator length determines how many levels there will be, as in each step one digit is pulled from the numerator. If there are too many boxes, they must be scaled down to fit. The boxes are always present and pre-intialized to make answer verification and box filling easier. If they are not needed, they are hidden.

#### Verifying tutorial answers
When a user clicks "Check Answers" in a tutorial, the app loops through the answers they have provided, comparing them to the answers given by the server and hightlights any correct answers in green and any incorrect answers in red. It also sends a message to the server indicating if the attempt was successful.

#### Completing parts of problems
The fillInBoxes() functions are used to complete parts of the displayed problem for a student, whether during a hint, tutorial or example. In a tutorial, the function is called without parameters and all of the boxes that are currently enabled (i.e. all of the boxes that are in the current step) are filled in with previously provided answers, which are stored in rBoxAnswers. In an example or a hint, the message much include a string specifying which boxes to fill in with which answers. Then, that string is parsed and the boxes are filled in. 

## Connection to server
The connection to the server is handled in the same way as in previous versions of the app via the TCPClient and TCPClientOwner classes.
The activity which is to receive messages from the server must implement the TCPClientOwner class, and control of the TCPClient instance must be transferred to in when the activity starts up.

More information on this class can be found [here](https://github.com/ScazLab/nao_tutoring/blob/master/README.md#implementing-the-tcpclientowner-interface)


## Installation Instructions: 

The following instructions are taken directly from [this previous project's README] (https://github.com/ScazLab/nao_tutoring/blob/master/README.md#installation-instructions-android-app) and were written by Arsalan Sufi. 
They explain how to get the project's Android app compiling and running.

### Installing Android Studio and the Android SDK

To get the project's tablet app compiling on your machine, first install [Android Studio](http://developer.android.com/sdk/index.html).

Once you've installed Android Studio, use it to install the Android 21 SDK:

1. If you're at the Android Studio welcome menu, navigate to *Configure > SDK Manager*.
   If you're inside a project, navigate to *Tools > Android > SDK Manager*.
2. Check **Android 5.0 (Lollipop)** (Android 21) and hit *Apply*.

Android 21 is the target SDK for the project as noted in `AndroidManifest.xml`.
You can install other Android SDKs as well.
For example, you may want to install Android 16, which is the minimum SDK that the project supports.
This is also noted in `AndroidManifest.xml`.

### Importing the Project

If you're at the welcome menu, navigate to *Check out project from Version Control > Git*.
If you're inside a project, navigate to *File > New > Project from Version Control > Git*.
After you hit *Clone*, you may or may not be given the following option:
*Would you like to create a Studio project for the sources you have checked out to...?*

**If you are given this option...**

1. Click *Yes*.
2. Hit *Next* repeatedly until you see the screen that asks you to select a project SDK.
3. If **Android 21** isn't included in the list of options,
   1. Click the plus and then *Android SDK*.
   2. Navigate to your SDK directory and hit *OK*.
      On a Mac, your SDK directory is probably `/Users/{username}/Library/Android/sdk/`.
4. You should now be able to select **Android 21**.
5. Hit *Next* and then *Finish*.

**If you aren't given this option...**

1. Navigate to *File > Project Structure...*.
2. Click the *Project* tab on the left.
3. If **Android 21** isn't included in the list of options for *Project SDK*,
   1. Click *New...* and then *Android SDK*.
   2. Navigate to your SDK directory and hit *OK*.
      On a Mac, your SDK directory is probably `/Users/{username}/Library/Android/sdk/`.
4. You should now be able to select **Android 21**.
5. For the *Project language level*, select any value greater than or equal to **6**.
6. For the *Project compiler output*, `/path/to/repo/out/` is a good option.
7. Now click the *Modules* tab on the left.
8. Even if **nao_tutoring** is already listed as a module, click on it and hit the minus to remove it.
   We're going to re-add it; this forces Android Studio to do some indexing.
9. Click the plus and then *Import Module*.
10. Navigate to the repo and hit *OK*.
11. Hit *Next* repeatedly and eventually *Finish*.
12. Click *Apply*.

### Compiling and Running the App

You should now be able to compile and run the app!
To do this, click the green run arrow.
A pop-up menu will ask you to select a deployment target.
You can run the app on an actual tablet or in an emulator. 
To run the app on a tablet, plug the tablet into your laptop and make sure that your laptop recognizes it.
The tablet should show up under the list of *Connected Devices*.
If you don't have access to a tablet, you can use the *Create New Emulator* option.
To manage your emulators, use *Tools > Android > AVD Manager*.
