package helpers;

import java.util.ArrayList;
import java.util.List;

public class ResourceFetcher {

    //list to keep the normal value generators
    private List<String> normalIotList;

    //list to keep the anomaly values generators
    private List<String> anomalyIotList;

    //constructor for the ResourceFetcher
    //inside the constructor the given .exe files are included in the lists from above
    public ResourceFetcher(){

        normalIotList = new ArrayList<>();
        anomalyIotList = new ArrayList<>();

        ClassLoader loader = getClass().getClassLoader();
        for(int i = 1; i <= 10; i++){
            normalIotList.add(loader.getResource("IoT" + i + ".exe").getPath());
            System.out.println(loader.getResource("IoT" + i + ".exe").getFile() + " was fetched");
            anomalyIotList.add(loader.getResource("IoT" + i + "_anomaly.exe").getPath());
            System.out.println(loader.getResource("IoT" + i + "_anomaly.exe").getFile() + " was fetched");
        }
    }

    //retrieves the list of normal values generator
    public List<String> getNormalIotList(){
        return this.normalIotList;
    }

    //retrieves the list of anomaly values generator
    public List<String> getAnomalyIotList(){
        return this.anomalyIotList;
    }
}
