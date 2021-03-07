package server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;

public class Server {
	
	public final static int PUERTO = 21; //Se utiliza FTP
	public final static String ARCHIVO100 = "data/Archivo100mb";
	public final static String ARCHIVO250 = "data/Archivo250mb";
	public final static String DIR_LOGS = "logs/";
	public final static int BUFFER = 64000;
	public final static int MIN_CON = 25;

	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		String tiempo = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Calendar.getInstance().getTime());
		File log = new File(DIR_LOGS + tiempo + ".txt");
		BufferedWriter bw;
		ServerSocket socket;
		try
		{
			bw = new BufferedWriter(new FileWriter(log));
			toLog(bw, "Fecha inicio: " + tiempo);

			socket = new ServerSocket(PUERTO);
			socket.setReceiveBufferSize(BUFFER);
			
		}
		catch(IOException io)
		{
			//error escribiendo buffer
		}
		catch(Exception e)
		{
			
		}
	}
	
	public static synchronized void toLog(BufferedWriter bw, String s)
	{
		try {
			bw.write(s);
			bw.newLine();
			bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
