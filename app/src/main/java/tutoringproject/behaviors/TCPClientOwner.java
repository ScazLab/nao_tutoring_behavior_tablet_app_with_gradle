package tutoringproject.behaviors;

import android.os.Looper;

/**
 * Created by Aleksandra Zakrzewska on 9/14/17.
 */

public interface TCPClientOwner {
    Looper getMainLooper();
    void disableButtons();
    void messageReceived(String message);
}
