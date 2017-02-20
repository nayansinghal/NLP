package nlp.lm;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by ns28844 on 2/10/17.
 * A simple Bidirectional Bidirectional bigram language model that uses simple fixed-weight interpolation
 * with a forward and backward model.
 */
public class BidirectionalBigramModel {
    /** Forward language model */
    private BigramModel forward;

    /** Backward language model */
    private BigramModel backward;

    /** Forward language model Weight*/
    private double fLambda;

    /** Backward language model Weight*/
    private double bLambda;

    /** Initialize model with empty forward and backward model */
    public BidirectionalBigramModel(){
        forward = new BigramModel(BigramModel.Model.Forward);
        backward = new BigramModel(BigramModel.Model.Backward);
        fLambda = 0.5;
        bLambda = 0.5;
    }

    /** Train the model on a List of sentences using forward
     *  and backward model */
    public void train (List<List<String>> sentences) {
        // Accumulate unigram and bigram counts in maps
        // for both forward and backward model
        forward.train(sentences);
        backward.train(sentences);
    }

    /** Use sentences as a test set excluding predicting end-of-sentence to evaluate the model. Print out perplexity
     *  of the model for this test data */
    public void test (List<List<String>> sentences) {
        // Compute log probability of sentence to avoid underflow
        double totalLogProb = 0;
        // Keep count of total number of tokens predicted
        double totalNumTokens = 0;
        // Accumulate log prob of all test sentences
        for (List<String> sentence : sentences) {
            // Num of tokens in sentence
            totalNumTokens += sentence.size();
            // Compute log prob of sentence
            double sentenceLogProb = sentenceLogProb(sentence);

            // Add to total log prob (since add logs to multiply probs)
            totalLogProb += sentenceLogProb;
        }
        // Given log prob compute perplexity
        double perplexity = Math.exp(-totalLogProb / totalNumTokens);
        System.out.println("Word Perplexity = " + perplexity );
    }

    /** Like sentenceLogProb but excludes predicting end-of-sentence when computing prob */
    public double sentenceLogProb (List<String> sentence) {
        /* Compute log probability of sentence for forward model */
        double[] fTokenProbs = forward.sentenceTokenProbs(sentence);
        /* Compute log probability of sentence for backward model */
        double[] bTokenProbs = backward.sentenceTokenProbs(sentence);

        double sentenceLogProb = 0;

        int len = fTokenProbs.length;
        for(int i = 0; i < len; i++ ){
            // Interpolate bigram prob
            sentenceLogProb += Math.log(interpolatedProb(fTokenProbs[i], bTokenProbs[len-i-1]));
        }

        return sentenceLogProb;
    }

    /** Interpolate bigram prob using forward and backward model predictions */
    public double interpolatedProb(double fProb, double bProb){
        // Linearly combine weighted forward and backward probs
        return fLambda * fProb + bLambda * bProb;
    }

    public static int wordCount (List<List<String>> sentences) {
        int wordCount = 0;
        for (List<String> sentence : sentences) {
            wordCount += sentence.size();
        }
        return wordCount;
    }

    /** Train and test a bigram model.
     *  Command format: "nlp.lm.BiDirectional [DIR]* [TestFrac]" where DIR
     *  is the name of a file or directory whose LDC POS Tagged files should be
     *  used for input data; and TestFrac is the fraction of the sentences
     *  in this data that should be used for testing, the rest for training.
     *  0 < TestFrac < 1
     *  Uses the last fraction of the data for testing and the first part
     *  for training.
     */
    public static void main(String[] args) throws IOException {
// All but last arg is a file/directory of LDC tagged input data
        File[] files = new File[args.length - 1];
        for (int i = 0; i < files.length; i++)
            files[i] = new File(args[i]);
        // Last arg is the TestFrac
        double testFraction = Double.valueOf(args[args.length -1]);
        // Get list of sentences from the LDC POS tagged input files
        List<List<String>> sentences = 	POSTaggedFile.convertToTokenLists(files);
        int numSentences = sentences.size();
        // Compute number of test sentences based on TestFrac
        int numTest = (int)Math.round(numSentences * testFraction);
        // Take test sentences from end of data
        List<List<String>> testSentences = sentences.subList(numSentences - numTest, numSentences);
        // Take training sentences from start of data
        List<List<String>> trainSentences = sentences.subList(0, numSentences - numTest);
        System.out.println("# Train Sentences = " + trainSentences.size() +
                " (# words = " + wordCount(trainSentences) +
                ") \n# Test Sentences = " + testSentences.size() +
                " (# words = " + wordCount(testSentences) + ")");

        BidirectionalBigramModel model = new BidirectionalBigramModel();
        System.out.println("Training...");
        model.train(trainSentences);
        model.test(trainSentences);
        System.out.println("Testing...");
        // Test on test data using test
        model.test(testSentences);
    }
}

