package main;

import ScreenToString.ScreenToString;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.CvType;
import org.opencv.core.Scalar;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;


public class main {
    static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    public static void main(String[] args) {

        //String input = ScreenToString.run(args[0]);

        //test
        String input = ScreenToString.run("C:\\Users\\thorf\\Documents\\JavaWS\\OpenCVGRadleBuildTest\\src\\main\\resources\\Ottertest1.PNG");
        TreeMap<Integer, ArrayList<String>> sortedPaths = Solver.Solver.run(input);

        int maxSize = sortedPaths.size();
        int count = 0;
        for(Map.Entry<Integer,ArrayList<String>> entry : sortedPaths.entrySet()) {
            if(count >= maxSize-3){
                Integer key = entry.getKey();
                ArrayList<String> value = entry.getValue();

                System.out.print("\n::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::\n Paths that have a point value of: " + key + "\n");
                for (int i = 0; i < value.size(); i++) {
                    System.out.print(value.get(i));
                }
            }
            count++;



            //System.out.println(key + " => " + value);
        }
    }
}