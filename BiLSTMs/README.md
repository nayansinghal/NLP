# Part of Speech Tagging with BiDirectional Long Short Term Memory Network
Bidirectional Long Short-Term Memory has been shown to be very effective for pos-tagging. Though word embedding has been shown as powerful for statistical pos-tagging of the sentence. In this task, we use orthographic features with word embedding for part-of-speech tagging. When tested on the Wall Street Journal, accuracy: 96.3% and out of vocabulary accuracy: 71.4% is achieved. 

# Main classes
1) pos_bilstm: It contains the baseline model, input model and output model which output the training accuracy, validation accuracy and out of vocabulary accuracy.

2) preprocess: It process directory, extracts words and orthographic features for each word in the sentence.

# How to run the program

1. For the model, run the following template
	
	python pos_bilstm.py DataPath Directory\path\to\training\directory standard train

# Results
Trace folder contains all the trace file i.e. performance of all different models on Wall Street Journal dataset.