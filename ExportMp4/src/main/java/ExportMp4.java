import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import cn.hutool.core.util.ObjectUtil;

import org.jim2mov.core.DefaultMovieInfoProvider;
import org.jim2mov.core.ImageProvider;
import org.jim2mov.core.Jim2Mov;
import org.jim2mov.core.MovieInfoProvider;
import org.jim2mov.core.MovieSaveException;
import org.jim2mov.utils.MovieUtils;
import ws.schild.jave.*;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.encode.VideoAttributes;
import ws.schild.jave.info.MultimediaInfo;
import ws.schild.jave.info.VideoInfo;
import ws.schild.jave.info.VideoSize;

public class ExportMp4 {

    private static void jpgsToAvi(String jpgDirPath, String aviFileName, int fps, int mWidth, int mHeight) {
        final File[] jpgs = new File(jpgDirPath).listFiles();
        if (jpgs == null || jpgs.length == 0) {
            return;
        }
        // 生成視訊的名稱
        DefaultMovieInfoProvider dmip = new DefaultMovieInfoProvider(aviFileName);
        // 設定每秒幀數
        dmip.setFPS(fps);
        // 設定總幀數
        dmip.setNumberOfFrames(jpgs.length);
        // 設定視訊寬和高（最好與圖片寬高保持一直）
        dmip.setMWidth(mWidth);
        dmip.setMHeight(mHeight);
        try {
            new Jim2Mov(new ImageProvider() {
                @Override
                public byte[] getImage(int frame) {
                    try {
                        // 設定壓縮比
                        return MovieUtils.convertImageToJPEG((jpgs[frame]), 1.0f);
                    } catch (IOException e) {
                        System.err.println(e);
                    }
                    return null;
                }
            }, dmip, null).saveMovie(MovieInfoProvider.TYPE_QUICKTIME_JPEG);
        } catch (MovieSaveException e) {
            System.err.println(e);
        }
    }

    private static void aviToMp4(File source, String targetPath) {
        MultimediaObject multimediaObject = new MultimediaObject(source);
        try {
            MultimediaInfo info = multimediaObject.getInfo();
            VideoInfo videoInfo = info.getVideo();
            VideoSize size = videoInfo.getSize();
            Integer bitRate = size.getWidth()*size.getHeight();
            VideoAttributes video = new VideoAttributes();
            //設定視訊編碼
            video.setCodec("h264");
            if (ObjectUtil.isNotNull(bitRate)) {
                //設定位元率
                video.setBitRate(bitRate * 1000);
            }
            File target = new File(targetPath);
            EncodingAttributes attrs = new EncodingAttributes();
            //設定轉換後的格式
            attrs.setOutputFormat("mp4");
            attrs.setVideoAttributes(video);
            Encoder encoder = new Encoder();
            encoder.encode(multimediaObject, target, attrs);
        } catch (EncoderException e) {
            e.printStackTrace();
        }
    }

    private static void mergeAudio(String videoPath, String audioPath, String outPut) throws Exception {
        FrameRecorder recorder = null;
        FrameGrabber grabber1 = null;
        FrameGrabber grabber2 = null;
        try {
            //抓取影檔幀數
            grabber1 = new FFmpegFrameGrabber(videoPath);
            //抓取音檔幀數
            grabber2 = new FFmpegFrameGrabber(audioPath);
            grabber1.start();
            grabber2.start();
            //創建錄製
            recorder = new FFmpegFrameRecorder(outPut,
                    grabber1.getImageWidth(), grabber1.getImageHeight(),
                    grabber2.getAudioChannels());

            recorder.setFormat("mp4");
            recorder.setFrameRate(grabber1.getFrameRate());
            recorder.setSampleRate(grabber2.getSampleRate());
            recorder.start();

            Frame frame1;
            Frame frame2 ;
            //錄入影檔
            while ((frame1 = grabber1.grabFrame()) != null ){
                recorder.record(frame1);
            }
            //錄入音檔
            while ((frame2 = grabber2.grabFrame()) != null) {
                recorder.record(frame2);
            }
            grabber1.stop();
            grabber2.stop();
            recorder.stop();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (recorder != null) {
                    recorder.release();
                }
                if (grabber1 != null) {
                    grabber1.release();
                }
                if (grabber2 != null) {
                    grabber2.release();
                }
            } catch (FrameRecorder.Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void exportMp4(String jpgDirPath, String audioPath, int fps, int width, int height, String exportPath) throws Exception {
        //將資料夾中的全部圖片(jpg)轉為mov檔再轉為mp4檔後再嵌入mp3融合出新的mp4
        jpgsToAvi(jpgDirPath, "temp.mov", fps, width, height);
        aviToMp4(new File("temp.mov"), "temp.mp4");
        mergeAudio("temp.mp4", audioPath, exportPath);
        //刪掉過程檔案
        (new File("temp.mov")).delete();
        (new File("temp.mp4")).delete();
    }

    public static void main(String[] args) throws Exception {
        //顧名思義
        String jpgDirPath = "C:/java_final/convert/";   //來源路徑
        String audioPath = "audio.mp3";     //來源路徑
        String exportPath = "video.mp4";    //創建路徑
        int fps = 30;
        int width = 1280;
        int height = 720;

        exportMp4(jpgDirPath, audioPath, fps, width, height, exportPath);
    }
}