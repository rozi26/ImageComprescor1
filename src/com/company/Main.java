package com.company;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
	// write your code here

        Data.setSource("F:\\programing\\java\\testing\\test.png");
        final int[][][] image = ImageMeneger.getImageAsArray(Data.getSourceImagePath(),false);
        System.out.println("image size " + image.length + " X " + image[0].length);
        System.out.print("before compress size: "); ImageMeneger.printSizeReport(image);
        final long startTime1 = System.currentTimeMillis();
        final boolean[] code = Compressor2.compress(image);
        final long endTime1 = System.currentTimeMillis();
        final int[][][] imageUn = Compressor2.uncompress(code);
        final long uncompressTime1 = System.currentTimeMillis() - endTime1;
        ImageMeneger.saveArrayAsImage(imageUn,false);
        System.out.print("after compress2 size: "); ImageMeneger.printSizeReport(code);
        final long startTime2 = System.currentTimeMillis();
        final boolean[] code2 = Compressor1.getCompressCode(image);
        final long endTime2 = System.currentTimeMillis();
        Compressor1.unCompress(code2);
        final long uncompressTime2 = System.currentTimeMillis() - endTime2;
        System.out.print("after compress1 size: ");ImageMeneger.printSizeReport(code2);
        ImageMeneger.save10(code,Data.getBitsSaveName());
        System.out.print("\nbool compress: "); ImageMeneger.printSizeReport(BoolCompresor.compressBool(code));
        System.out.println("\nc2 compression time: " + (endTime1 - startTime1) + " milliseconds\nc2 decompression time: " + uncompressTime1 + " milliseconds\n\nc1 compression time: " + (endTime2 - startTime2) + " milliseconds\nc1 decompression time: " + uncompressTime2 + " milliseconds\n");
        System.out.println("done");/**/
        /*Data.setSource("F:\\programing\\java\\testing\\download.jpg"); // the image that you want to compress
        Data.setRgbBits(4); // the level of neglect of similar colors 8 is maximum (the worst quality) 1 is minimal (lossless compression)
        int[][][] image = ImageMeneger.getImageAsArray(Data.getSourceImagePath(),false);
        final long startTime1 = System.currentTimeMillis();
        boolean[] codder = Compressor1.getCompressCode(image);
        final long endTime1 = System.currentTimeMillis();
        int[][][] un = Compressor1.unCompress(codder);
        final long endTime2 = System.currentTimeMillis();
        ImageMeneger.saveArrayAsCodeH(codder,Data.getHaxSaveName());
        ImageMeneger.saveArrayAsImage(un,false);
        ImageMeneger.save10(codder,Data.getBitsSaveName());
        System.out.println();
        System.out.print("before compressing size: ");
        ImageMeneger.printSizeReport(image);
        System.out.print("after compressing size: ");
        ImageMeneger.printSizeReport(codder);
        System.out.println("compressing time: " + (endTime1 - startTime1) + " milliseconds\ndecompressing time: " + (endTime2 - endTime1) + " milliseconds\n");
        System.out.println("color neglect level = " + (9 - Data.RGBBits()) + ((Data.RGBBits() == 8)?"\t (lossless compression)":""));
        final long boolCompressionStart = System.currentTimeMillis();
        boolean[] c2 = BoolCompresor.compressBool(codder);
        System.out.print("(testing) (" + (System.currentTimeMillis() - boolCompressionStart) + " ms) bool compression value: ");
        ImageMeneger.printSizeReport(c2);*/
        //printCode(codder);
       // final boolean[] compress = BoolCompresor.compressBool(codder);
        //printCode(compress);
      //  ImageMeneger.printSizeReport(compress);
    }

    void printImageArray(int[][][] image)
    {
        for(int i = 0 ; i < image.length; i++)
        {
            for(int g = 0; g < image[0].length; g++)
            {
                System.out.print("[" + image[i][g][0] + "|" + image[i][g][1] + "|" + image[i][g][2] + "]");
            }
            System.out.println();
        }
    }
    static void printCode(boolean[] code)
    {
        for(int i = 0; i < code.length; i++)
        {
            System.out.print((code[i])?"1":"0");
        }
        System.out.println();
    }

}
