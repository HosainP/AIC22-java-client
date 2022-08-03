package ir.sharif.aic.hideandseek.ai;

import com.fasterxml.jackson.annotation.JsonCreator;
import ir.sharif.aic.hideandseek.algorithms.CentralNode;
import ir.sharif.aic.hideandseek.algorithms.Dijkstra;
import ir.sharif.aic.hideandseek.algorithms.Mode;
import ir.sharif.aic.hideandseek.algorithms.RandomMove;
import ir.sharif.aic.hideandseek.client.Phone;
import ir.sharif.aic.hideandseek.protobuf.AIProto;
import ir.sharif.aic.hideandseek.protobuf.AIProto.GameView;

import java.lang.reflect.Array;
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

        /////////////////////////////////  printing "MONEY DETAILS"
//        System.out.println("first money: " + gameView.getBalance());
//        System.out.println("police each turn income: " + gameView.getConfig().getIncomeSettings().getPoliceIncomeEachTurn());
//        System.out.println("thief each turn income: " + gameView.getConfig().getIncomeSettings().getThievesIncomeEachTurn());
//        List<AIProto.Path> pathpath = gameView.getConfig().getGraph().getPathsList();
//        ArrayList<Double> prices = new ArrayList<>();
//        for(AIProto.Path pathPath : pathpath){
//            if (!prices.contains(pathPath.getPrice())){
//                prices.add(pathPath.getPrice());
//            }
//        }
//        System.out.println("paths price: " + prices);
        /////////////////////////////////

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

        ArrayList<Integer> cheap_path;
        ArrayList<Integer> short_path;
        int next_node;

        if (tracked_thieves != null) { // we have seen thieves at least once.
            while (selected_thief == null || selected_thief.getIsDead() || selected_thief.getNodeId() == gameView.getViewer().getNodeId()) { // if we are not chasing any thief, we choose another one.
                int rand = new Random().nextInt(tracked_thieves.size());
                selected_thief = tracked_thieves.get(rand);
                // todo: if i go to a thief location and it is not there, i choose another thief to follow, even if the thief is still alive.
                // todo: selecting a thief is random, which should not be. probably i should choose a thief by is distance from polices.
            }

            cheap_path = Dijkstra.findPath(graph, gameView.getViewer().getNodeId(), selected_thief.getNodeId(), Mode.CHEAP);
            int cheap_turns = HowManyTurns.getAns(graph, gameView.getViewer().getNodeId(), cheap_path, gameView.getBalance(), gameView.getConfig().getIncomeSettings().getPoliceIncomeEachTurn());
            short_path = Dijkstra.findPath(graph, gameView.getViewer().getNodeId(), selected_thief.getNodeId(), Mode.SHORT);
            int short_turns = HowManyTurns.getAns(graph, gameView.getViewer().getNodeId(), short_path, gameView.getBalance(), gameView.getConfig().getIncomeSettings().getPoliceIncomeEachTurn());
            if (cheap_path.size() != 0) { // there is a way to get there.
                if (cheap_turns <= short_turns)
                    next_node = cheap_path.get(cheap_path.size() - 2);
                else
                    next_node = short_path.get(short_path.size() - 2);
            } else {
                next_node = gameView.getViewer().getNodeId();
            }
//            System.out.println("path: " + cheap_path);
            System.out.println("cheap path from \"" + gameView.getViewer().getNodeId() + "\" to \"" + selected_thief.getNodeId() + "\" is " + cheap_path + " - turns taking: " + cheap_turns);
            System.out.println("short path from \"" + gameView.getViewer().getNodeId() + "\" to \"" + selected_thief.getNodeId() + "\" is " + short_path + " - turns taking: " + short_turns);
        } else { // we have not seen thieves never yet.
//            next_node = CentralNode.findPath(my_polices, all_nodes, gameView); // this method finds some central nodes, and gives each police one of them.
            next_node = RandomMove.getRandomNextNode(gameView.getConfig().getGraph(), gameView.getViewer(), taken_nodes, false); // the "false" is to go to a new node every turn.
            taken_nodes.add(gameView.getViewer().getNodeId());
        }
        return next_node;
    }
}



