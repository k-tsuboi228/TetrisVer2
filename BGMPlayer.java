package jp.co.abs.tetrisver2_1;

import android.content.Context;
import android.media.MediaPlayer;

public class BGMPlayer {
    private MediaPlayer mediaPlayer;

    public BGMPlayer(Context context){
        this.mediaPlayer = MediaPlayer.create(context, R.raw.tetrisbgm);
        this.mediaPlayer.setLooping(true);
        this.mediaPlayer.setVolume(1.0f,1.0f);
    }

    public void start(){
        if(!mediaPlayer.isPlaying()){
            mediaPlayer.seekTo(0);
            mediaPlayer.start();
        }
    }

    public void stop(){
        if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.prepareAsync();
        }
    }
}
