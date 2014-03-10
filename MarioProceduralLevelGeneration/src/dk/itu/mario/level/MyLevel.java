package dk.itu.mario.level;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import dk.itu.mario.MarioInterface.Constraints;
import dk.itu.mario.MarioInterface.GamePlay;
import dk.itu.mario.MarioInterface.LevelInterface;
import dk.itu.mario.engine.sprites.SpriteTemplate;
import dk.itu.mario.engine.sprites.Enemy;

public class MyLevel extends Level
{
	// Store information about the level
	public int ENEMIES = 0; // the number of enemies the level contains
	public int BLOCKS_EMPTY = 0; // the number of empty blocks
	public int BLOCKS_COINS = 0; // the number of coin blocks
	public int BLOCKS_POWER = 0; // the number of power blocks
	public int COINS = 0; // These are the coins in boxes that Mario collect

	private int curFloorHeight;
	private boolean[] hillEdge;
	private List<Hill> hills;
	private int[] floorHeight;
	private int[] hillHeight;
	private boolean[] pad;
	private int[] debug;

	private static Random levelSeedRandom = new Random();
	public static long lastSeed;

	Random random;

	private int difficulty;
	private int type;
	private int pits;

	public MyLevel(int width, int height) {
		super(width, height);
		curFloorHeight = height;
	}

	public MyLevel(int width, int height, long seed, int difficulty, int type, GamePlay playerMetrics) {
		this(width, height);
		curFloorHeight = height;
		creat(seed, difficulty, type);
	}

	public void creat(long seed, int difficulty, int type) {
		// width = 128;
		hillEdge = new boolean[width];
		hills = new ArrayList<Hill>();
		floorHeight = new int[width];
		hillHeight = new int[width];
		Arrays.fill(hillHeight, height+1);
		pad = new boolean[width];
		debug = new int[width];

		this.type = type;
		this.difficulty = difficulty;

		lastSeed = seed;
		random = new Random(seed);

		// create the start location
		int length = 0;
		// length += buildStraight(0, width, true);
		length += buildStart(0, width);

		// create all of the medium sections
		/*while (length < width - 64) {
			// length += buildZone(length, width - length);
			// length += buildStraight(length, width-length, false);
			// length += buildStraight(length, width-length, false);
			// length += buildHillStraight(length, width-length);
			// length += buildJump(length, width-length);
			// length += buildTubes(length, width-length);
			// length += buildCannons(length, width-length);
			// length += buildFlat(length, width-length, true);

			length += buildHillStraight(length, width - length);
		}*/
		
		double terrainModifier = random.nextDouble();
		double pitModifier = random.nextDouble();
		double hillModifier = random.nextDouble();
		
		System.out.printf("Modifiers:\n----------------\nter:\t%f\npit:\t%f\nhill:\t%f\n\n", terrainModifier, pitModifier, hillModifier);
		
		buildTerrain(length, width-length-12, terrainModifier); // 12 = 8 for end + gap
		addPits(length, width-length-12, pitModifier);
		addHills(length, width-length-12, hillModifier);
		length += width-length-12;

		// set the end piece
		int floor = Math.min(floorHeight[length-1], height-1); //height - 1;// - random.nextInt(4);

		xExit = length + 8;
		yExit = floor;

		// fills the end piece
		for (int x = length; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (y >= floor) {
					setBlock(x, y, GROUND);
				}
				
				floorHeight[x] = floor;
			}
		}

		if (type == LevelInterface.TYPE_CASTLE || type == LevelInterface.TYPE_UNDERGROUND) {
			int ceiling = 0;
			int run = 0;
			for (int x = 0; x < width; x++) {
				if (run-- <= 0 && x > 4) {
					ceiling = random.nextInt(4);
					run = random.nextInt(4) + 4;
				}
				for (int y = 0; y < height; y++) {
					if ((x > 4 && y <= ceiling) || x < 1) {
						setBlock(x, y, GROUND);
					}
				}
			}
		}

		fixWalls();
		fixCorners();
		
		System.out.println(Arrays.toString(hillHeight));
	}

	/*
	 * Constructs the beginning of the level - a flat, undecorated piece of
	 * flooring.
	 */
	private int buildStart(int zoneStart, int maxLength) {
		int length = random.nextInt(10) + 2;

		if (length > maxLength)
			length = maxLength;

		int floor = height - random.nextInt(2) - 1;

		// runs from the specified x position to the length of the segment
		for (int x = zoneStart; x < zoneStart + length; x++) {
			for (int y = 0; y < height; y++) {
				if (y >= floor) {
					setBlock(x, y, GROUND);
					if (getBlock(x, y) == HILL_TOP_LEFT)
						setBlock(x, y - 2, HILL_TOP_LEFT_IN);
					if (getBlock(x, y) == HILL_TOP_RIGHT)
						setBlock(x, y - 2, HILL_TOP_RIGHT_IN);
				}
			}
			floorHeight[x] = floor;
		}

		return length;
	}
	
	/*
	 * Creates the base terrain for the level and modifies how often the floor
	 * height changes and by how much based on the provided modifier value.
	 */
	private int buildTerrain(int zoneStart, int maxLength, double modifier) {
		int length = maxLength;
		int maxHeight = height - 6;
		int minHeight = height - 1;
		
		// make terrain more difficult depending on modifier
		int heightVariance = (int)(Math.round(4*modifier));
		int changeProbability = (int)(Math.round(10*modifier)) + 1;
		
		int minPadLength = 7;
		int floor = floorHeight[zoneStart-1];
		int padSize = 0;
		
		// alter floor for the length of the segment
		for (int x = zoneStart; x < zoneStart + length; x++) {
			
			if (floor < maxHeight) { floor = maxHeight; }
			if (floor > minHeight) { floor = minHeight; }
			
			// mark where each "pad" starts for later use and debugging
			if (padSize == 0 && floor != floorHeight[x-1]) {
				pad[x] = true;
				debug[x] = 1;
			}
			
			for (int y = 0; y < height; y++) {
				if (y >= floor) {
					setBlock(x, y, GROUND);
					if (getBlock(x, y) == HILL_TOP_LEFT)
						setBlock(x, y - 2, HILL_TOP_LEFT_IN);
					if (getBlock(x, y) == HILL_TOP_RIGHT)
						setBlock(x, y - 2, HILL_TOP_RIGHT_IN);
				}
			}
			
			floorHeight[x] = floor;
			padSize++;
			
			// once minimum length reached, alter height by some chance
			if (padSize > minPadLength && random.nextInt(changeProbability) != 0) {
				floor += random.nextInt(heightVariance*2+1) - heightVariance;
				padSize = 0;
			}
		}

		return length;
	}
	
	/* 
	 * Scatters pits throughout the map. The higher the modifier, the more
	 * frequent and large the pits become. Pits will not be created if its
	 * edges are near an elevation change. This prevents unwanted graphical
	 * errors.
	 */
	private int addPits(int zoneStart, int maxLength, double modifier) {
		int length = maxLength;
		
		// ensure we don't get any divide-by-zero errors
		if (modifier <= 0) { modifier = 0.01; }
		
		int pitProbability = (int)(Math.round(3*(1.0/Math.pow(modifier,3)))) + 1;
		int pitWidth = (int)(Math.round(2*modifier)) + 2; // cannot exceed min pad length
		
		// find location to create pit
		for (int x = zoneStart; x < zoneStart + length; x++) {
			if (random.nextInt(pitProbability) == 0) {
				
				// avoid graphical issue with single ground blocks
				if (!pad[x] && !pad[x-1] && !pad[x+pitWidth+2] && !pad[x+pitWidth+3]) {
					
					// create pit
					for (int pit = x+1; pit < x+1+pitWidth; pit++) {
						for (int y = floorHeight[pit]; y <= height; y++) {
							setBlock(pit, y, (byte) 0); // empty block
						}
						
						floorHeight[pit] = height+1;
						pad[pit] = false;
						debug[pit] = 2;
					}
				}
				
				pits++; // keep track of these, just cuz
				x += pitWidth+1; // ensure no side-by-side pits
			}
		}
		
		return length;
	}
	
	private int addHills(int zoneStart, int maxLength, double modifier) {
		int length = maxLength;
		
		int hillAttempts = (int)Math.round(modifier*maxLength);
		int minWidth = 3;
		int maxWidth = 7;
		
		// randomly disperse hills around map based on modifier
		for (int att = 0; att < hillAttempts; att++) {
			int start = random.nextInt(maxLength) + zoneStart;
			
			List<Hill> possibleHills = new ArrayList<Hill>();
			
			// left side not on hill edge, elevation change, or near pad start
			if (!hillEdge[start] && !hillEdge[start-1] && floorHeight[start] == floorHeight[start-1] && !pad[start-1]) {
				
				// attempt all possible widths
				for (int width = minWidth; width <= maxWidth; width++) {
					
					boolean validLocation = true;
					int heightMod = 4;
					
					// right side valid
					if (start+width <= zoneStart+maxLength && !hillEdge[start+width-1] && !hillEdge[start+width]) {
						
						// ensure edges not on elevation change
						if (floorHeight[start+width-1] != floorHeight[start+width]) {
							validLocation = false;
						}
						
						// ensure not near pad start
						if (pad[start+width]) {
							validLocation = false;
						}
						
						// ensure not over a pit
						for (int x = start; x < start + width; x++) {
							if (floorHeight[x] == height+1) {
								validLocation = false;
								break;
							}
							
							// TODO adapt height based on nearby hills
							if (hillHeight[x] != height+1 && floorHeight[start]-heightMod >= hillHeight[x]) {
								heightMod = hillHeight[x]-3;
								if (floorHeight[start]-heightMod <= 0) {
									validLocation = false;
									break;
								}
							}
						}
						
					} else {
						validLocation = false;
					}
					
					if (validLocation) {
						possibleHills.add(new Hill(start, width, heightMod));
					}
				}
			}
			
			if (!possibleHills.isEmpty()) {
				// select hill at random
				Hill hill = possibleHills.get(random.nextInt(possibleHills.size()));
				int width = hill.width();
				int hillStart = hill.start();
				hills.add(hill);

				int height = floorHeight[hillStart]-hill.height();
				
				// construct hill
				hillEdge[hillStart] = true;
				hillEdge[hillStart + width] = true;
				for (int x = hillStart; x < hillStart + width; x++) {
					
					hillHeight[x] = height;
					for (int y = height; y < floorHeight[x]; y++) {
						int xx = 5;
						if (x == hillStart)
							xx = 4; // if on start edge draw edge block
						if (x == hillStart + width - 1)
							xx = 6; // if on end edge, draw edge block
						int yy = 9;
						if (y == height)
							yy = 8; // if on top draw top edge block

						if (getBlock(x, y) == 0 || getBlock(x, y) == (5 + 9 * 16)) {
							setBlock(x, y, (byte) (xx + yy * 16));
						} else {
							if (getBlock(x, y) == HILL_TOP_LEFT)
								setBlock(x, y, HILL_TOP_LEFT_IN);
							if (getBlock(x, y) == HILL_TOP_RIGHT)
								setBlock(x, y, HILL_TOP_RIGHT_IN);
						}
					}
				}
			}
		}
		
		return length;
	}
	
	/*
	 * Iteratively goes through the generated world and fixes a graphical issue
	 * where terrain corners in front of hills wouldn't be filled in.
	 */
	private void fixCorners() {
		
		for (int x = 0; x < width + 1; x++) {
			for (int y = 0; y < height + 1; y++) {
				
				// if corner in front of hill
				if (getBlock(x, y) == LEFT_UP_GRASS_EDGE || getBlock(x, y) == RIGHT_UP_GRASS_EDGE) {
					if(getBlock(x, y-1) == HILL_FILL || getBlock(x, y-1) == HILL_RIGHT || getBlock(x, y-1) == HILL_LEFT || getBlock(x, y-1) == HILL_TOP) {
						
						// replace with appropriate filled corner tile
						if (getBlock(x, y) == LEFT_UP_GRASS_EDGE) {
							setBlock(x, y, (byte)(0 + 11 * 16));
						} else {
							setBlock(x, y, (byte)(2 + 11 * 16));
						}
					}
				}
			}
		}
	}
	
	private int buildJump(int xo, int maxLength) {
		pits++;
		// jl: jump length
		// js: the number of blocks that are available at either side for free
		int js = random.nextInt(4) + 2;
		int jl = random.nextInt(2) + 2;
		int length = js * 2 + jl;

		boolean hasStairs = random.nextInt(3) == 0;

		int floor = height - 1 - random.nextInt(4);
		// run from the start x position, for the whole length
		for (int x = xo; x < xo + length; x++) {
			if (x < xo + js || x > xo + length - js - 1) {
				// run for all y's since we need to paint blocks upward
				for (int y = 0; y < height; y++) { // paint ground up until the floor
					if (y >= floor) {
						setBlock(x, y, GROUND);
					}
					// if it is above ground, start making stairs of rocks
					else if (hasStairs) { // LEFT SIDE
						if (x < xo + js) { // we need to max it out and level because it wont
											// paint ground correctly unless two bricks are side by side
							if (y >= floor - (x - xo) + 1) {
								setBlock(x, y, ROCK);
							}
						} else { // RIGHT SIDE
							if (y >= floor - ((xo + length) - x) + 2) {
								setBlock(x, y, ROCK);
							}
						}
					}
				}
			}
		}

		return length;
	}

	private int buildCannons(int xo, int maxLength) {
		int length = random.nextInt(10) + 2;
		if (length > maxLength)
			length = maxLength;

		int floor = height - 1 - random.nextInt(4);
		int xCannon = xo + 1 + random.nextInt(4);
		for (int x = xo; x < xo + length; x++) {
			if (x > xCannon) {
				xCannon += 2 + random.nextInt(4);
			}
			if (xCannon == xo + length - 1)
				xCannon += 10;
			int cannonHeight = floor - random.nextInt(4) - 1;

			for (int y = 0; y < height; y++) {
				if (y >= floor) {
					setBlock(x, y, GROUND);
				} else {
					if (x == xCannon && y >= cannonHeight) {
						if (y == cannonHeight) {
							setBlock(x, y, (byte) (14 + 0 * 16));
						} else if (y == cannonHeight + 1) {
							setBlock(x, y, (byte) (14 + 1 * 16));
						} else {
							setBlock(x, y, (byte) (14 + 2 * 16));
						}
					}
				}
			}
		}

		return length;
	}

	/*
	private int buildHillStraight(int zoneStart, int maxLength) {
		int length = random.nextInt(10) + 10;
		if (length > maxLength)
			length = maxLength;

		int floor = height - 1 - random.nextInt(4);
		for (int x = zoneStart; x < zoneStart + length; x++) {
			for (int y = 0; y < height; y++) {
				if (y >= floor) {
					setBlock(x, y, GROUND);
				}
			}
		}

		int hillHeight = floor;
		boolean keepGoing = true;
		boolean firstHill = true;

		// attempt to add as many hills as possible
		while (keepGoing) {
			hillHeight = hillHeight - 2 - random.nextInt(3);

			if (hillHeight <= 0) {
				keepGoing = false;

			} else {
				int minWidth = 3;
				int maxWidth = 7;

				List<Hill> possibleHills = new ArrayList<Hill>();

				// find all legal hills for the given height
				for (int width = minWidth; width <= maxWidth; width++) {
					for (int start = 1; start < length - width - 1; start++) {

						if ((firstHill || (getBlock(start + zoneStart + width, floor - 1) == 0 || getBlock(start
								+ zoneStart, floor - 1) == 0))
								&& !hillEdge[start + zoneStart]
								&& !hillEdge[start + zoneStart + width]
								&& !hillEdge[start + zoneStart - 1] && !hillEdge[start + zoneStart + width + 1]) {
							possibleHills.add(new Hill(start + zoneStart, width));
						}
					}
				}

				if (possibleHills.isEmpty()) {
					keepGoing = false;
				} else {
					// select hill at random
					Hill hill = possibleHills.get(random.nextInt(possibleHills.size()));
					int hillWidth = hill.width();
					int hillStart = hill.start();
					hills.add(hill);

					// construct hill
					hillEdge[hillStart] = true;
					hillEdge[hillStart + hillWidth] = true;
					for (int x = hillStart; x < hillStart + hillWidth; x++) {
						for (int y = hillHeight; y < floor; y++) {
							int xx = 5;
							if (x == hillStart)
								xx = 4; // if on start edge draw edge block
							if (x == hillStart + hillWidth - 1)
								xx = 6; // if on end edge, draw edge block
							int yy = 9;
							if (y == hillHeight)
								yy = 8; // if on top draw top edge block

							if (getBlock(x, y) == 0 || getBlock(x, y) == (5 + 9 * 16)) {
								setBlock(x, y, (byte) (xx + yy * 16));
							} else {
								if (getBlock(x, y) == HILL_TOP_LEFT)
									setBlock(x, y, HILL_TOP_LEFT_IN);
								if (getBlock(x, y) == HILL_TOP_RIGHT)
									setBlock(x, y, HILL_TOP_RIGHT_IN);
							}
						}
					}

					firstHill = false;
				}
			}
		}

		return length;
	}*/

	private class Hill
	{
		private int start, width, height;

		public Hill(int start, int width, int height) {
			this.start = start;
			this.width = width;
			this.height = height;
		}

		public int start() {
			return start;
		}

		public int width() {
			return width;
		}
		
		public int height() {
			return height;
		}
	}

	private void addEnemyLine(int x0, int x1, int y) {
		for (int x = x0; x < x1; x++) {
			if (random.nextInt(35) < difficulty + 1) {
				int type = random.nextInt(4);

				if (difficulty < 1) {
					type = Enemy.ENEMY_GOOMBA;
				} else if (difficulty < 3) {
					type = random.nextInt(3);
				}

				setSpriteTemplate(x, y, new SpriteTemplate(type, random.nextInt(35) < difficulty));
				ENEMIES++;
			}
		}
	}

	private int buildTubes(int zoneStart, int maxLength) {
		int length = random.nextInt(10) + 5;
		if (length > maxLength)
			length = maxLength;

		int floor = height - 1 - random.nextInt(4);
		int xTube = zoneStart + 1 + random.nextInt(4);
		int tubeHeight = floor - random.nextInt(2) - 2;

		for (int x = zoneStart; x < zoneStart + length; x++) {
			if (x > xTube + 1) {
				xTube += 3 + random.nextInt(4); // 3-6 spaces between tubes
				tubeHeight = floor - random.nextInt(2) - 2; // tubes 2-3 blocks high
			}
			if (xTube >= zoneStart + length - 2)
				xTube += 10;

			if (x == xTube && random.nextInt(11) < difficulty + 1) {
				setSpriteTemplate(x, tubeHeight, new SpriteTemplate(Enemy.ENEMY_FLOWER, false));
				ENEMIES++;
			}

			for (int y = 0; y < height; y++) {
				if (y >= floor) {
					setBlock(x, y, GROUND);
				} else {
					if ((x == xTube || x == xTube + 1) && y >= tubeHeight) {
						int xPic = 10 + x - xTube;

						if (y == tubeHeight) {
							// tube top
							setBlock(x, y, (byte) (xPic + 0 * 16));
						} else {
							// tube side
							setBlock(x, y, (byte) (xPic + 1 * 16));
						}
					}
				}
			}
		}

		return length;
	}

	private int buildStraight(int xo, int maxLength, boolean safe) {
		int length = random.nextInt(10) + 2;

		if (safe)
			length = 10 + random.nextInt(5);

		if (length > maxLength)
			length = maxLength;

		int floor = height - 1 - random.nextInt(4);

		// runs from the specified x position to the length of the segment
		for (int x = xo; x < xo + length; x++) {
			for (int y = 0; y < height; y++) {
				if (y >= floor) {
					setBlock(x, y, GROUND);
				}
			}
		}

		if (!safe) {
			if (length > 5) {
				decorate(xo, xo + length, floor);
			}
		}

		return length;
	}

	private void decorate(int xStart, int xLength, int floor) {
		// if its at the very top, just return
		if (floor < 1)
			return;

		// boolean coins = random.nextInt(3) == 0;
		boolean rocks = true;

		// add an enemy line above the box
		addEnemyLine(xStart + 1, xLength - 1, floor - 1);

		int s = random.nextInt(4);
		int e = random.nextInt(4);

		if (floor - 2 > 0) {
			if ((xLength - 1 - e) - (xStart + 1 + s) > 1) {
				for (int x = xStart + 1 + s; x < xLength - 1 - e; x++) {
					setBlock(x, floor - 2, COIN);
					COINS++;
				}
			}
		}

		s = random.nextInt(4);
		e = random.nextInt(4);

		// this fills the set of blocks and the hidden objects inside them
		if (floor - 4 > 0) {
			if ((xLength - 1 - e) - (xStart + 1 + s) > 2) {
				for (int x = xStart + 1 + s; x < xLength - 1 - e; x++) {
					if (rocks) {
						if (x != xStart + 1 && x != xLength - 2 && random.nextInt(3) == 0) {
							if (random.nextInt(4) == 0) {
								setBlock(x, floor - 4, BLOCK_POWERUP);
								BLOCKS_POWER++;
							} else { // the fills a block with a hidden coin
								setBlock(x, floor - 4, BLOCK_COIN);
								BLOCKS_COINS++;
							}
						} else if (random.nextInt(4) == 0) {
							if (random.nextInt(4) == 0) {
								setBlock(x, floor - 4, (byte) (2 + 1 * 16));
							} else {
								setBlock(x, floor - 4, (byte) (1 + 1 * 16));
							}
						} else {
							setBlock(x, floor - 4, BLOCK_EMPTY);
							BLOCKS_EMPTY++;
						}
					}
				}
			}
		}
	}

	private void fixWalls() {
		boolean[][] blockMap = new boolean[width + 1][height + 1];

		for (int x = 0; x < width + 1; x++) {
			for (int y = 0; y < height + 1; y++) {
				int blocks = 0;
				for (int xx = x - 1; xx < x + 1; xx++) {
					for (int yy = y - 1; yy < y + 1; yy++) {
						if (getBlockCapped(xx, yy) == GROUND) {
							blocks++;
						}
					}
				}
				blockMap[x][y] = blocks == 4;
			}
		}
		blockify(this, blockMap, width + 1, height + 1);
	}

	private void blockify(Level level, boolean[][] blocks, int width, int height) {
		int to = 0;
		if (type == LevelInterface.TYPE_CASTLE) {
			to = 4 * 2;
		} else if (type == LevelInterface.TYPE_UNDERGROUND) {
			to = 4 * 3;
		}

		boolean[][] b = new boolean[2][2];

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				for (int xx = x; xx <= x + 1; xx++) {
					for (int yy = y; yy <= y + 1; yy++) {
						int _xx = xx;
						int _yy = yy;
						if (_xx < 0)
							_xx = 0;
						if (_yy < 0)
							_yy = 0;
						if (_xx > width - 1)
							_xx = width - 1;
						if (_yy > height - 1)
							_yy = height - 1;
						b[xx - x][yy - y] = blocks[_xx][_yy];
					}
				}

				if (b[0][0] == b[1][0] && b[0][1] == b[1][1]) {
					if (b[0][0] == b[0][1]) {
						if (b[0][0]) {
							level.setBlock(x, y, (byte) (1 + 9 * 16 + to));
						} else {
							// KEEP OLD BLOCK!
						}
					} else {
						if (b[0][0]) {
							// down grass top?
							level.setBlock(x, y, (byte) (1 + 10 * 16 + to));
						} else {
							// up grass top
							level.setBlock(x, y, (byte) (1 + 8 * 16 + to));
						}
					}
				} else if (b[0][0] == b[0][1] && b[1][0] == b[1][1]) {
					if (b[0][0]) {
						// right grass top
						level.setBlock(x, y, (byte) (2 + 9 * 16 + to));
					} else {
						// left grass top
						level.setBlock(x, y, (byte) (0 + 9 * 16 + to));
					}
				} else if (b[0][0] == b[1][1] && b[0][1] == b[1][0]) {
					level.setBlock(x, y, (byte) (1 + 9 * 16 + to));
				} else if (b[0][0] == b[1][0]) {
					if (b[0][0]) {
						if (b[0][1]) {
							level.setBlock(x, y, (byte) (3 + 10 * 16 + to));
						} else {
							level.setBlock(x, y, (byte) (3 + 11 * 16 + to));
						}
					} else {
						if (b[0][1]) {
							// right up grass top
							level.setBlock(x, y, (byte) (2 + 8 * 16 + to));
						} else {
							// left up grass top
							level.setBlock(x, y, (byte) (0 + 8 * 16 + to));
						}
					}
				} else if (b[0][1] == b[1][1]) {
					if (b[0][1]) {
						if (b[0][0]) {
							// left pocket grass
							level.setBlock(x, y, (byte) (3 + 9 * 16 + to));
						} else {
							// right pocket grass
							level.setBlock(x, y, (byte) (3 + 8 * 16 + to));
						}
					} else {
						if (b[0][0]) {
							level.setBlock(x, y, (byte) (2 + 10 * 16 + to));
						} else {
							level.setBlock(x, y, (byte) (0 + 10 * 16 + to));
						}
					}
				} else {
					level.setBlock(x, y, (byte) (0 + 1 * 16 + to));
				}
			}
		}
	}

	public RandomLevel clone() throws CloneNotSupportedException {

		RandomLevel clone = new RandomLevel(width, height);

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
