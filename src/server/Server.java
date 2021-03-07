package server;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;

public class Server {
	
	public final static int PUERTO = 21; //Se utiliza FTP
	public final static String ARCHIVO100 = "Archivo100MB";
	public final static String ARCHIVO250 = "Archivo250MB";
	public final static int BUFFER = 64000;
	public final static int MIN_CON = 25;
	public static BufferedWriter bw;

	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		String tiempo = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Calendar.getInstance().getTime());
		File log = new File("./logs/" + tiempo + ".txt");
		ServerSocket socket;
		try
		{
			bw = new BufferedWriter(new FileWriter(log));
			toLog(bw, "Fecha inicio: " + tiempo);

			socket = new ServerSocket(PUERTO);
			socket.setReceiveBufferSize(BUFFER);
			
			System.out.println("¿Qué archivo desea enviar?");
			System.out.println("Ingrese 1 para el de 100 MB o 2 para el de 250MB:");
			int chooseFile = in.nextInt();
			String file = "./data/";
			switch(chooseFile) {
			case 1:
				file += ARCHIVO100;
				toLog(bw, "Archivo seleccionado: " + ARCHIVO100 + "\n Peso archivo: 100MB");
				break;
			case 2:
				file += ARCHIVO250;
				toLog(bw, "Archivo seleccionado: " + ARCHIVO250 + "\n Peso archivo: 250MB");
				break;
			default:
				toLog(bw, "ERROR: Se seleccionó un archivo inválido");
				throw new Exception("Ingresó una opción inválida");
			}
			
			System.out.println("Ingrese el número de clientes a conectar:");
			int clients = in.nextInt();
			toLog(bw, "Número de clientes: " + clients);
			
			Socket[] socketClients = new Socket[clients];
			int currentClients = 0;
			System.out.println("Esperando...");
			while(currentClients < clients)
			{
				try
				{
					socketClients[currentClients] = socket.accept();
					DataOutputStream dOut = new DataOutputStream(socketClients[currentClients].getOutputStream());
					DataInputStream dIn = new DataInputStream(socketClients[currentClients].getInputStream());
					switch(chooseFile) {
					case 1:
						dOut.writeByte(1);
						dOut.writeUTF(ARCHIVO100);
						dOut.flush();
						break;
					default:
						dOut.writeByte(1);
						dOut.writeUTF(ARCHIVO250);
						dOut.flush();
					}
					currentClients++;
					if(dIn.readByte() == 2)
					{
						System.out.println("Conexión exitosa con el cliente: " + currentClients);
						System.out.println("El cliente " + currentClients + " está esperando el archivo");
					}
//					else
//					{
//						throw new Exception("La conexión con el cliente " + currentClients + " no fue exitosa");
//					}
					dOut.write(currentClients);
					toLog(bw, "Cliente " + currentClients + "conectado");
				}
				catch(Exception e)
				{
					toLog(bw, "ERROR: " + e.getMessage());
				}
			}
			System.out.println("Iniciando envío de archivo");
			toLog(bw, "Inicio de la transferencia");
			for(int i = 0; i < clients; i++)
			{
				//inicializar y lanzar threads
			}
		}
		catch(Exception e)
		{
			System.err.println("Ha sucedido un error en la ejecución. Revise el log.");
			toLog(bw, "ERROR: " + e.getMessage());
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
