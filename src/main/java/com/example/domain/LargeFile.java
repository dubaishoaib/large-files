package com.example.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Blob;

@Entity
@Table(name = "LARGE_FILE")
@Getter
@Setter
@NoArgsConstructor
public class LargeFile {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Lob
    private Blob csv;

    @Lob
    private Blob excel;
}
