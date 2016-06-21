package waveBoot.service;

import java.lang.IllegalStateException;
import java.lang.Exception;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

/**
 * Code is based on tutorials and other websites from the interwebs
 *
 * All strings (data, categories) will be converted to lower case before processing.
 */
public class CsvMLService{

    private Instances trainingData;
    private StringToWordVector filter;
    private Classifier classifier;
    private FastVector classValues;
    private FastVector attributes;
    private boolean needRebuild;
    private Instances filteredData;

    public CsvMLService(Classifier classifier){
        this(classifier, 10);
    }

    public CsvMLService(Classifier classifier, int startSize) {
        this.filter = new StringToWordVector();
        this.classifier = classifier;
        // Initial size of 2 for vector of attributes, capacity increases automatically.
        this.attributes = new FastVector(2);
        // Add attribute for data of type "text"
        this.attributes.addElement(new Attribute("text", (FastVector) null));
        // Initial size of 10 to hold categories. Capacity increases automatically.
        this.classValues = new FastVector(startSize);
    }

    public void addCategory(String category) {
        category = category.toLowerCase();
        int capacity = classValues.capacity();
        if (classValues.size() > (capacity - 5)) {
            classValues.setCapacity(capacity * 2);
        }
        classValues.addElement(category);
    }

    /**
     * Used to add training data.
     */
    public void addData(String message, String classValue) throws IllegalStateException {
        message = message.toLowerCase();
        //Category of the training data
        classValue = classValue.toLowerCase();
        Instance instance = createInstance(message, trainingData);
        //Set the category of the training data message
        instance.setClassValue(classValue);
        // Add training data to training data set
        trainingData.add(instance);
        needRebuild = true;
    }

    /**
     * Make sure that classifier and filter are up to date, build if necessary.
     */
    private void buildafterTrainingComplete() throws Exception {
        if (needRebuild) {
            filter.setInputFormat(trainingData);
            filteredData = Filter.useFilter(trainingData, filter);
            classifier.buildClassifier(filteredData);
            needRebuild = false;
        }
    }

    /**
     * Performs actual machine learning calculations, and returns a vector
     * of probability values that represent the categories and the confidence
     * values.
     */
    public double[] classifyMessage(String message) throws Exception {
        message = message.toLowerCase();
        if (trainingData.numInstances() == 0) {
            throw new Exception("No classifier available.");
        }
        buildafterTrainingComplete();
        Instances testset = trainingData.stringFreeStructure();
        Instance testInstance = createInstance(message, testset);

        filter.input(testInstance);
        Instance filteredInstance = filter.output();
        return classifier.distributionForInstance(filteredInstance);
    }

    private Instance createInstance(String text, Instances data) {
        Instance instance = new Instance(2);
        Attribute messageAtt = data.attribute("text");
        instance.setValue(messageAtt, messageAtt.addStringValue(text));
        instance.setDataset(data);
        return instance;
    }

    /** 
     * Must be called after adding all training data
     * 
     * Performs actual training of data
     */
    public void setupAfterCategorysAdded() {
        attributes.addElement(new Attribute("class", classValues));
        trainingData = new Instances("MessageClassificationProblem", attributes, 100);
        trainingData.setClassIndex(trainingData.numAttributes() - 1);
    }
}
