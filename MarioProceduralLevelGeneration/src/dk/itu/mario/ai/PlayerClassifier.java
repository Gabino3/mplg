package dk.itu.mario.ai;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.encog.Encog;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.ml.svm.SVM;
import org.encog.ml.svm.training.SVMSearchTrain;
import org.encog.ml.train.MLTrain;

import dk.itu.mario.MarioInterface.GamePlay;

public class PlayerClassifier
{
	public final static int NUM_ATTRIB = 14;
	public final static int NUM_CLASSIFICATIONS = 1;
	public final static double TRAINING_ERROR_TOLERANCE = 0.05;
	
	public final static String[] PLAYER_TYPES = { "Explorer", "Killer", "Speed Runner", "Noob" };
	public final static int PLAYER_EXPLORER = 0;
	public final static int PLAYER_KILLER = 1;
	public final static int PLAYER_SPEED_RUNNER = 2;
	public final static int PLAYER_NOOB = 3;
	
	//public static BasicNetwork network = new BasicNetwork();
	public static SVM svm = new SVM(NUM_ATTRIB, false);
	
	public static int classify(GamePlay gpmTest) {
		int classification = 0;
		
		// retrieve training data files
		File trainingDir = new File("training/");
		File[] trainingFiles = trainingDir.listFiles(new FilenameFilter() {
			@Override
		    public boolean accept(File dir, String name) {
				name = name.toLowerCase();
		        boolean txt = name.endsWith(".txt");
		        boolean test = name.startsWith("e") || name.startsWith("k") || name.startsWith("sr");
		        return txt && test;
		    }
		});
		
		double trainingInput[][] = new double[trainingFiles.length][NUM_ATTRIB];
		double idealOutput[][] = new double[trainingFiles.length][NUM_CLASSIFICATIONS];
		
		// retrieve training data from files
		for (int i = 0; i < trainingFiles.length; i++) {
			File f = trainingFiles[i];
			String name = f.getName().toLowerCase();
			GamePlay gpmTraining = GamePlay.read(f.toString());
			
			// generate ideal trial results
			if (name.startsWith("e")) {
				idealOutput[i][0] = PLAYER_EXPLORER;
			} else if (name.startsWith("k")) {
				idealOutput[i][0] = PLAYER_KILLER;
			} else if (name.startsWith("sr")) {
				idealOutput[i][0] = PLAYER_SPEED_RUNNER;
			} else if (name.startsWith("n")) {
				// TODO actually implement noob
				idealOutput[i][0] = PLAYER_NOOB;
			} else {
				System.out.println("ERROR! ABORT!");
			}
			
			// retrieve and normalize trial data
			List<Double> input = getTrialData(gpmTraining);
			Double max = Double.MIN_VALUE;
			for (Double d : input) {
				if (d > max) { max = d; }
			}
			for (int j = 0; j < NUM_ATTRIB; j++) {
				trainingInput[i][j] = input.get(j)/max;
			}
			
			System.out.println(Arrays.toString(trainingInput[i]));
		}
		
		// use training data to develop network and classify player
		trainNetwork(trainingInput, idealOutput);
		classification = getClassification(gpmTest);
		
		Encog.getInstance().shutdown();
		
		return classification;
	}
	
	private static List<Double> getTrialData(GamePlay gpm) {
		ArrayList<Double> input = new ArrayList<Double>();
		
		input.add((double) gpm.completionTime);
		input.add((double) gpm.timesPressedRun);
		input.add((double) gpm.timeRunningRight);
		input.add((double) gpm.kickedShells);
		input.add((double) gpm.enemyKillByFire);
		input.add((double) gpm.enemyKillByKickingShell);
		input.add(gpm.percentageBlocksDestroyed);
		input.add(gpm.percentageCoinBlocksDestroyed);
		input.add(gpm.percentageEmptyBlocksDestroyed);
		input.add(gpm.percentagePowerBlocksDestroyed);
		input.add((double) gpm.RedTurtlesKilled);
		input.add((double) gpm.GreenTurtlesKilled);
		input.add((double) gpm.GoombasKilled);
		input.add((double) gpm.CannonBallKilled);
		
		return input;
	}
	
	private static void trainNetwork(double[][] trainingInput, double[][] idealOutput) {
		MLDataSet trainingSet = new BasicMLDataSet(trainingInput, idealOutput);
		
		MLTrain train = new SVMSearchTrain(svm, trainingSet);
		
		// train network until tolerance met
		int epoch = 1;
		do {
			train.iteration();
			System.out.println("Epoch #" + epoch + " Error:" + train.getError());
			epoch++;
		} while(train.getError() > TRAINING_ERROR_TOLERANCE);
		train.finishTraining();
		
		// DEBUG: show training results on test data
		System.out.println("Neural Network Results:");
		for(MLDataPair pair: trainingSet ) {
			final MLData output = svm.compute(pair.getInput());
			System.out.println("actual=" + output.getData(0) + ",ideal=" + pair.getIdeal().getData(0));
		}
	}
	
	private static int getClassification(GamePlay gpm) {
		List<Double> trialData = getTrialData(gpm);
		double[] normalizedTrialData = new double[trialData.size()];
		
		// normalize trial data into an array
		Double max = Double.MIN_VALUE;
		for (Double d : trialData) {
			if (d > max) { max = d; }
		}
		for (int j = 0; j < NUM_ATTRIB; j++) {
			normalizedTrialData[j] = trialData.get(j)/max;
		}
		
		MLData input = new BasicMLData(normalizedTrialData);
		return svm.classify(input);
	}
}
