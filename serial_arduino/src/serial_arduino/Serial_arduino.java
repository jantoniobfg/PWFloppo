package serial_arduino;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import com.fazecast.jSerialComm.SerialPort;


public class Serial_arduino {
	public static void main(String[] args) throws IOException {


		Boolean fim=false;
		Boolean buffered_delta=false;
		int delta=0;
		int last_read=0;
		int[] buffer = new int[1];
		int extra_delta=0;

		SerialPort comPort = SerialPort.getCommPort("COM3");
		comPort.openPort();
		comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
		InputStream in = comPort.getInputStream();
		OutputStream out = comPort.getOutputStream();
		PrintWriter texto = new PrintWriter("filename.txt");
		int pulses_sent=0;
		int samples_read=0;
		try
		{


			WavFile wavFile = WavFile.openWavFile(new File("C:\\Users\\PC\\Music\\teste.wav"));
			wavFile.display();
			int numChannels = wavFile.getNumChannels();
			if(numChannels >1) {
				System.out.println("Ficheiro nao momo");
				return;
			}
			
			while(true) {


				while(true) {
					char read=(char) in.read();
					
					if(read=='1'){
						System.out.println("inicio");
						break;
					}
				}

					for(int i=0;i<(15360);i++) {

						texto.println(i);
						System.out.println(i);
						if(buffered_delta==false)
							if(fim==false)
								if((wavFile.readFrames(buffer,1))==-1)//nao leu uma amostra
									fim=true;
								else {
									samples_read++;
									buffer[0]=(int)Math.round(((float)buffer[0])/((float) 64));//ajustar a 10 bits
									texto.print((int)buffer[0] +"--> ");
								}
						if(fim==false) {
							if(buffered_delta==false){
								delta=buffer[0]-last_read;
								last_read=buffer[0];
								if(Math.abs(delta)>127) {
									if(delta>0) {
										extra_delta+=delta-127;
										delta=127;
									}
									else {
										extra_delta+=delta+127;
										delta=-127;
									}
								}
								else if(extra_delta!=0) {
									if(extra_delta>0)
										while(delta<127 && extra_delta>0) {
											delta++;
											extra_delta--;
										}
									if(extra_delta<0)
										while(delta>-127 && extra_delta<0) {
											delta--;
											extra_delta++;
										}
								}
							}
							if(buffered_delta==true)
								buffered_delta=false;
						}
						if(fim==true)
							delta=0;


						
						if(i==15359) {//nao há espaco suficiente
							buffered_delta=true;
							out.write((byte) (252));//sinal de fim
							pulses_sent+=180;
							texto.print((int) (252) + "  ");
							texto.println((byte) (252) + " / ");
							continue;
						}
						else {   
							texto.print(delta+ "-->");
							if(delta>0) {
								out.write((byte) (75));//positivo
								pulses_sent+=75;
								texto.print( (75) + " ");
							}
							else {
								out.write((byte) (84));//negativo
								pulses_sent+=84;
								texto.print( (84)+ " ");
							}
							
								out.write((byte) ((Math.abs(delta))+96));//entre 0 e 63
								pulses_sent+=Math.abs(delta)+96;
								texto.print(((int)(Math.abs(delta)+96))+"  ");
								texto.println(((byte)(Math.abs(delta)+96))+" / ");
								i++;
							


						}
					}
					System.out.println("fim ");
					System.out.println(((double)pulses_sent)/32000000.0);
					System.out.println(samples_read);
					texto.flush();
				}

			} 

			
		
		catch (Exception e) { 
			e.printStackTrace();
		}
		in.close();


		out.close();
		comPort.closePort();
		
		System.out.println("fim ");
		
		texto.flush();
	}
}

