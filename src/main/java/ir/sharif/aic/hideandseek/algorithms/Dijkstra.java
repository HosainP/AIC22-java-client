package ir.sharif.aic.hideandseek.algorithms;

import ir.sharif.aic.hideandseek.protobuf.AIProto;

import java.util.*;

public class Dijkstra {

    public static ArrayList<Integer> findPath(AIProto.Graph graph, int src, int dest, Mode mode) {
        Node[] nodes = new Node[graph.getNodesCount() + 1];
        ArrayList<Node> finished_nodes = new ArrayList<>();
        ArrayList<Node> unfinished_nodes = new ArrayList<>();

        for (int i = 1; i < graph.getNodesCount() + 1; i++) { // creat graph nodes.
            nodes[i] = ((i == src) ? new Node(i, 0) : new Node(i, Double.MAX_VALUE));
            unfinished_nodes.add(nodes[i]);
        }

        for (AIProto.Path path : graph.getPathsList()) { // creat graph edges.
            int first = path.getFirstNodeId();
            int second = path.getSecondNodeId();

            nodes[first].neighbors.add(new Neighbor(nodes[second], mode == Mode.CHEAP ? path.getPrice() : 1)); // todo path.getPrice();
            nodes[second].neighbors.add(new Neighbor(nodes[first], mode == Mode.CHEAP ? path.getPrice() : 1));
        } // the graph is complete.

        while (!finished_nodes.contains(nodes[dest])) {
            unfinished_nodes.sort((Comparator.comparingDouble(o -> o.value)));

            for (Neighbor neighbor : unfinished_nodes.get(0).neighbors) { // todo "java.lang.IndexOutOfBoundsException: Index 0 out of bounds for length 0"
                if (!finished_nodes.contains(neighbor.self)) {
                    if (neighbor.self.value > unfinished_nodes.get(0).value + neighbor.weight) {
                        neighbor.self.value = unfinished_nodes.get(0).value + neighbor.weight;
                        int id = neighbor.self.id;
                        nodes[id].before = unfinished_nodes.get(0);
//                        System.out.println("printing before here: " + nodes[id].before.id);
                    }
                }
            }

            finished_nodes.add(unfinished_nodes.get(0));
            unfinished_nodes.remove(0);
        } // now we have nodes[dest] in the finished nodes.
//        System.out.println("finished nodes = ");
//        for (Node node : finished_nodes) {
//            System.out.println("id: " + node.id + ", before: " + node.before);
//        }
        ArrayList<Integer> reversed_path = new ArrayList<>();
        Node temp = nodes[dest];
        while (temp.before != null) {
            reversed_path.add(temp.id);
            temp = temp.before;
        } // now we have our path but in reversed order.

        return reversed_path;
    }
}

class Node {
    int id;
    //////// these are for dijkstra queue.
    double value;
    Node before;
    ////////

    ArrayList<Neighbor> neighbors;

    Node(int id, double value) {
        this.id = id;
        this.value = value;
        neighbors = new ArrayList<>();
    }

    double getValue() {
        return this.value;
    }
}

class Neighbor {
    Node self;
    double weight;

    Neighbor(Node self, double weight) {
        this.self = self;
        this.weight = weight;
    }
}

