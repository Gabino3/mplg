package dk.itu.mario.level;

import dk.itu.mario.engine.sprites.SpriteTemplate;

/**
 * Represents a "chunk" or piece of a level.
 */
public class LevelNode {
	public static final byte CANNON_TOP = (byte) (14 + 0 * 16);

	
	/**
	 * An enum that represents what this type is
	 */
	public enum Type{FLAT, HILL, JUMP, TUBES, CANNONS, ENEMIES};
	
	/**
	 * The next node
	 */
	private LevelNode next;
	
	/**
	 * The previous node
	 */
	private LevelNode prev;

	/**
	 * Represents how difficult this chunk of the map is. Each obstacle adds 1 point to the difficulty rating
	 */
	private int difficulty;
	
	/**
	 * The Type of this node/chunk
	 */
	private Type type;
	
	/**
	 * The number of hills
	 */
	private int numHills;
	
	/**
	 * The number of jumps
	 */
	private int numJumps;
	
	/**
	 * The number of tubes
	 */
	private int numTubes;
	
	/**
	 * The number of cannons
	 */
	private int numCannons;
	
	/**
	 * The number of enemies
	 */
	private int numEnemies;
	
	/**
	 * The number of coins
	 */
	private int numCoins;
	
	/**
	 * The number of power ups
	 */
	private int numPowerups;
	
	/**
	 * The number of blocks with coins in them
	 */
	private int numCoinBlocks;
	
	/**
	 * The number of empty blocks;
	 */
	private int numEmptyBlocks;
	
	/**
	 * The subarray or "chunk" of the map that this node represents;
	 */
	private byte[][] map;
	
	/**
	 * The sprites for this submap
	 */
	private SpriteTemplate[][] sprites;
	
	/**
	 * Constructs a LevelNode and determines the data
	 * @param map
	 */
	public LevelNode(byte[][] map, SpriteTemplate[][] sprites) {
		this.map = map;
		this.sprites = sprites;
		this.type = Type.FLAT;
		determineChunkStats();
	}
	
	/**
	 * Iterates through the map and records the statistics
	 */
	public void determineChunkStats() {
		int emptyNeighborIndex = Integer.MIN_VALUE;
		reset();
		for (int i = 0; i < map.length; i++) {
			boolean isRowEmpty = true;
			for (int j = 0; j < map[i].length; j++) {
				if (map[i][j] != 0) { //This row is not empty. In other words, there is no jump in this row
					isRowEmpty = false;
				}
				checkEnemy(i, j);
				checkCannons(i, j);
				checkTubes(i, j);
				checkHills(i, j);
				recordOtherStats(i, j); //coins, blocks, etc.
			}
			if (isRowEmpty && i - 1 != emptyNeighborIndex) { //A jump has been found
				emptyNeighborIndex = i;
				numJumps++;
				difficulty++;
				if (this.type != Type.JUMP && numJumps > getCurrentTypeCount()) {
					this.type = Type.JUMP;
				}
			}
			else if (isRowEmpty) //We need to keep track of this empty row
				emptyNeighborIndex = i;
		}
	}
	
	/**
	 * Resets the counters
	 */
	public void reset() {
		this.difficulty = 0;
		this.numCannons = 0;
		this.numEnemies = 0;
		this.numHills = 0;
		this.numJumps = 0;
		this.numTubes = 0;
		this.numCoins = 0;
		this.numPowerups = 0;
		this.numCoinBlocks = 0;
		this.numEmptyBlocks = 0;
	}
	
	/**
	 * Records blocks with coins, empty blocks, powerup blocks, and coins
	 * @param x the x coordinate
	 * @param y the y coordinate
	 */
	public void recordOtherStats(int x, int y) {
		switch (map[x][y]) {
			case Level.BLOCK_COIN:
				numCoinBlocks++;
				break;
			case Level.BLOCK_EMPTY:
				numEmptyBlocks++;
				break;
			case Level.BLOCK_POWERUP:
				numPowerups++;
				break;
			case Level.COIN:
				numCoins++;
				break;
			default:
				break;
		}
	}
	
	/**
	 * Checks if this location is a hill. It is considered a hill of the top left corner
	 * is present at this location
	 * @param x the x coordinate
	 * @param y the y coordinate
	 */
	public void checkHills(int x, int y) {
		if (map[x][y] == Level.HILL_TOP_LEFT || map[x][y] == Level.HILL_TOP_LEFT_IN) { //Counts a hill of the left edge is present
			numHills++;
			difficulty++;
			if (this.type != Type.HILL && numHills > getCurrentTypeCount()) {
				this.type = Type.HILL;
			}
		}
	}
	
	/**
	 * Checks if this location has a tube
	 * @param x the x coordinate
	 * @param y the y coordinate
	 */
	public void checkTubes(int x, int y) {
		if (map[x][y] == Level.TUBE_TOP_LEFT) {
			numTubes++;
			difficulty++;
			if (this.type != Type.TUBES && numTubes > getCurrentTypeCount()) {
				this.type = Type.TUBES;
			}
		}
	}
	
	/**
	 * Checks if the given location contains a cannon
	 * @param x the x coordinate
	 * @param y the y coordinate
	 */
	public void checkCannons(int x, int y) {
		if (map[x][y] == CANNON_TOP) { //This byte is used at the top for every cannon
			numCannons++;
			difficulty++;
			if (this.type != Type.CANNONS && numCannons > getCurrentTypeCount()) {
				this.type = Type.CANNONS;
			}
		}
	}
	
	/**
	 * Check if this chunk contains an enemy
	 * @param x the x index of the map
	 * @param y the y index of the map
	 */
	public void checkEnemy(int x, int y) {
		if (sprites[x][y] != null) { //If sprite is at this location an enemy must be there
			numEnemies++;
			difficulty++;
			if (this.type != Type.ENEMIES && numEnemies > getCurrentTypeCount()) {
				this.type = Type.ENEMIES;
			}
		}
	}
		
	/**
	 * @return The current type count
	 */
	public int getCurrentTypeCount() {
		switch(type) {
			case FLAT:
				return 0;
			case HILL:
				return this.numHills;
			case JUMP:
				return this.numJumps;
			case TUBES:
				return this.numTubes;
			case CANNONS:
				return this.numCannons;
			case ENEMIES:
				return this.numEnemies;
			default:
				return 0;
		}
	}
		
	/**
	 * @return The next node
	 */
	public LevelNode getNextNode() {
		return this.next;
	}
	
	/**
	 * @param next The next node
	 */
	public void setNext(LevelNode next) {
		this.next = next;
	}
	
	/**
	 * @return The previous node
	 */
	public LevelNode getPrevNode() {
		return this.prev;
	}
	
	/**
	 * @param prev The previous node
	 */
	public void setPrevNode(LevelNode prev) {
		this.prev = prev;
	}
	
	/**
	 * @return The difficulty of this chunk
	 */
	public int getDifficulty() {
		return this.difficulty;
	}
	
	/**
	 * @return What type of chunk this is
	 */
	public Type getType() {
		return this.type;
	}
	
	/**
	 * @return The number of hills in this chunk
	 */
	public int getNumberOfHills() {
		return this.numHills;
	}
	
	/**
	 * @return The number of jumps in this chunk
	 */
	public int getNumberOfJumps() {
		return this.numJumps;
	}
	
	/**
	 * @return The number of tubes
	 */
	public int getNumberOfTubes() {
		return this.numTubes;
	}
	
	/**
	 * @return The number of cannons
	 */
	public int getNumberOfCannons() {
		return this.numCannons;
	}
	
	/**
	 * @return The number of enemies in this chunk
	 */
	public int getNumberOfEnemies() {
		return this.numEnemies;
	}
	
	/**
	 * @return The subarray of the level that represents this chunk
	 */
	public byte[][] getMap() {
		return this.map;
	}
	
	/**
	 * @return The sprites for this chunk
	 */
	public SpriteTemplate[][] getSprites() {
		return this.sprites;
	}
	
	/**
	 * @param map The new map for this chunk
	 */
	public void setMap(byte[][] map) {
		this.map = map;
	}
	
	/**
	 * Prints the stats of this chunk
	 */
	public void printStats() {
		System.out.println("Chunk Type: " + this.type.toString());
		System.out.println("Difficulty: " + this.difficulty);
		System.out.println("Number of Enemies: " + this.numEnemies);
		System.out.println("Number of jumps: " + this.numJumps);
		System.out.println("Number of Hills: " + this.numHills);
		System.out.println("Number of cannons: " + this.numCannons);
		System.out.println("Number of tubes: " + this.numTubes);
		System.out.println("Number of empty blocks: " + this.numEmptyBlocks);
		System.out.println("Number of coins: " + this.numCoins);
		System.out.println("Number of coin blocks: " + this.numCoinBlocks);
		System.out.println("Number of powerups: " + this.numPowerups);
	}
	
	/**
	 * Prints the map for this chunk
	 */
	public void printChunkMap() {
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[i].length; j++) {
				System.out.print(map[i][j] + " ");
			}
			System.out.println();
		}
	}
	
	//Returns a type based on a passed in int
	public Type getType(int i){
		Type toReturn = Type.FLAT;
		if(i==1){
			toReturn = Type.HILL;
		}
		else if(i==2){
			toReturn = Type.JUMP;
		}
		else if(i==3){
			toReturn = Type.TUBES;
		}
		else if(i==4){
			toReturn = Type.CANNONS;
		}
		else if(i==5){
			toReturn = Type.ENEMIES;
		}
		
		return toReturn;
	}
	
	public void setBlock(int x, int y, byte b)
    {
        if (x < 0) return;
        if (y < 0) return;
        if (x >= map.length) return;
        if (y >= map[0].length) return;
        map[x][y] = b;
    }
	
	public byte getBlock(int x, int y)
    {
        if (x < 0) x = 0;
        if (y < 0) return 0;
        if (x >= map.length) x = map.length - 1;
        if (y >= map[0].length) y = map[0].length - 1;
        return map[x][y];
    }
	
	public void setSpriteTemplate(int x, int y, SpriteTemplate spriteTemplate)
    {
        if (x < 0) return;
        if (y < 0) return;
        if (x >= map.length) return;
        if (y >= map[0].length) return;
        sprites[x][y] = spriteTemplate;
    }
	
	public SpriteTemplate getSprite(int x, int y){
		if (x < 0) x = 0;
        if (y < 0) y=0;
        if (x >= map.length) x = map.length - 1;
        if (y >= map[0].length) y = map[0].length - 1;
        return sprites[x][y];
	}
	
}
