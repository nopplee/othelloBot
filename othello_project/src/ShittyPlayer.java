import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.TreeMap;

public class ShittyPlayer extends Player {
	
	private static final int LIMIT = 5;
	byte opponent;
	int[][] moveWeight = new int[][] {{200,-90,8,6,6,8,-90,200},{-90,-100,-4,-3,-3,-4,-100,-90},{8,-4,7,4,4,7,-4,8},{6,-3,4,0,0,4,-3,6}
										,{6,-3,4,0,0,4,-3,6},{8,-4,7,4,4,7,-4,8},{-90,-100,-4,-3,-3,-4,-100,-90},{200,-90,8,6,6,8,-90,200}};
	
    public ShittyPlayer(byte player) {
        super(player);
        this.team = "meme";
        opponent = (player == OthelloGame.B ? OthelloGame.W : OthelloGame.B);
    }

    @Override
    public Move move(OthelloState state, HashSet<Move> legalMoves) throws InterruptedException {
    	Move best = null;
        double temp = Double.NEGATIVE_INFINITY;
        for(Move move: legalMoves) {
        	System.out.println(move.toString());
        	double maxVal = minValue(OthelloGame.transition(state, move), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 10);
        	if(temp < maxVal) {
        		temp = maxVal;
        		best = move;
        	}
        }
        return best;
	}

	private double maxValue(OthelloState state, double alpha, double beta, int depth) {
		HashSet<Move> legalMoves = OthelloGame.getAllLegalMoves(state.getBoard(), state.getPlayer());
		if(depth > LIMIT || legalMoves.size() == 0) return utility(state);
		double v = Double.NEGATIVE_INFINITY;
		for(Move move : legalMoves) {
			v = Math.max(v, minValue(OthelloGame.transition(state, move), alpha, beta, depth+1)); //state???
			if(v >= beta) return v; //Pruning here
			alpha = Math.max(alpha, v);
		}
		return v;
	}

	private double minValue(OthelloState state, double alpha, double beta, int depth) {
		HashSet<Move> legalMoves = OthelloGame.getAllLegalMoves(state.getBoard(), state.getPlayer()); //rival player???
		if(depth > LIMIT || legalMoves.size() == 0) return utility(state);
		double v = Double.POSITIVE_INFINITY;
		for(Move move : legalMoves) {
			v = Math.min(v, maxValue(OthelloGame.transition(state, move), alpha, beta, depth+1)); //state???
			if(v <= alpha) return v; //Pruning here
			beta = Math.min(beta, v);
		}
		return v;
	}
	
	private double utility(OthelloState state) {
		double myScore = 0.0;
		int myPieces = 0;
		int enemyLegalMoves = 64;
		int freeSpaces = 1;
		for(int i=0;i<OthelloGame.N;i++){
			for(int j=0;j<OthelloGame.N;j++){
				if(state.getBoard()[i][j] == OthelloGame.E){
					freeSpaces++;
				}
			}
		}
		//extra for final push?
		if(freeSpaces <= LIMIT+3){
			HashSet<Move> legalMoves = OthelloGame.getAllLegalMoves(state.getBoard(), state.getPlayer());
			double possibleScore = 0.0;
			for(Move move:legalMoves){
				OthelloState nextPossibleState = OthelloGame.transition(state, move);
				int enemyPossibleMove = OthelloGame.getAllLegalMoves(nextPossibleState.getBoard(), opponent).size();
				int possibleMyPieces = OthelloGame.computeScore(nextPossibleState.getBoard(), player);
				if(enemyPossibleMove == 0){
					possibleScore = 50;
				}
				if(myScore < possibleScore + possibleMyPieces){
					myScore = possibleScore + possibleMyPieces;
				}
			}
			return myScore;
		}else{
			HashSet<Move> legalMoves = OthelloGame.getAllLegalMoves(state.getBoard(), state.getPlayer());
			for(Move move:legalMoves){
				OthelloState nextPossibleState = OthelloGame.transition(state, move);
				double moveScore = moveWeight[move.row()][move.col()];
				int enemyPossibleMove = OthelloGame.getAllLegalMoves(nextPossibleState.getBoard(), opponent).size();
				if(enemyPossibleMove == 0){
					enemyPossibleMove = -30;
				}
				int possibleMyPieces = OthelloGame.computeScore(nextPossibleState.getBoard(), player);
				int possibleEnemyPieces = OthelloGame.computeScore(nextPossibleState.getBoard(), opponent);
				if(myScore*1.5 + myPieces - possibleEnemyPieces * enemyLegalMoves / (freeSpaces / 4.0) < moveScore*1.5 + possibleMyPieces - possibleEnemyPieces * enemyPossibleMove / (freeSpaces / 4.0)){
					myScore = moveScore*1.5;
					myPieces = possibleMyPieces;
					enemyLegalMoves = enemyPossibleMove;
				}
			}
			return myScore + myPieces - enemyLegalMoves / (freeSpaces / 2.5);
		}
	}
	
}