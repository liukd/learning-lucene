package com.liukd.index;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SearchUtil {

    // id
    private String[] ids = {"1", "2", "3", "4", "5", "6"};
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
    private String[] names = {"lhangsan", "lisi", "wangwl", "zhaoliu", "zhangzhang", "yingying"};
    // 邮件附件个数
    private int[] attaches = {2, 3, 4, 1, 6, 5};

    // 为文档加权重
    private Map<String, Float> scores = new HashMap<String, Float>();

    private Directory directory;
    private IndexReader indexReader;

    public SearchUtil() {
        this.directory = new RAMDirectory();
        index();
    }

    public IndexSearcher getIndexSearch(){

        try {
            if(indexReader == null){
                indexReader = IndexReader.open(directory);
            }else{
                IndexReader ir = IndexReader.openIfChanged(indexReader);
                if(ir != null){
                    indexReader = ir;
                }
            }
            return new IndexSearcher(indexReader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void index(){
        IndexWriter indexWriter = null;
        try {
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_36, new StandardAnalyzer(Version.LUCENE_36));
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
                doc.add(new NumericField("attach", Field.Store.YES, true).setIntValue(attaches[i]));

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


    private void search (Query query, int num){
        IndexSearcher indexSearcher = getIndexSearch();
        try {
            TopDocs topDocs = indexSearcher.search(query, num);
            System.out.println("一共查询到了 " + topDocs.totalHits);
            for(ScoreDoc sd : topDocs.scoreDocs){
                Document document = indexSearcher.doc(sd.doc);
                System.out.println(document.get("name") + ": [ " + document.get("email") + " ]");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(indexSearcher != null){
                try {
                    indexSearcher.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }




    /**
     * 精确匹配查询
     * @param field
     * @param value
     * @param num
     *
     * @注意：总共查询到的值和参数num没有关系的
     */
    public void searchByTerm(String field, String value, int num){
        Query query = new TermQuery(new Term(field, value));
        search(query, 10);
    }

    /**
     * 范围查找
     * @param field
     * @param lowerTerm
     * @param upperTerm
     * @param includeLower
     * @param includeUpper
     */
    public void searchByTermRange(String field, String lowerTerm, String upperTerm, boolean includeLower, boolean includeUpper){
        TermRangeQuery query = new TermRangeQuery(field, lowerTerm, upperTerm, includeLower, includeUpper);
        search(query, 10);
    }


    /**
     * 范围查询（数字类型）
     * @param field
     * @param min
     * @param max
     * @param minInclusive
     * @param maxInclusive
     * @param num
     */
    public void searchByNumericRange(String field,Integer min, Integer max, boolean minInclusive, final boolean maxInclusive, int num){
        NumericRangeQuery numericRangeQuery = NumericRangeQuery.newIntRange(field, min, max, minInclusive, maxInclusive);
        search(numericRangeQuery, 10);
    }


    /**
     * 前缀查找
     * @param field
     * @param prefix
     */
    public void searchByPrefixQuery(String field, String prefix){
        PrefixQuery query = new PrefixQuery(new Term(field, prefix));
        search(query, 10);
    }

    /**
     * 通配符查询
     * @param field
     * @param express
     */
    public void searchByWildcard(String field, String express){
        WildcardQuery query = new WildcardQuery(new Term(field, express));
        search(query, 10);
    }

    /**
     * 多条件查询，条件之间可以使用与或非
     */
    public void searchByBoolean(){
        BooleanQuery query = new BooleanQuery();
        WildcardQuery query1 = new WildcardQuery(new Term("name", "*a*"));
        NumericRangeQuery query2 = NumericRangeQuery.newIntRange("attach", 3, 4, true, true);
        query.add(query1, BooleanClause.Occur.MUST);
        query.add(query2, BooleanClause.Occur.MUST_NOT);
        search(query, 10);
    }

    /**
     * 短语查询
     */
    public void searchByPhrase(){

    }

    /**
     * 模糊查询，允许匹配有一个不匹配的, 可以调节相似度
     */
    public void searchByFuzzyQuery(){
        FuzzyQuery query = new FuzzyQuery(new Term("name", "zhangzhan"));
        search(query, 10);


    }







}
