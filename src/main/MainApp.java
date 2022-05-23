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
        verifyAnomaly(analyzer,1000);

    }

    //this method is calling the analyzer for 1000 times on every anomaly generator
    private static void verifyAnomaly(AnomalyAnalyzer analyzer, int numberOfData){
        ResourceFetcher fetcher = new ResourceFetcher();
        try {

                Runtime runtime = Runtime.getRuntime();

                List<String> chosenList = fetcher.getAnomalyIotList();

                for(int iotChoice = 0; iotChoice < chosenList.size(); iotChoice++) {
                    System.out.println(chosenList.get(iotChoice) + " was executed");
                    for(int k = 0; k < numberOfData; k++) {
                        Process process = runtime.exec(chosenList.get(iotChoice));
                        String output = processOutput(process);
                        System.out.println(output);
                        boolean normal = analyzer.isAnomaly(output, "IoT" + (iotChoice + 1));
                        if (!normal) {
                            System.out.println("ALERT! THIS IS AN ANOMALY!");
                        }
                    }
                }

        }catch(IOException e){
            e.printStackTrace();
        }

    }

    /*this method is used to train the analyzer
    the better the training, the better the results
    I chose to train it with 800 tries on each normal values generator

    =====================================================================

    Inside the method it is the following algorithm

    The main solution is based on getting the upper and lower bound for each of the IoTs
    It is calculated the lower bound and upper bound for the values that are sent by the control centre, the output and each parameter that is sent to the IoT devices
    After the calculations are done, they are added to a IoTContainer object to store all the data for every IoT device
    The data to every IoT device is mapped in the analyzer to its corresponding IoT device
    */
    private static AnomalyAnalyzer trainAnalyzer(){
        ResourceFetcher fetcher = new ResourceFetcher();
        AnomalyAnalyzer anomalyAnalyzer = new AnomalyAnalyzer();
        try{
            Runtime runtime = Runtime.getRuntime();
            for(int i=0; i < fetcher.getNormalIotList().size(); i++){
                IotContainer container = new IotContainer();
                Integer[] ccBounds = {Integer.MAX_VALUE,Integer.MIN_VALUE};
                Map<String,List<Integer>> iotBounds = new HashMap<>();
                Integer[] outputBounds = {Integer.MAX_VALUE, Integer.MIN_VALUE};
                System.out.println("Executing " + fetcher.getNormalIotList().get(i));
                for(int j=0; j <= 800; j++){
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

                    for(int row = 1; row < maxRows - 1; row++){
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


    //returns the output of every process
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

    //calculates the lower and upper bound for the values coming from the control centre
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

    //helper method to get the lower and upper bound from a list
    //used to get the lower and upper bound for every IoT device parameters
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

        return new Integer[]{min,max};
    }

    //method that checks if a key exists already in a map
    private static boolean keyExists(Map<String,List<Integer>> map, String key){

        for(String k: map.keySet()){
            if(k.equals(key)){
                return true;
            }
        }

        return false;
    }
}
