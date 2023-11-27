package com.example;

import com.example.service.LargeFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@SpringBootApplication(scanBasePackages = {"com.example.config"})
@ComponentScan(basePackages = {"com.example.service"})
public class LargeFilesApplication {

	@Autowired
	private LargeFileService largeFileService;

	public static void main(String[] args) throws IOException {
		SpringApplication.run(LargeFilesApplication.class, args);
		LargeFilesApplication f = new LargeFilesApplication();
		//f.processInputFile("F://SRC_CODE//large-files//src//main//resources//output_csv_full.csv");
		f.callService();
	}

	public void callService() throws IOException {
		largeFileService.createLargeFile("F://SRC_CODE//large-files//src//main//resources//output_csv_full.csv");
	}

	private List<YourJavaItem> processInputFile(String inputFilePath) {

		List<YourJavaItem> inputList = new ArrayList<YourJavaItem>();

		try{

			File inputF = new File(inputFilePath);
			InputStream inputFS = new FileInputStream(inputF);
			BufferedReader br = new BufferedReader(new InputStreamReader(inputFS));

			// skip the header of the csv
			inputList = br.lines().skip(1).map(mapToItem).collect(Collectors.toList());
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
     	}

		return inputList ;
	}

	private Function<String, YourJavaItem> mapToItem = (line) -> {

		String[] p = line.split(",");// a CSV has comma separated lines

		YourJavaItem item = new YourJavaItem();

		item.setItemNumber(p[0]);//<-- this is the first column in the csv file
		if (p[3] != null && p[3].trim().length() > 0) {
			item.setSomeProeprty(p[3]);
		}
		//more initialization goes here

		return item;
	};
}

class YourJavaItem {

	public void setItemNumber(String s) {
	}

	public void setSomeProeprty(String s) {
	}
}