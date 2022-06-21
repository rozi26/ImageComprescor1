package com.company;

import java.io.File;

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
}
