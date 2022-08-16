package ir.sharif.aic.hideandseek.ai;

import ir.sharif.aic.hideandseek.protobuf.AIProto;

import java.util.ArrayList;

public class Functions {

    /////////////////////////////////////////////// FUNCTIONS ///////////////////////////////////////////////

    static ArrayList<Integer> find_nodes_with_distance(AIProto.Graph graph, int source, int distance) {
        ArrayList<Integer> checked_nodes = new ArrayList<>();
        ArrayList<Integer> this_level_nodes = new ArrayList<>();
        ArrayList<Integer> next_level_nodes = new ArrayList<>();
        this_level_nodes.add(source); // the only use of 'source'. if the source is always 1, then it can simply be 1.

        for (int i = 0; i < distance; i++) {
            for (AIProto.Path path : graph.getPathsList()) {
                if (this_level_nodes.contains(path.getSecondNodeId()) && !checked_nodes.contains(path.getFirstNodeId())) {
                    next_level_nodes.add(path.getFirstNodeId());
                } else if (this_level_nodes.contains(path.getFirstNodeId()) && !checked_nodes.contains(path.getSecondNodeId())) {
                    next_level_nodes.add(path.getSecondNodeId());
                }
            }
            checked_nodes.addAll(this_level_nodes);
            this_level_nodes = next_level_nodes;
            next_level_nodes = new ArrayList<>();
        }
        return this_level_nodes; // return nodes with the distance of "distance" (price not calculated)
    }

    /////////////////////////////////////////////// FUNCTIONS ///////////////////////////////////////////////

    static ArrayList<Integer> find_nodes_with_distance_less_than(AIProto.Graph graph, int source, int distance) {
        ArrayList<Integer> checked_nodes = new ArrayList<>();
        ArrayList<Integer> this_level_nodes = new ArrayList<>();
        ArrayList<Integer> next_level_nodes = new ArrayList<>();
        this_level_nodes.add(source); // the only use of 'source'. if the source is always 1, then it can simply be 1.

        for (int i = 0; i < distance; i++) {
            for (AIProto.Path path : graph.getPathsList()) {
                if (this_level_nodes.contains(path.getSecondNodeId()) && !checked_nodes.contains(path.getFirstNodeId())) {
                    next_level_nodes.add(path.getFirstNodeId());
                } else if (this_level_nodes.contains(path.getFirstNodeId()) && !checked_nodes.contains(path.getSecondNodeId())) {
                    next_level_nodes.add(path.getSecondNodeId());
                }
            }
            checked_nodes.addAll(this_level_nodes);
            this_level_nodes = next_level_nodes;
            next_level_nodes = new ArrayList<>();
        }
        return checked_nodes; // return nodes with the distance of "distance" (price not calculated)
    }

    /////////////////////////////////////////////// FUNCTIONS ///////////////////////////////////////////////

}


