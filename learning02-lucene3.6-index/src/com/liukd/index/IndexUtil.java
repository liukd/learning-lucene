package com.liukd.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class IndexUtil {

    // id
    private String[] ids = {"1", "2", "3,", "4", "5", "6"};
    // 发件人邮箱
    private String[] emails = {
            "kedong_liu@sina.com", "liukd1@yohu.com", "liukd1@126.com.cn",
            "zhangfy@zoho.com", "zhangfy@163.com", "zhangfy@yusys.com.cn"
    };
    // 邮件正文
    private String[] contents = {
            "本次冬公开冬课着重使用OpenLiberty来开发和部署移冬动应用时一些有用的具体实践，除了一些通常应用程序都需要面对的普遍问题，如OSGi内核高模块、高动态性的轻量级应用服务器的优化和",
            "IBM混合云产品经理，2003年加冬入IBM中国软件研发中心，先后担任高级工程师，开发经理及产品经理，专注领域包括应用服务器，Java EE技术及云计算。",
            "您正在登录备冬案系统，验证码是 257959，请于 30分钟 内在页面输入，工作人员不会索取，请勿泄漏。 更多备案问题请查看备案帮助",
            "您好！您申请的报销冬单（报销单号：20171120132550746）已通过业务审批，请您登陆系统到报销单详细页面导出PDF文件并打印，及时递交财务审核，谢谢！",
            "各位好 （11-20）的冬销售市场会议将于上午10:30进行，请各位准备好资料参会，谢谢。",
            "您好，请您尽快重冬新提交有效期内的身份证电子版复印件（正反面复印到一张纸中，1:1复印）用于个人档案留存。 发送至邮箱：shengyd@yusys.com.cn    标题&扫描件文件名为：015853刘可冬身份证新。谢谢。"
    };
    // 发件人姓名
    private String[] names = {"zhangsan", "lisi", "wangwu", "zhaoliu", "zhangzhang", "yingying"};
    // 邮件附件个数
    private int[] attaches = {2, 3, 4, 1, 6, 5};

    // 为文档加权重
    private Map<String, Float> scores = new HashMap<String, Float>();


    Directory directory;
    Version matchVersion;

    public IndexUtil() {
        try {

            scores.put("yusys.com.cn", 2.0F);
            scores.put("163.com", 1.5F);
            this.directory = FSDirectory.open(new File("E:\\lucene\\index02"));
            this.matchVersion = Version.LUCENE_36;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化索引
     */
    public void index(){
        IndexWriter indexWriter = null;
        try {
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(matchVersion, new StandardAnalyzer(matchVersion));
            indexWriter = new IndexWriter(directory, indexWriterConfig);
            // 初始化索引的时候先将索引删除
            indexWriter.deleteAll();
            Document doc = null;
            for (int i = 0; i < ids.length; i++) {
                doc = new Document();
                doc.add(new Field("id", ids[i], Field.Store.YES, Field.Index.NOT_ANALYZED));
                doc.add(new Field("email", emails[i], Field.Store.YES, Field.Index.NOT_ANALYZED));
                doc.add(new Field("content", contents[i], Field.Store.NO, Field.Index.ANALYZED));
                doc.add(new Field("name", names[i], Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
                String suffix = emails[i].split("@")[1];
                if(scores.containsKey(suffix)){
                    doc.setBoost(scores.get(suffix));
                }else{
                    doc.setBoost(1.0F);
                }
                indexWriter.addDocument(doc);
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
     * 查询索引中文档个数
     */
    public void query(){
        IndexReader indexReader = null;
        try {
            indexReader = IndexReader.open(directory);
            System.out.println("目前索引目录中的文档数目： " + indexReader.numDocs());
            System.out.println("目前索引目录中的最大文档数目： " + indexReader.maxDoc());
            System.out.println("目前索引目录中的回收站中的文档数目： " + indexReader.numDeletedDocs());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(indexReader != null){
                try {
                    indexReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 搜索
     */
    public void search(){
        IndexReader indexReader = null;
        try {
            indexReader = IndexReader.open(directory);

            IndexSearcher indexSearcher = new IndexSearcher(indexReader);

            QueryParser queryParser = new QueryParser(matchVersion, "content", new StandardAnalyzer(matchVersion));
            Query query = queryParser.parse("冬");
            TopDocs topDocs = indexSearcher.search(query, 10);
            ScoreDoc[] scoreDocs = topDocs.scoreDocs;
            for(ScoreDoc sd : scoreDocs){
                Document document = indexSearcher.doc(sd.doc);
                System.out.println(document.get("name") + ": [ " + document.get("email") + " ]");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            if(indexReader != null){
                try {
                    indexReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 删除索引
     */
    public void delete(){
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(matchVersion, new StandardAnalyzer(matchVersion));
        IndexWriter indexWriter = null;
        try {
            indexWriter = new IndexWriter(directory, indexWriterConfig);
            Term term = new Term("id", "6");
            indexWriter.deleteDocuments(term);
            indexWriter.commit();
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
     * 使用IndexReader可以恢复回收站中的文档
     */
    public void undelete(){

        IndexReader indexReader = null;
        try {
            indexReader = IndexReader.open(directory, false);
            // 使用indexReader恢复数据，必须把open的readonly设置为false
            indexReader.undeleteAll();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(indexReader != null){
                try {
                    indexReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }



}
