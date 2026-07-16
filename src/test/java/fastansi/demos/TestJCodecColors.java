package fastansi.demos;

import java.awt.image.BufferedImage;
import java.io.File;

public class TestJCodecColors {
    public static void main(String[] args) throws Exception {
        File file = new File("docs/video.mp4");
        Class<?> grabClass = Class.forName("org.jcodec.api.awt.AWTFrameGrab");
        Class<?> nioClass = Class.forName("org.jcodec.common.io.NIOUtils");
        Object channel = nioClass.getMethod("readableChannel", File.class).invoke(null, file);
        Object grab = grabClass.getMethod("createAWTFrameGrab", Class.forName("org.jcodec.common.io.SeekableByteChannel")).invoke(null, channel);
        
        Object pic = grab.getClass().getMethod("getNativeFrame").invoke(grab);
        Class<?> awtUtil = Class.forName("org.jcodec.scale.AWTUtil");
        BufferedImage img = (BufferedImage) awtUtil.getMethod("toBufferedImage", Class.forName("org.jcodec.common.model.Picture")).invoke(null, pic);
        
        System.out.println("Image type: " + img.getType());
        System.out.println("Image width: " + img.getWidth() + " height: " + img.getHeight());
        
        // Check middle pixel
        int cx = img.getWidth() / 2;
        int cy = img.getHeight() / 2;
        int rgb = img.getRGB(cx, cy);
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        System.out.printf("Middle pixel: R=%d, G=%d, B=%d\n", r, g, b);
        
        // Sample a few more
        for (int i=0; i<5; i++) {
            rgb = img.getRGB(i*10, i*10);
            r = (rgb >> 16) & 0xFF;
            g = (rgb >> 8) & 0xFF;
            b = rgb & 0xFF;
            System.out.printf("Pixel (%d,%d): R=%d, G=%d, B=%d\n", i*10, i*10, r, g, b);
        }
    }
}
