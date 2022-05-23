package main;

import helpers.AnomalyAnalyzer;
import helpers.ResourceFetcher;
import models.IotContainer;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class MainApp {

    public static void main(String[] args) {
        //created a ResourceFetcher object to retrieve the list of anomaly
        AnomalyAnalyzer analyzer = trainAnalyzer();


    }

    private static AnomalyAnalyzer trainAnalyzer(){
        ResourceFetcher fetcher = new ResourceFetcher();
        AnomalyAnalyzer anomalyAnalyzer = new AnomalyAnalyzer();
        try{
            Runtime runtime = Runtime.getRuntime();
            for(int i=0; i < fetcher.getNormalIotList().size()-1; i++){
                IotContainer container = new IotContainer();
                Integer[] ccBounds = {Integer.MAX_VALUE,Integer.MIN_VALUE};
                Map<String,List<Integer>> iotBounds = new HashMap<>();
                Integer[] outputBounds = {Integer.MAX_VALUE, Integer.MIN_VALUE};
                System.out.println("Executing " + fetcher.getNormalIotList().get(i));
                for(int j=0; j <= 10; j++){
                    Process process = runtime.exec(fetcher.getNormalIotList().get(i));
                    String output = processOutput(process);
                    System.out.println(output);
                    String[] rows = output.split("\n");
                    int maxRows = rows.length;
                    String controlCentreValues = rows[0].split(": ")[1];
                    String[] valuesToBeParsed = controlCentreValues.split(" ");
                    Integer[] values = controlCenterInputCheck(valuesToBeParsed);
                    if(values[1] > ccBounds[1]){
                        ccBounds[1] = values[1];
                    }
                    if (values[0] < ccBounds[0]){
                        ccBounds[0] = values[0];
                    }

                    for(int row = 1; row < maxRows - 2; row++){
                        String iot = rows[row];
                        String[] splitRow = iot.split(": ");
                        Integer value = Integer.parseInt(splitRow[1]);
                        String key = "IoT" + splitRow[0].split(" ")[2];
                        if (!keyExists(iotBounds, key)) {
                            iotBounds.put(key, new ArrayList<>());
                        }
                        iotBounds.get(key).add(value);
                    }

                    String stringOutputValue = rows[maxRows-1].split(": ")[1];
                    int numberOutputValue = Integer.parseInt(stringOutputValue);
                    if(numberOutputValue > outputBounds[1]){
                        outputBounds[1] = numberOutputValue;
                    }
                    if(numberOutputValue < outputBounds[0]){
                        outputBounds[0] = numberOutputValue;
                    }
                }

                for(String key: iotBounds.keySet()){
                    container.getIotBounds().put(key,getMaxMin(iotBounds.get(key)));
                }
                container.setCommandCentreBounds(ccBounds);
                container.setOutputBounds(outputBounds);
                anomalyAnalyzer.addToNormalMap("IoT" + (i+1),container);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return anomalyAnalyzer;
    }

    private static String processOutput(Process process){
        //method for processing the output of the input-output generators
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        try {
            String s = null;
            while ((s = reader.readLine()) != null){
                output.append(s).append("\n");
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        return output.toString();
    }

    private static Integer[] controlCenterInputCheck(String[] controlCentreValues){
        int maxValue = -99999;
        int minValue = 99999;
        for(int i = 0; i < controlCentreValues.length-1; i++){
            if(Integer.parseInt(controlCentreValues[i]) > maxValue){
                maxValue = Integer.parseInt(controlCentreValues[i]);
            }
            if(Integer.parseInt(controlCentreValues[i]) < minValue){
                minValue = Integer.parseInt(controlCentreValues[i]);
            }
        }
        Integer[] minMaxValues = new Integer[2];
        minMaxValues[0] = minValue;
        minMaxValues[1] = maxValue;

        return minMaxValues;
    }

    private static Integer[] getMaxMin(List<Integer> list){
        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;

        for(Integer i: list){
            if(i > max){
                max = i;
            }
            if(i < min){
                min = i;
            }
        }

        Integer[] minMax = {min,max};

        return minMax;
    }

    private static boolean keyExists(Map<String,List<Integer>> map, String key){

        for(String k: map.keySet()){
            if(k.equals(key)){
                return true;
            }
        }

        return false;
    }
}
