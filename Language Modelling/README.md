# Language Modelling
Language model is the conditional distribution model that determines the probability of the word using all the previous words in the sentence. Generating a phrase or some combination of words is very important in Natural Language Processing especially while generating the text as output. It has application in various fields like Speech recognition, Optical Character Recognition, Machine Translation, Part of Speech Tagging , etc. N gram models are statistical model based on N-1 order Markov model i.e. it depends on N-1 previous words. In this assignment, we implement the backward bigram model and bidirectional bigram model using forward bigram model API and compare their performance on 3 dataset: axis, wsj, brown.

#Main Classes
1) BigramModel: It can be instantiated in FORWARD and BACKWARD mode. 

2) Bidirectional Bigram Model:  It contains instances of both forward and backward bigram model and combine their result to obtain the performance.

#How to run the program

1. From the top directory, run
	javac ./nlp/lm/*.java

2. To run the forward or backward model, run the following template
java FileName DataPath fraction forward/backward

e.g. java nlp.lm.BigramModel ./nlp/lm/data/atis/ 0.1 forward
	 
3. To run the bidirectional model, run the following template
java FileName DataPath fraction

e.g. java nlp.lm.BidirectionalBigramModel  ./nlp/lm/data/atis/ 0.1

# Results
Result folder contains all the trace file i.e. performance of all different model on atis, wsj and brown dataset.
