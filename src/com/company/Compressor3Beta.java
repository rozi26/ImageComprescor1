package com.company;

import java.util.ArrayList;
import java.util.List;

public class Compressor3Beta {

    //controllers
    final static int COLOR_SHIFT_MAX = 6;

    //general data
    int IMAGE_WIDTH = -1;
    int IMAGE_HEIGHT = -1;
    int WIDTH_MARK_COST = 32;
    int HEIGHT_MARK_COST = 32;


    int[][][] IMAGE_TESTING= null;
    private List<Square> squares = new ArrayList<>();
    public class Square
    {
        final private int startL;
        final private int startC;
        final private int[] startColor;
        final private int[] range;
        final private int width;
        final private int height;
        public Square(int[] color, int _startL,int _startC, int _width, int _height, int[] _range)
        {
            startL = _startL;
            startC = _startC;
            startColor = color;
            width = _width;
            height = _height;
            range = _range;
        }
        private int getSquareCost()
        {
            int shiftSum = range[0] + range[1] + range[2];
            return WIDTH_MARK_COST + HEIGHT_MARK_COST + minimumPixelToRepresentPositive(IMAGE_HEIGHT - startL) + minimumPixelToRepresentPositive(IMAGE_WIDTH - startC) + (COLOR_SHIFT_MAX * 3) + ((width * height - 1) * shiftSum) + 24;
        }
        private int getCost()
        {
            //return getSquareCost();
           return Math.min(width * height * 24,getSquareCost());
        }
        private boolean isSquare()
        {
            //return true;
            return getSquareCost() < (width * height * 24);
        }
        private int getPixels()
        {
            return width * height;
        }
        private double getPixelsWorth()
        {
            return  ((double)getCost()) / getPixels();
        }
        private boolean inRange(int i, int g)
        {
            return (i >= startL && i - startL < height) && (g >= startC) && (g - startC < width);
        }
        public String toString()
        {
            return "(" + startC + "," + startL + ") -- (" + (startC + width - 1) + "," + startL + ")\n|\t\t\t|\n|\t\t\t|\n(" + startC + "," + (startL + height -1 ) + ") -- (" + (startC + width - 1) + "," + (startL + height - 1) + ")\ncolor: " + Data.colorToRGBCode(startColor) + "\nrange: [" + range[0] +"|" + range[1] + "|" + range[2] + "]\ncost: " + getCost() + "\npixel worth: " + Data.round(getPixelsWorth(),3);
        }
        private boolean testRange(int[][][] image)
        {
            int[] color = startColor.clone();
            boolean first = true;
            for(int i = startL; i < startL + height; i++)
            {
                for(int g = startC + ((first)?1:0); g < startC + width; g++)
                {
                    int[] diffrent = getColorShiftRange(image[i][g],color);
                    for(int a = 0; a < 3; a++)
                    {
                        if(diffrent[a] > range[a])
                            return false;
                    }
                    color = image[i][g];
                }
                color = image[i][startC];
                first = false;
            }
            return true;
        }
    }
    private void calculateSquares(int[][][] image)
    {
        IMAGE_TESTING = image;
        IMAGE_HEIGHT = image.length;
        IMAGE_WIDTH = image[0].length;
        WIDTH_MARK_COST = minimumPixelToRepresentPositive(image[0].length);
        HEIGHT_MARK_COST = minimumPixelToRepresentPositive(image.length);
        SquaresRelative.Pixel[][] grid = new SquaresRelative.Pixel[image.length][image[0].length];
        for(int i = 0; i < image.length; i++)
        {
            for(int g = 0; g < image[0].length; g++)
            {
                grid[i][g] = new SquaresRelative.Pixel(i == image.length - 1?null:getColorShift(image[i + 1][g],image[i][g]),g == image[0].length - 1?null:getColorShift(image[i][g + 1],image[i][g]),g,i);
            }
        }
        SquaresRelative relative = new SquaresRelative(grid,0,COLOR_SHIFT_MAX);
        List<SquaresRelative.Square> list = relative.calculateSquares();
        for(SquaresRelative.Square square:list)
        {
            squares.add(new Square(image[square.startL][square.startC],square.startL,square.startC,square.width,square.height,square.range));
        }
    }
    public Main.CompressorRecordProps compress(int[][][] image) //compressor for neutral images
    {
        final long startTime = System.currentTimeMillis();
        calculateSquares(image);
        int fullSize = 32 + WIDTH_MARK_COST + HEIGHT_MARK_COST;
        for(Square square:squares)
        {
            fullSize += square.getCost();
        }
        boolean[] code = new boolean[fullSize];
        writeNumInCode(intToBoolean(image.length,16),0,code);
        writeNumInCode(intToBoolean(image[0].length,16),16,code);
        int writer = 32;
        int unSqure = 0;
        int outRange = 0;
        for(Square square:squares)
        {
            if(square.isSquare())
            {
                if(!square.testRange(image)) outRange++;
                writeNumInCode(intToBoolean(square.startC,WIDTH_MARK_COST),writer,code); // write the square x location
                writer += WIDTH_MARK_COST;
                writeNumInCode(intToBoolean(square.startL,HEIGHT_MARK_COST),writer,code); // write the square y location
                writer += HEIGHT_MARK_COST;
                final int widthLength = minimumPixelToRepresentPositive(IMAGE_WIDTH - square.startC);
                final int heightLength = minimumPixelToRepresentPositive(IMAGE_HEIGHT - square.startL);
                writeNumInCode(intToBoolean(square.width,widthLength),writer,code);
                writer += widthLength;
                writeNumInCode(intToBoolean(square.height,heightLength),writer,code);
                writer += heightLength;
                writeNumInCode(colorToBoolean(square.startColor),writer,code);
                writer += 24;
                for(int i = 0; i < 3; i++){writeNumInCode(intToBoolean(square.range[i],COLOR_SHIFT_MAX),writer,code); writer += COLOR_SHIFT_MAX;}
                boolean first = true;
                int[] color = square.startColor;
                for(int i = square.startL; i < square.startL + square.height; i++)
                {
                    for(int g = square.startC + ((first)?1:0); g < square.startC + square.width; g++)
                    {
                        final int[] difrrent = getColorShift(image[i][g],color);
                        for(int a = 0; a < 3; a++)
                        {
                            if(square.range[a] != 0)
                            {
                                writeNumInCode(allIntToBoolean(difrrent[a],square.range[a]),writer,code);
                                writer += square.range[a];
                            }
                        }
                        color = image[i][g];
                    }
                    first = false;
                    color = image[i][square.startC];
                }
            }
            else
                unSqure++;
        }
        writeNumInCode(intToBoolean(roundToClosestMax(IMAGE_WIDTH),WIDTH_MARK_COST),writer,code); writer += WIDTH_MARK_COST;
        writeNumInCode(intToBoolean(roundToClosestMax(IMAGE_HEIGHT),HEIGHT_MARK_COST),writer,code); writer += HEIGHT_MARK_COST;
        class PixelColor
        {
            final int x;
            final int y;
            final int[] color;
            PixelColor next = null;
            PixelColor nextLine = null;
            PixelColor(int _x, int _y)
            {
                color = image[_x][_y];
                x = _x;
                y = _y;
            }
            boolean isSmaller(int _x, int _y)
            {
                return x < _x || (x == _x && y < _y);
            }
            void add(PixelColor p)
            {
                if(next == null) next = p;
                else if(nextLine != null && nextLine.isSmaller(p.x,p.y))
                    nextLine.add(p);
                else if(next.isSmaller(p.x,p.y))
                {
                    if(p.x > x)
                        nextLine = p;
                    next.add(p);
                }
                else
                {
                    p.next = next;
                    next = p;
                }
            }
        }
        PixelColor first = null;
        final int differentLimit = 10;
        int counter = 0;
        for(Square square: squares)
        {
            if(!square.isSquare())
            {
                for(int i = square.startL; i < square.startL + square.height; i++)
                {
                    for(int g = square.startC; g < square.startC + square.width; g++)
                    {
                        if(first == null) first = new PixelColor(i,g);
                        else
                        {
                            PixelColor p = new PixelColor(i,g);
                            if(!first.isSmaller(i,g))
                            {
                                p.next = first;
                                first = p;
                            }
                            else
                            {
                                first.add(p);
                            }
                        }counter++;
                    }
                }
            }
        }
        while (first != null)
        {
            writeNumInCode(colorToBoolean(first.color),writer,code); writer += 24;
            first = first.next;
        }
        //only props writing
        final long time = System.currentTimeMillis() - startTime;
        final String report = printSquareReport(image,code.length);
       // System.out.println(report);

       // System.out.println("there " + Data.niceWrite(unSqure) + " pixels\nthere " + Data.niceWrite(squares.size() - unSqure) + " real squares");
        //System.out.println("there (" + Data.niceWrite(outRange) + "/" + Data.niceWrite(squares.size()) +") squares with out range (" + Data.presentOf(outRange,squares.size()) + "%)");
       // System.out.println("size: " + Data.niceWrite(code.length) + " (" + Data.niceWrite((code.length / 8 + ((code.length % 8 == 0)?0:1))) + " bytes) (" + Data.presentOf(code.length,((long)image.length * image[0].length * 24))  + "%)");
        //System.out.println("\nfinish to compress in \t|" + Data.niceWrite(time) + "| milliseconds");
        int[][][] imageResult = new int[image.length][image[0].length][];
        final int[] paintColor = new int[]{63,63,0};
        int failWidth = 0;
        int failHeight = 0;
        for(Square square: squares)
        {
            if(square.height < 1) failHeight++;
            if(square.width < 1) failWidth++;
            for(int i = square.startC; i < square.startC + square.width; i++)
            {
                imageResult[square.startL][i] = paintColor;
                imageResult[square.startL + square.height - 1][i] = paintColor;
            }
            for(int i = square.startL; i < square.startL + square.height; i++)
            {
                imageResult[i][square.startC] = paintColor;
                imageResult[i][square.startC + square.width - 1] = paintColor;
            }
        }/**/
        for(int i = 0; i < image.length; i++)
        {
            for(int g= 0; g < image[0].length; g++)
            {
                if(imageResult[i][g] == null) imageResult[i][g] = image[i][g].clone();
            }
        }
      //  System.out.println("fail width: " + failWidth + "\nfail height:  " + failHeight);
        return new Main.CompressorRecordProps(imageResult,time,report,"compressor3 Beta",fullSize,code);
    }
   /* private Square getSquare(int startL, int startC, int[] colorShift, int[] startColor, int gLimit, int[][][] image)
    {
        return getSquare(startL,startC,colorShift,startColor,gLimit,image,startL,startC,false);
    }
    private Square getSquare(int startL, int startC,int[] colorShift, int[] startColor, int gLimit, int[][][] image,int firstI, int firstG, boolean cl)
    {
        // final boolean newOne = (firstI == startL || firstG != startC) && firstG != gLimit;
        //int[] color = image[(newOne)?startL:firstI - 1][(newOne)?firstG:startC];
        int[] color = (cl)?image[firstI - 1][startC]:image[startL][firstG];
        // int[] color = (startL == firstI && startC == firstG)?image[startL][startC]:cl?image[firstI - 1][startC]:image[firstI][firstG];
        boolean FIRST_RUN = true;
        int[] range = colorShift.clone();
        int lastI = 0;
        for(int i = firstI; i < image.length; i++)
        {
            lastI = i;
            for(int g = (FIRST_RUN)?firstG:startC; g < gLimit; g++)
            {
                boolean changeAdd = false;
                boolean GetToLimit = false;
                final int[] shift = getColorShiftRange(image[i][g],color);
                int gLimitSetter = g;
                int[] needAdd = new int[3];
                for(int h = 0; h < 3; h++)
                {
                    final int rangeUpdate = range[h] + needAdd[h];
                    if(rangeUpdate == COLOR_SHIFT_MAX) { GetToLimit = true; break;}
                    if(shift[h] > rangeUpdate)
                    {
                        needAdd[h]++;
                        h--;
                        changeAdd = true;
                    }
                }
                if(!GetToLimit && changeAdd)
                {
                    final int[] rangeTest = range.clone();
                    for(int a = 0; a < 3; a++)
                    {
                        rangeTest[a] += needAdd[a];
                    }
                    // range = rangeTest.clone();
                    final boolean height1 = i - startL == 0;
                    final boolean firstLine =  g == startC || i + 1 == image.length;
                    final Square cutLine = (height1)?null:new Square(startColor,startL,startC,gLimit - startC,i - startL,range);
                    final Square cutColl = (firstLine)?null:getSquare(startL,startC,range.clone(),startColor,g,image,i + 1,startC,true);
                    final Square stop = (cutLine == null && cutColl == null)?
                            new Square(startColor,startL,startC,1,1,range):
                            (!height1 && (firstLine || cutLine.getPixelsWorth() < cutColl.getPixelsWorth()))?cutLine:cutColl;
                    final Square play = getSquare(startL,startC,rangeTest,startColor,gLimit,image,i,g,false);
                    if(play.getPixelsWorth() < stop.getPixelsWorth())
                        return play;
                    return stop;
                }
                boolean cantPoot = false;
                if(!GetToLimit && !canPutSquareIn(i,g + 1))
                {
                    GetToLimit = true;
                    gLimitSetter++;
                    cantPoot = true;
                }
                if(GetToLimit)
                {
                    final boolean height1 = i - startL == 0;
                    if(height1) {
                        gLimit = gLimitSetter;
                        if(gLimit == startC)
                            return new Square(startColor,startL,startC,0,1,range);
                    }
                    else
                    {
                        final Square stopNow = new Square(startColor,startL,startC,gLimit - startC,i - startL,range);
                        if(gLimitSetter == startC || i == image.length - 1){if(cantPoot){stopNow.height++;} return stopNow;}
                        final int[] addDown = getColorShiftRange(image[i][startC],image[i - 1][startC]);
                        //for(int a = 0; a < 3; a++) {addDown[a] = roundToClosestMax(addDown[a]);}
                        int[] diffrent = new int[3];
                        for(int a = 0; a < 3; a++){diffrent[a] = Math.max(addDown[a],range[a]); if(diffrent[a] > COLOR_SHIFT_MAX) return stopNow;}
                        final Square adder = getSquare(i ,startC,diffrent,image[i][startC],gLimitSetter,image);
                        final Square combine = new Square(startColor,startL,startC,adder.width,stopNow.height + adder.height,new int[]{Math.max(range[0],adder.range[0]),Math.max(range[1],adder.range[1]),Math.max(range[2],adder.range[2])});
                        //int[] a = new int[]{stopNow.getCost(),adder.getCost(),combine.getCost()};
                        if(stopNow.getCost() + adder.getCost() < combine.getCost())
                        {
                            squares.add(adder);
                            return stopNow;
                        }
                        return combine;
                    }
                }
                color = image[i][g];
            }
            color = image[i][startC];
            FIRST_RUN = false;
            if(!canPutSquareIn(i + 1,startC)) break;
        }
        return new Square(startColor,startL,startC,gLimit - startC,lastI - startL + 1,range);
    }
    private void addSquare(int startL, int startC, int[][][] image)
    {
        Square s = getSquare(startL,startC,new int[]{0,0,0},image[startL][startC],image[0].length,image);
        if(s != null)squares.add(s);
    }*/


    private int[] getColorShift(int[] c1, int[] c2)
    {
        return new int[]{c1[0]-c2[0],c1[1]-c2[1],c1[2]-c2[2]};
    }
    private int[] getColorShiftRange(int[] c1, int[] c2)
    {
        int[] r =  getColorShift(c1,c2);
        for(int i = 0; i < 3; i++){r[i] = minimumPixelToRepresent(r[i]);}
        return r;
    }
    private static int log2(int num)
    {
        for(int i = 0; i < 31; i++) {if(((num >> i) & 1) == 1) return i;}return -1;
    }
    private static int minimumPixelToRepresentPositive(int num)
    {
        for(int i = 30; i >= 0; i--)
        {
            if(((num >> i) & 1) == 1) return i + 1;
        }
        return 0;
    }
    public static int minimumPixelToRepresent(int num)
    {
        if(num == 0) return 0;
        if(((num >> 31) & 1) == 1) {num = Math.abs(num); num--;}
        return minimumPixelToRepresentPositive(num) + 1;
    }
    public static int roundToClosestMax(int num)
    {
        int r = 0;
        for(int i = 0; i < minimumPixelToRepresentPositive(num); i++)
        {
            r =  r | (1 << (i));
        }
        return r;
    }
    private Square add(Square a, Square b, int[][][] image)
    {

        //if(!Arrays.equals(a.startColor,b.startColor) || a.startC != b.startC || a.startL != b.startL) {System.out.println("error add two unConnected squares");}
        final int[] diffrents =  getColorShiftRange(image[a.startL + a.height - 1][a.startC + a.width - 1],image[b.startL][b.startC]);
        int[] range = new int[3];
        for(int i = 0; i < range.length; i++)
        {
            range[i] = Math.max(Math.max(a.range[i],b.range[i]),(diffrents[i]));
        }/**/
        return new Square(a.startColor,a.startL,a.startC,b.width,b.height + a.height,range);
    }
    private boolean canPutSquareIn(int i, int g)
    {
        for(Square square: squares)
        {
            if(square.inRange(i,g)) return false;
        }
        return true;
    }

    private String printSquareReport(int[][][] image, long costSum)
    {
        StringBuilder text = new StringBuilder();
        for(Square square: squares)
        {
            text.append(square.toString());
            text.append("\n");
        }
        text.append("there are " + Data.niceWrite(squares.size()) + " squares\n");
       /* final int clearCost = (image.length * image[0].length * 3);
        final int simpleCost = Compressor2.compress(image).length;
        text.append("totalCost: " + Data.niceWrite(costSum) + " (" + Data.niceWrite((costSum / 8 + ((costSum % 8 == 0)?0:1))) +" bytes) ("+ Data.presentOf(costSum,clearCost) + "%)\nLightCost: " + Data.niceWrite(simpleCost) + " (" + Data.niceWrite((simpleCost / 8 + ((simpleCost % 8 == 0)?0:1))) + " bytes) (" + Data.presentOf(simpleCost,clearCost) + "%)\nclearCost: " + Data.niceWrite(clearCost) + " (" + Data.niceWrite((clearCost / 8)) +" bytes)");/**/
        return text.toString();
    }

    private boolean ifGoodToQuit(int i,int g,int iStart,int gStart, int gLimit,int[][][] image,int startC,int[] colorShift)
    {
        int h = i;
        final int[] testingColor = image[h][startC];
        boolean leave = false;
        h++;
        while(h < image.length && !leave)
        {
            int[] diffrent = getColorShiftRange(testingColor,image[h][startC]);
            for(int k = 0; k < 3; k++)
            {
                if(diffrent[k] > colorShift[k]) {leave = true; break;}
            }
            h++;
        }
        h--;
        return (h - iStart) * (g - gStart) <= ((i + 1) - iStart) * ((gLimit - gStart) + ((g - gLimit)));
    }

    private static void writeNumInCode(boolean[] num, int start, boolean[] code)
    {
        for(int i = 0; i < num.length; i++)
        {
            code[start + i] = num[i];
        }
    }
    private static boolean[] readInCode(int start, int length, boolean[] code)
    {
        boolean[] read = new boolean[length];
        System.arraycopy(code, start, read, 0, length);
        return read;
    }
    public static boolean[] intToBoolean(int num, int size)
    {
        boolean[] code = new boolean[size];
        for(int i = size - 1; i >= 0; i--)
        {
            code[i] = num % 2 == 1;
            num /= 2;
        }
        return code;
    }
    public static boolean[] allIntToBoolean(int num, int size)
    {
        boolean[] arr = new boolean[size];
        if(num < 0){arr[0] = true; num = Math.abs(num) - 1;}
        final boolean[] numArr = intToBoolean(num,size - 1);
        for(int i=  1; i < size; i++)
        {
            arr[i] = numArr[i - 1];
        }
        return arr;
    }
    public static int booleanToInt(boolean[] num)
    {
        int add = 1;
        int res = 0;
        for (int i = num.length - 1; i >= 0; i--) {
            if (num[i])
                res += add;
            add *= 2;
        }
        return res;
    }
    public static int booleanToAllInt(boolean[] num)
    {
        if(num.length == 0) return 0;
        if(num[0]) {num[0] = false; return -(booleanToInt(num) + 1);}
        return booleanToInt(num);
    }
    private static boolean[] colorToBoolean(int[] colors)
    {
        final boolean[] r = intToBoolean(colors[0],8);
        final boolean[] g = intToBoolean(colors[1],8);
        final boolean[] b = intToBoolean(colors[2],8);
        boolean[] code = new boolean[24];
        for(int i = 0; i < 8; i++)
        {
            code[i] = r[i];
            code[8 + i] = g[i];
            code[16 + i] = b[i];
        }
        return code;
    }
    public static int[] booleanToColor(boolean[] arr)
    {
        boolean[] r = new boolean[8];
        boolean[] g = new boolean[8];
        boolean[] b = new boolean[8];
        for(int i = 0; i < 8; i++)
        {
            r[i] = arr[i];
            g[i] = arr[8 + i];
            b[i] = arr[16 + i];
        }
        return new int[]{booleanToInt(r),booleanToInt(g),booleanToInt(b)};
    }
    private static String booleanToString(boolean[] b)
    {
        StringBuilder s = new StringBuilder();
        for(int i = 0; i < b.length; i++)
        {
            s.append(b[i]?'1':'0');
        }
        return s.toString();
    }


    public static int[][][] uncompress(boolean[] code)
    {
        final int IMAGE_HEIGHT = booleanToInt(readInCode(0,16,code));
        final int IMAGE_WIDTH = booleanToInt(readInCode(16,16,code));
        int[][][] image = new int[IMAGE_HEIGHT][IMAGE_WIDTH][];
        int reader = 32;
        while (reader < code.length)
        {
            final int startC = booleanToInt(readInCode(reader,minimumPixelToRepresentPositive(IMAGE_WIDTH),code)); reader += minimumPixelToRepresentPositive(IMAGE_WIDTH);
            final int startL = booleanToInt(readInCode(reader,minimumPixelToRepresentPositive(IMAGE_HEIGHT),code)); reader += minimumPixelToRepresentPositive(IMAGE_HEIGHT);
            if(startC == roundToClosestMax(IMAGE_WIDTH) && startL == roundToClosestMax(IMAGE_HEIGHT))
            {break;}
            final int widthWriteLength = minimumPixelToRepresentPositive(IMAGE_WIDTH - startC);
            final int heightWriteLength = minimumPixelToRepresentPositive(IMAGE_HEIGHT - startL);
            final int width = booleanToInt(readInCode(reader,widthWriteLength,code)); reader += widthWriteLength;
            final int height = booleanToInt(readInCode(reader,heightWriteLength,code)); reader += heightWriteLength;
            int[] color = booleanToColor(readInCode(reader,24,code)); reader += 24;
            int[] range = new int[3]; for(int i = 0; i < 3; i++) {
            range[i] = booleanToInt(readInCode(reader,COLOR_SHIFT_MAX,code)); reader += COLOR_SHIFT_MAX;}
            image[startL][startC] = color.clone();
            boolean first = true;
            for(int i = startL; i < startL + height && i < IMAGE_HEIGHT; i++)
            {
                for(int g = startC + ((first)?1:0); g < startC + width && g < IMAGE_WIDTH; g++)
                {
                    image[i][g] = new int[3];
                    for(int a = 0; a < 3; a++){
                        final int diffrent = booleanToAllInt(readInCode(reader,range[a],code)); reader += range[a];
                        image[i][g][a] =  color[a] + diffrent;
                    }
                    color = image[i][g];
                }
                first = false;
                color = image[i][startC];
            }
        }
        for(int i = 0; i < IMAGE_HEIGHT; i++)
        {
            for(int g = 0; g < IMAGE_WIDTH; g++)
            {
                if(image[i][g] == null)
                {
                    try {
                        image[i][g] = booleanToColor(readInCode(reader,24,code)); reader += 24;
                    }
                    catch (Exception e)
                    {

                    }
                }
            }
        }
        return image;
    }



}
