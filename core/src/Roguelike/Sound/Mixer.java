package Roguelike.Sound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

public class Mixer
{
	String musicName;
	Music music;
	float volume;
	
	String mixName;
	Music mix;
	float mixTime;
	float time;
	
	public Mixer(String musicName, float volume)
	{
		this.musicName = musicName;
		this.volume = volume;
		
		music = Gdx.audio.newMusic(Gdx.files.internal("Music/"+musicName+".mp3"));
		music.play();
		music.setLooping(true);
		music.setVolume(volume);
	}
	
	public void mix(String mixName, float time)
	{
		if (musicName.equals(mixName)) { return; }
		
		this.mixName = mixName;
		mix = Gdx.audio.newMusic(Gdx.files.internal(mixName));
		mix.play();
		mix.setLooping(true);
		mix.setVolume(0);
		
		mixTime = time;
		this.time = 0;
	}
	
	public void update(float delta)
	{
		if (mix != null)
		{
			time += delta;
			
			float vol = volume * (time / mixTime);
			mix.setVolume(vol);
			music.setVolume(volume-vol);
			
			if (time >= mixTime)
			{
				musicName = mixName;
				music.stop();
				music.dispose();
				music = mix;
				mix = null;
				music.setVolume(volume);
			}
		}
	}
}
