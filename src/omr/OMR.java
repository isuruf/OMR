package omr;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 *
 * @author Isuru
 */
public class OMR {

    /**
     * @param args the command line arguments
     */
    static double y, x, cnt;
    final static int maxheight = 5000;
    final static int maxWidth = 5000;
    static boolean[][] result = new boolean[maxheight][maxWidth];
    static boolean[][] result2 = new boolean[maxheight][maxWidth];
    static boolean[][] found = new boolean[maxheight][maxWidth];
    static boolean[][] rotate = new boolean[1020][1415];

    public static void main(String[] args) {
        for (int i = 10001; i < 10068; i++) {
            rotate("Galle" + (i + "").substring(1) + ".jpg", "SLMC/");
            System.out.println(i);

        }

    }

    public static BufferedImage rotate(String filename, String folder) {
        try {

            BufferedImage image
                    = ImageIO.read(new File(folder + filename));
            int width = image.getWidth();
            int height = image.getHeight();
            int rgb, red, green, blue;
            int s = (int) (height * 0.005);
            int limit = (int) (4 * s * s * 0.01);
            double p = 256 * 0.85;

            //System.out.println(width + "  " + height);
            for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
                    found[row][col] = false;
                    rgb = image.getRGB(col, row);
                    red = (rgb >> 16) & 0x000000FF;
                    green = (rgb >> 8) & 0x000000FF;
                    blue = (rgb) & 0x000000FF;
                    boolean black = false;
                    if (red < p && blue < p && green < p) {
                        //    image.setRGB(col, row,  0);
                        black = true;
                    } else {
                        //    image.setRGB(col, row, 16777215);
                    }

                    result[row][col] = black;
                    result2[row][col] = black;
                }
                //  System.out.println("");
            }
            // ImageIO.write(image, "jpg", new File("output1.jpg"));
            s = (int) (height * 0.005);
            limit = (int) (4 * s * s * 0.05);
            //System.out.println(s+"  "+limit);
            for (int row = s; row < height - s; row++) {
                for (int col = s; col < width - s; col++) {
                    int count = 0;
                    boolean black = false;
                    for (int i = row - s; (i < row + s) && count <= limit; i++) {
                        for (int j = col - s; (j < col + s) && count <= limit; j++) {
                            if (!result[i][j]) {
                                count++;
                            }
                        }
                    }
                    if (count <= limit) {
//                        image.setRGB(col, row,  0);
                        black = true;
                    } else {
                        //                      image.setRGB(col, row, 16777215);
                    }
                    result2[row][col] = black;

                }
            }

            int y1 = 0, y2 = 0, x1 = 0, x2 = 0, count1 = 0, count2 = 0;

            for (int row = s; row < height / 8; row++) {
                for (int col = s; col < width / 2; col++) {
                    if (result2[row][col]) {
                        y1 += row;
                        x1 += col;
                        count1++;
                        //    System.out.println(col+ "  "+row);
                    }
                }
            }

            for (int row = s; row < height / 8; row++) {
                for (int col = (int) width / 2; col < width - s; col++) {
                    if (result2[row][col]) {
                        y2 += row;
                        x2 += col;
                        count2++;
                        //    System.out.println(col+ "  "+row);
                    }
                }
            }

            y1 = (int) (y1 / count1);
            x1 = (int) (x1 / count1);
            y2 = (int) (y2 / count2);
            x2 = (int) (x2 / count2);
            //System.out.println(x2+"  "+x1+" "+y2+"  "+y1);

            y = 0;
            x = 0;
            cnt = 0;
            find(found, result, y1, x1);

            y1 = (int) (y / cnt);
            x1 = (int) (x / cnt);
            //System.out.println(x2+"  "+x1+" "+y2+"  "+y1);
            y = 0;
            x = 0;
            cnt = 0;
            find(found, result, y2, x2);
            y2 = (int) (y / cnt);
            x2 = (int) (x / cnt);

            double r = Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
            AffineTransform transform = new AffineTransform();
            transform.scale(1000 / r, 1000 / r);
            transform.rotate(-Math.atan2(y2 - y1, x2 - x1), x1, y1);
            transform.translate(-x1, -y1);
            AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
            image = op.filter(image, null);
            image = image.getSubimage(0, 0, 1020, 1415);
            File dir = new File(folder + "Rotated");

            if (!dir.exists()) {
                dir.mkdir();
            }
            ImageIO.write(image, "jpg", new File(folder + "Rotated" + File.separator + filename));
            width = image.getWidth();
            height = image.getHeight();

            //System.out.println(width + "  " + height);
            for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
                    rotate[col][row] = false;
                    if (image.getRGB(col, row) == 0) {
                        rotate[col][row] = true;
                    }
                }
            }
            return image;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static void find(boolean[][] found, boolean[][] result, int X, int Y) {
        if (!found[X][Y] && result[X][Y]) {
            found[X][Y] = true;
            cnt++;
            y += X;
            x += Y;
            find(found, result, X - 1, Y);
            find(found, result, X, Y - 1);
            find(found, result, X, Y + 1);
            find(found, result, X + 1, Y);
        }
    }
}
