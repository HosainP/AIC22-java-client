package ir.sharif.aic.hideandseek.ai;

import ir.sharif.aic.hideandseek.algorithms.Dijkstra;
import ir.sharif.aic.hideandseek.algorithms.Mode;
import ir.sharif.aic.hideandseek.client.Phone;
import ir.sharif.aic.hideandseek.protobuf.AIProto;
import ir.sharif.aic.hideandseek.protobuf.AIProto.GameView;

import java.util.ArrayList;
import java.util.Comparator;

public class ThiefAI extends AI {

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
        ArrayList<AIProto.Agent> all_my_thieves = get_my_thieves(gameView);

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

    @Override
    public int move(GameView gameView) {

        boolean inDanger = in_danger(gameView, gameView.getViewer().getNodeId());
        // todo: doesn't work properly thieves start moving when polices are only one node far.

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

            ArrayList<Integer> usable_neighbor_nodes = new ArrayList<>(); // this array list, has all neighbor nodes,
            for (int i : neighbor_nodes) {                                // that we can go to, and has not a police
                if (usable_node(gameView, i)) {                           // in neighboring.
                    usable_neighbor_nodes.add(i);
                }
            }

            ArrayList<Integer> temp_neighbors = new ArrayList<>(); // deleting the paths that i can't afford to take.
            for (int neighbor : usable_neighbor_nodes) {
                if (can_afford_path(gameView, gameView.getViewer().getNodeId(), neighbor)) {
                    temp_neighbors.add(neighbor);
                }
            }
            usable_neighbor_nodes = temp_neighbors;
            usable_neighbor_nodes.sort(Comparator.comparingInt(o -> number_of_neighbors(gameView, o))); // sorting usable neighbors by the number of neighbors.

            ArrayList<AIProto.Agent> my_thieves = get_my_thieves(gameView);
            ArrayList<AIProto.Agent> my_thieves_in_my_node = new ArrayList<>();
            for (AIProto.Agent agent : my_thieves) {
                if (agent.getNodeId() == gameView.getViewer().getNodeId()) {
                    my_thieves_in_my_node.add(agent);
                }
            }
            my_thieves_in_my_node.sort(Comparator.comparingInt(AIProto.Agent::getId));

            for (int i = 0; i < my_thieves_in_my_node.size(); i++) { // if there are more than one thief in this node, they will separate.
                if (my_thieves_in_my_node.get(i).getId() == gameView.getViewer().getId()) {
                    if (usable_neighbor_nodes.size() - 1 - i >= 0) {
                        return usable_neighbor_nodes.get((usable_neighbor_nodes.size() - 1 - i));
                    } else {
                        if (usable_neighbor_nodes.size() != 0) {
                            return usable_neighbor_nodes.get(usable_neighbor_nodes.size() - 1); // if there might be an error, i go to neighbor with most neighbors.
                        } else {
                            System.out.println("god they can get me.");
                            ArrayList<Integer> neighbors = neighbors(gameView, gameView.getViewer().getNodeId());
                            for (int neighbor : neighbors) {
                                if (!is_filled_with_enemy_police(gameView, neighbor)) {
                                    return neighbor; // still moving to a neighbor even if i might get caught.
                                }
                            }
                        }
                    }
                }
            }
            return gameView.getViewer().getNodeId(); // there is nowhere i can go.
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
        ArrayList<Integer> near_nodes = new ArrayList<>();
        near_nodes.addAll(Functions.find_nodes_with_distance(gameView.getConfig().getGraph(), node_id, 1));
        near_nodes.addAll(Functions.find_nodes_with_distance(gameView.getConfig().getGraph(), node_id, 2));
        near_nodes.addAll(Functions.find_nodes_with_distance(gameView.getConfig().getGraph(), node_id, 3));
        near_nodes.addAll(Functions.find_nodes_with_distance(gameView.getConfig().getGraph(), node_id, 4));
//        near_nodes.addAll(Functions.find_nodes_with_distance(gameView.getConfig().getGraph(), gameView.getViewer().getNodeId(), 5));

        ArrayList<AIProto.Agent> enemy_polices = get_enemy_polices(gameView);

        for (AIProto.Agent enemy : enemy_polices) {
            if (near_nodes.contains(enemy.getNodeId())) {
                return true; // there is a police near us. we are in danger.
            }
        }

        return false;
    }

    /////////////////////////////////////////////// FUNCTIONS ///////////////////////////////////////////////

    ArrayList<AIProto.Agent> get_enemy_polices(GameView gameView) { // this function returns an arrayList of enemy polices.
        ArrayList<AIProto.Agent> enemy_polices = new ArrayList<>();
        for (AIProto.Agent agent : gameView.getVisibleAgentsList()) {
            if (agent.getTeam() != gameView.getViewer().getTeam() && agent.getType() == AIProto.AgentType.POLICE) {
                enemy_polices.add(agent);
            }
        }
        return enemy_polices;
    }

    /////////////////////////////////////////////// FUNCTIONS ///////////////////////////////////////////////

    ArrayList<AIProto.Agent> get_my_thieves(GameView gameView) { // this function returns an arrayList of my thieves.
        ArrayList<AIProto.Agent> my_thieves = new ArrayList<>();
        for (AIProto.Agent agent : gameView.getVisibleAgentsList()) {
            if (agent.getTeam() == gameView.getViewer().getTeam() && agent.getType() == AIProto.AgentType.THIEF) {
                my_thieves.add(agent);
            }
        }
        my_thieves.add(gameView.getViewer());
        return my_thieves;
    }

    /////////////////////////////////////////////// FUNCTIONS ///////////////////////////////////////////////

    boolean is_filled_with_enemy_police(GameView gameView, int node_id) { // this function returns true if there is an enemy police in node_id.
        ArrayList<AIProto.Agent> enemy_police = get_enemy_polices(gameView);
        for (AIProto.Agent agent : enemy_police) {
            if (agent.getNodeId() == node_id)
                return true;
        }
        return false;
    }

    /////////////////////////////////////////////// FUNCTIONS ///////////////////////////////////////////////

    boolean can_afford_path(GameView gameView, int src, int dest) {
        double balance = gameView.getBalance();
        double path_price = 0;
        for (AIProto.Path path : gameView.getConfig().getGraph().getPathsList()) {
            if ((path.getFirstNodeId() == src && path.getSecondNodeId() == dest) || (path.getSecondNodeId() == src && path.getFirstNodeId() == dest)) {
                path_price = path.getPrice();
            }
        }
        return balance >= path_price;
    }

    /////////////////////////////////////////////// FUNCTIONS ///////////////////////////////////////////////

}
