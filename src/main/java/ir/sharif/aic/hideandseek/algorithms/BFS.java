package ir.sharif.aic.hideandseek.algorithms;

import ir.sharif.aic.hideandseek.protobuf.AIProto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

// A class to store a graph edge
class Edge2 {
    public final int source, dest;

    private Edge2(int source, int dest) {
        this.source = source;
        this.dest = dest;
    }

    // Factory method for creating an immutable instance of `Edge`
    public static Edge2 of(int a, int b) {
        return new Edge2(a, b);        // calls private constructor
    }
}

// A class to represent a graph object
class Graph2 {
    // A list of lists to represent an adjacency list
    List<List<Integer>> adjList = null;

    // Constructor
    Graph2(List<Edge2> edges, int n) {
        adjList = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            adjList.add(new ArrayList<>());
        }

        // add edges to the directed graph
        for (Edge2 edge : edges) {
            adjList.get(edge.source).add(edge.dest);
        }
    }
}

class Main {
    // Function to perform DFS traversal in a directed graph to find the
    // complete path between source and destination vertices
    public static boolean isReachable(Graph2 graph, int src, int dest,
                                      boolean[] discovered, Stack<Integer> path) {
        // mark the current node as discovered
        discovered[src] = true;

        // include the current node in the path
        path.add(src);

        // if destination vertex is found
        if (src == dest) {
            return true;
        }

        // do for every edge (src, i)
        for (int i : graph.adjList.get(src)) {
            // if `u` is not yet discovered
            if (!discovered[i]) {
                // return true if the destination is found
                if (isReachable(graph, i, dest, discovered, path)) {
                    return true;
                }
            }
        }

        // backtrack: remove the current node from the path
        path.pop();

        // return false if destination vertex is not reachable from src
        return false;
    }

//    public static void main(String[] args) {
//        // List of graph edges as per the above diagram
//        List<Edge2> edges = Arrays.asList(
//                Edge2.of(0, 3), Edge2.of(1, 0), Edge2.of(1, 2), Edge2.of(1, 4),
//                Edge2.of(2, 7), Edge2.of(3, 4), Edge2.of(3, 5), Edge2.of(4, 3),
//                Edge2.of(4, 6), Edge2.of(5, 6), Edge2.of(6, 7));
//
//        // total number of nodes in the graph (labeled from 0 to 7)
//        int n = 8;
//
//        // build a graph from the given edges
//        Graph2 graph = new Graph2(edges, n);
//
//        // to keep track of whether a vertex is discovered or not
//        boolean[] discovered = new boolean[n];
//
//        // source and destination vertex
//        int src = 0, dest = 7;
//
//        // To store the complete path between source and destination
//        Stack<Integer> path = new Stack<>();
//
//        // perform DFS traversal from the source vertex to check the connectivity
//        // and store path from the source vertex to the destination vertex
//        if (isReachable(graph, src, dest, discovered, path)) {
//            System.out.println("Path exists from vertex " + src + " to vertex " + dest);
//            System.out.println("The complete path is " + path);
//            System.out.println(path.elementAt(1)); // this is what i want.
//        } else {
//            System.out.println("No path exists between vertices " + src +
//                    " and " + dest);
//        }
//    }
}


public class BFS {
    public Stack<Integer> nextStep(AIProto.Graph graph, int n, int src, int dest) {
        List<Edge2> edges = new ArrayList<>();

        List<AIProto.Path> pathsInAIProto = graph.getPathsList();
        for (AIProto.Path path : pathsInAIProto) {
            edges.add(Edge2.of(path.getFirstNodeId(), path.getSecondNodeId()));
            edges.add(Edge2.of(path.getSecondNodeId(), path.getFirstNodeId()));
        }

        Graph2 graph2 = new Graph2(edges, n + 1);

        boolean[] discovered = new boolean[n + 1];

        Stack<Integer> path = new Stack<>();

        Main.isReachable(graph2, src, dest, discovered, path);

//        if (Main.isReachable(graph2, src, dest, discovered, path)) {
//            System.out.println("Path exists from vertex " + src + " to vertex " + dest);
//            System.out.println("The complete path is " + path);
////            return (path.elementAt(1)); // this is what i want.
//        } else {
//            System.out.println("No path exists between vertices " + src +
//                    " and " + dest);
//        }
        return path;
    }
}
