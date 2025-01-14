package juloo.keyboard2;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import java.util.Timer;
import java.util.TimerTask;


public class OverlayService extends Service {

    private WindowManager windowManager;
    private View overlayView;

    public OverlayService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT);

        //setup the temporary button that the user can use to launch the soft keyboard
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_button, null);
        layoutParams.gravity =  Gravity.TOP | Gravity.START;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManager.addView(overlayView, layoutParams);

        //handle the show keyboard button
        overlayView.findViewById(R.id.invokeKeyboardButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Get the existing LayoutParams
                WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) overlayView.getLayoutParams();
                layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
                windowManager.updateViewLayout(overlayView, layoutParams);

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {

                        overlayView.requestFocus();
                        //force the keyboard to display, if possible
                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) overlayView.getLayoutParams();
                                layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                                windowManager.updateViewLayout(overlayView, layoutParams);
                            }
                        });

                    }
                }, 200);

            }
        });

        //handle the X button to close overlay and keyboard
        overlayView.findViewById(R.id.closeOverlayButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //if the keyboard is currently show, dismiss it
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(overlayView.getWindowToken(), 0);

                stopSelf(); //kill the overlay so it doesn't c continue interfering with key input
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (overlayView != null) {
            windowManager.removeView(overlayView);
        }
    }
}