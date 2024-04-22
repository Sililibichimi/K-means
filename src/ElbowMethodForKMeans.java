package KmeanAgl;

public class ElbowMethodForKMeans {
    public static int calK(DataSet data) {
        int maxK = 6;
        double[] SSE = new double[maxK];

        for(int k = 1; k < maxK; ++k) {
            SSE[k] = Kmean.kmeans(data, k);
        }

        double minSlope = SSE[1] - SSE[2];
        int optimalK = 1;

        for(int k = 1; k < maxK - 1; ++k) {
            double slope = SSE[k] - SSE[k + 1];
            if(slope < minSlope) {
                minSlope = slope;
                optimalK = k;
            }
        }
        return optimalK;
    }
}
