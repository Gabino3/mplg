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
	//
	// Store information about the level
	public int ENEMIES = 0; // the number of enemies the level contains
	public int BLOCKS_EMPTY = 0; // the number of empty blocks
	public int BLOCKS_COINS = 0; // the number of coin blocks
	public int BLOCKS_POWER = 0; // the number of power blocks
	public int COINS = 0; // These are the coins in boxes that Mario collect
	
	private List<GameElement> hills;
	private List<GameElement> tubes;
	private int[] floorHeight;
	private int[] peak;
	//private boolean[] pad;
	private int[] debug;
	
	private final int EMPTY = 0;
	private final int VISUAL = 1;
	private final int BLOCKABLE = 2;
	private final int SURFACE = 3;
	private int[][] blocks;

	private static Random levelSeedRandom = new Random();
	public static long lastSeed;

	Random random;

	private int difficulty;
	private int type;
	private int pits;

	public MyLevel(int width, int height) {
		super(width, height);
	}

	public MyLevel(int width, int height, long seed, int difficulty, int type, GamePlay playerMetrics) {
		this(width, height);
		creat(seed, difficulty, type);
	}

	public void creat(long seed, int difficulty, int type) {
		// width = 128;
		hills = new ArrayList<GameElement>();
		tubes = new ArrayList<GameElement>();
		floorHeight = new int[width];
		peak = new int[width];
		Arrays.fill(peak, height+1);
		debug = new int[width];

		blocks = new int[height][width];
		
		this.type = type;
		this.difficulty = difficulty;

		lastSeed = seed;
		random = new Random(seed);

		// create the start location
		int length = 0;
		length += buildStart(0, width);
		
		double terrainModifier = random.nextDouble();
		double pitModifier = random.nextDouble();
		double hillModifier = random.nextDouble();
		double tubeModifier = 0;//random.nextDouble();
		
		System.out.printf("Modifiers:\n----------------\nter:\t%f\npit:\t%f\nhill:\t%f\ntube:\t%f\n\n", terrainModifier, pitModifier, hillModifier, tubeModifier);
		
		buildTerrain(length, width-length-12, terrainModifier); // 12 = 8 for end + gap
		addPits(length, width-length-12, pitModifier);
		addHills(length, width-length-12, hillModifier);
		addTubes(length, width-length-12, tubeModifier);
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
		
		System.out.println(Arrays.toString(floorHeight));
		System.out.println(Arrays.toString(peak));
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
			peak[x] = floor;
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
				//pad[x] = true;
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
			peak[x] = floor;
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
				if (!nearElevationChange(x, true) && !nearElevationChange(x-1, true) && !nearElevationChange(x+pitWidth, false) && !nearElevationChange(x+pitWidth+1, false)) {
					
					// create pit
					for (int pit = x+1; pit <= x+pitWidth; pit++) {
						for (int y = floorHeight[pit]; y <= height; y++) {
							setBlock(pit, y, (byte) 0); // empty block
						}
						
						floorHeight[pit] = height+1;
						peak[pit] = height+1;
						//pad[pit] = false;
						debug[pit] = 2;
					}
				}
				
				pits++; // keep track of these, just cuz
				x += pitWidth+1; // ensure no side-by-side pits
			}
		}
		
		return length;
	}
	
	/*
	 * Scatters hills throughout the map. Hills cannot reside next to other
	 * hills or elevation changes, in pits, or within other hills. If a hill is
	 * lower than surrounding hills, it's height is adjusted to appropriately
	 * tower over the others, creating aesthetically pleasing mountain ranges
	 * that are fully traversable.
	 */
	private int addHills(int zoneStart, int maxLength, double modifier) {
		int length = maxLength;
		int zoneEnd = zoneStart + maxLength;
		
		int hillAttempts = (int)(Math.round(modifier*maxLength/3)); // TODO make maxLength-maxWidth instead of checking zoneEnd later
		int minWidth = 4;
		int maxWidth = 6;
		
		for (int att = 0; att < hillAttempts; att++) {
			int hillLeft = random.nextInt(maxLength) + zoneStart;
			
			List<GameElement> possibleHills = new ArrayList<GameElement>();
			
			// ensure valid left edge
			if (!nearPit(hillLeft) && !nearHillEdge(hillLeft, true) && !nearElevationChange(hillLeft, true)) {
			
				// test all possible hill widths
				for (int hillWidth = minWidth; hillWidth <= maxWidth; hillWidth++) {
					
					boolean validHill = true;
					int hillRight = hillLeft + hillWidth - 1;
					int hillHeight = height + 1;
					
					// ensure valid right edge
					if (nearPit(hillRight) || nearHillEdge(hillRight, false) || nearElevationChange(hillRight, false)
						|| hillRight >= zoneEnd) {
						validHill = false;
					}
					
					for (int x = hillLeft; x <= hillRight; x++) {
						// make sure hill doesn't rest in a pit
						if (isPit(x)) {
							validHill = false;
							break;
						}
						
						// make sure the hill is higher than surrounding hills
						if (hillHeight >= peak[x] - 1) {
							hillHeight = peak[x] - (random.nextInt(3) + 2);
							if (hillHeight <= 1) {
								validHill = false;
								break;
							}
						}
					}
					
					if (validHill) {
						possibleHills.add(new GameElement(hillLeft, hillWidth, hillHeight));
					}
				}
			}
			
			// select and build one of the valid hills
			if (!possibleHills.isEmpty()) {
				GameElement hill = possibleHills.get(random.nextInt(possibleHills.size()));
				int hillWidth = hill.width();
				hillLeft = hill.start();
				int hillRight = hillLeft + hillWidth - 1;
				int hillHeight = hill.height();
				hills.add(hill);
				
				// construct hill
				for (int x = hillLeft; x <= hillRight; x++) {
					peak[x] = hillHeight;
					for (int y = hillHeight; y < floorHeight[x]; y++) {
						int xx = 5;
						if (x == hillLeft)
							xx = 4; // left edge
						if (x == hillRight)
							xx = 6; // right edge
						int yy = 9;
						if (y == hillHeight)
							yy = 8; // hill top

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
	 * Returns whether or not x value is on or immediately near a pit.
	 */
	private boolean nearPit(int x) {
		return (isPit(x) || isPit(x-1) || isPit(x+1));
	}
	
	/*
	 * Returns whether or not x value is on a pit.
	 */
	private boolean isPit(int x) {
		return (floorHeight[x-1] == height+1);
	}
	
	/*
	 * Returns whether or not x value is on or immediately left/right of a pit.
	 * If left is true, determine whether there is a change to the left of x,
	 * otherwise determine whether there is a change to the right of x.
	 */
	private boolean nearElevationChange(int x, boolean left) {
		if (left) {
			return (floorHeight[x-1] != floorHeight[x]);
		}
		
		return (floorHeight[x+1] != floorHeight[x]);
	}
	
	/*
	 * Returns whether or not x value is on or immediately left/right of a hill
	 * edge. If left is true, determine whether there is a change to the left
	 * of x, otherwise determine whether there is a change to the right of x.
	 */
	private boolean nearHillEdge(int x, boolean left) {
		if (left) {
			return (peak[x-1] != peak[x]);
		}
		
		return (peak[x+1] != peak[x]);
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
					if(getBlock(x, y-1) == HILL_FILL || getBlock(x, y-1) == HILL_RIGHT || getBlock(x, y-1) == HILL_LEFT
					   || getBlock(x, y-1) == HILL_TOP || getBlock(x, y-1) == HILL_TOP_LEFT
					   || getBlock(x, y-1) == HILL_TOP_RIGHT) {
						
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
	
	private int addTubes(int zoneStart, int maxLength, double modifier) {
		int length = maxLength;
		
		int tubeAttempts = (int)(Math.round(modifier*maxLength));
		
		for (int att = 0; att < tubeAttempts; att++) {
			int tubeLeft = zoneStart + random.nextInt(maxLength-1);
			int tubeRight = tubeLeft+1;
			
			List<GameElement> possibleTubes = new ArrayList<GameElement>();
			
			// ensure valid edges
			if (!nearPit(tubeLeft) && !nearHillEdge(tubeLeft, true) && !nearElevationChange(tubeLeft, true)
				&& !nearPit(tubeRight) && !nearHillEdge(tubeRight, false) && !nearElevationChange(tubeRight, false)) {
				
				boolean validTube = true;
				int tubeHeight = height + 1;
				
				for (int x = tubeLeft; x <= tubeRight; x++) {
					if (tubeHeight >= floorHeight[x] - 1) {
						tubeHeight = floorHeight[x] - (random.nextInt(2) + 2);
						if (tubeHeight <= 1) {
							validTube = false;
							break;
						}
					}
				}
				
				if (validTube) {
					possibleTubes.add(new GameElement(tubeLeft, 2, tubeHeight));
				}
			}
			
			// select and build one of the valid hills
			if (!possibleTubes.isEmpty()) {
				GameElement tube = possibleTubes.get(random.nextInt(possibleTubes.size()));
				tubes.add(tube);
				
				for (int x = tube.start(); x <= tube.start()+1; x++) {
					for (int y = tube.height(); y <= height; y++) {
						int xPic = 10 + x - tube.start();
							
						if (y == tube.height()) {
							// tube top
							setBlock(x, y, (byte) (xPic + 0 * 16));
						} else {
							// tube side
							setBlock(x, y, (byte) (xPic + 1 * 16));
						}
						
						peak[x] = tube.height();
					}
				}
			}
		}
		
		return length;
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

	private class GameElement
	{
		private int start, width, height;

		public GameElement(int start, int width, int height) {
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
