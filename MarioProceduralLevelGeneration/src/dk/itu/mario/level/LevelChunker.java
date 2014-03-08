package dk.itu.mario.level;

import dk.itu.mario.engine.sprites.SpriteTemplate;

/**
 * Splits a level into chunks/nodes
 */
public class LevelChunker {
	
	/**
	 * The head node of the chunked list
	 */
	private LevelNode head;
	
	/**
	 * The size of each chunk that the level should be split into
	 */
	private int chunkSize;
	
	/**
	 * The level that is being separated into chunks
	 */
	private Level level;
	
	/**
	 * The enemies
	 */
	private SpriteTemplate[][] sprites;
	
	/**
	 * @param level The level
	 */
	public LevelChunker(Level level, int chunkSize) {
		sprites = level.getSpriteTemplates();
		this.level = level;
		this.chunkSize = chunkSize;
	}
	
	/**
	 * TODO Account for level sizes not divisible by chunkSize
	 * Splits the level into chunks
	 * @return The head of the chunked level list
	 */
	public LevelNode splitLevel() {
		byte[][] map = level.getMap();
		int numChunks = level.getWidth() / chunkSize;
		int count = 0;
		for (int i = 0; i < numChunks; i++) {
			byte[][] subMap = new byte[chunkSize][map[0].length];
			SpriteTemplate[][] spriteTemplate = new SpriteTemplate[chunkSize][map[0].length];
			System.arraycopy(sprites, count, spriteTemplate, 0, chunkSize);
			System.arraycopy(map, count, subMap, 0, chunkSize);
			add(new LevelNode(subMap, spriteTemplate));
			count += chunkSize;
			
		}
		return head;
	}
	
	/**
	 * Add the chunk/LevelNode to the end of the list
	 * @param levelNode The node to add
	 */
	public void add(LevelNode levelNode) {
		if (head == null)
			head = levelNode;
		else {
			LevelNode curNode = head;
			while (curNode.getNextNode() != null) {
				curNode = curNode.getNextNode();
			}
			curNode.setNext(levelNode);
			levelNode.setPrevNode(curNode);
		}
	}
	
	/**
	 * @return The level that is being split into chunks
	 */
	public Level getLevel() {
		return this.level;
	}
	
	/**
	 * @return The head node of this chunked level list
	 */
	public LevelNode getLevelHead() {
		return this.head;
	}
	
	/**
	 * @return This levels sprites
	 */
	public SpriteTemplate[][] getSprites() {
		return this.sprites;
	}
}
