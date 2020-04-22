package org.homebrew;

import java.io.*;

class LevelSave
{
    private static String level_save_path;
    public static void setLevelSavePath(String s)
    {
        level_save_path = s;
    }
    private static void writeInt(FileOutputStream fos, int x) throws IOException
    {
        //System.out.println("writeInt "+x);
        for(int i = 0; i < 4; i++)
        {
            fos.write(x & 255);
            x >>= 8;
        }
    }
    private static int readInt(FileInputStream fis) throws IOException
    {
        int ans = 0;
        for(int i = 0; i < 4; i++)
            ans |= fis.read() << (i << 3);
        //System.out.println("readInt "+ans);
        return ans;
    }
    private static void writeDouble(FileOutputStream fos, double d) throws IOException
    {
        long bits = Double.doubleToLongBits(d);
        writeInt(fos, (int)bits);
        writeInt(fos, (int)(bits >> 32));
    }
    private static double readDouble(FileInputStream fis) throws IOException
    {
        long low = readInt(fis);
        long high = readInt(fis);
        return Double.longBitsToDouble((high << 32) | (low & 0xffffffffl));
    }
    public static void loadGame(byte[] world, byte[] origWorld, int[] genParams, double[] playerPos, String filename)
    {
        try
        {
            FileInputStream fis = new FileInputStream(level_save_path+filename);
            int mapSeed = genParams[0] = readInt(fis);
            int mapVersion = genParams[1] = readInt(fis);
            for(int i = 0; i < 128*128*128; i++)
                world[i] = 0;
            MapGen.generate(world, mapSeed, mapVersion);
            for(int i = 0; i < 128*128*128; i++)
                origWorld[i] = world[i];
            for(int cur = readInt(fis); cur != -1; cur = readInt(fis))
                world[cur & 0xffffff] = (byte)(cur >> 24);
            for(int i = 0; i < 5; i++)
                playerPos[i] = readDouble(fis);
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }
    public static void saveGame(byte[] world, byte[] origWorld, int[] genParams, double[] playerPos, String filename)
    {
        try
        {
            FileOutputStream fos = new FileOutputStream(level_save_path+filename);
            writeInt(fos, genParams[0]);
            writeInt(fos, genParams[1]);
            for(int i = 0; i < 128*128*128; i++)
                if(world[i] != origWorld[i])
                    writeInt(fos, (((int)world[i])<<24)|i);
            writeInt(fos, -1);
            for(int i = 0; i < 5; i++)
                writeDouble(fos, playerPos[i]);
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }
    public static boolean saveExists(String filename)
    {
        return (new File(level_save_path+filename)).exists();
    }
}
