package main;

import helpers.ResourceFetcher;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainApp {

    public static void main(String[] args) {
        ResourceFetcher fetcher = new ResourceFetcher();
        try{
            Runtime runtime = Runtime.getRuntime();
            for(String path: fetcher.getNormalIotList()){
                System.out.println("Executing " + path);
                Process process = runtime.exec(path);
                System.out.println(processOutput(process));
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private static String processOutput(Process process){

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
}
