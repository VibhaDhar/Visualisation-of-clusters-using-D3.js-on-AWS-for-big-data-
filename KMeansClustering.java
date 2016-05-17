import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONObject;

import weka.clusterers.SimpleKMeans;
import weka.core.Instances;

public class KMeansClustering extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		// TODO Auto-generated method stub
		SimpleKMeans kmeans = new SimpleKMeans();
		int clusterNo = Integer.parseInt(req.getParameter("clusters"));
		JSONObject json = new JSONObject();
	    JSONArray toplevel = new JSONArray();
	    JSONObject sublevel;
	    

		try {
			kmeans.setNumClusters(clusterNo);
			AWSCredentials credentials = new BasicAWSCredentials("AKIAI3EDWIHLI7GM5UXQ","4BjJGCU4VbHL66wPpwx+FvqaIS6YXOUySgatchFG");
			AmazonS3 s3 = new AmazonS3Client(credentials);
			AmazonS3Client s3Client = new AmazonS3Client(credentials);			
			S3Object obj = s3Client.getObject("vibhabuck", "earthquake.csv.arff");
			File file = stream2file(obj.getObjectContent());
			
			BufferedReader datafile = new BufferedReader(new FileReader(file));
			Instances data = new Instances(datafile);
			kmeans.buildClusterer(data);
			Instances centroids = kmeans.getClusterCentroids();
		/*	for (int i = 0; i < centroids.numInstances(); i++) {
				System.out.println("Centroid " + i + 1 + ": "
						+ centroids.instance(i));
			}
		*/	// get cluster membership for each instance
			for (int i = 0; i < data.numInstances(); i++) {
				String[] latLong = data.instance(i).toString().split(",");
		    	String lat = latLong[0];
		    	String longs = latLong[1];
		      sublevel = new JSONObject();
		      sublevel.put("latitude", lat);
		      sublevel.put("longitude", longs);
		      sublevel.put("clusterId",String.valueOf(kmeans.clusterInstance(data.instance(i))));
		      toplevel.put(sublevel);
				System.out.println(data.instance(i) + " is in cluster "	+ kmeans.clusterInstance(data.instance(i)) + 1);

			}			
			req.setAttribute("jsonString", toplevel.toString());
			RequestDispatcher rd = req.getRequestDispatcher("/response.jsp");
			rd.forward(req, resp);
		} catch (Exception e) {
			PrintWriter writer = resp.getWriter();
			// TODO Auto-generated catch block
			writer.print(e.getMessage());
			e.printStackTrace();
		}
	}
	public static File stream2file(InputStream inputStream) throws IOException {
		File file = new File("file.tmp");
		OutputStream outputStream = new FileOutputStream(file);
		int read = 0;
		byte[] bytes = new byte[1024];
		while ((read = inputStream.read(bytes)) != -1) {
			outputStream.write(bytes, 0, read);			
		}
		outputStream.close();
		return file;
	}
}
