package com.example.defenselabs.airspaceinterdict;

import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

public class ViewCamera extends AppCompatActivity {

    Camera camera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    Button start, stop, capture;

    public ViewCamera() {
        Log.i("TAG", "Instantiated new " + this.getClass());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_camera);
        start = (Button)findViewById(R.id.btn_start);

    }


}
