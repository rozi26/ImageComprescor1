package com.company;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) throws IOException {
        //the program take image and compress and uncompress her in two ways to and from iic file.

        //choose the image you want to compress
        final String SOURCE_IMAGE = "F:\\programing\\java\\testing\\iddo\\image testing\\test.jpg";

        //paths
        final String SOURCE_FOLDER = ImageMeneger.getFileFolder(SOURCE_IMAGE);

        final long T1 = System.currentTimeMillis();
        final int compressor3_size = compressImage(SOURCE_IMAGE,"compress3",true);
        final long T2 = System.currentTimeMillis();
        final int compressor2_size = compressImage(SOURCE_IMAGE,"compress2",false);
        final long T3 = System.currentTimeMillis();

        System.out.println("\ncompressor 3 took " + Data.niceWrite(T2 - T1) + " milliseconds to compress");
        System.out.println("compressor 2 took " + Data.niceWrite(T3 - T2) + " milliseconds to compress");

        final long T4 = System.currentTimeMillis();
        unCompressImage(SOURCE_FOLDER + "\\compress3","uncompress3.png");
        final long T5 = System.currentTimeMillis();
        unCompressImage(SOURCE_FOLDER + "\\compress2","uncompress2.png");
        final long T6 = System.currentTimeMillis();

        System.out.println("\ncompressor 3 took " + Data.niceWrite(T5 - T4) + " milliseconds to uncompress");
        System.out.println("compressor 2 took " + Data.niceWrite(T6 - T4) + " milliseconds to uncompress");
        System.out.println("\n" + getSizeReport(compressor3_size,compressor2_size,SOURCE_FOLDER + "\\uncompress3.png"));

    }

    private static int compressImage(String source_path, String detestation_name, boolean compressor3) throws IOException {
        //find the image folder
        final String source_folder = ImageMeneger.getFileFolder(source_path);

        //get the image as array
        final int[][][] image = ImageMeneger.getImageAsArray(source_path,false);

        //compress the image
        boolean[] code;
        if(compressor3)
        {
            Compressor3Beta compressor3Beta = new Compressor3Beta();
            code = compressor3Beta.compress(image).code;
        }
        else code = Compressor2.compress(image);

        //save the code
        ImageMeneger.saveAsIIC(source_folder + "\\" + detestation_name,code,compressor3);

        return code.length;
    }

    private static void unCompressImage(String source, String result_name) throws IOException {
        //find the image folder
        final String source_folder = ImageMeneger.getFileFolder(source);

        //uncompress the image
        int[][][] image = ImageMeneger.readFromIIC(source);

        //save the image
        ImageMeneger.saveArrayAsImage(image,false,source_folder + "\\" + result_name);
    }

    //testing methods
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
    private static String getSizeReport(long compress3Size, long compress2Size, String sourcePath) throws IOException {

        final int[][][] image = ImageMeneger.getImageAsArray(sourcePath,false);
        long[] sizes = new long[]{Data.bitsToBytes(compress3Size),Data.bitsToBytes(compress2Size),ImageMeneger.getImageSize(sourcePath)};
        String[] names = new String[]{"compressor 3","compressor 2","PNG"};
        return ImageMeneger.printCompilationDifferent(sizes,names,(long)image.length * image[0].length * 3);
    }


    private static void recordRun(int[][][] image,CompressorRecordProps prop) throws IOException {
        class helper
        {
            private static boolean imageEqual(int[][][] image,int[][][] compare)
            {
                if(image.length != compare.length || image[0].length != compare[0].length) return false;
                for(int i = 0; i < image.length; i++)
                {
                    for(int g = 0; g < image[0].length; g++)
                    {
                        for(int a = 0; a < 3; a++)
                        {
                            if(image[i][g][a] != compare[i][g][a]) return false;
                        }
                    }
                }
                return true;
            }
        }
        final String recordFile = "F:\\programing\\java\\testing\\records";
        int imageID = 1;
        while (true)
        {
            File file = new File(recordFile + "\\image " + imageID);
            if(!file.exists())
            {
                file.mkdir();
                ImageMeneger.saveArrayAsImage(image,false,file.getPath() + "\\source.png");
                File props = new File(file.getPath() + "\\image properties.txt");
                props.createNewFile();
                FileWriter writer = new FileWriter(props);
                writer.write("image size " + image.length + " X " + image[0].length + "\nimage clear size: " + ImageMeneger.getSizeData((long)image.length * image[0].length * 24) + "\n" + Orginizer.getPixelsReport(image).getColorsReport());
                writer.close();
                break;
            }
            int[][][] compare = ImageMeneger.getImageAsArray(file.getPath() + "\\source.png",false);
            if(helper.imageEqual(image,compare))
                break;
            imageID++;
        }
        final String imageFile = recordFile + "\\image " + imageID + "\\run ";
        int runID = 1;
        while (true)
        {
            File file = new File(imageFile + runID);
            if(file.exists()) runID++;
            else {file.mkdir(); break;}
        }
        ImageMeneger.saveArrayAsImage(prop.resultImage,false,imageFile + runID + "\\result.png");
        File report = new File(imageFile + runID + "\\report.txt");
        report.createNewFile();
        FileWriter writer1 = new FileWriter(report);
        writer1.write("preform at " + Data.getDate() + "\nrun for: " + Data.getTimeDiffrent(0,prop.time) + "\nresult size " + ImageMeneger.getSizeData(prop.resultSize) + " (" + Data.presentOf(prop.resultSize,(long)image.length * image[0].length * 24) + "%)\n\ncompressor report:\n" + prop.compressReport);
        writer1.close();
        if(prop.code != null)
        {
            File code = new File(imageFile + runID + "\\bits.txt");
            code.createNewFile();
            FileWriter writer = new FileWriter(code);
            StringBuilder text = new StringBuilder();
            for(boolean b:prop.code)
            {
                text.append(b?'1':'0');
            }
            writer.write(text.toString());
            writer.close();
        }
    }
    public static class CompressorRecordProps
    {
        private final int[][][] resultImage;
        private final long time;
        private final String compressReport;
        private final String algorithmName;
        private final long resultSize;
        private final boolean[] code;
        public CompressorRecordProps(int[][][] _result, long _time, String _compressReport, String _algorithmName, long _resultSize, boolean[] _code)
        {
            resultImage = _result;
            time = _time;
            compressReport = _compressReport;
            algorithmName = _algorithmName;
            resultSize = _resultSize;
            code = _code;
        }
    }
}
