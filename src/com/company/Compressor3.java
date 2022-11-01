package com.company;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Compressor3 {
    final int COLOR_SHIFT_MAX = 32;
    int WIDTH_MARK_COST = 32;
    int HEIGHT_MARK_COST = 32;

    private List<Square> squares = new ArrayList<>();
    private class Square
    {
        final private int startL;
        final private int startC;
        final private int firstBoost;
        final private int lastBoost;
        final private int[] startColor;
        final private int[] range;
        final private int width;
        final private int height;
        private Square(int[] color, int _startL,int _startC, int _width, int _height, int[] _range, int _firstBoost, int _lastBoost)
        {
            startL = _startL;
            startC = _startC;
            startColor = color;
            width = _width;
            height = _height;
            range = _range;
            firstBoost = _firstBoost;
            lastBoost = _lastBoost;
        }
        private int getCost()
        {
            int shiftSum = minimumPixelToRepresent(range[0]) + minimumPixelToRepresent(range[1]) + minimumPixelToRepresent(range[2]);
            return (WIDTH_MARK_COST + HEIGHT_MARK_COST + minimumPixelToRepresent(width)) * 2 + (log2(COLOR_SHIFT_MAX) * 3) + 24 + (((width + 1) * (height + 1) - (firstBoost - startC) - lastBoost) * shiftSum);
        }
        private boolean inRange(int i, int g)
        {
            return (i >= startL && i - startL <= height) && ((g >= ((i == startL)?firstBoost:startC)) && ((i == startL + height)?g < lastBoost:g - startC <= width));
        }
        public String toString()
        {
            return "(" + startC + "," + startL + ") -- (" + (startC + width) + "," + startL + ")\n|\t\t\t|\n|\t\t\t|\n(" + startC + "," + (startL + height) + ") -- (" + (startC + width) + "," + (startL + height) + ")\ncolor: " + Data.colorToRGBCode(startColor) + "\nrange: [" + range[0] +"|" + range[1] + "|" + range[2] + "]\n(" + firstBoost + "|" + lastBoost + ")";
        }
    }
    public int[][][] compress(int[][][] image) //compressor for neutral images
    {
        HEIGHT_MARK_COST = minimumPixelToRepresent(image.length);
        WIDTH_MARK_COST =  minimumPixelToRepresent(image[0].length);
        for(int i =0; i < image.length; i++)
        {
            for(int g = 0; g < image[0].length; g++)
            {
                if(canPutSquareIn(i,g))
                {
                    addSquare(i,g,image);
                }
            }
        }
        printSquareReport(image);
        final int[] paintColor = new int[]{63,63,0};
        for(Square square: squares)
        {
            for(int i = square.startC; i <= square.startC + square.width; i++)
            {
                image[square.startL][i] = paintColor;
                image[square.startL + square.height][i] = paintColor;
            }
            for(int i = square.startL; i <= square.startL + square.height; i++)
            {
                image[i][square.startC] = paintColor;
                image[i][square.startC + square.width] = paintColor;
            }
        }/**/
        return null;
    }
    private Square getSquare(int startL, int startC, int[][][] image,int[] colorShift, int iStart, int gStart, int[] startColor,int[] color, int run, int firstBoost, int gLimit)
    {
        boolean first = true;
        int lastI = startL;
        int[] range = colorShift.clone();
        for(int i = iStart; i < image.length; i++)
        {
            lastI = i;
            if(!canPutSquareIn(i,startC))
                break;
            for(int g = (first)?gStart:startC; g < gLimit; g++)
            {
                boolean changeAdd = false;
                boolean GetToLimit = false;
                final int[] shift = getColorShift(image[i][g],color);
                int[] needAdd = new int[3];
                for(int h = 0; h < 3; h++)
                {
                    final int rangeUpdate = colorShift[h] * (int)Math.pow(2,needAdd[h]);
                    if(shift[h] >= rangeUpdate)
                    {
                        if(rangeUpdate == COLOR_SHIFT_MAX) { GetToLimit = true; break;}
                        needAdd[h]++;
                        h--;
                        changeAdd = true;
                    }
                }
                if(GetToLimit) // if the color shift is in the limit
                {
                   if(ifGoodToQuit(i,g,iStart,gStart,g,image,startC,colorShift))
                     return new Square(startColor,startL,startC,gLimit - startC - 1,i - startL,range,firstBoost,g);
                   else
                   {
                       gLimit = g;
                       break;
                   }
                }
                if(changeAdd)
                {
                    final Square stopNow = new Square(startColor,startL,startC,gLimit - startC - 1,i - startL,colorShift,firstBoost,g);
                    final Square adding = getSquare(i,g,image,new int[]{1,1,1},i,g + 1,image[i][g],image[i][g],run + 1,g,gLimit);
                    final Square combine = add(stopNow,adding,image);
                    int[] a = new int[]{stopNow.getCost(),adding.getCost(),combine.getCost()};
                    if(stopNow.getCost() + adding.getCost() < combine.getCost())
                    {
                        squares.add(adding);
                        if(ifGoodToQuit(i,g,iStart,gStart,gLimit,image,startC,colorShift))
                            return stopNow;
                        else
                            gLimit = g;
                    }
                    else
                        return combine;
                }
                if(g != gLimit) color = image[i][g];
                if(!canPutSquareIn(i,g + 1)) gLimit = g + 1;
            }
            first = false;
        }
        return new Square(startColor,startL,startC,gLimit - startC - 1,lastI - startL,range,firstBoost,gLimit);
    }
    private boolean addSquare(int startL, int startC, int[][][] image)
    {
        Square s = getSquare(startL,startC,image,new int[]{1,1,1},startL,startC + 1,image[startL][startC],image[startL][startC],0,0,image[0].length);
        if(s == null) return false;
        squares.add(s); return true;
    }


    private int[] getColorShift(int[] c1, int[] c2)
    {
        return new int[]{Math.abs(c1[0]-c2[0]),Math.abs(c1[1]-c2[1]),Math.abs(c1[2]-c2[2])};
    }
    private static int log2(int num)
    {
        for(int i = 0; i < 31; i++) {if(((num >> i) & 1) == 1) return i;}return -1;
    }
    private static int minimumPixelToRepresent(int num)
    {
        num--;
        for(int i = 30; i >= 0; i--)
        {
            if(((num >> i) & 1) == 1) return i + 1;
        }
        return 0;
    }
    private Square add(Square a, Square b, int[][][] image)
    {

        //if(!Arrays.equals(a.startColor,b.startColor) || a.startC != b.startC || a.startL != b.startL) {System.out.println("error add two unConnected squares");}
        final int[] diffrents =  getColorShift((a.lastBoost == a.startC?image[a.startL + a.height - 1][a.startC + a.width]:image[a.startL + a.height][a.lastBoost - 1]),image[b.startL][b.firstBoost]);
        int[] range = new int[3];
        for(int i = 0; i < range.length; i++)
        {
            range[i] = Math.max(Math.max(a.range[i],b.range[i]),(diffrents[i]));
        }/**/
        return new Square(a.startColor,a.startL,a.startC,b.startC + b.width - a.startC,b.startL + b.height - a.startL,range,a.firstBoost,b.lastBoost);
    }
    private boolean canPutSquareIn(int i, int g)
    {
        for(Square square: squares)
        {
            if(square.inRange(i,g)) return false;
        }
        return true;
    }

    private void printSquareReport(int[][][] image)
    {
        int costSum = minimumPixelToRepresent(image.length) + minimumPixelToRepresent(image[0].length);
        for(Square square: squares)
        {
            System.out.println(square.toString() + "\n");
            costSum += square.getCost();
        }
        final int clearCost = (image.length * image[0].length * 24);
        final int simpleCost = Compressor2.compress(image).length;
        System.out.println("there " + squares.size() + " squares\ntotalCost: " + costSum + " (" + Data.presentOf(costSum,clearCost) + "%)\nLightCost: " + simpleCost + " (" + Data.presentOf(simpleCost,clearCost) + "%)\nclearCost: " + clearCost);
    }

    private boolean ifGoodToQuit(int i,int g,int iStart,int gStart, int gLimit,int[][][] image,int startC,int[] colorShift)
    {
        int h = i;
        final int[] testingColor = image[h][startC];
        boolean leave = false;
        h++;
        while(h < image.length && !leave)
        {
            int[] diffrent = getColorShift(testingColor,image[h][startC]);
            for(int k = 0; k < 3; k++)
            {
                if(diffrent[k] > colorShift[k]) {leave = true; break;}
            }
            h++;
        }
        h--;
        return (h - iStart) * (g - gStart) <= ((i + 1) - iStart) * ((gLimit - gStart) + ((g - gLimit)));
    }
}
