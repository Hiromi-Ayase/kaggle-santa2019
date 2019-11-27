package Santa2019;

import java.util.Arrays;


public class Solver {
  private static class Xor128 {
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

  public static int[][] solve(int[][] input, int[][] state, long timeLimit, long alpha,
      long notUpdated) {
    int[][] bestState = state;
    double bestScore = getScore(input, state);
    double currentScore = bestScore;

    int n = input.length;

    long start = System.currentTimeMillis();
    long lap = start;
    long lastUpdated = start;

    for (int e = 1; lap - start < timeLimit && lap - lastUpdated < notUpdated; e++) {
      if (e % 10000 == 0) {
        lap = System.currentTimeMillis();
      }
      boolean forceNext = RATE * (timeLimit - (lap - start)) > timeLimit * RAND.next(RATE) * alpha;

      int type = 0;
      if (type == 0) {
        int family = RAND.next(n);
        int newDay = RAND.next(DAY) + 1;
        double scoreDiff = diffChange(input, state, family, newDay);
        if (scoreDiff < 0 || scoreDiff < INF && forceNext) {
          state = change(input, state, family, newDay);
          currentScore = getScore(input, state);
        }
      } else {
        int family1 = RAND.next(n);
        int family2 = RAND.next(n);
        double scoreDiff = diffSwap(input, bestState, family1, family2);
        if (scoreDiff < 0 || scoreDiff < INF && forceNext) {
          state = swap(input, state, family1, family2);
          currentScore = getScore(input, state);
        }
      }


      if (currentScore < bestScore) {
        bestState = state;
        bestScore = currentScore;
        lastUpdated = lap;
        System.out.printf("Score:%.3f%n", bestScore);
      }
    }
    long end = System.currentTimeMillis();
    System.out.printf("%d [ms]%n", end - start);
    return bestState;
  }

  private static double diffSwap(int[][] input, int[][] state, int family1, int family2) {
    if (family1 == family2) {
      return INF;
    }
    int day1 = state[0][family1];
    int day2 = state[0][family2];
    if (Math.abs(day1 - day2) <= 3) {
      return INF;
    }

    double ret = 0;
    ret += diffChange(input, state, family1, day2);
    ret += diffChange(input, state, family2, day1);
    return ret;
  }

  private static int[][] swap(int[][] input, int[][] state, int family1, int family2) {
    int[][] ret = new int[][] {Arrays.copyOf(state[0], state[0].length),
        Arrays.copyOf(state[1], state[1].length)};
    int day1 = state[0][family1];
    int day2 = state[0][family2];
    change(input, state, family1, day2);
    change(input, state, family2, day1);
    return ret;
  }

  private static int[][] change(int[][] input, int[][] state, int family, int newDay) {
    int[][] ret = new int[][] {Arrays.copyOf(state[0], state[0].length),
        Arrays.copyOf(state[1], state[1].length)};
    int oldDay = state[0][family];
    int people = input[family][0];

    ret[0][family] = newDay;
    ret[1][oldDay] -= people;
    ret[1][newDay] += people;
    return ret;
  }


  private static double diffChange(int[][] input, int[][] state, int family, int newDay) {
    double ret = 0;
    int oldDay = state[0][family];
    if (oldDay == newDay) {
      return 0;
    }
    int people = input[family][0];
    ret += input[family][newDay] - input[family][oldDay];

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

  public static double getScore(int[][] input, int[][] state) {
    int people = input.length;

    int cost = 0;
    for (int i = 0; i < people; i++) {
      int d = state[0][i];
      cost += input[i][d];
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

  public static int[][] outputToState(int[][] input, int[] output) {
    int[] count = new int[DAY + 1];
    int n = output.length;
    for (int family = 0; family < n; family++) {
      int d = output[family];
      count[d] += input[family][0];;
    }
    return new int[][] {Arrays.copyOf(output, n), count};
  }
}

