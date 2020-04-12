package org.homebrew;

import java.util.Random;

public class GameMain
{
    final static int[] block_colors = {
        0xff00ff00, 0xffff8000, 0xffff8000 /* grass */,
        0xff800000, 0xff800000, 0xff800000 /* wood */,
        0xff008000, 0xff008000, 0xff008000 /* leaves */,
        0xffff8000, 0xffff8000, 0xffff8000 /* dirt */,
    };
    final static int[] block_textures = {
        0, 1, 2 /* grass */,
        2, 2, 2 /* stub */,
        2, 2, 2 /* stub */,
        2, 2, 2 /* dirt */,
    };
    private byte[] world;
    private byte[] maxHeight;
    private double playerX;
    private int playerX_fp;
    private double playerY;
    private int playerY_fp;
    private double playerZ;
    private int playerZ_fp;
    private double playerYaw;
    private double playerYaw_cos;
    private int playerYaw_cos_fp;
    private double playerYaw_sin;
    private int playerYaw_sin_fp;
    private double playerPitch;
    private double playerPitch_cos;
    private int playerPitch_cos_fp;
    private double playerPitch_sin;
    private int playerPitch_sin_fp;
    private byte[] preflight;
    private int[] dsu;
    private boolean[] keyStates;
    private int vel_x;
    private double vel_y;
    private int vel_z;
    private int vel_yaw;
    private int vel_pitch;
    private long prev_time;
    private int[] texture_atlas;
    private int pointed_to;
    public GameMain()
    {
        world = new byte[128*128*128];
        maxHeight = new byte[128*128];
        MapGen.generate(world, 179);
        for(int i = 0; i < 128*128; i++)
            for(int j = 0; j < 128; j++)
                if(world[128*i+j] != 0)
                    maxHeight[i] = (byte)j;
        playerX = playerZ = 64;
        playerY = 127;
        playerYaw = playerYaw_sin = playerPitch_cos = 0;
        playerPitch = -Math.PI/2;
        playerYaw_cos = 1;
        playerPitch_sin = -1;
        vel_y = vel_x = vel_z = 0;
        preflight = new byte[(128*128*128)/8];
        dsu = new int[640*480];
        keyStates = new boolean[1024];
        prev_time = System.currentTimeMillis();
        texture_atlas = TextureAtlas.atlas;
        pointed_to = -1;
    }
    public void render(int[] buffer)
    {
        for(int i = 0; i < preflight.length; i++)
            preflight[i] = 0;
        int px = (int)playerX;
        int py = (int)playerY;
        int pz = (int)playerZ;
        dfs_preflight(preflight, world, px, py, pz, px, py, pz);
        for(int i = 0; i < 640*480; i++)
            buffer[i] = 0;
        for(int i = 0; i < 640*480; i++)
            dsu[i] = i + 1;
        for(int i = px; i >= 0; i--)
        {
            for(int j = py; j >= 0; j--)
            {
                for(int k = pz; k >= 0; k--)
                    render_block(buffer, i, j, k);
                for(int k = pz + 1; k < 128; k++)
                    render_block(buffer, i, j, k);
            }
            for(int j = py + 1; j < 128; j++)
            {
                for(int k = pz; k >= 0; k--)
                    render_block(buffer, i, j, k);
                for(int k = pz + 1; k < 128; k++)
                    render_block(buffer, i, j, k);
            }
        }
        for(int i = px + 1; i < 128; i++)
        {
            for(int j = py; j >= 0; j--)
            {
                for(int k = pz; k >= 0; k--)
                    render_block(buffer, i, j, k);
                for(int k = pz + 1; k < 128; k++)
                    render_block(buffer, i, j, k);
            }
            for(int j = py + 1; j < 128; j++)
            {
                for(int k = pz; k >= 0; k--)
                    render_block(buffer, i, j, k);
                for(int k = pz + 1; k < 128; k++)
                    render_block(buffer, i, j, k);
            }
        }
        int prev_pos = -1;
        int prev_side = -1;
        int tex_start = -1;
        if((buffer[640*240+320] & 0xff000000) == 0xb3000000)
            pointed_to = buffer[640*240+320] & 0x00ffffff;
        else
            pointed_to = -1;
        for(int i = 0; i < 640*480; i++)
            if(buffer[i] == 0)
                buffer[i] = -1;
            else if((buffer[i] & 0xff000000) == 0xb3000000)
            {
                int side = (buffer[i] & 0xe00000) >> 21;
                int pos = buffer[i] & 0x1fffff;
                if(pos != prev_pos || side != prev_side)
                {
                    prev_pos = pos;
                    prev_side = side;
                    int side2 = side;
                    if(side2 == 0)
                        side2 = 2;
                    else if(side2 == 1)
                        side2 = 0;
                    else
                        side2 = 1;
                    int tex_id = block_textures[3*((255&(int)world[pos])-128)+side2];
                    tex_start = 4096*(tex_id/16)+16*(tex_id%16);
                }
                int bx_fp = ((pos >> 14) << 16) - playerX_fp;
                int bz_fp = (((pos >> 7) & 127) << 16) - playerZ_fp;
                int by_fp = ((pos & 127) << 16) - playerY_fp;
                int vx_fp = ((i%640-320) << 16)/250;
                int vy_fp = ((240-i/640) << 16)/250;
                int vz_fp = 65536;
                int tmp_fp;
                tmp_fp = (int)((vz_fp * (long)playerPitch_cos_fp - vy_fp * (long)playerPitch_sin_fp)>>16);
                vy_fp = (int)((vy_fp * (long)playerPitch_cos_fp + vz_fp * (long)playerPitch_sin_fp)>>16);
                vz_fp = tmp_fp;
                tmp_fp = (int)((vx_fp * (long)playerYaw_cos_fp + vz_fp * (long)playerYaw_sin_fp)>>16);
                vz_fp = (int)((vz_fp * (long)playerYaw_cos_fp - vx_fp * (long)playerYaw_sin_fp)>>16);
                vx_fp = tmp_fp;
                if(side < 2) // y=c
                {
                    tmp_fp = by_fp;
                    by_fp = bz_fp;
                    bz_fp = tmp_fp;
                    tmp_fp = vy_fp;
                    vy_fp = vz_fp;
                    vz_fp = tmp_fp;
                }
                else if(side < 4) // x=c
                {
                    tmp_fp = bx_fp;
                    bx_fp = bz_fp;
                    bz_fp = tmp_fp;
                    tmp_fp = vx_fp;
                    vx_fp = vz_fp;
                    vz_fp = tmp_fp;
                }
                if(side % 2 == 1)
                    bz_fp += 65536;
                long tx_fp, ty_fp;
                if(vz_fp == 0) // wtf??
                    tx_fp = ty_fp = -1;
                else
                {
                    long coef_fp = (((long)bz_fp)<<16)/vz_fp;
                    tx_fp = ((vx_fp*coef_fp)>>16)-bx_fp;
                    ty_fp = ((vy_fp*coef_fp)>>16)-by_fp;
                }
                if(tx_fp < 0)
                    tx_fp = 0;
                if(tx_fp >= 65536)
                    tx_fp = 65535;
                if(ty_fp < 0)
                    ty_fp = 0;
                if(ty_fp >= 65536)
                    ty_fp = 65535;
                int tx_i = (int)(tx_fp>>12);
                int ty_i = (int)(ty_fp>>12);
                if(side >= 2)
                    ty_i = 15 - ty_i;
                buffer[i] = texture_atlas[tex_start+256*ty_i+tx_i];
            }
        playerPhysics();
    }
    private void playerPhysics()
    {
        long cur_time = System.currentTimeMillis();
        for(long tick = prev_time; tick < cur_time; tick++)
        {
            double playerX_prev = playerX;
            double playerY_prev = playerY;
            double playerZ_prev = playerZ;
            playerYaw += vel_yaw / 250.0;
            if(playerYaw > 2*Math.PI)
                playerYaw -= 2*Math.PI;
            else if(playerYaw < 0)
                playerYaw += 2*Math.PI;
            playerYaw_cos = Math.cos(playerYaw);
            playerYaw_sin = Math.sin(playerYaw);
            playerPitch += vel_pitch / 250.0;
            if(playerPitch > Math.PI/2)
                playerPitch = Math.PI/2;
            else if(playerPitch < -Math.PI/2)
                playerPitch = -Math.PI/2;
            playerPitch_cos = Math.cos(playerPitch);
            playerPitch_sin = Math.sin(playerPitch);
            playerX += vel_x * playerYaw_cos / 250.0 + vel_z * playerYaw_sin / 250.0;
            if(playerX < 0)
                playerX = 0;
            if(playerX >= 128)
                playerX = 127.999999;
            playerY += vel_y / 250.0;
            if(playerY < 0)
                playerY = 0;
            if(playerY >= 128)
                playerY = 127.999999;
            vel_y -= 0.004;
            playerZ += vel_z * playerYaw_cos / 250.0 - vel_x * playerYaw_sin / 250.0;
            if(playerZ < 0)
                playerZ = 0;
            if(playerZ >= 128)
                playerZ = 127.999999;
            int px = (int)Math.floor(playerX);
            if(px < 1)
                px = 1;
            if(px > 126)
                px = 126;
            int py = (int)Math.floor(playerY);
            if(py < 4)
                py = 4;
            if(py > 126)
                py = 126;
            int pz = (int)Math.floor(playerZ);
            if(pz < 1)
                pz = 1;
            if(pz > 126)
                pz = 126;
            for(int y = py - 4; y <= py + 1; y++)
                for(int x = px - 1; x <= px + 1; x++)
                    for(int z = pz - 1; z <= pz + 1; z++)
                    {
                        int pos = 16384*x+128*z+y;
                        if(world[pos] >= 0) // byte is signed, so >=0 means <128
                            continue;
                        if(playerX <= x - 0.3 || playerX >= x + 1.3
                        || playerY <= y - 0.3 || playerY > y + 2.6
                        || playerZ <= z - 0.3 || playerZ >= z + 1.3)
                            continue;
                        boolean prevX_ok = (playerX_prev <= x - 0.3 || playerX_prev >= x + 1.3);
                        boolean prevY_ok = (playerY_prev <= y - 0.3 || playerY_prev >= y + 2.6);
                        boolean prevZ_ok = (playerZ_prev <= z - 0.3 || playerZ_prev >= z + 1.3);
                        if(prevY_ok && playerY <= y + 0.5 && (y > 0 && world[pos-1] >= 0))
                        {
                            playerY = y - 0.3;
                            vel_y = 0;
                        }
                        else if(prevY_ok && playerY >= y + 0.5 && (y < 127 && world[pos+1] >= 0))
                        {
                            playerY = y + 2.6;
                            vel_y = 0;
                            if(keyStates[32] || keyStates[10])
                                vel_y = 1.5;
                        }
                        else if(prevX_ok && playerX <= x + 0.5 && (x > 0 && world[pos-16384] >= 0))
                            playerX = x - 0.3;
                        else if(prevX_ok && playerX >= x + 0.5 && (x < 127 && world[pos+16384] >= 0))
                            playerX = x + 1.3;
                        else if(prevZ_ok && playerZ <= z + 0.5 && (z > 0 && world[pos-128] >= 0))
                            playerZ = z - 0.3;
                        else if(prevZ_ok && playerZ >= z + 0.5 && (z < 127 && world[pos+128] >= 0))
                            playerZ = z + 1.3;
                    }
        }
        prev_time = cur_time;
        playerX_fp = (int)(playerX*65536);
        playerY_fp = (int)(playerY*65536);
        playerZ_fp = (int)(playerZ*65536);
        playerPitch_cos_fp = (int)(playerPitch_cos*65536);
        playerPitch_sin_fp = (int)(playerPitch_sin*65536);
        playerYaw_cos_fp = (int)(playerYaw_cos*65536);
        playerYaw_sin_fp = (int)(playerYaw_sin*65536);
    }
    private void dfs_preflight(byte[] preflight, byte[] world, int x, int y, int z, int px, int py, int pz)
    {
        int pos = x*16384+z*128+y;
        int idx = pos >> 3;
        int mask = 1 << (pos & 7);
        if((preflight[idx] & mask) != 0)
            return;
        preflight[idx] = (byte)(preflight[idx] | mask);
        if((world[pos] & 128) != 0) // can't see through solid blocks
        {
            if(y > maxHeight[pos>>7])
                throw new RuntimeException("maxHeight fucked up");
            return;
        }
        boolean skyHit = true;
        for(int xi = (x==0?x:x-1); xi <= x + 1 && xi < 128; xi++)
            for(int zi = (z==0?x:z-1); zi <= z + 1 && zi < 128; zi++)
                if(y - 1 <= maxHeight[128*xi+zi])
                    skyHit = false;
        // ycos       0    -ysin
        // -ysin*psin pcos -ycos*psin
        // ysin*pcos  psin ycos*pcos
        long c1_fp, c2_fp, c3_fp;
        c1_fp = (playerYaw_sin_fp*(long)playerPitch_cos_fp)>>16;
        c2_fp = playerPitch_sin_fp;
        c3_fp = (playerYaw_cos_fp*(long)playerPitch_cos_fp)>>16;
        int zz_fp = 0;
        if(c1_fp > 0)
            zz_fp += (int)(((((x+1)<<16)-playerX_fp)*c1_fp)>>16);
        else
            zz_fp += (int)((((x<<16)-playerX_fp)*c1_fp)>>16);
        if(c2_fp > 0)
            zz_fp += (int)(((((y+1)<<16)-playerY_fp)*c2_fp)>>16);
        else
            zz_fp += (int)((((y<<16)-playerY_fp)*c2_fp)>>16);
        if(c3_fp > 0)
            zz_fp += (int)(((((z+1)<<16)-playerZ_fp)*c3_fp)>>16);
        else
            zz_fp += (int)((((z<<16)-playerZ_fp)*c3_fp)>>16);
        if(zz_fp < 0) // block is fully invisible
            return;
        if((point_out_of_screen(x, y, z)
          & point_out_of_screen(x+1, y, z)
          & point_out_of_screen(x, y+1, z)
          & point_out_of_screen(x+1, y+1, z)
          & point_out_of_screen(x, y, z+1)
          & point_out_of_screen(x+1, y, z+1)
          & point_out_of_screen(x, y+1, z+1)
          & point_out_of_screen(x+1, y+1, z+1)) != 0) // block is in front of the player but outside of the frame
            return;
        if(skyHit && y >= py + 2)
        {
            dfs_preflight(preflight, world, x, y-1, z, px, py, pz);
            return;
        }
        if(x != 127 && x >= px)
            dfs_preflight(preflight, world, x+1, y, z, px, py, pz);
        if(x != 0 && x <= px)
            dfs_preflight(preflight, world, x-1, y, z, px, py, pz);
        if(y != 127 && y >= py && !skyHit)
            dfs_preflight(preflight, world, x, y+1, z, px, py, pz);
        if(y != 0 && y <= py)
            dfs_preflight(preflight, world, x, y-1, z, px, py, pz);
        if(z != 127 && z >= pz)
            dfs_preflight(preflight, world, x, y, z+1, px, py, pz);
        if(z != 0 && z <= pz)
            dfs_preflight(preflight, world, x, y, z-1, px, py, pz);
    }
    private int point_out_of_screen(int x_fp, int y_fp, int z_fp)
    {
        x_fp = (x_fp<<16) - playerX_fp;
        y_fp = (y_fp<<16) - playerY_fp;
        z_fp = (z_fp<<16) - playerZ_fp;
        int tmp_fp;
        tmp_fp = (int)((x_fp * (long)playerYaw_cos_fp - z_fp * (long)playerYaw_sin_fp)>>16);
        z_fp = (int)((x_fp * (long)playerYaw_sin_fp + z_fp * (long)playerYaw_cos_fp)>>16);
        x_fp = tmp_fp;
        tmp_fp = (int)((z_fp * (long)playerPitch_cos_fp + y_fp * (long)playerPitch_sin_fp)>>16);
        y_fp = (int)((y_fp * (long)playerPitch_cos_fp - z_fp * (long)playerPitch_sin_fp)>>16);
        z_fp = tmp_fp;
        if(z_fp == 0)
            return 0;
        long x_fpl = 320*65536 + (x_fp*(250l*65536l)) / z_fp;
        long y_fpl = 240*65536 - (y_fp*(250l*65536l)) / z_fp;
        int mask = 0;
        if(x_fpl < 0)
            mask |= 1;
        if(x_fpl > 640*65536)
            mask |= 2;
        if(y_fpl < 0)
            mask |= 4;
        if(y_fpl > 480*65536)
            mask |= 8;
        return mask;
    }
    private void render_block(int[] buffer, int x, int y, int z)
    {
        int pos = x*16384+z*128+y;
        int idx = pos >> 3;
        int mask = 1 << (pos & 7);
        if((preflight[idx] & mask) == 0)
            return;
        int block = 255&(int)world[x*16384+z*128+y];
        if(block == 0)
            return; //air
        boolean outline = false;//(x-playerX)*(x-playerX)+(y-playerY)*(y-playerY)+(z-playerZ)*(z-playerZ)<=625;
        if(playerY < y && world[pos-1] < 128)
            render_plane(buffer, x, y, z, x+1, y, z, x+1, y, z+1, x, y, z+1, 0xb3000000|pos, outline);
        if(playerY > y+1 && world[pos+1] < 128)
            render_plane(buffer, x, y+1, z, x+1, y+1, z, x+1, y+1, z+1, x, y+1, z+1, 0xb3200000|pos, outline);
        if(playerX < x && world[pos-16384] < 128)
            render_plane(buffer, x, y, z, x, y+1, z, x, y+1, z+1, x, y, z+1, 0xb3400000|pos, outline);
        if(playerX > x+1 && world[pos+16384] < 128)
            render_plane(buffer, x+1, y, z, x+1, y+1, z, x+1, y+1, z+1, x+1, y, z+1, 0xb3600000|pos, outline);
        if(playerZ < z && world[pos-128] < 128)
            render_plane(buffer, x, y, z, x+1, y, z, x+1, y+1, z, x, y+1, z, 0xb3800000|pos, outline);
        if(playerZ > z+1 && world[pos+128] < 128)
            render_plane(buffer, x, y, z+1, x+1, y, z+1, x+1, y+1, z+1, x, y+1, z+1, 0xb3a00000|pos, outline);
    }
    private void render_plane(int[] buffer, int x1_fp, int y1_fp, int z1_fp, int x2_fp, int y2_fp, int z2_fp, int x3_fp, int y3_fp, int z3_fp, int x4_fp, int y4_fp, int z4_fp, int color, boolean outline)
    {
        x1_fp = (x1_fp<<16) - playerX_fp;
        x2_fp = (x2_fp<<16) - playerX_fp;
        x3_fp = (x3_fp<<16) - playerX_fp;
        x4_fp = (x4_fp<<16) - playerX_fp;
        y1_fp = (y1_fp<<16) - playerY_fp;
        y2_fp = (y2_fp<<16) - playerY_fp;
        y3_fp = (y3_fp<<16) - playerY_fp;
        y4_fp = (y4_fp<<16) - playerY_fp;
        z1_fp = (z1_fp<<16) - playerZ_fp;
        z2_fp = (z2_fp<<16) - playerZ_fp;
        z3_fp = (z3_fp<<16) - playerZ_fp;
        z4_fp = (z4_fp<<16) - playerZ_fp;
        int tmp_fp;
        tmp_fp = (int)((x1_fp * (long)playerYaw_cos_fp - z1_fp * (long)playerYaw_sin_fp)>>16);
        z1_fp = (int)((x1_fp * (long)playerYaw_sin_fp + z1_fp * (long)playerYaw_cos_fp)>>16);
        x1_fp = tmp_fp;
        tmp_fp = (int)((z1_fp * (long)playerPitch_cos_fp + y1_fp * (long)playerPitch_sin_fp)>>16);
        y1_fp = (int)((y1_fp * (long)playerPitch_cos_fp - z1_fp * (long)playerPitch_sin_fp)>>16);
        z1_fp = tmp_fp;
        tmp_fp = (int)((x2_fp * (long)playerYaw_cos_fp - z2_fp * (long)playerYaw_sin_fp)>>16);
        z2_fp = (int)((x2_fp * (long)playerYaw_sin_fp + z2_fp * (long)playerYaw_cos_fp)>>16);
        x2_fp = tmp_fp;
        tmp_fp = (int)((z2_fp * (long)playerPitch_cos_fp + y2_fp * (long)playerPitch_sin_fp)>>16);
        y2_fp = (int)((y2_fp * (long)playerPitch_cos_fp - z2_fp * (long)playerPitch_sin_fp)>>16);
        z2_fp = tmp_fp;
        tmp_fp = (int)((x3_fp * (long)playerYaw_cos_fp - z3_fp * (long)playerYaw_sin_fp)>>16);
        z3_fp = (int)((x3_fp * (long)playerYaw_sin_fp + z3_fp * (long)playerYaw_cos_fp)>>16);
        x3_fp = tmp_fp;
        tmp_fp = (int)((z3_fp * (long)playerPitch_cos_fp + y3_fp * (long)playerPitch_sin_fp)>>16);
        y3_fp = (int)((y3_fp * (long)playerPitch_cos_fp - z3_fp * (long)playerPitch_sin_fp)>>16);
        z3_fp = tmp_fp;
        tmp_fp = (int)((x4_fp * (long)playerYaw_cos_fp - z4_fp * (long)playerYaw_sin_fp)>>16);
        z4_fp = (int)((x4_fp * (long)playerYaw_sin_fp + z4_fp * (long)playerYaw_cos_fp)>>16);
        x4_fp = tmp_fp;
        tmp_fp = (int)((z4_fp * (long)playerPitch_cos_fp + y4_fp * (long)playerPitch_sin_fp)>>16);
        y4_fp = (int)((y4_fp * (long)playerPitch_cos_fp - z4_fp * (long)playerPitch_sin_fp)>>16);
        z4_fp = tmp_fp;
        if(outline)
        {
            draw_line(buffer, x1_fp, y1_fp, z1_fp, x2_fp, y2_fp, z2_fp);
            draw_line(buffer, x2_fp, y2_fp, z2_fp, x3_fp, y3_fp, z3_fp);
            draw_line(buffer, x3_fp, y3_fp, z3_fp, x4_fp, y4_fp, z4_fp);
            draw_line(buffer, x4_fp, y4_fp, z4_fp, x1_fp, y1_fp, z1_fp);
        }
        render3(buffer, dsu, x1_fp, y1_fp, z1_fp, x2_fp, y2_fp, z2_fp, x3_fp, y3_fp, z3_fp, color);
        render3(buffer, dsu, x1_fp, y1_fp, z1_fp, x4_fp, y4_fp, z4_fp, x3_fp, y3_fp, z3_fp, color);
    }
    private static void draw_line(int[] buffer, int x1_fp, int y1_fp, int z1_fp, int x2_fp, int y2_fp, int z2_fp)
    {
        int tmp_fp;
        if(z1_fp > z2_fp)
        {
            tmp_fp = x1_fp;
            x1_fp = x2_fp;
            x2_fp = tmp_fp;
            tmp_fp = y1_fp;
            y1_fp = y2_fp;
            y2_fp = tmp_fp;
            tmp_fp = z1_fp;
            z1_fp = z2_fp;
            z2_fp = tmp_fp;
        }
        if(z2_fp <= 0)
            return; // invisible
        else if(z1_fp <= 0)
        {
            tmp_fp = (int)((((long)(z2_fp - 1)<<16)) / (z2_fp - z1_fp));
            x1_fp = x2_fp - (int)(((x2_fp-x1_fp)*(long)tmp_fp)>>16);
            y1_fp = y2_fp - (int)(((y2_fp-y1_fp)*(long)tmp_fp)>>16);
            z1_fp = 1;
        }
        long x1_fpl = 320*65536 + (x1_fp*(250l*65536l)) / z1_fp;
        long y1_fpl = 240*65536 - (y1_fp*(250l*65536l)) / z1_fp;
        long x2_fpl = 320*65536 + (x2_fp*(250l*65536l)) / z2_fp;
        long y2_fpl = 240*65536 - (y2_fp*(250l*65536l)) / z2_fp;
        if(x1_fpl == x2_fpl && y1_fpl == y2_fpl)
        {
            if(x1_fpl >= 0 && x1_fpl <= 639*65536 && y1_fpl >= 0 && y1_fpl <= 479*65536)
            {
                int x = (int)(x1_fpl>>16);
                int y = (int)(y1_fpl>>16);
                if(buffer[y*640+x] == 0)
                    buffer[y*640+x] = 0xff000000; // black
            }
            return;
        }
        long xdiff_fpl = x1_fpl - x2_fpl;
        long ydiff_fpl = y1_fpl - y2_fpl;
        long xd_fpl = xdiff_fpl<0?-xdiff_fpl:xdiff_fpl;
        long yd_fpl = ydiff_fpl<0?-ydiff_fpl:ydiff_fpl;
        if(xd_fpl > yd_fpl)
        {
            long xstart_fpl = (xdiff_fpl<0)?x1_fpl:x2_fpl;
            long xend_fpl = x1_fpl+x2_fpl-xstart_fpl;
            if(xstart_fpl < 0)
                xstart_fpl = 0;
            if(xend_fpl > 639*65536)
                xend_fpl = 639*65536;
            int xa = (int)(xstart_fpl>>16);
            int xb = (int)((xend_fpl+65535)>>16);
            for(int x = xa; x <= xb; x++)
            {
                int y = (int)((y1_fpl + (y2_fpl - y1_fpl) * ((x<<16) - x1_fpl) / (x2_fpl - x1_fpl))>>16);
                if(y >= 0 && y < 480 && buffer[y*640+x] == 0)
                    buffer[y*640+x] = 0xff000000; //black
            }
        }
        else
        {
            long ystart_fpl = ((ydiff_fpl<0)?y1_fpl:y2_fpl);
            long yend_fpl = y1_fpl+y2_fpl-ystart_fpl;
            if(ystart_fpl < 0)
                ystart_fpl = 0;
            if(yend_fpl > 479*65536)
                yend_fpl = 479*65536;
            int ya = (int)(ystart_fpl>>16);
            int yb = (int)((yend_fpl+65535)>>16);
            for(int y = ya; y <= yb; y++)
            {
                int x = (int)((x1_fpl + (x2_fpl - x1_fpl) * ((y<<16) - y1_fpl) / (y2_fpl - y1_fpl))>>16);
                if(x >= 0 && x < 640 && buffer[y*640+x] == 0)
                    buffer[y*640+x] = 0xff000000; //black
            }
        }
    }
    private static void render3(int[] buffer, int[] dsu, int x1_fp, int y1_fp, int z1_fp, int x2_fp, int y2_fp, int z2_fp, int x3_fp, int y3_fp, int z3_fp, int color)
    {
        int tmp_fp;
        if(z1_fp > z2_fp)
        {
            tmp_fp = x1_fp;
            x1_fp = x2_fp;
            x2_fp = tmp_fp;
            tmp_fp = y1_fp;
            y1_fp = y2_fp;
            y2_fp = tmp_fp;
            tmp_fp = z1_fp;
            z1_fp = z2_fp;
            z2_fp = tmp_fp;
        }
        if(z1_fp > z3_fp)
        {
            tmp_fp = x1_fp;
            x1_fp = x3_fp;
            x3_fp = tmp_fp;
            tmp_fp = y1_fp;
            y1_fp = y3_fp;
            y3_fp = tmp_fp;
            tmp_fp = z1_fp;
            z1_fp = z3_fp;
            z3_fp = tmp_fp;
        }
        if(z2_fp > z3_fp)
        {
            tmp_fp = x2_fp;
            x2_fp = x3_fp;
            x3_fp = tmp_fp;
            tmp_fp = y2_fp;
            y2_fp = y3_fp;
            y3_fp = tmp_fp;
            tmp_fp = z2_fp;
            z2_fp = z3_fp;
            z3_fp = tmp_fp;
        }
        if(z3_fp <= 0)
            return; // invisible to the player
        else if(z2_fp <= 0)
        {
            tmp_fp = (int)((((long)(z3_fp-1))<<16) / (z3_fp - z2_fp));
            x2_fp = x3_fp - (int)(((x3_fp - x2_fp) * (long)tmp_fp)>>16);
            y2_fp = y3_fp - (int)(((y3_fp - y2_fp) * (long)tmp_fp)>>16);
            z2_fp = 1;
            tmp_fp = (int)((((long)(z3_fp-1))<<16) / (z3_fp - z1_fp));
            x1_fp = x3_fp - (int)(((x3_fp - x1_fp) * (long)tmp_fp)>>16);
            y1_fp = y3_fp - (int)(((y3_fp - y1_fp) * (long)tmp_fp)>>16);
            z1_fp = 1;
        }
        else if(z1_fp <= 0)
        {
            int x4_fp, y4_fp, z4_fp, x5_fp, y5_fp, z5_fp;
            tmp_fp = (int)((((long)(z2_fp-1))<<16) / (z2_fp - z1_fp));
            x4_fp = x2_fp - (int)(((x2_fp - x1_fp) * (long)tmp_fp)>>16);
            y4_fp = y2_fp - (int)(((y2_fp - y1_fp) * (long)tmp_fp)>>16);
            z4_fp = 1;
            tmp_fp = (int)((((long)(z3_fp-1))<<16) / (z3_fp - z1_fp));
            x5_fp = x3_fp - (int)(((x3_fp - x1_fp) * (long)tmp_fp)>>16);
            y5_fp = y3_fp - (int)(((y3_fp - y1_fp) * (long)tmp_fp)>>16);
            z5_fp = 1;
            render3_raw(buffer, dsu, x4_fp, y4_fp, z4_fp, x5_fp, y5_fp, z5_fp, x3_fp, y3_fp, z3_fp, color);
            render3_raw(buffer, dsu, x4_fp, y4_fp, z4_fp, x2_fp, y2_fp, z2_fp, x3_fp, y3_fp, z3_fp, color);
            return;
        }
        render3_raw(buffer, dsu, x1_fp, y1_fp, z1_fp, x2_fp, y2_fp, z2_fp, x3_fp, y3_fp, z3_fp, color);
    }
    private static void render3_raw(int[] buffer, int[] dsu, int x1_fp, int y1_fp, int z1_fp, int x2_fp, int y2_fp, int z2_fp, int x3_fp, int y3_fp, int z3_fp, int color)
    {
        long x1_fpl = 320*65536 + (x1_fp*(250l*65536l)) / z1_fp;
        long y1_fpl = 240*65536 - (y1_fp*(250l*65536l)) / z1_fp;
        long x2_fpl = 320*65536 + (x2_fp*(250l*65536l)) / z2_fp;
        long y2_fpl = 240*65536 - (y2_fp*(250l*65536l)) / z2_fp;
        long x3_fpl = 320*65536 + (x3_fp*(250l*65536l)) / z3_fp;
        long y3_fpl = 240*65536 - (y3_fp*(250l*65536l)) / z3_fp;
        long tmp_fpl;
        if(y1_fpl > y2_fpl)
        {
            tmp_fpl = x1_fpl;
            x1_fpl = x2_fpl;
            x2_fpl = tmp_fpl;
            tmp_fpl = y1_fpl;
            y1_fpl = y2_fpl;
            y2_fpl = tmp_fpl;
        }
        if(y1_fpl > y3_fpl)
        {
            tmp_fpl = x1_fpl;
            x1_fpl = x3_fpl;
            x3_fpl = tmp_fpl;
            tmp_fpl = y1_fpl;
            y1_fpl = y3_fpl;
            y3_fpl = tmp_fpl;
        }
        if(y2_fpl > y3_fpl)
        {
            tmp_fpl = x2_fpl;
            x2_fpl = x3_fpl;
            x3_fpl = tmp_fpl;
            tmp_fpl = y2_fpl;
            y2_fpl = y3_fpl;
            y3_fpl = tmp_fpl;
        }
        if(y1_fpl == y3_fpl)
            return;
        int starty = (int)(y1_fpl>>16);
        if(starty < 0)
            starty = 0;
        int endy = (int)((y3_fpl+65535)>>16);
        if(endy >= 480)
            endy = 479;
        for(int y = starty; y <= endy; y++)
        {
            long xa_fpl = x1_fpl + (long)(((x3_fpl - x1_fpl) * (double)((y<<16) - y1_fpl)) / (y3_fpl - y1_fpl));
            long xb_fpl;
            if((y<<16) < y2_fpl)
            {
                if(y2_fpl == y1_fpl)
                    xb_fpl = x2_fpl;
                else
                    xb_fpl = x1_fpl + (long)(((x2_fpl - x1_fpl) * (double)((y<<16) - y1_fpl)) / (y2_fpl - y1_fpl));
            }
            else
            {
                if(y3_fpl == y2_fpl)
                    xb_fpl = x2_fpl;
                else
                    xb_fpl = x2_fpl + (long)(((x3_fpl - x2_fpl) * (double)((y<<16) - y2_fpl)) / (y3_fpl - y2_fpl));
            }
            if((y<<16) < y1_fpl)
                xa_fpl = xb_fpl = x1_fpl;
            else if((y<<16) > y3_fpl)
                xa_fpl = xb_fpl = x3_fpl;
            if(xb_fpl < xa_fpl)
            {
                tmp_fpl = xb_fpl;
                xb_fpl = xa_fpl;
                xa_fpl = tmp_fpl;
            }
            if(xa_fpl < 0)
                xa_fpl = 0;
            if(xb_fpl > 639*65535)
                xb_fpl = 639*65535;
            if(xa_fpl > xb_fpl)
                continue;
            int start = 640 * y + (int)(xa_fpl>>16);
            int end = 640 * y + (int)((xb_fpl+65535)>>16);
            int i = start;
            while(i < end)
            {
                if(buffer[i] == 0)
                    buffer[i] = color;
                int next = dsu[i];
                if(next < end)
                    dsu[i] = end;
                i = next;
            }
        }
    }
    private void onkeydown(int key)
    {
        System.out.println("keydown "+key);
        if(key == 65 || key == 412)
            vel_x--;
        if(key == 87 || key == 425)
            vel_z++;
        if(key == 68 || key == 417)
            vel_x++;
        if(key == 83 || key == 424)
            vel_z--;
        /*if(key == 32 || key == 10)
            vel_y++;
        if(key == 16 || key == 461)
            vel_y--;*/
        if(key == 37)
            vel_yaw--;
        if(key == 38)
            vel_pitch++;
        if(key == 39)
            vel_yaw++;
        if(key == 40)
            vel_pitch--;
        if((key == 27 || key == 19 || key == 415) && pointed_to >= 0)
        {
            world[pointed_to&0x1fffff] = 0; // remove block
            int xz = (pointed_to&0x1fffff)>>7;
            int y = pointed_to&127;
            if(y == maxHeight[xz])
            {
                while(y > 0 && world[128*xz+y] == 0)
                    y--;
                maxHeight[xz] = (byte)y;
            }
        }
        if((key == 112 || key == 461) && pointed_to >= 0 && world[pointed_to&0x1fffff] != 0)
        {
            int side = pointed_to >> 21;
            int x = (pointed_to >> 14) & 127;
            int z = (pointed_to >> 7) & 127;
            int y = pointed_to & 127;
            if(side == 0)
                y--;
            else if(side == 1)
                y++;
            else if(side == 2)
                x--;
            else if(side == 3)
                x++;
            else if(side == 4)
                z--;
            else
                z++;
            if(x >= 0 && x < 128 && y >= 0 && y < 128 && z >= 0 && z < 128 && world[16384*x+128*z+y] == 0)
                if(playerX < x - 0.3 || playerX > x + 1.3
                || playerY < y - 0.3 || playerY > y + 2.6
                || playerZ < z - 0.3 || playerZ > z + 1.3)
                {
                    world[16384*x+128*z+y] = (byte)129;
                    if(maxHeight[128*x+z] < y)
                        maxHeight[128*x+z] = (byte)y;
                }
        }
    }
    private void onkeyup(int key)
    {
        System.out.println("keyup "+key);
        if(key == 65 || key == 412)
            vel_x++;
        if(key == 87 || key == 425)
            vel_z--;
        if(key == 68 || key == 417)
            vel_x--;
        if(key == 83 || key == 424)
            vel_z++;
        /*if(key == 32 || key == 10)
            vel_y--;
        if(key == 16 || key == 461)
            vel_y++;*/
        if(key == 37)
            vel_yaw++;
        if(key == 38)
            vel_pitch--;
        if(key == 39)
            vel_yaw--;
        if(key == 40)
            vel_pitch++;
    }
    public void keyEvent(int key)
    {
        if(key > 0)
        {
            if(!keyStates[key])
                onkeydown(key);
            keyStates[key] = true;
        }
        else
        {
            if(keyStates[-key])
                onkeyup(-key);
            keyStates[-key] = false;
        }
    }
}
