/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.utils;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.DataBuffer;
import java.awt.image.Kernel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

public class ImageBlur {

    public static void saveToFile(BufferedImage img, String name)
            throws FileNotFoundException, IOException {

        File outputfile = new File(name);
        ImageIO.write(img, "png", outputfile);
    }
    private static BufferedImage mshi;
    private BufferedImage databuf;
    private static int blurRad = 300;

    public BufferedImage createBlur(BufferedImage origText, int rad) throws IOException {
        mshi = origText;

        BufferedImageOp op = new GaussianFilter(rad);

        mshi = op.filter(mshi, databuf);

        return mshi;

      // File outputfile = Gdx.files.local("Blur.png").file();
        // ImageIO.write(mshi, "png", outputfile);
    }
}
