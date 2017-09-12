package reversi;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageFilter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.python.core.Py;
import org.python.core.PyFunction;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

public class ReversiAPI {
	
	private int index = -1;
	private int[] blockState = new int[64];
	private int currentPlayer;
	private String block = "";
	private PythonInterpreter interpreter;
	
	public ReversiAPI() {
		
	}
	
	private void turnToString() {
		for (int i=0; i<64; ++i) {
			block += (String.valueOf(blockState[i]) + " ");
		}
	}
	
	private void initialize() {
		
	}
	
	private void importPy() {
		
		String location = System.getProperty("user.dir");
		String pyLocation = location + "\\pychess\\";
		
		String currentPlayer = String.valueOf(this.currentPlayer);
		//System.out.println(block);
		
		interpreter = new PythonInterpreter();
		PySystemState sys = Py.getSystemState();
		sys.path.add(pyLocation);
		//interpreter.exec("import pychess.pyversi");
		interpreter.execfile(pyLocation + "javaport.py");
		PyFunction func = (PyFunction)interpreter.get("calc",PyFunction.class); 
		
        PyObject pyobj = func.__call__(new PyString(block), new PyString(currentPlayer));  
        String pyResult = pyobj.toString();
        index = Integer.valueOf(pyResult);
        System.out.println("index = " + index);
        
        /*File file = new File(location + "\\log.txt");
        try {
        	file.createNewFile();
        } catch (IOException e) {
        	e.printStackTrace();
        }
        
        try {
			FileWriter fw = new FileWriter(file, true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(index + "\t");
			bw.flush();
			bw.close();
			fw.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}*/
	}
	
	public int getResult(int[] localBlockState, int currentPlayer) {
		block = "";
		index = -1;
		this.currentPlayer = currentPlayer;
		System.arraycopy(localBlockState, 0, blockState, 0, 64);
		turnToString();
		if (index == -1) {
			initialize();
		}
		
		importPy();
		
		return index;
	}
}
