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

  // private static long TIME_LIMIT = 120000;
  // private static long NOT_UPDATED = 20000;
  // private static double START_TEMP = 7;
  // private static double END_TEMP = 1;

  private static long TIME_LIMIT = 60000;
  private static long NOT_UPDATED = 20000;
  private static double START_TEMP = 15;
  private static double START_TEMP2 = 5;
  private static double END_TEMP = 0.3;

  public static void main(String[] args) throws IOException {
    Path inputPath = Paths.get("data/family_data.csv");
    Path outputPath = Paths.get("data/submission_1575145069860_71887.csv");
    int[][] input = readInput(inputPath);
    int[] output = readOutput(outputPath);
    // output = initOutput(output.length);
    State state = new State(input, output);

    double bestScore = state.getScore(true);
    for (int e = 0;; e++) {
      boolean main = e % 3 == 0;

      System.out.printf("***** Epoch: %d *****%n", e);
      State[] states = Solver.solve2(state, TIME_LIMIT, main ? START_TEMP2 : START_TEMP, END_TEMP,
          NOT_UPDATED, true);
      if (states[0].getScore(true) <= State.INF / 10) {
        state = states[0];
      }
      double score = states[1].getScore(true);

      dump(input, state);

      if (bestScore > score) {
        bestScore = score;
        String name = saveOutput(states[1].getAttend(), (int) bestScore);
        System.out.printf("High score!!: %.3f. Saved to %s%n", bestScore, name);
      }
    }
  }

  private static int[] initOutput(int n) {
    int[] ret = new int[n];
    for (int i = 0; i < n; i++) {
      ret[i] = i % State.MAX_DAY + 1;
    }
    return ret;
  }

  private static void dump(int[][] input, State state) {
    int n = input.length;
    int choice = 10;
    StringBuilder[] sbs = new StringBuilder[choice + 1];
    int[] cnt = new int[choice + 1];
    for (int i = 0; i <= choice; i++) {
      sbs[i] = new StringBuilder();
    }
    int[] attend = state.getAttend();
    int[][] costs = state.getCosts();
    int[] cost = new int[choice + 1];
    for (int f = 0; f < n; f++) {
      int d = attend[f];
      int c = input[f][d];
      sbs[c].append(f + ", ");
      cnt[c]++;
      cost[c] += costs[f][d];
    }

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i <= 10; i++) {
      sb.append(String.format("%02d [Count:%04d, Cost:%05d]: ", i, cnt[i], cost[i]));
      if (i < 3) {
        sb.append("(omitted)");
      } else {
        sb.append(sbs[i]);
      }
      sb.append("\n");
    }
    int[] occ = state.getOccupancy();
    for (int i = 1; i <= 100; i++) {
      sb.append(i + ":" + occ[i] + ", ");
    }
    sb.append("\n");
    sb.append(String.format("Current score: %.3f%n", state.getScore(true)));
    System.out.print(sb);
  }

  private static String saveOutput(int[] output, int score) throws IOException {
    String outputNameTemplate = "data/submission_%d_%d.csv";
    String outputName = String.format(outputNameTemplate, System.currentTimeMillis(), score);
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
