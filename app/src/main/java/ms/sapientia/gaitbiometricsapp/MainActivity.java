package ms.sapientia.gaitbiometricsapp;

import android.app.ProgressDialog;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ms.sapientia.gaitbiometricsapp.utils.classes.Accelerometer;
import ms.sapientia.gaitbiometricsapp.utils.classes.Util;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SensorEventListener{

    private final String TAG = "MainActivity";
    private Button startRecButton;
    private Button stopRecButton;
    private Button saveRecordedButton;
    private TextView accelerometerTextView;
    private TextView recordInfoTextView;
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private SensorEventListener accelerometerEventListener;
    private int sensorDelay = SensorManager.SENSOR_DELAY_FASTEST;
    // Recording:
    private ArrayList<Accelerometer> accelerometerArrayList = new ArrayList<Accelerometer>();
    private boolean isRecording = false;
    private int recordCount = 0;
    private int stepNumber = 0;
    private String rawDataStr = "";
    // Graph
    private final Handler mHandler = new Handler();
    private Runnable mTimer;
    private LineGraphSeries<DataPoint> mSeries = new LineGraphSeries<DataPoint>();
    private double graph2LastXValue = 5d;
    private GraphView graph;
    private int mMaxDataPoint = 100;
    DataPoint[] dpl = new DataPoint[mMaxDataPoint];
    private boolean firstIteration = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findAllView();

        initiateInternalFiles();

        initGraphView();


        // Setup starting interface:
        accelerometerTextView.setText("");
        recordInfoTextView.setText("Press \"Start\" to start recording.");

        /*
         *  Setting up: Toolbar, FloatingActionButton, Navigation View,...
         */

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        /*
         *  Sensor
         */

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometerSensor == null) {
            Toast.makeText(this, "The device has no Accelerometer !", Toast.LENGTH_LONG).show();
            //new Handler().postDelayed(()-> finish(), 3000); // TODO LATER
        }

        /*
        for(int c = 0; c< mMaxDataPoint; ++c){
            dpl[c] = new DataPoint(c,0);
        }

        */
        accelerometerEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                long timeStamp = event.timestamp;
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                String x2= String.format(java.util.Locale.US,"%.2f", x);
                String y2= String.format(java.util.Locale.US,"%.2f", y);
                String z2= String.format(java.util.Locale.US,"%.2f", z);
                if (isRecording) {
                    accelerometerArrayList.add(new Accelerometer(timeStamp, x, y, z, stepNumber));
                    accelerometerTextView.setText("X: " +x2+ "\nY: " +y2+ "\nZ: " +z2);
                    recordInfoTextView.setText("Current step count: " + stepNumber);
                    //textViewStatus.setText(("Recording: " + stepNumber + " steps made."));

                    /*
                    // Graph

                    int size = accelerometerArrayList.size();
                    List<Accelerometer> tail = accelerometerArrayList.subList(Math.max(size - mMaxDataPoint, 0), size);
                    double yy;

                    int rC = recordCount % mMaxDataPoint;


                    yy = (x+y+z)/3;

                    Log.i("TAG","dpl["+rC+"] = new DataPoint("+rC+","+yy+");" );
                    Log.i("TAG","LENGTH:" + dpl.length);

                    DataPoint[] dpl2 = new DataPoint[mMaxDataPoint];

                    for(int c=0; c<mMaxDataPoint; ++c){
                        if(c!=rC){
                            dpl2[c] = new DataPoint(dpl[c].getX(),dpl[c].getY());
                        }else{
                            dpl2[rC] = new DataPoint(dpl[c].getX(),yy);
                        }
                    }

                    dpl = dpl2;


                    //Log.i("TAG","dpl["+rC+"]= " + dpl[rC].toString() );






                    if( firstIteration ){
                        mSeries.appendData( dpl[rC], true, mMaxDataPoint, true);
                        firstIteration = false;
                    }else{
                        mSeries.resetData( dpl );
                    }


                    graph.addSeries(mSeries);
                    */
                    recordCount++;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        /*
         *  Setting Record & Save click listeners
         */

        startRecButton.setOnClickListener(startRecClickListener);
        stopRecButton.setOnClickListener(stopRecClickListener);
        saveRecordedButton.setOnClickListener(saveRecordClickListener);



    }

    private void initGraphView() {
        GraphView graph = (GraphView) findViewById(R.id.graph);
    }

    private void initiateInternalFiles() {

        // Internal Saving Location for ALL hidden files:
        Util.internalFilesRoot = new File(getFilesDir().toString());
        Log.i(TAG, "Util.internalFilesRoot.getAbsolutePath() = " + Util.internalFilesRoot.getAbsolutePath());

        Util.customDIR = "";

        // locate files:
        Util.feature_dummy_path = Util.internalFilesRoot.getAbsolutePath() + Util.customDIR + "/feature_dummy.arff";
        Util.rawdata_user_path = Util.internalFilesRoot.getAbsolutePath() + Util.customDIR + "/rawdata.csv";
        Util.feature_user_path = Util.internalFilesRoot.getAbsolutePath() + Util.customDIR + "/feature.arff";
        Util.model_user_path = Util.internalFilesRoot.getAbsolutePath() + Util.customDIR + "/model.mdl";
        //region Print this 4 paths
        Log.i(TAG, "PATH: Util.feature_dummy_path = " + Util.feature_dummy_path);
        Log.i(TAG, "PATH: Util.rawdata_user_path  = " + Util.rawdata_user_path);
        Log.i(TAG, "PATH: Util.feature_user_path  = " + Util.feature_user_path);
        Log.i(TAG, "PATH: Util.model_user_path    = " + Util.model_user_path);
        //endregion

        // internal files as File type:
        Util.featureDummyFile = new File(Util.feature_dummy_path);
        Util.rawdataUserFile = new File(Util.rawdata_user_path);
        Util.featureUserFile = new File(Util.feature_user_path);
        Util.modelUserFile = new File(Util.model_user_path);

        // if files are not exists => create them:
        if ( ! Util.featureDummyFile.exists()) {
            try {
                Util.featureDummyFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "File can't be created: " + Util.feature_dummy_path);
            }
        }
        if ( ! Util.rawdataUserFile.exists()) {
            try {
                Util.rawdataUserFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "File can't be created: " + Util.rawdata_user_path);
            }
        }
        if ( ! Util.featureUserFile.exists()) {
            try {
                Util.featureUserFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "File can't be created: " + Util.feature_user_path);
            }
        }
        if ( ! Util.modelUserFile.exists()) {
            try {
                Util.modelUserFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "File can't be created: " + Util.model_user_path);
            }
        }
    }

    private void findAllView() {
        startRecButton = findViewById(R.id.startRecButton);
        stopRecButton = findViewById(R.id.stopRecButton);
        saveRecordedButton = findViewById(R.id.saveRecordedButton);
        accelerometerTextView = findViewById(R.id.accelerometerTextView);
        recordInfoTextView = findViewById(R.id.recordInfoTextView);
        graph = (GraphView) findViewById(R.id.graph);
    }

    @Override
    protected void onResume(){
        super.onResume();
        sensorManager.registerListener(accelerometerEventListener, accelerometerSensor, sensorDelay);
    }

    @Override
    public void onPause(){
        mHandler.removeCallbacks(mTimer);
        super.onPause();
    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    /**
     *
     */
    private View.OnClickListener startRecClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //Snackbar.make(findViewById(R.id.app_bar_main_id),"Recording Started",Snackbar.LENGTH_SHORT).show();
            Toast.makeText(MainActivity.this, "Recording Started", Toast.LENGTH_SHORT).show();
            sensorManager.registerListener(MainActivity.this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
            isRecording = true;
            recordCount = 0;
            stepNumber = 0;
            accelerometerArrayList.clear();
            startRecButton.setEnabled(false);
            stopRecButton.setEnabled(true);
            saveRecordedButton.setEnabled(false);
        }
    };


    /**
     *
     */
    private View.OnClickListener stopRecClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //Snackbar.make(findViewById(R.id.app_bar_main_id),"Recording Finished",Snackbar.LENGTH_SHORT).show();
            Toast.makeText(MainActivity.this, "Recording Finished", Toast.LENGTH_SHORT).show();
            isRecording = false;
            startRecButton.setEnabled(true);
            stopRecButton.setEnabled(false);
            saveRecordedButton.setEnabled(true);
            sensorManager.unregisterListener(MainActivity.this);
            rawDataStr = Util.accelerometerArrayListToString(accelerometerArrayList);
            rawDataStr += ",end";
            accelerometerTextView.setText("");

            // Saving raw data with header into internal storage
            Util.rawDataHasHeader = true;
            Util.saveAccelerometerArrayIntoFile(accelerometerArrayList, Util.rawdataUserFile);
        }
    };


    /**
     *
     */
    private View.OnClickListener saveRecordClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //Snackbar.make(findViewById(R.id.app_bar_main_id),"Saving records",Snackbar.LENGTH_SHORT).show();
            Toast.makeText(MainActivity.this, "Saving records", Toast.LENGTH_SHORT).show();

            Util.progressDialog = new ProgressDialog( MainActivity.this, ProgressDialog.STYLE_SPINNER);
            Util.progressDialog.setTitle("TITLE");
            Util.progressDialog.setMessage("Saving records.");
            Util.progressDialog.show();

            GenerateModel();

            //Snackbar.make(findViewById(R.id.app_bar_main_id),"Records Saved",Snackbar.LENGTH_SHORT).show();
            Toast.makeText(MainActivity.this, "Records Saved", Toast.LENGTH_SHORT).show();
        }
    };

    /**
     *
     */
    private void GenerateModel(){



        // TODO



        Util.progressDialog.dismiss();
    }

    //
    // SensorEventListener:
    //


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            //simpleStepDetector.updateAccel(
            //        event.timestamp, event.values[0], event.values[1], event.values[2]);
        }
    }


}
