package ir.sharif.aic.hideandseek.ai;

import ir.sharif.aic.hideandseek.protobuf.AIProto;

import java.util.ArrayList;

public class HowManyTurns {
    public static int getAns(AIProto.Graph graph, int starting_node, ArrayList<Integer> path, double balance, double income) {
        int ans = 0;
        path.add(starting_node);

        ArrayList<Double> path_prices = new ArrayList<>();
        for (int i = path.size() - 2; i >= 0; i--) {
            for (AIProto.Path p : graph.getPathsList()) {
                if (p.getFirstNodeId() == path.get(i) && p.getSecondNodeId() == path.get(i + 1) ||
                        p.getSecondNodeId() == path.get(i) && p.getFirstNodeId() == path.get(i + 1)) { // if paths match
                    path_prices.add(p.getPrice());
                }
            }
        }
        int i = 0;
        while (!path_prices.isEmpty()) {
            if (balance > path_prices.get(0)) { // we can take this edge.
                balance = balance - path_prices.get(0);
                path_prices.remove(0);
            }
            balance = balance + income;
            i++;
        }
        return i;
    }
}
