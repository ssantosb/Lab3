package client;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;

public class Cliente {

	public final static String LOG = "logs/";
	
	private static final String DESCARGA = "data/descargas/";
	
	private static final int MENSAJE = 1024;
	
	private static final int PUERTO = 21; 
	
	public final static int BUFFERTAMANIO = 64000;

	private static BufferedWriter bw;

	public static void main(String argv[]) {

		Scanner inConsole = new Scanner(System.in);
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		Socket socket;
		String tiempo = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss").format(Calendar.getInstance().getTime());
		File log = new File(LOG + tiempo + ".txt");


		try {
			bw = new BufferedWriter(new FileWriter(log));
			System.out.println("Ingrese la IP del Servidor");
			String direccion = inConsole.next(); 
			socket = new Socket(direccion, PUERTO);
			socket.setReceiveBufferSize(BUFFERTAMANIO);
			socket.setSendBufferSize(BUFFERTAMANIO);
			
			
			System.out.println("Se realizó la conexión al servidor");
			System.out.println("Pronto recibirá el nombre del archivo");
			String nombre = "";
			
			DataInputStream inputStream = new DataInputStream(socket.getInputStream());
			DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
			
			int id = 0;
			int byteIn = inputStream.readByte();
			
			if (byteIn == 1) {
				nombre = inputStream.readUTF();
				System.out.println("El archivo a descargar se llama: " + nombre);
				bw.write("Nombre archivo prueba: " + nombre);
				
				bw.newLine();
				bw.flush();
				outputStream.writeByte(2);
				id = inputStream.readByte();
			}
			Cliente cliente = new Cliente();
			cliente.descargarArchivo(id, socket, nombre);

		}

		catch (Exception e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

	public void descargarArchivo(int id, Socket socket, String nombre) {
		
		int actual;
		int bytesLectura = 0;
		String inputLine;
		String outputLine;
		
		PrintWriter pw;
		BufferedReader bf;

		FileOutputStream fOutStream;
		BufferedOutputStream bOutStream;
		
		String timeLog = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(Calendar.getInstance().getTime());



		try {
			bw.write("Fecha y hora: " + timeLog);
			bw.newLine();
			bw.flush();

			pw = new PrintWriter(socket.getOutputStream(), true);
			bf = new BufferedReader(new InputStreamReader(socket.getInputStream()));


			DataInputStream dInputStream = new DataInputStream(socket.getInputStream());
			String hash = dInputStream.readUTF();
			bw.write("En espera de recibir paquete de " + MENSAJE + " bytes.");
			bw.newLine();
			bw.flush();

			byte[] mybytearray = new byte[700000000];
			InputStream inputStream = socket.getInputStream();
			fOutStream = new FileOutputStream(DESCARGA + nombre);
			bOutStream = new BufferedOutputStream(fOutStream);
			while (bytesLectura == 0) {
				bytesLectura = inputStream.read(mybytearray, 0, mybytearray.length);
			}
			System.out.println("Recibiendo el archivo");
			actual = bytesLectura;

			long startTime = System.currentTimeMillis();
			int bytesR = 0;
			int paquetes = 0;
			int mensajesR = 0;
			
			while(true) {
				bytesLectura = inputStream.read(mybytearray, actual, (mybytearray.length - actual));
				paquetes++;
				bytesR += bytesLectura;
				if (bytesR >= MENSAJE) {
					mensajesR += (bytesR / MENSAJE);
					bytesR -= MENSAJE * (bytesR / MENSAJE);
				}
				
				if (bytesLectura >= 0){
					actual += bytesLectura;
				}
				else{
					break;
				}

			} 
			bOutStream.write(mybytearray, 0, actual);
			bOutStream.flush();
			long endTime = System.currentTimeMillis();
			long tiempoTotal = endTime - startTime;
			System.out.println("Tiempo de descarga: " + tiempoTotal + " milisegundos");

			bw.write("Tiempo de descarga: " + tiempoTotal + " milisegundos");
			bw.newLine();
			bw.flush();
			bw.write("Tamaño del archivo leido: " + actual + " bytes");
			bw.newLine();
			bw.flush();
			bw.write("Cantidad de paquetes recibidos: " + paquetes);
			bw.newLine();
			bw.flush();


			DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
			dOut.writeByte(id);

			File archivo = new File(DESCARGA + nombre);
			byte[] archivoBytes = Files.readAllBytes(archivo.toPath());
			byte[] hashN = new byte[61440];
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			hashN = md.digest(archivoBytes);
			StringBuffer sb = new StringBuffer();
	        for (int i = 0; i < hashN.length; i++) {
	          sb.append(Integer.toString((hashN[i] & 0xff) + 0x100, 16).substring(1));
	        }
	        String hashGenerado = sb.toString();

	        
			if (hash.equals(hashGenerado)) {
				System.out.println("Se garantiza la integridad. El hash fue verificado");
				bw.write("Se garantiza la integridad. El hash fue verificado");
				bw.newLine();
				bw.flush();
			} else {
				System.out.println("No se garantiza la integridad. El hash no es el correcto");
				bw.write("No se garantiza la integridad. El hash no es el correcto");
				bw.newLine();
				bw.flush();
			}

			pw.close();
			bf.close();

			fOutStream.close();
			bOutStream.close();

		} catch (Exception e) {
			System.out.println(e.getMessage());
			try {
				bw.write("Error en el envio: " + e.getMessage());
				bw.newLine();
				bw.flush();
			} catch (Exception ex) {
				System.out.println("Error escritura mensaje: " + ex.getMessage());
			}
		}
	}

}
