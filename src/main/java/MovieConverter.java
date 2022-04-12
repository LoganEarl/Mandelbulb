import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.OpenCVFrameConverter;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.bytedeco.opencv.helper.opencv_imgcodecs.cvLoadImage;

public class MovieConverter {
    public void convertFramesToMovie(int width, int height) {
        try {
            //noinspection ResultOfMethodCallIgnored
            new File("./movie/").mkdir();

            OpenCVFrameConverter.ToIplImage grabberConverter = new OpenCVFrameConverter.ToIplImage();
            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder("./movie/mandelbulb.mp4", width, height);

            File screenshotDirectory = new File("./frames");
            File[] screenshots = screenshotDirectory.listFiles();

            if (screenshots != null) {
                List<String> fileNames = Arrays.stream(screenshots)
                        .map(File::getAbsolutePath)
                        .filter(name-> name.contains("frame"))
                        .sorted()
                        .collect(Collectors.toList());

                recorder.setFrameRate(30);
                recorder.setVideoCodec(avcodec.AV_CODEC_ID_MPEG4);
                recorder.setVideoBitrate(300000000);
                recorder.setFormat("mp4");
                recorder.setVideoQuality(0);
                //recorder.setAspectRatio(width / (double)height);
                recorder.setImageWidth(width);
                recorder.setImageHeight(height);
                recorder.start();

                for (String fileName : fileNames) {
                    recorder.record(grabberConverter.convert(cvLoadImage(fileName)));
                }

                recorder.stop();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        new MovieConverter().convertFramesToMovie(500, 500);
    }
}
