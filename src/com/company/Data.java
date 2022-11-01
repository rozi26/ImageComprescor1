package com.company;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Data {
    final static String byteSaveName = "bits.txt";
    final static String haxSaveName = "hax.txt";
    final static String resultName = "result.png";

    static String imageFolderPath = "";
    static String imageSourceName = "";
    private static int RGB_BITS = 6; // 1-8 bits for color
    public static void setSource(String path)
    {
        if(!new File(path).exists())
            System.out.println("the file doesn't exist");
        int end = 0;
        for(int i = path.length() - 1; i > 0; i--)
        {
            if(path.charAt(i) == '\\')
            {
                end = i + 1;
                break;
            }
        }
        System.out.println(imageFolderPath);
        imageFolderPath = path.substring(0,end);
        imageSourceName = path.substring(end);
    }
    public static void setRgbBits(int to)
    {
        if(to > 0 && to < 9)
            RGB_BITS = 9 - to;
        else
            System.out.println("the rgb range is 1 - 8 so " + to + " can't represent the number of bits");
    }
    public static int RGBBits()
    {
        return RGB_BITS;
    }
    public static String getSourceImagePath()
    {
        return imageFolderPath + imageSourceName;
    }
    public static String getBitsSaveName()
    {
        return imageFolderPath + byteSaveName;
    }
    public static String getHaxSaveName()
    {
        return imageFolderPath + haxSaveName;
    }
    public static String getResultSaveName()
    {
        return imageFolderPath + resultName;
    }

    public static long getFileSize(File f)
    {
        return f.length() * 8;
    }

    public static String colorToRGBCode(int[] color)
    {
        StringBuilder text = new StringBuilder();
        for(int i = 0; i < color.length; i++)
        {
            text.append((color[i] % 16 < 10)?(char)(color[i] % 16 + 48):(char)(color[i] % 16 + 55));
            text.append((color[i] / 16 < 10)?(char)(color[i] / 16 + 48):(char)(color[i] / 16 + 55));
        }
        return text.toString();
    }
    public static double presentOf(long num,long from)
    {
        return ((int)(((double)num / from * 100) * 100)) / 100.0;
    }
    public static String getDate()
    {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now).toString();
    }
    public static String getTimeDiffrent(long start, long end) {
        String time = "";
        long diffrent = end - start;
        if(diffrent >= 31557600000l)
        {
            time += (diffrent / 31557600000l) + " years, ";
            diffrent %= 31557600000l;
        }
        if (diffrent >= 86400000)
        {
            time += (diffrent / 86400000) + " days, ";
            diffrent %= 86400000;
        }
        if(diffrent >= 3600000)
        {
            time += (diffrent / 3600000) + " hours, ";
            diffrent %= 3600000;
        }
        if(diffrent >= 60000)
        {
            time += (diffrent / 60000) + " minutes, ";
            diffrent %= 60000;
        }
        if(diffrent >= 1000)
        {
            time += (diffrent / 1000) + " seconds, ";
            diffrent %= 1000;
        }
        time += diffrent + " milliseconds";
        return time;
    }
    public static String niceWrite(long _num)
    {
        final String num = Long.toString(_num);
        StringBuilder text = new StringBuilder();
        int count = 0;
        for(int i = num.length() - 1; i > 0; i--)
        {
            text.append(num.charAt(i));
            count++;
            if(count == 3)
            {
                text.append(",");
                count = 0;
            }
        }
        text.append(num.charAt(0));
        return text.reverse().toString();
    }
    public static double round(double num, int digits)
    {
        final int multer = (int)Math.pow(10,digits);
        return (double)((int)(num * multer)) / multer;
    }
    public static long bitsToBytes(long bits)
    {
        return bits / 8 + ((bits % 8 == 0)?0:1);
    }
}
