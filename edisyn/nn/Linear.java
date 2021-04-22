package edisyn.nn;
public class Linear implements Layer {

	private double[] data;
	private double[] bias;
	private int rows;
	private int columns;

	public Linear(int rows, int columns, double[] initial_data, double[] bias){
		this.rows = rows;
		this.columns = columns;
		data = new double[rows*columns];
		for(int i = 0; i < initial_data.length; i++){
			data[i] = initial_data[i];
		}
		this.bias = new double[rows];
		for(int i = 0; i < bias.length; i++){
			this.bias[i] = bias[i];
		}
	}

	public double[] feed(double[] vec){
		if(vec.length != columns){
			System.err.println("Bad input to feed: vec.length != columns");
			return null;
		}
		double[] out = new double[rows];
		for(int r = 0; r < rows; r++) {
			for(int c = 0; c < columns; c++) {
				out[r] += data[r*columns + c] * vec[c];
			}
			out[r] += bias[r];
		}
		return out;
	}

	public static Layer readFromString(String str){
		String[] strs = str.split(" ");
		int rows = Integer.parseInt(strs[1]);
		int columns = Integer.parseInt(strs[2]);
		double[] weights = new double[rows*columns];
		double[] bias = new double[rows];
		for(int i = 3; i < rows + 3; i++){
			bias[i-3] = Double.parseDouble(strs[i]);
		}
		for(int i = rows + 3; i < strs.length; i++){
			weights[i-3 - rows] = Double.parseDouble(strs[i]);
		}
		return new Linear(rows, columns, weights, bias);
	}
    
}
