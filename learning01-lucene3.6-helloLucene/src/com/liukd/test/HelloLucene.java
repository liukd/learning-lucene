package com.liukd.test;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class HelloLucene {

    /**
     * 建立索引
     */
    public void index() {

        IndexWriter indexWriter = null;
        try {
            // 1. 创建Directory
//        Directory directory = new RAMDirectory(); // 索引是建立在内存中的
            Directory directory = FSDirectory.open(new File("E:\\lucene\\index01"));
            // 2. 创建IndexWriter
            Version matchVersion = Version.LUCENE_36;
            Analyzer analyzer = new StandardAnalyzer(matchVersion);
            IndexWriterConfig iwc = new IndexWriterConfig(matchVersion, analyzer);
            indexWriter = new IndexWriter(directory, iwc);
            // 3. 创建Document对象
            Document document = null;
            // 4. 为Document添加Field
            File f = new File("E:\\lucene\\example");
            for (File file : f.listFiles()){
                document = new Document();
                document.add(new Field("content", new FileReader(file)));
                document.add(new Field("filename", file.getName(), Field.Store.YES, Field.Index.NOT_ANALYZED));
                document.add(new Field("path", file.getAbsolutePath(), Field.Store.YES, Field.Index.NOT_ANALYZED));
                // 5. 通过IndexWriter添加文档到索引中
                indexWriter.addDocument(document);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(indexWriter != null){
                try {
                    indexWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    /**
     * 搜索
     * 增量索引，导致每次创建索引后，搜索的时候回查询重复
     */
    public void search(){

        try {
            // 1. 创建Directory
            Directory directory = FSDirectory.open(new File("E:\\lucene\\index01"));
            // 2. 创建IndexReader
            IndexReader indexReader = IndexReader.open(directory);
            // 3. 根据IndexReader创建IndexSearcher
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            // 4. 创建搜索的Query
            // 创建parser来确定要搜索文件的内容，第二参数表示搜索的域
            QueryParser queryParser = new QueryParser(Version.LUCENE_36, "content", new StandardAnalyzer(Version.LUCENE_36));
            // 创建query， 表示搜索域为content中包含java的文档
            Query query = queryParser.parse("liukedong");
            // 5. 根据searcher搜索并且返回TopDocs
            TopDocs topDocs = indexSearcher.search(query, 10);
            // 6. 根据TopDocs获取ScoreDoc对象
            ScoreDoc[] arrScoreDoc = topDocs.scoreDocs;

            for(ScoreDoc sd : arrScoreDoc){
                // 7. 根据searcher对象和ScoreDoc获取具体的Document对象
                Document d = indexSearcher.doc(sd.doc);
                // 8. 根据Docuement获取需要的值
                System.out.println(d.get("filename") + "[ " + d.get("path") + " ]");
            }

            // 9. 关闭IndexReader
            indexReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }


    }

}
