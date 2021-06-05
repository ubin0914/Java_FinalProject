import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import javax.imageio.ImageIO;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.FrameGrabber.Exception;

public class WallpaperChanger {
    public static native int putImage(String path);
    static {
        System.loadLibrary("wallpaperchanger");
    }
    public static void playWallpaper(String path, int sleepTime) throws InterruptedException {
        System.out.println("Starting to replay video after every "+(sleepTime/1000)+"s");
        File f=new File(path);
        File[] filePath=f.listFiles();
        while(true) {
            for(int i=5;i<=filePath.length;i++) {
                putImage(path+"\\"+i+".jpg");
            }
            Thread.sleep(sleepTime);
        }
    }
    public static void convertMovietoJPG(String mp4Path, String imagePath, String imgType, int frameJump) throws Exception, IOException {
        Java2DFrameConverter converter = new Java2DFrameConverter();
        FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(mp4Path);
        frameGrabber.start();
        Frame frame;
        double frameRate=frameGrabber.getFrameRate();
        int imgNum=0;
        System.out.println("Video has "+frameGrabber.getLengthInFrames()+" frames and has frame rate of "+frameRate);
        try {
            for(int ii=1;ii<=frameGrabber.getLengthInFrames();ii++){
                imgNum++;
                frameGrabber.setFrameNumber(ii);
                frame = frameGrabber.grab();
                BufferedImage bi = converter.convert(frame);
                String path = imagePath+File.separator+imgNum+".jpg";
                ImageIO.write(bi,imgType, new File(path));
                ii+=frameJump;
            }
            frameGrabber.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws InterruptedException, Exception, IOException {
        String mp4Path="C:\\oozora_subaru.mp4";
        File imgPath=new File("convert");
        imgPath.delete();
        imgPath.mkdirs();
        String imagePath=imgPath.getAbsolutePath();
        //convertMovietoJPG(mp4Path, imagePath,"jpg",0);    //第一次用請開啟

        System.out.println("Conversion complete. Please find the images at "+imagePath);
        playWallpaper(imagePath,5000);
    }
}