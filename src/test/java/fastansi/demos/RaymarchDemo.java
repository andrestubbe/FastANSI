package fastansi.demos;

import fastansi.FastANSI;
import fastansi.density.FastGlyphDensity;
import java.nio.charset.StandardCharsets;

public class RaymarchDemo {
    
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
        // Sphere floating at (0, 0, 5) with Radius 1.5
        double dSphere = p.sub(new Vec3(0, 0, 5)).length() - 1.5;
        // Infinite flat Wall behind the sphere at Z = 10
        double dWall = p.z - 10.0;
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

    public static void main(String[] args) throws Exception {
        System.out.println("FastANSI 3D Raymarcher Demo");
        System.out.println("Initializing Scene & GPU (CPU)...");

        int width = 120;
        int height = 30;

        System.out.write("\033[?25l".getBytes(StandardCharsets.UTF_8)); // Hide Cursor
        
        long startTime = System.currentTimeMillis();
        byte[] frameBuffer = new byte[width * height * 10]; // Large enough for VT sequences

        // Enable VT Synchronized Output for flicker-free rendering
        System.out.write("\033[?2026h".getBytes(StandardCharsets.UTF_8));
        
        try {
            while (true) {
                double time = (System.currentTimeMillis() - startTime) / 1000.0;
                
                // Orbiting Spotlight (Moves left-right and up-down smoothly)
                Vec3 lightPos = new Vec3(
                    Math.sin(time) * 4.0, 
                    2.0 + Math.cos(time * 0.7) * 2.0, 
                    2.0
                );

                StringBuilder sb = new StringBuilder();
                sb.append("\033[H"); // Reset cursor to 0,0

                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        
                        // Normalize Coordinates from -1.0 to 1.0
                        double nx = (double) x / width * 2.0 - 1.0;
                        double ny = (double) y / height * 2.0 - 1.0;
                        
                        // Fix Terminal Aspect Ratio (Font is approx 2:1 height:width)
                        nx *= (double) width / height * 0.5;
                        ny = -ny; // Flip Y axis
                        
                        Vec3 ro = new Vec3(0, 0, 0); // Camera at origin
                        Vec3 rd = new Vec3(nx, ny, 1.0).normalize(); // Ray direction
                        
                        double dist = rayMarch(ro, rd, 20.0);
                        
                        if (dist > 0) {
                            Vec3 hitPos = ro.add(rd.mul(dist));
                            Vec3 normal = calcNormal(hitPos);
                            
                            // Diffuse Lighting (Dot product)
                            Vec3 lightDir = lightPos.sub(hitPos).normalize();
                            double diffuse = Math.max(0.0, normal.dot(lightDir));
                            
                            // Distance Attenuation (Light falls off as it gets further)
                            double lightDist = lightPos.sub(hitPos).length();
                            double attenuation = 1.0 / (1.0 + 0.05 * lightDist * lightDist);
                            diffuse *= attenuation;

                            // Cast Shadow Ray (Does a path from the surface to the light hit the sphere?)
                            double shadowDist = rayMarch(hitPos.add(normal.mul(0.02)), lightDir, lightDist);
                            if (shadowDist > 0 && shadowDist < lightDist) {
                                diffuse *= 0.1; // Hard Shadow
                            }
                            
                            // Ambient light minimum
                            double finalLighting = Math.max(0.02, diffuse);
                            if (finalLighting > 1.0) finalLighting = 1.0;
                            
                            // Get mathematically accurate glyph from our core database
                            char glyph = FastGlyphDensity.getGlyphForOpacity((float) finalLighting);
                            
                            // Add some subtle terminal green glow
                            int r = (int) (0 * finalLighting);
                            int g = (int) (255 * finalLighting);
                            int b = (int) (128 * finalLighting);
                            
                            sb.append(FastANSI.fg(r, g, b)).append(glyph);
                        } else {
                            sb.append(FastANSI.RESET).append(' '); // Empty space
                        }
                    }
                    if (y < height - 1) sb.append("\n");
                }
                
                byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
                System.out.write(bytes);
                System.out.flush();
                
                // Sleep to cap at 60 FPS
                Thread.sleep(16);
            }
        } finally {
            // Restore VT and cursor if interrupted
            System.out.write("\033[?2026l".getBytes(StandardCharsets.UTF_8));
            System.out.write("\033[?25h".getBytes(StandardCharsets.UTF_8)); // Show Cursor
            System.out.println(FastANSI.RESET);
        }
    }
}
