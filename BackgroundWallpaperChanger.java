package convert;

import javax.swing.SwingWorker;
import javafx.scene.control.TextArea;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class BackgroundWallpaperChanger extends SwingWorker< Boolean, Object > {
    private final String videoPath;
    private final String imgDir;
    private final TextArea eventLog;
    private final String EMOJI_FINISH = "\uD83C\uDFC1";
    private final String EMOJI_WARNING = "⚠";

    public BackgroundWallpaperChanger(String videoPath, String imgDir, TextArea eventLog) {
        this.videoPath = videoPath;
        this.imgDir = imgDir;
        this.eventLog = eventLog;
    }

    public Boolean doInBackground() throws InterruptedException, IOException {
        WallpaperChanger.convertMovietoJPG(videoPath, imgDir, "jpg", 0);
        WallpaperChanger.playWallpaper(imgDir, 0);
        return true;
    }

    protected void done() {
        try {
            get();
        }
        catch (InterruptedException ex) {
            eventLog.appendText(EMOJI_WARNING + "\tThe wallpaper-play has been interrupted.\n");
        }
        catch (ExecutionException ex) {
            eventLog.appendText(EMOJI_WARNING + "\tThe wallpaper-play has been interrupted.\n");
        }
    }
}
