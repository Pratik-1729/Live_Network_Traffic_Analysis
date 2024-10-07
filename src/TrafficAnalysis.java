import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import processing.*;
import trace.Capture;
import util.FilesUtil;
import util.Logger;
import util.MenuUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class TrafficAnalysis {

    private static String[] options = {
            "-1 - Capture network",
            "0- Exit",
            "1- Emitter-receiver pair analysis",
            "2- Total length and number of packets",
            "3- TCP ports and known services",
            "4- Number and types of ICMP packets",
            "5- Packets size analysis",
            "6- TCP connections (tries)",
            "7- TCP connections (established)",
            "8- Receiver of more traffic",
            "9- Emitter of more traffic"
    };
    public static  void PacketCapture(){
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
                        MenuUtils.showOutput(
                                String.format(
                                        "TShark process exited with value: 0"
                                )
                        );
                    }

                }
                catch (IOException | InterruptedException | CsvValidationException e) {
                    e.printStackTrace();
                }
    }


    private static Capture prepareAnalysis(String[] args) {
        String targetFile = args.length == 0 ? "C:\\Users\\prati\\IdeaProjects\\network_traffic_analysis\\src\\samples\\OUTPUT.csv" : args[0];
        Logger.info("Loading trace from file " + targetFile); 
        Capture capture = FilesUtil.readTrace(targetFile);
        Logger.info("Finished trace parsing. Ready to go.");
        return capture;
    }

    public static void main(String[] args) {
        MenuUtils.showBanner();
        Capture capture = prepareAnalysis(args);
        Scanner stdin = new Scanner(System.in);
        while (true) {
            switch (MenuUtils.getUserOption(options, stdin)) {
                case -1:
                    Logger.info("Capturing Packet:");
                    PacketCapture();
                    break;
                case 0:
                    Logger.info("Thank you for using TrafficAnalysis tool! Exiting...");
                    stdin.close();
                    System.exit(0);
                    break;
                case 1:
                    Logger.info("Running [Emitter-receiver pair analysis]... ");
                    EmitterReceiverPair.inspectAddresses(capture);
                    break;
                case 2:
                    Logger.info("Running [Total length and number of packets]");
                    TraceTimeSize.inspectTraceTimeAndSize(capture);
                    break;
                case 3:
                    Logger.info("Running [TCP ports and known services]");
                    TCPPorts.inspectPortsAndServices(capture);
                    break;
                case 4:
                    Logger.info("Running [Number and types of ICMP packets]...");
                    ControlMessagePacket.inspectICMP(capture);
                    break;
                case 5:
                    Logger.info("Running [Packets size analysis]...");
                    PacketSize.inspectPacketsSize(capture);       
                    Logger.info("Exported additional data to ./samples/data_{DATETIME}.csv file.");
                    Logger.info("In order to generate a barplot from the exported data, execute the python script inside 'plugin' folder.");
                    break;
                case 6:
                    Logger.info("Running [TCP connections (tries)]");
                    TriedTCPConnection.inspectTriedTcpConnections(capture);
                    break;
                case 7:
                    Logger.info("Running [TCP connections (established)");
                    EstablishedTCPConnection.inspectEstablishedTcpConnections(capture);
                    break;
                case 8:
                    Logger.info("Running [Receiver of more traffic]");
                    TrafficPerIP.inspectReceivers(capture);
                    break;
                case 9:
                    Logger.info("Running [Emitter of more traffic]");
                    TrafficPerIP.inspectEmitters(capture);
                    break;
                default:
                    Logger.error("The selected option is not implemented yet.");
                    break;
            }
        }
    }
}
