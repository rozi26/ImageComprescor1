package com.company;

public class Compressor1 {

    public static boolean[] getCompressCode(int[][][] image) //on binary
    {
        pixel head2 = null;
        for(int i = 0; i < image.length; i++)
        {
            for(int g = 0; g < image[0].length; g++)
            {
                final int[] colors = image[i][g];
                if(head2 == null)
                    head2 = new pixel(colors[0],colors[1],colors[2]);
                else
                {
                    head2.addColor(new int[]{colors[0],colors[1],colors[2]});
                }
            }
        }
        pixel head = null;
        final boolean orgMode = true;
        if(orgMode) //organize the colors by amount
        {
            while (true)
            {
                pixel prevPrevNode = null;
                pixel prevnode = null;
                pixel node = head2;
                while (node != null)
                {
                    if(prevPrevNode != null && prevnode.counts < node.counts)
                    {
                        prevPrevNode.next = node;
                        prevnode.next = node.next;
                        node.next = prevnode;
                    }
                    prevPrevNode = prevnode;
                    prevnode = node;
                    node = prevnode.next;
                }

                if(true)
                {
                    pixel prevTestNode = head2.next;
                    pixel testNode = head2.next.next;
                    boolean fail = false;
                    while (testNode != null)
                    {
                        if(prevTestNode.counts < testNode.counts)
                        {
                            fail = true;
                            break;
                        }
                        prevTestNode = testNode;
                        testNode = testNode.next;
                    }
                    if(!fail)
                    {
                        pixel eNode = head2.next;
                        pixel fNode = head2.withOutNext();
                        if(eNode.counts > fNode.counts)
                        {
                            boolean finish = false;
                            while (eNode.next != null)
                            {
                                if(eNode.next.counts < fNode.counts)
                                {
                                    fNode.next = eNode.next;
                                    eNode.next = fNode;
                                    finish = true;
                                    break;
                                }
                                eNode = eNode.next;
                            }
                            if(!finish)
                            {
                                pixel lNode = head2;
                                while (lNode.next != null)
                                {
                                    lNode = lNode.next;
                                }
                                lNode.next = fNode;
                            }
                            head2 = head2.next;
                        }
                        break;
                    }
                }
            }

            /*pixel node = head;
            while (node != null)
            {
                System.out.println(node.toString());
                node = node.next;
            }*/
        }
        head = head2;
        //pixel report
        pixel node = head;
        pixel linksHead = null;
        int keepBytes = 1; // how many bits there in the link of the color
        int keepSpace = 2; // the maximum number of colors with link
        int keepRegisters = 0; // the number of colors with link
        int colorLinks = 0; // how many colors use link
        int colors = 0; // how many colors there are
        boolean everLost = false;
        while (node != null)
        {
            final int count = node.counts;
            boolean add = false;
            if(keepRegisters < keepSpace)
            {
                add = true;
            }
            else if(!everLost)
            {
                if(!orgMode)
                {
                    int lost = colorLinks + 24; // all the colors that will add bit the and the markup
                    int earn = count * 24 / (int)Math.pow(2,keepBytes + 1);
                    if(earn > lost)
                    {
                        add = true;
                        keepBytes++;
                        keepSpace = (int)Math.pow(2,keepBytes);
                    }
                }
                else
                {
                    int lost = getCountPrev(head,node) + 25;
                    int win = (24 - keepBytes) * getCountForward(node,keepBytes * 2);
                    if(win > lost)
                    {
                        add = true;
                        keepBytes++;
                        keepSpace = (int)Math.pow(2,keepBytes);
                    }
                    else
                        everLost = true;
                }
            }

            if(add)
            {
                if(linksHead == null)
                    linksHead = node.withOutNext();
                else
                    linksHead.getLast().next = node.withOutNext();
                keepRegisters++;
                colorLinks += count;
            }
            else
                colors += count;
            node = node.next;
        }
        final int listLength = ((keepRegisters) * 25) + (colorLinks * (keepBytes + 1)) + ((colors) * 25) + 32;
        boolean[] code = new boolean[listLength];
        if(true)
        {
            final boolean[] width = intToBinary(image.length,16);
            final boolean[] height = intToBinary(image[0].length,16);
            for(int i = 0; i < 16; i++)
            {
                code[i + 16] = width[i];
                code[i] = height[i];
            }
        }
        int i = 32;
        if(true)
        {
            pixel node2 = linksHead;
            int counts = 0;
            while (node2 != null) // verify the links
            {
                node2.setIndex(counts,keepBytes); // mark the link
                final boolean[] color = getPixelColorsAsBinary(node2);
                for(int g = 0; g < 24; g++) // write the link color
                {
                    code[i + g] = color[g];
                }
                node2 = node2.next;
                code[i + 24] = node2 == null;
                i += 25; // if the link is the last
                counts++;
            }
        }
        final int startCode = i;
        for(int x = 0; x < image.length; x++)
        {
            for(int y = 0; y < image[0].length; y++)
            {
                final int red = image[x][y][0];
                final int green = image[x][y][1];
                final int blue = image[x][y][2];
                final pixel link = linksHead.inChildes(red,green,blue);
                int noCount = 0;
                if(link == null) //the color don't have link
                {
                    code[i] = false;
                    i++;
                    final boolean[] color = getPixelColorsAsBinary(red,green,blue);
                    for(int g = 0; g < 24; g++)
                    {
                        code[i + g] = color[g];
                    }
                    i += 24;
                }
                else // the color have link
                {
                    code[i] = true;
                    i++;
                    final boolean[] color = link.index;
                    for(int g = 0; g < keepBytes; g++)
                    {
                        code[i + g] = color[g];
                    }
                    i += keepBytes;
                }
            }
        }
        //return code;
        return backGroundCompressing(code,startCode,keepBytes,colors + colorLinks);
       // return code;
    }
    private static class pixel
    {
        pixel next;
        final int red;
        final int green;
        final int blue;
        boolean[] index; // only when in color_links
        int counts;
        public pixel(int _red, int _green, int _blue)
        {
            red = _red;
            green = _green;
            blue = _blue;
            counts = 1;
        }
        public boolean equal(int _red, int _green, int _blue)
        {
            return red == _red && green == _green && blue == _blue;
        }
        public void setNext(pixel _next)
        {
            next = _next;
        }
        public void addCount()
        {
            counts++;
        }
        public void setIndex(int num, int bits)
        {
            index = intToBinary(num,bits);
        }
        public void addColor(int[] RGB)
        {
            if(equal(RGB[0],RGB[1],RGB[2]))
                addCount();
            else
            {
                if(next == null)
                    next = new pixel(RGB[0],RGB[1],RGB[2]);
                else
                {
                    next.addColor(RGB);
                }
            }
        }
        public String toString()
        {
            return colorToRGBCode(red) + colorToRGBCode(green) + colorToRGBCode(blue) + ": " + counts;
        }
        private String colorToRGBCode(int color)
        {
            String code = "";
            if(color / 16 < 10)
                code += Integer.toString(color / 16);
            else
                code += (char)(color / 16 + 55);

            if(color % 16 < 10)
                code += Integer.toString(color % 16);
            else
                code += (char)(color % 16 + 55);
            return code;
        }
        private pixel inChildes(int _red, int _green, int _blue)
        {
            if(equal(_red,_green,_blue))
                return this;
            if(next == null)
                return null;
            return next.inChildes(_red,_green,_blue);
        }
        public pixel clone()
        {
            pixel p = new pixel(red,green,blue);
            if(next != null)
                p.setNext(next.clone());
            p.counts = counts;
            return p;
        }
        public pixel withOutNext()
        {
            pixel p = new pixel(red,green,blue);
            p.counts = counts;
            return p;
        }
        public pixel getLast()
        {
            if(next == null)
                return this;
            return next.getLast();
        }
        private pixel getPixelByIndex(boolean[] _index)
        {
            if(index == null)
            {
                System.out.println("error tried to find index on null indexes list");
                return null;
            }
            boolean fail = false;
            for(int i = 0; i < _index.length; i++)
            {
                if(index[i] != _index[i])
                {
                    fail = true;
                    break;
                }
            }
            if(fail)
            {
                if(next == null)
                    return null;
                return next.getPixelByIndex(_index);
            }
            return this;
        }
    }

    private static boolean[] getPixelColorsAsBinary(int red, int green, int blue)
    {
        boolean[] b = new boolean[24];
        final boolean[] b1 = intToBinary(red,8);
        final boolean[] b2 = intToBinary(green,8);
        final boolean[] b3 = intToBinary(blue,8);
        for(int i = 0; i < 8; i++)
        {
            b[i] = b1[i];
            b[i + 8] = b2[i];
            b[i + 16] = b3[i];
        }
        return b;
    }
    private static boolean[] getPixelColorsAsBinary(pixel p)
    {
        return getPixelColorsAsBinary(p.red,p.green,p.blue);
    }
    public static boolean[] intToBinary(int num, int bits)
    {
        boolean[] b = new boolean[bits];
        for(int i = 0; i < bits; i++)
        {
            final int test = (int)Math.pow(2,bits - i - 1);
            if(num >= test)
            {
                num -= test;
                b[i] = true;
            }
        }
        return b;
    }
    public static int binaryToInt(boolean[] bin)
    {
        int num = 0;
        int add = 1;
        for(int i = bin.length - 1; i >= 0; i--)
        {
            if(bin[i])
            {
                num += add;

            }
            add *= 2;
        }
        return num;
    }
    private static int[] binaryToRGB(boolean[] bin)
    {
        boolean[] b1 = new boolean[8];
        boolean[] b2 = new boolean[8];
        boolean[] b3 = new boolean[8];
        for(int i = 0; i < 8; i++)
        {
            b1[i] = bin[i];
            b2[i] = bin[i + 8];
            b3[i] = bin[i + 16];
        }
        return new int[]{binaryToInt(b1),binaryToInt(b2),binaryToInt(b3)};
    }


    public static int[][][] unCompress(boolean[] code)
    {
        final boolean[] widthBinary = new boolean[16];
        System.arraycopy(code, 0, widthBinary, 0, 16);
        final int width = binaryToInt(widthBinary);
        final boolean[] heightBinary = new boolean[16];
        System.arraycopy(code, 16, heightBinary, 0, 16);
        final int height = binaryToInt(heightBinary);
        int i = 32;
        pixel links = null;
        int linksCounter = 0;
        int linkBitSize = 1;
        while (true)
        {
            boolean[] color = new boolean[24];
            for(int g = 0; g < 24; g++)
            {
                color[g] = code[i + g];
            }
            final int[] RGB = binaryToRGB(color);
            if(links == null)
            {
                links = new pixel(RGB[0],RGB[1],RGB[2]);
            }
            else
                links.getLast().setNext(new pixel(RGB[0],RGB[1],RGB[2]));
            i += 25;
            linksCounter++;
            if(code[i - 1])
            {
                while (Math.pow(2,linkBitSize) < linksCounter)
                {
                    linkBitSize++;
                }
                break;
            }
        }
        code = backGroundUnCompensation(code,i,linkBitSize);
        if(true)
        {
            int nodeCount = 0;
            pixel node = links;
            while(node != null)
            {
                node.setIndex(nodeCount,linkBitSize);
                nodeCount++;
                node = node.next;
            }
        }
        int[][][] image = new int[height][width][3];
        int widthCount = 0;
        int heightCount = 0;
        pixel line = null;
        while (i < code.length)
        {
            final boolean link = code[i];
            i++;
            int red;
            int green;
            int blue;
            if(link)
            {
                boolean[] index = new boolean[linkBitSize];
                for(int g = 0; g < linkBitSize; g++)
                {
                    index[g] = code[i + g];
                }
                final pixel match = links.getPixelByIndex(index);
                if(match == null)
                {
                    System.out.println("error didn't found match");
                    red = -1;
                    green = -1;
                    blue = -1;
                }
                else
                {
                    red = match.red;
                    green = match.green;
                    blue = match.blue;
                }
                i += linkBitSize;
            }
            else
            {
                boolean[] colors = new boolean[24];
                for(int g = 0; g < 24; g++)
                {
                    colors[g] = code[i + g];
                }
                int[] RGB = binaryToRGB(colors);
                red = RGB[0];
                green = RGB[1];
                blue = RGB[2];
                i += 24;
            }
            image[heightCount][widthCount][0] = red;
            image[heightCount][widthCount][1] = green;
            image[heightCount][widthCount][2] = blue;
            widthCount++;
            if(widthCount == width)
            {
                heightCount++;
                widthCount = 0;
            }
        }
        return image;
    }


    private static int getCountForward(pixel head, int forward)
    {
        if(forward == 1 || head.next == null)
            return head.counts;
        return head.counts + getCountForward(head,forward - 1);
    }
    private static int getCountPrev(pixel head, pixel thisOne)
    {
        if(head == thisOne)
            return 0;
        return head.counts + getCountPrev(head.next,thisOne);
    }



    private static boolean[] backGroundCompressing(boolean[] arr, int codeStart, int linkLength, int pixels)
    {
        boolean[] lastColor = null;
        int equalCount = 0;
        int equalCountSetter = 0;
        boolean[] newArr = new boolean[arr.length + pixels];
        System.arraycopy(arr, 0, newArr, 0, codeStart);
        int i = codeStart;
        int s = codeStart;
        /*int length1 = 0;
        int length2 = 0;
        int length3 = 0;
        int length4 = 0;*/
        while (i < arr.length)
        {
            boolean fail = false;
            boolean finishLoop = false;
            boolean[] color = null;
            if(arr[i])
            {
                i++;
                color = new boolean[linkLength];
                for(int g = 0; g < linkLength; g++)
                {
                    color[g] = arr[i + g];
                }
                if(lastColor == null)
                {
                    equalCount++;
                    lastColor = color;
                }
                else if(equal(color,lastColor))
                {
                    equalCount++;
                }
                else
                {
                    equalCountSetter = 1;
                    finishLoop = true;
                }
                i += linkLength;
               // length4 += linkLength + 1;
            }
            else
            {
                equalCountSetter = 0;
                finishLoop = lastColor != null;
                fail = true;
            }
            if(finishLoop || (i >= arr.length))
            {
               // System.out.print("found loop of ") ; for(int g = 0;g < linkLength; g++){System.out.print((lastColor[g])?"1":"0");} System.out.println(" " + equalCount + " times");
                if(equalCount > 4)
                {
                    newArr[s] = true;
                    s++;
                    for(int g = 0; g < linkLength; g++)
                    {
                        newArr[s + g] = lastColor[g];
                    }
                    s += linkLength;
                    final boolean[] amount = intToBinary(equalCount,32);
                    for(int g= 0 ; g < 32; g++)
                    {
                        newArr[s + g] = amount[g];
                    }
                    s += 32;
                   // length1 += (linkLength + 1) * equalCount;
                }
                else
                {
                    for(int g = 0; g < equalCount; g++)
                    {
                        newArr[s] = false;
                        newArr[s + 1] = true;
                        s += 2;
                        for(int h = 0; h < linkLength; h++)
                        {
                            newArr[s + h] = lastColor[h];
                        }
                        s += linkLength;
                    }
                   // length3 += (linkLength + 1) * equalCount;
                }
                lastColor = color;
                equalCount = equalCountSetter;
            }
            if(fail)
            {
                newArr[s] = false;
                s++;
                for(int g = 0; g < 25; g++)
                {
                    newArr[s + g] = arr[i + g];
                }
                i += 25;
                s += 25;
               // length2 += 25;
            }
        }
        boolean[] lastArr = new boolean[s];
        System.arraycopy(newArr, 0, lastArr, 0, lastArr.length);
        //System.out.println("real length = " + arr.length);
       /* System.out.println("length 1 = " + length1);
        System.out.println("length 2 = " + length2);
        System.out.println("length 3 = " + (length3));
        System.out.println("length 4 = " + (length4));
        System.out.println("(" + length4 + " = " + (length1 + length3) + ") (" + (length4 == (length1 + length3)) + ")");
        System.out.println("together = " + (length2 + length4 + codeStart));*/
        return lastArr;
    }
    public static boolean[] backGroundUnCompensation(boolean[] arr, int codeStart, int linkLength)
    {
        int length = codeStart;
        //int length1 = 0;
        //int length2 = 0;
        if(true)
        {
            int i = codeStart;
            while(i < arr.length)
            {
                if(arr[i])
                {
                    i += linkLength + 1;
                    final boolean[] amount = new boolean[32];
                    for(int g = 0; g < 32; g++)
                    {
                        amount[g] = arr[i + g];
                    }
                    i += 32;
                    final int num = binaryToInt(amount);
                    length += (linkLength + 1) * num;
                    //length1 += (linkLength + 1) * num;
                }
                else
                {
                    if(arr[i + 1])
                    {
                        length += linkLength + 1;
                       // length2 += linkLength + 1;
                        i += linkLength + 2;
                    }
                    else
                    {
                        length += 25;
                        //length2 += 25;
                        i += 26;
                    }
                }
            }
        }
        //System.out.println("length = " + length);

       /* System.out.println("length1 = " + length1);
        System.out.println("length2 = " + length2);
        System.out.println("together " + (length2 + length1 + codeStart));/*
        */
        boolean[] code = new boolean[length];
        int i = codeStart;
        int s = codeStart;
        while(i < arr.length)
        {
            if(arr[i])
            {
                final boolean[] color = new boolean[linkLength];
                for(int g = 0; g < linkLength; g++)
                {
                    color[g] = arr[i + 1 + g];
                }
                i += linkLength + 1;
                final boolean[] amount = new boolean[32];
                for(int g = 0; g < 32; g++)
                {
                    amount[g] = arr[i + g];
                }
                i += 32;
                final int num = binaryToInt(amount);
                for(int g = 0; g < num; g++)
                {
                    code[s] = true;
                    for(int h = 0; h < linkLength; h++)
                    {
                        code[s + 1 + h] = color[h];
                    }
                    s += linkLength + 1;
                }
                //length1 += (linkLength + 1) * num;
            }
            else
            {
                if(arr[i + 1])
                {
                    for(int g = 0; g < linkLength + 1; g++)
                    {
                        code[s + g] = arr[i + 1 + g];
                    }
                    s += linkLength + 1;
                    // length2 += linkLength + 1;
                    i += linkLength + 2;
                }
                else
                {
                    for(int g = 0; g < 25; g++)
                    {
                        code[s + g] = arr[i + g + 1];
                    }
                    s += 25;
                    //length2 += 25;
                    i += 26;
                }
            }
        }
        return code;
    }
    private static boolean equal(boolean[] a, boolean[] b)
    {
        for(int i=  0; i < a.length; i++)
        {
            if(a[i] ^ b[i])
                return false;
        }
        return true;
    }
}
