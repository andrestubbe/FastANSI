package fastansi.demos;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class TestColorCount {
    public static void main(String[] args) throws Exception {
        File file = new File("docs/video.mp4");
        Class<?> grabClass = Class.forName("org.jcodec.api.awt.AWTFrameGrab");
        Class<?> nioClass = Class.forName("org.jcodec.common.io.NIOUtils");
        Object channel = nioClass.getMethod("readableChannel", File.class).invoke(null, file);
        Object grab = grabClass.getMethod("createAWTFrameGrab", Class.forName("org.jcodec.common.io.SeekableByteChannel")).invoke(null, channel);
        
        BufferedImage img = (BufferedImage) grab.getClass().getMethod("getFrame").invoke(grab);
        
        System.out.println("Image type: " + img.getType());
        
        Set<Integer> uniqueColors = new HashSet<>();
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                uniqueColors.add(img.getRGB(x, y) & 0xFFFFFF);
            }
        }
        
        System.out.println("Unique colors in first frame: " + uniqueColors.size());
    }
}
