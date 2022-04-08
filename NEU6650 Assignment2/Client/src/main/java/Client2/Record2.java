package Client2;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class Record2 {
    private List<RecordElement2> recordList;
    private static long throughPut;

    public Record2(List<RecordElement2> recordList, long throughPut) {
        this.recordList = recordList;
        Record2.throughPut = throughPut;
    }

    public void process(){
        writeToCSV();
        printResults();
    }

    //write records into CSV
    private void writeToCSV(){
        try {
            FileWriter csvWriter = new FileWriter( "/Users/lucyhoney/Desktop/NEU/6650/CSV/records.csv");
            csvWriter.append("startTime,requestType,latency,responseCode\n");
            for (RecordElement2 recordElement : this.recordList) {
                csvWriter.append(recordElement.toCSVFormat());
            }
           // csvWriter.flush();
            csvWriter.close();
            System.out.println("CSV Write successfully!");
        }
        catch (IOException e) {
            System.out.println("CSV Write failed!");
        }
    }

    public static long getThroughPut(){
        return throughPut;
    }
    //part2 print results
    private void printResults(){
        DescriptiveStatistics stats = new DescriptiveStatistics();
        List<Long> latencies = new ArrayList<>();
        for (RecordElement2 record: recordList){
            stats.addValue(record.getLatency());
            latencies.add(record.getLatency());
        }
        Collections.sort(latencies);
    }


}
