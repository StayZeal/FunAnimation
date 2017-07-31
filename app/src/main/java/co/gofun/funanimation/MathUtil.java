package co.gofun.funanimation;


import android.util.Log;
import android.util.SparseArray;


public class MathUtil {


    public static final String TAG = "MathUtil";

    /**
     *
     * @param x
     * @param offsetX
     * @return
     */
    public static double getY(float x, float offsetX,float amplitude ) {

//        x = x * Math.PI / 180;//转化为弧度

        double x45 = Math.pow(4 / (Math.pow(x, 4) + 4), 2.5);
        double result = amplitude * x45 * Math.sin(0.75 * Math.PI * x - Math.PI * offsetX);
//        Log.i(TAG, "Result:" + result);
        return result;
    }

    /**
     * 计算波形函数中x对应的y值
     *
     * @param mapX   换算到[-2,2]之间的x值
     * @param offset 偏移量
     * @return
     */
    public static double calcValue(float mapX, float offset) {
        int keyX = (int) (mapX * 1000);
        offset %= 2;
        double sinFunc = Math.sin(0.75 * Math.PI * mapX - offset * Math.PI);
        double recessionFunc;
        if (recessionFuncs.indexOfKey(keyX) >= 0) {
            recessionFunc = recessionFuncs.get(keyX);
        } else {
            recessionFunc = Math.pow(4 / (4 + Math.pow(mapX, 4)), 2.5);
            recessionFuncs.put(keyX, recessionFunc);
        }

        double amplitude = 0.5;
        double result = amplitude * sinFunc * recessionFunc;

        Log.i(TAG, "calcValue result:" + result);
        return result;
    }

    static SparseArray<Double> recessionFuncs = new SparseArray<>();


}
