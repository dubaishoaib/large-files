package com.example;

import com.example.config.AppConfig;
import com.example.domain.LargeFile;
import com.example.service.LargeFileService;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.runner.RunnerException;
import org.simpleflatmapper.csv.CsvParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.*;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@SpringBootTest(classes = {AppConfig.class})
@ContextConfiguration(loader= AnnotationConfigContextLoader.class)
public class LargeFilesApplicationTests {

	@Autowired
	private LargeFileService largeFileService;

	@Test
	void testLargeFile() throws IOException, SQLException {
		largeFileService.createLargeFile();
		largeFileService.createLargeFile("F://SRC_CODE//large-files//src//main//resources//output_csv_full.csv");
		LargeFile lf = largeFileService.getLargeFile(1L);
		//DataOutputStream os = new DataOutputStream(new FileOutputStream("F://SRC_CODE//large-files//src//main//resources//1.csv"));
		InputStream is = lf.getCsv().getBinaryStream();
		//IOUtils.copyLarge(is, os);

		Reader targetReader = new InputStreamReader(is);

		SXSSFWorkbook wb = new SXSSFWorkbook(100); // keep 100 rows in memory, exceeding rows will be flushed to disk
		wb.setCompressTempFiles(true);
		Sheet sh = wb.createSheet();
		AtomicInteger rowCount = new AtomicInteger(0);
		try (Stream<String[]> stream = CsvParser.stream(targetReader)) {
			stream.forEachOrdered(row -> {
				//System.out.println(Arrays.toString(row));
				Row r = sh.createRow(rowCount.get());
				AtomicInteger cellCount = new AtomicInteger(0);
				Arrays.stream(row).forEachOrdered(c -> {
					Cell cell = r.createCell(cellCount.get());
					cell.setCellValue(c);
					cellCount.getAndIncrement();
				});
				rowCount.getAndIncrement();
			});
		}
		FileOutputStream out = new FileOutputStream("F://SRC_CODE//large-files//src//main//resources//sxssf.xlsx");
		wb.write(out);
		out.close();
	}

	@Test
	public void createExcel() throws IOException, SQLException, RunnerException {
		csv();
		excel();
		save();
	}

	public void csv() throws IOException {
		largeFileService.createLargeFile();
		largeFileService.createLargeFile("F://SRC_CODE//large-files//src//main//resources//output_csv_full.csv");
	}

	public void excel() {
		largeFileService.transformCSVtoExcel(1L);
	}

	public void save() throws IOException, SQLException {
		LargeFile lf = largeFileService.getLargeFile(1L);
		DataOutputStream os = new DataOutputStream(new FileOutputStream("F://SRC_CODE//large-files//src//main//resources//out_sxssf.xlsx"));
		InputStream is = lf.getExcel().getBinaryStream();
		IOUtils.copyLarge(is, os);
	}
}
