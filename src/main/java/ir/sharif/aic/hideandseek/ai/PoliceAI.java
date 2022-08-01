package ir.sharif.aic.hideandseek.ai;

import ir.sharif.aic.hideandseek.algorithms.BFS;
import ir.sharif.aic.hideandseek.algorithms.Dijkstra;
import ir.sharif.aic.hideandseek.client.Phone;
import ir.sharif.aic.hideandseek.protobuf.AIProto;
import ir.sharif.aic.hideandseek.protobuf.AIProto.GameView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

public class PoliceAI extends AI {

    AIProto.Agent selected_thief; // we select a thieve "location" and follow it until we catch it.
    List<Integer> thieves_last_seen; // this is the array list of last places that thieves were seen.
    Stack<Integer> bfs_path; // this is the path we are going to take.
    ArrayList<AIProto.Agent> tracked_thieves; // i save details of enemy thieves her.
    int path_index;

    public PoliceAI(Phone phone) {
        this.phone = phone;
        selected_thief = null;
        thieves_last_seen = null;
        bfs_path = null;
    }

    /**
     * This function always returns zero (Polices can not set their starting node).
     */
    @Override
    public int getStartingNode(GameView gameView) {
        return 1;
    }

    /**
     * Implement this function to move your police agent based on current game view.
     */

//    @Override
//    public int move(GameView gameView) {
//
//        System.out.println("MOVE METHOD IN POLICE CALLED.");
//
//        AIProto.GameConfig config = gameView.getConfig();
//        List<Integer> visible_turns =  config.getTurnSettings().getVisibleTurnsList();
//
//        AIProto.Team myTeam = AIProto.Team.FIRST; // "FIRST" is temporary.
//        if (!visible_turns.contains(gameView.getTurn().getTurnNumber())) {
//            for (AIProto.Agent agent : gameView.getVisibleAgentsList()) {
//                if (agent.getType() == AIProto.AgentType.THIEF) {
//                    myTeam = agent.getTeam();    // here i find out which team i am in.
//                    break;
//                }
//            }
//        }
//
//        if (visible_turns.contains(gameView.getTurn().getTurnNumber())) {
//            thieves_last_seen = new ArrayList<>();
//            for (AIProto.Agent agent : gameView.getVisibleAgentsList()) {
//                if (!agent.getIsDead() && agent.getTeam() != myTeam && agent.getType() == AIProto.AgentType.THIEF) {
//                    thieves_last_seen.add(agent.getNodeId()); // now i have an array list of nodes of all enemy thieves.
//                }
//            }
//        }
//
//        AIProto.Graph graph = gameView.getConfig().getGraph();
//
//        int next_node = 1;
//        if (thieves_last_seen != null) { // we have seen thieves at least once.
//            System.out.println("i want to use dijkstra.");
//            if (selected_thief == null) { // if we are not chasing any thief, we choose another one.
//                int rand = new Random().nextInt(thieves_last_seen.size());
//                selected_thief = thieves_last_seen.get(rand);
//            }
//            System.out.println("i have selected a thief to follow.");
//            next_node = new Dijkstra().nextStep(graph, gameView.getViewer().getNodeId(), selected_thief);
//
//            System.out.println();
//            System.out.println("i used dijkstra");
//            System.out.println();
//        } else { // we have not seen thieves never yet.
//            next_node = gameView.getViewer().getNodeId(); // we stay in our place . // todo
//            System.out.println();
//            System.out.println("we didn't use dijkstra");
//            System.out.println();
//        }
//
//        return next_node;
//    } // DIJKSTRA


//    @Override
//    public int move(GameView gameView){
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

//        AIProto.Graph graph = gameView.getConfig().getGraph();
//        ArrayList<Integer> path = Dijkstra.findPath(graph, gameView.getViewer().getNodeId(), 150);
//        System.out.println(path);
//        return path.get(path.size() - 1);


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

        ArrayList<Integer> path = new ArrayList<>();
        int next_node;
        if (tracked_thieves != null) { // we have seen thieves at least once.
            System.out.println("i want to use dijkstra.");
            while (selected_thief == null || selected_thief.getIsDead() || selected_thief.getNodeId() == gameView.getViewer().getNodeId()) { // if we are not chasing any thief, we choose another one.
                int rand = new Random().nextInt(tracked_thieves.size()); // todo "while performance is not good."
                selected_thief = tracked_thieves.get(rand);
                System.out.println("i have selected a new thief to follow.");
            }

            path = Dijkstra.findPath(graph, gameView.getViewer().getNodeId(), selected_thief.getNodeId());
            if (path.size() != 0) {
                next_node = path.get(path.size() - 1);
            } else {
                next_node = gameView.getViewer().getNodeId();
            }
            System.out.println("i used dijkstra");
        } else { // we have not seen thieves never yet.
            next_node = gameView.getViewer().getNodeId(); // we stay in our place . // todo
            System.out.println("i didn't use dijkstra");
        }

        return next_node;
    }
}

