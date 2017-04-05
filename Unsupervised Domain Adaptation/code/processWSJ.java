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
 * TODO: convert txt format to conllx
 */

public class process {

	protected BufferedReader reader = null;
	protected BufferedWriter writer = null;
	protected static int count = 0;

	public process() throws IOException{
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
						this.writer = new BufferedWriter( new FileWriter("wsj/" + String.valueOf(count/1000 + 1) + ".conllx"));
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

	/* Retrieve file data */
	protected void getFileData(File dir) {
		File[] files = dir.listFiles();
		Arrays.sort(files);

		BufferedReader reader;

		for(int i = 0; i < files.length; i++){

			try {
	    		reader = new BufferedReader(new FileReader(files[i]));
	    		getNextPOSLine(reader);
			}
			catch (IOException e) {
	    		System.out.println("\nCould not open POSTaggedFile: " + files[i]);
	    		System.exit(1);
			}
		}
	}

	/* Retrieve all the files in the directory */
	protected  void getAllFiles(File dir) {
		File[] dirFiles = dir.listFiles();
		Arrays.sort(dirFiles);

		BufferedWriter writer = null;

		try {
		    this.writer = new BufferedWriter(new FileWriter("wsj/1.conllx"));
		}
		catch (IOException e) {
		    System.out.println("\nCould not open POSTaggedFile: " + null);
		    System.exit(1);
		}

		for(int i = 0; i < dirFiles.length; i++){

			if(!dirFiles[i].getName().startsWith(".") && dirFiles[i].getName().compareTo("02") >= 0 && dirFiles[i].getName().compareTo("22") <= 0){
				getFileData(dirFiles[i]);
				System.out.println(dirFiles[i]);
			}
		}
	}

	public static void main(String[] args) throws IOException {
		File[] files = new File[args.length];
		for (int i = 0; i < files.length; i++) {
			files[i] = new File(args[i]);
			new process().getAllFiles(files[i]);
			//System.out.println(files[i]);
		}
    }
}