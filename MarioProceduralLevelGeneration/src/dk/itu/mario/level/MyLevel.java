package dk.itu.mario.level;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import dk.itu.mario.MarioInterface.GamePlay;
import dk.itu.mario.MarioInterface.LevelInterface;
import dk.itu.mario.ai.PlayerClassifier;
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
	
	private int[] floorHeight;
	private int[] peak;
	
	// use
	private final int TERRAIN = 0;
	private final int HILL = 1;
	private final int TUBE = 2;
	private final int CANNON = 3;
	private final int BLOCK = 4;
	// behavior
	private final int EMPTY = 0;
	private final int VISUAL = 1;
	private final int BLOCKING = 2;
	private final int SURFACE = 3;
	// storage
	private int[][] blocks;

	private int playerClass;
	
	private static Random levelSeedRandom = new Random();
	public static long lastSeed;

	Random random;

	private int difficulty;
	private int type;
	private int pits;
	private List<Surface> surfaces;

	public MyLevel(int width, int height) {
		super(width, height);
		playerClass = PlayerClassifier.PLAYER_DEFAULT;
	}

	public MyLevel(int width, int height, long seed, int difficulty, int type, GamePlay playerMetrics) {
		this(width, height);
		
		if (playerMetrics != null) {
			playerClass = PlayerClassifier.classify(playerMetrics);
			System.out.printf("\nPLAYER IS A(N): %s\n\n", PlayerClassifier.PLAYER_TYPES[playerClass].toUpperCase());
		} else {
			playerClass = PlayerClassifier.PLAYER_DEFAULT;
			System.out.println("\nPLAYER IS A: DEFAULT\n");
		}
		
		creat(seed, difficulty, type);
	}

	public void creat(long seed, int difficulty, int type) {
		floorHeight = new int[width];
		peak = new int[width];
		Arrays.fill(peak, height+1);
		
		surfaces = new ArrayList<Surface>();

		blocks = new int[height][width];
		
		this.type = type;
		this.difficulty = difficulty;

		lastSeed = seed;
		random = new Random(seed);

		// create the start location
		int length = 0;
		length += buildStart(0, width);

		double terrainModifier = 0.5;
		double pitModifier = 0.5;
		double hillModifier = 0.5;
		double tubeModifier = 0.5;
		double cannonModifier = 0.5;
		double boxModifier = 0.5;
		double coinModifier = 0.5;
		double powerModifier = 0.5;
		double enemyModifier = 0.5;
		
		if (playerClass == PlayerClassifier.PLAYER_EXPLORER) {
			terrainModifier	= 0.2;
			pitModifier		= 0.2;
			hillModifier	= 0.4;
			tubeModifier	= 0;
			cannonModifier	= 0;
			boxModifier		= 1;
			coinModifier	= 0.7;
			powerModifier	= 0.7;
			enemyModifier	= 0;
		} else if (playerClass == PlayerClassifier.PLAYER_KILLER) {
			terrainModifier	= 0.5;
			pitModifier		= 0.5;
			hillModifier	= 0.5;
			tubeModifier	= 0.5;
			cannonModifier	= 0.7;
			boxModifier		= 0.5;
			coinModifier	= 0.5;
			powerModifier	= 0.5;
			enemyModifier	= 1;
		} else if (playerClass == PlayerClassifier.PLAYER_SPEED_RUNNER) {
			terrainModifier	= 1;
			pitModifier		= 0.6;
			hillModifier	= 1;
			tubeModifier	= 0;
			cannonModifier	= 0;
			boxModifier		= 0;
			coinModifier	= 0;
			powerModifier	= 0;
			enemyModifier	= 0;
		} else if (playerClass == PlayerClassifier.PLAYER_NOOB) {
			terrainModifier	= 0.2;
			pitModifier		= 0.2;
			hillModifier	= 0.2;
			tubeModifier	= 0.2;
			cannonModifier	= 0;
			boxModifier		= 0.2;
			coinModifier	= 0.2;
			powerModifier	= 0.7;
			enemyModifier	= 0.2;
		}
		
		System.out.printf("Modifiers:\n----------------\n" + 
				"ter :\t%f\n" +
				"pit :\t%f\n" +
				"hill:\t%f\n" +
				"tube:\t%f\n" +
				"can :\t%f\n" +
				"box :\t%f\n" +
				"coin:\t%f\n" +
				"pow :\t%f\n" +
				"enmy:\t%f\n" +
				"\n",
				terrainModifier,
				pitModifier,
				hillModifier,
				tubeModifier,
				cannonModifier,
				boxModifier,
				coinModifier,
				powerModifier,
				enemyModifier);
		//TODO
		buildTerrain(length, width-length-12, terrainModifier); // 12 = 8 for end + gap
		addPits(length, width-length-12, pitModifier);
		addHills(length, width-length-12, hillModifier);
		addTubes(length, width-length-12, tubeModifier, enemyModifier);
		addCannons(length, width-length-12, cannonModifier);
		addBoxes(length, width-length-12, boxModifier, coinModifier, powerModifier);
		addEnemies(length, width-length-12, enemyModifier);
		
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
		
		/*
		System.out.println(Arrays.toString(floorHeight));
		System.out.println(Arrays.toString(peak));
		
		System.out.println();
		for (int y = 0; y < blocks.length; y++) {
			for (int x = 0; x < blocks[0].length; x++) {
				if (blocks[y][x] < 10) {
					System.out.print(" ");
				}
				System.out.print(blocks[y][x] + ", ");
			}
			System.out.println();
		}
		*/
	}
	
	private double randomMod() {
		return random.nextDouble();
	}

	/*
	 * Constructs the beginning of the level - a flat, undecorated piece of
	 * flooring.
	 */
	private int buildStart(int zoneStart, int maxLength) {
		int length = 10 + random.nextInt(3);

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
		int maxHeight = height - 5;
		int minHeight = height - 1;
		
		// make terrain more difficult depending on modifier
		int heightVariance = (int)(Math.round(3*modifier));
		int changeProbability = (int)(Math.round(10*modifier)) + 1;
		
		int minPadLength = 7;
		int floor = floorHeight[zoneStart-1];
		int padSize = 0;
		
		// alter floor for the length of the segment
		for (int x = zoneStart; x < zoneStart + length; x++) {
			
			if (floor < maxHeight) { floor = maxHeight; }
			if (floor > minHeight) { floor = minHeight; }
			
			for (int y = 0; y < height; y++) {
				if (y >= floor) {
					setBlock(x, y, GROUND);
					if (getBlock(x, y) == HILL_TOP_LEFT)
						setBlock(x, y - 2, HILL_TOP_LEFT_IN);
					if (getBlock(x, y) == HILL_TOP_RIGHT)
						setBlock(x, y - 2, HILL_TOP_RIGHT_IN);
					
					if (y == floor) {
						saveBlockInfo(x, y, TERRAIN, SURFACE);
					} else {
						saveBlockInfo(x, y, TERRAIN, VISUAL);
					}
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
		
		int pitProbability = (int)(Math.round(3*(1.0/Math.pow(modifier,4)))) + 1;
		int pitWidth = (int)(Math.round(2*modifier)) + 2; // cannot exceed min pad length
		
		// to fix an issue
		if (pitProbability < 1) { pitProbability = 1; }
		
		// find location to create pit
		for (int x = zoneStart; x < zoneStart + length; x++) {
			if (random.nextInt(pitProbability) == 0) {
				
				// avoid graphical issue with single ground blocks
				if (!nearElevationChange(x, true)
					&& !nearElevationChange(x-1, true)
					&& !nearElevationChange(x+pitWidth, false)
					&& !nearElevationChange(x+pitWidth+1, false)) {
					
					// create pit
					for (int pit = x+1; pit <= x+pitWidth; pit++) {
						for (int y = floorHeight[pit]; y < height; y++) {
							setBlock(pit, y, (byte) 0); // empty block
							saveBlockInfo(pit, y, TERRAIN, EMPTY);
						}
						
						floorHeight[pit] = height+1;
						peak[pit] = height+1;
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
							hillHeight = peak[x] - (random.nextInt(2) + 2); // 2:3
							if (hillHeight <= 3) {
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
			
			// randomly select and build one of the valid hills
			if (!possibleHills.isEmpty()) {
				GameElement hill = possibleHills.get(random.nextInt(possibleHills.size()));
				
				int hillWidth = hill.width();
				hillLeft = hill.start();
				int hillRight = hillLeft + hillWidth - 1;
				int hillHeight = hill.height();
				
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
							if (y == hillHeight) {
								saveBlockInfo(x, y, HILL, SURFACE);
							} else {
								saveBlockInfo(x, y, HILL, VISUAL);
							}
							
						} else {
							if (getBlock(x, y) == HILL_TOP_LEFT)
								setBlock(x, y, HILL_TOP_LEFT_IN);
							if (getBlock(x, y) == HILL_TOP_RIGHT)
								setBlock(x, y, HILL_TOP_RIGHT_IN);
						}
					}
				}
				
				// register hill as surface
				surfaces.add(new Surface(hillLeft, hillHeight, hillRight-hillLeft+1, HILL));
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
	 * Returns whether or not their lies a pit between x1 and x2.
	 */
	private boolean nearPit(int x1, int x2) {
		for (int x = x1 - 1; x <= x2 + 1; x++) {
			if (isPit(x)) {
				return true;
			}
		}
		
		return false;
	}
	
	/*
	 * Returns whether or not x value is on a pit.
	 */
	private boolean isPit(int x) {
		return (floorHeight[x-1] == height+1);
	}
	
	/*
	 * Returns whether or not x value is on or immediately left/right of an
	 * elevation change on the map terrain. If left is true, determine whether
	 * there is a change to the left of x, otherwise determine whether there is
	 * a change to the right of x.
	 */
	private boolean nearElevationChange(int x, boolean left) {
		if (left) {
			return (floorHeight[x-1] != floorHeight[x]);
		}
		
		return (floorHeight[x+1] != floorHeight[x]);
	}
	
	/* 
	 * Returns whether or not there exists an elevation change anywhere between
	 * x1-1 to x2+1 inclusive.
	 */
	private boolean nearElevationChange(int x1, int x2) {
		for (int x = x1 - 1; x < x2 + 1; x++) {
			if (floorHeight[x] != floorHeight[x+1]) {
				return true;
			}
		}
		
		return false;
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
	 * Returns whether or not there exists a hill edge anywhere between x1-1 to
	 * x2+1 inclusive.
	 */
	private boolean nearHillEdge(int x1, int x2) {
		for (int x = x1 - 1; x < x2 + 1; x++) {
			if (peak[x] != peak[x+1]) {
				return true;
			}
		}
		
		return false;
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
	
	/*
	 * Saves a blocks use and behavior as a single integer in blocks[][].
	 */
	private void saveBlockInfo(int x, int y, int use, int behavior) {
		blocks[y][x] = use * 10 + behavior;
	}
	
	/*
	 * Returns a block's use as recorded in blocks[][]. 
	 */
	private int blockUse(int x, int y) {
		return blocks[y][x] / 10; 
	}
	
	/*
	 * Returns a block's behavior as recorded in blocks[][].
	 */
	private int blockBehavior(int x, int y) {
		return blocks[y][x] % 10;
	}
	
	/*
	 * Changes a blocks saved use in blocks[][].
	 */
	private void changeBlockUse(int x, int y, int use) {
		saveBlockInfo(x, y, use, blockBehavior(x, y));
	}
	
	/*
	 * Changes a blocks saved behavior in blocks[][].
	 */
	private void changeBlockBehavior(int x, int y, int behavior) {
		saveBlockInfo(x, y, blockUse(x, y), behavior);
	}
	
	/*
	 * Adds tubes in the area defined by zoneStart to zoneStart + maxLength. Amount based on the modifier. 
	 */
	private int addTubes(int zoneStart, int maxLength, double tubeMod, double enemyMod) {
		int length = maxLength;
		
		int tubeAttempts = (int)(Math.round(tubeMod*maxLength)/4);
		
		for (int att = 0; att < tubeAttempts; att++) {
			int tubeLeft = zoneStart + random.nextInt(maxLength-1);
			int tubeRight = tubeLeft+1;
			
			GameElement tube = null;
			
			// ensure valid edges
			if (!nearPit(tubeLeft, tubeRight) && !nearElevationChange(tubeLeft, tubeRight)
				&& !nearHillEdge(tubeLeft, tubeRight)) {
				
				boolean validTube = true;
				int tubeHeight = height + 1;
				
				for (int x = tubeLeft; x <= tubeRight; x++) {
					if (tubeHeight >= floorHeight[x] - 1) {
						tubeHeight = floorHeight[x] - (random.nextInt(1) + 3);
						if (tubeHeight <= 1) {
							validTube = false;
							break;
						}
					}
				}
				
				if (validTube) {
					for (int y = tubeHeight; y < floorHeight[tubeLeft]; y++) {
						for (int x = tubeLeft; x <= tubeRight; x++) {
							if (blockBehavior(x, y) != EMPTY) {
								validTube = false;
								break;
							}
						}
					}
				}
				
				if (validTube) {
					tube = new GameElement(tubeLeft, 2, tubeHeight);
				}
			}
			
			// select and build the tube if valid
			if (tube != null) {
				
				for (int x = tube.start(); x <= tube.start()+1; x++) {
					for (int y = tube.height(); y < floorHeight[x]; y++) {
						int xPic = 10 + x - tube.start();
							
						if (y == tube.height()) {
							// tube top
							setBlock(x, y, (byte) (xPic + 0 * 16));
							saveBlockInfo(x, y, TUBE, SURFACE);
						} else {
							// tube side
							setBlock(x, y, (byte) (xPic + 1 * 16));
							saveBlockInfo(x, y, TUBE, BLOCKING);
						}
						
						peak[x] = tube.height();
					}
					
					changeBlockBehavior(x, floorHeight[x], BLOCKING);
				}
				
				if (random.nextInt(101) < enemyMod*100) {
					setSpriteTemplate(tube.start(), tube.height(), new SpriteTemplate(Enemy.ENEMY_FLOWER, false));
					ENEMIES++;
				}
				
				// register tube as surface
				surfaces.add(new Surface(tube.start(), tube.height(), 2, TUBE));
			}
		}
		
		return length;
	}
	
	/*
	 * Adds cannons in the area defined by zoneStart to zoneStart + maxLength. Amount based on the modifier. 
	 */
	private int addCannons(int zoneStart, int maxLength, double modifier) {
		//TODO - make them always vary in height when next to each other
		int length = maxLength;
		System.out.println("Building Cannons:\n-----------------");
		System.out.println(" # | Height | Last Height ");
		int cannonsBuilt = 0;
		int cannonAttempts = (int)(Math.round(modifier*maxLength)/5);
		int lastCannonHeight = 0;
		for (int att = 0; att < cannonAttempts; att++) {
			int cannonX = zoneStart + random.nextInt(maxLength-1);
			
			GameElement cannon = null;
			
			// ensure valid edges
			if (!nearPit(cannonX) && !nearElevationChange(cannonX, cannonX)
				&& !nearHillEdge(cannonX, cannonX)) {
				
				boolean validCannon = true;
				int cannonHeight = height + 1;
				
				
				if (cannonHeight >= floorHeight[cannonX] - 1) {
					do {
						cannonHeight = floorHeight[cannonX] - (random.nextInt(3) + 2);
						if (cannonHeight <= 1) {
							validCannon = false;
						}
					} while(cannonHeight == lastCannonHeight || cannonHeight == lastCannonHeight+1 );
				}
				
				
				if (validCannon) {
					for (int y = cannonHeight; y < floorHeight[cannonX]; y++) {
						if (blockBehavior(cannonX, y) != EMPTY) {
							validCannon = false;
							break;
						}
					}
				}
				
				if (validCannon) {
					cannon = new GameElement(cannonX, 1, cannonHeight);	
				}
			}
			
			// select and build the tube if valid
			if (cannon != null) {
				for (int y = cannon.height(); y < floorHeight[cannonX]; y++) {
					int xPic = 10 + cannonX - cannon.start();
						
					if (y == cannon.height()) {
						// cannon top
						setBlock(cannonX, y, (byte) (14 + 0 * 16));
						saveBlockInfo(cannonX, y, CANNON, SURFACE);
					} else if (y == cannon.height() + 1) {
						setBlock(cannonX, y, (byte) (14 + 1 * 16));
						saveBlockInfo(cannonX, y, CANNON, BLOCKING);
					}else {
						// cannon body
						setBlock(cannonX, y, (byte) (14 + 2 * 16));
						saveBlockInfo(cannonX, y, CANNON, BLOCKING);
					}

					peak[cannonX] = cannon.height();
				}
				
				changeBlockBehavior(cannonX, floorHeight[cannonX], BLOCKING);
				
				// register cannon as surface
				surfaces.add(new Surface(cannonX, cannon.height(), 1, CANNON));
				
				cannonsBuilt++;
				if(cannonsBuilt < 10){
					System.out.println(String.format(" %d  |   %d   | %d ", cannonsBuilt, cannon.height(), lastCannonHeight));
				} else {
					System.out.println(String.format(" %d |   %d   | %d ", cannonsBuilt, cannon.height(), lastCannonHeight));
				}
				
				lastCannonHeight = cannon.height();
			}
		}
		System.out.println("-----------------");
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
	
	private class Surface
	{
		private int x, y, width, use;
		
		public Surface(int x, int y, int width, int use) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.use = use;
		}
		
		public int x() {
			return x;
		}
		
		public int y() {
			return y;
		}
		
		public int width() {
			return width;
		}
		
		public int use() {
			return use;
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

	private int addBoxes(int zoneStart, int maxLength, double modifier, double coinMod, double powerMod) {
		int length = maxLength;
		int boxAttempts = (int)(Math.round(modifier*maxLength));
		System.out.printf("Attempts: %d\n", boxAttempts);
		int[] usedX = new int[maxLength-zoneStart];
		int numUsed = 0;
		for (int att = 0; att < boxAttempts; att++) {
			int boxStartX = zoneStart + random.nextInt(maxLength-1);
			int boxY = peak[boxStartX] - 4;
			int maxBoxLength = 1;
			int boxLength = 1;
			boolean validBlockLine = true;
			
			if (boxY - 1 < 1 || Arrays.asList(usedX).contains(boxStartX)
				|| nearPit(boxStartX) 
				|| nearElevationChange(boxStartX, boxStartX+boxLength)
				|| nearHillEdge(boxStartX, boxStartX+boxLength))
				validBlockLine = false;
			
			//get valid box lengths 
			for (int x = 1; x <= 4; x++) {
				if (peak[boxStartX+x] < boxY + 1 
						|| Arrays.asList(usedX).contains(boxStartX+x) 
						|| nearElevationChange(boxStartX, boxStartX+x)
						|| nearHillEdge(boxStartX, boxStartX+x)){
					//validBoxes = false;
					break;
				} else {
					maxBoxLength++;
				}
			}

			if (maxBoxLength > 1) {
				boxLength = 2 + random.nextInt(maxBoxLength-1);
			} else {
				validBlockLine = false;
			}
			
			if (validBlockLine) {
				for (int x = boxStartX; x < boxLength + boxStartX; x++) {
					usedX[numUsed] = x;
					numUsed++;
					if (random.nextInt(15 - (int)(powerMod*10)) == 0) {
						setBlock(x, boxY, BLOCK_POWERUP);
						saveBlockInfo(x, boxY, BLOCK, SURFACE);
						BLOCKS_POWER++;
						peak[x] = boxY;
					} else if((random.nextInt(10 - (int)(coinMod*10)) == 0)) { // the fills a block with a hidden coin
						setBlock(x, boxY, BLOCK_COIN);
						saveBlockInfo(x, boxY, BLOCK, SURFACE);
						BLOCKS_COINS++;
						peak[x] = boxY;
					} else if (random.nextInt(4) == 0) {
						if (random.nextInt(4) == 0) {
							setBlock(x, boxY, (byte) (2 + 1 * 16));
							saveBlockInfo(x, boxY, BLOCK, SURFACE);
							peak[x] = boxY;
						} else {
							setBlock(x, boxY, (byte) (1 + 1 * 16));
							saveBlockInfo(x, boxY, BLOCK, SURFACE);
							peak[x] = boxY;
						}
					} else {
						setBlock(x, boxY, BLOCK_EMPTY);
						saveBlockInfo(x, boxY, BLOCK, SURFACE);
						BLOCKS_EMPTY++;
						peak[x] = boxY;
					}
				}
				
				// register boxline as surface
				surfaces.add(new Surface(boxStartX, boxY, boxLength, BLOCK));
			}
		}
		
		System.out.println(Arrays.toString(usedX));
		System.out.println(numUsed);
		Set<Integer> mySet = new HashSet (Arrays.asList(usedX));
		int temp = 0;
		for (int i = 0; i < usedX.length; i++) {
			if (usedX[i] == 0)
				temp++;
		}
		
		System.out.println(mySet.size() == usedX.length-temp);
		return length;
	}
	
	private int addEnemies(int zoneStart, int maxLength, double enemyMod) {
		int length = maxLength;
		
		// attempt to spawn an enemy on all surfaces
		for (Surface s : surfaces) {
			if (s.use() != CANNON && s.use() != TUBE) {
				if (random.nextInt(101) <= (enemyMod*100)) {
					int x = s.x() + random.nextInt(s.width());
					int y = s.y() - 1;
					
					int typeModifier = 0;
					int type = 0;
					
					// determine highest allowed enemy difficulty
					if (enemyMod < 0.25) {
						typeModifier = 1; // easy
					} else if (enemyMod >= 0.25 && enemyMod < 0.75) {
						typeModifier = 2; // medium
					} else if (enemyMod >= 0.75) {
						typeModifier = 3; // hard
					}
					
					// choose from easy to highest allowed difficulty
					type = random.nextInt(typeModifier) + 1;
					
					// spawn enemy from selected difficulty
					if (type == 1) {
						type = Enemy.ENEMY_GOOMBA;
					} else if (type == 2) {
						type = random.nextInt(2); // red/green koopa
					} else if (type == 3) {
						type = Enemy.ENEMY_SPIKY;
					}
					
					// flying only possible with 0.75 enemyMod, afterwards 0.25 chance of flying
					boolean flying = (random.nextInt(typeModifier)+1 == 3);
					
					setSpriteTemplate(x, y, new SpriteTemplate(type, flying));
				}
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
