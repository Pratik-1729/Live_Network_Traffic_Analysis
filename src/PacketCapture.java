import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
public class PacketCapture {
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter the network interface (e.g., eth0, wlan0): ");
            String networkInterface = scanner.nextLine();
            System.out.print("Enter the number of packets to capture: ");
            String packetCount = scanner.nextLine();

            String tsharkPath = "C:\\Program Files\\Wireshark\\tshark.exe";

            String[] command = {tsharkPath, "-i", networkInterface, "-c", packetCount , "-T", "fields", "-e", "frame.number", "-e", "frame.time_relative", "-e", "ip.src", "-e", "ip.dst", "-e", "tcp.srcport", "-e", "tcp.dstport", "-e", "_ws.col.Protocol", "-e", "icmp.type", "-e", "frame.len", "-e", "tcp.flags", "-E", "header=y", "-E", "separator=, ", "-E", "occurrence=f"};


            ProcessBuilder processBuilder = new ProcessBuilder(command);

            File outputFile = new File("captured_packets1.csv");
            processBuilder.redirectOutput(outputFile);

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            process.waitFor();

            try (CSVReader reader = new CSVReader(new FileReader("captured_packets1.csv"));
                 CSVWriter writer = new CSVWriter(new FileWriter("C:\\Users\\prati\\IdeaProjects\\network_traffic_analysis\\src\\samples\\OUTPUT.csv"))) {

                reader.skip(3);
                String[] newColumnNames = {"No.","Time","Source IP","Destination IP","Source Port","Destination Port","Protocol","ICMP type","Length","Flags"};
                writer.writeNext(newColumnNames);

                String[] nextLine;
                while ((nextLine = reader.readNext()) != null) {

                    writer.writeNext(nextLine);
                }
                int exitValue = process.exitValue();
                System.out.println("TShark process exited with value: " + exitValue);
                scanner.close();
            }

        }
        catch (IOException | InterruptedException | CsvValidationException e) {
            e.printStackTrace();
        }
    }
}
