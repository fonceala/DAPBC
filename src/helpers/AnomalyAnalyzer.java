package helpers;

import models.IotContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnomalyAnalyzer {

    //variable that contains all the values for each of the IoT devices
    private Map<String, IotContainer> iotNormalValues;

    public AnomalyAnalyzer(){
        iotNormalValues = new HashMap<>();
    }

    //method to add a record to the map (an IoT device with its corresponding bounds
    public void addToNormalMap(String iot, IotContainer iotValues){
        iotNormalValues.put(iot,iotValues);
    }

    //retrieves the map that contains the IoT devices with their corresponding values
    public Map<String, IotContainer> getIotNormalValues(){
        return iotNormalValues;
    }

    //verifies if the input-output that is generated represents an anomaly or not
    //for the values that are sent by the control centre, it is checked if any of them is out of bounds
    //for the IoT calls, it checks if there were any previous calls with that specific IoT and if there is, it checks if the value is between bounds
    //for the output it checks if it exceeds the bounds
    //returns true if there is no anomaly and false if it is anomaly
    public boolean isAnomaly(String processOutput, String key){
        String[] rows = processOutput.split("\n");
        String ccArray = rows[0].split(": ")[1];
        String[] values = ccArray.split(" ");
        boolean ccRange = false;
        for(String v: values){
            if(Integer.parseInt(v) >= getIotNormalValues().get(key).getCommandCentreBounds()[0] && Integer.parseInt(v) <= getIotNormalValues().get(key).getCommandCentreBounds()[1]){
                ccRange = true;
            }
        }
        int outputValue = Integer.parseInt(rows[rows.length-1].split(": ")[1]);
        boolean outputRange = outputValue >= getIotNormalValues().get(key).getOutputBounds()[0] && outputValue <= getIotNormalValues().get(key).getOutputBounds()[1];

        List<Boolean> iotBounds = new ArrayList<>();
        for(int row = 1; row < rows.length - 1; row++){
            String[] splitRow = rows[row].split(": ");
            String iot = "IoT" + splitRow[0].split(" ")[2];
            int value = Integer.parseInt(splitRow[1]);
            if(!getIotNormalValues().get(key).getIotBounds().containsKey(iot)){
                iotBounds.add(false);
            }else{
                Integer[] bounds = getIotNormalValues().get(key).getIotBounds().get(iot);
                if(value >= bounds[0] && value <= bounds[1]){
                    iotBounds.add(true);
                }else {
                    iotBounds.add(false);
                }
            }
        }

        boolean iotRange = true;
        for(Boolean bool: iotBounds){
            if (!bool) {
                iotRange = false;
                break;
            }
        }

        return iotRange && ccRange && outputRange;
    }
}
