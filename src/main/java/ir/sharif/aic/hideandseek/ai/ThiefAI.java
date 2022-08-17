package ir.sharif.aic.hideandseek.ai;

import ir.sharif.aic.hideandseek.algorithms.Dijkstra;
import ir.sharif.aic.hideandseek.algorithms.Mode;
import ir.sharif.aic.hideandseek.client.Phone;
import ir.sharif.aic.hideandseek.protobuf.AIProto;
import ir.sharif.aic.hideandseek.protobuf.AIProto.GameView;

import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.random.RandomGenerator;

public class ThiefAI extends AI {

    // todo : //////////////////////////////////////////////////////////////////////////
    // todo : i should split thieves apart when they stick together.                  //
    // todo : i should consider if i can or cant pay the price of a path.             //
    // todo: i should modify starting points for thieves (probably central nodes.)    // DONE!
    // todo : //////////////////////////////////////////////////////////////////////////

    public ThiefAI(Phone phone) {
        this.phone = phone;
    }

    /**
     * Used to select the starting node.
     */
    @Override
    public int getStartingNode(GameView gameView) {
        ArrayList<Integer> not_in_danger_nodes = new ArrayList<>(); // getting all nodes in graph that are not in danger.
        for (AIProto.Node node : gameView.getConfig().getGraph().getNodesList()) {
            if (!in_danger(gameView, node.getId())) {
                not_in_danger_nodes.add(node.getId());
            }
        }
        not_in_danger_nodes.sort(Comparator.comparingInt(o -> number_of_neighbors(gameView, o))); // making arrayList of my thieves.
        ArrayList<AIProto.Agent> all_my_thieves = new ArrayList<>();
        for (AIProto.Agent agent : gameView.getVisibleAgentsList()) {
            if (agent.getType() == AIProto.AgentType.THIEF && agent.getTeam() == gameView.getViewer().getTeam()) {
                all_my_thieves.add(agent);
            }
        }
        all_my_thieves.add(gameView.getViewer()); // adding this thief to list of my thieves.

        all_my_thieves.sort(Comparator.comparingInt(AIProto.Agent::getId));
        for (int i = 0; i < all_my_thieves.size(); i++) {
            if (all_my_thieves.get(i).getId() == gameView.getViewer().getId()) {
                return not_in_danger_nodes.get(not_in_danger_nodes.size() - 1 - i);
            }
        }
        return 2; // not ever gonna reach.
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

        boolean inDanger = in_danger(gameView, gameView.getViewer().getNodeId());
        // todo: doesn't work properly thieves start moving when polices are only one node far.
        // todo:
        // todo:
        // todo:

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

            int target_node = gameView.getViewer().getNodeId();
            ArrayList<Integer> usable_neighbor_nodes = new ArrayList<>(); // this array list, has all neighbor nodes,
            for (int i : neighbor_nodes) {                                // that we can go to, and has not a police
                if (usable_node(gameView, i)) {                           // in neighboring.
                    usable_neighbor_nodes.add(i);
                }
            }

            usable_neighbor_nodes.sort(Comparator.comparingInt(o -> number_of_neighbors(gameView, o))); // sorting usable neighbors by the number of neighbors.

            ArrayList<Integer> most_neighbor_usable_nodes = new ArrayList<>();
            for (int i : usable_neighbor_nodes) { // keeping only the nodes that have the most neighbors.
                if (number_of_neighbors(gameView, i) == number_of_neighbors(gameView, usable_neighbor_nodes.get(usable_neighbor_nodes.size() - 1))) {
                    most_neighbor_usable_nodes.add(i);
                }
            }

            ArrayList<AIProto.Agent> enemy_polices = new ArrayList<>(); // making list of all enemy polices.
            for (AIProto.Agent agent : gameView.getVisibleAgentsList()) {
                if (agent.getTeam() != gameView.getViewer().getTeam() && agent.getType() == AIProto.AgentType.POLICE) {
                    enemy_polices.add(agent);
                }
            }

            most_neighbor_usable_nodes.sort(Comparator.comparingInt(o -> distance_to_nearest_police(gameView, enemy_polices, o)));
            // between all usable nodes with most neighbors, i choose the one that has the most distance from the nearest police.
            if (usable_neighbor_nodes.size() != 0) {
                return usable_neighbor_nodes.get(usable_neighbor_nodes.size() - 1);
            } else {
                return gameView.getViewer().getNodeId();
            }

        } else { // not in danger, so we stay in our place.
            return gameView.getViewer().getNodeId();
        }
    }

    /////////////////////////////////////////////// FUNCTIONS ///////////////////////////////////////////////

    int distance_to_nearest_police(GameView gameView, ArrayList<AIProto.Agent> enemy_polices, int source) {
        int distance = Integer.MAX_VALUE;

        for (AIProto.Agent agent : enemy_polices) {
            if (agent.getNodeId() == source) {
                return 0;
            }
            ArrayList<Integer> short_path = Dijkstra.findPath(gameView.getConfig().getGraph(), source, agent.getNodeId(), Mode.SHORT);
            if (short_path.size() < distance) {
                distance = short_path.size();
            }
        }

        return distance;
    }

    /////////////////////////////////////////////// FUNCTIONS ///////////////////////////////////////////////

    boolean usable_node(GameView gameView, int node_id) { // this function returns true, if there is no enemy police, in neighboring of a node.
        ArrayList<Integer> neighbors = neighbors(gameView, node_id);
        neighbors.add(node_id);

        for (AIProto.Agent enemy_police : gameView.getVisibleAgentsList()) {
            if (enemy_police.getTeam() != gameView.getViewer().getTeam() && enemy_police.getType() == AIProto.AgentType.POLICE && neighbors.contains(enemy_police.getNodeId())) {
                return false;
            }
        }
        return true;
    }

    /////////////////////////////////////////////// FUNCTIONS ///////////////////////////////////////////////

    int number_of_neighbors(GameView gameView, int node_id) {
        ArrayList<Integer> neighbors = neighbors(gameView, node_id);
        return neighbors.size();
    }

    /////////////////////////////////////////////// FUNCTIONS ///////////////////////////////////////////////

    ArrayList<Integer> neighbors(GameView gameView, int node_id) {
        ArrayList<Integer> neighbors = new ArrayList<>();
        for (AIProto.Path path : gameView.getConfig().getGraph().getPathsList()) {
            if (path.getSecondNodeId() == node_id) {
                neighbors.add(path.getFirstNodeId());
            } else if (path.getFirstNodeId() == node_id) {
                neighbors.add(path.getSecondNodeId());
            }
        }

        return neighbors;
    }

    /////////////////////////////////////////////// FUNCTIONS ///////////////////////////////////////////////

    boolean in_danger(GameView gameView, int node_id) {
        boolean inDanger = false;
        ArrayList<Integer> near_nodes = new ArrayList<>();
        near_nodes.addAll(Functions.find_nodes_with_distance(gameView.getConfig().getGraph(), node_id, 1));
        near_nodes.addAll(Functions.find_nodes_with_distance(gameView.getConfig().getGraph(), node_id, 2));
        near_nodes.addAll(Functions.find_nodes_with_distance(gameView.getConfig().getGraph(), node_id, 3));
        near_nodes.addAll(Functions.find_nodes_with_distance(gameView.getConfig().getGraph(), node_id, 4));
//        near_nodes.addAll(Functions.find_nodes_with_distance(gameView.getConfig().getGraph(), gameView.getViewer().getNodeId(), 5));

        ArrayList<AIProto.Agent> enemy_polices = new ArrayList<>();
        for (AIProto.Agent agent : gameView.getVisibleAgentsList()) {
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

        return inDanger;
    }

    /////////////////////////////////////////////// FUNCTIONS ///////////////////////////////////////////////
}
