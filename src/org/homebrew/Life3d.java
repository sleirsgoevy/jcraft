package org.homebrew;

import java.util.Random;

class Life3d
{
    public static void life3d(Random r, byte[] world, byte[] world0, GeneratingGUI report)
    {
        for(int i = 0; i < 128*128*128; i++)
            world[i] = (byte)r.nextInt(2);
        for(int i = 0; i < 128*128*128; i++)
            world0[i] = 2;
        while(true)
        {
            for(int i = 0; i < 128*128*128; i++)
                world0[i] = (byte)((world0[i] & 2) << 1);
            for(int x = 0; x < 128; x++)
                for(int y = 0; y < 128; y++)
                    for(int z = 0; z < 128; z++)
                    {
                        if((world0[16384*x+128*z+y] & 4) == 0)
                            continue;
                        int cnt = 0;
                        for(int xi = x + 127; xi <= x + 129; xi++)
                            for(int yi = y + 127; yi <= y + 129; yi++)
                                for(int zi = z + 127; zi <= z + 129; zi++)
                                    if(world[16384*(xi%128)+128*(zi%128)+(yi%128)] != 0)
                                        cnt++;
                        if((world[16384*x+128*z+y] != 0 && cnt >= 9) || cnt >= 18)
                        {
                            world0[16384*x+128*z+y] |= 1;
                            if(world0[16384*x+128*z+y] != world[16384*x+128*z+y])
                                for(int xi = x + 127; xi <= x + 129; xi++)
                                    for(int yi = y + 127; yi <= y + 129; yi++)
                                        for(int zi = z + 127; zi <= z + 129; zi++)
                                            world0[16384*(xi%128)+128*(zi%128)+(yi%128)] |= 2;
                        }
                    }
            int modcnt = 0;
            int nonzero = 0;
            for(int i = 0; i < 128*128*128; i++)
            {
                if(world[i] != (world0[i] & 1))
                {
                    world[i] = (byte)(world0[i] & 1);
                    modcnt++;
                }
                if(world[i] != 0)
                    nonzero++;
            }
            System.out.println("life3d "+modcnt+", nonzero "+nonzero);
            if(modcnt == 0)
                break;
            double percent = 1 - Math.log(modcnt)/Math.log(50000);
            if(report != null)
                report.reportProgress(percent);
        }
    }
}
