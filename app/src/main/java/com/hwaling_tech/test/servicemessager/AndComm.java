package com.hwaling_tech.test.servicemessager;

/**
 * Created by yen.shih-chun on 2017/6/2.
 */

public class AndComm {

    private static int DI_channels = 16;
    private static int DO_channels = 16;
    private static int AI_channels = 10;
    private static int AO_channels = 10;

    private int[] DI_bit;
    private int[] DO_bit;
    private double[] AI_double;
    private double[] AO_double;

    public void AndComm() {
        DI_bit = new int[DI_channels];
        DO_bit = new int[DO_channels];
        AI_double = new double[AI_channels];
        AO_double = new double[AO_channels];
    }

    public void PV_get() {

    }

    public void SV_set() {

    }

    public int rollDI2int() {
        String sum = "";
        for (int i = 0; i > DI_channels; i++) {
            sum = Integer.toString(DI_bit[i]) + sum;
        }
        return Integer.parseInt(sum, 16);
    }

    public int rollDO2int() {
        String sum = "";
        for (int i = 0; i > DI_channels; i++) {
            sum = Integer.toString(DI_bit[i]) + sum;
        }
        return Integer.parseInt(sum, 16);
    }

}
