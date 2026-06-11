package fastansi.demos;

import fastansi.FastANSI;
import fastansi.density.FastGlyphDensity;
import java.nio.charset.StandardCharsets;

public class MultiLightDemo {
    
    // Lightweight Hyper-Optimized Vector Math
    static class Vec3 {
        final double x, y, z;
        Vec3(double x, double y, double z) { this.x = x; this.y = y; this.z = z; }
        Vec3 add(Vec3 v) { return new Vec3(x + v.x, y + v.y, z + v.z); }
        Vec3 sub(Vec3 v) { return new Vec3(x - v.x, y - v.y, z - v.z); }
        Vec3 mul(double s) { return new Vec3(x * s, y * s, z * s); }
        double dot(Vec3 v) { return x * v.x + y * v.y + z * v.z; }
        double length() { return Math.sqrt(x * x + y * y + z * z); }
        Vec3 normalize() {
            double l = length();
            return l == 0 ? new Vec3(0, 0, 0) : new Vec3(x / l, y / l, z / l);
        }
    }

    // Geometry Map (Signed Distance Field)
    static double map(Vec3 p) {
        // Sphere floating at (0, 0, 5) with Radius 2.625
        double dSphere = p.sub(new Vec3(0, 0, 5)).length() - 2.625;
        // Infinite flat Wall behind the sphere at Z = 10
        double dWall = 10.0 - p.z;
        return Math.min(dSphere, dWall);
    }

    // Surface Normal Calculation
    static Vec3 calcNormal(Vec3 p) {
        double e = 0.001;
        return new Vec3(
            map(new Vec3(p.x + e, p.y, p.z)) - map(new Vec3(p.x - e, p.y, p.z)),
            map(new Vec3(p.x, p.y + e, p.z)) - map(new Vec3(p.x, p.y - e, p.z)),
            map(new Vec3(p.x, p.y, p.z + e)) - map(new Vec3(p.x, p.y, p.z - e))
        ).normalize();
    }

    // Shoot Ray
    static double rayMarch(Vec3 ro, Vec3 rd, double maxDist) {
        double t = 0.0;
        for (int i = 0; i < 80; i++) {
            Vec3 p = ro.add(rd.mul(t));
            double d = map(p);
            if (d < 0.001) return t; // Hit
            t += d;
            if (t > maxDist) break; // Miss
        }
        return -1;
    }

    static double calcLighting(Vec3 hitPos, Vec3 normal, Vec3 lightPos, boolean isKeyLight, Vec3 ro) {
        Vec3 lightDir = lightPos.sub(hitPos).normalize();
        double diffuse = Math.max(0.0, normal.dot(lightDir));
        
        // Distance Attenuation
        double lightDist = lightPos.sub(hitPos).length();
        double attenuation = 1.0 / (1.0 + 0.02 * lightDist * lightDist);
        diffuse *= attenuation * 2.0;

        // Specular Highlights (Only for Key Light)
        double specular = 0.0;
        if (isKeyLight) {
            Vec3 viewDir = ro.sub(hitPos).normalize();
            Vec3 reflectDir = lightDir.mul(-1).add(normal.mul(2.0 * normal.dot(lightDir))).normalize();
            double specAngle = Math.max(0.0, viewDir.dot(reflectDir));
            specular = Math.pow(specAngle, 16.0) * 0.8;
        }

        // Shadows
        double shadowDist = rayMarch(hitPos.add(normal.mul(0.02)), lightDir, lightDist);
        if (shadowDist > 0 && shadowDist < lightDist) {
            diffuse *= 0.05; // Hard Shadow
            specular = 0.0;
        }

        double total = diffuse + specular;
        if (total > 1.0) total = 1.0;
        if (total < 0.0) total = 0.0;
        return total;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("FastANSI 3-Point Studio Lighting Demo");
        int width = 120;
        int height = 30;

        System.out.write("\033[?25l".getBytes(StandardCharsets.UTF_8)); // Hide Cursor
        long startTime = System.currentTimeMillis();
        
        try {
            while (true) {
                double time = (System.currentTimeMillis() - startTime) / 1000.0;
                
                // 1. Key Light (White/Beige) - Orbiting above and around front
                Vec3 light1 = new Vec3(Math.sin(time) * 6.0, 4.0, Math.cos(time) * 4.0);
                
                // 2. Fill Light (Red) - Orbiting below and side-to-side
                Vec3 light2 = new Vec3(Math.sin(time * 0.8) * -7.0, -4.0, 2.0 + Math.cos(time * 0.5) * 3.0); 
                double l2Pulse = 0.5 + Math.sin(time * 1.3) * 0.5; // Slight pulsing intensity
                
                // 3. Rim Light (Blue) - Orbiting wildly from back to front
                Vec3 light3 = new Vec3(Math.sin(time * -0.6) * 9.0, Math.cos(time * 0.4) * 5.0, 8.0 + Math.cos(time * -0.9) * 5.0);

                StringBuilder sb = new StringBuilder();
                sb.append("\033[?2026h"); // Start synchronized update
                sb.append("\033[H"); // Reset cursor to 0,0

                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        
                        double nx = (double) x / width * 2.0 - 1.0;
                        double ny = (double) y / height * 2.0 - 1.0;
                        nx *= (double) width / height * 0.5;
                        ny = -ny; 
                        
                        Vec3 ro = new Vec3(0, 0, 0); 
                        Vec3 rd = new Vec3(nx, ny, 1.0).normalize(); 
                        
                        double dist = rayMarch(ro, rd, 20.0);
                        
                        if (dist > 0) {
                            Vec3 hitPos = ro.add(rd.mul(dist));
                            Vec3 normal = calcNormal(hitPos);
                            
                            // Check object type
                            boolean hitWall = hitPos.sub(new Vec3(0, 0, 5)).length() - 2.625 > 0.1;

                            // Calculate 3 Lights
                            double l1 = calcLighting(hitPos, normal, light1, !hitWall, ro); // Key (White)
                            double l2 = calcLighting(hitPos, normal, light2, false, ro) * l2Pulse; // Fill (Red)
                            double l3 = calcLighting(hitPos, normal, light3, false, ro) * 1.5; // Rim (Blue)

                            
                            // Calculate base colors for the 3 lights
                            int kR = 255, kG = 240, kB = 220; // Key Light: Warm Beige/Yellowish
                            int fR = 255, fG = 20,  fB = 20;  // Fill Light: Deep Red
                            int rR = 20,  rG = 60,  rB = 255; // Rim Light: Blue

                            // Ambient Dim Blue Light with a radial falloff from the center
                            double screenDist = Math.sqrt(nx * nx + ny * ny);
                            double ambientAlpha = Math.max(0.0, 1.0 - screenDist * 0.7);
                            double aR = 0, aG = 15 * ambientAlpha, aB = 50 * ambientAlpha;

                            // Combine all lights to determine the total physical brightness
                            double totalBrightness = l1 + (l2 * 0.6) + (l3 * 0.6) + (ambientAlpha * 0.15);
                            if (totalBrightness > 1.0) totalBrightness = 1.0;
                            
                            // ALL lights now drive the ASCII character density!
                            char glyph;
                            if (totalBrightness <= 0.02) glyph = ' ';
                            else glyph = FastGlyphDensity.getGlyphForOpacity((float) totalBrightness);
                            
                            // True Color Mixing for the Foreground (ASCII Text Color)
                            double wallDarken = hitWall ? 0.6 : 1.0; // Dim the wall slightly so the sphere pops
                            int fgR = (int) Math.min(255, (kR * l1 * wallDarken) + (fR * l2) + (rR * l3) + aR);
                            int fgG = (int) Math.min(255, (kG * l1 * wallDarken) + (fG * l2) + (rG * l3) + aG);
                            int fgB = (int) Math.min(255, (kB * l1 * wallDarken) + (fB * l2) + (rB * l3) + aB);

                            // Background is pitch black, letting the text do all the work
                            int bgR = 0, bgG = 0, bgB = 0;

                            sb.append(FastANSI.bg(bgR, bgG, bgB))
                              .append(FastANSI.fg(fgR, fgG, fgB))
                              .append(glyph);
                        } else {
                            sb.append(FastANSI.RESET).append(' '); 
                        }
                    }
                    if (y < height - 1) sb.append(FastANSI.RESET).append("\n"); // Reset to prevent bg bleeding to next line
                }
                sb.append("\033[?2026l"); // Commit frame
                
                System.out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
                System.out.flush();
                Thread.sleep(16);
            }
        } finally {
            System.out.write("\033[?2026l".getBytes(StandardCharsets.UTF_8));
            System.out.write("\033[?25h".getBytes(StandardCharsets.UTF_8)); // Show Cursor
            System.out.println(FastANSI.RESET);
        }
    }
}
