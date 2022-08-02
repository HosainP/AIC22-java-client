package ir.sharif.aic.hideandseek.algorithms;

import ir.sharif.aic.hideandseek.protobuf.AIProto;

import java.util.ArrayList;
import java.util.List;

public class RandomMove {
    public static int getRandomNextNode(AIProto.Graph graph, AIProto.Agent agent, ArrayList<Integer> takenNodes, boolean repeated_nodes) {
        List<AIProto.Path> paths = graph.getPathsList();
        ArrayList<AIProto.Path> myPaths = new ArrayList<>();

        for (AIProto.Path path : paths) {
            if (path.getFirstNodeId() == agent.getNodeId()) {
                myPaths.add(path);
            } else if (path.getSecondNodeId() == agent.getNodeId()) {
                myPaths.add(path);
            }
        }

        int rand = new java.util.Random().nextInt(myPaths.size());
        AIProto.Path path = myPaths.get(rand);
        if (!repeated_nodes) { // if you want to go to a new node every time, this should execute.
            while (takenNodes.contains(path.getFirstNodeId()) || takenNodes.contains(path.getSecondNodeId())) {
                rand = new java.util.Random().nextInt(myPaths.size());
                path = myPaths.get(rand);
            }
        }

        if (agent.getNodeId() == path.getFirstNodeId())
            return path.getSecondNodeId();
        else
            return path.getFirstNodeId();
    }
}
