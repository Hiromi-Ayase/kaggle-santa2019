package Santa2019;

import java.util.Arrays;


public class Solver {
  public static class Xor128 {
    private int x = 123456789, y = 362436069, z = 521288629, w = 88675123;

    public int next(int max) {
      int t = (x ^ (x << 11));
      x = y;
      y = z;
      z = w;
      return (w = (w ^ (w >> 19)) ^ (t ^ (t >> 8))) % max;
    }
  }

  private static final Xor128 RAND = new Xor128();

  private static int DAY = 100;
  private static int OCCUPANCY_MAX = 300;
  private static int OCCUPANCY_MIN = 125;
  private static double INF = Double.MAX_VALUE / 10000;
  private static int RATE = 1000000000;

  public static int[][] solve(int[][] costs, int[][] state, long timeLimit, long alpha,
      long notUpdated) {
    int[][] bestState = state;
    double bestScore = getScore(costs, state);
    double currentScore = bestScore;

    int n = costs.length;

    long start = System.currentTimeMillis();
    long lap = start;
    long lastUpdated = start;

    for (int e = 1; lap - start < timeLimit && lap - lastUpdated < notUpdated; e++) {
      if (e % 100 == 0) {
        lap = System.currentTimeMillis();
      }
      boolean forceNext = RATE * (timeLimit - (lap - start)) > timeLimit * RAND.next(RATE) * alpha;

      int[][] p = new int[n][2];
      int sum = 0;
      for (int i = 0; i < n; i++) {
        p[i][0] = i;
        p[i][1] = costs[i][state[0][i]];
        if (state[0][i] == 10) {
          sum += p[i][1];
        }
      }
      Arrays.sort(p, (o1, o2) -> o2[1] - o1[1]);

      int family = p[RAND.next(500)][0];
      int newDay = RAND.next(DAY) + 1;
      double scoreDiff = diffChange(costs, state, family, newDay);
      if (scoreDiff < 0 || scoreDiff < INF && forceNext) {
        state = change(costs, state, family, newDay);
        currentScore = getScore(costs, state);
      }

      if (currentScore < bestScore) {
        bestState = state;
        bestScore = currentScore;
        lastUpdated = lap;
        System.out.printf("Score:%.3f%n", bestScore);
      }
    }

    return bestState;
  }

  private static int[][] change(int[][] costs, int[][] state, int family, int newDay) {
    int[][] ret = new int[][] {Arrays.copyOf(state[0], state[0].length),
        Arrays.copyOf(state[1], state[1].length)};
    int oldDay = state[0][family];
    int people = costs[family][0];

    ret[0][family] = newDay;
    ret[1][oldDay] -= people;
    ret[1][newDay] += people;
    return ret;
  }


  private static double diffChange(int[][] costs, int[][] state, int family, int newDay) {
    double ret = 0;
    int oldDay = state[0][family];
    if (oldDay == newDay) {
      return 0;
    }
    int people = costs[family][0];
    ret += costs[family][newDay] - costs[family][oldDay];

    if (oldDay > 1) {
      int preCnt = state[1][oldDay - 1];
      int oldCnt = state[1][oldDay];
      int newCnt = oldCnt - people;
      if (newCnt < OCCUPANCY_MIN) {
        return INF;
      }
      ret -= (preCnt - 125.0) / 400.0 * Math.pow(preCnt, 0.5 + Math.abs(preCnt - oldCnt) / 50.0);
      ret += (preCnt - 125.0) / 400.0 * Math.pow(preCnt, 0.5 + Math.abs(preCnt - newCnt) / 50.0);
    }

    {
      int oldCnt = state[1][oldDay];
      int newCnt = oldCnt - people;
      if (newCnt < OCCUPANCY_MIN) {
        return INF;
      }
      int oldNextCnt;
      int newNextCnt;
      if (oldDay == DAY) {
        oldNextCnt = oldCnt;
        newNextCnt = newCnt;
      } else {
        oldNextCnt = state[1][oldDay + 1];
        newNextCnt = state[1][oldDay + 1];
      }
      ret -=
          (oldCnt - 125.0) / 400.0 * Math.pow(oldCnt, 0.5 + Math.abs(oldCnt - oldNextCnt) / 50.0);
      ret +=
          (newCnt - 125.0) / 400.0 * Math.pow(newCnt, 0.5 + Math.abs(newCnt - newNextCnt) / 50.0);
    }

    if (newDay > 1) {
      int preCnt = state[1][newDay - 1];
      int oldCnt = state[1][newDay];
      int newCnt = oldCnt + people;
      if (newCnt > OCCUPANCY_MAX) {
        return INF;
      }
      ret -= (preCnt - 125.0) / 400.0 * Math.pow(preCnt, 0.5 + Math.abs(preCnt - oldCnt) / 50.0);
      ret += (preCnt - 125.0) / 400.0 * Math.pow(preCnt, 0.5 + Math.abs(preCnt - newCnt) / 50.0);
    }

    {
      int oldCnt = state[1][newDay];
      int newCnt = oldCnt + people;
      if (newCnt > OCCUPANCY_MAX) {
        return INF;
      }
      int oldNextCnt;
      int newNextCnt;
      if (newDay == DAY) {
        oldNextCnt = oldCnt;
        newNextCnt = newCnt;
      } else {
        oldNextCnt = state[1][newDay + 1];
        newNextCnt = state[1][newDay + 1];
      }
      ret -=
          (oldCnt - 125.0) / 400.0 * Math.pow(oldCnt, 0.5 + Math.abs(oldCnt - oldNextCnt) / 50.0);
      ret +=
          (newCnt - 125.0) / 400.0 * Math.pow(newCnt, 0.5 + Math.abs(newCnt - newNextCnt) / 50.0);
    }

    return ret;
  }

  public static double getScore(int[][] costs, int[][] state) {
    int people = costs.length;

    int cost = 0;
    for (int i = 0; i < people; i++) {
      int d = state[0][i];
      cost += costs[i][d];
    }

    double penalty = 0;
    for (int d = 1; d <= DAY; d++) {
      int n = state[1][d];
      if (n < 125 || n > 300)
        return INF;
      penalty += (n - 125.0) / 400.0
          * Math.pow(n, 0.5 + Math.abs(n - state[1][Math.min(DAY, d + 1)]) / 50.0);
    }
    return cost + penalty;
  }

  public static int[][] output2state(int[][] input, int[] output) {
    int[] count = new int[DAY + 1];
    int n = output.length;
    for (int family = 0; family < n; family++) {
      int d = output[family];
      count[d] += input[family][0];;
    }
    return new int[][] {Arrays.copyOf(output, n), count};
  }

  public static int[][] input2costs(int[][] input) {
    int n = input.length;
    int[][] costs = new int[n][DAY + 1];
    int[] costTableA = {0, 50, 50, 100, 200, 200, 300, 300, 400, 500, 500};
    int[] costTableB = {0, 0, 9, 9, 9, 18, 18, 36, 36, 36 + 199, 36 + 398};

    for (int f = 0; f < n; f++) {
      int p = input[f][0];
      costs[f][0] = p;
      for (int d = 1; d <= DAY; d++) {
        int c = input[f][d];
        costs[f][d] = costTableA[c] + costTableB[c] * p;
      }
    }
    return costs;
  }
}

