package com.example;

import com.example.config.AppConfig;
import com.example.domain.LargeFile;
import com.example.service.LargeFileService;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.openjdk.jmh.annotations.Level.Iteration;

@SpringBootTest(classes = {AppConfig.class})
@ContextConfiguration(loader= AnnotationConfigContextLoader.class)
@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.SECONDS)
public class LargeFilesBenchmark {

    private static LargeFileService largeFileService;

    @Autowired
    public void setLargeFileService(LargeFileService largeFileService){
        LargeFilesBenchmark.largeFileService = largeFileService;
    }

    @Test
    public void benchmark() throws IOException, SQLException, RunnerException {
        Properties properties = PropertiesLoaderUtils.loadAllProperties("benchmark.properties");

        int warmup = Integer.parseInt(properties.getProperty("benchmark.warmup.iterations", "1"));
        int iterations = Integer.parseInt(properties.getProperty("benchmark.test.iterations", "1"));
        int threads = Integer.parseInt(properties.getProperty("benchmark.test.threads", "1"));
        String resultFilePrefix = properties.getProperty("benchmark.global.resultfileprefix", "jmh-");

        ResultFormatType resultsFileOutputType = ResultFormatType.CSV;

        Options opt = new OptionsBuilder()
                .include("\\." + this.getClass().getSimpleName() + "\\.")
                .warmupIterations(warmup)
                .measurementIterations(iterations)
                // single shot for each iteration:
                .warmupTime(TimeValue.NONE)
                .measurementTime(TimeValue.NONE)
                // do not use forking or the benchmark methods will not see references stored within its class
                .forks(0)
                .threads(threads)
                .shouldDoGC(true)
                .shouldFailOnError(true)
                .resultFormat(resultsFileOutputType)
                .result(buildResultsFileName(resultFilePrefix, resultsFileOutputType))
                .shouldFailOnError(true)
                .jvmArgs("-server")
                .build();

        new Runner(opt).run();

    }

    @Setup(value = Iteration)
    public void setup() throws IOException {
        csv();
    }


/*
    public void createExcel() throws IOException, SQLException, RunnerException {
        excel();
        save();
    }
*/

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    public void excel() {
        largeFileService.transformCSVtoExcel(1L);
    }


    public void csv() throws IOException {
        largeFileService.createLargeFile();
        largeFileService.createLargeFile("F://SRC_CODE//large-files//src//main//resources//output_csv_full.csv");
    }


    public void save() throws IOException, SQLException {
        LargeFile lf = largeFileService.getLargeFile(1L);
        DataOutputStream os = new DataOutputStream(new FileOutputStream("F://SRC_CODE//large-files//src//main//resources//out_sxssf.xlsx"));
        InputStream is = lf.getExcel().getBinaryStream();
        IOUtils.copyLarge(is, os);
    }

    private static String buildResultsFileName(String resultFilePrefix, ResultFormatType resultType) {
        LocalDateTime date = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("mm-dd-yyyy-hh-mm-ss");

        String suffix;
        switch (resultType) {
            case CSV:
                suffix = ".csv";
                break;
            case SCSV:
                // Semi-colon separated values
                suffix = ".scsv";
                break;
            case LATEX:
                suffix = ".tex";
                break;
            case JSON:
            default:
                suffix = ".json";
                break;

        }

        return String.format("target/%s%s%s", resultFilePrefix, date.format(formatter), suffix);
    }


}
