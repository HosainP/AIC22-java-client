package ir.sharif.aic.hideandseek.algorithms;

import ir.sharif.aic.hideandseek.ai.MyNode;
import ir.sharif.aic.hideandseek.ai.MyPolice;
import ir.sharif.aic.hideandseek.protobuf.AIProto;

import java.util.ArrayList;
import java.util.List;


public class CentralNode {
    public static int findPath(List<MyPolice> my_polices, ArrayList<MyNode> all_nodes, AIProto.GameView gameView) {

//            ArrayList<Integer> temp = new ArrayList();
//            ArrayList<Integer> fakeIds = new ArrayList<>();
//            for (MyPolice myPolice : my_polices) {
//                temp.add(myPolice.police.getId());
//                fakeIds.add(myPolice.id);
//            }
//            System.out.println("i am " + gameView.getViewer().getTeam() + " " + gameView.getViewer().getType() + " " + gameView.getViewer().getId() + " and i see " + temp + "fake ids " + fakeIds);

        ArrayList<Integer> path = new ArrayList<>();
        int next_node = gameView.getViewer().getNodeId();
        for (int i = 0; i < my_polices.size(); i++) { // for every police id
            if (gameView.getViewer().getId() == my_polices.get(i).police.getId()) {
                int destination = all_nodes.get(all_nodes.size() - my_polices.get(i).id).node.getId();
                path = Dijkstra.findPath(gameView.getConfig().getGraph(), gameView.getViewer().getNodeId(), destination, Mode.CHEAP);
                next_node = path.get(path.size() - 1);
//                System.out.println("i am " + gameView.getViewer().getId() + "and i am going to " + next_node + "my destination is " + destination);
                break;
            }
        }
        return next_node;
    }
}
