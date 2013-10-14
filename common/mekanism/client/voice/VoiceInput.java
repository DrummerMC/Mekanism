package mekanism.client.voice;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

import mekanism.client.MekanismKeyHandler;

public class VoiceInput extends Thread
{
	public VoiceClient voiceClient;
	
	public DataLine.Info microphone = new DataLine.Info(TargetDataLine.class, voiceClient.format, 2200);
	
	public TargetDataLine targetLine;
	
	public VoiceInput(VoiceClient client)
	{
		voiceClient = client;
		
		setDaemon(true);
		setName("VoiceServer Client Input Thread");
	}
	
	@Override
	public void run()
	{
		try {
			targetLine = ((TargetDataLine)AudioSystem.getLine(microphone));
			targetLine.open(voiceClient.format, 2200);
			targetLine.start();
			AudioInputStream audioInput = new AudioInputStream(targetLine);
			
			boolean doFlush = false;
			
			while(voiceClient.running)
			{
				if(MekanismKeyHandler.voiceDown)
				{
					targetLine.flush();
					
					while(voiceClient.running && MekanismKeyHandler.voiceDown)
					{
						int availableBytes = audioInput.available();
						byte[] audioData = new byte[availableBytes > 2200 ? 2200 : availableBytes];
						int bytesRead = audioInput.read(audioData, 0, audioData.length);
						
						if(bytesRead > 0)
						{
							voiceClient.output.writeShort(audioData.length);
							voiceClient.output.write(audioData);
						}
					}
					
					try {
						Thread.sleep(200L);
					} catch(Exception e) {
						e.printStackTrace();
					}
					
					doFlush = true;
				}
				else if(doFlush)
				{
					voiceClient.output.flush();
					doFlush = false;
				}
				
				try {
					Thread.sleep(20L);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			
			audioInput.close();
		} catch(Exception e) {
			System.err.println("Error while running microphone loop.");
			e.printStackTrace();
		}
	}
	
	public void close()
	{
		targetLine.flush();
		targetLine.close();
	}
}
