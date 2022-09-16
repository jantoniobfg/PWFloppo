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
		int previous_delta=0;

		SerialPort comPort = SerialPort.getCommPort("COM5");
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

					for(int i=0;i<(7168);i++) {

						texto.println(i);
						if(i%1000==0) {
							System.out.println(i);
						}
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
						if(fim==true) {
							delta=0;
						}


						
						if(i==7167) {//nao h� espaco suficiente
							buffered_delta=true;
							out.write((byte) (255));//sinal de fim
							pulses_sent+=255;
							texto.print((int) (255) + "  ");
							texto.println((byte) (255) + " / ");
							continue;
						}
						else {   
							texto.print(delta+ "-->");
							if(delta>0 && previous_delta<=0) {
								out.write((byte) (240));//positivo
								pulses_sent+=240;
								texto.print( (240) + " ");
								i++;
							}
							
							else if(delta<0 && previous_delta>=0) {
								out.write((byte) (220));//negativo
								pulses_sent+=220;
								texto.print( (220)+ " ");
								i++;
							}
							if(i==7167) {//nao h� espaco suficiente
								buffered_delta=true;
								out.write((byte) (255));//sinal de fim
								pulses_sent+=255;
								texto.print((int) (255) + "  ");
								texto.println((byte) (255) + " / ");
								continue;
							}
								out.write((byte) ((Math.abs(delta))+64));//entre 0 e 63
								pulses_sent+=Math.abs(delta)+64;
								texto.print(((int)(Math.abs(delta)))+"  ");
								texto.println(((byte)(Math.abs(delta)+64))+" / ");
								
								if(i==7167) {//nao h� espaco suficiente
									out.write((byte) (255));//sinal de fim
									pulses_sent+=255;
									texto.print((int) (255) + "  ");
									texto.println((byte) (255) + " / ");
									continue;
								}
								previous_delta=delta;
							


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

