/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.assets;

import com.irgames.utils.ImageBlur;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 *
 * @author Andrew
 */
public class PngtoJpg {

    public static void convert(String pngPath, String jpgPath) {
        BufferedImage bufferedImage;

        try {

            //read image file
            bufferedImage = ImageIO.read(new File(pngPath));

            // create a blank, RGB, same width and height, and a white background
            BufferedImage image = new BufferedImage(bufferedImage.getWidth(),
                    bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            image.createGraphics().drawImage(bufferedImage, 0, 0, Color.WHITE, null);

            // Flip the image vertically
            AffineTransform tx;
            AffineTransformOp op;

// Flip the image vertically and horizontally; equivalent to rotating the image 180 degrees
            tx = AffineTransform.getScaleInstance(-1, -1);
            tx.translate(-image.getWidth(null), -image.getHeight(null));
            op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
            image = op.filter(image, null);

            ImageBlur blur = new ImageBlur();
           
            BufferedImage _2 = blur.createBlur(image, 3);
                //System.out.println("data/heightmaps/hm-" + newPoint.x + "-" + newPoint.y + ".bmp");

            // write to jpeg file
            ImageIO.write(_2, "jpg", new File(jpgPath));

        } catch (IOException e) {

            e.printStackTrace();

        }
    }
}
