package com.example.defenselabs.airspaceinterdict;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Created by DefenseLabs on 25-07-2016.
 */

public class SettingsDialog extends Dialog {

    Context context;
    File path,file;
    EditText ip_add, port_no;
    Button btn_save;
    String ip_address="10.0.1.18", port="4444";

    public SettingsDialog(Context context) {
        super(context);
        this.context = context;
        path = context.getFilesDir();
        file = new File(path, "IP_PORT.txt");
        if(file.exists())
        {
            String res = readFromFile();
            String[] arr = res.split(";");
            ip_address=arr[0];
            port=arr[1];
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.settings_dialog);
       // RelativeLayout rl = (RelativeLayout) findViewById(R.id.RL);
        findViewById(R.id.RL).getBackground().setAlpha(250);
        btn_save = (Button) findViewById(R.id.btn_save);
        ip_add = (EditText) findViewById(R.id.txt_ip);
        ip_add.setText(ip_address);
        port_no = (EditText) findViewById(R.id.txt_port);
        port_no.setText(port);
        ip_address = ip_add.getText().toString();
        port = port_no.getText().toString();
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ip_address = ip_add.getText().toString();
                port = port_no.getText().toString();
                writeToFile(ip_address,port);
                dismiss();
            }
        });


    }

    public String getIP()
    {
        return ip_address;
    }
    public String getPort()
    {
        return port;
    }

    private void writeToFile(String ip, String port) {
        try {
            PrintWriter writer = new PrintWriter(file);
            writer.print(ip+";"+port);
            writer.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private String readFromFile(){


        int length = (int) file.length();

        byte[] bytes = new byte[length];

        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            in.read(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String contents = new String(bytes);


        return contents;
    }
}
