package KmeanAgl;

import java.io.*;
import java.util.*;
public class DataSet {

    static class Record{
        private final HashMap<String, Double> record;
        private Integer clusterNo;
        public Record(HashMap<String, Double> record){
            this.record = record;
        }
        public void setClusterNo(Integer clusterNo) {
            this.clusterNo = clusterNo;
        }

        public HashMap<String, Double> getRecord() {
            return record;
        }
    }

    // List of attribute names
    private final LinkedList<String> attrNames = new LinkedList<>();
    // List of records in dataset
    private final LinkedList<Record> records = new LinkedList<>();
    // List of indices of centroids
    private final LinkedList<Integer> indicesOfCentroids = new LinkedList<>();
    // List of minimum and maximum values of each attribute
    private final HashMap<String, Double> minimums = new HashMap<>();
    private final HashMap<String, Double> maximums = new HashMap<>();
    private static final Random random = new Random();

    // Nhập dữ liệu từ file csv
    public DataSet(String csvFileName) throws IOException {

        String row;
        try(BufferedReader csvReader = new BufferedReader(new FileReader(csvFileName))) {
            if((row = csvReader.readLine()) != null) {
                String[] data = row.split(",");
                Collections.addAll(attrNames, data);
            }

            while((row = csvReader.readLine()) != null) {
                String[] data = row.split(",");
                HashMap<String, Double> record = new HashMap<>();

                if(attrNames.size() == data.length) {
                    for(int i = 0; i < data.length; ++i) {
                        String name = attrNames.get(i);
                        double val = Double.parseDouble(data[i]);
                        record.put(name, val);
                        updateMin(name, val);
                        updateMax(name, val);
                    }
                }
                else {
                    throw new IOException("Invalid data.");
                }

                records.add(new Record(record));
            }
        }
    }
    // tạo dữ liệu kết quả ra file csv
    public void createCsvOutput(String outputFileName) {

        try(BufferedWriter csvWriter = new BufferedWriter(new FileWriter(outputFileName))){

            for (String attrName : attrNames) {
                csvWriter.write(attrName);
                csvWriter.write(",");
            }
            csvWriter.write("Cluster");
            csvWriter.write('\n');
            for(var record : records) {
                for (String attrName : attrNames) {
                    csvWriter.write(String.valueOf(record.getRecord().get(attrName)));
                    csvWriter.write(",");
                }
                csvWriter.write(String.valueOf(record.clusterNo));
                csvWriter.write('\n');
            }
        }
        catch (IOException e){
            System.out.println("Error writing to file");
        }
    }
    // print data
    public void printData() {
        for (var record : records) {
            System.out.print('(');
            for(int i = 0; i < attrNames.size(); ++i) {
                System.out.print(record.getRecord().get(attrNames.get(i)));
                if(i < attrNames.size() - 1) {
                    System.out.print(", ");
                }
            }
            System.out.println("),");
        }
    }

    //cập nhập lại giá trị nhỏ nhất của các thuộc tính
    private void updateMin(String name, Double val) {
        if(minimums.containsKey(name)) {
            if(val < minimums.get(name)) {
                minimums.put(name, val);
            }
        }
        else {
            minimums.put(name, val);
        }
    }
    //cập nhập lại giá trị lớn nhất của các thuộc tính
    private void updateMax(String attrName, Double val) {
        if(maximums.containsKey(attrName)) {
            if(val > maximums.get(attrName)) {
                maximums.put(attrName, val);
            }
        }
        else {
            maximums.put(attrName, val);
        }
    }

    // Tính giá trị trung bình của thuộc tính attrName cho tất cả các record trong cụm
    public Double meanOfAttr(String attrName, LinkedList<Integer> indices) {
        Double sum = 0.0;
        // indices là tập tất cả các khóa của cụm
        for(int i : indices) {
            if(i < records.size()) {
                sum += records.get(i).getRecord().get(attrName);
            }
        }
        return sum / indices.size();
    }

    // Tính toán lại khóa cho cụm thứ clusterNo
    public HashMap<String, Double> calculateCentroid(int clusterNo){
        HashMap<String, Double> centroid = new HashMap<>(); // lưu trữ tâm cụm
        LinkedList<Integer> recsInCluster = new LinkedList<>(); // lưu trũ các record mà thuộc vào cụm clusterNo
        // tìm tất cả các record mà thuộc vào cụm clusterNo
        for(int i = 0; i < records.size(); ++i) {
            var record = records.get(i);
            if(record.clusterNo == clusterNo) {
                recsInCluster.add(i);
            }
        }
        // tính toán lại giá trị trung bình của các thuộc tính cho cụm clusterNo
        for(String attrName : attrNames) {
            centroid.put(attrName, meanOfAttr(attrName, recsInCluster));
        }
        return centroid;
    }
    // Tính lại tất cả giá trị trung bình cho tất cả K cụm
    public  LinkedList<HashMap<String, Double>> recomputeCentroids(int K) {
        LinkedList<HashMap<String, Double>> centroids = new LinkedList<>(); // danh sách lưu trữ
        for(int i = 0; i < K; ++i) {
            centroids.add(calculateCentroid(i));
        }
        return centroids;
    }
    // Xóa một thuộc tính ra khỏi bảng dữ liệu.
    public void removeAttr(String attrName) {
        if(attrNames.contains(attrName)) {
            attrNames.remove(attrName);

            for(var record : records) {
                record.getRecord().remove(attrName);
            }
            minimums.remove(attrName);
            maximums.remove(attrName);
        }
    }
    //Tạo dữ liệu ngẫu nhiên cho một thuộc tính
    public HashMap<String, Double> randomDataPoint() {
        HashMap<String, Double> res = new HashMap<>();

        for(String name : attrNames) {
            Double min = minimums.get(name);
            Double max = maximums.get(name);
            res.put(name, min + (max - min) * random.nextDouble());
        }
        return res;
    }
    //Lấy dữ liệu ngẫy nhiên từ bảng.
    public HashMap<String, Double> randomFromDataSet() {
        int index = random.nextInt(records.size());
        return records.get(index).getRecord();
    }
    //Tính khoảng cách eulic cho các điểm trong cùng một cụm
    public static Double euclideanDistance(HashMap<String, Double> a, HashMap<String, Double> b) {
        if(!a.keySet().equals(b.keySet())) {
            return Double.POSITIVE_INFINITY;
        }

        double sum = 0.0;
        for(String key : a.keySet()) {
            sum += Math.pow(a.get(key) - b.get(key), 2);
        }
        return Math.sqrt(sum);
    }
    // Tính giá trị của hàm SSE trong cụm clusterNo
    public Double calculateClusterSSE(HashMap<String, Double> centroid, int clusterNo) {
        double SSE = 0.0;
        // Vói
        for (Record record : records) {
            if (record.clusterNo == clusterNo) {
                SSE += Math.pow(euclideanDistance(record.getRecord(), centroid), 2);
            }
        }
        return SSE;
    }
    // Tính giá trị của hàm SSE cho tất cả các cụm
    public Double calculateTotalSSE(LinkedList<HashMap<String, Double>> centroids) {
        double totalSSE = 0.0;
        for(int i = 0; i < centroids.size(); ++i) {
            totalSSE += calculateClusterSSE(centroids.get(i), i);
        }
        return totalSSE;
    }

    public HashMap<String,Double> calculateWeighedCentroid(){
        double sum = 0.0;

        for(int i=0; i<records.size(); i++){
            if(!indicesOfCentroids.contains(i)){
                double minDist = Double.MAX_VALUE;
                for(int ind : indicesOfCentroids){
                    double dist = euclideanDistance(records.get(i).getRecord(), records.get(ind).getRecord());
                    if(dist<minDist)
                        minDist = dist;
                }
                if(indicesOfCentroids.isEmpty())
                    sum = 0.0;
                sum += minDist;
            }
        }

        double threshold = sum * random.nextDouble();

        for(int i=0; i<records.size(); i++){
            if(!indicesOfCentroids.contains(i)){
                double minDist = Double.MAX_VALUE;
                for(int ind : indicesOfCentroids){
                    double dist = euclideanDistance(records.get(i).getRecord(), records.get(ind).getRecord());
                    if(dist<minDist)
                        minDist = dist;
                }
                sum += minDist;

                if(sum > threshold) {
                    indicesOfCentroids.add(i);
                    return records.get(i).getRecord();
                }
            }
        }
        return new HashMap<>();
    }
    public LinkedList<String> getAttrNames() {
        return attrNames;
    }
    public LinkedList<Record> getRecords() {
        return records;
    }
    public Double getMinimum(String attrName) {
        return minimums.get(attrName);
    }
    public Double getMaximum(String attrName) {
        return maximums.get(attrName);
    }
}
