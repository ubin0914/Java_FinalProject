package convert;

import javax.swing.SwingWorker;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class BackgroundMP3toWAV extends SwingWorker< Boolean, Object > {
    private String mp3Path;
    private String wavPath;

    public BackgroundMP3toWAV(String mp3Path, String wavPath) {
        this.mp3Path = mp3Path;
        this.wavPath = wavPath;
    }

    public Boolean doInBackground() throws IOException {
        MP3toWAV.mp3ToWav(mp3Path, wavPath);
        return true;
    }

    protected void done() {
        try {
            get();
        }
        catch (InterruptedException ex) {
            
        }
        catch (ExecutionException ex) {

        }
    }
}
