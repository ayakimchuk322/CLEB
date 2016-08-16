package cleb.book;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * This class represents a single book entity in a database.
 */
@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "file_name")
    private String fileName;
    @Column(name = "md5sum")
    private String md5;
    @Column(name = "file_size")
    private long fileSize;
    @Column(name = "file_type")
    private String fileType;

    @Column(name = "genre")
    private String genre;
    @Column(name = "author_first_name")
    private String authorFirstName;
    @Column(name = "author_last_name")
    private String authorLastName;
    @Column(name = "title")
    private String title;
    @Column(name = "seq_name")
    private String seqName;
    @Column(name = "seq_number")
    private String seqNumber;
    @Column(name = "published")
    private String published;

    @Column(name = "uploaded_by")
    private String uploadedBy;

    public Book() {

    }

    public Book(String fileName, String md5, long fileSize, String fileType,
            String genre, String authorFirstName, String authorLastName,
            String title, String seqName, String seqNumber, String published,
            String uploadedBy) {
        this.fileName = fileName;
        this.md5 = md5;
        this.fileSize = fileSize;
        this.fileType = fileType;
        this.genre = genre;
        this.authorFirstName = authorFirstName;
        this.authorLastName = authorLastName;
        this.title = title;
        this.seqName = seqName;
        this.seqNumber = seqNumber;
        this.published = published;
        this.uploadedBy = uploadedBy;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getAuthorFirstName() {
        return authorFirstName;
    }

    public void setAuthorFirstName(String authorFirstName) {
        this.authorFirstName = authorFirstName;
    }

    public String getAuthorLastName() {
        return authorLastName;
    }

    public void setAuthorLastName(String authorLastName) {
        this.authorLastName = authorLastName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSeqName() {
        return seqName;
    }

    public void setSeqName(String seqName) {
        this.seqName = seqName;
    }

    public String getSeqNumber() {
        return seqNumber;
    }

    public void setSeqNumber(String seqNumber) {
        this.seqNumber = seqNumber;
    }

    public String getPublished() {
        return published;
    }

    public void setPublished(String published) {
        this.published = published;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

}
