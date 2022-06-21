package com.company;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Scanner;

public class ImageMeneger {

    public static int[][][] getImageAsArray(String source, boolean withAlpha) throws IOException {
        final int multer =  (int)Math.pow(2,8 - Data.RGBBits());
        File file = new File(source);
        if(file.exists())
        {
            BufferedImage img = ImageIO.read(file);
            int[][][] image = new int[img.getHeight()][img.getWidth()][(withAlpha)?4:3];
            for(int i = 0; i < image.length; i++)
            {
                for(int g = 0; g < image[i].length; g++)
                {
                    int pixel = img.getRGB(g,i);
                    Color color = new Color(pixel,withAlpha);
                    image[i][g][0] = color.getRed() / multer;
                    image[i][g][1] = color.getGreen() / multer;
                    image[i][g][2] = color.getBlue() / multer;
                    if(withAlpha)
                        image[i][g][3] = color.getAlpha() / multer;
                }
            }
            return image;
        }
        else
        {
            System.out.println("the file in {" + source + "} doesn't exist");
            return new int[0][][];
        }
    }

    public static void saveArrayAsImage(int[][][] image, boolean whiteAlpha) throws IOException {
        final int multer =  (int)Math.pow(2,8 - Data.RGBBits());
        BufferedImage bufferedImage = new BufferedImage(image[0].length, image.length,(whiteAlpha)?BufferedImage.TYPE_INT_ARGB:BufferedImage.TYPE_INT_RGB);
        Graphics g = bufferedImage.getGraphics();
        for(int i = 0; i < image.length; i++)
        {
            for(int j = 0; j < image[0].length; j++)
            {
                if(whiteAlpha)
                    g.setColor(new java.awt.Color(image[i][j][0] * multer,image[i][j][1] * multer,image[i][j][2] *4 ,image[i][j][3] * multer));
                else
                    g.setColor(new java.awt.Color(image[i][j][0] * multer,image[i][j][1] * multer,image[i][j][2] * multer));
                g.fillRect(j,i,1,1);
            }
        }
        ImageIO.write(bufferedImage, "PNG",new File(Data.getResultSaveName()));
    }

    public static void printSizeReport(byte[][][] arr)
    {
        printSizeData((long)arr.length * arr[0].length * arr[0][0].length);
    }
    public static void printSizeReport(int[][][] arr)
    {
        printSizeData((long)arr.length * arr[0].length * arr[0][0].length);
    }
    public static void printSizeReport(boolean[] arr)
    {
        printSizeData(arr.length / 8);
    }
    public static void printSizeReport(String arr)
    {
        int smallest = 256;
        int biggest = 0;
        for(int i = 0; i < arr.length(); i++)
        {
            final int num = (int)arr.charAt(i);
            smallest = Math.min(num,smallest);
            biggest = Math.max(num,biggest);
        }
        long bitsSize = 1;
        final int different = biggest - smallest;
        while (true)
        {
            if(different < Math.pow(2,bitsSize))
                break;
            else
                bitsSize++;
        }
        System.out.print("bit size = " + bitsSize);
        printSizeData((arr.length() / 8) * bitsSize);
    }
    private static void printSizeData(long bytes)
    {
       // bits = bits / 8;
        if(bytes / 1262485504 > 0)
        {
            System.out.print(bytes / 1262485504f + " GB ");
            bytes %= 1262485504;
        }
        if(bytes / 1232896 > 0)
        {
            System.out.print(bytes / 1232896 + " MB ");
            bytes %= 1232896;
        }
        if(bytes / 1024 > 0)
        {
            System.out.print(bytes / 1024 + " KB ");
            bytes %= 1024;
        }
        System.out.println(bytes + " B");
    }
    public static void saveArrayAsCodeH(boolean[] arr, String path) throws IOException // haxSaving
    {
        StringBuilder text = new StringBuilder();
        int adder = 256;
        int num = 0;
        for(int i = 0; i < arr.length; i++)
        {
            if(arr[i])
            {
                num += adder;
            }

            adder /= 2;
            if(adder == 0)
            {
                text.append(((char)(num)));
                adder = 256;
                num = 0;
            }
        }
        File file = new File(path);
        if(!file.exists())
        {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileWriter writer = new FileWriter(file);
        writer.write(text.toString());
        writer.close();
    }
    public static boolean[] readSaveArrayH(String path) throws FileNotFoundException {
        File file = new File(path);
        Scanner scanner = new Scanner(file);
        StringBuilder text = new StringBuilder();
        while (scanner.hasNext())
        {
            text.append(scanner.next());
        }
        boolean[] code = new boolean[text.length() * 16];
        for(int i = 0; i < text.length(); i++)
        {
            final int num = (int)text.charAt(i);
            final boolean[] index = Compressor1.intToBinary(num,8);
            for(int g = 0; g < 8; g++)
            {
                code[i * 8 + g] = index[g];
            }
        }
        return code;
    }
    public static void save10(boolean[] code, String path) throws IOException {
        File file = new File(path);
        if(!file.exists())
            file.createNewFile();
        StringBuilder text = new StringBuilder();
        for(int i = 0; i < code.length; i++)
        {
            if(code[i])
                text.append("1");
            else
                text.append("0");
        }
        FileWriter writer = new FileWriter(file);
        writer.write(text.toString());
        writer.close();
    }
}
