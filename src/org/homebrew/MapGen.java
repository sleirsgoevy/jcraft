package org.homebrew;

import java.util.Random;
import java.util.HashMap;

class MapGen
{
    public static void generate(byte[] world, int seed)
    {
        Random r = new Random(seed);
        byte[] heightmap = new byte[129*129];
        heightmap[0] = heightmap[128] = heightmap[128*129] = heightmap[128*130] = 64;
        for(int step = 64; step != 0; step >>= 1)
        {
            //square
            for(int x = step; x <= 128; x += 2 * step)
                for(int y = step; y <= 128; y += 2 * step)
                {
                    int pos = y*129+x;
                    int cur = (heightmap[pos-130*step] + heightmap[pos-128*step] + heightmap[pos+128*step] + heightmap[pos+130*step]) / 4;
                    cur += r.nextInt(2*step)-step;
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
                    cur += r.nextInt(2*step)-step;
                    if(cur < 1)
                        cur = 1;
                    if(cur > 127)
                        cur = 127;
                    heightmap[y*129+x] = (byte)cur;
                }
            }
        }
        for(int x = 0; x < 128; x++)
            for(int z = 0; z < 128; z++)
            {
                int h = heightmap[x*129+z];
                int offset = 16384*x+128*z;
                for(int i = 0; i < h; i++)
                    world[offset+i] = (byte)131; //dirt
                world[offset+h-1] = (byte)128; //grass
            }
        heightmap = null;
    }
}
