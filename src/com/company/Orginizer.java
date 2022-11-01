package com.company;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class Orginizer {

    public static PixelsReport getPixelsReport(int[][][] image)
    {
        ListMeneger meneger = new ListMeneger(image[0][0]);
        int index = 1;
        for(int i = 0; i < image.length; i++)
        {
            for(int g = (i == 0)?1:0; g < image[0].length; g++)
            {
                meneger.addColor(image[i][g],index);
                index++;
            }
        }
        meneger.endLoop(index);
        meneger.sortList();
        return new PixelsReport(meneger.list,meneger.biggestLoop,index);
    }
    public static class PixelsReport
    {
        final private List<Pixel> list;
        final private int biggestLoop;
        final private int index;
        private PixelsReport(List<Pixel> _list, int _biggestLoop, int _index)
        {
            list = _list;
            biggestLoop = _biggestLoop;
            index = _index;
        }
        public List<Pixel> getList()
        {
            return list;
        }
        public int getBiggestLoop()
        {
            return biggestLoop;
        }
        public int getIndex()
        {
            return index;
        }
        public String getColorsReport()
        {
            StringBuilder text = new StringBuilder("there " + Data.niceWrite(list.size()) + " colors:\n");
            for(Pixel p:list)
            {
                //text.append(Data.colorToRGBCode(p.color));
                text.append("\n");
                text.append(Data.colorToRGBCode(p.color));
                text.append(" (");
                for(int i = 0; i < 3; i++)
                {
                    text.append("[");
                    text.append(p.color[i]);
                    text.append("]");
                }
                text.append(") ");
                text.append(Data.niceWrite(p.getAllPixels()));
            }
            return text.toString();
        }
    }
    private static class ListMeneger
    {
        List<Pixel> list = new ArrayList<Pixel>();
        private int biggestLoop = 0;

        private int[] prevColor;
        private int prevStart;
        private int prevListIndex;
        ListMeneger(int[] firstColor)
        {
            prevColor = firstColor;
            prevStart = 0;
            prevListIndex = 0;
            list.add(new Pixel(firstColor));
        }
        private void addColor(int[] color, int index)
        {
            if(!equalInt3(prevColor,color))
            {
                endLoop(index);
                prevColor = color;
                prevStart = index;
                for(int i = 0; i < list.size(); i++)
                {
                    if(list.get(i).equal(color)){list.get(i).count++;prevListIndex = i; return;}
                }
                prevListIndex = list.size();
                list.add(new Pixel(color));
            }
        }
        private void endLoop(int index)
        {
            if(index - prevStart > 1)
            {
                list.get(prevListIndex).addLoop(prevStart,index - 1);
                biggestLoop = Math.max(biggestLoop,index - 1 - prevStart);
            }
        }
        private void  printReport()
        {
            for(Pixel pixel: list)
            {
                System.out.println(pixel.toString());
            }
        }
        private void sortList()
        {
            list.sort(new Comparator<Pixel>() {
                @Override
                public int compare(Pixel o1, Pixel o2) {
                    return (o1.count > o2.count) ? -1 : 0;
                }
            });
        }
    }
    public static class Pixel
    {
        final int[] color;
        boolean[] colorIdentify;
        int count = 1;
        int loopCount = 0;
        List<Integer> loopsIndex = null;
        List<Integer> loopsLength = null;
        public Pixel(int[] _color)
        {
            color = _color;
        }
        public boolean equal(int[] _color)
        {
            return equalInt3(color,_color);
        }
        public void addLoop(int from, int to)
        {
            if(loopsIndex == null)
            {
                loopsLength = new ArrayList<Integer>();
                loopsIndex = new ArrayList<Integer>();
            }
            loopsIndex.add(from);
            loopsLength.add(to - from);
            loopCount++;
        }
        /* public String toString()
         {
             StringBuilder text = new StringBuilder();
             text.append("color: " + Data.colorToRGBCode(color));
             text.append("\ncount: " + count);
             text.append("\nloops count: " + ((loopsIndex == null)?0:loopsIndex.size()));
             if(loopsIndex != null)
             {
                 for(int i = 0; i < loopsIndex.size(); i++)
                 {
                     text.append("\n\tloop from " + loopsIndex.get(i));
                     text.append(" -> " + (loopsIndex.get(i) + loopsLength.get(i)));
                     text.append(" (" + (loopsLength.get(i)) + ")");
                 }
             }
             text.append("\n\n");
             return text.toString();
         }*/
        public int haveLoopIn(int index)
        {
            if(loopsIndex == null) return 0;
            for(int i = 0; i < loopsIndex.size(); i++)
            {
                if(loopsIndex.get(i) == index) return loopsLength.get(i);
            }
            return 0;
        }
        private  int getAllPixels()
        {
            if(loopsLength == null) return count;
            int sum = count;
            for(int i:loopsLength)
            {
                sum += i;
            }
            return sum;
        }
        public void setColorIdentify(boolean[] id)
        {
            colorIdentify = id.clone();
        }
    }


    private static boolean equalInt3(int[] a, int[] b)
    {
        return a[0] == b[0] && a[1] == b[1] && a[2] == b[2];
    }

}
