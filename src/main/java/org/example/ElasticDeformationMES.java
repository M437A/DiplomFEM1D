package org.example;

import org.apache.commons.math3.analysis.integration.IterativeLegendreGaussIntegrator;
import org.apache.commons.math3.linear.*;

public class ElasticDeformationMES {
    public double[] xi;
    public double[] yi;
    public static double domain=2;
    public ElasticDeformationMES() {}
    public ElasticDeformationMES(double dz) {
        domain = dz;
    }

    //Вычисление интеграла
    public double getIntegral(int i, int j, int n) {

        double integral2 =  new IterativeLegendreGaussIntegrator( //FIXME: fix gaussian integral
                n,
                1e-6,
                1e-6).integrate(
                Integer.MAX_VALUE,
                x -> E(x) * e_i_dx(i, x, n) * e_i_dx(j, x, n),
                0,
                2);



//        double p1=(1.0 / Math.sqrt(3.0));
//        double p2 =(-1.0 / Math.sqrt(3.0));
//        double l = domain / n;
//        double middle = l * i;
//        double left_edge = l * (i - 1)+1;
//        double right_edge = l * (i + 1)+1;

//        double o = (p1) * ((middle-left_edge) / 2.0) + (middle+left_edge / 2.0);
//        double s = (p2) * ((middle-left_edge) / 2.0) + (middle+left_edge / 2.0);
//        double integral_01 = ((middle-left_edge) / 2.0) * (E(o) * e_i_dx(i, o, n) * e_i_dx(j, o, n) + E(s) * e_i_dx(i, s, n) * e_i_dx(j, s, n));
//
//        double oo = (p1) * ((right_edge-middle) / 2.0) + ((right_edge+middle) / 2.0);
//        double ss = (p2) *((right_edge-middle) / 2.0) + ((right_edge+middle) / 2.0);
//        double integral_12 = ((right_edge-middle)) * (E(oo) * e_i_dx(i, oo, n) * e_i_dx(j, oo, n) + E(ss) * e_i_dx(i, ss, n) * e_i_dx(j, ss, n));



//        double o = (1.0 / Math.sqrt(3.0)) * (1.0 / 2.0) + (1.0 / 2.0);
//        double s = (-1.0 / Math.sqrt(3.0)) * (1.0 / 2.0) + (1.0 / 2.0);
//        double integral_01 = (1.0 / 2.0) * (E(o) * e_i_dx(i, o, n) * e_i_dx(j, o, n) + E(s) * e_i_dx(i, s, n) * e_i_dx(j, s, n));
//
//        double oo = (1.0 / Math.sqrt(3.0)) * (1.0 / 2.0) + (3.0 / 2.0);
//        double ss = (-1.0 / Math.sqrt(3.0)) * (1.0 / 2.0) + (3.0 / 2.0);
//        double integral_12 = (1.0 / 2.0) * (E(oo) * e_i_dx(i, oo, n) * e_i_dx(j, oo, n) + E(ss) * e_i_dx(i, ss, n) * e_i_dx(j, ss, n));
//
//        double integral = integral_01 + integral_12;
//
//
//        System.out.println("c1 " + integral2);
//        System.out.println("c2 " + integral + " " + integral_01 + " " + integral_12);

        return integral2;
    }

    //Основная функция для расчета информации
    public void calculate(int n) {
        //Создаем матрицу размером NxN, которая представляет систему линейных уравненией
        RealMatrix K_uv = new Array2DRowRealMatrix(n, n);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                K_uv.setEntry(i, j, F_ei_ej_x(i, j, 0.0, n));
            }
        }

        //Вычисляем вектор, содержащий правую часть
        RealVector F_v = new ArrayRealVector(n);
        for (int i = 0; i < n; i++) {
            F_v.setEntry(i, F_v_x(i, 0.0, n));
        }
        //Производим QR разложения, чтобы получить коэффициенты решения.
        RealVector w = new QRDecomposition(K_uv).getSolver().solve(F_v);
        double[] ws = w.toArray();

        //Создание и заполнения массивов xi, yi
        double acc = domain/n;
        yi = new double[ n + 2];
        xi = new double[ n + 2];
        double x = 0.0;
        int y = 0;
        while (x <= domain) {
            for (int i = 0; i < n; i++) {
                yi[y] += e_i(i, x, n) * ws[i];
            }
            xi[y] = x;
            y++;
            x += acc;
        }
    }

    //Вспомогательная функция для вычисления вектора Правой части
    private double F_v_x(int i, double x, int n) {
        return -10 * E(x) * e_i(i, x, n);
    }

    //Вспомогательная функция для создания матрицы жесткости
    private double F_ei_ej_x(int i, int j, double x, int n) {
        double integral = 0.0;
        if (Math.abs(j - i) <= 1) {
            integral = getIntegral(i, j, n);
        }
        return integral - E(x) * e_i(i, x, n) * e_i(j, x, n);
    }

    private static double E(double x) {
        if (x >= 0 && x <= 2.0) {
            return 3.0;
        } else throw new IllegalArgumentException("out of domain, x= " + x);
    }

    private static double e_i(int i, double x, int n) {
        double l = domain / n;
        double middle = l * i;
        double left_edge = l * (i - 1);
        double right_edge = l * (i + 1);
        if (x < left_edge || x > right_edge) {
            return new LinearFunction().getValue(x);
        }
        if (x >= middle) {
            return new LinearFunction(1, 0, middle, right_edge).getValue(x);
        } else {
            return new LinearFunction(0, 1, left_edge, middle).getValue(x);
        }
    }

    private static double e_i_dx(int i, double x, int n) {
        double l = domain / n;
        double middle = l * i;
        double left_edge = l * (i - 1);
        double right_edge = l * (i + 1);
        if (x < left_edge || x > right_edge) {
            return new LinearFunction().getDerivative();
        }
        if (x >= middle) {
            return new LinearFunction(1, 0, middle, right_edge).getDerivative();
        } else {
            return new LinearFunction(0, 1, left_edge, middle).getDerivative();
        }
    }
}
