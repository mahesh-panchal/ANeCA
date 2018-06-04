public class ChiSq{
	/* Chi square functions from Cern*/

	protected static final double MACHEP =	1.11022302462515654042E-16;
	protected static final double MAXLOG =	7.09782712893383996732E2;
	protected static final double biginv =	2.22044604925031308085e-16;
	protected static final double big = 4.503599627370496e15;

	private ChiSq(){
	}

	public static double logGamma(double x) {
		// This is a quick approximate of log gamma.
		final double c0 = 9.1893853320467274e-01, c1 = 8.3333333333333333e-02,
			c2 =-2.7777777777777777e-03, c3 = 7.9365079365079365e-04,
			c4 =-5.9523809523809524e-04, c5 = 8.4175084175084175e-04,
			c6 =-1.9175269175269175e-03;
		double g,r,z;

		if (x <= 0.0) return -999;

		for (z = 1.0; x < 11.0; x++) z *= x;

		r = 1.0 / (x * x);
		g = c1 + r * (c2 + r * (c3 + r * (c4 + r * (c5 + r + c6))));
		g = (x - 0.5) * Math.log(x) - x + c0 + g / x;
		if (z == 1.0) return g;
		return g - Math.log(z);
	}

	public static double pdf(double x, double freedom) {
		if (x <= 0.0) throw new IllegalArgumentException();
		double logGamma = logGamma(freedom/2.0);
		return Math.exp((freedom/2.0 - 1.0) * Math.log(x/2.0) - x/2.0 - logGamma) / 2.0;
	}

	public static double cdf(double x, double freedom) throws ArithmeticException {
		if( x < 0.0 || freedom < 1.0 ) return 0.0;
		return incompleteGamma(x/2.0,freedom/2.0);
	}

	public static double incompleteGamma(double x, double a) throws ArithmeticException {
		double ans, ax, c, r;
		if( x <= 0 || a <= 0 ) return 0.0;
		if( x > 1.0 && x > a ) return 1.0 - incompleteGammaComplement(x,a);
	   /* Compute  x**a * exp(-x) / gamma(a)  */
		ax = a * Math.log(x) - x - logGamma(a);
		if( ax < -MAXLOG ) return( 0.0 );
		ax = Math.exp(ax);
		/* power series */
		r = a;
		c = 1.0;
		ans = 1.0;
		do {
			r += 1.0;
			c *= x/r;
			ans += c;
		} while( c/ans > MACHEP );
		return( ans * ax/a );
	 }

	 public static double incompleteGammaComplement(double x, double a ) throws ArithmeticException {
		double ans, ax, c, yc, r, t, y, z;
		double pk, pkm1, pkm2, qk, qkm1, qkm2;
		if( x <= 0 || a <= 0 ) return 1.0;
		if( x < 1.0 || x < a ) return 1.0 - incompleteGamma(x,a);
		ax = a * Math.log(x) - x - logGamma(a);
		if( ax < -MAXLOG ) return 0.0;
		ax = Math.exp(ax);
		/* continued fraction */
		y = 1.0 - a;
		z = x + y + 1.0;
		c = 0.0;
		pkm2 = 1.0;
		qkm2 = x;
		pkm1 = x + 1.0;
		qkm1 = z * x;
		ans = pkm1/qkm1;
		do {
			c += 1.0;
			y += 1.0;
			z += 2.0;
			yc = y * c;
			pk = pkm1 * z  -  pkm2 * yc;
			qk = qkm1 * z  -  qkm2 * yc;
			if( qk != 0 ) {
				r = pk/qk;
				t = Math.abs( (ans - r)/r );
				ans = r;
			} else {
				t = 1.0;
			}
			pkm2 = pkm1;
			pkm1 = pk;
			qkm2 = qkm1;
			qkm1 = qk;
			if( Math.abs(pk) > big ) {
				pkm2 *= biginv;
				pkm1 *= biginv;
				qkm2 *= biginv;
				qkm1 *= biginv;
			}
		} while( t > MACHEP );
		return ans * ax;
	}

}
