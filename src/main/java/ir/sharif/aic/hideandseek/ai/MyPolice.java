package ir.sharif.aic.hideandseek.ai;


import ir.sharif.aic.hideandseek.protobuf.AIProto;

public class MyPolice {
    public AIProto.Agent police;
    public int id;
    public int first_destination;

    MyPolice(AIProto.Agent police) {
        this.police = police;
    }
}