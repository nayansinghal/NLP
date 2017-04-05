# Neural Dependency Parsing with "Unsupervised" Domain Adaptation
Creating a large amount of annotated data for training the parser is very expensive and the performance decreases if we use training and testing data from different domains. Since learning statistical parser can be quite specific to the genre of the training corpus. In this assignment, we use self training in order to adapt the system trained on one domain to perform better on the other domain, using only a small amount of the annotated data. In this task, we train our model on Wall Street Journal corpora and self-train on brown corpora and achieved the overall performance gain of 4.1\% on brown testing dataset. 

# Main classes
1) processBrown: For brown corpus, it split the train and test 90-10 for each genre. For splitting the dataset, we calculate the total sentences in the corpora and divide accordingly.


2) UnsupervisedDomainAdaption: It trained the model on the seed data (wsj dataset) that consists of very less annotated data. The model trained with the small dataset are used to parse the self-training dataset (brown corpus), that contains several thousand sentences. The automatically annotated data is merged with the manually annotated seed training data which is then used to retrain the parser. The retrained parser are then used for testing on in-domain and out-domain corpus.

# How to run the program

1. For preprocessing of the brown data:
	
	java processBrown /path/to/brown/dataset/dir /path/to/output/dir
	
	e.g.
	java processBrown preprocess/brown-conllx/ data/

2. Train the model:
	
	java UnsupervisedDomainAdaption seed_train_path seed_size self_training_path self_training_size seed_test_path self_training_test_path output_dir/filename

	eg.g
	java UnsupervisedDomainAdaption data/wsj_train.conllx  10000 data/brown_train.conllx 1000 data/wsj_test.conllx data/brown_test.conllx 

# Results
Trace folder contains all the trace file i.e. performance of all different models on Wall Street Journal dataset.