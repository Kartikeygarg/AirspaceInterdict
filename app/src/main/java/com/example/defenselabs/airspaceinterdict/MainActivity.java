package com.example.defenselabs.airspaceinterdict;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.ColorRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    ImageButton shoot, connect;
    long touchMillis = 0L;
    int width = 640, height=480;
    ImageView logo,img_pan , img_lock ;
    boolean pan=true , lock = false;
    Animation scale,translate, fadein, fadeout, zoom_in_out, click_anim; AnimationSet animSet;
    TextView text_connect;
    public ImageView bmImage;
    byte[] dataP;
    Mat mat;
    int x=0, y=0;
    Rect touchedRect;
    private int zoomWindowZize=9;
    public Context applicationContext;
    Socket socket;
    Handler handel;
    Runnable run;
    OutputStream out;
    GestureDetector gdt;
    byte fire_array[];
    byte move_array[];
    byte ptxy_array[];
    ImageButton btn_setings ;
    SettingsDialog sd;
    int xPer,yPer,  heightPer , widthPer;


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("OPENCV Opened ", "OpenCV loaded successfully");
                    mat=new Mat(height,width, CvType.CV_8UC3);

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      //  setContentView(R.layout.activity_main);
        setAnimations();
        applicationContext = getApplicationContext();

    }

    public void onResume()
    {
        super.onResume();
        setContentView(R.layout.activity_main);
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
            fire_array = new byte[20];
            move_array = new byte[20];
            ptxy_array = new byte[28];
            byte[] Magic = toByteArray("AndC");
            System.arraycopy(toByteArray("AndC"),0,fire_array,0,4);
            System.arraycopy(toByteArray("AndC"),0,move_array,0,4);
            System.arraycopy(toByteArray("AndC"),0,ptxy_array,0,4);

            System.arraycopy(ByteBuffer.allocate(4).putInt(4).array(),0,fire_array,4,4);
            System.arraycopy(ByteBuffer.allocate(4).putInt(12).array(),0,move_array,4,4);
            System.arraycopy(ByteBuffer.allocate(4).putInt(20).array(),0,ptxy_array,4,4);



            System.arraycopy(toByteArray("Fire"),0,fire_array,8,4);
            System.arraycopy(toByteArray("Move"),0,move_array,8,4);
            System.arraycopy(toByteArray("PtXY"),0,ptxy_array,8,4);

            shoot =(ImageButton) findViewById(R.id.btn_shoot);
            logo = (ImageView) findViewById(R.id.logo);
            img_pan = (ImageView) findViewById(R.id.img_pan);
            img_pan.setAnimation(click_anim);
            img_lock = (ImageView) findViewById(R.id.img_lock);
            connect =(ImageButton) findViewById(R.id.btn_connect); connect.setAnimation(zoom_in_out);
            text_connect = (TextView) findViewById(R.id.text_connect) ; text_connect.setAnimation(fadein);
            bmImage = (ImageView) findViewById(R.id.imgs);
            btn_setings = (ImageButton) findViewById(R.id.btn_setings);
            btn_setings.getBackground().setAlpha(120);
            img_lock.getBackground().setAlpha(130);
            sd= new SettingsDialog(MainActivity.this);

        img_pan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(pan)
                {
                    img_pan.setAlpha((float) 0.3);
                    img_pan.clearAnimation();
                    pan=false;
                }
                else{
                    img_pan.setAlpha((float) 0.8);
                    img_pan.startAnimation(click_anim);
                    pan=true;
                }

            }
        });
          /*  img_lock.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                     shoot.setVisibility(View.VISIBLE);
                        img_lock.setVisibility(View.GONE);
                        lock=true;
                        System.arraycopy(ByteBuffer.allocate(4).putInt(xPer).array(),0,ptxy_array,12,4);
                        System.arraycopy(ByteBuffer.allocate(4).putInt(yPer).array(),0,ptxy_array,16,4);
                        System.arraycopy(ByteBuffer.allocate(4).putInt(widthPer).array(),0,ptxy_array,20,4);
                        System.arraycopy(ByteBuffer.allocate(4).putInt(heightPer).array(),0,ptxy_array,24,4);

                        try {
                            out.write(ptxy_array,0,ptxy_array.length);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                });*/

            btn_setings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    sd.show();
                }
            });

            run = new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),"SHOOT", Toast.LENGTH_SHORT).show();
                    shoot.setVisibility(View.GONE);

                    try {
                        out.write(fire_array,0,fire_array.length);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //   byte[] bytes = message.getBytes(Charset.forName("ISO-8859-1"));
                }
            };

         handel = new Handler();
        gdt = new GestureDetector(new GestureListener());

            shoot.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {

                    switch(motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            shoot.setImageResource(R.drawable.shotzoom);;
                            handel.postDelayed(run, 1500);
                            return true;

                        case MotionEvent.ACTION_UP:
                            shoot.clearAnimation();
                            shoot.setImageResource(R.drawable.shoot);
                            handel.removeCallbacks(run);
                            break;

                       /* default:
                            handel.removeCallbacks(run);
                            break;*/

                    }
                    return false;
                }});



            connect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(sd.getIP()=="" || sd.getPort() == "" || sd.getIP() == null || sd.getPort() == null)
                    {
                        sd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        sd.show();
                        return;
                    }

                    btn_setings.setVisibility(View.GONE);
                    logo.startAnimation(animSet);
                    bmImage.setVisibility(View.VISIBLE);
                    img_pan.setVisibility(View.VISIBLE);
                    bmImage.setOnTouchListener(MainActivity.this);

                    connect.clearAnimation();
                    connect.setAnimation(fadeout);
                    connect.setVisibility(View.GONE);
                    text_connect.setAnimation(fadeout);
                    text_connect.setVisibility(View.GONE);
                    MyClientTask myClientTask = new MyClientTask();
                    myClientTask.execute();



                }
            });
    }

    public void onPause()
    {
        super.onPause();
        if(socket != null){
            try {
                socket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }


    }


    void setAnimations()
    {
        animSet = new AnimationSet(true); animSet.setFillEnabled(true);
        animSet.setFillAfter(true);
        scale = AnimationUtils.loadAnimation(getBaseContext(),R.anim.scale);
        translate = AnimationUtils.loadAnimation(getBaseContext(),R.anim.translate);
        fadein = AnimationUtils.loadAnimation(getBaseContext(),R.anim.fadein);
        fadeout = AnimationUtils.loadAnimation(getBaseContext(),R.anim.fadeout);

        zoom_in_out =new ScaleAnimation(1f,1.1f,1f,1.1f ,Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        zoom_in_out.setRepeatMode(Animation.REVERSE);
        zoom_in_out.setRepeatCount(-1);
        zoom_in_out.setDuration(900);
        zoom_in_out.setInterpolator(new AccelerateInterpolator());

        click_anim =new ScaleAnimation(1f,1.1f,1f,1.1f ,Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        click_anim.setRepeatMode(Animation.REVERSE);
        click_anim.setRepeatCount(-1);
        click_anim.setDuration(700);
        click_anim.setInterpolator(new AccelerateInterpolator());



        animSet.addAnimation(scale);
        animSet.addAnimation(translate);


    }

    byte[] toByteArray(String value) {
        return new byte[] {
                (byte)(value.charAt(0) ),
                (byte)(value.charAt(1) ),
                (byte)(value.charAt(2)),
                (byte)value.charAt(3)   };
    }




    public void draw(byte[] colors)
    {

        dataP=colors;

        runOnUiThread(new Runnable() //run on ui thread
        {
            public void run()
            {
                long millis = System.currentTimeMillis();
                mat.put(0, 0, dataP);
                Bitmap bm = Bitmap.createBitmap(mat.cols(), mat.rows(),Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(mat, bm);
                bmImage.setImageBitmap(bm);
                long diff = System.currentTimeMillis()- millis;
              //  Log.i("DIFF4 ",""+ diff );

            }
        });

    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
            gdt.onTouchEvent(event);
            return true;
    }


    private static final int SWIPE_MIN_DISTANCE = 70;
    private static final int SWIPE_THRESHOLD_VELOCITY = 30;

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent event)
        {
            int xPos = (int)event.getX();
            int yPos = (int)event.getY();
            int xPer =( xPos *100) / (bmImage.getWidth() );
            int yPer = (yPos *100 ) / bmImage.getHeight();
            Log.i("TOUCH", "X: "+ xPos+ " Y: "+yPos +" xPER: "+xPer+" yPer: "+yPer);
            if(!pan)
            {
                shoot.setVisibility(View.VISIBLE);
                img_lock.setVisibility(View.GONE);
                lock=true;
                System.arraycopy(ByteBuffer.allocate(4).putInt(xPer).array(),0,ptxy_array,12,4);
                System.arraycopy(ByteBuffer.allocate(4).putInt(yPer).array(),0,ptxy_array,16,4);
                System.arraycopy(ByteBuffer.allocate(4).putInt(10).array(),0,ptxy_array,20,4);
                System.arraycopy(ByteBuffer.allocate(4).putInt(10).array(),0,ptxy_array,24,4);

                try {
                    out.write(ptxy_array,0,ptxy_array.length);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            return true;
        }

    /*    @Override
        public boolean onDoubleTap(MotionEvent event) {
            Log.d("TOUCH ", "onDoubleTap: " + event.toString());
            onLongPress(event);
            return true;
        }



      @Override
      public void onLongPress(MotionEvent event) {
          int xPos = (int)event.getX();
          int yPos = (int)event.getY();
          int xPer =( xPos *100) / (bmImage.getWidth() );
          int yPer = (yPos *100 ) / bmImage.getHeight();
          if(xPer<0) xPer=0;
          if(xPer>=100) xPer=99;


          Log.i("TOUCH", "X: "+ xPos+ " Y: "+yPos +" xPER: "+xPer+" yPer: "+yPer);

          System.arraycopy(ByteBuffer.allocate(4).putInt(xPer).array(),0,ptxy_array,12,4);
          System.arraycopy(ByteBuffer.allocate(4).putInt(yPer).array(),0,ptxy_array,16,4);

          shoot.setVisibility(View.VISIBLE);



             try {
            out.write(ptxy_array,0,ptxy_array.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
      }
*/
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            int diffX = (int) (e1.getX() - e2.getX());
            int diffY = (int) (e1.getY() - e2.getY());
            int swipePer = 0;
            if (pan)
            {
                if (diffX > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    swipePer = (diffX * 100) / (bmImage.getWidth());
                    System.arraycopy(toByteArray("Left"), 0, move_array, 12, 4);
                    System.arraycopy(ByteBuffer.allocate(4).putInt(swipePer).array(), 0, move_array, 16, 4);

                    img_pan.setPivotX(img_pan.getWidth() / 2);
                    img_pan.setPivotY(img_pan.getHeight() / 2);
                    img_pan.setRotation(img_pan.getRotation() - swipePer);
                    Log.i("Swipe Left : ", "" + swipePer);

                    // Right to left
                } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    diffX *= -1;
                    swipePer = (diffX * 100) / (bmImage.getWidth());
                    System.arraycopy(toByteArray("Rght"), 0, move_array, 12, 4);
                    System.arraycopy(ByteBuffer.allocate(4).putInt(swipePer).array(), 0, move_array, 16, 4);
                    img_pan.setPivotX(img_pan.getWidth() / 2);
                    img_pan.setPivotY(img_pan.getHeight() / 2);
                    img_pan.setRotation(img_pan.getRotation() + swipePer);
                    Log.i("Swipe Right : ", "" + swipePer);
                    // Left to right
                }


            if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                swipePer = (diffY * 100) / bmImage.getHeight();
                System.arraycopy(toByteArray("GoUp"), 0, move_array, 12, 4);
                System.arraycopy(ByteBuffer.allocate(4).putInt(swipePer).array(), 0, move_array, 16, 4);
                Log.i("Swipe UP : ", "" + swipePer);
               // Bottom to top
            } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                diffY *= -1;
                swipePer = (diffY * 100) / bmImage.getHeight();
                System.arraycopy(toByteArray("Down"), 0, move_array, 12, 4);
                System.arraycopy(ByteBuffer.allocate(4).putInt(swipePer).array(), 0, move_array, 16, 4);
                Log.i("Swipe Down : ", "" + swipePer);
                // Top to bottom
            }

                Log.i("OUT.write","Value of swipe : "+swipePer);

            if (swipePer > 0) {
                try {

                    out.write(move_array, 0, move_array.length);
                    Log.i("OUT.write","Writing PAN cmd of length "+move_array.length);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
                return false;
        }
            /*else if(!lock)
            {

               touchedRect = new Rect();
                int cols = mat.cols();
                int rows = mat.rows();
                int real_x1 = (int) (e1.getX() * cols) / bmImage.getWidth();
                int real_y1 = (int) (e1.getY() * rows) / bmImage.getHeight();
                int real_x2 = (int) (e2.getX() * cols) / bmImage.getWidth();
                int real_y2 = (int) (e2.getY() * rows) / bmImage.getHeight();

                int x1Per =(int)( e1.getX() *100) / (bmImage.getWidth() );
                int y1Per = (int)(e1.getY() *100 ) / bmImage.getHeight();
                int x2Per =(int)( e2.getX() *100) / (bmImage.getWidth() );
                int y2Per = (int)(e2.getY() *100 ) / bmImage.getHeight();
                xPer= (x1Per<x2Per)? x1Per : x2Per ;
                yPer= (y1Per<y2Per)? y1Per : y2Per ;
                widthPer = Math.abs(x1Per-x2Per);
                heightPer = Math.abs(y1Per-y2Per);


                Mat duplicate = mat.clone();
                Imgproc.rectangle(duplicate, new Point(real_x1,real_y1) ,new Point(real_x2,real_y2),new Scalar(0,0,255));
                Bitmap bm = Bitmap.createBitmap(mat.cols(), mat.rows(),Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(duplicate, bm);
                bmImage.setImageBitmap(bm);
                img_pan.clearAnimation();
                img_pan.setVisibility(View.GONE);
                img_lock.setX(e1.getX()+20);
                img_lock.setY(e1.getY());
                img_lock.setVisibility(View.VISIBLE);

              //  shoot.setVisibility(View.VISIBLE);
            }*/
            return false;
        }
    }


    public class MyClientTask extends AsyncTask<Void, Void, Void> {

        String dstAddress = sd.getIP();
        int dstPort = Integer.parseInt(sd.getPort());
        String response = "";


        @Override
        protected Void doInBackground(Void... voids) {

            socket = null;

            try {
                Log.i("TRYING","IP "+dstAddress+", Port: "+dstPort);
                socket = new Socket(dstAddress, dstPort);
                Log.i("Connected","IP "+dstAddress+", Port: "+dstPort);

                runOnUiThread(new Runnable() //run on ui thread
                {
                    public void run() {
//                      bmImage.setOnTouchListener(MainActivity.this);
                    }
                });
                byte[] input = new byte[width*height*3];

              out = socket.getOutputStream();
               /*  //String str = "Android Command";
              //  byte[] array = str.getBytes();

                byte[] array = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(26).array();
                Log.i("Array length", "" + array.length);
                out.write(array ,0 , array.length);*/


               /* PrintStream PS =new PrintStream(socket.getOutputStream());
                PS.println("Fire");*/
                InputStream in = socket.getInputStream();
                for(int i=0;i>-1;i++) {
                    long mill = System.currentTimeMillis();
                    IOUtils.read(in, input);
                    draw(input);
                 //   Log.i("DIFF Frame"+i, ""+(System.currentTimeMillis()-mill));

                }
                response = "";
                Log.i("DONE", "DONE");
            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                Log.i("ERROR 1","@");
             e.printStackTrace();
                response = "UnknownHostException: " + e.toString();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.i("ERROR 2","@ "+e.toString());
                e.printStackTrace();
                response = "IOException: " + e.toString();
            }  finally{
                if(socket != null){
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }
    }








}
