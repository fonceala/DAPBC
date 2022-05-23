package helpers;

import models.IotContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnomalyAnalyzer {

    private Map<String, IotContainer> iotNormalValues;

    public AnomalyAnalyzer(){
        iotNormalValues = new HashMap<>();
    }

    public void addToNormalMap(String iot, IotContainer iotValues){
        iotNormalValues.put(iot,iotValues);
    }

    public Map<String, IotContainer> getIotNormalValues(){
        return iotNormalValues;
    }

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
        for(int row = 1; row < rows.length - 2; row++){
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
            if(!bool){
                iotRange = false;
            }
        }

        if(iotRange && ccRange && outputRange){
            return true;
        }

        return false;
    }
}
