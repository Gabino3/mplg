package dk.itu.mario.level;

import java.util.Random;

import dk.itu.mario.MarioInterface.Constraints;
import dk.itu.mario.MarioInterface.GamePlay;
import dk.itu.mario.MarioInterface.LevelInterface;
import dk.itu.mario.engine.sprites.SpriteTemplate;
import dk.itu.mario.engine.sprites.Enemy;


public class MyLevel extends Level{
	//Store information about the level
	 public   int ENEMIES = 0; //the number of enemies the level contains
	 public   int BLOCKS_EMPTY = 0; // the number of empty blocks
	 public   int BLOCKS_COINS = 0; // the number of coin blocks
	 public   int BLOCKS_POWER = 0; // the number of power blocks
	 public   int COINS = 0; //These are the coins in boxes that Mario collect
	 private int curFloorHeight;

 
	private static Random levelSeedRandom = new Random();
	    public static long lastSeed;

	    Random random;

  
	    private int difficulty;
	    private int type;
		private int gaps;
		
		public MyLevel(int width, int height)
	    {
			super(width, height);
			curFloorHeight = height;
	    }


		public MyLevel(int width, int height, long seed, int difficulty, int type, GamePlay playerMetrics)
	    {
	        this(width, height);
	        curFloorHeight = height;
	        creat(seed, difficulty, type);
	    }
		
		public int randomInt(int minInt, int maxInt){
			return minInt + random.nextInt(maxInt+1-minInt);
		}

	    public void creat(long seed, int difficulty, int type)
	    {
	    	width = 128;
	    	
	        this.type = type;
	        this.difficulty = difficulty;

	        lastSeed = seed;
	        random = new Random(seed);

	        //create the start location
	        int length = 0;
	        //length += buildStraight(0, width, true);
	        length += buildFlat(0, width, true);
	        
	        //create all of the medium sections
	        while (length < width - 64)
	        {
	            //length += buildZone(length, width - length);
				//length += buildStraight(length, width-length, false);
				//length += buildStraight(length, width-length, false);
				//length += buildHillStraight(length, width-length);
				//length += buildJump(length, width-length);
				//length += buildTubes(length, width-length);
				//length += buildCannons(length, width-length);
	        	//length += buildFlat(length, width-length, true);

	        	length += buildTubes(length, width-length);

	        }
	        

	        //set the end piece
	        int floor = height - 1;// - random.nextInt(4);

	        xExit = length + 8;
	        yExit = floor;

	        // fills the end piece
	        for (int x = length; x < width; x++)
	        {
	            for (int y = 0; y < height; y++)
	            {
	                if (y >= floor)
	                {
	                    setBlock(x, y, GROUND);
	                }
	            }
	        }
	        
	        if (type == LevelInterface.TYPE_CASTLE || type == LevelInterface.TYPE_UNDERGROUND)
	        {
	            int ceiling = 0;
	            int run = 0;
	            for (int x = 0; x < width; x++)
	            {
	                if (run-- <= 0 && x > 4)
	                {
	                    ceiling = randomInt(0,3);//random.nextInt(4);
	                    run = randomInt(4,7);//random.nextInt(4) + 4;
	                }
	                for (int y = 0; y < height; y++)
	                {
	                    if ((x > 4 && y <= ceiling) || x < 1)
	                    {
	                        setBlock(x, y, GROUND);
	                    }
	                }
	            }
	        }
	        
	        fixWalls();
	    }


	    @SuppressWarnings("unused")
		private int buildGap(int zoneStart, int maxLength, int difficulty)
	    {	gaps++;
	        int rocksWidth = randomInt(1,5);//difficulty could add more rocks
	        int gapWidth = randomInt(2,3);//difficulty could increase gap width
	        int length = rocksWidth * 2 + gapWidth;
	        int gapStart = zoneStart + rocksWidth;
	        int gapEnd = zoneStart + length - rocksWidth - 1;

	        boolean hasStairs = randomInt(0,2) == 0;//difficulty could increase chance of stairs

	        int floor = height - 1 ;//- randomInt(0,3);
	      //run from the start x position, for the whole length
	        for (int x = zoneStart; x < zoneStart + length; x++)
	        {
	            if (x < gapStart || x > gapEnd)
	            {
	            	//run for all y's since we need to paint blocks upward
	                for (int y = 0; y < height; y++)
	                {	//paint ground up until the floor
	                    if (y >= floor)
	                    {
	                        setBlock(x, y, GROUND);
	                    }
	                    //if it is above ground, start making stairs of rocks
	                    else if (hasStairs)
	                    {	//LEFT SIDE
	                        if (x < gapStart)
	                        { //we need to max it out and level because it wont
	                          //paint ground correctly unless two bricks are side by side
	                            if (y >= floor - (x - zoneStart) + 1)
	                            {
	                                setBlock(x, y, ROCK);
	                            }
	                        }
	                        else
	                        { //RIGHT SIDE
	                            if (y >= floor - ((zoneStart + length) - x) + 2)
	                            {
	                                setBlock(x, y, ROCK);
	                            }
	                        }
	                    }
	                }
	            }
	        }

	        return length;
	    }

	    private int buildCannons(int xo, int maxLength)
	    {
	        int length = random.nextInt(10) + 2;
	        if (length > maxLength) length = maxLength;

	        int floor = height - 1 - random.nextInt(4);
	        int xCannon = xo + 1 + random.nextInt(4);
	        for (int x = xo; x < xo + length; x++)
	        {
	            if (x > xCannon)
	            {
	                xCannon += 2 + random.nextInt(4);
	            }
	            if (xCannon == xo + length - 1) xCannon += 10;
	            int cannonHeight = floor - random.nextInt(4) - 1;

	            for (int y = 0; y < height; y++)
	            {
	                if (y >= floor)
	                {
	                    setBlock(x, y, GROUND);
	                }
	                else
	                {
	                    if (x == xCannon && y >= cannonHeight)
	                    {
	                        if (y == cannonHeight)
	                        {
	                            setBlock(x, y, (byte) (14 + 0 * 16));
	                        }
	                        else if (y == cannonHeight + 1)
	                        {
	                            setBlock(x, y, (byte) (14 + 1 * 16));
	                        }
	                        else
	                        {
	                            setBlock(x, y, (byte) (14 + 2 * 16));
	                        }
	                    }
	                }
	            }
	        }

	        return length;
	    }

	    private int buildHillStraight(int zoneStart, int maxLength)
	    {
	        int length = random.nextInt(10) + 10;
	        if (length > maxLength) length = maxLength;

	        int floor = height - 1 - random.nextInt(4);
	        for (int x = zoneStart; x < zoneStart + length; x++)
	        {
	            for (int y = 0; y < height; y++)
	            {
	                if (y >= floor)
	                {
	                    setBlock(x, y, GROUND);
	                }
	            }
	        }

	        addEnemyLine(zoneStart + 1, zoneStart + length - 1, floor - 1);

	        int hillHeight = floor;

	        boolean keepGoing = true;

	        boolean[] occupied = new boolean[length];
	        while (keepGoing)
	        {
	            hillHeight = hillHeight - 2 - random.nextInt(3);

	            if (hillHeight <= 0)
	            {
	                keepGoing = false;
	            }
	            else
	            {
	                int hillWidth = random.nextInt(5) + 3;
	                int hillStart = random.nextInt(length - hillWidth - 2) + zoneStart + 1;

	                if (occupied[hillStart - zoneStart] || occupied[hillStart - zoneStart + hillWidth] || occupied[hillStart - zoneStart - 1] || occupied[hillStart - zoneStart + hillWidth + 1])
	                {
	                    keepGoing = false;
	                }
	                else
	                {
	                    occupied[hillStart - zoneStart] = true;
	                    occupied[hillStart - zoneStart + hillWidth] = true;
	                    addEnemyLine(hillStart, hillStart + hillWidth, hillHeight - 1);
	                    if (random.nextInt(4) == 0)
	                    {
	                        decorate(hillStart - 1, hillStart + hillWidth + 1, hillHeight);
	                        keepGoing = false;
	                    }
	                    for (int x = hillStart; x < hillStart + hillWidth; x++)
	                    {
	                        for (int y = hillHeight; y < floor; y++)
	                        {
	                            int xx = 5;
	                            if (x == hillStart) xx = 4;//if on start edge draw edge block
	                            if (x == hillStart + hillWidth - 1) xx = 6;//if on end edge, draw edge block
	                            int yy = 9;
	                            if (y == hillHeight) yy = 8;//if on top draw top edge block

	                            if (getBlock(x, y) == 0)
	                            {
	                                setBlock(x, y, (byte) (xx + yy * 16));
	                            }
	                            else
	                            {
	                                if (getBlock(x, y) == HILL_TOP_LEFT) setBlock(x, y, HILL_TOP_LEFT_IN);
	                                if (getBlock(x, y) == HILL_TOP_RIGHT) setBlock(x, y, HILL_TOP_RIGHT_IN);
	                            }
	                        }
	                    }
	                }
	            }
	        }

	        return length;
	    }

	    private void addEnemyLine(int x0, int x1, int y)
	    {
	        for (int x = x0; x < x1; x++)
	        {
	            if (random.nextInt(35) < difficulty + 1)
	            {
	                int type = random.nextInt(4);

	                if (difficulty < 1)
	                {
	                    type = Enemy.ENEMY_GOOMBA;
	                }
	                else if (difficulty < 3)
	                {
	                    type = random.nextInt(3);
	                }

	                setSpriteTemplate(x, y, new SpriteTemplate(type, random.nextInt(35) < difficulty));
	                ENEMIES++;
	            }
	        }
	    }

	    private int buildTubes(int zoneStart, int maxLength)
	    {
	        int length = random.nextInt(10) + 5;
	        if (length > maxLength) length = maxLength;

	        int floor = height - 1 - random.nextInt(4);
	        int xTube = zoneStart + 1 + random.nextInt(4);
	        int tubeHeight = floor - random.nextInt(2) - 2;
	        
	        for (int x = zoneStart; x < zoneStart + length; x++)
	        {
	            if (x > xTube + 1)
	            {
	                xTube += 3 + random.nextInt(4); // 3-6 spaces between tubes
	                tubeHeight = floor - random.nextInt(2) - 2; // tubes 2-3 blocks high
	            }
	            if (xTube >= zoneStart + length - 2) xTube += 10;

	            if (x == xTube && random.nextInt(11) < difficulty + 1)
	            {
	                setSpriteTemplate(x, tubeHeight, new SpriteTemplate(Enemy.ENEMY_FLOWER, false));
	                ENEMIES++;
	            }

	            for (int y = 0; y < height; y++)
	            {
	                if (y >= floor)
	                {
	                    setBlock(x, y, GROUND);
	                }
	                else
	                {
	                    if ((x == xTube || x == xTube + 1) && y >= tubeHeight)
	                    {
	                        int xPic = 10 + x - xTube;

	                        if (y == tubeHeight)
	                        {
	                        	//tube top
	                            setBlock(x, y, (byte) (xPic + 0 * 16));
	                        }
	                        else
	                        {
	                        	//tube side
	                            setBlock(x, y, (byte) (xPic + 1 * 16));
	                        }
	                    }
	                }
	            }
	        }

	        return length;
	    }

	    private int buildStraight(int xo, int maxLength, boolean safe)
	    {
	        int length = random.nextInt(10) + 2;

	        if (safe)
	        	length = 10 + random.nextInt(5);

	        if (length > maxLength)
	        	length = maxLength;

	        int floor = height - 1 - random.nextInt(4);

	        //runs from the specified x position to the length of the segment
	        for (int x = xo; x < xo + length; x++)
	        {
	            for (int y = 0; y < height; y++)
	            {
	                if (y >= floor)
	                {
	                    setBlock(x, y, GROUND);
	                }
	            }
	        }

	        if (!safe)
	        {
	            if (length > 5)
	            {
	                decorate(xo, xo + length, floor);
	            }
	        }

	        return length;
	    }
	    
	    private int buildFlat(int xo, int maxLength, boolean safe) {
	    	//int length = random.nextInt(10) + 2;
	    	int length = 2;
	    	
	        if (length > maxLength)
	        	length = maxLength;

	        int floor = curFloorHeight += ((random.nextInt(2) == 0) ? 1 : -1);
	        
	        if (floor < 2) {
	        	curFloorHeight = floor = 2;
	        }
	        if (floor > height-1) {
	        	curFloorHeight = floor = height-1;
	        }
	        
	        //runs from the specified x position to the length of the segment
	        for (int x = xo; x < xo + length; x++)
	        {
	            for (int y = 0; y < height; y++)
	            {
	                if (y >= floor)
	                {
	                    setBlock(x, y, GROUND);
	                    if (getBlock(x, y) == HILL_TOP_LEFT) setBlock(x, y-2, HILL_TOP_LEFT_IN);
                        if (getBlock(x, y) == HILL_TOP_RIGHT) setBlock(x, y-2, HILL_TOP_RIGHT_IN);
	                }
	            }
	        }

	        if (!safe)
	        {
            	type = 0;
            	
            	for (int x = xo; x < xo + length; x++) {
	    			setSpriteTemplate(x, floor-1, new SpriteTemplate(type, false));
	    			ENEMIES++;
            	}
	        }

	        return length;
	    }

	    private void decorate(int xStart, int xLength, int floor)
	    {
	    	//if its at the very top, just return
	        if (floor < 1)
	        	return;

	        //        boolean coins = random.nextInt(3) == 0;
	        boolean rocks = true;

	        //add an enemy line above the box
	        addEnemyLine(xStart + 1, xLength - 1, floor - 1);

	        int s = random.nextInt(4);
	        int e = random.nextInt(4);

	        if (floor - 2 > 0){
	            if ((xLength - 1 - e) - (xStart + 1 + s) > 1){
	                for(int x = xStart + 1 + s; x < xLength - 1 - e; x++){
	                    setBlock(x, floor - 2, COIN);
	                    COINS++;
	                }
	            }
	        }

	        s = random.nextInt(4);
	        e = random.nextInt(4);
	        
	        //this fills the set of blocks and the hidden objects inside them
	        if (floor - 4 > 0)
	        {
	            if ((xLength - 1 - e) - (xStart + 1 + s) > 2)
	            {
	                for (int x = xStart + 1 + s; x < xLength - 1 - e; x++)
	                {
	                    if (rocks)
	                    {
	                        if (x != xStart + 1 && x != xLength - 2 && random.nextInt(3) == 0)
	                        {
	                            if (random.nextInt(4) == 0)
	                            {
	                                setBlock(x, floor - 4, BLOCK_POWERUP);
	                                BLOCKS_POWER++;
	                            }
	                            else
	                            {	//the fills a block with a hidden coin
	                                setBlock(x, floor - 4, BLOCK_COIN);
	                                BLOCKS_COINS++;
	                            }
	                        }
	                        else if (random.nextInt(4) == 0)
	                        {
	                            if (random.nextInt(4) == 0)
	                            {
	                                setBlock(x, floor - 4, (byte) (2 + 1 * 16));
	                            }
	                            else
	                            {
	                                setBlock(x, floor - 4, (byte) (1 + 1 * 16));
	                            }
	                        }
	                        else
	                        {
	                            setBlock(x, floor - 4, BLOCK_EMPTY);
	                            BLOCKS_EMPTY++;
	                        }
	                    }
	                }
	            }
	        }
	    }

	    private void fixWalls()
	    {
	        boolean[][] blockMap = new boolean[width + 1][height + 1];

	        for (int x = 0; x < width + 1; x++)
	        {
	            for (int y = 0; y < height + 1; y++)
	            {
	                int blocks = 0;
	                for (int xx = x - 1; xx < x + 1; xx++)
	                {
	                    for (int yy = y - 1; yy < y + 1; yy++)
	                    {
	                        if (getBlockCapped(xx, yy) == GROUND){
	                        	blocks++;
	                        }
	                    }
	                }
	                blockMap[x][y] = blocks == 4;
	            }
	        }
	        blockify(this, blockMap, width + 1, height + 1);
	    }

	    private void blockify(Level level, boolean[][] blocks, int width, int height){
	        int to = 0;
	        if (type == LevelInterface.TYPE_CASTLE)
	        {
	            to = 4 * 2;
	        }
	        else if (type == LevelInterface.TYPE_UNDERGROUND)
	        {
	            to = 4 * 3;
	        }

	        boolean[][] b = new boolean[2][2];

	        for (int x = 0; x < width; x++)
	        {
	            for (int y = 0; y < height; y++)
	            {
	                for (int xx = x; xx <= x + 1; xx++)
	                {
	                    for (int yy = y; yy <= y + 1; yy++)
	                    {
	                        int _xx = xx;
	                        int _yy = yy;
	                        if (_xx < 0) _xx = 0;
	                        if (_yy < 0) _yy = 0;
	                        if (_xx > width - 1) _xx = width - 1;
	                        if (_yy > height - 1) _yy = height - 1;
	                        b[xx - x][yy - y] = blocks[_xx][_yy];
	                    }
	                }

	                if (b[0][0] == b[1][0] && b[0][1] == b[1][1])
	                {
	                    if (b[0][0] == b[0][1])
	                    {
	                        if (b[0][0])
	                        {
	                            level.setBlock(x, y, (byte) (1 + 9 * 16 + to));
	                        }
	                        else
	                        {
	                            // KEEP OLD BLOCK!
	                        }
	                    }
	                    else
	                    {
	                        if (b[0][0])
	                        {
	                        	//down grass top?
	                            level.setBlock(x, y, (byte) (1 + 10 * 16 + to));
	                        }
	                        else
	                        {
	                        	//up grass top
	                            level.setBlock(x, y, (byte) (1 + 8 * 16 + to));
	                        }
	                    }
	                }
	                else if (b[0][0] == b[0][1] && b[1][0] == b[1][1])
	                {
	                    if (b[0][0])
	                    {
	                    	//right grass top
	                        level.setBlock(x, y, (byte) (2 + 9 * 16 + to));
	                    }
	                    else
	                    {
	                    	//left grass top
	                        level.setBlock(x, y, (byte) (0 + 9 * 16 + to));
	                    }
	                }
	                else if (b[0][0] == b[1][1] && b[0][1] == b[1][0])
	                {
	                    level.setBlock(x, y, (byte) (1 + 9 * 16 + to));
	                }
	                else if (b[0][0] == b[1][0])
	                {
	                    if (b[0][0])
	                    {
	                        if (b[0][1])
	                        {
	                            level.setBlock(x, y, (byte) (3 + 10 * 16 + to));
	                        }
	                        else
	                        {
	                            level.setBlock(x, y, (byte) (3 + 11 * 16 + to));
	                        }
	                    }
	                    else
	                    {
	                        if (b[0][1])
	                        {
	                        	//right up grass top
	                            level.setBlock(x, y, (byte) (2 + 8 * 16 + to));
	                        }
	                        else
	                        {
	                        	//left up grass top
	                            level.setBlock(x, y, (byte) (0 + 8 * 16 + to));
	                        }
	                    }
	                }
	                else if (b[0][1] == b[1][1])
	                {
	                    if (b[0][1])
	                    {
	                        if (b[0][0])
	                        {
	                        	//left pocket grass
	                            level.setBlock(x, y, (byte) (3 + 9 * 16 + to));
	                        }
	                        else
	                        {
	                        	//right pocket grass
	                            level.setBlock(x, y, (byte) (3 + 8 * 16 + to));
	                        }
	                    }
	                    else
	                    {
	                        if (b[0][0])
	                        {
	                            level.setBlock(x, y, (byte) (2 + 10 * 16 + to));
	                        }
	                        else
	                        {
	                            level.setBlock(x, y, (byte) (0 + 10 * 16 + to));
	                        }
	                    }
	                }
	                else
	                {
	                    level.setBlock(x, y, (byte) (0 + 1 * 16 + to));
	                }
	            }
	        }
	    }
	    
	    public RandomLevel clone() throws CloneNotSupportedException {

	    	RandomLevel clone=new RandomLevel(width, height);

	    	clone.xExit = xExit;
	    	clone.yExit = yExit;
	    	byte[][] map = getMap();
	    	SpriteTemplate[][] st = getSpriteTemplate();
	    	
	    	for (int i = 0; i < map.length; i++)
	    		for (int j = 0; j < map[i].length; j++) {
	    			clone.setBlock(i, j, map[i][j]);
	    			clone.setSpriteTemplate(i, j, st[i][j]);
	    	}
	    	clone.BLOCKS_COINS = BLOCKS_COINS;
	    	clone.BLOCKS_EMPTY = BLOCKS_EMPTY;
	    	clone.BLOCKS_POWER = BLOCKS_POWER;
	    	clone.ENEMIES = ENEMIES;
	    	clone.COINS = COINS;
	    	
	        return clone;

	      }


}
