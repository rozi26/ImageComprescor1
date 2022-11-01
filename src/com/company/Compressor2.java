package com.company;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.company.Orginizer.Pixel;

public class Compressor2 { //better version of compressor1. great with simple drawable images
   /* public class Pixel extends Orginizer.Pixel
    {
        private boolean[] colorIdentify;
        public Pixel(int[] _color)
        {
            super(_color);
        }
        public Pixel(Pixel p)
        {
            super(p.color);
        }
        private void setColorIdentify(boolean[] id)
        {
            colorIdentify = id.clone();
        }
    }*/
    public static boolean[] compress(int[][][] image)
    {
        return compress(Orginizer.getPixelsReport(image),image);
    }
    public static boolean[] compress(Orginizer.PixelsReport menger, int[][][] image)
    {
        /*
        0-15 image width
        16-20 loop size
        */
        final int EMPTY_COLOR_SIZE = 24;
        int linkSize = 1; // what is the size of the link length
        int linkColors = 0;//how many colors use linker
        int linkPixels = 0;//how many pixels use linker
        int loopPixels = 0;//how many loops there are
        int totalPixels = 0;// how many pixels there are (loop count as 1)

        final List<Pixel> list = menger.getList();
        final int LOOP_LENGTH = (int)(Math.log((menger.getBiggestLoop())) / Math.log(2)) + 1;
        final int index = menger.getIndex();
        final int LIST_SIZE = list.size();
        for(int i = 0; i < LIST_SIZE; i++)
        {
            final Pixel pixel = list.get(i);
            if(linkColors < Math.pow(2,linkSize))
            {
                linkColors++;
                linkPixels += pixel.count;
            }
            else // check if it's worth to make the link size bigger
            {
                int profit = 0;
                for(int g = i; g < Math.pow(2,linkSize + 1) && g < LIST_SIZE; g++)
                {
                    profit += (list.get(g).count * (EMPTY_COLOR_SIZE - linkSize - 1)) - 25;
                }
                if(profit > linkPixels)
                {
                    linkSize++;
                    i--;
                }
                else
                    break;
            }
        }
        //System.out.println("best link size is " + linkSize);
        for(int i = 0; i < LIST_SIZE; i++)
        {
            Pixel pixel = list.get(i);
            totalPixels += pixel.count;
            loopPixels += pixel.loopCount;
            if(i < linkColors)
                pixel.setColorIdentify(intToBoolean(i,linkSize));
            else
                pixel.setColorIdentify(colorToBoolean(pixel.color));
        }
        boolean[] code = new boolean[37 + linkColors * 25 + totalPixels * 2 + loopPixels * LOOP_LENGTH + linkPixels * linkSize + (totalPixels - linkPixels) * EMPTY_COLOR_SIZE];
        writeNumInCode(intToBoolean(image.length,16),0,code); // write the image height
        writeNumInCode(intToBoolean(image[0].length,16),16,code); // write the image width
        writeNumInCode(intToBoolean(LOOP_LENGTH,5),32,code); // write the loop size
        int writer = 37;
        for(int i = 0; i < linkColors; i++) // write the linkers
        {
            writeNumInCode(colorToBoolean(list.get(i).color),writer,code);
            writer += EMPTY_COLOR_SIZE;
            code[writer] = i == linkColors - 1;
            writer++;
        }
        for(int i = 0; i < index; i++)
        {
            final Pixel pixel = getEqualTo(image[i / image[0].length][i % image[0].length],list);
            final boolean[] id = pixel.colorIdentify;
            final int loopLength = pixel.haveLoopIn(i);
            code[writer] = id.length != 24;
            writer++;
            code[writer] = loopLength != 0;
            writer++;
            writeNumInCode(id,writer,code);
            writer += id.length;
            if(loopLength != 0)
            {
                writeNumInCode(intToBoolean(loopLength,LOOP_LENGTH),writer,code);
                writer += LOOP_LENGTH;
                i += loopLength;
            }
        }
        return code;
    }

    public static int[][][] uncompress(boolean[] code)
    {
        final int height = booleanToInt(readInCode(0,16,code));
        final int width = booleanToInt(readInCode(16,16,code));
        final int loopSize = booleanToInt(readInCode(32,5,code));
        int reader = 37;
        class LinksMenger
        {
            class Link
            {
                boolean[] id;
                final boolean[] color;
                private Link(boolean[] _color)
                {
                    color = _color;
                }
                private boolean idEqual(boolean[] to)
                {
                    for(int i = 0; i < to.length; i++)
                    {
                        if(to[i] ^ id[i]) return false;
                    }
                    return true;
                }
            }
            List<Link> list = new ArrayList<Link>();
            int LinkLength;
            public void addLink(boolean[] color)
            {
                list.add(new Link(color));
            }
            public void setLinks()
            {
                final int size = list.size();
                LinkLength = (size < 2)?1:(int)(Math.log(size - 1) / Math.log(2)) + 1;
                for(int i = 0; i < size; i++)
                {
                    list.get(i).id = intToBoolean(i,LinkLength);
                    //System.out.println("link " + booleanToString(intToBoolean(i,length)) + " -> " + Data.colorToRGBCode(booleanToColor(list.get(i).color)));
                }
            }
            public boolean[] linkToColor(boolean[] link)
            {
                for(Link id:list)
                {
                    if(id.idEqual(link))
                        return id.color;
                }
                System.out.println("error didn't found connection for " + booleanToString(link) + " (" + booleanToInt(link) + ")");
                return null;
            }
        }
        final LinksMenger menger = new LinksMenger();
        while (true)
        {
            menger.addLink(readInCode(reader,24,code));
            reader += 24;
            if(code[reader])
            {
                reader++;
                menger.setLinks();
                break;
            }
            else
                reader++;
        }
        int[][][] image = new int[height][width][3];
        int index = 0;
        final int LINK_LENGTH = menger.LinkLength;
        while (reader < code.length)
        {
            final boolean link = code[reader]; reader++;
            final boolean loop = code[reader]; reader++;
            boolean[] color;
            if(link)
            {
                color = menger.linkToColor(readInCode(reader,LINK_LENGTH,code));
                reader += LINK_LENGTH;
            }
            else
            {
                color = readInCode(reader,24,code);
                reader += 24;
            }
            final int[] RGB = booleanToColor(color);
            int copies;
            if(loop)
            {
                copies = booleanToInt(readInCode(reader,loopSize,code)) + 1;
                reader += loopSize;
            }
            else copies = 1;
            for(int i = 0; i < copies; i++)
            {
                image[index / width][index % width] = RGB;
                index++;
            }
        }
        return image;
    }

    private static boolean equalInt3(int[] a, int[] b)
    {
        return a[0] == b[0] && a[1] == b[1] && a[2] == b[2];
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
    private static Pixel getEqualTo(int[] color, List<Pixel> list)
    {
        for(Pixel pixel: list)
        {
            if(pixel.equal(color))
                return pixel;
        }
        System.out.println("error didn't found match");
        return null;
    }
}
