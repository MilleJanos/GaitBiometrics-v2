package ms.sapientia.gaitbiometricsapp.utils.classes;

import android.app.ProgressDialog;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Util {

    // Singleton
    private Util() {

    }

    private static final String TAG = "Util";

    // progressDialog
    public static ProgressDialog progressDialog;

    // stored internal files location
    public static File internalFilesRoot;
    public static String customDIR = "";    // example: "/dir1/dir2", NOT: "dir1/dir2" !

    // internal stored Paths
    public static String feature_dummy_path = "";
    public static String rawdata_user_path = "";
    public static String feature_user_path = "";
    public static String model_user_path = "";

    // internal storave files variable
    public static File featureDummyFile;
    public static File rawdataUserFile;
    public static File featureUserFile;
    public static File modelUserFile;

    // internal files header
    public static boolean rawDataHasHeader = false;
    public static String rawDataHeaderStr = "timestamp,accx,accy,accz,stepnum";

    //
    //  Common Functions:
    //
    
    /**
     * accelerometerArrayListToString()
     * | ArrayList<Accelerometer> accelerometerArrayList ==> String str
     * |
     * | output format:   "timestamp,x,y,z,currentStepCount,timestamp,x,y,z,currentStepCount,timestamp,x,y,z,timestamp,currentStepCount, ... ,end"
     *
     * @return the custom string representation of accelerometerArrayList
     * @author Mille Janos
     */
    public static String accelerometerArrayListToString(ArrayList<Accelerometer> list) {
        Log.d(TAG, ">>>RUN>>>accelerometerArrayListToString()");
        StringBuilder sb = new StringBuilder();
        int i;
        for (i = 0; i <  list.size() - 1; ++i) {
            sb.append( list.get(i).getTimeStamp())
                    .append(",")
                    .append( list.get(i).getX())
                    .append(",")
                    .append( list.get(i).getY())
                    .append(",")
                    .append( list.get(i).getZ())
                    .append(",")
                    .append( list.get(i).getStep())
                    .append(",");
        }
        sb.append( list.get(i).getTimeStamp())
                .append(",")
                .append( list.get(i).getX())
                .append(",")
                .append( list.get(i).getY())
                .append(",")
                .append( list.get(i).getZ())
                .append(",")
                .append( list.get(i).getStep());
        //.append(",");
        return sb.toString();
    }

    /**
     * This method saves the accArray<Accelerometer> list into file including header.
     *
     * @param accArray array that contains the data that will be writtem to the file
     * @param file     descriptor of the file all the writing will be made into
     * @return 0 if there is no error
     * 1 if there occurred an error
     */
    public static short saveAccelerometerArrayIntoFile(ArrayList<Accelerometer> accArray, File file) {
        String TAG = "Util";
        Log.d(TAG, ">>>RUN>>>savingAccArrayIntoCSV()");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Log.e(TAG, "IOException: file.createNewFile()");
                e.printStackTrace();
                return 1;
            }
        }

        try {
            FileOutputStream fos = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(fos);

            // Header:
            if (Util.rawDataHasHeader) {
                pw.println(Util.rawDataHeaderStr);
            }

            for (Accelerometer a : accArray) {
                pw.println(a.toString());
            }
            pw.flush();
            pw.close();
            fos.close();
        } catch (
                FileNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, "******* File not found.");
            return 1;
        } catch (IOException e) {
            e.printStackTrace();
            return 1;
        }
        Log.d(TAG, "<<<FINISH<<<savingAccArrayIntoCSV()");
        return 0;
    }


}
