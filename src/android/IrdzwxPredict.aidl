// IrdzwxPredict.aidl
package de.dl9rdz.rdzwx_predict;

// Declare any non-default types here with import statements
import de.dl9rdz.rdzwx_predict.Result;

interface IrdzwxPredict {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    int testPredict(double lat, double lon);
    //Result testPredict(double lat, double lon);

}
