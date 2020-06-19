package org.homebrew;

import java.util.Random;

public class GameMain
{
    final static int[] block_textures = {
        7, 7, 7 /* lava */,
        7, 7, 7 /* lava */,
        0, 1, 2 /* grass */,
        4, 3, 4 /* wood */,
        5, 5, 5 /* leaves */,
	2, 2, 2 /* dirt */,
	6, 6, 6 /* stone*/,
    };
    final static int[] bboxes = {
        0, 52429, 0, 65536, 0, 65536, // lava top
        0, 65536, 0, 65536, 0, 65536, // lava full block
        0, 65536, 0, 65536, 0, 65536, // default
    };
    final static int[] side_masks = {0x948, 0xdda, /*0x990*/0x816, /*0xbd9*/0xa5f, /*0x4c8*/0x05a, /*0xdec*/0x97e};
    private byte[] world;
    private byte[] world0;
    private byte[] maxHeight;
    private int[] aux;
    private long[] aux2;
    private int[] genParams;
    private double[] playerMeta;
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
    private byte[] preflight_bl;
    private int[] segtree;
    public int[] matrix_fp;
    private int skip;
    private boolean[] keyStates;
    private int vel_x;
    private double vel_x_stick;
    private double vel_y;
    private int vel_z;
    private double vel_z_stick;
    private int vel_yaw;
    private int vel_pitch;
    private int deltaX;
    private int deltaY;
    private long prev_time;
    private int[] texture_atlas;
    private int pointed_to;
    private Inventory inv;
    private GUI gui;
    public GameMain(String level_save_path)
    {
        LevelSave.setLevelSavePath(level_save_path);
        world = new byte[128*128*128];
        world0 = new byte[128*128*128];
        maxHeight = new byte[128*128];
        aux = new int[699050];
        aux2 = new long[1024];
        genParams = new int[2];
        playerMeta = new double[5];
        genHomeScreen();
        preflight = new byte[(128*128*128)/8];
        preflight_bl = new byte[(128*128*128)/8];
        segtree = aux;
        matrix_fp = new int[18];
        skip = 256;
        keyStates = new boolean[1024];
        prev_time = System.currentTimeMillis();
        texture_atlas = TextureAtlas.atlas;
        pointed_to = -1;
        inv = new Inventory();
        showGUI(new StartMenu(this));
    }
    public void loadFrom(final String filename)
    {
        final GameMain self = this;
        showGUI(new GeneratingGUI());
        (new Thread()
        {
            public void run()
            {
                LevelSave.loadGame(world, world0, aux, genParams, playerMeta, filename, (GeneratingGUI)gui);
                for(int i = 0; i < 128*128; i++)
                    maxHeight[i] = 0;
                for(int i = 0; i < 128*128; i++)
                    for(int j = 0; j < 128; j++)
                        if(world[128*i+j] != 0)
                            maxHeight[i] = (byte)j;
                playerX = playerMeta[0];
                playerY = playerMeta[1];
                playerZ = playerMeta[2];
                playerYaw = playerMeta[3];
                playerYaw_cos = Math.cos(playerYaw);
                playerYaw_sin = Math.sin(playerYaw);
                playerPitch = playerMeta[4];
                playerPitch_cos = Math.cos(playerPitch);
                playerPitch_sin = Math.sin(playerPitch);
                playerX_fp = (int)(playerX*65536);
                playerY_fp = (int)(playerY*65536);
                playerZ_fp = (int)(playerZ*65536);
                playerPitch_cos_fp = (int)(playerPitch_cos*65536);
                playerPitch_sin_fp = (int)(playerPitch_sin*65536);
                playerYaw_cos_fp = (int)(playerYaw_cos*65536);
                playerYaw_sin_fp = (int)(playerYaw_sin*65536);
                synchronized(self)
                {
                    showGUI(null);
                }
            }
        }).start();
    }
    public void saveTo(String filename)
    {
        playerMeta[0] = playerX;
        playerMeta[1] = playerY;
        playerMeta[2] = playerZ;
        playerMeta[3] = playerYaw;
        playerMeta[4] = playerPitch;
        LevelSave.saveGame(world, world0, genParams, playerMeta, filename);
    }
    public void genWorld(final int seed)
    {
        final GameMain self = this;
        showGUI(new GeneratingGUI());
        (new Thread()
        {
            public void run()
            {
                genWorld(seed, false);
                synchronized(self)
                {
                    showGUI(null);
                }
            }
        }).start();
    }
    private void genWorld(int seed, boolean lite)
    {
        genParams[0] = seed;
        genParams[1] = MapGen.LATEST_MAP_VERSION;
        for(int i = 0; i < 128*128*128; i++)
            world[i] = 0;
        MapGen.generate(world, world0, aux, seed, MapGen.LATEST_MAP_VERSION, lite, lite?null:(GeneratingGUI)this.gui);
        for(int i = 0; i < 128*128*128; i++)
            world0[i] = world[i];
        for(int i = 0; i < 128*128; i++)
            maxHeight[i] = 0;
        for(int i = 0; i < 128*128; i++)
            for(int j = 0; j < 128; j++)
                if(world[128*i+j] != 0)
                    maxHeight[i] = (byte)j;
        playerX = playerZ = 64.5;
        playerY = maxHeight[64*129] + 2.6;
        playerYaw = playerYaw_sin = playerPitch = playerPitch_sin = 0;
        playerYaw_cos = playerPitch_cos = 1;
        vel_x_stick = vel_z_stick = vel_y = vel_x = vel_z = 0;
	playerX_fp = (int)(playerX*65536);
	playerY_fp = (int)(playerY*65536);
	playerZ_fp = (int)(playerZ*65536);
	playerPitch_cos_fp = (int)(playerPitch_cos*65536);
	playerPitch_sin_fp = (int)(playerPitch_sin*65536);
	playerYaw_cos_fp = (int)(playerYaw_cos*65536);
	playerYaw_sin_fp = (int)(playerYaw_sin*65536);
    }
    public void genHomeScreen()
    {
        genWorld(179, true);
    }
    public void render(int[] buffer)
    {
        if(gui == null || !gui.doStopEngine())
        {
            for(int i = 0; i < preflight.length; i++)
                preflight[i] = preflight_bl[i] = 0;
            int px = (int)playerX;
            int py = (int)playerY;
            int pz = (int)playerZ;
            bfs_preflight(preflight, world, px, py, pz);
            for(int i = 0; i < 640*480; i++)
                buffer[i] = 0;
            for(int i = 0; i < 699050; i++)
                segtree[i] = 0;
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
            flush(buffer, segtree, 0, 0, 512, 0, 512);
            flush(buffer, segtree, 1, 512, 1024, 0, 512);
            int prev_pos = -1;
            int prev_side = -1;
            int tex_start = -1;
            if((buffer[640*240+320] & 0xfe000000) == 0xb2000000)
                pointed_to = buffer[640*240+320] & 0x00ffffff;
            else
                pointed_to = -1;
            for(int i = 0; i < 640*480; i++)
                if(buffer[i] == 0)
                    buffer[i] = -1;
                else if((buffer[i] & 0xfe000000) == 0xb2000000)
                {
                    int side = (buffer[i] & 0xe00000) >> 21;
                    int pos = buffer[i] & 0x1fffff;
                    int block = (255&(int)world[pos]);
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
                        int tex_id = block_textures[3*(block-126)+side2];
                        tex_start = 4096*(tex_id/16)+16*(tex_id%16);
                        get_plane_coords(pos >> 14, pos & 127, (pos >> 7) & 127, side);
                        matrix_invert();
                    }
                    /*int bx_fp = ((pos >> 14) << 16) - playerX_fp;
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
                    int bbox_offset;
                    if(block >= 128)
                        bbox_offset = 12;
                    else
                        bbox_offset = 6 * (block - 126);
                    bz_fp += bboxes[bbox_offset+side];
                    long tx_fp, ty_fp;
                    if(vz_fp == 0) // wtf??
                        tx_fp = ty_fp = -1;
                    else
                    {
                        long coef_fp = (((long)bz_fp)<<16)/vz_fp;
                        tx_fp = ((vx_fp*coef_fp)>>16)-bx_fp;
                        ty_fp = ((vy_fp*coef_fp)>>16)-by_fp;
                    }*/
                    int vx_fp = ((i%640-320) << 16)/250;
                    int vy_fp = ((240-i/640) << 16)/250;
                    int vz_fp = 65536;
                    int tz_fp = (int)((vx_fp*(long)matrix_fp[0]+vy_fp*(long)matrix_fp[3]+vz_fp*(long)matrix_fp[6])>>16);
                    int tx_fp = (int)((vx_fp*(long)matrix_fp[1]+vy_fp*(long)matrix_fp[4]+vz_fp*(long)matrix_fp[7])>>16);
                    int ty_fp = (int)((vx_fp*(long)matrix_fp[2]+vy_fp*(long)matrix_fp[5]+vz_fp*(long)matrix_fp[8])>>16);
                    if(tz_fp == 0)
                    {
                        buffer[i] = 0xffff0000;
                        continue;
                    }
                    tx_fp = (int)((((long)tx_fp)<<16)/tz_fp);
                    ty_fp = (int)((((long)ty_fp)<<16)/tz_fp);
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
                    /*if(side >= 2)
                        ty_i = 15 - ty_i;*/
                    buffer[i] = texture_atlas[tex_start+256*ty_i+tx_i];
                }
            playerPhysics();
        }
        else
            prev_time = System.currentTimeMillis(); // otherwise it would be treated as a lag
        if(gui == null)
        {
            // crosshair
            for(int i = 0; i < 6; i++)
            {
                // invert colors
                buffer[640*239+320+i] ^= 0xffffff;
                buffer[640*240+320+i] ^= 0xffffff;
                buffer[640*239+319-i] ^= 0xffffff;
                buffer[640*240+319-i] ^= 0xffffff;
                buffer[640*(240+i)+320] ^= 0xffffff;
                buffer[640*(240+i)+319] ^= 0xffffff;
                buffer[640*(239-i)+320] ^= 0xffffff;
                buffer[640*(239-i)+319] ^= 0xffffff;
            }
            inv.renderHotbar(buffer);
        }
        else
            gui.render(buffer);
    }
    private void playerPhysics()
    {
        long cur_time = System.currentTimeMillis();
        boolean do_rot = (gui != null && gui.doRotateCamera());
        for(long tick = prev_time; tick < cur_time; tick++)
        {
            double playerX_prev = playerX;
            double playerY_prev = playerY;
            double playerZ_prev = playerZ;
            playerYaw += vel_yaw / 250.0 + deltaX / 250.0;
            if(do_rot)
                playerYaw += 0.0003;
            deltaX = 0;
            if(playerYaw > 2*Math.PI)
                playerYaw -= 2*Math.PI;
            else if(playerYaw < 0)
                playerYaw += 2*Math.PI;
            playerYaw_cos = Math.cos(playerYaw);
            playerYaw_sin = Math.sin(playerYaw);
            playerPitch += vel_pitch / 250.0 - deltaY / 250.0;
            if(do_rot)
            {
                if(playerPitch > 0.001)
                    playerPitch -= 0.001;
                else if(playerPitch < -0.001)
                    playerPitch += 0.001;
                else
                    playerPitch = 0;
            }
            deltaY = 0;
            if(playerPitch > Math.PI/2)
                playerPitch = Math.PI/2;
            else if(playerPitch < -Math.PI/2)
                playerPitch = -Math.PI/2;
            playerPitch_cos = Math.cos(playerPitch);
            playerPitch_sin = Math.sin(playerPitch);
            playerX += (vel_x + vel_x_stick) * playerYaw_cos / 250.0 + (vel_z + vel_z_stick) * playerYaw_sin / 250.0;
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
            playerZ += (vel_z + vel_z_stick) * playerYaw_cos / 250.0 - (vel_x + vel_x_stick) * playerYaw_sin / 250.0;
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
                            if((keyStates[32] || keyStates[10]) && gui == null)
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
        if(world[16384*(playerX_fp>>16)+128*(playerZ_fp>>16)+(playerY_fp>>16)] >= 0)
            skip = (world[16384*(playerX_fp>>16)+128*(playerZ_fp>>16)+(playerY_fp>>16)]&254);
    }
    private void bfs_preflight(byte[] preflight, byte[] world, int px, int py, int pz)
    {
        int pos = 16384*px+128*pz+py;
        int idx = pos >> 3;
        int mask = 1 << (pos&7);
        preflight[idx] |= mask;
        for(int i = px; i >= 0; i--)
        {
            for(int j = pz; j >= 0; j--)
            {
                for(int k = 127; k >= 0; k--)
                    if((preflight[(16384*i+128*j+k)>>3]&(1<<(k&7)))!=0)
                        bfs_step(preflight, world, i, k, j, px, py, pz);
                for(int k = 0; k < 128; k++)
                    if((preflight[(16384*i+128*j+k)>>3]&(1<<(k&7)))!=0)
                        bfs_step(preflight, world, i, k, j, px, py, pz);
            }
            for(int j = pz + 1; j < 128; j++)
            {
                for(int k = 127; k >= 0; k--)
                    if((preflight[(16384*i+128*j+k)>>3]&(1<<(k&7)))!=0)
                        bfs_step(preflight, world, i, k, j, px, py, pz);
                for(int k = 0; k < 128; k++)
                    if((preflight[(16384*i+128*j+k)>>3]&(1<<(k&7)))!=0)
                        bfs_step(preflight, world, i, k, j, px, py, pz);
            }
        }
        for(int i = px + 1; i < 128; i++)
        {
            for(int j = pz; j >= 0; j--)
            {
                for(int k = 127; k >= 0; k--)
                    if((preflight[(16384*i+128*j+k)>>3]&(1<<(k&7)))!=0)
                        bfs_step(preflight, world, i, k, j, px, py, pz);
                for(int k = 0; k < 128; k++)
                    if((preflight[(16384*i+128*j+k)>>3]&(1<<(k&7)))!=0)
                        bfs_step(preflight, world, i, k, j, px, py, pz);
            }
            for(int j = pz + 1; j < 128; j++)
            {
                for(int k = 127; k >= 0; k--)
                    if((preflight[(16384*i+128*j+k)>>3]&(1<<(k&7)))!=0)
                        bfs_step(preflight, world, i, k, j, px, py, pz);
                for(int k = 0; k < 128; k++)
                    if((preflight[(16384*i+128*j+k)>>3]&(1<<(k&7)))!=0)
                        bfs_step(preflight, world, i, k, j, px, py, pz);
            }
        }
    }
    private void bfs_step(byte[] preflight, byte[] world, int x, int y, int z, int px, int py, int pz)
    {
        int pos = x*16384+z*128+y;
        int idx = pos >> 3;
        int mask = 1 << (pos & 7);
        if((preflight[idx] & mask) == 0)
            return;
        boolean onGround = y > maxHeight[pos>>7];
        boolean skyHit = true;
        for(int xi = (x==0?x:x-1); xi <= x + 1 && xi < 128; xi++)
            for(int zi = (z==0?z:z-1); zi <= z + 1 && zi < 128; zi++)
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
            zz_fp += (int)(((((onGround?128:y+1)<<16)-playerY_fp)*c2_fp)>>16);
        else
            zz_fp += (int)((((y<<16)-playerY_fp)*c2_fp)>>16);
        if(c3_fp > 0)
            zz_fp += (int)(((((z+1)<<16)-playerZ_fp)*c3_fp)>>16);
        else
            zz_fp += (int)((((z<<16)-playerZ_fp)*c3_fp)>>16);
        if(zz_fp < 0) // block is fully invisible
        {
            preflight_bl[idx] |= mask;
            return;
        }
        if((point_out_of_screen(x, y, z)
          & point_out_of_screen(x+1, y, z)
          & point_out_of_screen(x, (onGround?128:y+1), z)
          & point_out_of_screen(x+1, (onGround?128:y+1), z)
          & point_out_of_screen(x, y, z+1)
          & point_out_of_screen(x+1, y, z+1)
          & point_out_of_screen(x, (onGround?128:y+1), z+1)
          & point_out_of_screen(x+1, (onGround?128:y+1), z+1)) != 0) // block is in front of the player but outside of the frame
        {
            preflight_bl[idx] |= mask;
            return;
        }
        if((world[pos] & 128) != 0) // can't see through solid blocks
        {
            if(y > maxHeight[pos>>7])
                throw new RuntimeException("maxHeight fucked up");
            return;
        }
        if(skyHit)
        {
            if(mask == 1)
                preflight[idx-1] |= 128;
            else
                preflight[idx] |= mask >> 1;
            return;
        }
        if(x != 127 && x >= px)
            preflight[idx+2048] |= mask;
        if(x != 0 && x <= px)
            preflight[idx-2048] |= mask;
        if(y != 127 && (y >= py || onGround) && !skyHit)
        {
            if(mask == 128)
                preflight[idx+1] |= 1;
            else
                preflight[idx] |= mask << 1;
        }
        if(y != 0 && y <= py)
        {
            if(mask == 1)
                preflight[idx-1] |= 128;
            else
                preflight[idx] |= mask >> 1;
        }
        if(z != 127 && z >= pz)
            preflight[idx+16] |= mask;
        if(z != 0 && z <= pz)
            preflight[idx-16] |= mask;
    }
    public void matrix_invert()
    {
        for(int i = 0; i < 3; i++)
        {
            matrix_fp[i + 3] -= matrix_fp[i];
            matrix_fp[i + 6] = matrix_fp[i + 9] - matrix_fp[i];
        }
        int det_fp = 0;
        int idx = 0;
        for(int i = 0; i < 3; i++)
            for(int j = 0; j < 3; j++)
                if(i != j)
                {
                    int k = 3 - i - j;
                    int sign = 1-((idx ^ idx << 1) & 2);
                    det_fp += (int)(sign*((((matrix_fp[i] * (long)matrix_fp[3+j])>>16)*(long)matrix_fp[6+k])>>16));
                    idx++;
                }
        for(int i = 0; i < 9; i++)
            matrix_fp[i + 9] = matrix_fp[i];
        matrix_fp[0] = (int)((matrix_fp[13]*(long)matrix_fp[17]-matrix_fp[14]*(long)matrix_fp[16])/det_fp);
        matrix_fp[3] = (int)((matrix_fp[14]*(long)matrix_fp[15]-matrix_fp[12]*(long)matrix_fp[17])/det_fp);
        matrix_fp[6] = (int)((matrix_fp[12]*(long)matrix_fp[16]-matrix_fp[13]*(long)matrix_fp[15])/det_fp);
        matrix_fp[1] = (int)((matrix_fp[11]*(long)matrix_fp[16]-matrix_fp[10]*(long)matrix_fp[17])/det_fp);
        matrix_fp[4] = (int)((matrix_fp[ 9]*(long)matrix_fp[17]-matrix_fp[11]*(long)matrix_fp[15])/det_fp);
        matrix_fp[7] = (int)((matrix_fp[10]*(long)matrix_fp[15]-matrix_fp[ 9]*(long)matrix_fp[16])/det_fp);
        matrix_fp[2] = (int)((matrix_fp[10]*(long)matrix_fp[14]-matrix_fp[11]*(long)matrix_fp[13])/det_fp);
        matrix_fp[5] = (int)((matrix_fp[11]*(long)matrix_fp[12]-matrix_fp[ 9]*(long)matrix_fp[14])/det_fp);
        matrix_fp[8] = (int)((matrix_fp[ 9]*(long)matrix_fp[13]-matrix_fp[10]*(long)matrix_fp[12])/det_fp);
        /*for(int i = 0; i < 18; i++)
            System.out.print(matrix_fp[i]+" ");
        System.out.println("");*/
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
        if(z_fp < 0)
        {
            int mask = 0;
            if(x_fp < 0)
                mask |= 1;
            if(x_fp > 0)
                mask |= 2;
            if(y_fp > 0)
                mask |= 4;
            if(y_fp < 0)
                mask |= 8;
            return mask;
        }
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
        if(z_fp < 0)
            mask = (mask&5)<<1|(mask&10)>>1;
        return mask;
    }
    private void render_block(int[] buffer, int x, int y, int z)
    {
        if(segtree[0] != 0 && segtree[0] != 1)
            return;
        int pos = x*16384+z*128+y;
        int idx = pos >> 3;
        int mask = 1 << (pos & 7);
        if((preflight[idx] & mask) == 0 || (preflight_bl[idx] & mask) != 0)
            return;
        int block = 255&(int)world[pos];
        if(block == 0 /* air */ || (block ^ skip) < 2)
            return;
        int bbox_offset;
        if(block >= 128)
            bbox_offset = 12;
        else
            bbox_offset = 6 * (block - 126);
        int y0 = (y<<16)+bboxes[bbox_offset];
        int y1 = (y<<16)+bboxes[bbox_offset+1];
        int x0 = (x<<16)+bboxes[bbox_offset+2];
        int x1 = (x<<16)+bboxes[bbox_offset+3];
        int z0 = (z<<16)+bboxes[bbox_offset+4];
        int z1 = (z<<16)+bboxes[bbox_offset+5];
        if(block < 128)
        {
            if(playerY_fp < y0 && (y0 != (y<<16) || world[pos-1] >= 0))
                render_plane(buffer, x, y, z, 0, 0xb2000000|pos, false);
            if(playerY_fp > y1 && (y1 != ((y+1)<<16) || world[pos+1] >= 0))
                render_plane(buffer, x, y, z, 1, 0xb2200000|pos, false);
            if(playerX_fp < x0 && (x0 != (x<<16) || world[pos-16384] >= 0))
                render_plane(buffer, x, y, z, 2, 0xb2400000|pos, false);
            if(playerX_fp > x1 && (x1 != ((x+1)<<16) || world[pos+16384] >= 0))
                render_plane(buffer, x, y, z, 3, 0xb2600000|pos, false);
            if(playerZ_fp < z0 && (z0 != (z<<16) || world[pos-128] >= 0))
                render_plane(buffer, x, y, z, 4, 0xb2800000|pos, false);
            if(playerZ_fp > z1 && (z1 != ((z+1)<<16) || world[pos+128] >= 0))
                render_plane(buffer, x, y, z, 5, 0xb2a00000|pos, false);
        }
        else
        {
            if(playerY_fp < y0 && world[pos-1] >= 0)
                render_plane_legacy(buffer, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1, 0xb3000000|pos, false);
            if(playerY_fp > y1 && world[pos+1] >= 0)
                render_plane_legacy(buffer, x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1, 0xb3200000|pos, false);
            if(playerX_fp < x0 && world[pos-16384] >= 0)
                render_plane_legacy(buffer, x0, y0, z0, x0, y1, z0, x0, y1, z1, x0, y0, z1, 0xb3400000|pos, false);
            if(playerX_fp > x1 && world[pos+16384] >= 0)
                render_plane_legacy(buffer, x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1, 0xb3600000|pos, false);
            if(playerZ_fp < z0 && world[pos-128] >= 0)
                render_plane_legacy(buffer, x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0, 0xb3800000|pos, false);
            if(playerZ_fp > z1 && world[pos+128] >= 0)
                render_plane_legacy(buffer, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, 0xb3a00000|pos, false);
        }
    }
    private void get_plane_coords(int x, int y, int z, int side)
    {
        int block = 255&(int)world[x*16384+z*128+y];
        int bbox_offset;
        if(block >= 128)
            bbox_offset = 12;
        else
            bbox_offset = 6 * (block - 126);
        int y0 = (y<<16)+bboxes[bbox_offset];
        int y1 = (y<<16)+bboxes[bbox_offset+1];
        int x0 = (x<<16)+bboxes[bbox_offset+2];
        int x1 = (x<<16)+bboxes[bbox_offset+3];
        int z0 = (z<<16)+bboxes[bbox_offset+4];
        int z1 = (z<<16)+bboxes[bbox_offset+5];
        int x1_fp, y1_fp, z1_fp, x2_fp, y2_fp, z2_fp, x3_fp, y3_fp, z3_fp, x4_fp, y4_fp, z4_fp;
        int mask = side_masks[side];
        if((mask & 1) != 0)
            x1_fp = x1;
        else
            x1_fp = x0;
        if((mask & 2) != 0)
            y1_fp = y1;
        else
            y1_fp = y0;
        if((mask & 4) != 0)
            z1_fp = z1;
        else
            z1_fp = z0;
        if((mask & 8) != 0)
            x2_fp = x1;
        else
            x2_fp = x0;
        if((mask & 16) != 0)
            y2_fp = y1;
        else
            y2_fp = y0;
        if((mask & 32) != 0)
            z2_fp = z1;
        else
            z2_fp = z0;
        if((mask & 64) != 0)
            x3_fp = x1;
        else
            x3_fp = x0;
        if((mask & 128) != 0)
            y3_fp = y1;
        else
            y3_fp = y0;
        if((mask & 256) != 0)
            z3_fp = z1;
        else
            z3_fp = z0;
        if((mask & 512) != 0)
            x4_fp = x1;
        else
            x4_fp = x0;
        if((mask & 1024) != 0)
            y4_fp = y1;
        else
            y4_fp = y0;
        if((mask & 2048) != 0)
            z4_fp = z1;
        else
            z4_fp = z0;
        x1_fp -= playerX_fp;
        x2_fp -= playerX_fp;
        x3_fp -= playerX_fp;
        x4_fp -= playerX_fp;
        y1_fp -= playerY_fp;
        y2_fp -= playerY_fp;
        y3_fp -= playerY_fp;
        y4_fp -= playerY_fp;
        z1_fp -= playerZ_fp;
        z2_fp -= playerZ_fp;
        z3_fp -= playerZ_fp;
        z4_fp -= playerZ_fp;
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
        matrix_fp[0] = x1_fp;
        matrix_fp[1] = y1_fp;
        matrix_fp[2] = z1_fp;
        matrix_fp[3] = x2_fp;
        matrix_fp[4] = y2_fp;
        matrix_fp[5] = z2_fp;
        matrix_fp[6] = x3_fp;
        matrix_fp[7] = y3_fp;
        matrix_fp[8] = z3_fp;
        matrix_fp[9] = x4_fp;
        matrix_fp[10] = y4_fp;
        matrix_fp[11] = z4_fp;
    }
    private void render_plane(int[] buffer, int x, int y, int z, int side, int color, boolean outline)
    {
        get_plane_coords(x, y, z, side);
        int x1_fp = matrix_fp[0];
        int y1_fp = matrix_fp[1];
        int z1_fp = matrix_fp[2];
        int x2_fp = matrix_fp[3];
        int y2_fp = matrix_fp[4];
        int z2_fp = matrix_fp[5];
        int x3_fp = matrix_fp[6];
        int y3_fp = matrix_fp[7];
        int z3_fp = matrix_fp[8];
        int x4_fp = matrix_fp[9];
        int y4_fp = matrix_fp[10];
        int z4_fp = matrix_fp[11];
        if(outline)
        {
            draw_line(buffer, x1_fp, y1_fp, z1_fp, x2_fp, y2_fp, z2_fp);
            draw_line(buffer, x2_fp, y2_fp, z2_fp, x3_fp, y3_fp, z3_fp);
            draw_line(buffer, x3_fp, y3_fp, z3_fp, x4_fp, y4_fp, z4_fp);
            draw_line(buffer, x4_fp, y4_fp, z4_fp, x1_fp, y1_fp, z1_fp);
        }
        render3(segtree, aux2, x1_fp, y1_fp, z1_fp, x2_fp, y2_fp, z2_fp, x3_fp, y3_fp, z3_fp, color);
        render3(segtree, aux2, x1_fp, y1_fp, z1_fp, x4_fp, y4_fp, z4_fp, x3_fp, y3_fp, z3_fp, color | 0x1000000);
    }
    private void render_plane_legacy(int[] buffer, int x1_fp, int y1_fp, int z1_fp, int x2_fp, int y2_fp, int z2_fp, int x3_fp, int y3_fp, int z3_fp, int x4_fp, int y4_fp, int z4_fp, int color, boolean outline)
    {
        x1_fp -= playerX_fp;
        x2_fp -= playerX_fp;
        x3_fp -= playerX_fp;
        x4_fp -= playerX_fp;
        y1_fp -= playerY_fp;
        y2_fp -= playerY_fp;
        y3_fp -= playerY_fp;
        y4_fp -= playerY_fp;
        z1_fp -= playerZ_fp;
        z2_fp -= playerZ_fp;
        z3_fp -= playerZ_fp;
        z4_fp -= playerZ_fp;
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
        render3(segtree, aux2, x1_fp, y1_fp, z1_fp, x2_fp, y2_fp, z2_fp, x3_fp, y3_fp, z3_fp, color);
        render3(segtree, aux2, x1_fp, y1_fp, z1_fp, x4_fp, y4_fp, z4_fp, x3_fp, y3_fp, z3_fp, color);
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
    private static void render3(int[] segtree, long[] aux2, int x1_fp, int y1_fp, int z1_fp, int x2_fp, int y2_fp, int z2_fp, int x3_fp, int y3_fp, int z3_fp, int color)
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
            render3_raw(segtree, aux2, x4_fp, y4_fp, z4_fp, x5_fp, y5_fp, z5_fp, x3_fp, y3_fp, z3_fp, color);
            render3_raw(segtree, aux2, x4_fp, y4_fp, z4_fp, x2_fp, y2_fp, z2_fp, x3_fp, y3_fp, z3_fp, color);
            return;
        }
        render3_raw(segtree, aux2, x1_fp, y1_fp, z1_fp, x2_fp, y2_fp, z2_fp, x3_fp, y3_fp, z3_fp, color);
    }
    private static int draw0(int[] segtree, int idx, int l, int r, int up, int dn, long a, long b, long c, long d, long e, long f, long g, long h, long i, int color)
    {
        if(segtree[idx] != 0 && segtree[idx] != 1)
            return 0;
        int m1 = (l+r)/2;
        int m2 = (up+dn)/2;
        boolean ul1 = (a*l+b*up+c) > 0;
        boolean ul2 = (d*l+e*up+f) > 0;
        boolean ul3 = (g*l+h*up+i) > 0;
        boolean ur1 = (a*r+b*up+c) > 0;
        boolean ur2 = (d*r+e*up+f) > 0;
        boolean ur3 = (g*r+h*up+i) > 0;
        boolean dl1 = (a*l+b*dn+c) > 0;
        boolean dl2 = (d*l+e*dn+f) > 0;
        boolean dl3 = (g*l+h*dn+i) > 0;
        boolean dr1 = (a*r+b*dn+c) > 0;
        boolean dr2 = (d*r+e*dn+f) > 0;
        boolean dr3 = (g*r+h*dn+i) > 0;
        if(ul1 && ul2 && ul3 && ur1 && ur2 && ur3 && dl1 && dl2 && dl3 && dr1 && dr2 && dr3 && segtree[idx] == 0)
        {
            segtree[idx] = color;
            return 1;
        }
        else if((!ul1 && !ur1 && !dl1 && !dr1) || (!ul2 && !ur2 && !dl2 && !dr2) || (!ul3 && !ur3 && !dl3 && !dr3))
        {
            return 0;
        }
        else if(m1 != l)
        {
            if((draw0(segtree, 4*idx+2, l, m1, up, m2, a, b, c, d, e, f, g, h, i, color) | draw0(segtree, 4*idx+3, m1, r, up, m2, a, b, c, d, e, f, g, h, i, color) | draw0(segtree, 4*idx+4, l, m1, m2, dn, a, b, c, d, e, f, g, h, i, color) | draw0(segtree, 4*idx+5, m1, r, m2, dn, a, b, c, d, e, f, g, h, i, color)) != 0)
            {
                if(segtree[4*idx+2] != 0 && segtree[4*idx+2] != 1 && segtree[4*idx+3] != 0 && segtree[4*idx+3] != 1 && segtree[4*idx+4] != 0 && segtree[4*idx+4] != 1 && segtree[4*idx+5] != 0 && segtree[4*idx+5] != 1)
                    segtree[idx] = 2;
                else
                    segtree[idx] = 1;
                return 1;
            }
        }
        else if((ul1 && ul2 && ul3) || (ur1 && ur2 && ur3) || (dl1 && dl2 && dl3) || (dr1 && dr2 && dr3))
            segtree[idx] = color;
        return 0;
    }
    private static void draw1(int[] segtree, long a, long b, long c, long d, long e, long f, long g, long h, long i, int color)
    {
        draw0(segtree, 0, 0, 512, 0, 512, a, b, c, d, e, f, g, h, i, color);
        draw0(segtree, 1, 512, 1024, 0, 512, a, b, c, d, e, f, g, h, i, color);
    }
    private static void get_abc(int x1, int y1, int x2, int y2, int x3, int y3, long[] ans)
    {
        long a = y2 - y1;
        long b = x1 - x2;
        long c = -a*x1-b*y1;
        if(a*x3+b*y3+c < 0)
        {
            a = -a;
            b = -b;
            c = -c;
        }
        ans[0] = a;
        ans[1] = b;
        ans[2] = c;
    }
    private static void draw(int[] segtree, long[] aux2, int x1, int y1, int x2, int y2, int x3, int y3, int color)
    {
        get_abc(x1, y1, x2, y2, x3, y3, aux2);
        long a1 = aux2[0], b1 = aux2[1], c1 = aux2[2];
        get_abc(x2, y2, x3, y3, x1, y1, aux2);
        long a2 = aux2[0], b2 = aux2[1], c2 = aux2[2];
        get_abc(x3, y3, x1, y1, x2, y2, aux2);
        long a3 = aux2[0], b3 = aux2[1], c3 = aux2[2];
        draw1(segtree, a1, b1, c1, a2, b2, c2, a3, b3, c3, color);
    }
    private static void flush(int[] framebuffer, int[] segtree, int idx, int l, int r, int up, int dn)
    {
        if(up >= 480 || l >= 640)
            return;
        int m1 = (l+r)/2;
        int m2 = (up+dn)/2;
        if(segtree[idx] != 0 && segtree[idx] != 1 && segtree[idx] != 2)
        {
            int c = segtree[idx];
            segtree[idx] = 0;
            for(int y = up; y < dn && y < 480; y++)
                for(int x = l; x < r && x < 640; x++)
                    framebuffer[640*y+x] = c;
        }
        else if(l != m1)
        {
            flush(framebuffer, segtree, 4*idx+2, l, m1, up, m2);
            flush(framebuffer, segtree, 4*idx+3, m1, r, up, m2);
            flush(framebuffer, segtree, 4*idx+4, l, m1, m2, dn);
            flush(framebuffer, segtree, 4*idx+5, m1, r, m2, dn);
        }
        else
            framebuffer[640*up+l] = 0;
    }
    private static void render3_raw(int[] segtree, long[] aux2, int x1_fp, int y1_fp, int z1_fp, int x2_fp, int y2_fp, int z2_fp, int x3_fp, int y3_fp, int z3_fp, int color)
    {
        long x1_fpl = 320*65536 + (x1_fp*(250l*65536l)) / z1_fp;
        long y1_fpl = 240*65536 - (y1_fp*(250l*65536l)) / z1_fp;
        long x2_fpl = 320*65536 + (x2_fp*(250l*65536l)) / z2_fp;
        long y2_fpl = 240*65536 - (y2_fp*(250l*65536l)) / z2_fp;
        long x3_fpl = 320*65536 + (x3_fp*(250l*65536l)) / z3_fp;
        long y3_fpl = 240*65536 - (y3_fp*(250l*65536l)) / z3_fp;
        int x1 = (int)(x1_fpl >> 16);
        int y1 = (int)(y1_fpl >> 16);
        int x2 = (int)(x2_fpl >> 16);
        int y2 = (int)(y2_fpl >> 16);
        int x3 = (int)(x3_fpl >> 16);
        int y3 = (int)(y3_fpl >> 16);
        draw(segtree, aux2, x1, y1, x2, y2, x3, y3, color);
    }
    private void onkeydown(int key)
    {
        System.out.println("keydown "+key);
        if(gui != null)
        {
            gui.onkeydown(key);
            return;
        }
        boolean magic = keyStates[424] && keyStates[425] && keyStates[412] && keyStates[417];
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
        if(key == 27 || ((key == 19 || key == 145) && magic))
            showGUI(new PauseMenu(this));
        if((key == 113 || ((key == 19 || key == 415) && !magic) || key == 1001) && pointed_to >= 0)
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
        if((key == 112 || key == 461 || key == 1003) && pointed_to >= 0 && world[pointed_to&0x1fffff] != 0)
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
            int item = inv.getHotbarItem();
            if(item > 0 && item < 256)
                if(x >= 0 && x < 128 && y >= 0 && y < 128 && z >= 0 && z < 128 && world[16384*x+128*z+y] == 0)
                    if(playerX <= x - 0.3 || playerX >= x + 1.3
                    || playerY <= y - 0.3 || playerY >= y + 2.6
                    || playerZ <= z - 0.3 || playerZ >= z + 1.3)
                    {
                        world[16384*x+128*z+y] = (byte)item;
                        if(maxHeight[128*x+z] < y)
                            maxHeight[128*x+z] = (byte)y;
                    }
        }
        if(key >= 49 && key <= 57) // digits
            inv.setHotbarSlot(key - 49);
    }
    private void onkeyup(int key)
    {
        System.out.println("keyup "+key);
        if(gui != null)
        {
            gui.onkeyup(key);
            return;
        }
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
    public void mouseMove(int dx, int dy)
    {
        if(gui != null)
        {
            gui.mousemove(dx, dy);
            return;
        }
        deltaX += dx;
        deltaY += dy;
    }
    public void rightStick(double vx, double vz)
    {
        if(gui == null)
        {
            vel_x_stick = vx;
            vel_z_stick = vz;
        }
    }
    public void mouseEvent(int button)
    {
        if(button > 0)
            keyEvent(button+1000);
        else
            keyEvent(button-1000);
    }
    public void mouseWheel(int clicks)
    {
        System.out.println("wheel "+clicks);
    }
    public boolean doCaptureMouse()
    {
        return (gui==null?true:gui.doCaptureMouse());
    }
    public void showGUI(GUI gui)
    {
        this.gui = gui;
    }
}
