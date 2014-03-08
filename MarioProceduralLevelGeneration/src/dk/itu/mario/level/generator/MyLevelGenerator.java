package dk.itu.mario.level.generator;

import java.util.Random;

import dk.itu.mario.MarioInterface.Constraints;
import dk.itu.mario.MarioInterface.GamePlay;
import dk.itu.mario.MarioInterface.LevelGenerator;
import dk.itu.mario.MarioInterface.LevelInterface;
import dk.itu.mario.level.CustomizedLevel;
import dk.itu.mario.level.Level;
import dk.itu.mario.level.LevelChunker;
import dk.itu.mario.level.LevelNode;
import dk.itu.mario.level.MyLevel;
import dk.itu.mario.level.RandomLevel;

public class MyLevelGenerator extends CustomizedLevelGenerator implements LevelGenerator{

	public LevelInterface generateLevel(GamePlay playerMetrics) {
		//LevelInterface level = new RandomLevel(320, 15, new Random().nextLong(),3, LevelInterface.TYPE_OVERGROUND);
//		LevelChunker chunker = new LevelChunker((Level) level, 10);
//		LevelNode head = chunker.splitLevel();
//		while (head != null) {
//			head.printChunkMap();
//			head.printStats();
//			System.out.println();
//			head = head.getNextNode();
//		}
		LevelInterface level = new MyLevel(320,15,new Random().nextLong(),1,LevelInterface.TYPE_OVERGROUND,playerMetrics);
		return level;
	}

	@Override
	public LevelInterface generateLevel(String detailedInfo) {
		// TODO Auto-generated method stub
		return null;
	}

}
