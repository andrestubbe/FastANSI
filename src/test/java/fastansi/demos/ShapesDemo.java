package fastansi.demos;

import fastansi.FastANSI;
import fastansi.density.FastGlyphDensity;
import java.nio.charset.StandardCharsets;

public class ShapesDemo {
    
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
        Vec3 rotateX(double a) {
            double s = Math.sin(a), c = Math.cos(a);
            return new Vec3(x, y * c - z * s, y * s + z * c);
        }
        Vec3 rotateY(double a) {
            double s = Math.sin(a), c = Math.cos(a);
            return new Vec3(x * c + z * s, y, -x * s + z * c);
        }
        Vec3 rotateZ(double a) {
            double s = Math.sin(a), c = Math.cos(a);
            return new Vec3(x * c - y * s, x * s + y * c, z);
        }
    }
    
    static double timeContext = 0.0;

    // Geometry Map (Signed Distance Field)
    static double map(Vec3 p) {
        // Infinite scrolling logic using Modulo
        double spacing = 15.0;
        double halfSpace = spacing / 2.0;
        
        // Subtract time to scroll everything to the Right
        double scrollX = p.x - (timeContext * 2.5);
        double px = ((scrollX + halfSpace) % spacing + spacing) % spacing - halfSpace;
        
        Vec3 lp = new Vec3(px, p.y, p.z);

        // 1. Sphere at X = -5.0
        double dSphere = lp.sub(new Vec3(-5.0, 0, 5.0)).length() - 1.8;

        // 2. Cube (Box) at X = 0.0
        Vec3 boxCenter = lp.sub(new Vec3(0.0, 0, 5.0));
        // Tumble on all 3 axes
        Vec3 bRot = boxCenter.rotateX(timeContext * 1.2).rotateY(timeContext * 0.8).rotateZ(timeContext * 1.5);
        Vec3 qBox = new Vec3(Math.abs(bRot.x) - 1.1, Math.abs(bRot.y) - 1.1, Math.abs(bRot.z) - 1.1);
        double dBox = Math.min(Math.max(qBox.x, Math.max(qBox.y, qBox.z)), 0.0) +
                      new Vec3(Math.max(qBox.x, 0.0), Math.max(qBox.y, 0.0), Math.max(qBox.z, 0.0)).length();

        // 3. Pyramid (Octahedron) at X = 5.0
        Vec3 pyrCenter = lp.sub(new Vec3(5.0, 0, 5.0));
        // Tumble wildly on all 3 axes
        Vec3 pRot = pyrCenter.rotateX(-timeContext * 1.5).rotateY(timeContext * 2.1).rotateZ(-timeContext * 0.9);
        Vec3 qPyr = new Vec3(Math.abs(pRot.x), Math.abs(pRot.y), Math.abs(pRot.z));
        double dPyr = (qPyr.x + qPyr.y + qPyr.z - 2.2) * 0.57735027; // 1/sqrt(3)

        // Infinite flat Wall behind everything at Z = 10 (Does not scroll!)
        double dWall = 10.0 - p.z;
        
        return Math.min(Math.min(dSphere, dBox), Math.min(dPyr, dWall));
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
        System.out.println("FastANSI 3D Primitives Demo (Sphere, Cube, Pyramid)");
        int width = 120;
        int height = 30;

        System.out.write("\033[?25l".getBytes(StandardCharsets.UTF_8)); // Hide Cursor
        long startTime = System.currentTimeMillis();
        
        try {
            while (true) {
                double time = (System.currentTimeMillis() - startTime) / 1000.0;
                timeContext = time; // Global context for map() wrapping
                
                Vec3 light1 = new Vec3(Math.sin(time) * 6.0, 4.0, Math.cos(time) * 4.0);
                Vec3 light2 = new Vec3(Math.sin(time * 0.8) * -7.0, -4.0, 2.0 + Math.cos(time * 0.5) * 3.0); 
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
                            
                            // Check object type (If distance to wall is almost 0, we hit the wall)
                            boolean hitWall = (10.0 - hitPos.z) < 0.1;

                            // Calculate 3 Lights
                            double l1 = calcLighting(hitPos, normal, light1, !hitWall, ro); // Key (White)
                            double l2 = calcLighting(hitPos, normal, light2, false, ro) * 0.5; // Fill (Red)
                            double l3 = calcLighting(hitPos, normal, light3, false, ro) * 1.5; // Rim (Blue)

                            // Base colors
                            int kR = 255, kG = 240, kB = 220; 
                            int fR = 255, fG = 20,  fB = 20;  
                            int rR = 20,  rG = 60,  rB = 255; 

                            double screenDist = Math.sqrt(nx * nx + ny * ny);
                            double ambientAlpha = Math.max(0.0, 1.0 - screenDist * 0.7);
                            double aR = 0, aG = 15 * ambientAlpha, aB = 50 * ambientAlpha;

                            double totalBrightness = l1 + (l2 * 0.6) + (l3 * 0.6) + (ambientAlpha * 0.15);
                            if (totalBrightness > 1.0) totalBrightness = 1.0;
                            
                            char glyph;
                            if (totalBrightness <= 0.02) glyph = ' ';
                            else glyph = FastGlyphDensity.getGlyphForOpacity((float) totalBrightness);
                            
                            double wallDarken = hitWall ? 0.6 : 1.0; 
                            int fgR = (int) Math.min(255, (kR * l1 * wallDarken) + (fR * l2) + (rR * l3) + aR);
                            int fgG = (int) Math.min(255, (kG * l1 * wallDarken) + (fG * l2) + (rG * l3) + aG);
                            int fgB = (int) Math.min(255, (kB * l1 * wallDarken) + (fB * l2) + (rB * l3) + aB);

                            int bgR = 0, bgG = 0, bgB = 0;

                            // Remove tint, leave colors as mathematically calculated
                            sb.append(FastANSI.bg(bgR, bgG, bgB))
                              .append(FastANSI.fg(fgR, fgG, fgB))
                              .append(glyph);
                        } else {
                            sb.append(FastANSI.RESET).append(' '); 
                        }
                    }
                    if (y < height - 1) sb.append(FastANSI.RESET).append("\n"); 
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
