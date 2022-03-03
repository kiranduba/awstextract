package com.stanchartered.textract;

import java.awt.image.BufferedImage;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.textract.AmazonTextract;
import com.amazonaws.services.textract.AmazonTextractClientBuilder;
import com.amazonaws.services.textract.model.DetectDocumentTextRequest;
import com.amazonaws.services.textract.model.DetectDocumentTextResult;
import com.amazonaws.services.textract.model.Document;
import com.amazonaws.services.textract.model.S3Object;

public class ImageTextParser extends JPanel {

	private static final long serialVersionUID = 1L;

	BufferedImage image;
	DetectDocumentTextResult result;

	public ImageTextParser(DetectDocumentTextResult documentResult, BufferedImage bufImage) throws Exception {
		super();

		result = documentResult; // Results of text detection.
		image = bufImage; // The image containing the document.

	}
	
	public static void main(String arg[]) throws Exception {

		try {
			// The S3 bucket and document
			String document = "Youareawesome.png"; // file which needs to be parsed
			String bucket = "stancbucket";   // bucket needs to be created with public access

			AWSCredentials credentials = new BasicAWSCredentials("XXXXXXXXXXXXXXXXXXXXXXXX",
					"XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");

			AmazonS3 s3client = AmazonS3ClientBuilder.standard()
					.withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(Regions.US_EAST_1)  // S3 bucket kept at US EAST location
					.build();

			List<Bucket> buckets = s3client.listBuckets();
			for (Bucket bucketname : buckets) {
				System.out.println(bucketname.getName());
			}

			// Get the document from S3
			com.amazonaws.services.s3.model.S3Object s3object = s3client.getObject(bucket, document);
			S3ObjectInputStream inputStream = s3object.getObjectContent();
			BufferedImage image = ImageIO.read(inputStream);

			// Call DetectDocumentText
			EndpointConfiguration endpoint = new EndpointConfiguration("https://textract.us-east-1.amazonaws.com",
					"us-east-1"); // This service is used like a SaaS

			AmazonTextract client = AmazonTextractClientBuilder.standard().withEndpointConfiguration(endpoint)
					.withCredentials(new AWSStaticCredentialsProvider(credentials)).build();

			DetectDocumentTextRequest request = new DetectDocumentTextRequest()
					.withDocument(new Document().withS3Object(new S3Object().withName(document).withBucket(bucket)));

			DetectDocumentTextResult result = client.detectDocumentText(request);

			System.out.println(result);

			result.getBlocks().forEach(block -> {
				if (block.getBlockType().equals("LINE")) {
					System.out.println("Text is : " + block.getText());  // Test to get all the text parsed from image
				}
			});

		} catch (Exception ex) {
			System.out.println(ex);
			throw ex;
		}

	}
}
