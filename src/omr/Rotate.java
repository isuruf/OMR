package omr;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.imageio.ImageIO;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Rotate {

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
        File folder = new File("SLMC/");
        File[] listOfFiles = folder.listFiles();

        File dir = new File("SLMC" + File.separator + "Processed");
        if (!dir.exists()) {
            dir.mkdir();
        }
        dir = new File("SLMC" + File.separator + "Unprocessed");
        if (!dir.exists()) {
            dir.mkdir();
        }
        dir = new File("SLMC" + File.separator + "Renamed");
        if (!dir.exists()) {
            dir.mkdir();
        }
        XSSFWorkbook workbook = new XSSFWorkbook();

        XSSFSheet sheet = workbook.createSheet();
        Row row;
        
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                try {
                    if (listOfFiles[i].getName().endsWith(".jpg")) {
                        row = sheet.createRow(i);
                        process(listOfFiles[i].getName(), "SLMC" + File.separator, row);
                        File file = new File("SLMC" + File.separator + listOfFiles[i].getName());
                        File newfile = new File("SLMC" + File.separator + "Processed" + File.separator + listOfFiles[i].getName());
                        file.renameTo(newfile);
                    }

                } catch (Exception e) {
                    File file = new File("SLMC" + File.separator + listOfFiles[i].getName());
                    File newfile = new File("SLMC" + File.separator + "Unprocessed" + File.separator + listOfFiles[i].getName());
                    file.renameTo(newfile);
                }
                System.out.println(listOfFiles[i].getName());
            }
        }

        try {
            String time = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(Calendar.getInstance().getTime());
            FileOutputStream fos = new FileOutputStream(new File("SLMC" + File.separator + "Results-" + time + ".xlsx"));
            workbook.write(fos);
            fos.close();
        } catch (FileNotFoundException ex) {
            System.out.println(ex.getMessage());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

    }

    public static BufferedImage process(String filename, String folder, Row excelRow) throws Exception {
        try {
                
            BufferedImage image
                    = ImageIO.read(new File(folder + filename));
            int width = image.getWidth();
            int height = image.getHeight();
            int rgb, red, green, blue;
            int s = (int) (height * 0.005);
            int limit = (int) (4 * s * s * 0.01);
            double p = 256 * 0.95;

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
                        //            image.setRGB(col, row, 0);
                        black = true;
                    } else {
                        //            image.setRGB(col, row, 16777215);
                    }

                    result[row][col] = black;
                    result2[row][col] = black;
                }
                //  System.out.println("");
            }
            // ImageIO.write(image, "jpg", new File(folder + "Renamed" + File.separator +filename));

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
                        //    image.setRGB(col, row,  0);
                        black = true;
                    } else {
                        //    image.setRGB(col, row, 16777215);
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

            int index = 0;
            
            int c = 0;
            if (filename.contains("-")) {
                int index1 = filename.indexOf("-");
                int index2 = filename.indexOf(".");
                index = Integer.parseInt(filename.substring(index1, index2));
            }
            while (c < 5) {
                index = getIndex(image, p);
                //System.out.println(index);
                if (index < 1000) {
                    p += 2;
                } else if (index >= 10000) {
                    p -= 2;
                } else {
                    break;
                }
                c++;
            }
            if (index < 1000 || index >= 10000) {
                throw new Exception();
            }
            excelRow.createCell(0).setCellValue(index);
            excelRow.createCell(1).setCellValue(filename);

            String[] sol = getSolutions(image);
            for (int q = 0; q < 30; q++) {
                excelRow.createCell(q + 2).setCellValue(sol[q]);
            }

            ImageIO.write(image, "jpg", new File(folder + "Renamed" + File.separator + index + "-" + filename));
            return image;

        } catch (IOException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static void find(boolean[][] found, boolean[][] result, int X, int Y) throws Exception {
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

    public static int getIndex(BufferedImage image, double p1) throws Exception {
        int index = 0;
        int fx, fy, gx, gy, height = image.getHeight(), width = image.getWidth();

        int red, green, blue, rgb;
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                rgb = image.getRGB(col, row);
                red = (rgb >> 16) & 0x000000FF;
                green = (rgb >> 8) & 0x000000FF;
                blue = (rgb) & 0x000000FF;

                if (red < p1 && blue < p1 && green < p1) {
                    rotate[col][row] = true;
                } else {
                    rotate[col][row] = false;
                }
            }
        }

        fx = 727;
        fy = 993;
        gx = 818;
        gy = 1268;

        for (int q = 0; q < 4; q++) {
            int marked=0;
            for (int a = 0; a < 10; a++) {
                int qx = (int) Math.round(q * (gx - fx) / 3.00);
                int qy = (int) Math.round(a * (gy - fy) / 9.00);
                int count = 0;
                for (int i = fx + qx - 7; i <= fx + qx + 7; i++) {
                    for (int j = fy + qy - 7; j <= fy + qy + 7; j++) {

                        if (rotate[i][j]) {
                            count++;
                        }
                        //image.setRGB(i, j, 0);
                    }
                }
                
                if (count > 112) {
                    //System.out.println(" "+q+" "+a+"  "+count);
                    index *= 10;
                    index += (a + 1) % 10;
                    marked++;
                    // break;
                }
            }
            if(marked>1)
                return 10000;

        }
        System.out.print("  " + index + "  ");

        return index;
    }

    public static String[] getSolutions(BufferedImage image) {

        int fx, fy, gx, gy;
        fx = 220;
        fy = 1024;
        gx = 637;
        gy = 1149;
        String[] sol = new String[30];
        for (int q = 0; q < 30; q++) {
            sol[q] = "";
            for (int a = 0; a < 5; a++) {
                int qx = (int) Math.round((q % 15) * (gx - fx) / 14.00);
                int qy = (int) Math.round(a * (gy - fy) / 4.00);
                int count = 0;
                for (int i = fx + qx - 7; i <= fx + qx + 7; i++) {
                    for (int j = fy + qy - 7; j <= fy + qy + 7; j++) {

                        if (rotate[i][j]) {
                            count++;
                        }
                        //image.setRGB(i, j, 0);
                    }
                }
                if (count > 112) {
                    sol[q] += (char) ('A' + a);
                }
            }
            if (sol[q].equals("")) {
                sol[q] = "U";
            } else if (sol[q].length() > 1) {
                sol[q] = "M";
            }

            System.out.print(sol[q]);
            if (q == 14) {
                fy = 1210;
                gy = 1328;
            }
        }
        return sol;
    }
}
