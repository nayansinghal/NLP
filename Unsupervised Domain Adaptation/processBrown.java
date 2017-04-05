import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.BufferedWriter;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;

/**
 * Created by nayan singhal on 3/23/17.
 */

public class processBrown {

	protected BufferedReader reader = null;
	protected BufferedWriter writer = null;
	protected static int count = 0;
	protected static boolean writebrown_test = false;

	public processBrown() throws IOException{
    }

    protected String getNextPOSLine(BufferedReader reader) {
		String line = null;
		try {
		    do {
				// Read a line from the file
				line = reader.readLine();
				if (line == null) {
				    // End of file, no more tokens, return null
				    reader.close();
				    return null;
				}
				
				this.writer.write(line + '\n');

				if(line.length() == 0){
					count++;
					if((count)%1000 == 0){
						this.writer.close();
						this.writer = new BufferedWriter( new FileWriter("brown/" + String.valueOf(count/1000 + 1) + ".conllx"));
					}
				}

			} while (line != null);
		}
		catch (IOException e) {
		    System.out.println("\nCould not read from TextFileDocument: ");
		    System.exit(1);
		}
		return null;
	}

	protected String getFileData(BufferedReader reader, int countSent, String testFileName) {
		String line = null;
		try {
		    do {
				// Read a line from the file
				line = reader.readLine();
				if (line == null) {
				    // End of file, no more tokens, return null
				    reader.close();
				    return null;
				}
				
				this.writer.write(line + '\n');

				if(line.length() == 0){
					count++;
					if((count) >= 0.9*countSent && writebrown_test == false){
						this.writer.close();
						this.writer = new BufferedWriter( new FileWriter(testFileName, true));
						this.writebrown_test = true;
					}
				}

			} while (line != null);
		}
		catch (IOException e) {
		    System.out.println("\nCould not read from TextFileDocument: ");
		    System.exit(1);
		}
		return null;
	}

	/* Count sentences in a file */
	protected int countSentences(BufferedReader reader){
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

	protected void openbrown_trainFile(String trainFileName){
		try {
		    this.writer = new BufferedWriter(new FileWriter(trainFileName, true));
		}
		catch (IOException e) {
		    System.out.println("\nCould not open POSTaggedFile: " + null);
		    System.exit(1);
		}
	}

	protected void getAllFiles(File dir, String trainFileName, String testFileName) {
		File[] files = dir.listFiles();
		Arrays.sort(files);

		BufferedReader reader;
		int countSent = 0;

		// count sentences in genre
		for(int i = 0; i < files.length; i++){
			try {
				reader = new BufferedReader(new FileReader(files[i]));
				countSent += countSentences(reader);
			}
			catch (IOException e) {
	    		System.out.println("\nCould not open POSTaggedFile: " + files[i]);
	    		System.exit(1);
			}
		}
		System.out.println(countSent);

		openbrown_trainFile(trainFileName);

		// divide each genre in 90 -10 ratio
		for(int i = 0; i < files.length; i++){
			try {
				reader = new BufferedReader(new FileReader(files[i]));
				getFileData(reader, countSent,testFileName);
			}
			catch (IOException e) {
	    		System.out.println("\nCould not open POSTaggedFile: " + files[i]);
	    		System.exit(1);
			}
		}

		try{
			this.writer.close();
		}
		catch (IOException e) {
	    	System.out.println("\nCould not close writeFile: " + this.writer);
	    	System.exit(1);
		}
	}

	/* Retrieve all the files in the directory */
	protected  void getAllDirectories(File dir, String trainFileName, String testFileName) {
		File[] dirFiles = dir.listFiles();
		Arrays.sort(dirFiles);

		BufferedWriter writer = null;

		try {
		    this.writer = new BufferedWriter(new FileWriter(trainFileName));
		}
		catch (IOException e) {
		    System.out.println("\nCould not open POSTaggedFile: " + null);
		    System.exit(1);
		}

		for(int i = 0; i < dirFiles.length ; i++){

			if(!dirFiles[i].getName().startsWith(".")){
				this.writebrown_test = false;
				this.count = 0;
				System.out.println(dirFiles[i] + "\n");
				getAllFiles(dirFiles[i], trainFileName, testFileName);
			}
		}
	}

	/* Split brown_train data into bin containing each 1000 sentences. */
	/*protected void splitbrown_trainFiles() {
		try{
			BufferedReader reader = new BufferedReader(new FileReader("brown/brown_train.conllx"));
			this.writer = new BufferedWriter( new FileWriter("brown/1.conllx"));
			this.count  = 0;
			getNextPOSLine(reader);
		}
		catch (IOException e) {
		    System.out.println("\nCould not open POSTaggedFile: " + null);
		    System.exit(1);
		}
	}*/

	public static void openbrown_trainAndbrown_testOpenMode(String trainFileName, String testFileName){
		BufferedWriter writer = null;
		try {
		    writer = new BufferedWriter( new FileWriter(trainFileName));
			writer.close();

			writer = new BufferedWriter( new FileWriter(testFileName));
			writer.close();
		}
		catch (IOException e) {
		    System.out.println("\nCould not open POSTaggedFile: " + null);
		    System.exit(1);
		}
	}

	public static int countSentences(String filename){
		try{
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			return new processBrown().countSentences(reader);
		}
		catch (IOException e) {
		    System.out.println("\nCould not open POSTaggedFile: " + filename);
		    System.exit(1);
		}
		return 0;
	}

	public static void countFilesInDir(String dirName) {
		File dir = new File(dirName);
		File[] dirFiles = dir.listFiles();
		Arrays.sort(dirFiles);
		for(int i = 0; i < dirFiles.length ; i++){

			if(!dirFiles[i].getName().startsWith(".")){
				System.out.println(dirFiles[i]);
				System.out.println(dirFiles[i].getName() + " " + String.valueOf(countSentences("brown/" + dirFiles[i].getName())));
			}
		}
	}
	public static void main(String[] args) throws IOException {

		File directory = new File(String.valueOf(args[1]));
	    if (! directory.exists()){
	        directory.mkdir();
    	}

		String trainFileName = args[1] + "brown_train.conllx";
		String testFileName = args[1] + "brown_test.conllx";

		openbrown_trainAndbrown_testOpenMode(trainFileName, testFileName);

		System.out.println("Before brown_train File: " + String.valueOf(countSentences(trainFileName)));
		System.out.println("Before brown_test File: " + String.valueOf(countSentences(testFileName)));

		File[] files = new File[1];
		processBrown obj = new processBrown();

		for (int i = 0; i < files.length; i++) {
			files[i] = new File(args[i]);
			obj.getAllDirectories(files[i], trainFileName, testFileName);
		}

		System.out.println("brown_train File: " + String.valueOf(countSentences(trainFileName)));
		System.out.println("brown_test File: " + String.valueOf(countSentences(testFileName)));
		
    }
}