package com.company;

public class BoolCompresor {
    public static boolean[] compressBool(boolean[] arr)
    {
        int[] counts = new int[(int)Math.pow(2,stackSize)];
        getEqual c = new getEqual(stackSize);
        boolean[] num = new boolean[stackSize];
        final int limit = arr.length - stackSize;
        for(int i = 0; i < limit; i += stackSize)
        {
           for(int g = 0; g < stackSize; g++)
           {
               num[g] = arr[i + g];
           }
           counts[c.getIndex(num)]++;
        }
        int[] orderIndex = orderCounts(counts);
        chengeText con = new chengeText(orderIndex,stackSize);
        /*for(int i = 0; i < counts.length; i++)
        {
            boolean[] number = Compressor1.intToBinary(i,stackSize);
            for(int g = 0; g < stackSize; g++)
            {
                System.out.print((number[g])?"1":"0");
            }
            System.out.print(":\t" + counts[i]);
            System.out.println();
        }*/
        int lengthCount = 0;
        for(int i = 0; i < counts.length; i++)
        {
            lengthCount += con.getCode(i).length * (counts[i] + 1);
        }
        boolean[] newList = new boolean[lengthCount + 4];
        if(true)
        {
            final boolean[] stackSizeBinary = Compressor1.intToBinary(stackSize,4);
            System.arraycopy(stackSizeBinary, 0, newList, 0, 4);
            int i = 4;
            for(int g = 0; g < counts.length; g++)
            {
                final boolean[] numBin = con.getCode(g);
                for(int h = 0; h < numBin.length; h++)
                {
                    newList[i + h] = numBin[h];
                }
                i += numBin.length;
            }
        }
        return newList;
    }
    public boolean[] unCompressBool(boolean[] arr)
    {
        return new boolean[1];
    }

    private static class getEqual
    {
        boolean[][] models;
        getEqual(int stackSize)
        {
            final int limit = (int)Math.pow(2,stackSize);
            models = new boolean[limit][];
            for(int i = 0; i < limit; i++)
            {
                models[i] = Compressor1.intToBinary(i,stackSize);
            }
        }
        int getIndex(boolean[] num)
        {
            for(int i = 0; i < models.length; i++)
            {
                if(equal(num,models[i]))
                    return i;
            }
            return -1;
        }
    }
    private static boolean equal(boolean[] a, boolean[] b)
    {
        for(int i = 0; i < a.length; i++)
        {
            if(a[i] ^ b[i])
                return false;
        }
        return true;
    }
    private static int[] orderCounts(int[] counts)
    {
        int[] countsOrder = new int[counts.length];
        int[] countIndex = new int[counts.length];
        for(int i = 0; i < counts.length; i++)
        {
            for(int g = 0; g <= i; g++)
            {
                if(g == i)
                {
                    countsOrder[g] = counts[i];
                    countIndex[g] = i;
                }
                else if(countsOrder[g] > counts[i])
                {
                    for(int h = counts.length - 1; h > g; h--)
                    {
                        countsOrder[h] = countsOrder[h - 1];
                        countIndex[h] = countIndex[h - 1];
                    }
                    countsOrder[g] = counts[i];
                    countIndex[g] = i;
                    break;
                }
            }
        }
        return countIndex;
    }
    final static int stackSize = 4;
    private static class  chengeText
    {
        boolean[][] models;

        chengeText(int[] index, int buffer)
        {
            models = new boolean[index.length][];
            final boolean[][] texts = generateFromBuffer(buffer);
            for(int i = 0 ; i < texts.length; i++)
            {
                models[i] = texts[index.length - 1 - index[i]];
                //System.out.print("text " + i + " = " ); for(int g = 0; g < texts[i].length; g++){System.out.print((texts[i][g])?"1":"0");}
               // System.out.println();
            }
        }

        private boolean[][] generateFromBuffer(int buffer)
        {
           return new boolean[][]
                   {
                           {false, false},
                           {false, true, false},
                           {false, true, true, false},
                           {false, true, true, true},
                           {true,false,false,false},
                           {true,false,false,true},
                           {true,false,true,false},
                           {true,false,true,true},
                           {true,true,false,false},
                           {true,true,false,true},
                           {true,true,true,false,true},
                           {true,true,true,false,false},
                           {true,true,true,true,false,false},
                           {true,true,true,true,false,true},
                           {true,true,true,true,true,false},
                           {true,true,true,true,true,true}
                   };
        }
        boolean[] getCode(int loc)
        {
           return models[loc];
        }
    }
}
