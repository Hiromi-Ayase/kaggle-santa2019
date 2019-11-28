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

  // private static long TIME_LIMIT = 10000;
  // private static long NOT_UPDATED = 200;
  // private static int ALPHA = 50000;
  // private static int EPOCH_LOOP = 50;

  private static long TIME_LIMIT = 10000;
  private static long NOT_UPDATED = 100;
  private static int ALPHA = 50000;
  private static int EPOCH_LOOP = 50;

  public static void main(String[] args) throws IOException {
    Path inputPath = Paths.get("data/family_data.csv");
    Path outputPath = Paths.get("data/submission_1574898475215.csv");
    int[][] input = readInput(inputPath);
    int[] output = readOutput(outputPath);

    int[][] state = Solver.output2state(input, output);
    int[][] costs = Solver.input2costs(input);

    double bestScore = Solver.getScore(costs, state);
    for (int e = 0;; e++) {
      System.out.printf("***** Epoch: %d *****%n", e);
      for (int t = 0; t < EPOCH_LOOP; t++) {
        state = Solver.solve(costs, state, TIME_LIMIT, ALPHA, NOT_UPDATED);
      }
      double score = Solver.getScore(costs, state);

      dump(input, state);
      System.out.println("Current Score: " + score);

      if (bestScore > score) {
        bestScore = score;
        String name = saveOutput(state[0]);
        System.out.printf("High score!!: %.3f. Saved to %s%n", bestScore, name);
      }
    }
  }

  private static void dump(int[][] input, int[][] state) {
    int n = input.length;
    int choice = 10;
    StringBuilder[] sbs = new StringBuilder[choice + 1];
    int[] cnt = new int[choice + 1];
    for (int i = 0; i <= choice; i++) {
      sbs[i] = new StringBuilder();
    }
    for (int f = 0; f < n; f++) {
      int c = input[f][state[0][f]];
      sbs[c].append(f + ", ");
      cnt[c]++;
    }

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i <= 10; i++) {
      sb.append(i + "(" + cnt[i] + "):");
      if (i < 3) {
        sb.append("(omitted)");
      } else {
        sb.append(sbs[i]);
      }
      sb.append("\n");
    }
    System.out.print(sb);
  }

  private static String saveOutput(int[] output) throws IOException {
    String outputNameTemplate = "data/submission_%d.csv";
    String outputName = String.format(outputNameTemplate, System.currentTimeMillis());
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
    int day = 100;
    List<int[]> ret = new ArrayList<>();
    try (BufferedReader br = Files.newBufferedReader(inputPath);
        CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(br)) {
      for (CSVRecord record : parser) {
        int[] rec = new int[day + 1];
        int people = Integer.parseInt(record.get(choice + 1));
        Arrays.fill(rec, choice);

        for (int i = 0; i < choice; i++) {
          int d = Integer.parseInt(record.get(i + 1));
          rec[d] = i;
        }
        rec[0] = people;
        ret.add(rec);
      }
    }
    return ret.stream().toArray(n -> new int[n][]);
  }
}
