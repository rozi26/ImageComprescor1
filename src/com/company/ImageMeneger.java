package com.company;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
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

    public static void  saveArrayAsImage(int[][][] image, boolean whiteAlpha, String loc) throws IOException {
        final int multer =  (int)Math.pow(2,8 - Data.RGBBits());
        BufferedImage bufferedImage = new BufferedImage(image[0].length, image.length,(whiteAlpha)?BufferedImage.TYPE_INT_ARGB:BufferedImage.TYPE_INT_RGB);
        Graphics g = bufferedImage.getGraphics();
        for(int i = 0; i < image.length; i++)
        {
            for(int j = 0; j < image[0].length; j++)
            {
                try
                {
                    if(whiteAlpha)
                        g.setColor(new java.awt.Color(image[i][j][0] * multer,image[i][j][1] * multer,image[i][j][2] *4 ,image[i][j][3] * multer));
                    else
                        g.setColor(new java.awt.Color(image[i][j][0] * multer,image[i][j][1] * multer,image[i][j][2] * multer));
                    g.fillRect(j,i,1,1);
                }
                catch (Exception e)
                {
                   // System.out.println("crash color is "  + Arrays.toString(image[i][j]));
                }
            }
        }
        ImageIO.write(bufferedImage, "PNG",new File(loc));
    }
    public static void saveArrayAsImage(int[][][] image, boolean whiteAlpha) throws IOException {
        saveArrayAsImage(image,whiteAlpha,Data.getResultSaveName());
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
        System.out.println(getSizeData(bytes));
    }
    public static String getSizeData(long bytes)
    {
        StringBuilder s = new StringBuilder();
        if(bytes / 1262485504 > 0)
        {
            s.append(bytes / 1262485504f);
            s.append(" GB ");
            bytes %= 1262485504;
        }
        if(bytes / 1232896 > 0)
        {
            s.append(bytes / 1232896);
            s.append(" MB ");
            bytes %= 1232896;
        }
        if(bytes / 1024 > 0)
        {
            s.append(bytes / 1024);
            s.append(" KB ");
            bytes %= 1024;
        }
        s.append(bytes );
        s.append(" B");
        return s.toString();
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
    public static boolean[] readBitArrayToCode(String path) throws FileNotFoundException {
        File file = new File(path);
        if(!file.exists())
        {
            System.out.println("didn't found bit file in [" + path + "]");
            return null;
        }
        Scanner scanner = new Scanner(file);
        boolean[] code = new boolean[(int)file.length()];
        int index = 0;
        while (scanner.hasNext())
        {
            final String line = scanner.nextLine();
            for(int i = 0; i < line.length(); i++)
            {
                code[index] = line.charAt(i) == '1';
                index++;
            }
        }
        return code;
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
    public static boolean getImageEqual(String image1, String image2,boolean report) throws IOException {
        int[][][] i1 = getImageAsArray(image1,false);
        int[][][] i2 = getImageAsArray(image2,false);
        if(i1.length != i2.length || i1[0].length != i2[0].length) {
            if(report) System.out.println("\nimage size doesn't equal\ni1: (" + i1[0].length + "," + i1.length +  ")\ni2: (" + i2[0].length +"," + i2.length + ")");
            return false;
        }
        for(int i=  0; i < i1.length; i++)
        {
            for(int g = 0; g < i1[0].length; g++)
            {
                if(!Arrays.equals(i1[i][g],i2[i][g]))
                {
                    if(report) System.out.println("\nimages pixel doesn't equal in pixel (" + g + "," + i + ")\ni1: " + Arrays.toString(i1[i][g]) + "\ni2: " + Arrays.toString(i2[i][g]));
                    return false;
                }
            }
        }
        return true;
    }
    public static long getImageSize(String path) throws IOException {
        return Files.size(Path.of(path));
    }
    public static String printCompilationDifferent(long[] size, long originalSize)
    {
        String[] names = new String[size.length];
        for(int i =  1; i <= names.length; i++){names[i] = "compressor " + i;}
        return printCompilationDifferent(size,names,originalSize);
    }
    public static String printCompilationDifferent(long[] size, String[] names, long originalSize)
    {
        StringBuilder s = new StringBuilder();
        long bestSize = size[0];
        int bestLoc = 0;
        for(int i = 0; i < size.length; i++)
        {
            s.append(singleCompressReport(size[i],names[i],originalSize)); s.append("\n");
            if(size[i] < bestSize)
            {
                bestSize = size[i];
                bestLoc = i;
            }
        }
        /*s.append("\nthe best compressor was " + names[bestLoc] + " with " + Data.presentOf(bestSize,originalSize) + " present\n");
        for(int i = 0; i < size.length; i++)
        {
            if(i != bestLoc)
            {
                s.append("better then " + names[i] + " in " + Data.niceWrite((size[i] - bestSize)) + " bytes (" + Data.round((Data.presentOf(size[i],originalSize) - Data.presentOf(bestSize,originalSize)),3) + "%)\n");
            }
        }*/
        return s.toString();
    }
    private static String singleCompressReport(long size, String name, long originalSize)
    {
        return name + " size: " + Data.niceWrite(size) + " bytes (" + Data.presentOf(size,originalSize) + "%)";
    }

    public static String getFileFolder(String path)
    {
        int nameStart = path.length() - 1;
        while (path.charAt(nameStart) != '\\')
            nameStart--;
        return path.substring(0,nameStart);
    }

    //format methods
    public static void saveAsIIC(String path, boolean[] code, boolean compressor3) throws IOException {

        //create the detestation file
        final File detestation = new File(path + ".iic");
        if(detestation.exists() && !detestation.delete()) System.out.println("fail to delete [" + path + ".iic]");
        if(!detestation.createNewFile()) System.out.println("fail to create [" + path + ".iic]");

        FileOutputStream outputStream = new FileOutputStream(detestation);

        byte[] length_bytes = new byte[4];
        for(int i = 0; i < 8; i++)
        {
            final int ind = 7 - i;
            if((code.length >> i & 1) == 1)
                length_bytes[0] |= 1 << ind;
            if((code.length >> (i + 8) & 1) == 1)
                length_bytes[1] |= 1 << ind;
            if((code.length >> (i + 16) & 1) == 1)
                length_bytes[2] |= 1 << ind;
            if((code.length >> (i + 24) & 1) == 1)
                length_bytes[3] |= 1 << ind;
        }
        for(int i = 0; i < 4; i++)
            outputStream.write(length_bytes[3 - i]);

        int index = 1;
        byte add = 0;
        if(compressor3) add |= 1;
        for(boolean bit: code)
        {
            if(bit) add |= 1 << index;
            index++;
            if(index == 8)
            {
                outputStream.write(add);
                add = 0;
                index = 0;
            }
        }
        if(index != 0) outputStream.write(add);
        outputStream.close();

    }
    public static int[][][] readFromIIC(String path) throws IOException {
        path += ".iic";
        final File file = new File(path);
        FileInputStream inputStream = new FileInputStream(file);

        boolean[] length_b = new boolean[32];
        int read = inputStream.read();
        for(int i = 0; i < 32; i++)
        {
            if(i % 8 == 0 && i != 0)
                read = inputStream.read();
            length_b[i] = (read >> (i % 8) & 1) == 1;
        }
        read = inputStream.read();
        final int length = Compressor2.booleanToInt(length_b);
        boolean[] code = new boolean[length];

        int bit = 1;
        final boolean c3 = (read & 1) == 1;
        for(int i = 0; i < length; i++)
        {
            if(bit == 8)
            {
                bit = 0;
                read = inputStream.read();
            }
            code[i] = (read >> bit & 1) == 1;
            bit++;
        }
        inputStream.close();
        int[][][] image = c3?Compressor3Beta.uncompress(code):Compressor2.uncompress(code);
        return image;
    }
}
