package ir.sharif.aic.hideandseek.ai;

import ir.sharif.aic.hideandseek.client.Phone;
import ir.sharif.aic.hideandseek.protobuf.AIProto;
import ir.sharif.aic.hideandseek.protobuf.AIProto.GameView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ThiefAI extends AI {

    public ThiefAI(Phone phone) {
        this.phone = phone;
    }

    /**
     * Used to select the starting node.
     */
    @Override
    public int getStartingNode(GameView gameView) {
        AIProto.Graph graph = gameView.getConfig().getGraph();
        int size = graph.getNodesCount();
        return new Random().nextInt(size) + 1;
    }

    /**
     * Implement this function to move your thief agent based on current game view.
     */
//    @Override
//    public int move(GameView gameView) {
//        AIProto.Graph graph = gameView.getConfig().getGraph();
//        return gameView.getViewer().getNodeId();
//    }
    @Override
    public int move(GameView gameView) {

        if (gameView.getConfig().getTurnSettings().getVisibleTurnsList().contains(gameView.getTurn().getTurnNumber())) {

        }

        AIProto.Graph graph = gameView.getConfig().getGraph();
        List<AIProto.Path> paths = graph.getPathsList();
        ArrayList<AIProto.Path> myPaths = new ArrayList<>();

        for (AIProto.Path path : paths) {
            if (path.getFirstNodeId() == gameView.getViewer().getNodeId()) {
                myPaths.add(path);
            } else if (path.getSecondNodeId() == gameView.getViewer().getNodeId()) {
                myPaths.add(path);
            }
        }

        int rand = new Random().nextInt(myPaths.size());
        AIProto.Path path = myPaths.get(rand);
        if (gameView.getViewer().getNodeId() == path.getFirstNodeId())
            return path.getSecondNodeId();
        else
            return path.getFirstNodeId();
    } // MOVING COMPLETELY RANDOM.

}
