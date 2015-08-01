package mq;

import java.util.HashMap;
import java.util.Locale;


import android.util.Log;
import android.content.Context;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

public class Speaker implements OnInitListener {

	private TextToSpeech tts;
	
	private boolean ready = false;
	
	private boolean allowed = false;
	
	public Speaker(Context context){
		tts = new TextToSpeech(context, this);		
	}	
	
	public boolean isAllowed(){
		return allowed;
	}
	
	public void allow(boolean allowed){
		this.allowed = allowed;
	}

	public void onInit(int status) {
		if(status == TextToSpeech.SUCCESS){
			// Change this to match your
			// locale
			// tts.setLanguage(Locale.FRANCE);    // TODO : improve
			tts.setLanguage(new Locale(Locale.getDefault().getISO3Language(), Locale.getDefault().getISO3Country()));
			//tts.setLanguage(new Locale("fra", "FRA"));
			ready = true;
		}else{
			ready = false;
            Log.e(this.getClass().getSimpleName(), "Unable to set TTS to ready (check your locales)");
            // TODO : TOAST
		}
	}
	
	public void speak(String text){
		
		// Speak only if the TTS is ready
		// and the user has allowed speech
		
		if(ready && allowed) {
//			Log.e(this.getClass().getSimpleName(),"Try tts "+text);
//			HashMap<String, String> hash = new HashMap<String,String>();
//			hash.put(TextToSpeech.Engine.KEY_PARAM_STREAM, 
//					String.valueOf(AudioManager.STREAM_NOTIFICATION));
			//tts.speak(text, TextToSpeech.QUEUE_ADD, hash);
			tts.speak(text, TextToSpeech.QUEUE_ADD, null);
		}
	}
	
	public void pause(int duration){
		tts.playSilence(duration, TextToSpeech.QUEUE_ADD, null);
	}
		
	// Free up resources
	public void destroy(){
		tts.shutdown();
	}
	
}

