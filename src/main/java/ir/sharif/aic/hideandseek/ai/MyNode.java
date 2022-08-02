package ir.sharif.aic.hideandseek.ai;

import ir.sharif.aic.hideandseek.protobuf.AIProto;

public class MyNode {
    public AIProto.Node node;
    int number_of_neighbors;

    MyNode(AIProto.Graph graph, AIProto.Node node) {
        this.node = node;
        this.number_of_neighbors = 0;
        for (AIProto.Path path : graph.getPathsList()) {
            if (path.getSecondNodeId() == node.getId() || path.getFirstNodeId() == node.getId()) {
                number_of_neighbors++;
            }
        }
    }
}
