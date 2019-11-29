package Santa2019;

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


  public static State solve(State state, long timeLimit, double startTemp, double endTemp,
      long notUpdated, boolean main) {
    double bestScore = state.getScore(main);
    double currentScore = bestScore;

    int n = state.familySize();

    long start = System.currentTimeMillis();
    long lap = start;
    long lastUpdated = start;

    int[] fa = new int[n];
    for (int i = 0; i < n; i++) {
      fa[i] = i;
    }
    int[] da = new int[State.MAX_DAY];
    for (int i = 0; i < State.MAX_DAY; i++) {
      da[i] = i + 1;
    }

    State bestState = state.clone();
    for (int e = 1; lap - start < timeLimit && lap - lastUpdated < notUpdated; e++) {
      shuffle(da);
      shuffle(fa);

      for (int family : fa) {
        for (int newDay : da) {
          lap = System.currentTimeMillis();

          int oldDay = state.getAttend()[family];
          state.change(family, newDay);
          double newScore = state.getScore(main);
          double scoreDiff = newScore - currentScore;
          double temp = startTemp + (endTemp - startTemp) * (lap - start) / timeLimit;
          double probability = Math.exp(-scoreDiff / temp);

          if (newScore < State.INF && probability * RATE > RAND.next(RATE)) {
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

    return bestState;
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

