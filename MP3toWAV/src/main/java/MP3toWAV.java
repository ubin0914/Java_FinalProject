import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MP3toWAV {

    private static AudioFormat targetFormat = null;
    //mp3傳pcm
    private static boolean mp3ToPcm(String mp3filepath, String pcmfilepath){
        try {
            //獲得音頻串流
            AudioInputStream audioInputStream = getPcmAudioInputStream(mp3filepath);
            //將音頻存為pcm檔
            AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, new File(pcmfilepath));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    //獲取mp3檔案的pcm格式音頻串流
    private static AudioInputStream getPcmAudioInputStream(String mp3filepath) {
        File mp3 = new File(mp3filepath);
        AudioInputStream audioInputStream = null;

        try {
            AudioInputStream in = null;
            //讀取音檔的類別
            MpegAudioFileReader mp = new MpegAudioFileReader();
            in = mp.getAudioInputStream(mp3);
            AudioFormat baseFormat = in.getFormat();
            //設定輸出格式為pcm格式
            targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16,
                    baseFormat.getChannels(), baseFormat.getChannels()*16/8, baseFormat.getFrameRate(), false);
            //輸出到音頻串流
            audioInputStream = AudioSystem.getAudioInputStream(targetFormat, in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return audioInputStream;
    }
    //pcm(8k 16bit)轉wav(16k 16bit)
    private static void pcmToWav(String pcmfilepath,String wavfilepath) throws IOException {
        FileInputStream fis = new FileInputStream(pcmfilepath);
        byte channels = (byte) targetFormat.getChannels();
        int sampleRate = (int) targetFormat.getSampleRate();
        int byteRate = targetFormat.getFrameSize()*sampleRate*channels/8;
        int datalen = (int)fis.getChannel().size();
        ByteBuffer bb = ByteBuffer.allocate(44);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(new byte[] {'R','I','F','F'});//RIFF標記
        bb.putInt(datalen+44-8);//原始數據長度（不包含RIFF和本字段共8個字節）
        bb.put(new byte[] {'W','A','V','E'});//WAVE標記
        bb.put(new byte[] {'f','m','t',' '});//fmt標記
        bb.putInt(16);//“fmt”字段的長度，存儲該子塊的字節數（不含前面的Subchunk1ID和Subchunk1Size這8個字節）
        bb.putShort((short)1);//存儲音頻文件的編碼格式，PCM其存儲值為1
        bb.putShort((short) targetFormat.getChannels());//通道數，單通道(Mono)值為1，雙通道(Stereo)值為2
        //採樣率
        bb.putInt(sampleRate);
        //音頻數據傳送速率,採樣率*通道數*採樣率深度/8。(每秒存儲的bit數，其值=SampleRate * NumChannels * BitsPerSample/8)
        bb.putInt(byteRate);
        //塊對齊/幀率大小，NumChannels * BitsPerSample/8
        bb.putShort((short)(targetFormat.getChannels()*16/8));
        //pcm數據位數，一般為8,16,32等
        bb.putShort((short)16);
        bb.put(new byte[] {'d','a','t','a'});//data標記
        bb.putInt(datalen);//data數據長度
        byte[] header = bb.array();
        /*wav頭
        for(int i=0;i<header.length;i++) {
            System.out.printf("%02x ",header[i]);
        }*/
        ByteBuffer wavbuff = ByteBuffer.allocate(44+datalen);
        wavbuff.put(header);
        byte[] temp = new byte[datalen];
        fis.read(temp);
        wavbuff.put(temp);
        byte[] wavbytes = wavbuff.array();
        FileOutputStream fos = new FileOutputStream(wavfilepath);
        fos.write(wavbytes);
        fos.flush();
        fos.close();
        fis.close();
        System.out.println("end");
    }
    //對外提供功能
    public static void mp3ToWav(String mp3Path, String wavPath) throws IOException {
        String pcmPath = "D:\\temp.pcm";    //轉化過程的pcm路徑
        mp3ToPcm(mp3Path,pcmPath);
        pcmToWav(pcmPath,wavPath);
        (new File(pcmPath)).delete();   //刪除pcm檔
    }

    public static void main(String[] args) throws IOException {
        String mp3Path = "C:\\486.mp3";
        String wavPath = "D:\\test.wav";
        mp3ToWav(mp3Path, wavPath);
    }
}