package ir.sharif.aic.hideandseek.ai;

import ir.sharif.aic.hideandseek.algorithms.Dijkstra;
import ir.sharif.aic.hideandseek.algorithms.Mode;
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
    ArrayList<Integer> furthest_nodes;

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

//        print_money_details(gameView);
//        System.out.println("visible turns: " + gameView.getConfig().getTurnSettings().getVisibleTurnsList());

        my_polices = make_my_polices_list(gameView.getVisibleAgentsList(), gameView.getViewer()); // making list of my polices
        my_polices = put_first_destinations(gameView, my_polices); // giving each police a first destination to be before the first visible turn.

        return 1;
    }

    /**
     * Implement this function to move your police agent based on current game view.
     */


    @Override
    public int move(GameView gameView) {

        updateThieves(gameView); // update thieves list in each turn, if it's a visible turn it will update them.

        AIProto.Graph graph = gameView.getConfig().getGraph();

        int next_node = 1;

        if (tracked_thieves != null) { // we have seen thieves at least once.
            while (selected_thief == null || selected_thief.getIsDead() || selected_thief.getNodeId() == gameView.getViewer().getNodeId()) { // if we are not chasing any thief, we choose another one.
                int rand = new Random().nextInt(tracked_thieves.size());
                selected_thief = tracked_thieves.get(rand);
                // todo: if i go to a thief location and it is not there, i choose another thief to follow, even if the thief is still alive.
                // todo: selecting a thief is random, which should not be. probably i should choose a thief by is distance from polices.
            }

            for (AIProto.Agent thief : tracked_thieves) {        // the thief is still alive, and we update its location
                if (thief.getId() == selected_thief.getId()) {   // and we follow it with its new location.
                    selected_thief = thief;
                }
            }

            next_node = go(gameView, selected_thief.getNodeId());

        } else { // we have not seen thieves never yet.

            // in this case, we see where all polices can get until the first visible turn,
            // and each of them will go to one of them, with the most neighbors.

            for (MyPolice myPolice : my_polices) {
                if (myPolice.police.getId() == gameView.getViewer().getId()) {
                    next_node = go(gameView, myPolice.first_destination);
                    break;
                }
            }

        }
        return next_node;
    }

    /////////////////////////////////////////////// FUNCTIONS ///////////////////////////////////////////////

    void updateThieves(GameView gameView) {
        if (gameView.getConfig().getTurnSettings().getVisibleTurnsList().contains(gameView.getTurn().getTurnNumber())) { // if this is the visible turn, i choose new thieves to follow.
            this.tracked_thieves = new ArrayList<>();
            for (AIProto.Agent agent : gameView.getVisibleAgentsList()) {
                if (agent.getTeam() != gameView.getViewer().getTeam() && agent.getType() == AIProto.AgentType.THIEF) {
                    this.tracked_thieves.add(agent); // now i have an array list of all enemy thieves.
                }
            }
            System.out.println("thieves updated.");
        }
    }

    /////////////////////////////////////////////// FUNCTIONS ///////////////////////////////////////////////

    // this function makes a list of "MyPolices" with the ids that i give them.
    // ids are sorted from 1 to how many they are.
    List<MyPolice> make_my_polices_list(List<AIProto.Agent> agents, AIProto.Agent agent) {
        my_polices = new ArrayList<>();
        for (AIProto.Agent agent1 : agents) { // i have a list of all my polices in here. which are numbered as i want.
            if (agent1.getTeam() == agent.getTeam() && agent1.getType() == AIProto.AgentType.POLICE) {
                my_polices.add(new MyPolice(agent1));
            }
        }
        my_polices.add(new MyPolice(agent)); // add this police to list of polices.

        my_polices.sort(Comparator.comparingInt(o -> o.police.getId()));
        for (int i = 1; i < my_polices.size() + 1; i++) {
            my_polices.get(i - 1).id = i;
        }

        return my_polices;
    }

    /////////////////////////////////////////////// FUNCTIONS ///////////////////////////////////////////////

    void print_money_details(GameView gameView) {
        System.out.println("first money: " + gameView.getBalance());
        System.out.println("police each turn income: " + gameView.getConfig().getIncomeSettings().getPoliceIncomeEachTurn());
        System.out.println("thief each turn income: " + gameView.getConfig().getIncomeSettings().getThievesIncomeEachTurn());
        List<AIProto.Path> pathpath = gameView.getConfig().getGraph().getPathsList();
        ArrayList<Double> prices = new ArrayList<>();
        for (AIProto.Path pathPath : pathpath) {
            if (!prices.contains(pathPath.getPrice())) {
                prices.add(pathPath.getPrice());
            }
        }
        System.out.println("paths price: " + prices);
    }

    /////////////////////////////////////////////// FUNCTIONS ///////////////////////////////////////////////

    int go(GameView gameView, int destination) {
        ArrayList<Integer> cheap_path;
        ArrayList<Integer> short_path;
        int next_node;

        cheap_path = Dijkstra.findPath(gameView.getConfig().getGraph(), gameView.getViewer().getNodeId(), destination, Mode.CHEAP);
        int cheap_turns = HowManyTurns.getAns(gameView.getConfig().getGraph(), gameView.getViewer().getNodeId(), cheap_path, gameView.getBalance(), gameView.getConfig().getIncomeSettings().getPoliceIncomeEachTurn());
        short_path = Dijkstra.findPath(gameView.getConfig().getGraph(), gameView.getViewer().getNodeId(), destination, Mode.SHORT);
        int short_turns = HowManyTurns.getAns(gameView.getConfig().getGraph(), gameView.getViewer().getNodeId(), short_path, gameView.getBalance(), gameView.getConfig().getIncomeSettings().getPoliceIncomeEachTurn());
        if (cheap_path.size() != 0) { // there is a way to get there.
            try {
                if (cheap_turns <= short_turns)
                    next_node = cheap_path.get(cheap_path.size() - 2);
                else
                    next_node = short_path.get(short_path.size() - 2);
            } catch (Exception e) {
                next_node = gameView.getViewer().getNodeId();
            }
        } else {
            next_node = gameView.getViewer().getNodeId();
        }

        System.out.println("cheap path from \"" + gameView.getViewer().getNodeId() + "\" to \"" + destination + "\" is " + cheap_path + " - turns taking: " + cheap_turns + " this is turn: " + gameView.getTurn());
        System.out.println("short path from \"" + gameView.getViewer().getNodeId() + "\" to \"" + destination + "\" is " + short_path + " - turns taking: " + short_turns + " this is turn: " + gameView.getTurn());
        return next_node;
    }

    /////////////////////////////////////////////// FUNCTIONS ///////////////////////////////////////////////

    List<MyPolice> put_first_destinations(GameView gameView, List<MyPolice> my_polices) {
        int visible_ptr = 0;
        int first_visible_turn = gameView.getConfig().getTurnSettings().getVisibleTurnsList().get(visible_ptr);
        while (first_visible_turn % 2 == 1) {
            visible_ptr++;
            first_visible_turn = gameView.getConfig().getTurnSettings().getVisibleTurnsList().get(visible_ptr);
        }
        ArrayList<Integer> distance_n_nodes_number = Functions.find_nodes_with_distance(gameView.getConfig().getGraph(), 1, (first_visible_turn - 2) / 2);
        ArrayList<AIProto.Node> distance_n_nodes = new ArrayList<>();
        for (AIProto.Node node : gameView.getConfig().getGraph().getNodesList()) {
            if (distance_n_nodes_number.contains(node.getId())) {
                distance_n_nodes.add(node);
            }
        }
//        System.out.println("distance " + (first_visible_turn - 2) / 2 + " nodes : " + distance_n_nodes);
        ArrayList<MyNode> sorted_distance_n_nodes = new ArrayList<>();
        for (AIProto.Node node : distance_n_nodes) {
            sorted_distance_n_nodes.add(new MyNode(gameView.getConfig().getGraph(), node));
        }
        sorted_distance_n_nodes.sort(Comparator.comparingInt(o -> o.number_of_neighbors));
        // the one with the most neighbors is at the end of all_nodes ArrayList.
        for (MyPolice myPolice : my_polices) {
            myPolice.first_destination = sorted_distance_n_nodes.get(sorted_distance_n_nodes.size() - (myPolice.id % sorted_distance_n_nodes.size()) - 1).node.getId(); //todo
        }

        return my_polices;
    }

    /////////////////////////////////////////////// FUNCTIONS ///////////////////////////////////////////////
}



