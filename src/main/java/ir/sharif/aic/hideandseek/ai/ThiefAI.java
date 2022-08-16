package ir.sharif.aic.hideandseek.ai;

import ir.sharif.aic.hideandseek.algorithms.Dijkstra;
import ir.sharif.aic.hideandseek.algorithms.Mode;
import ir.sharif.aic.hideandseek.client.Phone;
import ir.sharif.aic.hideandseek.protobuf.AIProto;
import ir.sharif.aic.hideandseek.protobuf.AIProto.GameView;

import java.net.Inet4Address;
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
//
//        AIProto.Graph graph = gameView.getConfig().getGraph();
//        List<AIProto.Path> paths = graph.getPathsList();
//        ArrayList<AIProto.Path> myPaths = new ArrayList<>();
//
//        for (AIProto.Path path : paths) {
//            if (path.getFirstNodeId() == gameView.getViewer().getNodeId()) {
//                myPaths.add(path);
//            } else if (path.getSecondNodeId() == gameView.getViewer().getNodeId()) {
//                myPaths.add(path);
//            }
//        }
//
//        int rand = new Random().nextInt(myPaths.size());
//        AIProto.Path path = myPaths.get(rand);
//        if (gameView.getViewer().getNodeId() == path.getFirstNodeId())
//            return path.getSecondNodeId();
//        else
//            return path.getFirstNodeId();
//    } // MOVING COMPLETELY RANDOM.
    @Override
    public int move(GameView gameView) {

        boolean inDanger = false;
        ArrayList<Integer> near_nodes = new ArrayList<>();
        near_nodes.addAll(Functions.find_nodes_with_distance(gameView.getConfig().getGraph(), gameView.getViewer().getNodeId(), 1));
        near_nodes.addAll(Functions.find_nodes_with_distance(gameView.getConfig().getGraph(), gameView.getViewer().getNodeId(), 2));
        near_nodes.addAll(Functions.find_nodes_with_distance(gameView.getConfig().getGraph(), gameView.getViewer().getNodeId(), 3));
        near_nodes.addAll(Functions.find_nodes_with_distance(gameView.getConfig().getGraph(), gameView.getViewer().getNodeId(), 4));
//        near_nodes.addAll(Functions.find_nodes_with_distance(gameView.getConfig().getGraph(), gameView.getViewer().getNodeId(), 5));

        List<AIProto.Agent> agents = gameView.getVisibleAgentsList();
        ArrayList<AIProto.Agent> enemy_polices = new ArrayList<>();
        for (AIProto.Agent agent : agents) {
            if (agent.getTeam() != gameView.getViewer().getTeam() && agent.getType() == AIProto.AgentType.POLICE) {
                enemy_polices.add(agent);
            }
        }

        for (AIProto.Agent enemy : enemy_polices) {
            if (near_nodes.contains(enemy.getNodeId())) {
                inDanger = true; // there is a police near us. we are in danger.
                break;
            }
        }

        if (inDanger) { // we should do something if we are in danger.
            ArrayList<Integer> neighbor_nodes = new ArrayList<>(); // array list for all the nodes we can be in, in the next turn.
            for (AIProto.Path path : gameView.getConfig().getGraph().getPathsList()) { // adding all neighbors to the list.
                if (path.getFirstNodeId() == gameView.getViewer().getNodeId()) {
                    neighbor_nodes.add(path.getSecondNodeId());
                } else if (path.getSecondNodeId() == gameView.getViewer().getNodeId()) {
                    neighbor_nodes.add(path.getFirstNodeId());
                }
            }
            neighbor_nodes.add(gameView.getViewer().getNodeId()); // adding this node to the list, cause we can stay  in our place.

            int final_distance = 0;
            int target_node = gameView.getViewer().getNodeId();
            for (int n : neighbor_nodes) {
                int temp_distance = distance_to_nearest_police(gameView, enemy_polices, n);
                if (temp_distance > final_distance) {
                    target_node = n;
                    final_distance = temp_distance;
                }
            }
            return target_node;
        } else { // not in danger, so we stay.
            return gameView.getViewer().getNodeId();
        }
    }

    /////////////////////////////////////////////// FUNCTIONS ///////////////////////////////////////////////

    int distance_to_nearest_police(GameView gameView, ArrayList<AIProto.Agent> enemy_polices, int source) {
        int distance = Integer.MAX_VALUE;

        for (AIProto.Agent agent : enemy_polices) {
            ArrayList<Integer> short_path = Dijkstra.findPath(gameView.getConfig().getGraph(), source, agent.getNodeId(), Mode.SHORT);
            if (short_path.size() < distance) {
                distance = short_path.size();
            }
        }

        return distance;
    }

    /////////////////////////////////////////////// FUNCTIONS ///////////////////////////////////////////////

}
