package nl.tue.s2id90.group77;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import nl.tue.s2id90.draughts.DraughtsState;
import nl.tue.s2id90.draughts.player.DraughtsPlayer;
import org10x10.dam.game.Move;

/**
 * Implementation of the DraughtsPlayer interface.
 *
 * @author huub
 */
// ToDo: rename this class (and hence this file) to have a distinct name
//       for your player during the tournament
public class ScythePlayer extends DraughtsPlayer {

    private int bestValue = 0;
    int maxSearchDepth;

    /**
     * boolean that indicates that the GUI asked the player to stop thinking.
     */
    private boolean stopped;

    public ScythePlayer(int maxSearchDepth) {
        super("best.png"); // ToDo: replace with your own icon
        this.maxSearchDepth = maxSearchDepth;
    }

    @Override
    public Move getMove(DraughtsState s) {
        Move bestMove = null;
        bestValue = 0;
        DraughtsNode node = new DraughtsNode(s);    // the root of the search tree
        try {
            // compute bestMove and bestValue in a call to alphabeta
            int depth = 0;
            while (true) {
                bestValue = alphaBeta(node, MIN_VALUE, MAX_VALUE, depth);
                depth++;
                bestMove = node.getBestMove();
            }

            // store the bestMove found uptil now
            // NB this is not done in case of an AIStoppedException in alphaBeta()
            // print the results for debugging reasons
        } catch (AIStoppedException ex) {
            /* nothing to do */        }

        if (bestMove == null) {
            System.err.println("no valid move found!");
            return getRandomValidMove(s);
        } else {
            return bestMove;
        }
    }

    /**
     * This method's return value is displayed in the AICompetition GUI.
     *
     * @return the value for the draughts state s as it is computed in a call to
     * getMove(s).
     */
    @Override
    public Integer getValue() {
        return bestValue;
    }

    /**
     * Tries to make alphabeta search stop. Search should be implemented such
     * that it throws an AIStoppedException when boolean stopped is set to true;
     *
     */
    @Override
    public void stop() {
        stopped = true;
    }

    /**
     * returns random valid move in state s, or null if no moves exist.
     */
    Move getRandomValidMove(DraughtsState s) {
        List<Move> moves = s.getMoves();
        Collections.shuffle(moves);
        return moves.isEmpty() ? null : moves.get(0);
    }

    /**
     * Implementation of alphabeta that automatically chooses the white player
     * as maximizing player and the black player as minimizing player.
     *
     * @param node contains DraughtsState and has field to which the best move
     * can be assigned.
     * @param alpha
     * @param beta
     * @param depth maximum recursion Depth
     * @return the computed value of this node
     * @throws AIStoppedException
     *
     */
    int alphaBeta(DraughtsNode node, int alpha, int beta, int depth)
            throws AIStoppedException {
        if (node.getState().isWhiteToMove()) {
            for (Move m : node.getState().getMoves()) {
                node.getState().doMove(m);
                int value = alphaBetaMax(node, alpha, beta, depth);
                if (value < beta) {
                    node.setBestMove(m);
                    beta = value;
                }
                node.getState().undoMove(m);
            }
            return beta;
        } else {
            for (Move m : node.getState().getMoves()) {
                node.getState().doMove(m);
                int value = alphaBetaMin(node, alpha, beta, depth);
                if (value > alpha) {
                    node.setBestMove(m);
                    alpha = value;
                }
                node.getState().undoMove(m);
            }
            return alpha;
        }
    }

    /**
     * Does an alphabeta computation with the given alpha and beta where the
     * player that is to move in node is the minimizing player.
     *
     * <p>
     * Typical pieces of code used in this method are:
     * <ul> <li><code>DraughtsState state = node.getState()</code>.</li>
     * <li><code> state.doMove(move); .... ; state.undoMove(move);</code></li>
     * <li><code>node.setBestMove(bestMove);</code></li>
     * <li><code>if(stopped) { stopped=false; throw new AIStoppedException(); }</code></li>
     * </ul>
     * </p>
     *
     * @param node contains DraughtsState and has field to which the best move
     * can be assigned.
     * @param alpha
     * @param beta
     * @param depth maximum recursion Depth
     * @return the compute value of this node
     * @throws AIStoppedException thrown whenever the boolean stopped has been
     * set to true.
     */
    int alphaBetaMin(DraughtsNode node, int alpha, int beta, int depth)
            throws AIStoppedException {
        if (stopped) {
            stopped = false;
            throw new AIStoppedException();
        }
        DraughtsState state = node.getState();
        // ToDo: write an alphabeta search to compute bestMove and value
        // Evaluate the state when depth is 0 or if it is an endstate.
        if (depth <= 0 || node.getState().isEndState()) {
            return evaluate(state);
        }
        for (Move m : state.getMoves()) {
            node.getState().doMove(m);
            int value = alphaBetaMax(node, alpha, beta, depth - 1);
            if (value < beta) {
                beta = value;
            }
            node.getState().undoMove(m);
            // Pruning.
            if (beta <= alpha) {
                return alpha;
            }
        }
        return beta;
    }

    int alphaBetaMax(DraughtsNode node, int alpha, int beta, int depth)
            throws AIStoppedException {
        if (stopped) {
            stopped = false;
            throw new AIStoppedException();
        }
        DraughtsState state = node.getState();
        // ToDo: write an alphabeta search to compute bestMove and value
        // Evaluate the state when depth is 0 or if it is an endstate.
        if (depth <= 0 || node.getState().isEndState()) {
            return evaluate(state);
        }
        for (Move m : state.getMoves()) {
            node.getState().doMove(m);
            int value = alphaBetaMin(node, alpha, beta, depth - 1);
            if (value > alpha) {
                alpha = value;
            }
            node.getState().undoMove(m);
            // Pruning.
            if (alpha >= beta) {
                return beta;
            }
        }
        return alpha;
    }

    /**
     * A method that evaluates the given state.
     */
    // ToDo: write an appropriate evaluation function
    int evaluate(DraughtsState state) {
        int score = 0;
        // The score from countPieces is given a multiplier of 5 as it is the
        // most basic rule to follow.
        score += (countPieces(state) * 5);
        // Controlling the center, strong formations and homeRow are not given
        // multipliers because they should not be able to override countPieces.
        score += controlCenter(state);
        score += fiveStoneSquare(state);
        score += homeRow(state);
        return score;
    }

    /**
     * An evaluation method that just counts pieces. Kings are given a higher
     * weight.
     *
     * @return the score from this evaluation method.
     */
    int countPieces(DraughtsState state) {
        int score = 0;
        int[] pieces = state.getPieces();
        // Go through each piece and evaluate its effect on the total score.
        for (int i = 1; i < pieces.length; i++) {
            if (pieces[i] == DraughtsState.WHITEPIECE) {
                score -= 1;
            } else if (pieces[i] == DraughtsState.WHITEKING) {
                score -= 3;
            } else if (pieces[i] == DraughtsState.BLACKKING) {
                score += 3;
            } else if (pieces[i] == DraughtsState.BLACKPIECE) {
                score += 1;
            }
        }
        return score;
    }

    /**
     * An evaluation method that gives weight to states where the center is
     * controlled.
     *
     * @return the score from this evaluation method.
     */
    int controlCenter(DraughtsState state) {
        int score = 0;
        int[] pieces = state.getPieces();
        // Go through the center of the board to give extra weight to pieces
        // occupying the center.
        for (int i = 21; i < 31; i++) {
            if (pieces[i] == DraughtsState.WHITEPIECE || pieces[i] == DraughtsState.WHITEKING) {
                score -= 1;
            } else if (pieces[i] == DraughtsState.BLACKKING || pieces[i] == DraughtsState.BLACKPIECE) {
                score += 1;
            }
        }
        return score;
    }

    /**
     * An evaluation method that gives weight to a strong formation where five pieces form a square.
     * How it looks: The representation of 5 on a dice.
     *
     * @return the score from this evaluation method.
     */
    int fiveStoneSquare(DraughtsState state) {
        int score = 0;
        int[] pieces = state.getPieces();
        // The first row is an odd row.
        boolean rowIsOdd = true;
        int rowPosition = 1;
        int distanceFromMiddle = 5;
        for (int i = 1; i < 41; i++) {
            if (i%5 == 0) {
                continue;
            }
            // When evaluating odd rows, the middle piece of the strong formation is 6 positions away from the first piece.
            // When evaluating even rows, the middle piece of the strong formation is 5 positions away from the first piece.
            if (rowIsOdd) {
                distanceFromMiddle = 6;
            } else {
                distanceFromMiddle = 5;
            }
            if ((pieces[i] == DraughtsState.BLACKPIECE) && (pieces[i + 1] == DraughtsState.BLACKPIECE)
                    && (pieces[i + distanceFromMiddle] == DraughtsState.BLACKPIECE) && (pieces[i + 10] == DraughtsState.BLACKPIECE) 
                    && (pieces[i + 11] == DraughtsState.BLACKPIECE)) {
                score += 2;
            } else if ((pieces[i] == DraughtsState.WHITEPIECE) && (pieces[i + 1] == DraughtsState.WHITEPIECE)
                    && (pieces[i + distanceFromMiddle] == DraughtsState.WHITEPIECE) && (pieces[i + 10] == DraughtsState.WHITEPIECE) 
                    && (pieces[i + 11] == DraughtsState.WHITEPIECE)) {
                score -= 2;
            }              
            rowPosition++;
            if (rowPosition == 4) {
                rowPosition = 1;
                rowIsOdd = !rowIsOdd;
            }
        }
        return score;
    }
    
    /**
     * An evaluation method that gives weight to keeping pieces in your home row.
     *
     * @return the score from this evaluation method.
     */
    private int homeRow(DraughtsState state) {
        int score = 0;
        int[] pieces = state.getPieces();
        for (int i = 1; i < 6; i++) {
            if (pieces[i] == DraughtsState.BLACKPIECE) {
                score += 1;
            }
        }
        for (int i = 46; i < 51; i++) {
            if (pieces[i] == DraughtsState.WHITEPIECE) {
               score -= 1;
            }
        }
        return score;
    }
}
