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

  private static int RATE = 1000000000;


  public static State[] solve(State state, long timeLimit, double startTemp, double endTemp,
      long notUpdated) {
    double bestScore = state.getScore();
    double currentScore = bestScore;

    int n = state.familySize();

    long start = System.currentTimeMillis();
    long lap = start;
    long lastUpdated = start;

    int[] fa = new int[n];
    for (int i = 0; i < n; i++) {
      fa[i] = i;
    }
    int[][] da = new int[n][];
    int fp = 0;
    for (int[] c : state.getCand()) {
      da[fp++] = Arrays.copyOf(c, c.length);
    }

    State bestState = state.clone();
    int count = 0;
    outer: while (true) {
      shuffle(fa);

      for (int family : fa) {
        shuffle(da[family]);
        for (int newDay : da[family]) {
          if (lap - lastUpdated >= notUpdated
              || (lap = System.currentTimeMillis()) - start >= timeLimit) {
            break outer;
          }
          count++;

          int oldDay = state.getAttend()[family];
          state.change(family, newDay);

          double newScore = state.getScore();
          double temp = startTemp + (endTemp - startTemp) * (lap - start) / timeLimit;

          if (Math.exp(-(newScore - currentScore) / temp) * RATE > RAND.next(RATE)) {
            currentScore = newScore;
          } else {
            state.change(family, oldDay);
          }

          if (currentScore < bestScore) {
            bestState = state.clone();
            bestScore = currentScore;
            lastUpdated = lap;
            System.out.printf("High Score!!: %.3f%n", bestScore);
          }
        }
      }
    }
    System.out.println("Loop count: " + count);

    return new State[] {state, bestState};
  }

  public static State[] solve2(State state, long timeLimit, double startTemp, double endTemp,
      long notUpdated) {
    double bestScore = state.getScore();
    double currentScore = bestScore;

    int n = state.familySize();

    long start = System.currentTimeMillis();
    long lap = start;
    long lastUpdated = start;

    int[] fa1 = new int[n];
    int[] fa2 = new int[n];
    for (int i = 0; i < n; i++) {
      fa1[i] = i;
      fa2[i] = i;
    }
    int[][] da = new int[n][];
    int fp = 0;
    for (int[] c : state.getCand()) {
      da[fp++] = Arrays.copyOf(c, 6);
    }

    State bestState = state.clone();

    int count = 0;
    outer: while (true) {
      shuffle(fa1);
      for (int family1 : fa1) {
        shuffle(fa2);
        for (int family2 : fa2) {
          if (family1 == family2)
            continue;
          for (int newDay1 : da[family1]) {
            for (int newDay2 : da[family2]) {
              if (lap - lastUpdated >= notUpdated
                  || (lap = System.currentTimeMillis()) - start >= timeLimit) {
                break outer;
              }
              count++;

              int[] attend = state.getAttend();
              int oldDay1 = attend[family1];
              int oldDay2 = attend[family2];
              state.change(family1, newDay1);
              state.change(family2, newDay2);

              double newScore = state.getScore();
              double temp = startTemp + (endTemp - startTemp) * (lap - start) / timeLimit;

              if (Math.exp(-(newScore - currentScore) / temp) * RATE > RAND.next(RATE)) {
                currentScore = newScore;
              } else {
                state.change(family1, oldDay1);
                state.change(family2, oldDay2);
              }

              if (currentScore < bestScore) {
                bestState = state.clone();
                bestScore = currentScore;
                lastUpdated = lap;
                System.out.printf("High Score!!: %.3f%n", bestScore);
              }
            }
          }
        }
      }
    }
    System.out.println("Loop count: " + count);
    return new State[] {state, bestState};
  }


  public static State[] solve3(State state, long timeLimit, double startTemp, double endTemp,
      long notUpdated) {
    double bestScore = state.getScore();
    double currentScore = bestScore;

    int n = state.familySize();

    long start = System.currentTimeMillis();
    long lap = start;
    long lastUpdated = start;

    int[] fa = new int[n];
    for (int i = 0; i < n; i++) {
      fa[i] = i;
    }
    int[][] da = new int[n][];
    int fp = 0;
    for (int[] c : state.getCand()) {
      da[fp++] = Arrays.copyOf(c, 6);
    }

    State bestState = state.clone();

    int count = 0;
    outer: while (true) {
      shuffle(fa);
      for (int i = 0; i + 2 < n; i += 3) {
        int family1 = fa[i + 0];
        int family2 = fa[i + 1];
        int family3 = fa[i + 2];
        for (int newDay1 : da[family1]) {
          for (int newDay2 : da[family2]) {
            for (int newDay3 : da[family3]) {
              if (lap - lastUpdated >= notUpdated
                  || (lap = System.currentTimeMillis()) - start >= timeLimit) {
                break outer;
              }
              count++;

              int[] attend = state.getAttend();
              int oldDay1 = attend[family1];
              int oldDay2 = attend[family2];
              int oldDay3 = attend[family3];
              state.change(family1, newDay1);
              state.change(family2, newDay2);
              state.change(family3, newDay3);

              double newScore = state.getScore();
              double temp = startTemp + (endTemp - startTemp) * (lap - start) / timeLimit;

              if (Math.exp(-(newScore - currentScore) / temp) * RATE > RAND.next(RATE)) {
                currentScore = newScore;
              } else {
                state.change(family1, oldDay1);
                state.change(family2, oldDay2);
                state.change(family3, oldDay3);
              }

              if (currentScore < bestScore) {
                bestState = state.clone();
                bestScore = currentScore;
                lastUpdated = lap;
                System.out.printf("High Score!!: %.3f%n", bestScore);
              }
            }
          }
        }
      }
    }
    System.out.println("Loop count: " + count);
    return new State[] {state, bestState};
  }


  public static void shuffle(int[] a) {
    for (int i = 0, n = a.length; i < n; i++) {
      int ind = RAND.next(n - i) + i;
      int d = a[i];
      a[i] = a[ind];
      a[ind] = d;
    }
  }

}

