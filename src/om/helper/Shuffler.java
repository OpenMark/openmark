/**
 * 
 */
package om.helper;

import java.util.Random;

/**
 * Methods to shuffle an arrays properly.
 */
public abstract class Shuffler {
	/**
	 * @param <T> the array element type.
	 * @param array the array to be shuffled, it is shuffled in-place.
	 * @param r the random number generator to use to control the shuffling.
	 */
	public static <T> void shuffle(T[] array, Random r) {
		for (int i = array.length - 1; i > 0; i--) {
			int swapPos = r.nextInt(i + 1);
			if (swapPos != i) {
				T temp = array[i];
				array[i] = array[swapPos];
				array[swapPos] = temp;
			}
		}
	}

	/**
	 * @param array the array to be shuffled, it is shuffled in-place.
	 * @param r the random number generator to use to control the shuffling.
	 */
	public static void shuffle(int[] array, Random r) {
		for (int i = array.length - 1; i > 0; i--) {
			int swapPos = r.nextInt(i + 1);
			if (swapPos != i) {
				int temp = array[i];
				array[i] = array[swapPos];
				array[swapPos] = temp;
			}
		}
	}

	/**
	 * @param array the array to be shuffled, it is shuffled in-place.
	 * @param r the random number generator to use to control the shuffling.
	 */
	public static void shuffle(long[] array, Random r) {
		for (int i = array.length - 1; i > 0; i--) {
			int swapPos = r.nextInt(i + 1);
			if (swapPos != i) {
				long temp = array[i];
				array[i] = array[swapPos];
				array[swapPos] = temp;
			}
		}
	}

	/**
	 * @param array the array to be shuffled, it is shuffled in-place.
	 * @param r the random number generator to use to control the shuffling.
	 */
	public static void shuffle(float[] array, Random r) {
		for (int i = array.length - 1; i > 0; i--) {
			int swapPos = r.nextInt(i + 1);
			if (swapPos != i) {
				float temp = array[i];
				array[i] = array[swapPos];
				array[swapPos] = temp;
			}
		}
	}

	/**
	 * @param array the array to be shuffled, it is shuffled in-place.
	 * @param r the random number generator to use to control the shuffling.
	 */
	public static void shuffle(double[] array, Random r) {
		for (int i = array.length - 1; i > 0; i--) {
			int swapPos = r.nextInt(i + 1);
			if (swapPos != i) {
				double temp = array[i];
				array[i] = array[swapPos];
				array[swapPos] = temp;
			}
		}
	}
}
