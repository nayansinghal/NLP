import edu.stanford.nlp.parser.nndep.DependencyParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.io.BufferedWriter;
import java.io.Writer;
import java.io.FileWriter;

/**
 *  
 * Created by nayans on 3/20/17.
 */

public class UnsupervisedDomainAdaption {

    /* Count sentences in a file */
    protected static int countSentences(BufferedReader reader){
        String line = null;
        int countSent = 0;
        try {
            do {
                line = reader.readLine();
                if (line == null) {
                    reader.close();
                    return countSent;
                }

                if(line.length() == 0)
                    countSent++;
            
            } while (line != null);
        }
        catch (IOException e) {
            System.out.println("\nCould not read from TextFileDocument: ");
            System.exit(1);
        }
        return countSent;
    }

    protected static void countSentences(String filename){
        try{
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            System.out.println(filename + " contains sentences: " + String.valueOf(countSentences(reader)));
        }
        catch (IOException e) {
            System.out.println("\nCould not open POSTaggedFile: " + filename);
            System.exit(1);
        }
    }

    protected void getNextPOSLine(BufferedReader reader, BufferedWriter writer, int size) {
        String line = null;
        int count = 0;
        try {
            do {
                // Read a line from the file
                line = reader.readLine();
                if (line == null) {
                    // End of file, no more tokens, return null
                    reader.close();
                    return;
                }
                
                writer.write(line + '\n');

                if(line.length() == 0){
                    count++;
                    if(count == size)
                        return;
                }

            } while (line != null);
        }
        catch (IOException e) {
            System.out.println("\nCould not read from TextFileDocument: ");
            e.printStackTrace();
            System.exit(1);
        }
        return;
    }

    protected void data(String readFilename, String writeFileName, int count, Boolean merge){

        try{
            BufferedReader reader = new BufferedReader(new FileReader(readFilename));
            BufferedWriter writer = new BufferedWriter(new FileWriter(writeFileName, merge));
            getNextPOSLine(reader, writer, count);
            reader.close();
            writer.close();
        }
        catch (IOException e) {
            System.out.println("\nCould not open POSTaggedFile: " + readFilename);
            System.out.println("\nCould not open POSTaggedFile: " + writeFileName);
            e.printStackTrace();
            System.exit(1);
        }
    }

    protected void train(String seedPath, String modelPath, String embedPath, Boolean flag) {

        Properties prop = new Properties();
        prop.setProperty("maxIter", "5");
        DependencyParser p = new DependencyParser(prop);
        
        if(flag)
            p.train(seedPath, null, modelPath, embedPath);

        else
            p.train(seedPath, null, modelPath, embedPath, modelPath);
    }

    protected void test(String testPath, String modelPath, String testAnnotationsPath) {
        // Load a saved path
        DependencyParser model = DependencyParser.loadFromModelFile(modelPath);
        
        // Test model on test data, write annotations to testAnnotationsPath
        System.out.println(model.testCoNLL(testPath, testAnnotationsPath));
    }

    /*
    args[0] = seed path
    args[1] = size of seed
    args[2] = selftrain path
    args[3] = selftrain size
    args[4] = inTestFile path
    args[5] = test data path
    args[6] = embedding file path
    args[7] = temp path for creations
    */
    public static void main(String[] args) {

        File directory = new File(String.valueOf(args[7]));
        if (! directory.exists()){
            directory.mkdir();
        }
        //  Training Data path
        String seed = args[7] +"_seed.conllx";
        String selfTrain = args[7] +"_selfTrain.conllx";
        UnsupervisedDomainAdaption obj = new UnsupervisedDomainAdaption();

        /* seed data manipulation */
        String seedPath = args[0];
        int seedSize = Integer.parseInt(args[1]);
        obj.data(seedPath, seed, seedSize, false);
        obj.countSentences(seed);
        
        /* Self Train data manipulation */
        String selfTrainPath = args[2];
        obj.countSentences(selfTrainPath);
        int selfTrainSize = Integer.parseInt(args[3]);
        obj.data(selfTrainPath, selfTrain, selfTrainSize, false);
        obj.countSentences(selfTrain);

        // Path where model is to be saved
        String modelPath = args[7] +"_model";
        String embeddingPath = args[6];
        // Path where test data annotations are stored
        String testAnnotationsPath = args[7] + "_test.conllx";

        /* Testing data manipulation */
        String inTestPath = args[4];
        obj.countSentences(inTestPath);

        /* Testing data manipulation */
        String testPath = args[5];
        obj.countSentences(testPath);

        /* Train and test seed model */
        obj.train(seed, modelPath, embeddingPath, true);
        obj.test(inTestPath, modelPath, testAnnotationsPath);
        obj.test(testPath, modelPath, testAnnotationsPath);

        /* Merge seed and Self Train data */
        obj.data(testAnnotationsPath, seed, selfTrainSize, true);
        obj.countSentences(seed);

        /* train seed + selfTrained data */
        obj.train(seed, modelPath, embeddingPath, false);
        obj.test(testPath, modelPath, testAnnotationsPath);
        
    }
}
