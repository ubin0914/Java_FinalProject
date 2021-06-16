package convert;

import javax.swing.SwingWorker;
import javafx.scene.control.TextArea;
import java.util.concurrent.ExecutionException;

public class BackgroundExportMp4 extends SwingWorker< Boolean, Object > {
    private String jpgDirPath;
    private String audioPath;
    private float fps;
    private int width;
    private int height;
    private String exportPath;
    private int imgNum;
    private final TextArea eventLog;
    private final String EMOJI_FINISH = "\uD83C\uDFC1";
    private final String EMOJI_WARNING = "⚠";

    public BackgroundExportMp4(String jpgDirPath, String audioPath, float fps, int width, int height, String exportPath, int imgNum, TextArea eventLog) {
        this.jpgDirPath = jpgDirPath;
        this.audioPath = audioPath;
        this.fps = fps;
        this.width = width;
        this.height = height;
        this.exportPath = exportPath;
        this.imgNum = imgNum;
        this.eventLog = eventLog;
    }

    public Boolean doInBackground() throws Exception {
        ExportMp4.exportMp4(jpgDirPath, audioPath, fps, width, height, exportPath);

        for (int i = 1; i <= imgNum; i++) {
            java.io.File imgFile = new java.io.File(String.format("image/%06d.jpg", i));
            imgFile.delete();
        }
        return true;
    }

    protected void done() {
        try {
            if(get())
                eventLog.appendText(EMOJI_FINISH + "\tThe file has been exported correctly.\n");
        }
        catch (InterruptedException ex) {
            eventLog.appendText(EMOJI_WARNING + "\tInterrupted while waiting for export\n");
        }
        catch (ExecutionException ex) {
            eventLog.appendText(EMOJI_WARNING + "\tThe file can not be exported correctly.\n");
        }
    }
}