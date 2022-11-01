package com.company;

import java.util.ArrayList;
import java.util.List;

public class SquaresRelative {
    final int MIN_SIZE = 100000;
    final int JEN_LIMIT = 5;

    final int COLOR_SHIFT_MAX;
    final int IMAGE_WIDTH;
    final int IMAGE_HEIGHT;
    final int WIDTH_MARK_COST;
    final int HEIGHT_MARK_COST;

    public class Square
    {
        final public int startL;
        final public int startC;
        final public int[] range;
        public int width;
        public int height;
        private Square(int _startL,int _startC, int _width, int _height, int[] _range)
        {
            startL = _startL;
            startC = _startC;
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
            return "(" + startC + "," + startL + ") -- (" + (startC + width - 1) + "," + startL + ")\n|\t\t\t|\n|\t\t\t|\n(" + startC + "," + (startL + height -1 ) + ") -- (" + (startC + width - 1) + "," + (startL + height - 1) + ")\nrange: [" + range[0] +"|" + range[1] + "|" + range[2] + "]\ncost: " + getCost() + "\npixel worth: " + Data.round(getPixelsWorth(),3);
        }
        /*private boolean testRange(int[][][] image)
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
        }*/
    }
    public static class Pixel
    {
        final int x;
        final int y;
        int[] relativeDown;
        int[] relativeRight;
        public Pixel(int[] _relativeDown, int[] _relativeRight, int _x, int _y)
        {
            x = _x;
            y = _y;
            relativeDown = _relativeDown;
            relativeRight = _relativeRight;
        }
        private int[] getDownRange()
        {
            return new int[]{minimumPixelToRepresent(relativeDown[0]),minimumPixelToRepresent(relativeDown[1]),minimumPixelToRepresent(relativeDown[2])};
        }
        private int[] getRightRange()
        {
            return new int[]{minimumPixelToRepresent(relativeRight[0]),minimumPixelToRepresent(relativeRight[1]),minimumPixelToRepresent(relativeRight[2])};
        }
    }
    private SquaresRelative layerDown = null;
    public Pixel[][] grid;
    final int shrink;
    List<Square> squares = new ArrayList<>();
    public SquaresRelative(Pixel[][] _grid, int _shrink,int SHIFT_MAX)
    {
        COLOR_SHIFT_MAX = SHIFT_MAX;
        grid = _grid;
        IMAGE_WIDTH = grid[0].length; WIDTH_MARK_COST = minimumPixelToRepresentPositive(IMAGE_WIDTH);
        IMAGE_HEIGHT = grid.length; HEIGHT_MARK_COST = minimumPixelToRepresentPositive(IMAGE_HEIGHT);
        shrink = _shrink;
        final int area = grid.length * grid[0].length;
        if(area > MIN_SIZE && shrink < JEN_LIMIT)
        {
            Pixel[][] nextGrid = new Pixel[(grid.length + 1) / 2][(grid[0].length + 1) / 2];
            for(int i = 0 ; i < nextGrid.length; i++)
            {
                final int iLoc = i * 2;
                for(int g = 0; g < nextGrid[0].length; g++)
                {
                    try {
                        final int[] r11 = grid[iLoc][g * 2].relativeRight;
                        final int[] r12 = (g * 2 + 1 == grid[0].length || grid[iLoc][g * 2 + 1].relativeRight == null)?r11:grid[iLoc][g * 2 + 1].relativeRight;
                        final int[] r1 = biggestAbs(r11,r12);
                        int[] r2;
                        if(iLoc + 1 == grid.length) r2 = r1;
                        else
                        {
                            final int[] r21 = grid[iLoc + 1][g*2].relativeRight;
                            final int[] r22 =  (g * 2 + 1 == grid[0].length || grid[iLoc + 1][g * 2 + 1].relativeRight == null)?r21:grid[iLoc + 1][g * 2 + 1].relativeRight;
                            r2 = biggestAbs(r21,r22);
                        }
                        final int[] d11 = grid[iLoc][g * 2].relativeDown;
                        final int[] d12 = (grid[iLoc + 1][g * 2].relativeDown == null)?d11:grid[iLoc + 1][g * 2].relativeDown;
                        final int[] d1 = biggestAbs(d11,d12);
                        int[] d2;
                        if(g * 2 + 1 == grid[0].length) d2 = d1;
                        else
                        {
                            final int[] d21 = grid[iLoc][g * 2 + 1].relativeDown;
                            final int[] d22 = (grid[iLoc + 1][g * 2 + 1].relativeDown == null)?d21:grid[iLoc + 1][g * 2 + 1].relativeDown;
                            d2 = biggestAbs(d21,d22);
                        }
                        nextGrid[i][g] = new Pixel(biggestAbs(d1,d2),biggestAbs(r1,r2),g,i);
                    }
                    catch (Exception e){
                        System.out.println("fa");
                    }
                }
            }
            layerDown = new SquaresRelative(nextGrid,shrink + 1,SHIFT_MAX);
        }
        else
        {
            for(int i = 0; i < grid.length; i++)
            {
                for(int g= 0 ; g < grid[0].length; g++)
                {
                    if(canPutSquareIn(i,g))
                    {
                        addSquare(i,g);
                    }
                }
            }
        }
    }
    public List<Square> calculateSquares()
    {
        if(layerDown != null)
        {
            List<Square> list = layerDown.calculateSquares();
            for(Square square:list)
            {
                Square result = new Square(square.startL * 2,square.startC * 2,square.width * 2 - 1,square.height * 2 - 1,square.range);
                if(result.startL + result.height > grid.length) result.height = grid.length - result.startL - 1;
                if(result.startC + result.width > grid[0].length) result.width = grid[0].length - result.startC - 1;
                final boolean thereRightSpace = result.startC + result.width != grid[0].length;
                final boolean thereDownSpace = result.startL + result.height != grid.length;
                boolean addToRight = thereRightSpace;
                if(addToRight)
                {
                    for(int i = result.startL; i < result.startL + result.height; i++)
                    {
                        final int[] d = grid[i][result.startC + result.width - 1].getRightRange();
                        for(int a = 0; a < 3;a++){if(d[a] > square.range[a]){addToRight = false; break;}}
                    }
                    if(addToRight) result.width++;
                }
                boolean addToDown = thereDownSpace;
                if(addToDown)
                {
                    for(int i = result.startC; i < result.startC + result.width; i++)
                    {
                        final int[] d = grid[result.startL + result.height - 1][i].getDownRange();
                        for(int a = 0; a < 3;a++){if(d[a] > square.range[a]){addToDown = false; break;}}
                    }
                    if(addToDown) result.height++;
                }
                squares.add(result);
                if(thereRightSpace && !addToRight)
                {
                    for(int i = result.startL; i < result.startL + result.height; i++)
                    {
                        squares.add(new Square(i,result.startC + result.width,1,1,new int[]{0,0,0}));
                    }
                }
                if(thereDownSpace && !addToDown)
                {
                    for(int g = result.startC; g < result.startC + result.width; g++)
                    {
                        squares.add(new Square(result.startL + result.height,g,1,1,new int[]{0,0,0}));
                    }
                }/**/
                if(thereDownSpace && thereRightSpace && !addToDown && !addToRight) squares.add(new Square(result.startL + result.height,result.startC + result.width,1,1,new int[]{0,0,0}));
            }
        }
        return squares;
    }
    private Square getSquare(int startL, int startC, int[] colorShift, int gLimit)
    {
        return getSquare(startL,startC,colorShift,gLimit,startL,startC,false);
    }
    private Square getSquare(int startL, int startC, int[] colorShift, int gLimit, int firstI, int firstG, boolean cl)
    {
        // final boolean newOne = (firstI == startL || firstG != startC) && firstG != gLimit;
        //int[] color = image[(newOne)?startL:firstI - 1][(newOne)?firstG:startC];
        Pixel color = (cl)?grid[firstI - 1][startC]:(firstG == startC)?grid[startL][firstG]:grid[startL][firstG - 1];
        // int[] color = (startL == firstI && startC == firstG)?image[startL][startC]:cl?image[firstI - 1][startC]:image[firstI][firstG];
        boolean FIRST_RUN = true;
        int[] range = colorShift.clone();
        int lastI = 0;
        for(int i = firstI; i < grid.length; i++)
        {
            lastI = i;
            for(int g = (FIRST_RUN)?firstG:startC; g < gLimit; g++)
            {
                boolean changeAdd = false;
                boolean GetToLimit = false;
                final int[] shift = (i == color.y && g == color.x)?new int[]{0,0,0}:(g == startC)?color.getDownRange():color.getRightRange();
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
                    final boolean firstLine =  g == startC || i + 1 == grid.length;
                    final Square cutLine = (height1)?null:new Square(startL,startC,gLimit - startC,i - startL,range);
                    final Square cutColl = (firstLine)?null:getSquare(startL,startC,range.clone(),g,i + 1,startC,true);
                   // final Square cutColl = (firstLine)?null:getSquare(i,startC,range.clone(),g);
                    final Square stop = (cutLine == null && cutColl == null)?
                            new Square(startL,startC,1,1,range):
                            (!height1 && (firstLine || cutLine.getPixelsWorth() < cutColl.getPixelsWorth()))?cutLine:cutColl;
                    final Square play = getSquare(startL,startC,rangeTest,gLimit,i,g,false);
                    if(play.getPixelsWorth() < stop.getPixelsWorth())
                        return play;
                    return stop;/**/
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
                            return new Square(startL,startC,0,1,range);
                    }
                    else
                    {
                        final Square stopNow = new Square(startL,startC,gLimit - startC,i - startL,range);
                        if(gLimitSetter == startC || i == grid.length - 1){if(cantPoot){stopNow.height++;} return stopNow;}
                        //final int[] addDown = getColorShiftRange(image[i][startC],image[i - 1][startC]);
                        final int[] addDown = grid[i - 1][startC].getDownRange();
                        //for(int a = 0; a < 3; a++) {addDown[a] = roundToClosestMax(addDown[a]);}
                        int[] diffrent = new int[3];
                        for(int a = 0; a < 3; a++){diffrent[a] = Math.max(addDown[a],range[a]); if(diffrent[a] > COLOR_SHIFT_MAX) return stopNow;}
                        final Square adder = getSquare(i ,startC,diffrent,gLimitSetter);
                        final Square combine = new Square(startL,startC,adder.width,stopNow.height + adder.height,new int[]{Math.max(range[0],adder.range[0]),Math.max(range[1],adder.range[1]),Math.max(range[2],adder.range[2])});
                        //int[] a = new int[]{stopNow.getCost(),adder.getCost(),combine.getCost()};
                        if(stopNow.getCost() + adder.getCost() < combine.getCost())
                        {
                            squares.add(adder);
                            return stopNow;
                        }
                        return combine;
                    }
                }
                color = grid[i][g];
            }
            color = grid[i][startC];
            FIRST_RUN = false;
            if(!canPutSquareIn(i + 1,startC)) break;
        }
        return new Square(startL,startC,gLimit - startC,lastI - startL + 1,range);
    }
    private void addSquare(int startL, int startC)
    {
        Square s = getSquare(startL,startC,new int[]{0,0,0},grid[0].length);
        if(s != null)squares.add(s);
    }



    private int biggestAbs(int a, int b)
    {
        if(Math.abs(a) == Math.abs(b)) return Math.max(a,b);
        return Math.abs(a) > Math.abs(b)?a:b;
    }
    private int[] biggestAbs(int[] a,int[] b)
    {
      //  if(a == null) return b;
        int[] c = new int[a.length];
        for(int i = 0; i < a.length; i++)
        {
            c[i] = biggestAbs(a[i],b[i]);
        }
        return c;
    }
    private static int[] getColorShift(int[] c1, int[] c2)
    {
        return new int[]{c1[0]-c2[0],c1[1]-c2[1],c1[2]-c2[2]};
    }
    private static int minimumPixelToRepresentPositive(int num)
    {
        for(int i = 30; i >= 0; i--)
        {
            if(((num >> i) & 1) == 1) return i + 1;
        }
        return 0;
    }
    private static int minimumPixelToRepresent(int num)
    {
        if(num == 0) return 0;
        if(((num >> 31) & 1) == 1) {num = Math.abs(num); num--;}
        return minimumPixelToRepresentPositive(num) + 1;
    }

    private boolean canPutSquareIn(int i, int g)
    {
        for(Square square: squares)
        {
            if(square.inRange(i,g)) return false;
        }
        return true;
    }
}
