package ir.sharif.aic.hideandseek.ai;

import ir.sharif.aic.hideandseek.algorithms.CentralNode;
import ir.sharif.aic.hideandseek.algorithms.Dijkstra;
import ir.sharif.aic.hideandseek.algorithms.RandomMove;
import ir.sharif.aic.hideandseek.client.Phone;
import ir.sharif.aic.hideandseek.protobuf.AIProto;
import ir.sharif.aic.hideandseek.protobuf.AIProto.GameView;

import java.util.*;

public class PoliceAI extends AI {

    AIProto.Agent selected_thief; // we select a thieve "location" and follow it until we catch it.
    List<Integer> thieves_last_seen; // this is the array list of last places that thieves were seen.
    Stack<Integer> bfs_path; // this is the path we are going to take.
    ArrayList<AIProto.Agent> tracked_thieves; // i save details of enemy thieves her.
    List<MyPolice> my_polices;
    ArrayList<MyNode> all_nodes;
    ArrayList<Integer> taken_nodes; // this saves the nodes that we have been through.
                                    // works for random moving, so we don't go to a repeated
                                    // node again.

    public PoliceAI(Phone phone) {
        this.phone = phone;
        selected_thief = null;
        thieves_last_seen = null;
        bfs_path = null;
        taken_nodes = new ArrayList<>();
    }

    /**
     * This function always returns zero (Polices can not set their starting node).
     */
    @Override
    public int getStartingNode(GameView gameView) {
        List<AIProto.Agent> agents = gameView.getVisibleAgentsList();
        my_polices = new ArrayList<>();
        for (AIProto.Agent agent : agents) { // i have a list of all my polices in here. which are numbered as i want.
            if (agent.getTeam() == gameView.getViewer().getTeam() && agent.getType() == AIProto.AgentType.POLICE) {
                my_polices.add(new MyPolice(agent));
            }
        }
        my_polices.add(new MyPolice(gameView.getViewer())); // add this police to list of polices.

        my_polices.sort(Comparator.comparingInt(o -> o.police.getId()));
        for (int i = 1; i < my_polices.size() + 1; i++) {
            my_polices.get(i - 1).id = i;
        }

        all_nodes = new ArrayList<>();
        for (AIProto.Node node : gameView.getConfig().getGraph().getNodesList()) {
            all_nodes.add(new MyNode(gameView.getConfig().getGraph(), node));
        }
        all_nodes.sort(Comparator.comparingInt(o -> o.number_of_neighbors));
        // the one with the most neighbors is at the end of all_nodes ArrayList.
//        System.out.print("selected nodes = ");
//        for (MyNode myNode : all_nodes) {
//            System.out.print(myNode.node.getId() + ", ");
//        }
//        System.out.println();

        return 1;
    }

    /**
     * Implement this function to move your police agent based on current game view.
     */

//    @Override
//    public int move(GameView gameView) {
//        AIProto.Graph graph = gameView.getConfig().getGraph();
//        BFS bfs = new BFS();
//        if (bfs_path == null) { // if we have no path.
//            bfs_path = bfs.nextStep(graph, graph.getNodesCount(), gameView.getViewer().getNodeId(), 150); // 150 is temp
//            path_index = 0;
//        }
//        if (bfs_path != null && bfs_path.size() == path_index + 1) { // if the last path was finished.
//            bfs_path = bfs.nextStep(graph, graph.getNodesCount(), gameView.getViewer().getNodeId(), 150); // 150 is temp
//            path_index = 0;
//        }
//        path_index++;
//        if (bfs_path.elementAt(path_index) > 0)
//            return bfs_path.elementAt(path_index);
//        else
//            return gameView.getViewer().getNodeId();
//    } // BFS PATH (COMPLETELY BULLSHIT)


    @Override
    public int move(GameView gameView) {

        AIProto.GameConfig config = gameView.getConfig();
        List<Integer> visible_turns = config.getTurnSettings().getVisibleTurnsList();

        AIProto.Team myTeam = gameView.getViewer().getTeam(); // myTeam is the team i am in.

        if (visible_turns.contains(gameView.getTurn().getTurnNumber())) { // if this is the visible turn, i choose new thieves to follow.
            tracked_thieves = new ArrayList<>();
            for (AIProto.Agent agent : gameView.getVisibleAgentsList()) {
                if (agent.getTeam() != myTeam && agent.getType() == AIProto.AgentType.THIEF) {
                    tracked_thieves.add(agent); // now i have an array list of all enemy thieves.
                }
            }
        }

        AIProto.Graph graph = gameView.getConfig().getGraph();

        ArrayList<Integer> path;
        int next_node;

        if (tracked_thieves != null) { // we have seen thieves at least once.
            while (selected_thief == null || selected_thief.getIsDead() || selected_thief.getNodeId() == gameView.getViewer().getNodeId()) { // if we are not chasing any thief, we choose another one.
                int rand = new Random().nextInt(tracked_thieves.size());
                selected_thief = tracked_thieves.get(rand);
                // todo: if i go to a thief location and it is not there, i choose another thief to follow, even if the thief is still alive.
                // todo: selecting a thief is random, which should not be.
            }

            path = Dijkstra.findPath(graph, gameView.getViewer().getNodeId(), selected_thief.getNodeId());
            if (path.size() != 0) {
                next_node = path.get(path.size() - 1);
            } else {
                next_node = gameView.getViewer().getNodeId();
            }
        } else { // we have not seen thieves never yet.
//            next_node = CentralNode.findPath(my_polices, all_nodes, gameView); // this method finds some central nodes, and gives each police one of them.
            next_node = RandomMove.getRandomNextNode(gameView.getConfig().getGraph(), gameView.getViewer(), taken_nodes, false); // the "false" is to go to a new node every turn.
            taken_nodes.add(gameView.getViewer().getNodeId());
        }
        return next_node;
    }
}



