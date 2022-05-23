package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IotContainer {

    private Integer[] commandCentreBounds = new Integer[2];
    private Map<String,Integer[]> iotBounds = new HashMap<>();
    private Integer[] outputBounds = new Integer[2];


    public Integer[] getCommandCentreBounds() {
        return commandCentreBounds;
    }

    public Map<String, Integer[]> getIotBounds() {
        return iotBounds;
    }

    public Integer[] getOutputBounds() {
        return outputBounds;
    }

    public void setCommandCentreBounds(Integer[] commandCentreBounds) {
        this.commandCentreBounds = commandCentreBounds;
    }

    public void setIotBounds(Map<String,Integer[]> iotBounds) {
        this.iotBounds = iotBounds;
    }

    public void setOutputBounds(Integer[] outputBounds) {
        this.outputBounds = outputBounds;
    }
}
