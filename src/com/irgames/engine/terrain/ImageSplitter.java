/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.terrain;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

/**
 *
 * @author Andrew
 */
public class ImageSplitter {

    static int blurAmount = 3;

    public static Point getPoint(Point before, int rows, int cols) {
        Point fPoint = new Point();

        fPoint.x = (-before.y) + (rows / 2);
        fPoint.y = (-before.x) + (cols / 2);

        return fPoint;
    }

    public static void doHeightmap(File file, int rows, int cols) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        BufferedImage image = ImageIO.read(fis); //reading the image file  

        int chunks = rows * cols;

        int chunkWidth = image.getWidth()/rows; // determines the chunk width and height  
        int chunkHeight = image.getHeight()/cols;
       
        int count = 0;
        
        BufferedImage imgs[] = new BufferedImage[chunks]; //Image array to hold image chunks  
        for (int x = 0; x < rows; x++) {
            for (int y = 0; y < cols; y++) {
                //Initialize the image array with image chunks  
                Graphics2D gr;
                imgs[count] = new BufferedImage(chunkWidth + 1, chunkHeight + 1, image.getType());
                gr = imgs[count].createGraphics();
                try {

                    gr.drawImage(image, 0, 0, chunkWidth + 1, chunkHeight + 1, (chunkWidth * y), chunkHeight * x, chunkWidth * y + chunkWidth + 1, chunkHeight * x + chunkHeight + 1, null);

                } catch (Exception ex) {
                    System.out.println(ex);
                    gr.drawImage(image, 0, 0, chunkWidth, chunkHeight, chunkWidth * y, chunkHeight * x, chunkWidth * y + chunkWidth, chunkHeight * x + chunkHeight, null);
                }
                gr.dispose();

                Point cPoint = new Point(x, y);
                Point newPoint = getPoint(cPoint, rows, cols);

                AffineTransform tx = AffineTransform.getScaleInstance(-1, -1);
                tx.translate(-imgs[count].getWidth(null), -imgs[count].getHeight(null));
                AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
             //   imgs[count] = op.filter(imgs[count], null);
                /*ImageBlur blur = new ImageBlur();
                 BufferedImage _2 = blur.createBlur(imgs[count], 2);*/
                //System.out.println("data/heightmaps/hm-" + newPoint.x + "-" + newPoint.y + ".bmp");
                ImageIO.write(imgs[count], "png", new File("data/heightmaps/hm-" + newPoint.x + "-" + newPoint.y + ".png"));
                count += 1;

            }
        }

        System.out.println("Mini images created");
    }
}
