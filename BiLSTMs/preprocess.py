import glob
from random import shuffle
import operator

MAX_LENGTH = 100

class PreprocessData:

	def __init__(self, dataset_type='wsj'):
		self.vocabulary = {}
		self.pos_tags = {}
		self.dataset_type = dataset_type

		# suffix := 39
		self.suffix_list = ['acy', 'al', 'nce', 'dom', 'nce', 'er', 'or', 'ism', 'ist', 'ty',
							'ment', 'ness', 'ship', 'ion', 'ate', 'en', 'fy', 'ize', 'ise', 'ble', 'al',
							'al', 'esque', 'ful', 'ic', 'ical', 'ous', 'ish', 'ive', 'less', 'y', 'ship',
							'ary', 'hood', 'age', 'logy', 'ing', 's', 'es']

	    # prefix := 71
		self.prefix_list = ['a','an', 'ante', 'anti', 'auto', 'circum', 'be', 'circum', 'co', 'col', 'com',
							'con', 'contra', 'contro', 'deca', 'de', 'dia', 'dis', 'di', 'en', 'ex', 'extra',
							'fore', 'hecto', 'hemi', 'hetero', 'hexa', 'hepta', 'homo', 'homeo', 'hyper', 'il', 
							'im', 'in', 'inter', 'ir', 'inter', 'intro', 'intra', 'kilo', 'mal', 'macro', 'micro',
							'mid', 'mis', 'mono', 'multi', 'non', 'octo', 'omni', 'over', 'para', 'penta', 'post',
							'poly', 'pre', 'pro', 're', 'retro', 'semi', 'sym', 'sub', 'super', 'syn', 'tele', 
							'tetra', 'therm', 'trans', 'tri', 'un', 'uni']

	## Get standard split for WSJ
	def get_standard_split(self, files):
		if self.dataset_type == 'wsj':
			train_files = []
			val_files = []
			test_files = []
			for file_ in files:
				partition = int(file_.split('/')[-2])
				if partition >= 0 and partition <= 18:
					train_files.append(file_)
				elif partition <= 21:
					val_files.append(file_)
				else:
					test_files.append(file_)
			return train_files, val_files, test_files
		else:
			raise Exception('Standard Split not Implemented for '+ self.dataset_type)


	## Split data in the fraction user wants
	def split_data(self, data, fraction):
		split_index = int(fraction*len(data))
		left_split = data[:split_index]
		right_split = data[split_index:]
		if not(left_split):
			raise Exception('Fraction too small')
		if not(right_split):
			raise Exception('Fraction too big')
		return left_split, right_split


	@staticmethod
	def isFeasibleStartingCharacter(c):
		unfeasibleChars = '[]@\n'
		return not(c in unfeasibleChars)

	## unknown words represented by len(vocab)
	def get_unk_id(self, dic):
		return len(dic)

	## return dictionary length
	## TODO: pass dictionary argument for calculating length
	def get_pad_id(self, dic):
		return len(self.vocabulary) + 1

	## get id of given token(pos) from dictionary dic.
	## if not in dic, extend the dic if in train mode
	## else use representation for unknown token
	def get_id(self, pos, dic, mode):
		if pos not in dic:
			if mode == 'train':
				dic[pos] = len(dic)
			else:
				return self.get_unk_id(dic)
		return dic[pos]

	def containSuffix(self, word):
	## TODO: create hashmap for suffix_list for optimization
		for i in range(0, len(self.suffix_list)):
			if word.endswith(self.suffix_list[i]):
				return i;
		return len(self.suffix_list)

	## Process single file to get raw data matrix
	def processSingleFile(self, inFileName, mode):
		matrix = []
		row = []
		with open(inFileName) as f:
			lines = f.readlines()
			for line in lines:
				line = line.strip()
				if line == '':
					pass
				else:
					tokens = line.split()
					for token in tokens:
						## ==== indicates start of new example					
						if token[0] == '=':
							if row:
								matrix.append(row)
							row = []
							break
						elif PreprocessData.isFeasibleStartingCharacter(token[0]):
							wordPosPair = token.split('/')
							if len(wordPosPair) == 2:
								## get ids for word and pos tag from vocabulary and pos_tags dictionary
								feature = self.get_id(wordPosPair[0], self.vocabulary, mode)
								suffix = self.containSuffix(wordPosPair[0])
								# include all pos tags in pos_tags dictionary.
								#row.append((feature, self.get_id(wordPosPair[1], 
								#			self.pos_tags, 'train')))
								row.append((feature, suffix, self.get_id(wordPosPair[1], 
											self.pos_tags, 'train')))
		if row:
			matrix.append(row)
		return matrix

	## get all data files in given subdirectories of given directory
	def preProcessDirectory(self, inDirectoryName, subDirNames=['*']):
		if not(subDirNames):
			files = glob.glob(inDirectoryName+'/*.pos')
		else:
			files = [glob.glob(inDirectoryName+ '/' + subDirName + '/*.pos')
					for subDirName in subDirNames]
			files = set().union(*files)
		return list(files)


	## Get basic data matrix with (possibly) variable sized senteces, without padding
	def get_raw_data(self, files, mode):
		matrix = []
		for f in files:
			matrix.extend(self.processSingleFile(f, mode))
		return matrix


	## Get rid of sentences greater than max_size
	## and pad the remaining if less than max_size
	def get_processed_data(self, mat, max_size):
		X = []
		y = []
		list1 = [0,1]
		my_items = operator.itemgetter(*list1)

		original_len = len(mat)
		mat = filter(lambda x: len(x) <= max_size, mat)
		no_removed = original_len - len(mat)
		for row in mat:
			#X_row = [tup[0] for tup in row]
			X_row = [my_items(tup) for tup in row]
			y_row = [tup[2] for tup in row]
			## padded words represented by len(vocab) + 1
			#X_row = X_row + [self.get_pad_id(self.vocabulary)]*(max_size - len(X_row))
			X_row = X_row + [[self.get_pad_id(self.vocabulary),2]]*(max_size - len(X_row))
			## Padded pos tags represented by -1
			y_row = y_row + [-1]*(max_size-len(y_row))
			X.append(X_row)
			y.append(y_row)
		return X, y, no_removed
