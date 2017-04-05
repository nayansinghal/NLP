package edu.stanford.nlp.pipeline;

import java.util.*;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.tokensregex.TokenSequencePattern;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.WordToSentenceProcessor;
import edu.stanford.nlp.util.ArraySet;
import edu.stanford.nlp.util.ArrayUtils;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Generics;
import edu.stanford.nlp.util.logging.Redwood;


/**
 * This class assumes that there is a {@code List<CoreLabel>}
 * under the {@code TokensAnnotation} field, and runs it
 * through {@link edu.stanford.nlp.process.WordToSentenceProcessor}
 * and puts the new {@code List<Annotation>}
 * under the {@code SentencesAnnotation} field.
 *
 * @author Jenny Finkel
 * @author Christopher Manning
 */
public class WordsToSentencesAnnotator implements Annotator  {

  /** A logger for this class */
  private static final Redwood.RedwoodChannels log = Redwood.channels(WordsToSentencesAnnotator.class);

  private final WordToSentenceProcessor<CoreLabel> wts;

  private final boolean VERBOSE;

  private final boolean countLineNumbers;

  public WordsToSentencesAnnotator() {
    this(false);
  }


  public WordsToSentencesAnnotator(Properties properties) {
    // log.info(signature());
    // todo: The above shows that signature is edu.stanford.nlp.pipeline.AnnotatorImplementations: and doesn't reflect what annotator it is! Should fix. Maybe is fixed now [2016]. Test!
    boolean nlSplitting = Boolean.valueOf(properties.getProperty(StanfordCoreNLP.NEWLINE_SPLITTER_PROPERTY, "false"));
    if (nlSplitting) {
      boolean whitespaceTokenization = Boolean.valueOf(properties.getProperty("tokenize.whitespace", "false"));
      if (whitespaceTokenization) {
        if (System.lineSeparator().equals("\n")) {
          // this constructor will keep empty lines as empty sentences
          WordToSentenceProcessor<CoreLabel> wts1 =
                  new WordToSentenceProcessor<>(ArrayUtils.asImmutableSet(new String[]{"\n"}));
          VERBOSE = false;
          this.countLineNumbers = true;
          this.wts = wts1;
        } else {
          // throw "\n" in just in case files use that instead of
          // the system separator
          // this constructor will keep empty lines as empty sentences
          WordToSentenceProcessor<CoreLabel> wts1 =
                  new WordToSentenceProcessor<>(ArrayUtils.asImmutableSet(new String[]{System.lineSeparator(), "\n"}));
          VERBOSE = false;
          this.countLineNumbers = true;
          this.wts = wts1;
        }
      } else {
        // this constructor will keep empty lines as empty sentences
        WordToSentenceProcessor<CoreLabel> wts1 =
                new WordToSentenceProcessor<>(ArrayUtils.asImmutableSet(new String[]{PTBTokenizer.getNewlineToken()}));
        VERBOSE = false;
        this.countLineNumbers = true;
        this.wts = wts1;
      }

    } else {
      // Treat as one sentence: You get a no-op sentence splitter that always returns all tokens as one sentence.
      String isOneSentence = properties.getProperty("ssplit.isOneSentence");
      if (Boolean.parseBoolean(isOneSentence)) { // this method treats null as false
        WordToSentenceProcessor<CoreLabel> wts1 = new WordToSentenceProcessor<>(true);
        VERBOSE = false;
        this.countLineNumbers = false;
        this.wts = wts1;
      } else {

        // multi token sentence boundaries
        String boundaryMultiTokenRegex = properties.getProperty("ssplit.boundaryMultiTokenRegex");

        // Discard these tokens without marking them as sentence boundaries
        String tokenPatternsToDiscardProp = properties.getProperty("ssplit.tokenPatternsToDiscard");
        Set<String> tokenRegexesToDiscard = null;
        if (tokenPatternsToDiscardProp != null) {
          String[] toks = tokenPatternsToDiscardProp.split(",");
          tokenRegexesToDiscard = Generics.newHashSet(Arrays.asList(toks));
        }
        // regular boundaries
        String boundaryTokenRegex = properties.getProperty("ssplit.boundaryTokenRegex");
        Set<String> boundariesToDiscard = null;

        // todo [cdm 2016]: Add support for specifying ssplit.boundaryFollowerRegex here and send down to WordsToSentencesAnnotator

        // newline boundaries which are discarded.
        String bounds = properties.getProperty("ssplit.boundariesToDiscard");
        if (bounds != null) {
          String[] toks = bounds.split(",");
          boundariesToDiscard = Generics.newHashSet(Arrays.asList(toks));
        }
        Set<String> htmlElementsToDiscard = null;
        // HTML boundaries which are discarded
        bounds = properties.getProperty("ssplit.htmlBoundariesToDiscard");
        if (bounds != null) {
          String[] elements = bounds.split(",");
          htmlElementsToDiscard = Generics.newHashSet(Arrays.asList(elements));
        }
        String nlsb = properties.getProperty(StanfordCoreNLP.NEWLINE_IS_SENTENCE_BREAK_PROPERTY,
            StanfordCoreNLP.DEFAULT_NEWLINE_IS_SENTENCE_BREAK);

        WordToSentenceProcessor<CoreLabel> wts = new WordToSentenceProcessor<>(boundaryTokenRegex, null,
            boundariesToDiscard, htmlElementsToDiscard,
            WordToSentenceProcessor.stringToNewlineIsSentenceBreak(nlsb),
            (boundaryMultiTokenRegex != null) ? TokenSequencePattern.compile(boundaryMultiTokenRegex) : null, tokenRegexesToDiscard);
        VERBOSE = false;
        this.countLineNumbers = false;
        this.wts = wts;
      }
    }
  }

  public WordsToSentencesAnnotator(boolean verbose) {
    this(verbose, false, new WordToSentenceProcessor<>());
  }

  public WordsToSentencesAnnotator(boolean verbose, String boundaryTokenRegex,
                                   Set<String> boundaryToDiscard, Set<String> htmlElementsToDiscard,
                                   String newlineIsSentenceBreak, String boundaryMultiTokenRegex,
                                   Set<String> tokenRegexesToDiscard) {
    this(verbose, false,
            new WordToSentenceProcessor<>(boundaryTokenRegex, null,
                    boundaryToDiscard, htmlElementsToDiscard,
                    WordToSentenceProcessor.stringToNewlineIsSentenceBreak(newlineIsSentenceBreak),
                    (boundaryMultiTokenRegex != null) ? TokenSequencePattern.compile(boundaryMultiTokenRegex) : null, tokenRegexesToDiscard));
  }

  private WordsToSentencesAnnotator(boolean verbose, boolean countLineNumbers,
                                    WordToSentenceProcessor<CoreLabel> wts) {
    VERBOSE = verbose;
    this.countLineNumbers = countLineNumbers;
    this.wts = wts;
  }


  /** Return a WordsToSentencesAnnotator that splits on newlines (only), which are then deleted.
   *  This constructor counts the lines by putting in empty token lists for empty lines.
   *  It tells the underlying splitter to return empty lists of tokens
   *  and then treats those empty lists as empty lines.  We don't
   *  actually include empty sentences in the annotation, though. But they
   *  are used in numbering the sentence. Only this constructor leads to
   *  empty sentences.
   *
   *  @param  nlToken Zero or more new line tokens, which might be a {@literal \n} or the fake
   *                 newline tokens returned from the tokenizer.
   *  @return A WordsToSentenceAnnotator.
   */
  public static WordsToSentencesAnnotator newlineSplitter(String... nlToken) {
    // this constructor will keep empty lines as empty sentences
    WordToSentenceProcessor<CoreLabel> wts =
            new WordToSentenceProcessor<>(ArrayUtils.asImmutableSet(nlToken));
    return new WordsToSentencesAnnotator(false, true, wts);
  }


  /** Return a WordsToSentencesAnnotator that never splits the token stream. You just get one sentence.
   *
   *  @return A WordsToSentenceAnnotator.
   */
  public static WordsToSentencesAnnotator nonSplitter() {
    WordToSentenceProcessor<CoreLabel> wts = new WordToSentenceProcessor<>(true);
    return new WordsToSentencesAnnotator(false, false, wts);
  }


  /**
   * If setCountLineNumbers is set to true, we count line numbers by
   * telling the underlying splitter to return empty lists of tokens
   * and then treating those empty lists as empty lines.  We don't
   * actually include empty sentences in the annotation, though.
   **/
  @Override
  public void annotate(Annotation annotation) {
    if (VERBOSE) {
      log.info("Sentence splitting ...");
    }
    if ( !annotation.containsKey(CoreAnnotations.TokensAnnotation.class)) {
      throw new IllegalArgumentException("WordsToSentencesAnnotator: unable to find words/tokens in: " + annotation);
    }

    // get text and tokens from the document
    String text = annotation.get(CoreAnnotations.TextAnnotation.class);
    List<CoreLabel> tokens = annotation.get(CoreAnnotations.TokensAnnotation.class);
    String docID = annotation.get(CoreAnnotations.DocIDAnnotation.class);
    // log.info("Tokens are: " + tokens);

    // assemble the sentence annotations
    int tokenOffset = 0;
    int lineNumber = 0;
    // section annotations to mark sentences with
    CoreMap sectionAnnotations = null;
    List<CoreMap> sentences = new ArrayList<>();
    for (List<CoreLabel> sentenceTokens: wts.process(tokens)) {
      if (countLineNumbers) {
        ++lineNumber;
      }
      if (sentenceTokens.isEmpty()) {
        if (!countLineNumbers) {
          throw new IllegalStateException("unexpected empty sentence: " + sentenceTokens);
        } else {
          continue;
        }
      }

      // get the sentence text from the first and last character offsets
      int begin = sentenceTokens.get(0).get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
      int last = sentenceTokens.size() - 1;
      int end = sentenceTokens.get(last).get(CoreAnnotations.CharacterOffsetEndAnnotation.class);
      String sentenceText = text.substring(begin, end);

      // create a sentence annotation with text and token offsets
      Annotation sentence = new Annotation(sentenceText);
      sentence.set(CoreAnnotations.CharacterOffsetBeginAnnotation.class, begin);
      sentence.set(CoreAnnotations.CharacterOffsetEndAnnotation.class, end);
      sentence.set(CoreAnnotations.TokensAnnotation.class, sentenceTokens);
      sentence.set(CoreAnnotations.TokenBeginAnnotation.class, tokenOffset);
      tokenOffset += sentenceTokens.size();
      sentence.set(CoreAnnotations.TokenEndAnnotation.class, tokenOffset);
      sentence.set(CoreAnnotations.SentenceIndexAnnotation.class, sentences.size());

      if (countLineNumbers) {
        sentence.set(CoreAnnotations.LineNumberAnnotation.class, lineNumber);
      }

      // Annotate sentence with section information.
      // Assume section start and end appear as first and last tokens of sentence
      CoreLabel sentenceStartToken = sentenceTokens.get(0);
      CoreLabel sentenceEndToken = sentenceTokens.get(sentenceTokens.size()-1);

      CoreMap sectionStart = sentenceStartToken.get(CoreAnnotations.SectionStartAnnotation.class);
      if (sectionStart != null) {
        // Section is started
        sectionAnnotations = sectionStart;
      }
      if (sectionAnnotations != null) {
        // transfer annotations over to sentence
        ChunkAnnotationUtils.copyUnsetAnnotations(sectionAnnotations, sentence);
      }
      String sectionEnd = sentenceEndToken.get(CoreAnnotations.SectionEndAnnotation.class);
      if (sectionEnd != null) {
        sectionAnnotations = null;
      }

      if (docID != null) {
        sentence.set(CoreAnnotations.DocIDAnnotation.class, docID);
      }

      int index = 1;
      for (CoreLabel token : sentenceTokens) {
        token.setIndex(index++);
        token.setSentIndex(sentences.size());
        if (docID != null) {
          token.setDocID(docID);
        }
      }

      // add the sentence to the list
      sentences.add(sentence);
    }
    // the condition below is possible if sentenceBoundaryToDiscard is initialized!
      /*
      if (tokenOffset != tokens.size()) {
        throw new RuntimeException(String.format(
            "expected %d tokens, found %d", tokens.size(), tokenOffset));
      }
      */

    // add the sentences annotations to the document
    annotation.set(CoreAnnotations.SentencesAnnotation.class, sentences);
  }


  @Override
  public Set<Class<? extends CoreAnnotation>> requires() {
    return Collections.unmodifiableSet(new ArraySet<>(Arrays.asList(
        CoreAnnotations.TextAnnotation.class,
        CoreAnnotations.TokensAnnotation.class,
        CoreAnnotations.CharacterOffsetBeginAnnotation.class,
        CoreAnnotations.CharacterOffsetEndAnnotation.class
    )));
  }

  @Override
  public Set<Class<? extends CoreAnnotation>> requirementsSatisfied() {
    return new HashSet<>(Arrays.asList(
        CoreAnnotations.SentencesAnnotation.class,
        CoreAnnotations.SentenceIndexAnnotation.class
    ));
  }

}
