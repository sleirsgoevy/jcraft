package org.homebrew;

import java.util.Random;
import java.util.HashMap;

class MapGen
{
    public static final int LATEST_MAP_VERSION = 5;
    private static void gen_heightmap(double[] heightmap, Random r, int map_version)
    {
        heightmap[0] = heightmap[128] = heightmap[128*129] = heightmap[128*130] = 64;
        for(int step = 64; step != 0; step >>= 1)
        {
            //square
            for(int x = step; x <= 128; x += 2 * step)
                for(int y = step; y <= 128; y += 2 * step)
                {
                    int pos = y*129+x;
                    double cur = (heightmap[pos-130*step] + heightmap[pos-128*step] + heightmap[pos+128*step] + heightmap[pos+130*step]) / 4;
                    cur += (step*step)/64*r.nextDouble()-(step*step)/128;
                    heightmap[pos] = cur;
                }
            //diamond
            for(int y = 0; y <= 128; y += step)
            {
                for(int x = (y + step) % (2 * step); x <= 128; x += 2 * step)
                {
                    double cur = (heightmap[((y+129-step)%129)*129+x]
                                 +heightmap[((y+step)%129)*129+x]
                                 +heightmap[y*129+(x+129-step)%129]
                                 +heightmap[y*129+(x+step)%129])/4;
                    cur += (step*step)/64*r.nextDouble()-(step*step)/128;
                    heightmap[y*129+x] = cur;
                }
            }
        }
    }
    private static void gen_heightmap_old(byte[] heightmap, Random r, int map_version)
    {
        heightmap[0] = heightmap[128] = heightmap[128*129] = heightmap[128*130] = 64;
        for(int step = 64; step != 0; step >>= 1)
        {
            //square
            for(int x = step; x <= 128; x += 2 * step)
                for(int y = step; y <= 128; y += 2 * step)
                {
                    int pos = y*129+x;
                    int cur = (heightmap[pos-130*step] + heightmap[pos-128*step] + heightmap[pos+128*step] + heightmap[pos+130*step]) / 4;
                    if(map_version == 0)
                        cur += r.nextInt(2*step) - step;
                    else
                        cur += r.nextInt((step*step)/64+1)-(step*step)/128;
                    if(cur < 1)
                        cur = 1;
                    if(cur > 127)
                        cur = 127;
                    heightmap[pos] = (byte)cur;
                }
            //diamond
            for(int y = 0; y <= 128; y += step)
            {
                for(int x = (y + step) % (2 * step); x <= 128; x += 2 * step)
                {
                    int cur = (heightmap[((y+129-step)%129)*129+x]
                              +heightmap[((y+step)%129)*129+x]
                              +heightmap[y*129+(x+129-step)%129]
                              +heightmap[y*129+(x+step)%129])/4;
                    if(map_version == 0)
                        cur += r.nextInt(2*step) - step;
                    else
                        cur += r.nextInt((step*step)/64+1)-(step*step)/128;
                    if(cur < 1)
                        cur = 1;
                    if(cur > 127)
                        cur = 127;
                    heightmap[y*129+x] = (byte)cur;
                }
            }
        }
    }
    public static void generate(byte[] world, byte[] world0, int[] aux, int seed, int map_version, boolean lite /* used for start screen */, GeneratingGUI report)
    {
        Random r = new Random(seed);
        byte[] heightmap = new byte[129*129];
        if(map_version <= 2)
            gen_heightmap_old(heightmap, r, map_version);
        else
        {
            double[] heightmap_d = new double[129*129];
            gen_heightmap(heightmap_d, r, map_version);
            for(int i = 0; i < 129*129; i++)
            {
                double x = heightmap_d[i];
                if(x < 0)
                    x = 0;
                if(x > 127)
                    x = 127;
                heightmap[i] = (byte)Math.floor(x);
            }
            heightmap_d = null;
        }
        if(map_version >= 4)
        {
            if(!lite) // full gen
            {
                Life3d.life3d(r, world, world0, aux, report);
                for(int i = 0; i < 128*128*128; i++)
                    world0[i] = world[i]; // back up cave data
            }
            else
            {
                for(int i = 0; i < 128*128*128; i++)
                {
                    world0[i] = 1;
                    r.nextInt(1); // dry-run the RNG to get the same random numbers later on
                }
            }
        }
        else
            for(int i = 0; i < 128*128*128; i++)
                world0[i] = 1;
        for(int i = 0; i < 128*128*128; i++)
            world[i] = 0;
        for(int x = 0; x < 128; x++)
            for(int z = 0; z < 128; z++)
            {
                int h = heightmap[x*129+z];
                int offset = 16384*x+128*z;
                while(h > 0 && world0[offset + h - 1] == 0)
                    h--;
                for(int i = 0; i < h; i++)
                    if(world0[offset+i] != 0)
                        world[offset+i] = (byte)(map_version>=4?132:131); //stone or dirt
                for(int i = (h>3?h-3:0); i < h; i++)
                    if(world0[offset+i] != 0)
                        world[offset+i] = (byte)131; //dirt
                if(world0[offset+h-1] != 0)
                    world[offset+h-1] = (byte)128; //grass
                if(map_version >= 2 && r.nextInt(100) == 0)
                {
                    world[offset+h-1] = (byte)131; //dirt
                    for(int i = h; i < 128 && i < h + 4; i++)
                        world[offset+i] = (byte)129; //wood
                    for(int i = h + 3; i < 128 && i < h + 5; i++)
                        for(int xi = (x>0?x-1:x); xi < 128 && xi <= x + 1; xi++)
                            for(int zi = (z>0?z-1:z); zi < 128 && zi <= z + 1; zi++)
                                if(world[16384*xi+128*zi+i] == 0)
                                    world[16384*xi+128*zi+i] = (byte)130; //leaves
                    if(h + 5 < 128 && world[offset+h+5] == 0)
                        world[offset+h+5] = (byte)130; //leaves
                }
            }
        if(map_version >= 5)
            for(int x = 0; x < 128; x++)
                for(int z = 0; z < 128; z++)
                    for(int y = 0; y < 11; y++)
                    {
                        int pos = 16384*x+128*z+y;
                        if(world[pos] == 0)
                            world[pos] = (byte)(y==10?126:127);
                    }
        heightmap = null;
    }
}
