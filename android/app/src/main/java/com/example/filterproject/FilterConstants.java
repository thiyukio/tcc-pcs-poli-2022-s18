package com.example.filterproject;

import java.util.ArrayList;

public class FilterConstants {

        // 44khz
        public static IIRFilter[] getFilters(int sampleRate) {
                IIRFilter[] filters = new IIRFilter[Player.NUM_BANDS];

                ArrayList<double[]> a = new ArrayList<>();
                ArrayList<double[]> b = new ArrayList<>();

                double[] a1 = { 1.000000000000, -9.39331398000000, 40.0758751800000,
                                -102.252093950000, 172.765289700000,
                                -201.969268100000, 165.442085090000, -93.7680344200000, 35.1938395300000,
                                -7.89982228000000,
                                0.805444390000000 };
                double[] b1 = { 0.00590141000000000, -0.0439812000000000, 0.140436120000000,
                                -0.239946760000000, 0.205003030000000,
                                0, -0.205003030000000, 0.239946760000000, -0.140436120000000,
                                0.0439812000000000, -0.00590141000000000 };

                double[] a2 = { 1.000000000000, -9.39331398000000, 40.0758751800000, -102.252093950000,
                                172.765289700000,
                                -201.969268100000, 165.442085090000, -93.7680344200000, 35.1938395300000,
                                -7.89982228000000,
                                0.805444390000000 };
                double[] b2 = { 0.00590141000000000, -0.0439812000000000, 0.140436120000000,
                                -0.239946760000000, 0.205003030000000,
                                0, -0.205003030000000, 0.239946760000000, -0.140436120000000,
                                0.0439812000000000, -0.00590141000000000 };

                double[] a3 = { 1.000000000000, -9.39331398000000, 40.0758751800000, -102.252093950000,
                                172.765289700000,
                                -201.969268100000, 165.442085090000, -93.7680344200000, 35.1938395300000,
                                -7.89982228000000,
                                0.805444390000000 };
                double[] b3 = { 0.00590141000000000, -0.0439812000000000, 0.140436120000000,
                                -0.239946760000000, 0.205003030000000,
                                0, -0.205003030000000, 0.239946760000000, -0.140436120000000,
                                0.0439812000000000, -0.00590141000000000 };

                double[] a4 = { 1.000000000000, -9.39331398000000, 40.0758751800000, -102.252093950000,
                                172.765289700000,
                                -201.969268100000, 165.442085090000, -93.7680344200000, 35.1938395300000,
                                -7.89982228000000,
                                0.805444390000000 };
                double[] b4 = { 0.00590141000000000, -0.0439812000000000, 0.140436120000000,
                                -0.239946760000000, 0.205003030000000,
                                0, -0.205003030000000, 0.239946760000000, -0.140436120000000,
                                0.0439812000000000, -0.00590141000000000 };

                double[] a5 = { 1.000000000000, -9.39331398000000, 40.0758751800000, -102.252093950000,
                                172.765289700000,
                                -201.969268100000, 165.442085090000, -93.7680344200000, 35.1938395300000,
                                -7.89982228000000,
                                0.805444390000000 };
                double[] b5 = { 0.00590141000000000, -0.0439812000000000, 0.140436120000000,
                                -0.239946760000000, 0.205003030000000,
                                0, -0.205003030000000, 0.239946760000000, -0.140436120000000,
                                0.0439812000000000, -0.00590141000000000 };

                double[] a6 = { 1.000000000000, -9.39331398000000, 40.0758751800000, -102.252093950000,
                                172.765289700000,
                                -201.969268100000, 165.442085090000, -93.7680344200000, 35.1938395300000,
                                -7.89982228000000,
                                0.805444390000000 };
                double[] b6 = { 0.00590141000000000, -0.0439812000000000, 0.140436120000000,
                                -0.239946760000000, 0.205003030000000,
                                0, -0.205003030000000, 0.239946760000000, -0.140436120000000,
                                0.0439812000000000, -0.00590141000000000 };

                {
                        a.add(a1);
                        a.add(a2);
                        a.add(a3);
                        a.add(a4);
                        a.add(a5);
                        a.add(a6);

                        b.add(b1);
                        b.add(b2);
                        b.add(b3);
                        b.add(b4);
                        b.add(b5);
                        b.add(b6);

                        for (int i = 0; i < Player.NUM_BANDS; i++) {
                                filters[i] = new IIRFilter(a.get(i), b.get(i));
                        }
                }
                return filters;
        }
}
