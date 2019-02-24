package ms.sapientia.modelbuilder;

//package ro.sapientia.gaitbiom;

import weka.classifiers.Classifier;
import weka.core.Attribute;

import java.util.ArrayList;

public interface IGaitModelBuilder {
    public Classifier createModel(String arffFile);
    public ArrayList<Attribute> getAttributes(String arffFile);
}