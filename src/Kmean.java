package KmeanAgl;

import java.util.HashMap;
import java.util.LinkedList;

public class Kmean {

    static final Double PRECISION = 0.0;

    static LinkedList<HashMap <String, Double>> kmeanssp(DataSet data, int k) {
        LinkedList<HashMap <String, Double>> centroids = new LinkedList<>();

        centroids.add(data.randomFromDataSet());

        for(int i = 1; i < k; ++i) {
            centroids.add(data.calculateWeighedCentroid());
        }

        return centroids;
    }

    static Double kmeans(DataSet data, int k) {
        // select k intial centroids
        LinkedList<HashMap <String, Double>> centroids = kmeanssp(data, k);

        // intialize Sum of Squared Error to max,
        Double SSE = Double.MAX_VALUE;

        while(true) {
            var records = data.getRecords();

            // for each record
            for(var record : records) {
                Double minDist = Double.MAX_VALUE;
                // find the centroid with the minimum distance and add the record to the cluster
                for (int i = 0; i < centroids.size(); ++i) {
                    Double dist = DataSet.euclideanDistance(centroids.get(i), record.getRecord());
                    if (dist < minDist) {
                        minDist = dist;
                        record.setClusterNo(i);
                    }
                }
            }

            // recompute the centroids
            centroids = data.recomputeCentroids(k);

            Double newSSE = data.calculateTotalSSE(centroids);
            if(SSE - newSSE <= PRECISION) {
                break;
            }
            SSE = newSSE;
        }
        return SSE;
    }

    public static void main(String[] args) {
        try {
            DataSet data = new DataSet("KmeanAgl/wine.csv");
            // Remove prior classfications attr if it exists
            data.removeAttr("Class");
           int k = ElbowMethodForKMeans.calK(data);
           System.out.println(k);
           kmeans(data, k);
           data.createCsvOutput("KmeanAgl/output.csv");

        }
        catch (Exception e) {
            System.out.println("ERROR");
        }
    }
    
}
