package com.example.service;

import com.example.domain.LargeFile;
import com.example.repo.LargeFileRepository;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.simpleflatmapper.csv.CsvParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.rowset.serial.SerialBlob;
import javax.transaction.Transactional;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Service
@Transactional
public class LargeFileService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private LargeFileRepository largeFileRepository;

    public Long createLargeFile() {
        LargeFile l = new LargeFile();
        l.setId(1L);
        largeFileRepository.save(l);
        return l.getId();
    }

    public LargeFile getLargeFile(Long id) {
        return largeFileRepository.findById(id).get();
    }

    public void createLargeFile(final String fileName) throws IOException {
        System.out.println("saving csv to db...");
        Session session = (Session) entityManager.getDelegate(); // Hibernate session
        session.doWork(new Work() {
            @Override
            public void execute(Connection connection) throws SQLException {
                String updateSql = "UPDATE LARGE_FILE SET csv = ? WHERE ID = 1";
                File inputF = new File(fileName);

                try (PreparedStatement preparedStatement = connection.prepareStatement(updateSql);
                     InputStream inputStream = new FileInputStream(inputF)) {
                    preparedStatement.setBlob(1, inputStream);
                    preparedStatement.executeUpdate();
                    connection.commit();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void transformCSVtoExcel(final Long id) {
        System.out.println("saving excel to db...");
        Session session = (Session) entityManager.getDelegate(); // Hibernate session
        session.doWork(new Work() {
            @SneakyThrows
            @Override
            public void execute(Connection connection) throws SQLException {
                String QUERY_STATEMENT = "SELECT csv FROM LARGE_FILE WHERE id = 1";
                PreparedStatement preparedStatement = connection.prepareStatement(QUERY_STATEMENT);
                ResultSet rs = preparedStatement.executeQuery();
                rs.next();
                InputStream inStream = rs.getBinaryStream(1);

                Reader targetReader = new InputStreamReader(inStream);

                SXSSFWorkbook wb = new SXSSFWorkbook(); // keep 100 rows in memory, exceeding rows will be flushed to disk
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

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                wb.write(baos);

                ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                String UPDATE_STATEMENT = "UPDATE LARGE_FILE SET excel = ? WHERE ID = 1";
                PreparedStatement updateStatement = connection.prepareStatement(UPDATE_STATEMENT);
                updateStatement.setBinaryStream(1, bais, baos.size());
                updateStatement.executeUpdate();
                connection.commit();
                bais.close();
                baos.close();
            }
        });
    }

    private void readBlob() {
        Session session = (Session) entityManager.getDelegate(); // hibernate session
        session.doWork(new Work() {
            @Override
            public void execute(Connection connection) throws SQLException {
                try {
                    String QUERY_STATEMENT = "SELECT * FROM LARGE_FILE WHERE id= ?";
                    PreparedStatement preparedStatement = connection.prepareStatement(QUERY_STATEMENT);
                    preparedStatement.setLong(1, new Long(1));
                    ResultSet rs = preparedStatement.executeQuery();

                    while (((ResultSet) rs).next()) {
                        String fileName = ((ResultSet) rs).getString("FILE_NAME");
                        FileOutputStream outStream = new FileOutputStream(fileName);
                        InputStream inStream = ((ResultSet) rs).getBinaryStream("CONTENT");
                        try {
                            IOUtils.copy(inStream, outStream);
                        } catch (Exception exc) {
                            exc.printStackTrace();
                        } finally {
                            IOUtils.closeQuietly(outStream);
                            IOUtils.closeQuietly(inStream);
                        }
                    }
                } catch (Exception exc) {
                    exc.printStackTrace();
                }
            }
        });
    }

}
