package fastansi.demos;

import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Method;

public class TestJCodecGetFrame {
    public static void main(String[] args) throws Exception {
        Class<?> grabClass = Class.forName("org.jcodec.api.awt.AWTFrameGrab");
        System.out.println("Methods in AWTFrameGrab:");
        for (Method m : grabClass.getMethods()) {
            System.out.println(m.getName() + " -> " + m.getReturnType().getSimpleName());
        }
    }
}
