package Santa2019;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

public class Santa2019 {

  private static long TIME_LIMIT = 100000;
  private static long NOT_UPDATED = 1000;
  private static int ALPHA = 30000;
  private static String OUTPUT_NAME = "data/submission_%d.csv";

  public static void main(String[] args) throws IOException {
    Path inputPath = Paths.get("data/family_data.csv");
    Path outputPath = Paths.get("data/submission_1574882276520.csv");
    int[][] input = readInput(inputPath);
    int[] output = readOutput(outputPath);

    int[][] state = Solver.outputToState(input, output);
    double bestScore = Solver.getScore(input, state);
    for (int e = 0;; e++) {
      System.out.printf("***** Epoch: %d *****%n", e);
      for (int t = 0; t < 10; t++) {
        state = Solver.solve(input, state, TIME_LIMIT, ALPHA, NOT_UPDATED);
      }
      double score = Solver.getScore(input, state);
      if (bestScore > score) {
        bestScore = score;
        String name = saveOutput(state[0]);
        System.out.printf("High score: %.3f. Saved to %s%n", bestScore, name);
      }
    }
  }

  private static String saveOutput(int[] output) throws IOException {
    String outputName = String.format(OUTPUT_NAME, System.currentTimeMillis());
    try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(outputName));
        CSVPrinter printer = CSVFormat.DEFAULT.print(bw)) {
      printer.print("family_id");
      printer.print("assigned_day");
      printer.println();

      int n = output.length;
      for (int i = 0; i < n; i++) {
        printer.print(i);
        printer.print(output[i]);
        printer.println();
      }
    }
    return outputName;
  }

  private static int[] readOutput(Path outputPath) throws IOException {
    List<Integer> attend = new ArrayList<>();
    try (BufferedReader br = Files.newBufferedReader(outputPath);
        CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(br)) {
      for (CSVRecord record : parser) {
        int d = Integer.parseInt(record.get(1));
        attend.add(d);
      }
    }
    return attend.stream().mapToInt(v -> v).toArray();
  }

  private static int[][] readInput(Path inputPath) throws IOException {
    int choice = 10;
    int[] costTableA = {0, 50, 50, 100, 200, 200, 300, 300, 400, 500, 500};
    int[] costTableB = {0, 0, 9, 9, 9, 18, 18, 36, 36, 36 + 199, 36 + 398};
    int day = 100;
    List<int[]> ret = new ArrayList<>();
    try (BufferedReader br = Files.newBufferedReader(inputPath);
        CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(br)) {
      for (CSVRecord record : parser) {
        int[] rec = new int[day + 1];
        int people = Integer.parseInt(record.get(choice + 1));
        Arrays.fill(rec, costTableA[choice] + costTableB[choice] * people);

        for (int i = 0; i < choice; i++) {
          int d = Integer.parseInt(record.get(i + 1));
          rec[d] = costTableA[i] + costTableB[i] * people;
        }
        rec[0] = people;
        ret.add(rec);
      }
    }
    return ret.stream().toArray(n -> new int[n][]);
  }
}
