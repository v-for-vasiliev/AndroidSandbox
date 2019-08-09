package ru.visionlab;

public class Utils {


    public static void rotateClockwise90(int[] result, int[] tempArray, int width, int height) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                final int pix = result[x + (y * width)];
                final int newX = height - 1 - y;
                tempArray[newX + (height * x)] = pix;
            }
        }

        System.arraycopy(tempArray, 0, result, 0, tempArray.length);
    }

    public static void byteToIntArray(byte[] pixels, int[] result) {
        for (int pixel = 0, resInd = 0; pixel < pixels.length; pixel += 4) {
            result[resInd++] = (((int) pixels[pixel + 3] & 0xff) << 24) | (
                    ((int) pixels[pixel + 2] & 0xff) << 16) | (((int) pixels[pixel + 1] & 0xff)
                    << 8) | ((int) pixels[pixel + 0] & 0xff);
        }
    }

    public static void flip(int[] picture, int width, int height) {
        for (int row = 0; row < width / 2; row++) {
            for (int col = 0; col < height; col++) {
                final int curItemIndex = row * height + col;
                final int flipItemIndex = (width - row - 1) * height + col;
                final int tmp = picture[curItemIndex];
                picture[curItemIndex] = picture[flipItemIndex];
                picture[flipItemIndex] = tmp;
            }
        }
    }

    public static int countUpper(byte[] buffer, int width, android.graphics.Rect rect,
            int threshold) {
        int count = 0;

        for (int row = rect.top; row < rect.bottom; row++) {
            for (int col = rect.left; col < rect.right; col++) {
                if (((int) buffer[row * width + col] & 0xff) > threshold) {
                    ++count;
                }
            }
        }

        return count;
    }

    public static int countLower(byte[] buffer, int width, android.graphics.Rect rect,
            int threshold) {
        int count = 0;

        for (int row = rect.top; row < rect.bottom; row++) {
            for (int col = rect.left; col < rect.right; col++) {
                if (((int) buffer[row * width + col] & 0xff) < threshold) {
                    ++count;
                }
            }
        }

        return count;
    }
}