package Santa2019;

import java.util.Arrays;

/**
 * state[0][family]: family's attendance day.<br>
 * state[1][day]: Number of attendee.<br>
 * costs[family][day]: Cost of day.<br>
 */
public class State {
  public static int MAX_DAY = 100;
  public static double INF = Double.MAX_VALUE / 10000;
  public static int OCCUPANCY_MAX = 300;
  public static int OCCUPANCY_MIN = 125;

  private static final double[][] PENALTY_TABLE;
  static {
    PENALTY_TABLE = new double[OCCUPANCY_MAX + 50][OCCUPANCY_MAX + 50];
    for (double[] v : PENALTY_TABLE) {
      Arrays.fill(v, INF);
    }
    for (int i = OCCUPANCY_MIN; i <= OCCUPANCY_MAX; i++) {
      for (int j = OCCUPANCY_MIN; j <= OCCUPANCY_MAX; j++) {
        PENALTY_TABLE[i][j] = (i - 125.0) / 400.0 * Math.pow(i, 0.5 + Math.abs(i - j) / 50.0);
      }
    }
  }

  private final int[][] costs;
  private final int[][] cand;
  private final int[][] state;

  private int currentCost;

  public State(int[][] input, int[] output) {
    this.costs = input2costs(input);
    this.state = output2state(input, output);
    this.cand = input2cand(input);

    int people = input.length;
    for (int i = 0; i < people; i++) {
      int d = state[0][i];
      currentCost += costs[i][d];
    }
  }

  private State(State state) {
    this.costs = state.costs;
    this.cand = state.cand;

    this.state = new int[][] {Arrays.copyOf(state.state[0], state.state[0].length),
        Arrays.copyOf(state.state[1], state.state[1].length)};
    this.currentCost = state.currentCost;
  }

  public State clone() {
    return new State(this);
  }


  public double getScore(boolean flg) {
    double penalty = 0;
    for (int d = 1; d <= MAX_DAY; d++) {
      int n = state[1][d];
      if (n < OCCUPANCY_MIN || n > OCCUPANCY_MAX)
        return INF;
      int m = state[1][Math.min(MAX_DAY, d + 1)];
      penalty += PENALTY_TABLE[n][m];
    }

    double penalty2 = 0;
    if (!flg) {
      int margin = 50;
      for (int d = 1; d <= MAX_DAY; d++) {
        int n = state[1][d];
        double diff = 0;
        if (n < OCCUPANCY_MIN + margin) {
          diff = OCCUPANCY_MIN + margin - n;
        } else if (n > OCCUPANCY_MAX - margin) {
          diff = n - (OCCUPANCY_MAX - margin);
        }
        penalty2 += diff * diff;
      }
    }
    return currentCost + penalty2 / 10 + penalty;
  }

  public boolean check(int family, int newDay) {
    int oldDay = state[0][family];
    int people = costs[family][0];
    if (state[1][oldDay] < OCCUPANCY_MIN || state[1][newDay] + people > OCCUPANCY_MAX) {
      return false;
    }
    return true;
  }

  public boolean change(int family, int newDay) {
    int oldDay = state[0][family];
    int people = costs[family][0];
    if (state[1][oldDay] < OCCUPANCY_MIN || state[1][newDay] + people > OCCUPANCY_MAX) {
      return false;
    }
    state[0][family] = newDay;
    state[1][oldDay] -= people;
    state[1][newDay] += people;
    currentCost += costs[family][newDay] - costs[family][oldDay];
    return true;
  }

  public int familySize() {
    return costs.length;
  }

  public int[] getAttend() {
    return state[0];
  }

  public int[] getOccupancy() {
    return state[1];
  }

  public int[][] getCosts() {
    return costs;
  }

  public int[][] getCand() {
    return cand;
  }

  public static int[][] output2state(int[][] input, int[] output) {
    int[] count = new int[MAX_DAY + 1];
    int n = output.length;
    for (int family = 0; family < n; family++) {
      int d = output[family];
      count[d] += input[family][0];;
    }
    return new int[][] {Arrays.copyOf(output, n), count};
  }

  public static int[][] input2costs(int[][] input) {
    int n = input.length;
    int[][] costs = new int[n][MAX_DAY + 1];
    int[] costTableA = {0, 50, 50, 100, 200, 200, 300, 300, 400, 500, 500};
    int[] costTableB = {0, 0, 9, 9, 9, 18, 18, 36, 36, 36 + 199, 36 + 398};

    for (int f = 0; f < n; f++) {
      int p = input[f][0];
      costs[f][0] = p;
      for (int d = 1; d <= MAX_DAY; d++) {
        int c = input[f][d];
        costs[f][d] = costTableA[c] + costTableB[c] * p;
      }
    }
    return costs;
  }

  private static int[][] input2cand(int[][] input) {
    int n = input.length;
    int candMax = 10;
    int[][] cand = new int[n][candMax];

    for (int f = 0; f < n; f++) {
      for (int d = 1; d <= MAX_DAY; d++) {
        int c = input[f][d];
        if (c < candMax) {
          cand[f][c] = d;
        }
      }
    }
    return cand;
  }
}
