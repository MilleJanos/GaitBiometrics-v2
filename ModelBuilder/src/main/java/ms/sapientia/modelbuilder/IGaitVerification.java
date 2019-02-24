package ms.sapientia.modelbuilder;

//package ro.sapientia.gaitbiom;

import FeatureExtractorLibrary.Accelerometer;
import FeatureExtractorLibrary.IUtil;
import weka.classifiers.Classifier;
import weka.core.Attribute;

import java.util.ArrayList;

public interface IGaitVerification {
    public double verifyUser(Classifier classifier, ArrayList<Attribute> attributes, ArrayList<Accelerometer> rawdata, String userName);
    public double verifyUser( Classifier classifier, ArrayList<Attribute> attributes, String rawdata_file);
}