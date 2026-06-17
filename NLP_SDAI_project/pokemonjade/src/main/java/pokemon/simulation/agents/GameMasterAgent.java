package pokemon.simulation.agents;

import java.util.HashMap;
import java.util.Map;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;


public class GameMasterAgent extends Agent {
    private Map<String, AID> playersAIDs = new HashMap<>();
    private boolean p1Start = false;
    private boolean p2Start = false;
    private boolean gameLaunched = false;    
    


    @Override
    protected void setup() {
    System.out.println("GameMaster waiting for players...");

    addBehaviour(new CyclicBehaviour() {
        @Override
        public void action() {
            ACLMessage msg = receive();
            if (msg != null) {
            String senderName = msg.getSender().getLocalName();
            String content = msg.getContent();
            if (senderName.equals("Player1") || senderName.equals("Player2")) {
                playersAIDs.put(senderName, msg.getSender());
            }

            switch (content) {
                case "INITIAL_HAND_REQUEST":
                System.out.println("GameMaster: " + senderName + " may draw your first hand");
                //a player has started and asks if s/he can process his/her starting hand
                ACLMessage processHandMsg = new ACLMessage(ACLMessage.INFORM);
                processHandMsg.setContent("PROCESS_INITIAL_HAND");
                processHandMsg.addReceiver(new AID(senderName, AID.ISLOCALNAME));
                send(processHandMsg);
                break;

                case "START":
                if (senderName.equals("Player1"))      p1Start = true;
                else if (senderName.equals("Player2")) p2Start = true;

                if (!gameLaunched && p1Start && p2Start) {
                    gameLaunched = true; 
                    System.out.println("GameMaster: You can START!");
                    for (AID aid : playersAIDs.values()) {
                        ACLMessage go = new ACLMessage(ACLMessage.INFORM);
                        go.setContent("GO");
                        go.addReceiver(aid);
                        send(go);
                    }
                }
                break;

                case "MULLIGAN_REQUEST":
                //a player requests a mulligan
                System.out.println("GameMaster: " + senderName + " requests a mulligan.");

                ACLMessage redrawMsg = new ACLMessage(ACLMessage.REQUEST);
                redrawMsg.setContent("REDRAW_HAND"); 
                redrawMsg.addReceiver(new AID(senderName, AID.ISLOCALNAME));
                send(redrawMsg);
                break;

                case "DEFEAT":
                System.out.println("GameMaster: Partita finita!");
                for (Map.Entry<String, AID> entry : playersAIDs.entrySet()) {
                    ACLMessage terminateMsg = new ACLMessage(ACLMessage.INFORM);
                    terminateMsg.setContent("END_GAME"); 
                    terminateMsg.addReceiver(entry.getValue()); 
                    send(terminateMsg);
                    System.out.println("GameMaster: Inviato END_GAME a " + entry.getKey());
                }
                doDelete(); //turn off the GameMaster
                break;

                default:
                break;
                }
            } else {
                block();
            }
            }

        });
    }
}