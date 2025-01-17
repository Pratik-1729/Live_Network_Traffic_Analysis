package processing;

import trace.Capture;
import trace.Packet;
import util.ClusteringUtils;
import util.MathUtils;
import util.MenuUtils;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PacketSize {

    private static int[] findMinMaxMean(List<Packet> packets) {
        int min = Integer.MAX_VALUE; 
        int max = 0, sum = 0, n = 0;   

        for (Packet packet : packets) {
            int packetSize = packet.getLength();
            if (packetSize > max) max = packetSize;
            if (packetSize < min) min = packetSize;
            sum += packetSize;
            n++;
        }
        return new int[] {min, max, sum / n};     
    }
    private static void showGeneralStats(List<Packet> packets) {
        int[] minMaxMean = findMinMaxMean(packets);
        MenuUtils.showOutput(
            String.format(
                "Minimum:\t%s bytes\nMaximum:\t%s bytes\nAr. Mean:\t%s bytes",
                minMaxMean[0], minMaxMean[1], minMaxMean[2]
            )
        );
    }
    private static void writeBarplotData(List<List<Packet>> clusters, int size) {
        HashMap<String, Double> packetClusters = new HashMap<>(); 
        for (List<Packet> cluster : clusters) {
            int[] minMaxMean = findMinMaxMean(cluster);
            packetClusters.put(
                String.format("%d-%d", minMaxMean[0], minMaxMean[1]),
                cluster.size() / (size * 1.0)
            );
        }

        try (PrintWriter pw = new PrintWriter(new FileOutputStream(
            String.format("samples/data_%s.csv", 
            new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()))
        ));) {

            for (String sizeRange : packetClusters.keySet())
                pw.print(sizeRange + ",");
            pw.print("\n");
            for (Double sizePercentage : packetClusters.values())
                pw.print(String.format(Locale.US, "%6.5f,", sizePercentage * 100));
                
        } catch (Exception e) {
        }
    }
    public static void inspectPacketsSize(Capture capture) {
        List<Packet> packets = capture.getPackets();
        showGeneralStats(packets);
        List<List<Packet>> clusters = ClusteringUtils.findClusters(
            packets, (x) -> MathUtils.log2(((Packet) x).getLength() / 80)
        );
        writeBarplotData(clusters, packets.size());
    }
}