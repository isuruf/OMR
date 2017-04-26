package io.github.isuruf.omr;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import javax.imageio.ImageIO;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Recognize {

    static double y, x, cnt;
    static int ux, uy, vx, vy;
    final static int maxheight = 5000;
    final static int maxWidth = 5000;
    static boolean[][] result = new boolean[maxheight][maxWidth];
    static boolean[][] result2 = new boolean[maxheight][maxWidth];
    static boolean[][] found = new boolean[maxheight][maxWidth];
    static boolean[][] rotate = new boolean[1020][1435];
    static String folderPath = "samples";
    static int solutionCount = 62;
    static int radius = 5;

    public static void main(String[] args) {
        //process();
        //renameByRank();
        App.main(args);
    }

    public static void process() {
        File folder = new File(folderPath);
        File[] listOfFiles = folder.listFiles();

        File dir = new File(folderPath + File.separator + "Processed");
        if (!dir.exists()) {
            dir.mkdir();
        }
        dir = new File(folderPath + File.separator + "Unprocessed");
        if (!dir.exists()) {
            dir.mkdir();
        }
        dir = new File(folderPath + File.separator + "ByIndex");
        if (!dir.exists()) {
            dir.mkdir();
        }
        dir = new File(folderPath + File.separator + "tmp");
        if (!dir.exists()) {
            dir.mkdir();
        }
        dir = new File(folderPath + File.separator + "Check");
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
                        System.out.println(listOfFiles[i].getName());
                        processImage(listOfFiles[i].getName(), folderPath + File.separator, row);
                        File file = new File(folderPath + File.separator + listOfFiles[i].getName());
                        File newfile = new File(folderPath + File.separator + "Processed" + File.separator + listOfFiles[i].getName());
                        file.renameTo(newfile);
                    }

                } catch (Exception e) {
                    File file = new File(folderPath + File.separator + listOfFiles[i].getName());
                    File newfile = new File(folderPath + File.separator + "Unprocessed" + File.separator + listOfFiles[i].getName());
                    file.renameTo(newfile);
                }
                System.out.println(listOfFiles[i].getName());
            }
        }

        try {
            String time = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(Calendar.getInstance().getTime());
            FileOutputStream fos = new FileOutputStream(new File(folderPath + File.separator + "Results-" + time + ".xlsx"));
            workbook.write(fos);
            fos.close();
        } catch (FileNotFoundException ex) {
            System.out.println(ex.getMessage());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

    }

    private static BufferedImage processImage(String filename, String folder, Row excelRow) throws Exception {
        try {

            BufferedImage image
                    = ImageIO.read(new File(folder + filename));
            int width = image.getWidth();
            int height = image.getHeight();
            int rgb, red, green, blue;
            int s = (int) (height * 0.005);
            int limit = (int) (4 * s * s * 0.01);
            double p = 256 * 0.90;


//            ImageIO.write(image, "jpg", new File(folder + "tmp" + File.separator +filename));

            // System.out.println(width + "  " + height);
            for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
                    found[row][col] = false;
                    rgb = image.getRGB(col, row);
                    red = (rgb >> 16) & 0x000000FF;
                    green = (rgb >> 8) & 0x000000FF;
                    blue = (rgb) & 0x000000FF;
                    boolean black = false;
                    if (red < p && blue < p && green < p) {
                        // image.setRGB(col, row, 0);
                        black = true;
                    } else {
                        // image.setRGB(col, row, 16777215);
                    }

                    result[row][col] = black;
                    result2[row][col] = black;
                }
                //  System.out.println("");
            }
            //ImageIO.write(image, "jpg", new File(folder + "tmp" + File.separator +filename));

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
                        // image.setRGB(col, row, 0);
                        black = true;
                    } else {
                        // image.setRGB(col, row, 16777215);
                    }
                    result2[row][col] = black;

                }
            }

            int y1 = 0, y2 = 0, x1 = 0, x2 = 0, count1 = 0, count2 = 0;
            outerloop:
            for (int row = height - 2 * s; row > 3 * height / 4; row--) {
                for (int col = 2 * s; col < width / 2; col++) {
                    if (result2[row][col]) {
                        y1 += row;
                        x1 += col;
                        count1++;
                        break outerloop;
                        // System.out.println(col+ "  "+row);
                    }
                }
            }

            outerloop2:
            for (int row = height - 2 * s; row > 3 * height / 4; row--) {
                for (int col = (int) width / 2; col < width - 2 * s; col++) {
                    if (result2[row][col]) {
                        y2 += row;
                        x2 += col;
                        count2++;
                        break outerloop2;
                        //    System.out.println(col+ "  "+row);
                    }
                }
            }

            y1 = (int) (y1 / count1);
            x1 = (int) (x1 / count1);
            y2 = (int) (y2 / count2);
            x2 = (int) (x2 / count2);
            System.out.println(x2 + "  " + x1 + " " + y2 + "  " + y1);

            y = 0;
            x = 0;
            cnt = 0;
            find(found, result, y1, x1, true);

            y1 = (int) (y / cnt);
            x1 = (int) (x / cnt);
            y = 0;
            x = 0;
            cnt = 0;
            find(found, result, y2, x2, true);
            y2 = (int) (y / cnt);
            x2 = (int) (x / cnt);

            System.out.println(filename+"  "+x2+"  "+x1+" "+y2+"  "+y1);

            double r = Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
            AffineTransform transform;
            AffineTransformOp op;

            transform = new AffineTransform();
            transform.rotate(-Math.atan2(y2 - y1, x2 - x1), x1, y1);
            op = new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
            image = op.filter(image, null);

            transform = new AffineTransform();
            transform.translate(-x1, 0);
            op = new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
            image = op.filter(image, null);

            transform = new AffineTransform();
            transform.scale(1000 / r, 1000 / r);
            op = new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
            image = op.filter(image, null);

            image = image.getSubimage(0, (int) (y1 * 1000 / r - 1435), 1020, 1435);

            ImageIO.write(image, "jpg", new File(folder + "tmp" + File.separator + "asd" + filename));

            int index = 0;

            int c = 0;
            boolean manual = false;
            if (filename.contains("-")) {
                manual = true;
                int index1 = filename.indexOf("-");
                int index2 = filename.indexOf(".");
                index = Integer.parseInt(filename.substring(index1 + 1, index2));
                //System.out.println("manual "+index);
                filename = filename.substring(0, index1) + filename.substring(index2);
            }
            while (c < 5 && !manual) {
                index = getIndex(image, p);
                //System.out.println(index);
                if (index < 1000) {
                    p += 2;
                } else if (index >= 10000) {
                    p -= 2;
                } else {
                    break;
                }
                //ImageIO.write(image, "jpg", new File(folder + "tmp" + File.separator +filename));
                c++;
            }
            if (manual) {
                getIndex(image, p);
            }
            if (index < 1000 || index >= 10000) {
                //throw new Exception();
            }
            ImageIO.write(image, "jpg", new File(folder + "ByIndex" + File.separator + index + "-" + filename));
            excelRow.createCell(0).setCellValue(index);
            excelRow.createCell(1).setCellValue(filename);

            width = image.getWidth();
            height = image.getHeight();
            for (int row = 0; row < height / 2; row++) {
                for (int col = 0; col < width; col++) {
                    image.setRGB(col, row, image.getRGB(col, row + height / 2));
                }
            }

            String[] sol = getSolutions(image);
            for (int q = 0; q < 30; q++) {
                excelRow.createCell(q + 2).setCellValue(sol[q]);
            }

            ImageIO.write(image, "jpg", new File(folder + "Check" + File.separator + index + "-" + filename));

            return image;

        } catch (IOException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    private static void find(boolean[][] found, boolean[][] result, int X, int Y, boolean first) throws Exception {
        if (!found[X][Y] && (first || result[X][Y])) {
            found[X][Y] = true;
            cnt++;
            y += X;
            x += Y;
            find(found, result, X - 1, Y, false);
            find(found, result, X, Y - 1, false);
            find(found, result, X, Y + 1, false);
            find(found, result, X + 1, Y, false);
        }
    }

    private static int getIndex(BufferedImage image, double p1) throws Exception {
        int index = 0;
        boolean firstMark = true;
        int fx, fy, gx, gy, height = image.getHeight(), width = image.getWidth();
        int x1 = 0, x2 = 0, x3 = 0, y1 = 0, y2 = 0, y3 = 0, c1 = 0, c2 = 0, c3 = 0, r1 = 0, r2 = 0, r3 = 0;

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

        fx = 723;
        fy = 1425 - 413;
        gx = 813;
        gy = 1435 - 146;
        boolean ret = false;
        for (int q = 0; q < 4; q++) {
            int marked = 0;
            for (int a = 0; a < 10; a++) {
                int qx = (int) Math.round(q * (gx - fx) / 3.00);
                int qy = (int) Math.round(a * (gy - fy) / 9.00);
                int count = 0;
                for (int i = fx + qx - 7; i <= fx + qx + 7; i++) {
                    for (int j = fy + qy - 7; j <= fy + qy + 7; j++) {

                        if (rotate[i][j]) {
                            count++;
                            //    image.setRGB(i, j, 0);
                        }
                        //image.setRGB(i, j, 0);
                    }
                }

                if (count > 112) {
                    System.out.println(" " + q + " " + a + "  " + count);
                    index *= 10;
                    index += (a + 1) % 10;
                    marked++;

                    y = 0;
                    x = 0;
                    cnt = 0;
                    find(found, result, fy + qy, fx + qx, true);
                    if (firstMark) {
                        firstMark = false;
                        y1 = (int) (y / cnt);
                        x1 = (int) (x / cnt);
                        c1 = q;
                        r1 = a;
                    } else {
                        if (c1 != q) {
                            y2 = (int) (y / cnt);
                            x2 = (int) (x / cnt);
                            c2 = q;
                            r2 = a;
                        }
                        if (r1 != a) {
                            y3 = (int) (y / cnt);
                            x3 = (int) (x / cnt);
                            c3 = q;
                            r3 = a;
                        }
                    }

                    // break;
                }
            }
            if (marked > 1) {
                ret = true;
            }

        }
        if (x1 != 0 && x2 != 0 && y3 != 0) {
            System.out.println("asd");

            ux = (int) (x1 - (x2 - x1) * (17 + c1) / (c2 - c1 + 0.0));
            vx = (int) (x1 - (x2 - x1) * (3 + c1) / (c2 - c1 + 0.0));

            uy = (int) (y1 - (y3 - y1) * (r1 - 1) / (r3 - r1 + 0.0));
            vy = (int) (y1 - (y3 - y1) * (r1 - 10) / (r3 - r1 + 0.0));
        } else {

            ux = 209;
            uy = 1435 - 370;
            vx = 634;
            vy = 1435 - 242;
        }
        //System.out.print("  " + index + "  ");
        if (ret) {
            return 10000;
        }
        return index;
    }

    private static String[] getSolutions(BufferedImage image) {

        int fx, fy, gx, gy;
        fx = 215;//ux;   //209
        fy = 1435 - 393; //uy;  // 1065
        gx = 633;//vx;   // 634
        gy = 1435 - 269;//uy ;//+ ((vy - uy)*4)/10; // 1193
        String[] sol = new String[30];
        for (int q = 0; q < 30; q++) {
            sol[q] = "";
            for (int a = 0; a < 5; a++) {
                int qx = (int) Math.round((q % 15) * (gx - fx) / 14.00);
                int qy = (int) Math.round(a * (gy - fy) / 4.00);
                int count = 0;
                for (int i = fx + qx - radius; i <= fx + qx + radius; i++) {
                    for (int j = fy + qy - radius; j <= fy + qy + radius; j++) {

                        if (rotate[i][j]) {
                            count++;
                        }
                        //image.setRGB(i, j, 0);
                    }
                }
                int colour = 16777215;
                if (count > solutionCount) {
                    sol[q] += (char) ('A' + a);
                    colour = 0;
                    //    System.out.println(count+" "+q+"  "+sol[q]);
                }
                for (int i = fx + qx - radius; i <= fx + qx + radius; i++) {
                    for (int j = fy + qy - radius; j <= fy + qy + radius; j++) {
                        if (i >= image.getWidth() || i < 0) continue;
                        if (j >= image.getHeight() || j < 0) continue;
                        try {
                            image.setRGB(i, j, colour);
                        } catch (Throwable e) {
                            System.out.println(i + " " + j);
                        }
                    }
                }
            }
            if (sol[q].equals("")) {
                sol[q] = "U";
            } else if (sol[q].length() > 1) {
                sol[q] = "M";
                //System.out.println("asdasdasdasdasdasdasd");
            }

            System.out.print(sol[q]);
            if (q == 14) {
                fy = 1435 - 209;
                gy = 1435 - 86;
            }
        }
        return sol;
    }

    /*
        public static void cropHandWritten() {
            String fPath=folderPath+File.separator+"ByIndex"+File.separator;
            File folder = new File(fPath);
            File[] listOfFiles = folder.listFiles();

            File dir = new File(fPath + File.separator + "Name");
            if (!dir.exists()) {
                dir.mkdir();
            }
            dir = new File(fPath + File.separator + "Telephone");
            if (!dir.exists()) {
                dir.mkdir();
            }
            dir = new File(fPath + File.separator + "Email");
            if (!dir.exists()) {
                dir.mkdir();
            }

            for (File listOfFile : listOfFiles) {
                if (listOfFile.isFile()) {
                    try {
                        cropHandWrittenImage(ImageIO.read(new File(fPath + File.separator + listOfFile.getName())), fPath, listOfFile.getName());
                    } catch (IOException e) {
                    }
                    System.out.println(listOfFile.getName());
                }
            }

        }
    */
    private static void cropHandWrittenImage(BufferedImage image, String folder, String filename) {

        try {
            BufferedImage temp = image.getSubimage(145, 244, 564, 117);
            ImageIO.write(temp, "jpg", new File(folder + "Name" + File.separator + "Name-" + filename));
            temp = image.getSubimage(145, 422, 295, 25);
            ImageIO.write(temp, "jpg", new File(folder + "Telephone" + File.separator + "Telephone-" + filename));
            temp = image.getSubimage(743, 422, 276, 28);
            ImageIO.write(temp, "jpg", new File(folder + "Email" + File.separator + "Email-" + filename));

        } catch (IOException ex) {

        }

    }

    public static void renameByRank() {
        String f = folderPath + File.separator;
        System.out.println(f);
        File dir = new File(f + "ByRank");
        if (!dir.exists()) {
            dir.mkdir();
        }
        dir = new File(f + "ByRank" + File.separator + "Name");
        if (!dir.exists()) {
            dir.mkdir();
        }
        dir = new File(f + "ByRank" + File.separator + "Email");
        if (!dir.exists()) {
            dir.mkdir();
        }
        dir = new File(f + "ByRank" + File.separator + "Telephone");
        if (!dir.exists()) {
            dir.mkdir();
        }
        try {
            FileInputStream in = new FileInputStream(new File(f + "Results.xlsx"));
            XSSFWorkbook workbook = new XSSFWorkbook(in);

            XSSFSheet sheet = workbook.getSheetAt(0);

            Iterator<Row> rowIter = sheet.iterator();
            while (rowIter.hasNext()) {
                Row row = rowIter.next();
                Iterator<Cell> cellIter = row.cellIterator();
                String[] arr = new String[3];
                for (int i = 0; i < 3; i++) {
                    Cell cell = cellIter.next();
                    String text = "";
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_NUMERIC:
                            text = (int) cell.getNumericCellValue() + "";
                            break;
                        case Cell.CELL_TYPE_STRING:
                            text = cell.getStringCellValue();
                            break;
                    }
                    arr[i] = text;
                }
                System.out.println(f + "ByIndex" + File.separator + arr[0] + "-" + arr[1]);
                arr[2] = (Integer.parseInt(arr[2]) + 10000 + "").substring(1);

                copy(f + "ByIndex" + File.separator + arr[0] + "-" + arr[1],
                        f + "ByRank" + File.separator + arr[2] + "-" + arr[0] + "-" + arr[1]
                );
                cropHandWrittenImage(ImageIO.read(new File(f + "ByRank" + File.separator + arr[2] + "-" + arr[0] + "-" + arr[1])),
                        f + "ByRank" + File.separator, arr[2] + "-" + arr[0] + "-" + arr[1]);
            }
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
        } catch (NumberFormatException e) {
            System.out.println(e.getLocalizedMessage());
        }

    }

    private static void copy(String source, String dest) {
        try {
            File oldFile = new File(source);
            File newFile = new File(dest);
            Files.copy(oldFile.toPath(), newFile.toPath());
        } catch (IOException ex) {
        }
    }

}
